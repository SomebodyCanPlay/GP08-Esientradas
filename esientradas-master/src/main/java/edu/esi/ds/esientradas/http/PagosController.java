package edu.esi.ds.esientradas.http;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.stripe.exception.StripeException;
import edu.esi.ds.esientradas.services.PagosService;

// ============================================================
// CONTROLADOR DE PAGOS — integración con Stripe en 2 pasos
// ============================================================
// PASO 1: prepararPago → Stripe crea un intento y devuelve clientSecret
//   El frontend usa ese clientSecret con Stripe.js para mostrar el
//   formulario de tarjeta de forma segura (nunca pasa por nosotros)
//
// PASO 2: firmarPago → Stripe confirmó el pago, marcamos entrada VENDIDA
// ============================================================
@RestController
@RequestMapping("/pagos")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PagosController {

    @Autowired
    private PagosService pagosService;

    // POST /pagos/prepararPago
    // Body: { "centimos": 2500 } ← 2500 = 25,00 €
    // Devuelve: el clientSecret de Stripe (texto)
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

    // POST /pagos/firmarPago
    // Body: { "entradaId": 42, "paymentIntentId": "pi_xxxxx" }
    // Stripe confirmó el pago → marcamos la entrada VENDIDA en nuestra BD
    @PostMapping("/firmarPago")
    public void firmarPago(@RequestBody Map<String, Object> body) {
        Long entradaId = ((Number) body.get("entradaId")).longValue();
        String paymentIntentId = (String) body.get("paymentIntentId");
        pagosService.firmarPago(entradaId, paymentIntentId);
    }
}