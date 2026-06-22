package edu.esi.ds.esientradas.services;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import edu.esi.ds.esientradas.model.Entrada;

@Service
public class EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private PDFService pdfService;

    @Value("${brevo.api.url}")
    private String brevoApiUrl;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${app.mail.from}")
    private String emailFrom;

    @Value("${app.mail.from.name}")
    private String emailFromName;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Async
    public void enviarConfirmacionConPdf(Entrada entrada, String recipientEmail, byte[] pdfBytes) {
        if (entrada == null || recipientEmail == null || pdfBytes == null) return;
        enviarConfirmacionCompraMultiple(List.of(entrada), recipientEmail, List.of(pdfBytes));
    }

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

            List<Map<String, String>> attachments = new ArrayList<>();
            
            // ---> NUEVO: Usamos StringBuilder para crear la lista de enlaces de cancelación
            StringBuilder enlacesCancelacion = new StringBuilder();
            
            for (int i = 0; i < pdfs.size(); i++) {
                Entrada entrada = entradas.get(i);
                byte[] pdfBytes = pdfs.get(i);
                
                // 1. GUARDAMOS EL PDF EN EL DISCO Y REGISTRAMOS EN LA BBDD
                pdfService.guardarPdfFisicoYRegistrar(entrada.getId(), pdfBytes);

                // 2. LO ADJUNTAMOS AL CORREO
                String nombreArchivo = "Entrada_" + entrada.getId() + ".pdf";
                String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
                attachments.add(Map.of("name", nombreArchivo, "content", pdfBase64));
                
                // 3. AÑADIMOS EL ENLACE INDIVIDUAL A LA LISTA HTML
                String urlCancelacion = appBaseUrl + "/entradas/cancelar?token=" + entrada.getTokenCancelacion();
                enlacesCancelacion.append("<li style='margin-bottom: 10px;'>")
                                  .append("<b>Entrada #").append(entrada.getId()).append("</b>: ")
                                  .append("<a href='").append(urlCancelacion).append("' style='color: #dc3545; text-decoration: none;'>[Cancelar esta entrada y reembolsar saldo]</a>")
                                  .append("</li>");
            }

            // Integración de la lista de cancelación en el HTML del correo
            String htmlBody = "<h2>¡Gracias por tu compra!</h2>" +
                    "<p>Hola,</p>" +
                    intro +
                    "<p>Por favor, lleva las entradas impresas o descargadas en tu móvil para poder acceder al recinto el día del evento.</p>" +
                    "<br><hr><br>" +
                    "<h3 style='color: #555;'>¿No puedes asistir? (Tienes 15 minutos para cancelar)</h3>" +
                    "<ul style='list-style-type: none; padding-left: 0;'>" + enlacesCancelacion.toString() + "</ul>" +
                    "<br><p>¡Que disfrutes del espectáculo!<br><b>El equipo de ESIentradas</b></p>";

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