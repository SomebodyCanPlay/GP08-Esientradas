package edu.esi.ds.esientradas.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.services.EscenarioService;

// ============================================================
// CONTROLADOR DE ESCENARIOS — administración de recintos
// ============================================================
// Permite a los administradores crear nuevos escenarios en el sistema.
// Los usuarios finales solo leen escenarios (a través de BusquedaController).
//
// @RequestMapping("/escenarios") → todas las rutas empiezan por /escenarios
// (Sin @CrossOrigin porque solo lo usa el admin, no el frontend Angular)
// ============================================================
@RestController
@RequestMapping("/escenarios")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class EscenarioController {

    @Autowired
    private EscenarioService service;

    // POST /escenarios/insertar
    // Body: { "nombre": "Wizink Center", "descripcion": "Pabellón cubierto en
    // Madrid" }
    //
    // Valida que los campos obligatorios no estén vacíos y llama al servicio.
    // Si todo está bien: devuelve 200 OK (vacío)
    // Si falta algún campo: devuelve 400 Bad Request con mensaje de error
    @PostMapping("/insertar")
    public void insertar(@RequestBody Escenario escenario) {

        // Validación del nombre — no puede estar vacío
        if (escenario.getNombre() == null || escenario.getNombre().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del escenario no puede ser nulo o vacío.");
        }

        // Validación de la descripción — tampoco puede estar vacía
        if (escenario.getDescripcion() == null || escenario.getDescripcion().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La descripción del escenario no puede ser nula o vacía.");
        }

        // Si las validaciones pasan, delegamos al servicio para guardar en BD
        this.service.insertar(escenario);
    }
}
