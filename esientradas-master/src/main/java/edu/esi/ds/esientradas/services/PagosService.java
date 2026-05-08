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
    // PASO 1 DEL NUEVO FLUJO: iniciarPago
    // ============================================================
    // El usuario pulsa "Ir al Pago" → este método crea un registro PENDIENTE
    // en la tabla pago por cada entrada que tiene en el carrito.
    // El frontend lo usa para mostrar el formulario de tarjeta.
    // El pago pasa a COMPLETADO cuando el usuario confirma la tarjeta.
    // ============================================================
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

            // Buscamos si ya existe un pago PENDIENTE para esta entrada
            // (por si el usuario pulsó "Ir al Pago" dos veces)
            Optional<Pago> pagoExistente = pagoDao.findByEntradaId(entrada.getId());
            Pago pago = pagoExistente.orElse(new Pago());

            // Creamos o actualizamos el registro con estado PENDIENTE
            pago.setEntrada(entrada);
            pago.setEstado("PENDIENTE");
            pago.setCantidadCentimos(entrada.getPrecio());
            pagoDao.save(pago);

            pagoIds.add(pago.getId());
            totalCentimos += entrada.getPrecio();

            System.out.println("[PagosService] Pago PENDIENTE creado → entrada " + entrada.getId()
                    + " (" + (entrada.getPrecio() / 100.0) + " €)");
        }

        // Devolvemos los IDs de pago y el total (en céntimos → el frontend divide entre 100)
        return java.util.Map.of("pagoIds", pagoIds, "totalCentimos", totalCentimos);
    }

    // ============================================================
    // PASO 2 DEL NUEVO FLUJO: confirmarPagosPorSession (PENDIENTE → COMPLETADO)
    // ============================================================
    // El usuario ha introducido los datos de la tarjeta y confirmado el pago.
    // Este método actualiza los pagos PENDIENTE a COMPLETADO y marca las entradas VENDIDAS.
    @Transactional
    public void firmarPagosPorSession(String sessionId, String userEmail) {
        List<Token> tokens = tokenDao.findAllBySessionId(sessionId);

        if (tokens.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay reservas activas para esta sesión");
        }

        Configuracion config = configuracionDao.findAll().stream().findFirst().orElse(null);

        for (Token token : tokens) {
            Entrada entrada = token.getEntrada();
            if (entrada == null) continue;

            // 1. Marcar entrada como VENDIDA
            entrada.setEstado(Estado.VENDIDA);
            entrada.setToken(null);
            entradaDao.save(entrada);

            // 2. Actualizar el pago PENDIENTE a COMPLETADO
            // Si por algún motivo no hay pago PENDIENTE previo, creamos uno nuevo
            Pago pago = pagoDao.findByEntradaId(entrada.getId())
                    .orElse(new Pago(entrada, entrada.getPrecio(), "EUR"));
            pago.setEstado("COMPLETADO");
            pagoDao.save(pago);

            // 3. Generar PDF y enviar email de confirmación
            if (pdfService != null) {
                pdfService.generarYEnviar(entrada, config);
            }
            this.emailService.enviarConfirmacion(entrada, userEmail);

            System.out.println("[PagosService] ✓ Pago COMPLETADO → entrada " + entrada.getId()
                    + " → email a " + userEmail);

            // 4. Borrar el token de reserva (ya no hace falta)
            tokenDao.delete(token.getValor());
        }
    }
}