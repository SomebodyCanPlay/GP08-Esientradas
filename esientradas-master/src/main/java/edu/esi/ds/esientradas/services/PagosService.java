package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.transaction.Transactional;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.PagoDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Pago;
import edu.esi.ds.esientradas.model.Estado;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class PagosService {
    @Autowired
    private static final String secretKey="sk_test_51T92np0X24g3D2snorVjpIBkAnJqITaNqwigCQjy7GwZDEz0BYFmF8LIrtIAOkJ1slKrwGNchTn96N2GP8PaqdMh00XVIz1NqW";

    public void prepararPago(Long centimos) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(centimos)
            .setCurrency("eur")
            .build();
        PaymentIntent intent = PaymentIntent.create(params);
        JSONObject jso = new JSONObject(intent.toJson());
        String clientSecret = jso.getString("client_secret");
        System.out.println("Client secret: " + clientSecret);
        return clientSecret;
    }
}