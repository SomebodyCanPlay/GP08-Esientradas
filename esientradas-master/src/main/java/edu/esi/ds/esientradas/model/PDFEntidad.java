package edu.esi.ds.esientradas.model;

import jakarta.persistence.*;

// Entidad PDFEntidad: guarda la URL del recibo PDF asociado a una entrada.
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
