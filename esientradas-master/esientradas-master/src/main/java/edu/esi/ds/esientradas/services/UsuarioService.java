package edu.esi.ds.esientradas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class UsuarioService {
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    /**
     * Valida un token contra el servicio externo y devuelve el nombre de usuario asociado.
     *
     * @param userToken token a comprobar
     * @return nombre de usuario si el token es válido, {@code null} en caso de error o token inválido
     */
    public String checkToken(String userToken) {
        String endpoint = "http://localhost:8081/external/checkToken/1234";
        RestTemplate rest = new RestTemplate();

        try {
            String userName= rest.getForObject(endpoint + "/" + userToken, String.class);
            if (userName == null || userName.isEmpty()) {
                throw  new RestStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o no asociado a ningún usuario");
            }
        
        return userName;
        }catch (RestClientException e) {
            throw new RestStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al comunicarse con el servicio de validación de tokens", e);
        }
    }

