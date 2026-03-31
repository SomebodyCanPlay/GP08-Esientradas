package edu.esi.ds.esientradas.cleanuptask;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.model.Token;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;

import java.util.List;

/**
 * Tarea programada que limpia tokens caducados:
 * - Busca tokens con hora anterior a 10 minutos
 * - Marca la entrada asociada como DISPONIBLE
 * - Borra el token
 *
 * Nota: requiere @EnableScheduling en la configuración de la aplicación.
 */
@Component
public class ReservaCleanUpTask {

    private final TokenDao tokenDao;
    private final EntradaDao entradaDao;

    @Autowired
    public ReservaCleanUpTask(TokenDao tokenDao, EntradaDao entradaDao) {
        this.tokenDao = tokenDao;
        this.entradaDao = entradaDao;
    }

    // Ejecutar cada minuto
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredTokens() {
        long cutoff = System.currentTimeMillis() - 600_000L; // 10 minutos en ms
        List<Token> expired = tokenDao.findAllOlderThan(cutoff);
        for (Token t : expired) {
            Entrada e = t.getEntrada();
            if (e != null) {
                // Usar el método de EntradaDao para actualizar el estado al enum DISPONIBLE
                entradaDao.updateEstado(e.getId(), Estado.DISPONIBLE);
            }
            // Borrar el token de la base de datos
            tokenDao.delete(t.getValor());
        }
    }
}
    

