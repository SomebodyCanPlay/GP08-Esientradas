package edu.esi.ds.esientradas.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.services.EscenarioService;

// Controlador de escenarios: gestión para administradores.
@RestController
@RequestMapping("/escenarios")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class EscenarioController {

    @Autowired
    private EscenarioService service;

    // Inserta un nuevo escenario tras validar campos obligatorios.
    @PostMapping("/insertar")
    public void insertar(@RequestBody Escenario escenario) {

        if (escenario.getNombre() == null || escenario.getNombre().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del escenario no puede ser nulo o vacío.");
        }

        if (escenario.getDescripcion() == null || escenario.getDescripcion().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La descripción del escenario no puede ser nula o vacía.");
        }

        this.service.insertar(escenario);
    }
}
