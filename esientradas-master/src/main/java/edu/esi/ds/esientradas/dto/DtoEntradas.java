package edu.esi.ds.esientradas.dto;

// ============================================================
// DTO — Data Transfer Object (Objeto de Transferencia de Datos)
// ============================================================
// Un DTO es un objeto SIMPLIFICADO que usamos para enviar datos al frontend.
// No tiene @Entity ni se guarda en la BD — es solo un "sobre" para transportar datos.
//
// ¿Por qué no mandamos el objeto Entrada directamente?
// Porque Entrada es compleja: tiene relación con Espectaculo, con Token...
// y si necesitamos saber cuántas entradas libres quedan, no hace falta
// mandar todos esos objetos — con 4 números es suficiente.
//
// ANTES (sin DTO):  mandamos 500 objetos Entrada → frontend recibe megas de datos
// AHORA  (con DTO): mandamos 4 números           → frontend recibe esto:
//
//   {
//     "total":      500,    ← cuántas entradas tiene el espectáculo en total
//     "libres":     120,    ← cuántas están disponibles para comprar ahora
//     "reservadas":  30,    ← cuántas están bloqueadas (en el carrito de alguien)
//     "vendidas":   350     ← cuántas ya se han vendido (no se pueden comprar)
//   }
//
// Usado en: BusquedaService.getNumeroDeEntradasComoDto() →
//           BusquedaController.getResumenEntradas()
// ============================================================
public class DtoEntradas {

    // Número total de entradas del espectáculo (independientemente del estado)
    private Integer total;

    // Entradas con estado DISPONIBLE → se pueden comprar ahora mismo
    private Integer libres;

    // Entradas con estado RESERVADA → alguien las tiene en el carrito (10 min)
    private Integer reservadas;

    // Entradas con estado VENDIDA → ya compradas, no disponibles
    private Integer vendidas;

    // Constructor vacío: JPA/Spring lo necesita en algunos casos
    public DtoEntradas() {}

    // Constructor de conveniencia: creamos el DTO con todos los datos de golpe
    // BusquedaService lo usa así: new DtoEntradas(total, libres, reservadas, vendidas)
    public DtoEntradas(Integer total, Integer libres, Integer reservadas, Integer vendidas) {
        this.total = total;
        this.libres = libres;
        this.reservadas = reservadas;
        this.vendidas = vendidas;
    }

    // ── GETTERS Y SETTERS ──
    // Spring los necesita para convertir el objeto a JSON automáticamente

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Integer getLibres() { return libres; }
    public void setLibres(Integer libres) { this.libres = libres; }

    public Integer getReservadas() { return reservadas; }
    public void setReservadas(Integer reservadas) { this.reservadas = reservadas; }

    public Integer getVendidas() { return vendidas; }
    public void setVendidas(Integer vendidas) { this.vendidas = vendidas; }
}