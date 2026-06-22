package edu.esi.ds.esientradas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
@JsonSubTypes({
    @Type(value = Precisa.class, name = "precisa"),
    @Type(value = DeZona.class, name = "dezona")
})
public abstract class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    private Long precio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espectaculo_id", nullable = false)
    protected Espectaculo espectaculo;

    @Enumerated(EnumType.STRING)
    protected Estado estado;

    @OneToOne(mappedBy = "entrada", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    protected Token token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    protected Pago pago;

    // El código  para cancelar entrada concreta
    @Column(name = "token_cancelacion", unique = true)
    protected String tokenCancelacion;

    // ============================================================
    // GETTERS Y SETTERS
    // ============================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @JsonIgnore
    public Espectaculo getEspectaculo() { return espectaculo; }
    public void setEspectaculo(Espectaculo espectaculo) { this.espectaculo = espectaculo; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Long getPrecio() { return precio; }
    public void setPrecio(Long precio) { this.precio = precio; }

    @JsonIgnore
    public Token getToken() { return token; }
    public void setToken(Token token) { this.token = token; }

    @JsonIgnore
    public Pago getPago() { return pago; }
    public void setPago(Pago pago) { this.pago = pago; }

    public String getTokenCancelacion() { return tokenCancelacion; }
    public void setTokenCancelacion(String tokenCancelacion) { this.tokenCancelacion = tokenCancelacion; }
}