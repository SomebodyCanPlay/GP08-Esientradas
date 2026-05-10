package edu.esi.ds.esientradas.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import edu.esi.ds.esientradas.services.ReservasService;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

// Controlador de reservas: gestiona prerreservas mediante HttpSession.
@RestController
@RequestMapping("/reservas")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")

public class ReservasController {

    @Autowired
    private ReservasService service;

    // Reserva una entrada para la sesión actual y acumula el precio en la sesión.
    @PutMapping("/reservar")
    public Map<String, Object> reservar(
            HttpSession session,
            @RequestParam Long idEntrada,
            @RequestParam(required = false) String token
    ) {
        // Delegamos al servicio la lógica de reserva
        Map<String, Object> reservaResult = this.service.reservar(idEntrada, session.getId(), token);

        Long precioEntrada = (Long) reservaResult.get("precioEntrada");
        String tokenResult = (String) reservaResult.get("token");

        // Acumulamos el precio total en la sesión
        Long precioTotal = (Long) session.getAttribute("precioTotal");
        if (precioTotal == null) {
            precioTotal = precioEntrada;
        } else {
            precioTotal += precioEntrada;
        }
        session.setAttribute("precioTotal", precioTotal);

        Map<String, Object> response = new HashMap<>();
        response.put("token", tokenResult);
        response.put("precioTotal", precioTotal);
        return response;
    }
}
