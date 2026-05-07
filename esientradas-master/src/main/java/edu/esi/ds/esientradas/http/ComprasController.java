package edu.esi.ds.esientradas.http;

import edu.esi.ds.esientradas.services.ColaService;
import edu.esi.ds.esientradas.services.PagosService;
import edu.esi.ds.esientradas.services.ReservasService;
import edu.esi.ds.esientradas.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// ============================================================
// CONTROLADOR DE COMPRAS — el más importante del sistema
// ============================================================
// Coordina el flujo completo de compra de entradas:
//
//   PASO 1: /compras/check    → el usuario se apunta a la cola y sabe su posición
//   PASO 2: /compras/prereservar → bloquea la entrada (la "pone en el carrito")
//   PASO 3: /compras/cancelar → si el usuario cambia de opinión
//   PASO 4: /compras/confirmar → confirma el pago y finaliza la compra
//
// En el PASO 4 se produce la comunicación entre microservicios:
//   ComprasController → UsuarioService → esiusuarios (GET /checkToken)
//   Si esiusuarios confirma el token → PagosService.firmarPagosPorSession()
// ============================================================
@RestController
@RequestMapping("/compras")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ComprasController {

    @Autowired
    private ColaService colaService;

    // UsuarioService se comunica con el microservicio esiusuarios
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PagosService pagosService;

    @Autowired
    private ReservasService reservasService;

    // ──────────────────────────────────────────────────────────
    // GET /compras/check?sessionId=...
    // El frontend llama a esto periódicamente para saber su turno en la cola.
    // Devuelve: { "canPass": true/false, "posicion": N }
    //   posicion 0 → es su turno (puede pasar a ver entradas)
    //   posicion N → hay N-1 personas delante
    // ──────────────────────────────────────────────────────────
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> check(@RequestParam String sessionId) {
        // anotarseEnCola añade al usuario si no estaba ya, y devuelve su posición
        int posicion = colaService.anotarseEnCola(sessionId);
        boolean canPass = colaService.canPass(sessionId);

        return ResponseEntity.ok(Map.of(
            "canPass", canPass,
            "posicion", canPass ? 0 : posicion
        ));
    }

    // ──────────────────────────────────────────────────────────
    // POST /compras/prereservar?sessionId=...
    // Body: { "idEntrada": 42, "token": "xxxxx" (opcional) }
    //
    // Bloquea la entrada por 10 minutos (la "mete en el carrito").
    // Solo funciona si el usuario es su turno en la cola (canPass = true).
    // Devuelve: { "token": "xxxxx", "precioEntrada": 2500 }
    // ──────────────────────────────────────────────────────────
    @PostMapping("/prereservar")
    public ResponseEntity<?> preReservar(@RequestParam String sessionId, @RequestBody Map<String, Object> payload) {
        if (!colaService.canPass(sessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Debes esperar tu turno.");
        }

        Long idEntrada = Long.valueOf(payload.get("idEntrada").toString());
        // El token es opcional: si ya tiene uno (de una entrada previa en el carrito) lo reutiliza
        String tokenValor = (payload.containsKey("token") && payload.get("token") != null)
                ? payload.get("token").toString() : null;

        try {
            Map<String, Object> resultado = reservasService.reservar(idEntrada, sessionId, tokenValor);
            return ResponseEntity.ok(resultado);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno procesando reserva"));
        }
    }

    // ──────────────────────────────────────────────────────────
    // POST /compras/cancelar?sessionId=...
    // Body: { "idEntrada": 42 }
    //
    // Libera la entrada que el usuario había prerreservado (quita del carrito).
    // La entrada vuelve a estar DISPONIBLE para otros usuarios.
    // ──────────────────────────────────────────────────────────
    @PostMapping("/cancelar")
    public ResponseEntity<?> cancelarReserva(@RequestParam String sessionId, @RequestBody Map<String, Object> payload) {
        Long idEntrada = Long.valueOf(payload.get("idEntrada").toString());
        try {
            reservasService.cancelarReserva(idEntrada, sessionId);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno cancelando reserva"));
        }
    }

    // ──────────────────────────────────────────────────────────
    // POST /compras/confirmar?sessionId=...
    // Body: { "token": "token_de_sesion_de_esiusuarios" }
    //
    // PASO FINAL: confirma la compra de todas las entradas del carrito.
    // Pasos internos:
    //   1. Verifica que el usuario tiene turno en la cola
    //   2. Llama a esiusuarios para validar el token del usuario
    //   3. Si el token es válido → firma el pago (VENDIDA) y envía PDF/email
    //   4. Libera el turno en la cola para el siguiente usuario
    // ──────────────────────────────────────────────────────────
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarCompra(@RequestParam String sessionId, @RequestBody Map<String, String> payload) {
        String userToken = payload.get("token"); // token de sesión de esiusuarios

        // 1. ¿Es su turno en la cola?
        if (!colaService.canPass(sessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Debes esperar tu turno en la cola."));
        }

        // 2. Comunicación con esiusuarios: ¿el token es válido? ¿quién es el usuario?
        //    GET http://localhost:8081/checkToken?token=xxxxx
        //    Si es válido → devuelve el email del usuario
        //    Si no → devuelve null
        String email = usuarioService.checkToken(userToken);

        if (email != null && !email.isEmpty()) {
            // 3. Confirmar todas las entradas prerreservadas de esta sesión
            this.pagosService.firmarPagosPorSession(sessionId, email);

            // 4. Liberar el puesto en la cola → el siguiente usuario puede entrar
            colaService.finalizarCompra(sessionId);

            return ResponseEntity.ok(Map.of("status", "success"));
        }

        // Si el token de esiusuarios no es válido → el usuario no está autenticado
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token de usuario no válido"));
    }
}