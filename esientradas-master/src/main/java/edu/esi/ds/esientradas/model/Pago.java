package edu.esi.ds.esientradas.model;

import jakarta.persistence.*;
import java.time.Instant;

// Entidad Pago: registro histórico de pagos (cantidad en céntimos).
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_id")
    private Entrada entrada;

    @Column(name = "cantidad_centimos", nullable = false)
    private Long cantidadCentimos;

    @Column(name = "moneda", length = 8, nullable = false)
    private String moneda;

    @Column(name = "estado", length = 32, nullable = false)
    private String estado;

    @Column(name = "id_intento_pago", length = 128)
    private String idIntentoPago;

    @Column(name = "creado_en", nullable = false)
    private Long creadoEn;

    public Pago() {
        this.creadoEn = Instant.now().toEpochMilli();
        this.moneda = "EUR";
        this.estado = "PENDIENTE";
    }

    public Pago(Entrada entrada, Long cantidadCentimos, String moneda) {
        this();
        this.entrada = entrada;
        this.cantidadCentimos = cantidadCentimos;
        if (moneda != null) this.moneda = moneda;
    }

    // ── GETTERS Y SETTERS ──

    public Long getId() { return id; }

    public Entrada getEntrada() { return entrada; }
    public void setEntrada(Entrada entrada) { this.entrada = entrada; }

    public Long getCantidadCentimos() { return cantidadCentimos; }
    public void setCantidadCentimos(Long cantidadCentimos) { this.cantidadCentimos = cantidadCentimos; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getIdIntentoPago() { return idIntentoPago; }
    public void setIdIntentoPago(String idIntentoPago) { this.idIntentoPago = idIntentoPago; }

    public Long getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Long creadoEn) { this.creadoEn = creadoEn; }
}
