package edu.esi.ds.esientradas.model;

import jakarta.persistence.Entity;

// ============================================================
// ENTRADA DE ZONA — para recintos sin asientos numerados
// ============================================================
// Hereda de Entrada todos los campos comunes (id, precio, estado, espectáculo, token)
// y añade solo el número de ZONA, sin fila ni columna.
//
// Ejemplo real: concierto de Rosalía en el Bernabéu.
//   - Zona 1 → pista (más cara, cerca del escenario)
//   - Zona 2 → gradas bajas
//   - Zona 3 → gradas altas (más barata)
// El usuario entra a la zona y se sienta donde quiere.
//
// En la base de datos existe una tabla "de_zona" con columnas:
//   id (FK que apunta a entrada.id), zona
// ============================================================
@Entity
public class DeZona extends Entrada {

    // Número de zona del recinto (cada zona puede tener un precio diferente)
    private Integer zona;

    // Constructor vacío: JPA lo necesita obligatoriamente
    public DeZona() {
        super();
    }

    public Integer getZona() { return zona; }
    public void setZona(Integer zona) { this.zona = zona; }
}
