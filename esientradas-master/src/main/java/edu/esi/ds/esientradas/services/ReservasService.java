package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Token;

// Servicio de reservas: gestiona reservas temporales.
@Service
public class ReservasService {

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private TokenDao tokenDao;

    // ============================================================
    // RESERVAR una entrada
    // ============================================================
    // Reserva una entrada y devuelve token + precio.
    // ============================================================
    @Transactional
    public Map<String, Object> reservar(Long idEntrada, String sessionId, String tokenValor) {

        Token token;
        boolean tokenEsNuevo = false;

        // Si el usuario no tenía token → crear uno nuevo
        // Si ya tenía un token (de una entrada anterior en el carrito) → reutilizarlo
        if (tokenValor == null || tokenValor.isEmpty()) {
            // Crear token nuevo
            token = new Token();
            tokenValor = UUID.randomUUID().toString();
            token.setValor(tokenValor);
            token.setSessionId(sessionId);
            token.setHora(System.currentTimeMillis());
            tokenEsNuevo = true;
        } else {
            Optional<Token> opt = tokenDao.findByValor(tokenValor);
            if (opt.isPresent()) {
                // El token existe → actualizamos su hora (reiniciamos el temporizador)
                token = opt.get();
                token.setSessionId(sessionId);
                token.setHora(System.currentTimeMillis());
            } else {
                // El token ya no existe en BD (expiró) → crear uno nuevo
                token = new Token();
                token.setValor(tokenValor);
                token.setSessionId(sessionId);
                token.setHora(System.currentTimeMillis());
                tokenEsNuevo = true;
            }
        }

        // Buscamos la entrada en la BD
        Entrada entrada = entradaDao.findById(idEntrada)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        // Comprobamos que la entrada esté libre — si ya está reservada o vendida, rechazamos
        if (entrada.getEstado() != Estado.DISPONIBLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Entrada no disponible para reservar");
        }


        entrada.setEstado(Estado.RESERVADA);
        entrada.setToken(token);
        token.setEntrada(entrada);

        // Guardamos en la BD
        if (tokenEsNuevo) {
            tokenDao.save(token);    // INSERT
        } else {
            tokenDao.update(token);  // UPDATE
        }
        entradaDao.save(entrada);    // UPDATE del estado

        // Preparamos la respuesta para el frontend
        Map<String, Object> result = new HashMap<>();
        result.put("token", token.getValor());
        result.put("precioEntrada", entrada.getPrecio());
        return result;
    }


    @Transactional
    public void cancelarReserva(Long idEntrada, String sessionId) {
        Entrada entrada = entradaDao.findById(idEntrada).orElse(null);

        if (entrada != null && entrada.getEstado() == Estado.RESERVADA) {
            Token token = entrada.getToken();

            // Verificamos que el token pertenece a esta sesión (seguridad)
            if (token != null && sessionId.equals(token.getSessionId())) {
                entrada.setEstado(Estado.DISPONIBLE); // liberar entrada
                entrada.setToken(null);               // desvincular token
                entradaDao.save(entrada);
                tokenDao.delete(token.getValor());    // borrar el token de la BD
            }
        }
    }

    // Devuelve los IDs de las entradas que esta sesión tiene reservadas
    public java.util.List<Long> getIdsReservados(String sessionId) {
        return tokenDao.findAllBySessionId(sessionId)
                .stream()
                .map(t -> t.getEntrada().getId())
                .toList();
    }
}
