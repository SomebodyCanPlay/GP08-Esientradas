package edu.esi.ds.esientradas.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class ColaService {

    private final int MAX_COMPRADORES_SIMULTANEOS = 3;

    private class ColaEspectaculo {
        Queue<String> colaEspera = new ConcurrentLinkedQueue<>();
        Set<String> autorizados = ConcurrentHashMap.newKeySet();
        int compradoresActivos = 0;
    }

    private final Map<Long, ColaEspectaculo> colasPorEspectaculo = new ConcurrentHashMap<>();
    private final Map<String, Long> ultimaActividad = new ConcurrentHashMap<>();
    private final Map<String, Long> sesionConcierto = new ConcurrentHashMap<>();

    private ColaEspectaculo getCola(Long espectaculoId) {
        return colasPorEspectaculo.computeIfAbsent(espectaculoId, k -> new ColaEspectaculo());
    }

    public synchronized int anotarseEnCola(Long espectaculoId, String sessionId) {
        ultimaActividad.put(sessionId, System.currentTimeMillis());
        sesionConcierto.put(sessionId, espectaculoId);

        ColaEspectaculo cola = getCola(espectaculoId);

        if (!cola.colaEspera.contains(sessionId) && !cola.autorizados.contains(sessionId)) {
            cola.colaEspera.add(sessionId);
        }
        return getPosicion(espectaculoId, sessionId);
    }

    public synchronized int getPosicion(Long espectaculoId, String sessionId) {
        ColaEspectaculo cola = getCola(espectaculoId);
        if (cola.autorizados.contains(sessionId))
            return 0;

        int posicion = 1;
        for (String id : cola.colaEspera) {
            if (id.equals(sessionId))
                return posicion;
            posicion++;
        }
        return posicion;
    }

    public synchronized boolean canPass(Long espectaculoId, String sessionId) {
        ultimaActividad.put(sessionId, System.currentTimeMillis());
        ColaEspectaculo cola = getCola(espectaculoId);

        if (cola.autorizados.contains(sessionId))
            return true;

        if (sessionId.equals(cola.colaEspera.peek()) && cola.compradoresActivos < MAX_COMPRADORES_SIMULTANEOS) {
            cola.colaEspera.poll();
            cola.autorizados.add(sessionId);
            cola.compradoresActivos++;
            return true;
        }
        return false;
    }

    // =========================================================
    // ESTE ES EL MÉTODO QUE ESTABA FUERA DE SITIO
    // =========================================================
    public synchronized boolean canPass(String sessionId) {
        Long espectaculoId = sesionConcierto.get(sessionId);
        if (espectaculoId == null)
            return false;
        return canPass(espectaculoId, sessionId);
    }

    public synchronized void finalizarCompra(String sessionId) {
        Long espectaculoId = sesionConcierto.get(sessionId);
        if (espectaculoId != null) {
            ColaEspectaculo cola = getCola(espectaculoId);
            if (cola.autorizados.remove(sessionId)) {
                cola.compradoresActivos--;
            }
        }
        ultimaActividad.remove(sessionId);
        sesionConcierto.remove(sessionId);
    }

    // El "barrendero" pasa CADA SEGUNDO (1000 ms) para asegurar que la cola avance
    // rapidísimo
    // El limpiador revisa la cola rapidísimo (cada 1 segundo)
    @Scheduled(fixedRate = 1000)
    public synchronized void limpiarInactivos() {
        long ahora = System.currentTimeMillis();

        // 15 segundos de margen para los que están en la pantalla roja (avisan cada 2s)
        long TIMEOUT_COLA = 15_000L;

        // LOS 2 MINUTOS REALES para el que está comprando
        long TIMEOUT_COMPRA = 120_000L;

        ultimaActividad.entrySet().removeIf(entry -> {
            String sId = entry.getKey();
            Long espectaculoId = sesionConcierto.get(sId);

            if (espectaculoId != null) {
                ColaEspectaculo cola = getCola(espectaculoId);

                boolean estaComprando = cola.autorizados.contains(sId);
                long limite = estaComprando ? TIMEOUT_COMPRA : TIMEOUT_COLA;

                boolean inactivo = (ahora - entry.getValue()) > limite;

                if (inactivo) {
                    cola.colaEspera.remove(sId);
                    // ¡AQUÍ ESTÁ LA CLAVE! Al echarlo, restamos 1 a los activos
                    if (cola.autorizados.remove(sId)) {
                        cola.compradoresActivos--;
                    }
                    sesionConcierto.remove(sId);
                    System.out.println("[ColaService] Sesión expulsada tras 2 minutos inactivo: " + sId);
                    return true;
                }
            } else {
                if ((ahora - entry.getValue()) > TIMEOUT_COLA) {
                    return true;
                }
            }
            return false;
        });
    }
}