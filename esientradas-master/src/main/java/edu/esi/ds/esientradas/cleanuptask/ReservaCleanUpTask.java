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

// @Component → le dice a Spring que esta clase existe y debe gestionarla
// Es igual que @Service pero para clases de infraestructura (tareas, etc.)
@Component
public class ReservaCleanUpTask {

    // Inyectamos los DAOs que necesitamos para hablar con la base de datos
    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private EntradaDao entradaDao;

    // Tiempo máximo que una entrada puede estar RESERVADA sin que se pague
    // Si el usuario no paga en 10 minutos → la entrada vuelve a estar DISPONIBLE
    // 10 minutos = 10 * 60 * 1000 = 600.000 milisegundos
    private static final long TIEMPO_LIMITE_MS = 600_000L;

    // ============================================================
    // TAREA PROGRAMADA — se ejecuta automáticamente cada 60 segundos
    // ============================================================
    // @Scheduled(fixedRate = 60000) → cada 60.000 milisegundos = cada 1 minuto
    //   Spring arranca un hilo (proceso paralelo) y ejecuta este método solo
    //   No necesitas llamarlo manualmente — Spring lo hace por ti
    //
    // @Transactional → si algo falla a mitad, deshace todos los cambios
    //   (como cuando el banco cancela una transferencia si algo va mal)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void limpiarReservasCaducadas() {

        // Calculamos el "punto de corte":
        // tiempoActual - 10 minutos = el momento a partir del cual los tokens son "viejos"
        // Los tokens creados ANTES de este cutoff llevan más de 10 minutos → los borramos
        long cutoff = System.currentTimeMillis() - TIEMPO_LIMITE_MS;

        // Pedimos al DAO todos los tokens que sean más antiguos que el cutoff
        // SQL equivalente: SELECT * FROM token WHERE hora < cutoff
        List<Token> tokensCaducados = tokenDao.findAllOlderThan(cutoff);

        if (!tokensCaducados.isEmpty()) {
            System.out.println("[ReservaCleanUpTask] Limpiando " + tokensCaducados.size() + " reservas caducadas...");
        }

        // Procesamos cada token caducado uno a uno
        for (Token token : tokensCaducados) {

            // Obtenemos la entrada que tiene asociada este token
            Entrada entrada = token.getEntrada();

            if (entrada != null) {
                // La entrada llevaba más de 10 minutos RESERVADA sin pagar
                // → La ponemos de nuevo en DISPONIBLE para que otro usuario pueda comprarla
                entradaDao.updateEstado(entrada.getId(), Estado.DISPONIBLE);
                System.out.println("[ReservaCleanUpTask] Entrada " + entrada.getId() + " liberada (token caducado)");
            }

            // Borramos el token de la base de datos
            // Ya no tiene sentido conservarlo — la reserva ha expirado
            tokenDao.delete(token.getValor());
        }

        // ── CASO ESPECIAL: entradas RESERVADAS sin token (estado corrupto) ──
        // Puede ocurrir si el servidor se reinició a mitad de una operación
        // En ese caso la entrada quedó como RESERVADA pero sin token asociado
        // → Las liberamos también para que no queden bloqueadas para siempre
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

    

