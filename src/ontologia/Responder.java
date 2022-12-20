

package ontologia;

/**
* Protege name: Responder
* @author OntologyBeanGenerator v4.1
* @version 2021/12/23, 20:52:12
*/
public class Responder implements jade.content.AgentAction {

  private static final long serialVersionUID = -9059911614860990042L;

  private String _internalInstanceName = null;

  public Responder() {
    this._internalInstanceName = "";
  }

  public Responder(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: participo
   */
   private boolean participo;
   public void setParticipo(boolean value) { 
    this.participo=value;
   }
   public boolean getParticipo() {
     return this.participo;
   }

}
