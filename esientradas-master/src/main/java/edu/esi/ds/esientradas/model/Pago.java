package edu.esi.ds.esientradas.model;

import jakarta.persistence.*;
import java.time.Instant;

// ============================================================
// PAGO — registro de cada pago completado
// ============================================================
// Cuando un usuario confirma la compra y el pago se procesa por Stripe,
// se crea un objeto Pago en la base de datos como registro histórico.
//
// ¿Por qué guardamos el pago? Para:
//   1. Llevar contabilidad (cuánto se ha ingresado)
//   2. Poder hacer devoluciones (tenemos el idIntentoPago de Stripe)
//   3. Justificante del usuario
//
// Nota: el precio está en CÉNTIMOS (como Stripe) → 2500 = 25,00€
// ============================================================
@Entity
@Table(name = "pago")
public class Pago {

    // ID único del pago (generado por MySQL)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // La entrada que se compró con este pago
    // @OneToOne → un pago corresponde a UNA sola entrada
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_id")
    private Entrada entrada;

    // Cuánto se pagó, en CÉNTIMOS de euro
    // Ejemplo: 2500 → 25,00 €
    @Column(name = "cantidad_centimos", nullable = false)
    private Long cantidadCentimos;

    // Moneda del pago (siempre "EUR" en este sistema)
    @Column(name = "moneda", length = 8, nullable = false)
    private String moneda;

    // Estado del pago: "PENDIENTE" al crearse, "COMPLETADO" cuando Stripe confirma
    @Column(name = "estado", length = 32, nullable = false)
    private String estado;

    // ID del intento de pago en Stripe (empieza por "pi_")
    // Lo necesitamos si hay que hacer una devolución o consultar el estado en Stripe
    @Column(name = "id_intento_pago", length = 128)
    private String idIntentoPago;

    // Timestamp de cuándo se creó este registro de pago (en milisegundos)
    @Column(name = "creado_en", nullable = false)
    private Long creadoEn;

    // Constructor vacío: pone los valores por defecto
    public Pago() {
        this.creadoEn = Instant.now().toEpochMilli(); // momento actual
        this.moneda = "EUR";
        this.estado = "PENDIENTE";
    }

    // Constructor de conveniencia para crear el pago de una entrada
    public Pago(Entrada entrada, Long cantidadCentimos, String moneda) {
        this(); // llama al constructor vacío para poner los defaults
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
