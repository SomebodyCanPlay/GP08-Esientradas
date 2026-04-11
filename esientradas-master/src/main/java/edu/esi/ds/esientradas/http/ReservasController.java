package edu.esi.ds.esientradas.http;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.esi.ds.esientradas.services.ReservasService;
import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/reservas")
public class ReservasController {

    @Autowired
    private ReservasService service;

    @PutMapping("/reservar")
    public Map<String, Object> reservar(HttpSession session,
                                       @RequestParam Long idEntrada,
                                       @RequestParam(required = false) String token) {

        // service.reservar debe aceptar el token (puede ser null) y devolver
        // un Map con keys "token" (String) y "precioEntrada" (Long).
        Map<String, Object> reservaResult = this.service.reservar(idEntrada, session.getId(), token);

        Long precioEntrada = (Long) reservaResult.get("precioEntrada");
        String tokenResult = (String) reservaResult.get("token");

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
