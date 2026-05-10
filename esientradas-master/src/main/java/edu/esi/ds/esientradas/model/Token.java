package edu.esi.ds.esientradas.model;

import java.util.UUID;
import jakarta.persistence.*;

// Token de reserva temporal vinculado a una entrada y una sesión.
@Entity
public class Token {

    @Id
    @Column(name = "valor", nullable = false, length = 32)
    private String valor;

    @Column(name = "hora", nullable = false)
    private Long hora;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_id")
    private Entrada entrada;

    public Token() {
        this.valor = UUID.randomUUID().toString().replace("-", "");
        this.hora = System.currentTimeMillis();
    }

    // GETTERS Y SETTERS

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public Long getHora() { return hora; }
    public void setHora(Long hora) { this.hora = hora; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Entrada getEntrada() { return entrada; }
    public void setEntrada(Entrada entrada) { this.entrada = entrada; }
}
