package edu.esi.ds.esientradas.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import edu.esi.ds.esientradas.model.Espectaculo;

// DAO de Espectaculo: extiende JpaRepository y añade búsquedas por campos comunes.
public interface EspectaculoDao extends JpaRepository<Espectaculo, Long> {

    // Busca todos los espectáculos de un artista.
    List<Espectaculo> findByArtista(String artista);

    // Busca todos los espectáculos que se celebran en un escenario.
    List<Espectaculo> findByEscenarioId(Long escenarioId);
}
