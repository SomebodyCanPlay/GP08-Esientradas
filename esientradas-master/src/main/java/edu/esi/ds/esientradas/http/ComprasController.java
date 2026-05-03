package edu.esi.ds.esientradas.http;

import edu.esi.ds.esientradas.services.ColaService;
import edu.esi.ds.esientradas.services.UsuarioService; // Asegúrate de tener este servicio
import edu.esi.ds.esientradas.services.PagosService; // Y este para la firma
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/compras")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ComprasController {

    @Autowired
    private ColaService colaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PagosService pagosService;

    @Autowired
    private edu.esi.ds.esientradas.services.ReservasService reservasService;

    // Endpoint para que el frontend haga el seguimiento de su turno y se anote
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> check(@RequestParam String sessionId) {
        int posicion = colaService.anotarseEnCola(sessionId);
        boolean canPass = colaService.canPass(sessionId);
        
        return ResponseEntity.ok(Map.of(
            "canPass", canPass,
            "posicion", canPass ? 0 : posicion
        ));
    }

    // Endpoint para bloquear la entrada por 10 minutos
    @PostMapping("/prereservar")
    public ResponseEntity<?> preReservar(@RequestParam String sessionId, @RequestBody Map<String, Object> payload) {
        if (!colaService.canPass(sessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Debes esperar tu turno.");
        }

        Long idEntrada = Long.valueOf(payload.get("idEntrada").toString());
        String tokenValor = (payload.containsKey("token") && payload.get("token") != null) ? payload.get("token").toString() : null;

        try {
            Map<String, Object> resultado = reservasService.reservar(idEntrada, sessionId, tokenValor);
            return ResponseEntity.ok(resultado);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno procesando reserva"));
        }
    }

    // Endpoint de confirmación de compra (Punto 5 central)
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarCompra(@RequestParam String sessionId, @RequestBody Map<String, String> payload) {
        String userToken = payload.get("token");

        // 1. VALIDACIÓN DE COLA: ¿Es su turno?
        if (!colaService.canPass(sessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Error: Debes esperar tu turno en la cola."));
        }

        // 2. COMUNICACIÓN ENTRE SUBSISTEMAS: Validar token en esiusuarios
        String email = usuarioService.checkToken(userToken);

        // Dentro de confirmarCompra en ComprasController
        if (email != null && !email.isEmpty()) {
            // Usamos el nombre exacto de tu PagosService.java
            this.pagosService.firmarPago(userToken, email);

            colaService.finalizarCompra(sessionId);
            return ResponseEntity.ok(Map.of("status", "success"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token de usuario no válido"));
    }
}