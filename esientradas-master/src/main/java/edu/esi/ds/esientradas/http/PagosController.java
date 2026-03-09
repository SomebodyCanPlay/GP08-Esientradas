package edu.esi.ds.esientradas.http;  

import javax.servlet.http.HttpSession; 
import org.json.JSONObject; 
import org.springframework.http.HttpStatus; 
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.bind.annotation.RestController; 
import org.springframework.web.server.ResponseStatusException; 
import com.stripe.Stripe; 
import com.stripe.model.PaymentIntent; 
import com.stripe.param.PaymentIntentCreateParams; 
@RestController 
@RequestMapping("/pagos") 
public class PagosController {
    @Autowired
    private PagosService pagosService;

    @PostMapping("/prepararPago")
    public void prepararPago(@RequestBody Map<String, Object> infoPago) {
        long centimos = ((Number) infoPago.get("centimos")).longValue();
        try {
            this.Service.prepararPago(centimos);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al preparar el pago", e);
        }
    }
}