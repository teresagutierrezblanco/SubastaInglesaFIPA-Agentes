

package ontologia;

/**
* Protege name: Ganar
* @author OntologyBeanGenerator v4.1
* @version 2021/12/23, 20:52:12
*/
public class Ganar implements jade.content.Predicate{

  private static final long serialVersionUID = -9059911614860990042L;

  private String _internalInstanceName = null;

  public Ganar() {
    this._internalInstanceName = "";
  }

  public Ganar(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: puja
   */
   private Oferta puja;
   public void setPuja(Oferta value) { 
    this.puja=value;
   }
   public Oferta getPuja() {
     return this.puja;
   }

   /**
   * Protege name: ganador
   */
   private String ganador;
   public void setGanador(String value) { 
    this.ganador=value;
   }
   public String getGanador() {
     return this.ganador;
   }

}
