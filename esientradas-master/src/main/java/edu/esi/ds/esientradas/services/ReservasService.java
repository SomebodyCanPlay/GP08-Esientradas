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

// ============================================================
// SERVICIO DE RESERVAS
// ============================================================
// Gestiona el "bloqueo temporal" de entradas.
//
// Flujo completo de una reserva:
//   1. Usuario ve el plano de asientos y hace clic en una butaca
//   2. ReservasController llama a reservar(idEntrada, sessionId, tokenValor)
//   3. Este servicio:
//      a) Crea o recupera el Token del usuario
//      b) Verifica que la entrada esté DISPONIBLE
//      c) Cambia el estado a RESERVADA
//      d) Vincula el Token con la Entrada
//      e) Devuelve el valor del token y el precio al frontend
//   4. El frontend guarda el token y lo manda en el pago
//   5. Si no paga en 2 min → ReservaCleanUpTask libera la entrada
//   6. Si paga → PagosService borra el token y marca la entrada VENDIDA
// ============================================================
@Service
public class ReservasService {

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private TokenDao tokenDao;

    // ============================================================
    // RESERVAR una entrada
    // ============================================================
    // Devuelve un Map con:
    //   - "token"        → el valor del token (para usarlo al pagar)
    //   - "precioEntrada" → el precio en céntimos (para mostrarlo en el frontend)
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
