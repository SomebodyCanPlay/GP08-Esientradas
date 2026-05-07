package edu.esi.ds.esientradas.model;

import jakarta.persistence.Entity;

// ============================================================
// ENTRADA PRECISA — para recintos con asientos numerados
// ============================================================
// Hereda de Entrada todos los campos comunes (id, precio, estado, espectáculo, token)
// y añade la UBICACIÓN EXACTA del asiento:
//   - planta  → en qué piso está (planta baja, primera, etc.)
//   - fila    → número de fila dentro de la planta
//   - columna → número de asiento dentro de la fila
//
// Ejemplo real: Teatro de la Zarzuela, Planta 1, Fila 3, Asiento 14
//
// En la base de datos existe una tabla "precisa" con columnas:
//   id (FK que apunta a entrada.id), planta, fila, columna
// Hibernate hace el JOIN automáticamente.
// ============================================================
@Entity
public class Precisa extends Entrada {

    // Número de planta (0 = planta baja, 1 = primera planta, etc.)
    private int planta;

    // Número de fila dentro de la planta
    private int fila;

    // Número de columna (= número del asiento) dentro de la fila
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
