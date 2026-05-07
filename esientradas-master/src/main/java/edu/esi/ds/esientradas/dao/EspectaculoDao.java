package edu.esi.ds.esientradas.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import edu.esi.ds.esientradas.model.Espectaculo;

// ============================================================
// DAO de Espectaculo
// ============================================================
// Extiende JpaRepository → ya tiene save(), findById(), findAll(), delete()...
//
// Métodos de búsqueda personalizados:
// Spring los implementa automáticamente usando la convención de nombres
// ============================================================
public interface EspectaculoDao extends JpaRepository<Espectaculo, Long> {

    // Busca todos los espectáculos de un artista
    // SQL generado: SELECT * FROM espectaculo WHERE artista = ?
    // Ejemplo: findByArtista("Coldplay") → todos los conciertos de Coldplay
    List<Espectaculo> findByArtista(String artista);

    // Busca todos los espectáculos que se celebran en un escenario concreto
    // SQL generado: SELECT * FROM espectaculo WHERE escenario_id = ?
    List<Espectaculo> findByEscenarioId(Long escenarioId);
}
