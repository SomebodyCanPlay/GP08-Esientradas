package edu.esi.ds.esientradas.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import edu.esi.ds.esientradas.services.ReservasService;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

// ============================================================
// CONTROLADOR DE RESERVAS
// ============================================================
// Gestiona la prerreserva de entradas a través de la sesión HTTP de Spring.
//
// Nota: este controlador usa HttpSession (la sesión del servidor de Spring),
// mientras que ComprasController usa el sessionId que manda el frontend.
// Ambos son válidos; este es un endpoint más "clásico".
//
// La lógica real de reserva está en ReservasService.
// ============================================================
@RestController
@RequestMapping("/reservas")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")

public class ReservasController {

    @Autowired
    private ReservasService service;

    // ──────────────────────────────────────────────────────────
    // PUT /reservas/reservar?idEntrada=X&token=Y (token opcional)
    //
    // Bloquea la entrada X para la sesión actual del usuario.
    // Va acumulando el precio total en la sesión de Spring (session.getAttribute).
    //
    // Devuelve: { "token": "xxxxx", "precioTotal": 5000 }
    // precioTotal → suma del precio de todas las entradas reservadas hasta ahora
    // token → para identificar esta reserva en el pago
    // ──────────────────────────────────────────────────────────
    @PutMapping("/reservar")
    public Map<String, Object> reservar(
            HttpSession session, // sesión HTTP de Spring (automática)
            @RequestParam Long idEntrada, // qué entrada quiere reservar
            @RequestParam(required = false) String token // token previo del carrito (opcional)
    ) {
        // Delegamos al servicio la lógica de reserva
        Map<String, Object> reservaResult = this.service.reservar(idEntrada, session.getId(), token);

        // Precio de esta entrada concreta
        Long precioEntrada = (Long) reservaResult.get("precioEntrada");
        String tokenResult = (String) reservaResult.get("token");

        // Acumulamos el precio total en la sesión
        // session.getAttribute("precioTotal") → recupera el total previo (si existe)
        Long precioTotal = (Long) session.getAttribute("precioTotal");
        if (precioTotal == null) {
            // Primera entrada del carrito → el total es solo esta entrada
            precioTotal = precioEntrada;
        } else {
            // Ya había entradas → sumamos
            precioTotal += precioEntrada;
        }
        // Guardamos el nuevo total en la sesión para la próxima llamada
        session.setAttribute("precioTotal", precioTotal);

        // Devolvemos el token y el precio total acumulado al frontend
        Map<String, Object> response = new HashMap<>();
        response.put("token", tokenResult);
        response.put("precioTotal", precioTotal);
        return response;
    }
}
