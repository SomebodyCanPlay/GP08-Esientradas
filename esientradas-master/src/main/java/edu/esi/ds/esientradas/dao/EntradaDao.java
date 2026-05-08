package edu.esi.ds.esientradas.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;

// ============================================================
// DAO (Data Access Object) de Entrada
// ============================================================
// Esta interfaz es el puente entre nuestro código Java y la tabla "entrada" de MySQL.
//
// ¿Qué es JpaRepository<Entrada, Long>?
// Es una interfaz mágica de Spring. Al extenderla, obtenemos GRATIS los métodos básicos:
//   - save(entrada)        → INSERT o UPDATE en la BD
//   - findById(id)         → SELECT WHERE id = ?
//   - findAll()            → SELECT * FROM entrada
//   - delete(entrada)      → DELETE WHERE id = ?
// No hace falta escribir SQL para estos casos.
//
// Además añadimos métodos personalizados:
// Spring los implementa automáticamente si sigues la convención de nombres
// "findBy + NombreCampo" → genera el SQL correspondiente
// ============================================================
public interface EntradaDao extends JpaRepository<Entrada, Long> {

    // Devuelve todas las entradas de un espectáculo concreto
    // SQL generado: SELECT * FROM entrada WHERE espectaculo_id = ?
    List<Entrada> findByEspectaculoId(Long espectaculoId);

    // Devuelve todas las entradas con un estado concreto
    // SQL generado: SELECT * FROM entrada WHERE estado = ?
    // Muy usado por ReservaCleanUpTask para encontrar entradas RESERVADAS
    List<Entrada> findByEstado(Estado estado);

    // Cuenta cuántas entradas tiene un espectáculo (total, sin filtro de estado)
    // SQL generado: SELECT COUNT(*) FROM entrada WHERE espectaculo_id = ?
    Integer countByEspectaculoId(Long espectaculoId);

    // Cuenta entradas de un espectáculo filtrando también por estado
    // Ejemplo: countByEspectaculoIdAndEstado(1L, DISPONIBLE) → cuántas libres
    // quedan
    Integer countByEspectaculoIdAndEstado(Long espectaculoId, Estado estado);

    // @Query → aquí SÍ escribimos el SQL/JPQL nosotros porque Spring no puede
    // deducirlo solo del nombre del método (es una UPDATE, no una SELECT)
    // @Modifying → es obligatorio en updates y deletes
    // @Param → vincula el parámetro Java con el :nombre en la query
    @Query(value = "UPDATE Entrada e SET e.estado = :estado WHERE e.id = :idEntrada")
    @Modifying
    void updateEstado(@Param("idEntrada") Long idEntrada, @Param("estado") Estado estado);

    // Devuelve estadísticas agregadas de entradas de un espectáculo en un solo
    // objeto
    // SQL: COUNT total, SUM de disponibles, reservadas y vendidas
    // Usado en el panel de administración para mostrar el resumen de venta
    @Query("""
            SELECT COUNT(*) AS total,
            SUM(estado = 'DISPONIBLE') AS libres,
            SUM(estado = 'RESERVADA') AS reservadas,
            SUM(estado = 'VENDIDA') AS vendidas
            FROM Entrada e
            WHERE e.espectaculo.id = :espectaculoId""")
    Object getNumeroEntradasComoDto(@Param("espectaculoId") Long espectaculoId);
}