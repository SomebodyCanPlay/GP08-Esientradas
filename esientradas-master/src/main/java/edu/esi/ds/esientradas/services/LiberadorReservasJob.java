package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import jakarta.transaction.Transactional;
import java.util.List;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Token;

@Service
public class LiberadorReservasJob {

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private TokenDao tokenDao;

    // Se ejecuta cada 60 segundos
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void liberarReservasCaducadas() {
        List<Entrada> reservadas = entradaDao.findByEstado(Estado.RESERVADA);
        long tiempoActual = System.currentTimeMillis();
        // 2 minutos = 2 * 60 * 1000 = 120000 ms
        long LIMITE_MS = 120000;

        for (Entrada entrada : reservadas) {
            Token token = entrada.getToken();
            if (token != null) {
                if (tiempoActual - token.getHora() > LIMITE_MS) {
                    System.out.println("Liberando entrada caducada automáticamente: " + entrada.getId());
                    entrada.setEstado(Estado.DISPONIBLE);
                    entrada.setToken(null);
                    entradaDao.save(entrada);
                    tokenDao.delete(token);
                }
            } else {
                // Si está reservada pero sin token (corrupción de estado), forzar liberación
                entrada.setEstado(Estado.DISPONIBLE);
                entradaDao.save(entrada);
            }
        }
    }
}
