package edu.esi.ds.esientradas.http;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.stripe.exception.StripeException;
import edu.esi.ds.esientradas.services.PagosService;

// Controlador de pagos: integración en dos pasos con Stripe.
@RestController
@RequestMapping("/pagos")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PagosController {

    @Autowired
    private PagosService pagosService;

    // Prepara el pago en Stripe y devuelve el clientSecret.
    @PostMapping("/prepararPago")
    public String prepararPago(@RequestBody Map<String, Object> infoPago) {
        long centimos = ((Number) infoPago.get("centimos")).longValue();
        try {
            return this.pagosService.prepararPago(centimos);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al preparar el pago con Stripe", e);
        }
    }

    // Firma el pago tras la confirmación de Stripe y marca la entrada como vendida.
    @PostMapping("/firmarPago")
    public void firmarPago(@RequestBody Map<String, Object> body) {
        Long entradaId = ((Number) body.get("entradaId")).longValue();
        String paymentIntentId = (String) body.get("paymentIntentId");
        pagosService.firmarPago(entradaId, paymentIntentId);
    }
}