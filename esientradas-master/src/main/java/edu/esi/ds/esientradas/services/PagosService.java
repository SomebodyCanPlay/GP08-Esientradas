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
    public void firmarPago(Long entradaId, String paymentIntentId, String userEmail) {
        Entrada entrada = entradaDao.findById(entradaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        entrada.setEstado(Estado.VENDIDA);
        
        Token t = entrada.getToken();
        if (t != null) {
            entrada.setToken(null);
            tokenDao.delete(t.getValor());
        }
        
        entradaDao.save(entrada);

        Pago pago = pagoDao.findByIdIntentoPago(paymentIntentId).orElse(new Pago()); 
        pago.setEntrada(entrada);
        pago.setEstado("COMPLETADO");
        pago.setIdIntentoPago(paymentIntentId);
        pago.setCantidadCentimos(entrada.getPrecio()); 
        pagoDao.save(pago);

        Configuracion config = configuracionDao.findAll().stream().findFirst().orElse(null);
        try {
            // Generar PDF y enviar correo
            byte[] pdfBytes = pdfService.generarPdf(entrada, config);
            emailService.enviarConfirmacionConPdf(entrada, userEmail, pdfBytes);
        } catch (Exception e) {
            System.err.println("Error generando/enviando PDF: " + e.getMessage());
        }

        System.out.println("[PagosService] ¡Venta confirmada! Entrada: " + entradaId + " -> email enviado a: " + userEmail);
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
        entradaDao.save(entrada);

        tokenDao.delete(tokenValor);

        Pago pago = new Pago();
        pago.setEntrada(entrada);
        pago.setEstado("COMPLETADO");
        pago.setIdIntentoPago(null);
        pago.setCantidadCentimos(entrada.getPrecio());
        pagoDao.save(pago);

        Configuracion config = configuracionDao.findAll().stream().findFirst().orElse(null);
        try {
            // Generar PDF y enviar correo
            byte[] pdfBytes = pdfService.generarPdf(entrada, config);
            emailService.enviarConfirmacionConPdf(entrada, userEmail, pdfBytes);
        } catch (Exception e) {
            System.err.println("Error generando/enviando PDF: " + e.getMessage());
        }

        System.out.println("[PagosService] Venta firmada por token: " + tokenValor + " → email enviado a: " + userEmail);
    }

    @Transactional
    public java.util.Map<String, Object> iniciarPago(String sessionId) {
        List<Token> tokens = tokenDao.findAllBySessionId(sessionId);
        if (tokens.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay reservas activas para esta sesión");
        }

        long totalCentimos = 0;
        java.util.List<Long> pagoIds = new java.util.ArrayList<>();

        for (Token token : tokens) {
            Entrada entrada = token.getEntrada();
            if (entrada == null) continue;

            Pago pago = pagoDao.findByEntradaId(entrada.getId()).orElse(new Pago());
            pago.setEntrada(entrada);
            pago.setEstado("PENDIENTE");
            pago.setCantidadCentimos(entrada.getPrecio());
            pagoDao.save(pago);

            pagoIds.add(pago.getId());
            totalCentimos += entrada.getPrecio();
        }

        return java.util.Map.of("pagoIds", pagoIds, "totalCentimos", totalCentimos);
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

        for (Token token : tokens) {
            Entrada entrada = token.getEntrada();
            if (entrada == null) continue;

            entrada.setEstado(Estado.VENDIDA);
            entrada.setToken(null);
            entradaDao.save(entrada);

            Pago pago = pagoDao.findByEntradaId(entrada.getId()).orElse(new Pago(entrada, entrada.getPrecio(), "EUR"));
            pago.setEstado("COMPLETADO");
            pagoDao.save(pago);

            try {
                // Generar PDF y guardarlo en la lista, en lugar de enviarlo directamente
                byte[] pdfBytes = pdfService.generarPdf(entrada, config);
                entradasCompradas.add(entrada);
                pdfs.add(pdfBytes);
            } catch (Exception e) {
                // Loguear el error y continuar con las demás entradas
                System.err.println("Error generando PDF para la entrada " + entrada.getId() + ": " + e.getMessage());
            }

            tokenDao.delete(token.getValor());
        }

        // Enviar un único correo con todas las entradas y PDFs al final del bucle
        if (!entradasCompradas.isEmpty()) {
            // Este método se encarga de enviar uno o varios PDFs en un solo correo
            emailService.enviarConfirmacionCompraMultiple(entradasCompradas, userEmail, pdfs);
        }
    }
}