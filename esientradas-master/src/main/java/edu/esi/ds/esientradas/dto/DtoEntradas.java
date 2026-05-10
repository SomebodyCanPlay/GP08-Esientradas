package edu.esi.ds.esientradas.dto;

// DTO para estadísticas de entradas; versión simplificada para el frontend.
public class DtoEntradas {

    // Total de entradas del espectáculo.
    private Integer total;

    // Entradas DISPONIBLES.
    private Integer libres;

    // Entradas RESERVADAS (en carrito).
    private Integer reservadas;

    // Entradas VENDIDAS.
    private Integer vendidas;

    // Constructor vacío.
    public DtoEntradas() {}

    // Constructor con todos los campos.
    public DtoEntradas(Integer total, Integer libres, Integer reservadas, Integer vendidas) {
        this.total = total;
        this.libres = libres;
        this.reservadas = reservadas;
        this.vendidas = vendidas;
    }

    // Getters y setters.
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Integer getLibres() { return libres; }
    public void setLibres(Integer libres) { this.libres = libres; }

    public Integer getReservadas() { return reservadas; }
    public void setReservadas(Integer reservadas) { this.reservadas = reservadas; }

    public Integer getVendidas() { return vendidas; }
    public void setVendidas(Integer vendidas) { this.vendidas = vendidas; }
}