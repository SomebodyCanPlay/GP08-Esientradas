package edu.esi.ds.esientradas.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;

// DAO de Entrada: extiende JpaRepository y ofrece consultas específicas.
public interface EntradaDao extends JpaRepository<Entrada, Long> {

    // Todas las entradas de un espectáculo.
    List<Entrada> findByEspectaculoId(Long espectaculoId);
    
    // Entradas por estado.
    List<Entrada> findByEstado(Estado estado);

    // Cuenta entradas de un espectáculo.
    Integer countByEspectaculoId(Long espectaculoId);

    // Cuenta entradas por espectáculo y estado.
    Integer countByEspectaculoIdAndEstado(Long espectaculoId, Estado estado);

    // Actualiza estado de una entrada (UPDATE).
    @Query(value = "UPDATE Entrada e SET e.estado = :estado WHERE e.id = :idEntrada")
    @Modifying
    void updateEstado(@Param("idEntrada") Long idEntrada, @Param("estado") Estado estado);

    // Devuelve estadísticas agregadas de entradas de un espectáculo.
    @Query("""
            SELECT COUNT(*) AS total,
            SUM(estado = 'DISPONIBLE') AS libres,
            SUM(estado = 'RESERVADA') AS reservadas,
            SUM(estado = 'VENDIDA') AS vendidas
            FROM Entrada e
            WHERE e.espectaculo.id = :espectaculoId""")
    Object getNumeroEntradasComoDto(@Param("espectaculoId") Long espectaculoId);

    // PARA CANCELACIONES 
    Optional<Entrada> findByTokenCancelacion(String tokenCancelacion);
    
}