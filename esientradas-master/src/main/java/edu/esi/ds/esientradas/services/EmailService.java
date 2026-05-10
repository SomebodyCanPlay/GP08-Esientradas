package edu.esi.ds.esientradas.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import edu.esi.ds.esientradas.model.DeZona;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.model.Precisa;

// Servicio de email: envía confirmaciones de compra.
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Método para enviar confirmaciones al usuario.
    public void enviarConfirmacion(Entrada entrada, String recipientEmail) {
        if (entrada == null || recipientEmail == null)
            return;

        try {
            // MimeMessage permite configurar un nombre de remitente personalizado (alias)
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, "utf-8");

            // Configuramos el remitente con el correo y el nombre visible
            helper.setFrom("edugallego23@gmail.com", "ESIentradas");
            helper.setTo(recipientEmail);
            helper.setSubject("ESIentradas - Confirmación de compra");

            StringBuilder texto = new StringBuilder();
            texto.append("¡Hola!\n\n");
            texto.append("Has confirmado tu compra. Aquí tienes los detalles:\n");
            texto.append("--------------------------------------------------\n");
            texto.append("Ticket ID: ").append(entrada.getId()).append("\n");

            Espectaculo espectaculo = entrada.getEspectaculo();
            if (espectaculo != null) {
                texto.append("Artista/Evento: ").append(espectaculo.getArtista()).append("\n");
            }

            // Lógica inteligente para saber qué tipo de entrada es
            if (entrada instanceof Precisa precisa) {
                texto.append("Tipo: Butaca Asignada\n");
                texto.append("Planta: ").append(precisa.getPlanta()).append("\n");
                texto.append("Fila: ").append(precisa.getFila()).append(" | Columna: ").append(precisa.getColumna())
                        .append("\n");

            } else if (entrada instanceof DeZona deZona) {
                texto.append("Tipo: Entrada General\n");
                texto.append("Zona: ").append(deZona.getZona()).append("\n");
            }

            double precioEuros = entrada.getPrecio() / 100.0;
            texto.append("Precio Total: ").append(precioEuros).append(" €\n");
            texto.append("--------------------------------------------------\n\n");
            texto.append("¡Que disfrutes del evento!");

            helper.setText(texto.toString());
            mailSender.send(mensaje);

            System.out.println("[EmailService] Email real y personalizado enviado a: " + recipientEmail);

        } catch (Exception e) {
            // Log de error en caso de que falle la construcción o el envío del mensaje
            System.err.println("[EmailService] Error enviando el correo de confirmación: " + e.getMessage());
        }
    }
}