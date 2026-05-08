package edu.esi.ds.esientradas.http;

import edu.esi.ds.esientradas.services.ColaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/cola")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ColaController {

    private final ColaService colaService;

    public ColaController(ColaService colaService) {
        this.colaService = colaService;
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> check(
            @RequestParam Long espectaculoId,
            @RequestParam String sessionId) {

        int posicion = colaService.anotarseEnCola(espectaculoId, sessionId);
        boolean canPass = colaService.canPass(espectaculoId, sessionId);

        Map<String, Object> response = new HashMap<>();
        response.put("canPass", canPass);
        response.put("posicion", posicion);

        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────────────────
    // GET o POST /cola/abandonar?sessionId=...
    // Se llama INSTANTÁNEAMENTE cuando el usuario cierra la pestaña
    // o le da hacia atrás, para que el siguiente de la cola entre ya.
    // ──────────────────────────────────────────────────────────
    @RequestMapping(value = "/abandonar", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<Map<String, String>> abandonar(@RequestParam String sessionId) {
        colaService.finalizarCompra(sessionId);
        return ResponseEntity.ok(Collections.singletonMap("status", "abandonado"));
    }
}