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
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ontologia.*;

/**
 *
 * @author teres
 */
public class AgenteComprador extends Agent {

    private InterfazComprador gui;
    private HashMap<String, Integer> libros;
    private Codec codec;
    private Ontology onto;

    protected void setup() {
        gui = new InterfazComprador(this);
        gui.setVisible(true);
        libros = new HashMap<>();
// Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("comprador");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        codec = new SLCodec();
        onto = SubastaOntology.getInstance();
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(onto);
        addBehaviour(new OfferRequestsServer());
        addBehaviour(new PurchaseOrdersServer());
        addBehaviour(new Noticia());
        addBehaviour(new Informacion());

    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        System.out.println( getAID().getName() + " terminating.");
        this.gui.dispose();
        
    }

    public void anadirLibro(final String title, final int price) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                libros.put(title, price);
                gui.actualizarTabla(title, price);
                System.out.println(getAID().getName() +" interesado en " +title + " Price = " + price);
            }
        });
    }

    public void borrarLibro(final String title) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                libros.remove(title);
                gui.actualizarTabla(title, 0);
            }
        });
    }

    private class OfferRequestsServer extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP), MessageTemplate.and(MessageTemplate.MatchOntology(onto.getName()), MessageTemplate.MatchLanguage(codec.getName())));

            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                Action a;
                try {
                    a = (Action) getContentManager().extractContent(msg);
                    Ofertar o = (Ofertar) a.getAction();
                    String title = o.getOferta().getProducto().getTitulo();
                    Integer precio = o.getOferta().getPrecio();
                    ACLMessage reply = msg.createReply();
                    reply.setOntology(onto.getName());
		    reply.setLanguage(codec.getName());
                    Responder r = new Responder();

                    Integer price = libros.get(title);
                    if (price != null) {
                        // The requested book is available for sale. Reply with the price
                        if (precio <= price) {
                            r.setParticipo(true);
                            
                            gui.actualizarNoticias("Pujando " + precio + " por " + title + " id:" + o.getOferta().getProducto().getId()+ ", MAXIMO: " + libros.get(title));
                        } else {
                            r.setParticipo(false);

                            gui.actualizarNoticias("Abandonando puja: " + title + " id:" + o.getOferta().getProducto().getId()+ " por " + precio +", MAXIMO: " + libros.get(title));
                        }
                    } else {
                        r.setParticipo(false);

                    }
                    getContentManager().fillContent(reply, new Action(getAID(), r));
                    reply.setPerformative(ACLMessage.PROPOSE);

                    myAgent.send(reply);
                } catch (Codec.CodecException ex) {
                    Logger.getLogger(AgenteComprador.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OntologyException ex) {
                    Logger.getLogger(AgenteComprador.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                block();
            }
        }
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {

        public void action() {

            // Receive the purchase order reply
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.and(MessageTemplate.MatchOntology(onto.getName()), MessageTemplate.MatchLanguage(codec.getName())));

            ACLMessage reply = myAgent.receive(mt);
            if (reply != null) {
                try {
                    // Purchase successful. We can terminate
                    Action a = (Action) getContentManager().extractContent(reply);

                    Ofertar o = (Ofertar) a.getAction();

                    System.out.println(o.getOferta().getProducto().getTitulo() + " successfully purchased from agent " + reply.getSender().getName());
                    gui.actualizarNoticias("GANADO " + o.getOferta().getProducto().getTitulo()+" id:" + o.getOferta().getProducto().getId() + " por " + o.getOferta().getPrecio() + ", MAXIMO: " + libros.get(o.getOferta().getProducto().getTitulo()));

                    gui.actualizarTabla(o.getOferta().getProducto().getTitulo(), o.getOferta().getPrecio());
                    libros.remove(o.getOferta().getProducto().getTitulo());
                } catch (Codec.CodecException ex) {
                    Logger.getLogger(AgenteComprador.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OntologyException ex) {
                    Logger.getLogger(AgenteComprador.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

    private class Informacion extends CyclicBehaviour {

        public void action() {

            // Receive the purchase order reply
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL), MessageTemplate.and(MessageTemplate.MatchOntology(onto.getName()), MessageTemplate.MatchLanguage(codec.getName())));

            ACLMessage reply = myAgent.receive(mt);
            if (reply != null) {
                // Purchase successful. We can terminate

                try {
                    Action a = (Action) getContentManager().extractContent(reply);

                    Ofertar p = (Ofertar) a.getAction();

                    gui.actualizarNoticias("RONDA ganada " + p.getOferta().getProducto().getTitulo() +" id:" + p.getOferta().getProducto().getId()+ ", precio " + p.getOferta().getPrecio() + ", MAXIMO: " + libros.get(p.getOferta().getProducto().getTitulo()));

                } catch (Codec.CodecException ex) {
                    Logger.getLogger(AgenteComprador.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OntologyException ex) {
                    Logger.getLogger(AgenteComprador.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

//if (reply.getPerformative() == ACLMessage.PROPOSE) {
    private class Noticia extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.and(MessageTemplate.MatchOntology(onto.getName()), MessageTemplate.MatchLanguage(codec.getName())));

            // Receive the purchase order reply
            ACLMessage reply = myAgent.receive(mt);
            if (reply != null) {
                // Purchase successful. We can terminate
                Ganar p;
                try {
                    p = (Ganar) getContentManager().extractContent(reply);
                    Oferta o = p.getPuja();
                    gui.actualizarNoticias(o.getProducto().getTitulo() +" id:" + o.getProducto().getId() + " ganado por " + p.getGanador() + " por " + o.getPrecio() + " euros");

                } catch (Codec.CodecException ex) {
                    Logger.getLogger(AgenteComprador.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OntologyException ex) {
                    Logger.getLogger(AgenteComprador.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

}
