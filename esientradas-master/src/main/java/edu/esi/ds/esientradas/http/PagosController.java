package edu.esi.ds.esientradas.http;  

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; 
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.bind.annotation.RestController; 
import org.springframework.web.server.ResponseStatusException; 

import com.stripe.exception.StripeException;
import edu.esi.ds.esientradas.services.PagosService;

@RestController 
@RequestMapping("/pagos") 
public class PagosController {
    
    @Autowired
    private PagosService pagosService;

    @PostMapping("/prepararPago")
    public String prepararPago(@RequestBody Map<String, Object> infoPago) {
        long centimos = ((Number) infoPago.get("centimos")).longValue();
        try {
            return this.pagosService.prepararPago(centimos);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al preparar el pago", e);
        }
    }

    @PostMapping("/firmarPago")
    public void firmarPago(@RequestBody Map<String, Object> body) {
        Long entradaId = ((Number) body.get("entradaId")).longValue();
        String paymentIntentId = (String) body.get("paymentIntentId");
        
        pagosService.firmarPago(entradaId, paymentIntentId);
    }
}