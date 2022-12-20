package agentes;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author teres
 */
public class ModeloTablaLibros extends AbstractTableModel {

    private java.util.ArrayList<Puja> libro;

    public ModeloTablaLibros() {
        this.libro = new java.util.ArrayList<>();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getRowCount() {
        return libro.size();
    }

    @Override
    public String getColumnName(int col) {
        String nombre = "";

        switch (col) {
            case 0:
                nombre = "Titulo";
                break;
            case 2:
                nombre = "Valor actual";
                break;
            case 1:
                nombre = "id";
                break;
        }
        return nombre;
    }

    @Override
    public Class getColumnClass(int col) {
        Class clase = null;

        switch (col) {
            case 0:
                clase = java.lang.String.class;
                break;
            case 1:
                clase = java.lang.Integer.class;
                break;
            case 2:
                clase = java.lang.Integer.class;
                break;
        }
        return clase;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Object resultado = null;
        if (row < libro.size()) {
            switch (col) {
                case 0:
                    resultado = libro.get(libro.size() - 1 - row).getL().getTitulo();
                    break;
                case 1:
                    resultado = libro.get(libro.size() - 1 - row).getL().getId();
                    break;
                case 2:
                    resultado = libro.get(libro.size() - 1 - row).getPrecioActual();
                    break;
            }

            return resultado;
        }
        return null;
    }

    public void setFilas(java.util.Set<Puja> libro) {
        this.libro.clear();
        for (Puja aux : libro) {
            this.libro.add(aux);
        }
        fireTableDataChanged();
    }

    public void borrarTabla() {
        this.libro.clear();
        fireTableDataChanged();
    }

    public void borrarFila(int indice) {
        this.libro.remove(indice);
        fireTableRowsDeleted(indice, indice);
    }

    public void anadirFila(Puja usuario) {
        this.libro.add(usuario);
        //fireTableRowsInserted(this.libro.size() - 1, this.libro.size() - 1);
        fireTableDataChanged();
    }

    public ArrayList<Puja> getFilas() {
        return libro;
    }

    public Puja getFila(int fila) {
        return libro.get(fila);
    }
}
