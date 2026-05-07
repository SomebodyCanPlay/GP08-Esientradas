package edu.esi.ds.esientradas.model;

import jakarta.persistence.*;

// ============================================================
// CONFIGURACION — datos de la empresa para el PDF del recibo
// ============================================================
// Esta tabla guarda la información de la empresa que vende las entradas.
// Se usa cuando se genera el PDF de confirmación de compra para personalizar el recibo.
//
// Solo debería haber UNA fila en esta tabla (la configuración de la empresa).
// PagosService la carga así: configuracionDao.findAll().stream().findFirst()
// ============================================================
@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de la empresa (ej: "ESIentradas S.L.")
    private String nombre;

    // URL de la web (ej: "https://www.esientradas.com") — aparece en el recibo
    private String url;

    // Lista de vendedores autorizados (puede ser un texto o JSON con nombres)
    private String vendedores;

    // Constructor vacío requerido por JPA
    public Configuracion() {}

    // ── GETTERS Y SETTERS ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getVendedores() { return vendedores; }
    public void setVendedores(String vendedores) { this.vendedores = vendedores; }
}
