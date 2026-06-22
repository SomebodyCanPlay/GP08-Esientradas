package edu.esi.ds.esientradas.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

// Servicio de usuario: consulta el microservicio esiusuarios para verificar tokens y gestionar el monedero.
@Service
public class UsuarioService {

    // URL base del microservicio esiusuarios (ej: "http://localhost:8081")
    private final String esiusuariosBaseUrl;

    // RestTemplate es la herramienta de Spring para hacer peticiones HTTP a otros servidores
    private final RestTemplate restTemplate = new RestTemplate();

    // @Value("${esiusuarios.url}") → lee el valor de application.properties
    // Lo recibimos en el constructor (inyección por constructor)
    public UsuarioService(@Value("${esiusuarios.url}") String esiusuariosBaseUrl) {
        this.esiusuariosBaseUrl = esiusuariosBaseUrl;
    }

    // Verifica el token llamando al endpoint /checkToken del servicio de usuarios.
    public String checkToken(String token) {
        if (token == null || token.isEmpty()) {
            return null; // sin token no hay usuario
        }

        try {
            // Construimos la URL: http://localhost:8081/checkToken?token=xxxxx
            String url = UriComponentsBuilder.fromHttpUrl(esiusuariosBaseUrl)
                    .pathSegment("checkToken")     // añade /checkToken
                    .queryParam("token", token)    // añade ?token=xxxxx
                    .build()
                    .toUriString();

            // Hacemos la petición GET y esperamos una respuesta String
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            // is2xxSuccessful() → comprueba si el código HTTP es 200, 201, 202...
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody(); // el email del usuario
            }
            return null;

        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            System.err.println("Token no válido o expirado: " + ex.getMessage());
            return null;
        } catch (RestClientException ex) {
            System.err.println("Error de comunicación con esiusuarios: " + ex.getMessage());
            
            String detalles = "El servicio de usuarios no está disponible";
            if (ex instanceof org.springframework.web.client.HttpServerErrorException) {
                detalles = ((org.springframework.web.client.HttpServerErrorException) ex).getResponseBodyAsString();
            }
            
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, 
                detalles
            );
        }
    }

    // ============================================================
    // NUEVOS MÉTODOS DE COMUNICACIÓN CON EL MONEDERO
    // ============================================================

    // Envía una petición POST para sumarle dinero al monedero de un usuario tras una cancelación.
    public boolean sumarSaldo(String email, double cantidad) {
        try {
            String url = esiusuariosBaseUrl + "/sumarSaldo";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("cantidad", cantidad);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error de comunicación al sumar saldo en esiusuarios: " + e.getMessage());
            return false;
        }
    }

    // Envía una petición POST para descontar dinero del monedero cuando el usuario compra con su saldo.
    public boolean restarSaldo(String email, double cantidad) {
        try {
            String url = esiusuariosBaseUrl + "/restarSaldo";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("cantidad", cantidad);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error de comunicación al restar saldo en esiusuarios: " + e.getMessage());
            return false;
        }
    }
}