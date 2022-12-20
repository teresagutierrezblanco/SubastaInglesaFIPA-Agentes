package ontologia;




/**
* Protege name: Oferta
* @author OntologyBeanGenerator v4.1
* @version 2021/12/24, 12:37:29
*/
public class Oferta implements jade.content.Concept {

  private static final long serialVersionUID = 7915611569178847533L;

  private String _internalInstanceName = null;

  public Oferta() {
    this._internalInstanceName = "";
  }

  public Oferta(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: producto
   */
   private Libro producto;
   public void setProducto(Libro value) { 
    this.producto=value;
   }
   public Libro getProducto() {
     return this.producto;
   }

   /**
   * precio actual
   * Protege name: precio
   */
   private int precio;
   public void setPrecio(int value) { 
    this.precio=value;
   }
   public int getPrecio() {
     return this.precio;
   }

}
