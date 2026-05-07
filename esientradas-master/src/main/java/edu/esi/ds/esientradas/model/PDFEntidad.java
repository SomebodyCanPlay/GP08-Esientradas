package edu.esi.ds.esientradas.model;

import jakarta.persistence.*;

// ============================================================
// PDFEntidad — registro del PDF del recibo de compra
// ============================================================
// Cuando un usuario compra una entrada, PDFService genera un recibo en PDF
// y guarda la URL donde está almacenado en esta tabla.
//
// En la implementación actual el PDF es SIMULADO:
// se guarda una URL ficticia como "/storage/pdfs/recibo_X.pdf"
// pero en producción real se usaría una librería como iText o Apache PDFBox.
//
// Esta tabla relaciona: entrada_id → urlPDF
// Así podemos buscar el PDF de cualquier entrada por su ID.
// ============================================================
@Entity
@Table(name = "pdf")
public class PDFEntidad {

    // ID único del registro PDF (generado por MySQL)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID de la entrada a la que corresponde este PDF
    // (no usamos @OneToOne aquí para simplificar — guardamos solo el ID)
    private Long entradaId;

    // Ruta o URL donde está guardado el PDF del recibo
    // Ejemplo simulado: "/storage/pdfs/recibo_42.pdf"
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
