/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes;

import jade.core.AID;
import ontologia.Libro;

/**
 *
 * @author teres
 */
public class Puja {
    private Libro l;
    private Integer precioActual;
    private AID ganadorRonda;
    private boolean activa;
    private Integer intervalo;

    public Puja(Libro l, Integer precioActual,Integer intervalo) {
        this.l=l;
        this.precioActual = precioActual;
        this.activa=true;
        this.intervalo=intervalo;
    }

    public Integer getIntervalo() {
        return intervalo;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public Integer getPrecioActual() {
        return precioActual;
    }

    public void setPrecioActual(Integer precioActual) {
        this.precioActual = precioActual;
    }

    public AID getGanadorRonda() {
        return ganadorRonda;
    }

    public void setGanadorRonda(AID ganadorRonda) {
        this.ganadorRonda = ganadorRonda;
    }

    public Libro getL() {
        return l;
    }

    
    
}
