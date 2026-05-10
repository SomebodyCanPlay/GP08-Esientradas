package edu.esi.ds.esientradas.cleanuptask;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Token;

import java.util.List;

// Componente Spring que libera reservas caducadas.
@Component
public class ReservaCleanUpTask {

    // DAOs inyectados.
    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private EntradaDao entradaDao;

    // Tiempo máximo de reserva: 10 minutos.
    private static final long TIEMPO_LIMITE_MS = 600_000L;

    // Tarea programada cada minuto para limpiar tokens/entradas caducadas.
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void limpiarReservasCaducadas() {

        // Punto de corte: ahora - 10 minutos.
        long cutoff = System.currentTimeMillis() - TIEMPO_LIMITE_MS;

        // Obtener tokens anteriores al cutoff.
        List<Token> tokensCaducados = tokenDao.findAllOlderThan(cutoff);

        if (!tokensCaducados.isEmpty()) {
            System.out.println("[ReservaCleanUpTask] Limpiando " + tokensCaducados.size() + " reservas caducadas...");
        }

        // Procesar tokens caducados.
        for (Token token : tokensCaducados) {

            Entrada entrada = token.getEntrada();

            if (entrada != null) {
                // Liberar la entrada reservada.
                entradaDao.updateEstado(entrada.getId(), Estado.DISPONIBLE);
                System.out.println("[ReservaCleanUpTask] Entrada " + entrada.getId() + " liberada (token caducado)");
            }

            // Eliminar el token.
            tokenDao.delete(token.getValor());
        }

        // Entradas marcadas como RESERVADA pero sin token: liberarlas.
        List<Entrada> reservadasSinToken = entradaDao.findByEstado(Estado.RESERVADA);
        for (Entrada entrada : reservadasSinToken) {
            if (entrada.getToken() == null) {
                entrada.setEstado(Estado.DISPONIBLE);
                entradaDao.save(entrada);
                System.out.println("[ReservaCleanUpTask] Entrada " + entrada.getId() + " liberada (RESERVADA sin token)");
            }
        }
    }
}