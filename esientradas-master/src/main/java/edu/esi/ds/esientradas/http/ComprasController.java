package edu.esi.ds.esientradas.http;

import java.util.Map;
import edu.esi.ds.esientradas.services.UsuarioService;

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

    @PutMapping("/comprar")
    public void comprar(HttpSession session,HttpServletResponse response, @RequestBody String userToken) {
        String sessionId = session.getId();
        if (userToken == null || userToken.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", "http://www.uclm.es");
            return;
        }
        this.usuarioService.checkToken(userToken);
    }
    
}
