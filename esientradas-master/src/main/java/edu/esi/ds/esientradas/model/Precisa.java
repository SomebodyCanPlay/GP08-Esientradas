package edu.esi.ds.esientradas.model;

import jakarta.persistence.Entity;

// Entrada con asiento numerado (planta, fila, columna).
@Entity
public class Precisa extends Entrada {

    private int planta;
    private int fila;
    private int columna;

    // Constructor vacío requerido por JPA
    public Precisa() {
        super();
    }

    public int getPlanta() { return planta; }
    public void setPlanta(int planta) { this.planta = planta; }

    public int getFila() { return fila; }
    public void setFila(int fila) { this.fila = fila; }

    public int getColumna() { return columna; }
    public void setColumna(int columna) { this.columna = columna; }
}
