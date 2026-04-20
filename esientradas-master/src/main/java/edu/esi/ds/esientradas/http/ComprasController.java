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

    // Endpoint de confirmación de compra (Punto 5 central)
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarCompra(HttpSession session, @RequestBody Map<String, String> payload) {
        String sessionId = session.getId();
        String userToken = payload.get("token");

        // 1. VALIDACIÓN DE COLA: ¿Es su turno?
        if (!colaService.canPass(sessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: Debes esperar tu turno en la cola.");
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

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de usuario no válido");
    }
}