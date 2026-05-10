package edu.esi.ds.esientradas.model;

import jakarta.persistence.*;

// ============================================================
// PDFEntidad — registro del PDF del recibo de compra
// ============================================================
// Cuando un usuario compra una entrada, PDFService genera un recibo en PDF
// y guarda la URL donde está almacenado en esta tabla.
// Esta tabla relaciona: entrada_id → urlPDF
// Así podemos buscar el PDF de cualquier entrada por su ID.
// ============================================================
@Entity
@Table(name = "pdf")
public class PDFEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID de la entrada a la que corresponde este PDF
    private Long entradaId;
    private String urlPDF;

    // Constructor vacío requerido por JPA
    public PDFEntidad() {}

    // ── GETTERS Y SETTERS ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEntradaId() { return entradaId; }
    public void setEntradaId(Long entradaId) { this.entradaId = entradaId; }

    public String getUrlPDF() { return urlPDF; }
    public void setUrlPDF(String urlPDF) { this.urlPDF = urlPDF; }
}
