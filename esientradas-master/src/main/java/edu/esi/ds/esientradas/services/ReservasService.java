package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Token;
import edu.esi.ds.esientradas.model.Estado;

@Service
public class ReservasService {

	@Autowired
	private EntradaDao entradaDao;

	@Autowired
	private TokenDao tokenDao;

	/**
	 * Reserva una entrada asociándola a un token (nuevo o existente).
	 * Retorna un Map con "token" (String) y "precioEntrada" (Long).
	 */
	@Transactional
	public Map<String, Object> reservar(Long idEntrada, String sessionId, String tokenValor) {
		// Preparar/obtener token
		Token token;
		boolean tokenEsNuevo = false;

		if (tokenValor == null || tokenValor.isEmpty()) {
			token = new Token();
			tokenValor = UUID.randomUUID().toString();
			token.setValor(tokenValor);
			token.setSessionId(sessionId);
			token.setHora(System.currentTimeMillis());
			tokenEsNuevo = true;
		} else {
			Optional<Token> opt = tokenDao.findByValor(tokenValor);
			if (opt.isPresent()) {
				token = opt.get();
				token.setSessionId(sessionId);
				token.setHora(System.currentTimeMillis());
			} else {
				token = new Token();
				token.setValor(tokenValor);
				token.setSessionId(sessionId);
				token.setHora(System.currentTimeMillis());
				tokenEsNuevo = true;
			}
		}

		// Buscar la entrada
		Entrada entrada = entradaDao.findById(idEntrada)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

		// Comprobar disponibilidad
		if (entrada.getEstado() != Estado.DISPONIBLE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Entrada no disponible para reservar");
		}

		// Asociar y marcar como reservada
		entrada.setEstado(Estado.RESERVADA);
		entrada.setToken(token);
		token.setEntrada(entrada);

		// Persistir token y entrada
		if (tokenEsNuevo) {
			tokenDao.save(token);
		} else {
			tokenDao.update(token);
		}
		entradaDao.save(entrada);

		// Preparar resultado
		Map<String, Object> result = new HashMap<>();
		result.put("token", token.getValor());
		result.put("precioEntrada", entrada.getPrecio()); // se asume Long
		return result;
	}

	@Transactional
	public void cancelarReserva(Long idEntrada, String sessionId) {
		Entrada entrada = entradaDao.findById(idEntrada).orElse(null);
		if (entrada != null && entrada.getEstado() == Estado.RESERVADA) {
			Token token = entrada.getToken();
			if (token != null && sessionId.equals(token.getSessionId())) {
				entrada.setEstado(Estado.DISPONIBLE);
				entrada.setToken(null);
				entradaDao.save(entrada);
				tokenDao.delete(token.getValor());
			}
		}
	}

}
