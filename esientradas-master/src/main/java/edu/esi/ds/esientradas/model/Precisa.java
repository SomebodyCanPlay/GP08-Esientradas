package edu.esi.ds.esientradas.model;

import jakarta.persistence.Entity;

// ENTRADA PRECISA — para recintos con asientos numerados

@Entity
public class Precisa extends Entrada {

    private int planta;

    private int fila;

    private int columna;

    // Constructor vacío: Spring/JPA lo necesita para crear objetos desde la BD
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
