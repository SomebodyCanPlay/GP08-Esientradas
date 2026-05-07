package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import jakarta.transaction.Transactional;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.PagoDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.dao.ConfiguracionDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Pago;
import edu.esi.ds.esientradas.model.Token;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Configuracion;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

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

    // Se usa @Value para no dejar la clave a la vista en el código si es posible
    @Value("${stripe.key:sk_test_51T92np0X24g3D2snorVjpIBkAnJqITaNqwigCQjy7GwZDEz0BYFmF8LIrtIAOkJ1slKrwGNchTn96N2GP8PaqdMh00XVIz1NqW}")
    private String secretKey;

    public String prepararPago(Long centimos) throws StripeException {
        // Stripe trabaja SIEMPRE en céntimos. 
        // Si el controlador ya te pasa los céntimos, no multipliques por 100.
        Stripe.apiKey = this.secretKey;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(centimos)
                .setCurrency("eur")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getClientSecret();
    }

    @Transactional
    public void firmarPago(Long entradaId, String paymentIntentId) {
        // 1. Buscamos la entrada
        Entrada entrada = entradaDao.findById(entradaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        // 2. Actualizamos estado de la entrada a VENDIDA
        entrada.setEstado(Estado.VENDIDA);
        
        // 3. Gestión del Token
        Token t = entrada.getToken();
        if (t != null) {
            entrada.setToken(null); // Desvinculamos
            tokenDao.delete(t.getValor()); // Borramos el token de reserva
        }
        
        entradaDao.save(entrada);

        // 4. Registrar o actualizar el Pago
        Pago pago = pagoDao.findByIdIntentoPago(paymentIntentId)
                .orElse(new Pago()); 
        pago.setEntrada(entrada);
        pago.setEstado("COMPLETADO");
        pago.setIdIntentoPago(paymentIntentId);
        pago.setCantidadCentimos(entrada.getPrecio()); 
        pagoDao.save(pago);

        System.out.println("[PagosService] ¡Venta confirmada! Entrada: " + entradaId);

        // 5. Buscar la configuración de la empresa para el PDF (nombre, logo, etc.)
        Configuracion config = configuracionDao.findAll().stream().findFirst().orElse(null);
        
        // 6. Generar PDF y enviar Email de confirmación
        // pdfService.generarYEnviar() ya llama internamente a emailService.enviarConfirmacion()
        // así que solo hace falta esta línea — NO llamar a emailService por separado
        if (pdfService != null) {
            pdfService.generarYEnviar(entrada, config);
        }
    }

    /**
     * Nuevo método: firma (completa) la venta usando el token de reserva.
     * Busca el token, obtiene la entrada asociada, marca VENDIDA, borra el token,
     * registra el pago y envía PDF/email al email proporcionado.
     */
    @Transactional
    public void firmarPago(String tokenValor, String userEmail) {
        if (tokenValor == null || tokenValor.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido");
        }

        Optional<Token> optToken = tokenDao.findByValor(tokenValor);
        Token token = optToken.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token no encontrado"));

        Entrada entrada = token.getEntrada();
        if (entrada == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada asociada al token no encontrada");
        }

        // Marcar entrada como vendida y desvincular token
        entrada.setEstado(Estado.VENDIDA);
        entrada.setToken(null);
        entradaDao.save(entrada);

        // Borrar token de reservas
        tokenDao.delete(tokenValor);

        // Registrar pago (sin idIntento concreto en este flujo)
        Pago pago = new Pago();
        pago.setEntrada(entrada);
        pago.setEstado("COMPLETADO");
        pago.setIdIntentoPago(null);
        pago.setCantidadCentimos(entrada.getPrecio());
        pagoDao.save(pago);

        // Generar PDF y enviar el email de confirmación al email del usuario
        // userEmail viene de esiusuarios (es el email real del comprador)
        Configuracion config = configuracionDao.findAll().stream().findFirst().orElse(null);
        if (pdfService != null) {
            pdfService.generarYEnviar(entrada, config);
        }
        // Notificamos con el email real del usuario (el que devolvió checkToken de esiusuarios)
        this.emailService.enviarConfirmacion(entrada, userEmail);

        System.out.println("[PagosService] Venta firmada por token: " + tokenValor + " → email enviado a: " + userEmail);
    }

    // ============================================================
    // MÉTODO PRINCIPAL DEL FLUJO DE COMPRA
    // ============================================================
    // Este es el método que llama ComprasController cuando el usuario pulsa "Confirmar compra"
    // Recibe el sessionId (identifica al usuario en la cola) y el userEmail (de esiusuarios)
    // Confirma la venta de TODAS las entradas que el usuario prerreservó en esta sesión
    @Transactional
    public void firmarPagosPorSession(String sessionId, String userEmail) {

        // Buscamos todos los tokens de reserva de esta sesión
        // Cada token representa UNA entrada que el usuario puso en el carrito
        List<Token> tokens = tokenDao.findAllBySessionId(sessionId);
        
        if (tokens.isEmpty()) {
            // Si no hay tokens, el usuario no tenía nada en el carrito
            // (o la sesión ya expiró y ReservaCleanUpTask limpió los tokens)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay reservas activas para esta sesión");
        }

        // Cargamos la configuración de la empresa (nombre, logo) para el PDF
        Configuracion config = configuracionDao.findAll().stream().findFirst().orElse(null);

        // Procesamos cada entrada del carrito una por una
        for (Token token : tokens) {
            Entrada entrada = token.getEntrada();

            if (entrada != null) {

                // 1. Cambiar estado de RESERVADA → VENDIDA
                // y desvincular el token (ya no hace falta, la venta está confirmada)
                entrada.setEstado(Estado.VENDIDA);
                entrada.setToken(null);
                entradaDao.save(entrada);

                // 2. Registrar el pago en la tabla 'pago'
                // Esto guarda un registro de cuánto pagó y por qué entrada
                Pago pago = new Pago();
                pago.setEntrada(entrada);
                pago.setEstado("COMPLETADO");
                pago.setCantidadCentimos(entrada.getPrecio());
                pagoDao.save(pago);

                // 3. Generar PDF del recibo (simulado → guarda URL en BD)
                if (pdfService != null) {
                    pdfService.generarYEnviar(entrada, config);
                }

                // 4. Enviar email de confirmación al comprador
                // userEmail viene de esiusuarios (lo devolvió checkToken)
                this.emailService.enviarConfirmacion(entrada, userEmail);
                
                System.out.println("[PagosService] ✓ Entrada " + entrada.getId() + " vendida → email a " + userEmail);
            }

            // 5. Borrar el token de reserva de la BD
            // Ya no hace falta — la entrada está VENDIDA
            tokenDao.delete(token.getValor());
        }
    }
}