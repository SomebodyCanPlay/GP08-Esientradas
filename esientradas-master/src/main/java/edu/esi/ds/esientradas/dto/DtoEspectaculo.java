package edu.esi.ds.esientradas.dto;

import java.time.LocalDateTime;

// ============================================================
// DTO de Espectaculo — versión simplificada para el frontend
// ============================================================
// El objeto Espectaculo del model tiene relaciones complejas:
//   - escenario → objeto Escenario entero (con su id, nombre, descripcion...)
//   - entradas  → lista de cientos de objetos Entrada
//
// El frontend solo necesita saber el artista, la fecha, el NOMBRE del escenario
// y el id del espectáculo. Con este DTO mandamos exactamente eso, sin datos extra.
//
// ANTES (con Espectaculo):     AHORA (con DtoEspectaculo):
// ────────────────────────     ──────────────────────────
// id                  ✅       id          ✅
// artista             ✅       artista     ✅
// fecha               ✅       fecha       ✅
// escenario (objeto)  ❌       escenario   ← solo el NOMBRE (String), no el objeto entero
// entradas (lista)    ❌       (no incluido)
//
// JSON que recibe el frontend:
//   {
//     "id": 7,
//     "artista": "Natos y Waor",
//     "fecha": "2026-03-14T21:00:00",
//     "escenario": "Wizink Center"    ← solo el nombre, no todo el objeto
//   }
// ============================================================
public class DtoEspectaculo {

    // ID del espectáculo (para que el frontend pueda identificarlo al comprar)
    private Long id;

    // Nombre del artista o grupo (ej: "Coldplay", "Aitana")
    private String artista;

    // Fecha y hora del evento (ej: 2026-03-14T21:00)
    private LocalDateTime fecha;

    // Solo el NOMBRE del escenario (String), no el objeto Escenario entero
    // Ejemplo: "Wizink Center" en vez de { id:1, nombre:"Wizink Center", descripcion:"..." }
    private String escenario;

    // ── GETTERS Y SETTERS ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getArtista() { return artista; }
    public void setArtista(String artista) { this.artista = artista; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getEscenario() { return escenario; }
    public void setEscenario(String escenario) { this.escenario = escenario; }
}
