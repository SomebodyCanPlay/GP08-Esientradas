package edu.esi.ds.esientradas.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dto.DtoEntradas;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.services.BusquedaService;
import edu.esi.ds.esientradas.services.ColaService;
import java.util.List;

// ============================================================
// CONTROLADOR DE BÚSQUEDA
// ============================================================
// Este controlador es la "puerta de entrada" para las búsquedas del frontend.
// El frontend (Angular en puerto 4200) llama a estos endpoints para:
//   - Ver qué escenarios existen
//   - Buscar espectáculos por artista
//   - Ver las entradas de un espectáculo (el plano de asientos)
//
// @CrossOrigin → permite peticiones desde http://localhost:4200
//   (sin esto, el navegador bloquearía las peticiones por CORS)
//
// Todos los endpoints que muestran entradas comprueban la COLA:
//   si el usuario no está autorizado (no es su turno), devuelve 403 Forbidden
// ============================================================
@RestController
@RequestMapping("/busqueda")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class BusquedaController {

    @Autowired
    private BusquedaService service;

    @Autowired
    private EntradaDao entradaDao;

    // ColaService para verificar si el usuario puede acceder
    @Autowired
    private ColaService colaService;

    // ──────────────────────────────────────────────────────────
    // GET /busqueda/getEntradas?espectaculoid=X&sessionId=Y
    // Devuelve todas las entradas de un espectáculo (el plano de asientos)
    // Requiere estar autorizado en la cola
    // ──────────────────────────────────────────────────────────
    @GetMapping("/getEntradas")
    public List<Entrada> getEntradas(@RequestParam Long espectaculoid, @RequestParam String sessionId) {
        if (!colaService.canPass(sessionId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debe esperar en la cola");
        }
        return this.entradaDao.findByEspectaculoId(espectaculoid);
    }

    // ──────────────────────────────────────────────────────────
    // GET /busqueda/getEscenarios
    // Devuelve todos los recintos disponibles (sin necesidad de estar en la cola)
    // El frontend los muestra en el filtro de búsqueda
    // ──────────────────────────────────────────────────────────
    @GetMapping("/getEscenarios")
    public List<Escenario> getEscenarios() {
        return this.service.getEscenarios();
    }

    // ──────────────────────────────────────────────────────────
    // GET /busqueda/getEspectaculos?artista=X&sessionId=Y
    // Busca espectáculos por nombre de artista
    // Requiere estar autorizado en la cola
    // ──────────────────────────────────────────────────────────
    @GetMapping("/getEspectaculos")
    public List<Espectaculo> getEspectaculos(@RequestParam String artista, @RequestParam String sessionId) {
        // Hemos quitado la comprobación de la cola aquí para que la búsqueda sea
        // pública
        return this.service.getEspectaculos(artista);
    }

    // ──────────────────────────────────────────────────────────
    // GET /busqueda/getEspectaculosPorEscenario?escenarioId=X&sessionId=Y
    // Filtra los espectáculos por recinto
    // ──────────────────────────────────────────────────────────
    @GetMapping("/getEspectaculosPorEscenario")
    public List<Espectaculo> getEspectaculosPorEscenario(@RequestParam Long escenarioId,
            @RequestParam String sessionId) {
        // Hemos quitado la comprobación de la cola aquí para que el listado sea público
        return this.service.getEspectaculosPorEscenario(escenarioId);
    }

    // ──────────────────────────────────────────────────────────
    // GET /busqueda/getResumenEntradas?espectaculoId=X&sessionId=Y
    // Devuelve { total, libres, reservadas, vendidas }
    // Sirve para mostrar "Quedan X entradas disponibles" en el frontend
    // ──────────────────────────────────────────────────────────
    @GetMapping("/getResumenEntradas")
    public DtoEntradas getNumeroDeEntradasComoDto(@RequestParam Long espectaculoId, @RequestParam String sessionId) {
        // Quitamos la cola para que se pueda ver cuántas entradas quedan desde la
        // cartelera
        return this.service.getNumeroDeEntradasComoDto(espectaculoId);
    }

    // ──────────────────────────────────────────────────────────
    // GET /busqueda/getNumeroEntradas?espectaculoId=X
    // Devuelve el número total de entradas (sin filtro de cola)
    // ──────────────────────────────────────────────────────────
    @GetMapping("/getNumeroEntradas")
    public Integer getNumeroEntradas(@RequestParam Long espectaculoId) {
        return this.service.getNumeroEntradasDisponibles(espectaculoId);
    }
}