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

// Controlador de búsqueda: expone endpoints para buscar escenarios, espectáculos y entradas.
@RestController
@RequestMapping("/busqueda")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class BusquedaController {

    @Autowired
    private BusquedaService service;

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private ColaService colaService;

    // Devuelve todas las entradas de un espectáculo (requiere cola).
    @GetMapping("/getEntradas")
    public List<Entrada> getEntradas(@RequestParam Long espectaculoid, @RequestParam String sessionId) {
        if (!colaService.canPass(sessionId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debe esperar en la cola");
        }
        return this.entradaDao.findByEspectaculoId(espectaculoid);
    }

    // Devuelve todos los escenarios.
    @GetMapping("/getEscenarios")
    public List<Escenario> getEscenarios() {
        return this.service.getEscenarios();
    }

    // Busca espectáculos por artista (público).
    @GetMapping("/getEspectaculos")
    public List<Espectaculo> getEspectaculos(@RequestParam String artista, @RequestParam String sessionId) {
        return this.service.getEspectaculos(artista);
    }

    // Filtra espectáculos por escenario (público).
    @GetMapping("/getEspectaculosPorEscenario")
    public List<Espectaculo> getEspectaculosPorEscenario(@RequestParam Long escenarioId,
            @RequestParam String sessionId) {
        return this.service.getEspectaculosPorEscenario(escenarioId);
    }

    // Devuelve resumen de entradas { total, libres, reservadas, vendidas }.
    @GetMapping("/getResumenEntradas")
    public DtoEntradas getNumeroDeEntradasComoDto(@RequestParam Long espectaculoId, @RequestParam String sessionId) {
        return this.service.getNumeroDeEntradasComoDto(espectaculoId);
    }

    // Devuelve el número total de entradas.
    @GetMapping("/getNumeroEntradas")
    public Integer getNumeroEntradas(@RequestParam Long espectaculoId) {
        return this.service.getNumeroEntradasDisponibles(espectaculoId);
    }
}