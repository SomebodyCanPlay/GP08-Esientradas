package edu.esi.ds.esientradas.services;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import edu.esi.ds.esientradas.model.Entrada;

// Servicio de email: envía confirmaciones de compra con PDF adjunto.
@Service
public class EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api.url}")
    private String brevoApiUrl;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${app.mail.from}")
    private String emailFrom;

    @Value("${app.mail.from.name}")
    private String emailFromName;

    @Async
    public void enviarConfirmacionConPdf(Entrada entrada, String recipientEmail, byte[] pdfBytes) {
        if (entrada == null || recipientEmail == null || pdfBytes == null) return;
        // Delegamos a la nueva función que gestiona listas para no duplicar código
        enviarConfirmacionCompraMultiple(List.of(entrada), recipientEmail, List.of(pdfBytes));
    }

    /**
     * Envía un único correo de confirmación con una o varias entradas en PDF como adjuntos.
     * @param entradas Lista de entradas compradas.
     * @param recipientEmail Email del destinatario.
     * @param pdfs Lista de PDFs (en bytes) correspondientes a cada entrada.
     */
    @Async
    public void enviarConfirmacionCompraMultiple(List<Entrada> entradas, String recipientEmail, List<byte[]> pdfs) {
        if (entradas == null || entradas.isEmpty() || recipientEmail == null || pdfs == null || pdfs.isEmpty()) {
            return;
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Entrada primeraEntrada = entradas.get(0);
            String tituloEspectaculo = primeraEntrada.getEspectaculo() != null ? primeraEntrada.getEspectaculo().getArtista() : "tu evento";
            
            boolean esMultiple = entradas.size() > 1;
            String subject = "🎟️ Tu" + (esMultiple ? "s" : "") + " entrada" + (esMultiple ? "s" : "") + " para: " + tituloEspectaculo;

            String intro;
            if (esMultiple) {
                intro = "<p>Tu compra de " + entradas.size() + " entradas se ha completado con éxito. Adjunto a este correo encontrarás tus entradas oficiales en formato PDF.</p>";
            } else {
                intro = "<p>Tu compra se ha completado con éxito. Adjunto a este correo encontrarás tu entrada oficial en formato PDF.</p>";
            }

            String htmlBody = "<h2>¡Gracias por tu compra!</h2>" +
                    "<p>Hola,</p>" +
                    intro +
                    "<p>Por favor, lleva las entradas impresas o descargadas en tu móvil para poder acceder al recinto el día del evento.</p>" +
                    "<br><p>¡Que disfrutes del espectáculo!<br><b>El equipo de ESIentradas</b></p>";

            List<Map<String, String>> attachments = new ArrayList<>();
            for (int i = 0; i < pdfs.size(); i++) {
                Entrada entrada = entradas.get(i);
                byte[] pdfBytes = pdfs.get(i);
                String nombreArchivo = "Entrada_" + entrada.getId() + ".pdf";
                String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
                attachments.add(Map.of("name", nombreArchivo, "content", pdfBase64));
            }

            Map<String, Object> sender = Map.of("name", emailFromName, "email", emailFrom);
            List<Map<String, String>> to = List.of(Map.of("email", recipientEmail));
            Map<String, Object> body = new HashMap<>();
            body.put("sender", sender);
            body.put("to", to);
            body.put("subject", subject);
            body.put("htmlContent", htmlBody);
            body.put("attachment", attachments);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(brevoApiUrl, request, String.class);
            
            String logMessage = "[EmailService] Email con " + entradas.size() + " PDF" + (esMultiple ? "s" : "") + " adjunto" + (esMultiple ? "s" : "") + " enviado a: " + recipientEmail + " via Brevo API.";
            System.out.println(logMessage);
        } catch (Exception e) {
            System.err.println("[EmailService] Error enviando el correo con PDF via Brevo API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}