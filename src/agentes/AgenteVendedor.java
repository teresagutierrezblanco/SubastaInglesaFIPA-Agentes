/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes;

import jade.content.Predicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import ontologia.*;

/**
 *
 * @author teres
 */
public class AgenteVendedor extends Agent {

    private InterfazV gui;
    private HashMap<Integer, Puja> libros;
    private AID[] compradores;
    private Puja libroActual;
    private Codec codec;
    private Ontology onto;

    protected void setup() {
        this.libros = new HashMap<>();

        gui = new InterfazV(this);
        gui.setVisible(true);
        codec = new SLCodec();
        onto = SubastaOntology.getInstance();
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(onto);
//       
    }

    public void anadirLibro(String titulo, Integer precio, Integer intervalo) {
        Integer id = libros.size();
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                Libro l= new Libro();
                l.setId(id);
                l.setTitulo(titulo);
                Puja p = new Puja(l, precio, intervalo);
                libros.put(id, p);
                System.out.println(titulo + " inserted into catalogue. Price = " + precio);
                gui.actualizarTabla(p);
            }
        });
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {

                if (libros.get(id).isActiva()) {
                    System.out.println("Buscando comprador para " + titulo);
                    // Update the list of seller agents
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("comprador");
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Compradores conectados:  ");
                        compradores = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            compradores[i] = result[i].getName();
                            System.out.println(compradores[i].getName());
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }

                    // Perform the request
                    myAgent.addBehaviour(new RequestPerformer());
                    libroActual = libros.get(id);
                } else {
                    System.out.println("Puja del libro " + libros.get(id).getL().getTitulo() + " con id: " + id + " finalizada");
                    myAgent.removeBehaviour(this);
                }
            }
        });

    }
     // Put agent clean-up operations here
    protected void takeDown() {
        

        // Printout a dismissal message
        System.out.println( getAID().getName() + " terminating.");
        this.gui.dispose();
        
    }

    private class RequestPerformer extends Behaviour {

       
        private int nPujadores = 0; // The counter of replies from seller agents
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;
        private int aceptan = 0;
        private int primero = 0;
        private ArrayList<AID> perdedores = new ArrayList<>();

        private Ofertar of = new Ofertar();
        private Oferta o = new Oferta();

                            

        public void action() {
            o.setProducto(libroActual.getL());
            switch (step) {
                case 0: //mando a ver quien quiere este libro
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    System.out.println("Precio: " + libroActual.getPrecioActual());
                    cfp.setOntology(onto.getName());
                    cfp.setLanguage(codec.getName());
                    if (compradores.length > 0) {
                        for (int i = 0; i < compradores.length; ++i) {
                            cfp.addReceiver(compradores[i]);
                        }
                        try {

                            o.setPrecio(libroActual.getPrecioActual());
                            of.setOferta(o);
                            getContentManager().fillContent(cfp, new Action(getAID(), of));
                        } catch (Codec.CodecException | OntologyException e) {
                            e.printStackTrace();
                        }

                        cfp.setConversationId("puja");
                        cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                        myAgent.send(cfp);
                        // Prepare the template to get proposals
                        mt = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchOntology(onto.getName()), MessageTemplate.MatchLanguage(codec.getName())), MessageTemplate.and(MessageTemplate.MatchConversationId("puja"),
                                MessageTemplate.MatchInReplyTo(cfp.getReplyWith())));
                        step = 1;
                    } else {
                        step = 3;
                    }
                    break;

                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);

                    if (reply != null) {
                        try {
                            // Reply received
                            Action a = (Action) getContentManager().extractContent(reply);
                            Responder r = (Responder) a.getAction();
                            if (r.getParticipo()) {
                                aceptan += 1;
                                System.out.println(reply.getSender().getName() + " interesado");
                            } else {

                                System.out.println(reply.getSender().getName() + " no interesado");
                            }
                            if (r.getParticipo() && primero == 0) {
                                // This is an offer
                                System.out.println("gana " + reply.getSender().getName());
                                primero = 1;
                                libroActual.setGanadorRonda(reply.getSender());

                            } else {
                                perdedores.add(reply.getSender());

                            }
                            nPujadores++;
                            if (nPujadores >= compradores.length) {
                                // We received all replies
                                step = 2;
                            }
                        } catch (Codec.CodecException ex) {
                            Logger.getLogger(AgenteVendedor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (OntologyException ex) {
                            Logger.getLogger(AgenteVendedor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        block();
                    }
                    break;

                case 2:
                    // Send the purchase order to the seller that provided the best offer
                    if (aceptan == 1 || (aceptan == 0 && libroActual.getGanadorRonda() != null)) {
                        // This is the best offer at present

                        ACLMessage order = new ACLMessage(ACLMessage.REQUEST);
                        order.setOntology(onto.getName());
                        order.setLanguage(codec.getName());
                        order.addReceiver(libroActual.getGanadorRonda());
                        Integer precio;
                        if (aceptan == 0) {
                            precio = libroActual.getPrecioActual() - libroActual.getIntervalo();

                        } else {
                            precio = libroActual.getPrecioActual();

                        }
                        try {
                            o.setPrecio(precio);
                            o.setProducto(libroActual.getL());
                            of.setOferta(o);
                            getContentManager().fillContent(order, new Action(getAID(), of));
                        } catch (Codec.CodecException ex) {
                            Logger.getLogger(AgenteVendedor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (OntologyException ex) {
                            Logger.getLogger(AgenteVendedor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        libros.get(libroActual.getL().getId()).setActiva(false);
                        order.setConversationId("puja");
                        order.setReplyWith("order" + System.currentTimeMillis());
                        myAgent.send(order);
                        libroActual.setPrecioActual(precio);
                        gui.actualizarAnuncios(libroActual);
                        System.out.println(libroActual.getL().getTitulo() + " successfully purchased by agent " + libroActual.getGanadorRonda().getName() + "por:" + libroActual.getPrecioActual());
                        ACLMessage orden = new ACLMessage(ACLMessage.INFORM);
                        orden.setOntology(onto.getName());
                        orden.setLanguage(codec.getName());
                        for (int i = 0; i < perdedores.size(); i++) {
                            if (!perdedores.get(i).getName().equals(libroActual.getGanadorRonda().getName())) {
                                orden.addReceiver(perdedores.get(i));
                            }

                        }
                        try {
                            Ganar g = new Ganar();
                            
                            o.setPrecio(precio);
                            o.setProducto(libroActual.getL());
                            g.setPuja(o);
                            g.setGanador(libroActual.getGanadorRonda().getName());
                            getContentManager().fillContent(orden, g);
                        } catch (Codec.CodecException ex) {
                            Logger.getLogger(AgenteVendedor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (OntologyException ex) {
                            Logger.getLogger(AgenteVendedor.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        orden.setConversationId("puja");
                        myAgent.send(orden);

                    } else if (aceptan > 1) {

                        gui.actualizarTabla(libroActual);
                        // This is the best offer at present
                        ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        order.setOntology(onto.getName());
                        order.setLanguage(codec.getName());
                        order.addReceiver(libroActual.getGanadorRonda());
                        try {
                            
                            o.setPrecio(libroActual.getPrecioActual());
                            
                            o.setProducto(libroActual.getL());
                            of.setOferta(o);
                            getContentManager().fillContent(order, new Action(getAID(), of));
                        } catch (Codec.CodecException ex) {
                            Logger.getLogger(AgenteVendedor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (OntologyException ex) {
                            Logger.getLogger(AgenteVendedor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        order.setConversationId("puja");
                        //order.setReplyWith("order" + System.currentTimeMillis());
                        myAgent.send(order);
                        // Prepare the template to get the purchase order reply
//             
                        ACLMessage orden = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                        for (int i = 0; i < perdedores.size(); i++) {

                            orden.addReceiver(perdedores.get(i));

                        }

                        orden.setConversationId("puja");
                        myAgent.send(orden);
                        libros.get(libroActual.getL().getId()).setPrecioActual(libroActual.getPrecioActual() + libroActual.getIntervalo());
                        System.out.println("precio actualizado");
                    }
                    step = 3;
                    break;

            }
        }

        public boolean done() {
//            if (step == 2 && bestSeller == null) {
//                System.out.println("Attempt failed: " + targetBookTitle + " not available for sale");
//            }
//            return ((step == 2 && bestSeller == null) || step == 4);
            if (step == 3) {
                return true;
            } else {
                return false;
            }
        }
    }  // End of inner class RequestPerformer
}
