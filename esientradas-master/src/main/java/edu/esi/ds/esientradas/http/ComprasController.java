package edu.esi.ds.esientradas.http;

import java.util.Map;


import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/compras")
public class ComprasController {

    @PutMapping("/comprar")
    public void comprar(HttpSession session,HttpServletResponse response, @RequestBody String userToken) {
        String sessionId = session.getId();
        if (userToken == null || userToken.isEmpty()) {
            response.setRedirect("http:www.uclm.es");
            return;
        }
        this.usuarioService.checkToken(userToken);
        
    }
    /* 
    @PostMapping("/comprar")
    public void comprar(@RequestBody Map<String, Object> payload) {
        String sessionId = (String) payload.get("sessionId");
        Long idEntrada = ((Number) payload.get("idEntrada")).longValue();

    }
*/
}
