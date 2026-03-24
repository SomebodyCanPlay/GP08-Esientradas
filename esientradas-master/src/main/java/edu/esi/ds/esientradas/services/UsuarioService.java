package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UsuarioService {

	private final String esiusuariosBaseUrl;
	private final RestTemplate restTemplate = new RestTemplate();

	public UsuarioService(@Value("${esiusuarios.url}") String esiusuariosBaseUrl) {
		this.esiusuariosBaseUrl = esiusuariosBaseUrl;
	}

	/**
	 * Envía una petición GET a {esiusuarios.url}/checkToken?token=...
	 * Devuelve el cuerpo de la respuesta si el status es 200 OK, devuelve null si token nulo/vacío
	 * Lanza RestClientException si ocurre un error de cliente/servidor (para que el controlador lo capture).
	 */
	public String checkToken(String token) {
		if (token == null || token.isEmpty()) {
			return null;
		}
		try {
			String url = UriComponentsBuilder.fromHttpUrl(esiusuariosBaseUrl)
					.pathSegment("checkToken")
					.queryParam("token", token)
					.build()
					.toUriString();

			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				return response.getBody();
			}
			return null;
		} catch (RestClientException ex) {
			// Propagar para que el controlador maneje la autorización/errores
			throw ex;
		}
	}
}