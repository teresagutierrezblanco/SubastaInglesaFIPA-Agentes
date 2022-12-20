package ontologia;


/**
* Protege name: Libro
* @author OntologyBeanGenerator v4.1
* @version 2021/12/24, 12:37:29
*/
public class Libro implements jade.content.Concept{

  private static final long serialVersionUID = 7915611569178847533L;

  private String _internalInstanceName = null;

  public Libro() {
    this._internalInstanceName = "";
  }

  public Libro(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: id
   */
   private int id;
   public void setId(int value) { 
    this.id=value;
   }
   public int getId() {
     return this.id;
   }

   /**
   * titulo del libro
   * Protege name: Titulo
   */
   private String titulo;
   public void setTitulo(String value) { 
    this.titulo=value;
   }
   public String getTitulo() {
     return this.titulo;
   }

}
