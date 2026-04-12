package edu.esi.ds.esientradas.http;

import java.util.Map;
import edu.esi.ds.esientradas.services.UsuarioService;
import edu.esi.ds.esientradas.services.PagosService; // ...nuevo...

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/compras")
public class ComprasController {

    @Autowired
    private UsuarioService usuarioService;

    // Inyectar PagosService para firmar la venta cuando tengamos el email real
    @Autowired
    private PagosService pagosService;

    @PutMapping("/comprar")
    public void comprar(HttpSession session,HttpServletResponse response, @RequestBody String userToken) {
        String sessionId = session.getId();
        if (userToken == null || userToken.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", "http://www.uclm.es");
            return;
        }

        // Llamada a servicio de usuarios para validar token y obtener email del usuario
        String userEmail = this.usuarioService.checkToken(userToken);

        // Si obtenemos un email, pasar token+email a PagosService para que firme la venta
        if (userEmail != null && !userEmail.isEmpty()) {
            // El nuevo método firmarPago(String tokenValor, String userEmail) procesará
            // la entrada asociada al token y enviará la confirmación al email.
            this.pagosService.firmarPago(userToken, userEmail);
            return;
        }

        // Si no hay email, mantener comportamiento actual (redirigir)
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", "http://www.uclm.es");
    }
    
}
