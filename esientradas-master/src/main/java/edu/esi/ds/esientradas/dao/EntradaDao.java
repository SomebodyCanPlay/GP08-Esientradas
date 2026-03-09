package edu.esi.ds.esientradas.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.dto.DtoEntradas;

public interface EntradaDao extends JpaRepository<Entrada, Long> {
    List<Entrada> findByEspectaculoId(Long espectaculoId);

    @Query(value = "UPDATE Entrada e SET e.estado = :estado WHERE e.id = :idEntrada")
    @Modifying
    void updateEstado(@Param("idEntrada") Long idEntrada, @Param("estado") Estado estado);

    @Query("""
        SELECT COUNT(*) AS total,
        SUM(estado = 'DISPONIBLE') AS libres,
        SUM(estado = 'RESERVADA') AS reservadas,
        SUM(estado = 'VENDIDA') AS vendidas
        FROM Entrada e
        WHERE e.espectaculo_id = :espectaculoId""")
    DtoEntradas getNumeroDeEntradasComoDto(@Param("espectaculoId") Long espectaculoId);
}