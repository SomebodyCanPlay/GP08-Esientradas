package edu.esi.ds.esientradas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;

// ============================================================
// CLASE BASE ABSTRACTA — Entrada
// ============================================================
// Esta es la clase "padre" de todos los tipos de entrada.
// Es ABSTRACTA → no se puede crear un objeto Entrada directamente.
// Solo existen dos tipos concretos: Precisa y DeZona.
//
// ¿Por qué usar herencia?
// En un concierto de campo (como Aitana en el Bernabéu) las entradas son
// por ZONA (zona A, zona B...) → clase DeZona
// En un teatro (como el Auditorio Nacional) hay butacas concretas
// con fila y columna → clase Precisa
// Ambas comparten precio, estado y espectáculo → esos campos van aquí en Entrada.
//
// @Inheritance(strategy = JOINED):
// En la base de datos habrá 3 tablas:
//   - entrada  → campos comunes (id, precio, estado, espectaculo_id)
//   - precisa  → fila, columna, planta (comparte id con entrada)
//   - de_zona  → zona (comparte id con entrada)
// Hibernate hace el JOIN automáticamente.
//
// @JsonTypeInfo y @JsonSubTypes:
// Cuando el servidor devuelve una entrada al frontend en JSON,
// añade automáticamente un campo "tipo": "precisa" o "tipo": "dezona"
// para que el frontend sepa de qué tipo es.
// ============================================================
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
@JsonSubTypes({
    @Type(value = Precisa.class, name = "precisa"),
    @Type(value = DeZona.class, name = "dezona")
})
public abstract class Entrada {

    // ID único de la entrada (lo genera MySQL automáticamente con AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    // Precio en CÉNTIMOS de euro (no en euros enteros)
    // Ejemplo: 2500 = 25,00 € — así lo usa Stripe también
    private Long precio;

    // Relación con el espectáculo al que pertenece esta entrada
    // @ManyToOne → muchas entradas pertenecen a un espectáculo
    // @JoinColumn → en la tabla "entrada" habrá una columna "espectaculo_id"
    // FetchType.LAZY → Hibernate NO carga el espectáculo hasta que lo pedimos explícitamente
    //   (optimización: evita cargar datos innecesarios de la BD)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espectaculo_id", nullable = false)
    protected Espectaculo espectaculo;

    // Estado actual de la entrada: DISPONIBLE, RESERVADA o VENDIDA
    // @Enumerated(STRING) → guarda el texto "DISPONIBLE" en BD, no un número
    //   Así si ves la BD directamente puedes entender el valor sin consultar el código
    @Enumerated(EnumType.STRING)
    protected Estado estado;

    // Token de prerreserva: vincula esta entrada con la sesión del usuario que la está comprando
    // @OneToOne → una entrada tiene como máximo UN token de reserva
    // mappedBy = "entrada" → el FK (entrada_id) vive en la tabla TOKEN, no aquí
    // CascadeType.ALL → si borramos la entrada, el token se borra también automáticamente
    @OneToOne(mappedBy = "entrada", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    protected Token token;

    // ============================================================
    // GETTERS Y SETTERS
    // ============================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // @JsonIgnore → cuando el servidor convierte esta entrada a JSON para el frontend,
    // NO incluye el espectáculo dentro (evita JSON infinito: entrada→espectaculo→entradas→...)
    @JsonIgnore
    public Espectaculo getEspectaculo() { return espectaculo; }
    public void setEspectaculo(Espectaculo espectaculo) { this.espectaculo = espectaculo; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Long getPrecio() { return precio; }
    public void setPrecio(Long precio) { this.precio = precio; }

    // @JsonIgnore también en el token — no queremos exponer el token al frontend por seguridad
    @JsonIgnore
    public Token getToken() { return token; }
    public void setToken(Token token) { this.token = token; }
}
