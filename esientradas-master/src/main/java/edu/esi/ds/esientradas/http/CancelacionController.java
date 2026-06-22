package edu.esi.ds.esientradas.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.esi.ds.esientradas.services.CancelacionService;

@RestController
@RequestMapping("/entradas")
public class CancelacionController {

    @Autowired
    private CancelacionService cancelacionService;

    @GetMapping("/cancelar")
    public ResponseEntity<String> cancelarEntrada(@RequestParam("token") String token) {
        String resultado = cancelacionService.cancelarEntrada(token);

        if ("OK".equals(resultado)) {
            String htmlExito = "<html><body style='font-family: sans-serif; text-align: center; margin-top: 50px;'>" +
                               "<h1 style='color: #28a745;'>✅ Reserva Cancelada</h1>" +
                               "<p>Tu entrada ha sido liberada correctamente.</p>" +
                               "<p>El importe ha sido devuelto al <b>monedero virtual</b> de tu cuenta y está listo para tu próxima compra.</p>" +
                               "</body></html>";
            return ResponseEntity.ok(htmlExito);
        } else {
            String htmlError = "<html><body style='font-family: sans-serif; text-align: center; margin-top: 50px;'>" +
                               "<h1 style='color: #dc3545;'>❌ Error al Cancelar</h1>" +
                               "<p>" + resultado + "</p>" +
                               "</body></html>";
            return ResponseEntity.badRequest().body(htmlError);
        }
    }
}