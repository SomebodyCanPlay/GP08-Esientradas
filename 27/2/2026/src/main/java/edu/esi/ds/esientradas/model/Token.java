package edu.esi.ds.esientradas.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;

@Entity
public class Token {
    @Id
    @Column(name = "valor", nullable = false, length = 32)//36
    private String valor;

    @Column(name = "hora", nullable = false)
    private Long hora;

    @Column(name = "session_id", nullable = false, length = 64)//el 64 problablemente se quite
    private String sessionId;

    // ahora Token es el lado propietario de la relación, con la FK en la tabla token
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_id")          // columna que faltaba en tu script SQL
    private Entrada entrada;

    public Token() {
        // generar UUID sin guiones para 32 caracteres
        this.valor = UUID.randomUUID().toString().replace("-", "");
        this.hora = System.currentTimeMillis();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    // Getters and setters
    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Long getHora() {
        return hora;
    }

    public void setHora(Long hora) {
        this.hora = hora;
    }

    public Entrada getEntrada() {
        return entrada;
    }

    public void setEntrada(Entrada entrada) {
        this.entrada = entrada;
    }
}
