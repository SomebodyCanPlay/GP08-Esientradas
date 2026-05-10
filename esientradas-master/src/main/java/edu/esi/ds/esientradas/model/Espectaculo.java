package edu.esi.ds.esientradas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


// Relaciones:
//   Escenario ──1:N──► Espectaculo ──1:N──► Entrada
//   (un escenario tiene muchos espectáculos)
//   (un espectáculo tiene muchas entradas)
// ============================================================
@Entity
public class Espectaculo {

    // ID único del espectáculo (generado por MySQL automáticamente)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String artista;
    private LocalDateTime fecha;

    // @ManyToOne → muchos espectáculos pueden ser en el mismo escenario
    // FetchType.LAZY → no carga el escenario de la BD hasta que lo necesites
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escenario_id", nullable = false)
    private Escenario escenario;

    // CascadeType.ALL → si borramos el espectáculo, sus entradas también se borran
    // orphanRemoval → si quitamos una entrada de la lista, se borra de la BD
    @OneToMany(mappedBy = "espectaculo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Entrada> entradas = new ArrayList<>();

    // ── GETTERS Y SETTERS ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getArtista() { return artista; }
    public void setArtista(String artista) { this.artista = artista; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    // @JsonIgnore → evita bucle infinito al serializar a JSON
    // (espectaculo→escenario→espectaculos→espectaculo→...)
    @JsonIgnore
    public Escenario getEscenario() { return escenario; }
    public void setEscenario(Escenario escenario) { this.escenario = escenario; }

    // @JsonIgnore → las entradas se piden por separado con otro endpoint
    @JsonIgnore
    public List<Entrada> getEntradas() { return entradas; }
    public void setEntradas(List<Entrada> entradas) { this.entradas = entradas; }
}
