package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

// Servicio de usuario: consulta el microservicio esiusuarios para verificar tokens.
@Service
public class UsuarioService {

    // URL base del microservicio esiusuarios (ej: "http://localhost:8081")
    private final String esiusuariosBaseUrl;

    // RestTemplate es la herramienta de Spring para hacer peticiones HTTP a otros servidores
    // Es como un "cliente HTTP" incorporado — similar a fetch() en JavaScript
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
            // UriComponentsBuilder es una herramienta para construir URLs de forma segura
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
            // Si esiusuarios devuelve 400/401/403, el token no es válido o caducó
            System.err.println("Token no válido o expirado: " + ex.getMessage());
            return null;
        } catch (RestClientException ex) {
            // Si esiusuarios está caído o devuelve 500, informamos en log
            System.err.println("Error de comunicación con esiusuarios: " + ex.getMessage());
            
            // Si es un HttpServerErrorException, podemos sacar el cuerpo del error
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
}