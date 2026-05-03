package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.Queue;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class ColaService {
    private final Queue<String> colaEspera = new ConcurrentLinkedQueue<>();
    // Usuarios que ya han salido de la cola y están en la pasarela de pago
    private final Set<String> autorizados = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> ultimaActividad = new ConcurrentHashMap<>();
    
    private final int MAX_COMPRADORES_SIMULTANEOS = 5;
    private int compradoresActivos = 0;

    public synchronized int anotarseEnCola(String sessionId) {
        ultimaActividad.put(sessionId, System.currentTimeMillis());
        if (!colaEspera.contains(sessionId) && !autorizados.contains(sessionId)) {
            colaEspera.add(sessionId);
        }
        return getPosicion(sessionId);
    }

    public synchronized int getPosicion(String sessionId) {
        if (autorizados.contains(sessionId)) return 0; // Posición 0 significa "puedes pasar"
        int posicion = 1;
        for (String id : colaEspera) {
            if (id.equals(sessionId)) return posicion;
            posicion++;
        }
        return posicion;
    }

    public synchronized boolean canPass(String sessionId) {
        ultimaActividad.put(sessionId, System.currentTimeMillis());
        if (autorizados.contains(sessionId)) return true;

        // Lógica FIFO: Solo si eres el primero y hay hueco disponible
        if (sessionId.equals(colaEspera.peek()) && compradoresActivos < MAX_COMPRADORES_SIMULTANEOS) {
            colaEspera.poll();
            autorizados.add(sessionId);
            compradoresActivos++;
            return true;
        }
        return false;
    }

    public synchronized void finalizarCompra(String sessionId) {
        if (autorizados.remove(sessionId)) {
            compradoresActivos--;
        }
        ultimaActividad.remove(sessionId);
    }

    // Se ejecuta cada minuto para limpiar inactivos
    @Scheduled(fixedRate = 60000)
    public synchronized void limpiarInactivos() {
        long ahora = System.currentTimeMillis();
        // 5 minutos sin peticiones = inactivo
        long LIMITE_INACTIVIDAD = 300000; 

        ultimaActividad.entrySet().removeIf(entry -> {
            boolean inactivo = (ahora - entry.getValue()) > LIMITE_INACTIVIDAD;
            if (inactivo) {
                String sId = entry.getKey();
                colaEspera.remove(sId);
                if (autorizados.remove(sId)) {
                    compradoresActivos--;
                }
            }
            return inactivo;
        });
    }
}