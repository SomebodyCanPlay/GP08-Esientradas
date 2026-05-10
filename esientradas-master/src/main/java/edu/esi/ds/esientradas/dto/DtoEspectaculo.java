package edu.esi.ds.esientradas.dto;

import java.time.LocalDateTime;

// DTO simplificado de Espectaculo para el frontend.
public class DtoEspectaculo {

    // ID del espectáculo.
    private Long id;

    // Nombre del artista o grupo.
    private String artista;

    // Fecha y hora del evento.
    private LocalDateTime fecha;

    // Nombre del escenario (solo String).
    private String escenario;

    // Getters y setters.
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getArtista() { return artista; }
    public void setArtista(String artista) { this.artista = artista; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getEscenario() { return escenario; }
    public void setEscenario(String escenario) { this.escenario = escenario; }
}