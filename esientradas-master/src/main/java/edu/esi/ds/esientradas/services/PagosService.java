package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.transaction.Transactional;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.PagoDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Pago;
import edu.esi.ds.esientradas.model.Token;
import edu.esi.ds.esientradas.model.Estado;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class PagosService {

	@Autowired
	private EntradaDao entradaDao;

	@Autowired
	private PagoDao pagoDao;

	@Autowired
	private TokenDao tokenDao;

	// Inyectar el nuevo EmailService
	@Autowired
	private EmailService emailService;

	private static final String secretKey="sk_test_51T92np0X24g3D2snorVjpIBkAnJqITaNqwigCQjy7GwZDEz0BYFmF8LIrtIAOkJ1slKrwGNchTn96N2GP8PaqdMh00XVIz1NqW";

	public String prepararPago(Long centimos) throws StripeException {
		Stripe.apiKey = secretKey;

		PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
				.setAmount(centimos)
				.setCurrency("eur")
				.build();

		PaymentIntent intent = PaymentIntent.create(params);
		return intent.getClientSecret();
	}

	@Transactional
	public void confirmarVenta(Long entradaId, String paymentIntentId) {
		// 1. Buscamos la entrada
		Entrada entrada = entradaDao.findById(entradaId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

		// 2. Actualizamos estado de la entrada
		entrada.setEstado(Estado.VENDIDA);
		
		// 3. Gestión del Token (como es OneToOne, lo desvinculamos)
		Token t = entrada.getToken();
		if (t != null) {
			entrada.setToken(null);
			tokenDao.delete(t.getValor()); // Aquí sí lo borramos porque ya no es una "reserva", es una venta
		}
		
		entradaDao.save(entrada);

		// 4. Registrar el éxito en la tabla de Pagos
		Pago pago = pagoDao.findByIdIntentoPago(paymentIntentId)
				.orElse(new Pago()); // Por si no se creó al principio
		pago.setEntrada(entrada);
		pago.setEstado("COMPLETADO");
		pago.setIdIntentoPago(paymentIntentId);
		pagoDao.save(pago);

		// Llamada al servicio de email (sigue dentro de la transacción)
		emailService.enviarConfirmacion(entrada);

		System.out.println("¡Venta confirmada! Entrada: " + entradaId);
	}
}