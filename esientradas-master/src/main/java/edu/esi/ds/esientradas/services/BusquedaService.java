package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.dao.*;
import edu.esi.ds.esientradas.dto.DtoEntradas;

// ============================================================
// Servicio de búsquedas: operaciones para obtener escenarios, espectáculos y entradas.
// ============================================================
@Service
public class BusquedaService {

    // DAO para acceder a las entradas en la BD
    @Autowired
    private EntradaDao entradaDao;

    // DAO para acceder a los escenarios en la BD
    @Autowired
    private EscenarioDao dao;

    // DAO para acceder a los espectáculos en la BD
    @Autowired
    private EspectaculoDao espectaculoDao;

    // Devuelve todos los escenarios (recintos) disponibles
    // El frontend los muestra en un desplegable para filtrar espectáculos
    public List<Escenario> getEscenarios() {
        return this.dao.findAll();
    }

    // Busca espectáculos por nombre de artista
    // Ejemplo: getEspectaculos("Aitana") → todos los conciertos de Aitana
    public List<Espectaculo> getEspectaculos(String artista) {
        return this.espectaculoDao.findByArtista(artista);
    }

    // Busca espectáculos que se celebran en un escenario concreto
    // El frontend pasa el ID del escenario seleccionado en el filtro
    public List<Espectaculo> getEspectaculosPorEscenario(Long escenarioId) {
        return this.espectaculoDao.findByEscenarioId(escenarioId);
    }

    // Devuelve las entradas de un espectáculo.
    // El frontend muestra el plano del recinto con cada asiento
    public List<Entrada> getEntradas(String espectaculoId) {
        return this.espectaculoDao
                .findById(Long.parseLong(espectaculoId))
                .orElseThrow() // lanza excepción si no existe el espectáculo
                .getEntradas();
    }

    // Cuenta el TOTAL de entradas de un espectáculo.
    public Integer getNumeroEntradasDisponibles(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoId(espectaculoId);
    }

    // Cuenta solo las entradas que están DISPONIBLES.
    public Integer getEntradasLibres(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
    }

    // Devuelve resumen con total, libres, reservadas y vendidas.
    public DtoEntradas getNumeroDeEntradasComoDto(Long espectaculoId) {
        int total = entradaDao.countByEspectaculoId(espectaculoId);
        int libres = entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
        int reservadas = entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.RESERVADA);
        int vendidas = entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.VENDIDA);

        // DtoEntradas agrupa estos cuatro contadores.
        return new DtoEntradas(total, libres, reservadas, vendidas);
    }
}