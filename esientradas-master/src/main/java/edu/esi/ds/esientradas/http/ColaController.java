package edu.esi.ds.esientradas.http;

import edu.esi.ds.esientradas.services.ColaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

// ============================================================
// CONTROLADOR DE COLA DE ESPERA
// ============================================================
// Expone un único endpoint que usa el frontend para saber si puede pasar.
//
// El frontend de Angular lo llama repetidamente (polling):
//   - Cada X segundos llama a GET /cola/check?sessionId=...
//   - Si la respuesta es { "canPass": true } → puede pasar a buscar entradas
//   - Si la respuesta es { "canPass": false } → sigue esperando (muestra pantalla de cola)
//
// La lógica real de gestión de la cola está en ColaService.
// ============================================================
@RestController
@RequestMapping("/cola")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ColaController {

    // Inyección por constructor (alternativa válida a @Autowired en el campo)
    // Es la forma recomendada porque hace más claro qué necesita la clase
    private final ColaService colaService;

    public ColaController(ColaService colaService) {
        this.colaService = colaService;
    }

    // ──────────────────────────────────────────────────────────
    // GET /cola/check?sessionId=...
    // Devuelve: { "canPass": true } → el usuario puede pasar a comprar
    //           { "canPass": false } → debe esperar en la cola
    //
    // El frontend llama a este endpoint cada pocos segundos
    // mientras el usuario está en la pantalla de espera
    // ──────────────────────────────────────────────────────────
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> check(@RequestParam String sessionId) {
        boolean canPass = colaService.canPass(sessionId);
        // Collections.singletonMap → crea un Map con una sola entrada { "canPass": true/false }
        return ResponseEntity.ok(Collections.singletonMap("canPass", canPass));
    }
}