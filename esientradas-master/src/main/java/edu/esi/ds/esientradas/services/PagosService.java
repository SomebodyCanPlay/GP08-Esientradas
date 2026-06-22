package edu.esi.ds.esientradas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import edu.esi.ds.esientradas.dao.ConfiguracionDao;
import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.PagoDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Configuracion;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Pago;
import edu.esi.ds.esientradas.model.Token;
import jakarta.transaction.Transactional;

@Service
public class PagosService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private PagoDao pagoDao;

    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private ConfiguracionDao configuracionDao;

    @Autowired
    private PDFService pdfService;

    @Value("${stripe.key:sk_test_51T92np0X24g3D2snorVjpIBkAnJqITaNqwigCQjy7GwZDEz0BYFmF8LIrtIAOkJ1slKrwGNchTn96N2GP8PaqdMh00XVIz1NqW}")
    private String secretKey;

    public String prepararPago(Long centimos) throws StripeException {
        Stripe.apiKey = this.secretKey;
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(centimos)
                .setCurrency("eur")
                .build();
        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getClientSecret();
    }

    @Transactional
    public java.util.Map<String, Object> iniciarPago(String sessionId, String emailComprador) throws StripeException {
        List<Token> tokens = tokenDao.findAllBySessionId(sessionId);
        if (tokens.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay reservas activas para esta sesión");
        }

        long totalCentimos = 0;
        
        // 1. Creamos un único "carrito" de pago
        Pago carrito = new Pago();
        carrito.setEstado("PENDIENTE");
        carrito.setUsuarioEmail(emailComprador); 

        // 2. Metemos todas las entradas en el carrito y sumamos el precio
        for (Token token : tokens) {
            Entrada entrada = token.getEntrada();
            if (entrada == null) continue;

            totalCentimos += entrada.getPrecio();
            
            carrito.getEntradas().add(entrada);
            entrada.setPago(carrito); 
        }

        carrito.setCantidadCentimos(totalCentimos);

        // 3. Hablamos con Stripe
        Stripe.apiKey = this.secretKey;
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(totalCentimos)
                .setCurrency("eur")
                .build();
        PaymentIntent intent = PaymentIntent.create(params);

        // 4. Guardamos el ID real de Stripe en nuestro carrito
        carrito.setIdIntentoPago(intent.getId());
        pagoDao.save(carrito); 

        return java.util.Map.of(
            "pagoId", carrito.getId(),
            "totalCentimos", totalCentimos,
            "clientSecret", intent.getClientSecret() 
        );
    }

    @Transactional
    public void firmarPago(Long entradaId, String paymentIntentId, String userEmail) {
        Entrada entrada = entradaDao.findById(entradaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        entrada.setEstado(Estado.VENDIDA);
        
        Token t = entrada.getToken();
        if (t != null) {
            entrada.setToken(null);
            tokenDao.delete(t.getValor());
        }
        
        Pago pago = pagoDao.findByIdIntentoPago(paymentIntentId).orElse(new Pago()); 
        pago.getEntradas().add(entrada);
        entrada.setPago(pago);
        pago.setEstado("COMPLETADO");
        pago.setIdIntentoPago(paymentIntentId);
        pago.setCantidadCentimos(entrada.getPrecio()); 
        pagoDao.save(pago);

        Configuracion config = configuracionDao.findAll().stream().findFirst().orElse(null);
        try {
            byte[] pdfBytes = pdfService.generarPdf(entrada, config);
            emailService.enviarConfirmacionConPdf(entrada, userEmail, pdfBytes);
        } catch (Exception e) {
            System.err.println("Error generando/enviando PDF: " + e.getMessage());
        }
    }

    @Transactional
    public void firmarPago(String tokenValor, String userEmail) {
        if (tokenValor == null || tokenValor.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido");
        }

        Token token = tokenDao.findByValor(tokenValor).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token no encontrado"));
        Entrada entrada = token.getEntrada();
        if (entrada == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada asociada al token no encontrada");
        }

        entrada.setEstado(Estado.VENDIDA);
        entrada.setToken(null);

        tokenDao.delete(tokenValor);

        Pago pago = new Pago();
        pago.getEntradas().add(entrada);
        entrada.setPago(pago);
        pago.setEstado("COMPLETADO");
        pago.setCantidadCentimos(entrada.getPrecio());
        pagoDao.save(pago);

        Configuracion config = configuracionDao.findAll().stream().findFirst().orElse(null);
        try {
            byte[] pdfBytes = pdfService.generarPdf(entrada, config);
            emailService.enviarConfirmacionConPdf(entrada, userEmail, pdfBytes);
        } catch (Exception e) {
            System.err.println("Error generando/enviando PDF: " + e.getMessage());
        }
    }

    @Transactional
    public void firmarPagosPorSession(String sessionId, String userEmail) {
        List<Token> tokens = tokenDao.findAllBySessionId(sessionId);
        if (tokens.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay reservas activas para esta sesión");
        }

        Configuracion config = configuracionDao.findAll().stream().findFirst().orElse(null);
        List<Entrada> entradasCompradas = new java.util.ArrayList<>();
        List<byte[]> pdfs = new java.util.ArrayList<>();

        // 1. Recuperamos el carrito (Pago) PENDIENTE
        Entrada primeraEntrada = tokens.get(0).getEntrada();
        Pago pagoExistente = primeraEntrada.getPago();

        if (pagoExistente != null) {
            // Marcamos la hora exacta de la compra para que empiecen a contar los 15 minutos
            pagoExistente.setEstado("COMPLETADO");
            pagoExistente.setFechaCompra(java.time.LocalDateTime.now());
            pagoDao.save(pagoExistente);
        }

        for (Token token : tokens) {
            Entrada entrada = token.getEntrada();
            if (entrada == null) continue;

            // Marcamos la entrada como vendida y le quitamos el token de reserva
            entrada.setEstado(Estado.VENDIDA);
            entrada.setToken(null);
            
            // Le generamos un código de cancelación único a esta entrada
            entrada.setTokenCancelacion(java.util.UUID.randomUUID().toString());
            
            try {
                // Generamos el PDF
                byte[] pdfBytes = pdfService.generarPdf(entrada, config);
                entradasCompradas.add(entrada);
                pdfs.add(pdfBytes);
            } catch (Exception e) {
                System.err.println("Error generando PDF para la entrada " + entrada.getId() + ": " + e.getMessage());
            }

            // Borramos el token porque ya se ha comprado
            tokenDao.delete(token.getValor());
        }

        // Enviamos el correo final (que ahora incluirá el enlace de cancelación)
        if (!entradasCompradas.isEmpty()) {
            emailService.enviarConfirmacionCompraMultiple(entradasCompradas, userEmail, pdfs);
        }
    }
}