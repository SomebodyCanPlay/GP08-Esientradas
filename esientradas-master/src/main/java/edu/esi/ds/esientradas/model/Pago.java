package edu.esi.ds.esientradas.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

// Entidad Pago: representa una transacción de compra completa (el carrito).
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Un pago agrupa a una o más entradas
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)
    private List<Entrada> entradas = new ArrayList<>();

    @Column(name = "cantidad_centimos", nullable = false)
    private Long cantidadCentimos;

    @Column(name = "moneda", length = 8, nullable = false)
    private String moneda;

    @Column(name = "estado", length = 32, nullable = false)
    private String estado;

    @Column(name = "id_intento_pago", length = 128)
    private String idIntentoPago;

    // Añadimos el email del comprador directamente al pago
    @Column(name = "usuario_email")
    private String usuarioEmail;

    @Column(name = "creado_en", nullable = false)
    private Long creadoEn;

    public Pago() {
        this.creadoEn = Instant.now().toEpochMilli();
        this.moneda = "EUR";
        this.estado = "PENDIENTE";
    }

    public Pago(Long cantidadCentimos, String moneda) {
        this();
        this.cantidadCentimos = cantidadCentimos;
        if (moneda != null) this.moneda = moneda;
    }

    // ── GETTERS Y SETTERS ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<Entrada> getEntradas() { return entradas; }
    public void setEntradas(List<Entrada> entradas) { this.entradas = entradas; }

    public Long getCantidadCentimos() { return cantidadCentimos; }
    public void setCantidadCentimos(Long cantidadCentimos) { this.cantidadCentimos = cantidadCentimos; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getIdIntentoPago() { return idIntentoPago; }
    public void setIdIntentoPago(String idIntentoPago) { this.idIntentoPago = idIntentoPago; }

    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }

    public Long getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Long creadoEn) { this.creadoEn = creadoEn; }
}