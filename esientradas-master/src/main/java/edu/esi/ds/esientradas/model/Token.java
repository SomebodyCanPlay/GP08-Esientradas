package edu.esi.ds.esientradas.model;

import java.util.UUID;
import jakarta.persistence.*;

// ============================================================
// TOKEN — la "reserva temporal" de una entrada
// ============================================================
// Cuando un usuario selecciona una entrada, el sistema crea un Token
// y lo vincula a esa entrada. Así queda "bloqueada" unos minutos.
//
// Es como el carrito de Amazon: pones algo y queda reservado.
// Si no pagas, vuelve al "stock" (DISPONIBLE) pasado un tiempo.
//
// Ciclo de vida:
//   1. Usuario selecciona entrada → se crea Token → entrada pasa a RESERVADA
//   2a. Usuario paga → PagosService borra el Token → entrada pasa a VENDIDA
//   2b. Pasan 10 min sin pagar → ReservaCleanUpTask borra Token → DISPONIBLE
// ============================================================
@Entity
public class Token {

    // El valor es la clave primaria (un texto UUID de 32 caracteres, sin guiones)
    // Ejemplo: "a3f9k2b1d4e5f6g7h8i9j0k1l2m3n4o5"
    @Id
    @Column(name = "valor", nullable = false, length = 32)
    private String valor;

    // Momento exacto en que se creó el token (milisegundos desde 1970)
    // ReservaCleanUpTask compara esto con el tiempo actual para saber si caducó
    @Column(name = "hora", nullable = false)
    private Long hora;

    // ID de la sesión del navegador — identifica qué usuario tiene qué tokens
    // Es el mismo valor que Angular guarda en sessionStorage
    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    // La entrada que este token está bloqueando
    // @OneToOne → un token bloquea exactamente UNA entrada (y viceversa)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_id")
    private Entrada entrada;

    // Al crear un Token se auto-genera el valor (UUID) y se anota la hora actual
    public Token() {
        this.valor = UUID.randomUUID().toString().replace("-", "");
        this.hora = System.currentTimeMillis();
    }

    // ── GETTERS Y SETTERS ──

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public Long getHora() { return hora; }
    public void setHora(Long hora) { this.hora = hora; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Entrada getEntrada() { return entrada; }
    public void setEntrada(Entrada entrada) { this.entrada = entrada; }
}
