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
// SERVICIO DE BÚSQUEDA
// ============================================================
// Este servicio responde a todas las búsquedas del usuario en el frontend:
//   - Buscar escenarios (recintos donde hay espectáculos)
//   - Buscar espectáculos por artista
//   - Ver las entradas disponibles de un espectáculo
//   - Contar cuántas entradas quedan libres
//
// El BusquedaController recibe la petición HTTP del frontend
// y delega el trabajo aquí. Este servicio consulta los DAOs y devuelve resultados.
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

    // Devuelve las entradas de un espectáculo (todas, sin filtrar por estado)
    // El frontend muestra el plano del recinto con cada asiento
    public List<Entrada> getEntradas(String espectaculoId) {
        return this.espectaculoDao
                .findById(Long.parseLong(espectaculoId))
                .orElseThrow() // lanza excepción si no existe el espectáculo
                .getEntradas();
    }

    // Cuenta el TOTAL de entradas de un espectáculo (vendidas + libres +
    // reservadas)
    public Integer getNumeroEntradasDisponibles(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoId(espectaculoId);
    }

    // Cuenta solo las entradas que están DISPONIBLES (= se pueden comprar ahora)
    public Integer getEntradasLibres(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
    }

    // Devuelve un resumen con los 4 contadores: total, libres, reservadas y
    // vendidas
    // Usado por el panel de administración o el frontend para mostrar "quedan X
    // entradas"
    public DtoEntradas getNumeroDeEntradasComoDto(Long espectaculoId) {
        int total = entradaDao.countByEspectaculoId(espectaculoId);
        int libres = entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
        int reservadas = entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.RESERVADA);
        int vendidas = entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.VENDIDA);

        // DtoEntradas es un objeto simple que agrupa estos 4 números para mandarlo al
        // frontend
        return new DtoEntradas(total, libres, reservadas, vendidas);
    }
}
