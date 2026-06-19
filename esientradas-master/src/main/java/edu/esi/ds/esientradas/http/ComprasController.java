package edu.esi.ds.esientradas.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.esi.ds.esientradas.services.ColaService;
import edu.esi.ds.esientradas.services.PagosService;
import edu.esi.ds.esientradas.services.ReservasService;
import edu.esi.ds.esientradas.services.UsuarioService;

// Controlador de compras: gestiona cola, prerreserva y pagos.
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
    private ReservasService reservasService;

    // Comprueba y anota en la cola para un espectáculo.
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> check(@RequestParam Long espectaculoId, @RequestParam String sessionId) {
        int posicion = colaService.anotarseEnCola(espectaculoId, sessionId);
        boolean canPass = colaService.canPass(espectaculoId, sessionId);

        return ResponseEntity.ok(Map.of(
                "canPass", canPass,
                "posicion", canPass ? 0 : posicion));
    }

    // Pre-reserva una entrada (requiere estar en la cola).
    @PostMapping("/prereservar")
    public ResponseEntity<?> preReservar(@RequestParam String sessionId, @RequestBody Map<String, Object> payload) {
        if (!colaService.canPass(sessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Debes esperar tu turno.");
        }

        Long idEntrada = Long.valueOf(payload.get("idEntrada").toString());
        String tokenValor = (payload.containsKey("token") && payload.get("token") != null)
                ? payload.get("token").toString()
                : null;

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

    // Cancelar una reserva.
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

    // Inicia el proceso de pago (requiere usuario válido y estar en la cola).
    @PostMapping("/iniciarPago")
    public ResponseEntity<?> iniciarPago(@RequestParam String sessionId, @RequestBody Map<String, String> payload) {
        String userToken = payload.get("token");

        if (!colaService.canPass(sessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Debes esperar tu turno en la cola."));
        }

        String email = usuarioService.checkToken(userToken);
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token de usuario no válido"));
        }

        try {
            java.util.Map<String, Object> resultado = pagosService.iniciarPago(sessionId, email);
            return ResponseEntity.ok(resultado);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) { // Es buena práctica atrapar excepciones de Stripe aquí
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al iniciar el pago con Stripe"));
        }
    }

    // Confirma la compra después del pago y finaliza la cola.
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarCompra(@RequestParam String sessionId, @RequestBody Map<String, String> payload) {
        String userToken = payload.get("token");

        if (!colaService.canPass(sessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Debes esperar tu turno en la cola."));
        }

        String email = usuarioService.checkToken(userToken);

        if (email != null && !email.isEmpty()) {
            this.pagosService.firmarPagosPorSession(sessionId, email);
            colaService.finalizarCompra(sessionId);
            return ResponseEntity.ok(Map.of("status", "success"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token de usuario no válido"));
    }

    // Devuelve las IDs de las reservas de la sesión.
    @GetMapping("/misReservas")
    public ResponseEntity<java.util.List<Long>> misReservas(@RequestParam String sessionId) {
        java.util.List<Long> ids = reservasService.getIdsReservados(sessionId);
        return ResponseEntity.ok(ids);
    }
}