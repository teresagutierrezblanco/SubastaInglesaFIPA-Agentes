package ontologia;


import ontologia.*;

/**
* Protege name: Ofertar
* @author OntologyBeanGenerator v4.1
* @version 2021/12/23, 20:52:12
*/
public class Ofertar implements jade.content.AgentAction {

  private static final long serialVersionUID = -9059911614860990042L;

  private String _internalInstanceName = null;

  public Ofertar() {
    this._internalInstanceName = "";
  }

  public Ofertar(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: oferta
   */
   private Oferta oferta;
   public void setOferta(Oferta value) { 
    this.oferta=value;
   }
   public Oferta getOferta() {
     return this.oferta;
   }

}
