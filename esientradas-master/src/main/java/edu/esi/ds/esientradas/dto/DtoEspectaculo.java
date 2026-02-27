package edu.esi.ds.esientradas.dto;
import java.time.LocalDateTime;

public class DtoEspectaculo {
    public String artista;
    public LocalDateTime fecha;
    private String escenario;
    private Long id;

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public void setEscenario(String escenario) {
        this.escenario = escenario;
    }

    public String getArtista() {
        return this.artista;
    }

    public LocalDateTime getFecha() {
        return this.fecha;
    }

    public String getEscenario() {
        return this.escenario;
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
