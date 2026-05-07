package edu.esi.ds.esientradas.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

// ============================================================
// SERVICIO DE COLA DE ESPERA
// ============================================================
// Imagina la taquilla física del Bernabéu el día de venta de entradas:
// hay colas de gente esperando para comprar. Este servicio hace lo mismo
// pero en el sistema web.
//
// ¿Por qué necesitamos una cola?
// Si 1000 usuarios intentan comprar a la vez la misma entrada,
// sin cola habría un caos (varios comprarían la misma). La cola
// garantiza que solo MAX_COMPRADORES_SIMULTANEOS usuarios
// (en este caso 3) pueden estar comprando al mismo tiempo.
// El resto espera su turno.
//
// Flujo del usuario:
//   1. El frontend llama a /cola/anotarse → el usuario entra en la cola
//   2. El frontend llama a /cola/posicion cada pocos segundos → sabe su turno
//   3. Cuando su posición es 0, puede pasar a comprar (canPass = true)
//   4. Cuando termina de pagar → finalizarCompra() → libera su hueco
//
// ¿Qué es ConcurrentLinkedQueue y ConcurrentHashMap?
// Son colecciones de Java diseñadas para ser usadas por MÚLTIPLES hilos a la vez
// sin corromper los datos. Como el servidor atiende peticiones en paralelo,
// necesitamos que estas estructuras sean "thread-safe" (seguras ante concurrencia).
// ============================================================
@Service
public class ColaService {

    // La cola de espera — los sessionIds en orden de llegada
    // ConcurrentLinkedQueue es como una lista pero thread-safe y en orden FIFO
    // FIFO = First In, First Out (el primero que llega, el primero en salir)
    private final Queue<String> colaEspera = new ConcurrentLinkedQueue<>();

    // Conjunto de usuarios que YA pasaron la cola y están comprando ahora mismo
    // ConcurrentHashMap.newKeySet() → un Set thread-safe
    private final Set<String> autorizados = ConcurrentHashMap.newKeySet();

    // Registra cuándo fue la última petición de cada usuario (para detectar inactivos)
    // Clave: sessionId | Valor: timestamp en milisegundos
    private final Map<String, Long> ultimaActividad = new ConcurrentHashMap<>();

    // Máximo de usuarios comprando simultáneamente
    // Si hay 3 comprando y llega un 4º, se queda en la cola esperando
    private final int MAX_COMPRADORES_SIMULTANEOS = 3;

    // Contador de cuántos usuarios hay actualmente en proceso de pago
    private int compradoresActivos = 0;

    // Añade al usuario a la cola si no está ya.
    // synchronized → garantiza que solo un hilo a la vez modifica la cola
    // Devuelve la posición actual del usuario (1 = primero, 2 = segundo, etc.)
    public synchronized int anotarseEnCola(String sessionId) {
        ultimaActividad.put(sessionId, System.currentTimeMillis());
        if (!colaEspera.contains(sessionId) && !autorizados.contains(sessionId)) {
            colaEspera.add(sessionId);
        }
        return getPosicion(sessionId);
    }

    // Devuelve la posición actual del usuario en la cola
    // Posición 0 → ya está autorizado para comprar (¡es su turno!)
    // Posición 1 → es el siguiente en entrar
    // Posición N → hay N-1 personas delante
    public synchronized int getPosicion(String sessionId) {
        if (autorizados.contains(sessionId)) return 0;
        int posicion = 1;
        for (String id : colaEspera) {
            if (id.equals(sessionId)) return posicion;
            posicion++;
        }
        return posicion;
    }

    // Comprueba si el usuario puede pasar a comprar.
    // Solo puede pasar si: es el primero de la cola Y hay hueco (< MAX simultáneos)
    // Si puede pasar: lo saca de la cola, lo pone en "autorizados" y aumenta el contador
    public synchronized boolean canPass(String sessionId) {
        ultimaActividad.put(sessionId, System.currentTimeMillis());
        if (autorizados.contains(sessionId)) return true; // ya estaba autorizado

        // peek() → mira el primero de la cola sin sacarlo
        // poll() → lo saca de la cola
        if (sessionId.equals(colaEspera.peek()) && compradoresActivos < MAX_COMPRADORES_SIMULTANEOS) {
            colaEspera.poll();
            autorizados.add(sessionId);
            compradoresActivos++;
            return true;
        }
        return false;
    }

    // El usuario terminó de comprar (o canceló) — libera su hueco para el siguiente
    public synchronized void finalizarCompra(String sessionId) {
        if (autorizados.remove(sessionId)) {
            compradoresActivos--;
        }
        ultimaActividad.remove(sessionId);
    }

    // Tarea programada: se ejecuta cada 5 segundos para detectar usuarios inactivos
    // Si un usuario lleva más de 20 segundos sin hacer ninguna petición,
    // se le expulsa de la cola (cerró el navegador o navegó a otra web)
    //
    // ANTES era: @Scheduled(fixedRate = 60000) con 5 minutos de límite
    //   → tardaba hasta 5 minutos en liberar el hueco de alguien que se iba
    // AHORA: cada 5 segundos, límite 20 segundos
    //   → en máximo 20 segundos el hueco se libera y el siguiente puede entrar
    @Scheduled(fixedRate = 5000)
    public synchronized void limpiarInactivos() {
        long ahora = System.currentTimeMillis();
        long LIMITE_INACTIVIDAD = 120_000L; // 2 minutos — tiempo para login y volver

        ultimaActividad.entrySet().removeIf(entry -> {
            boolean inactivo = (ahora - entry.getValue()) > LIMITE_INACTIVIDAD;
            if (inactivo) {
                String sId = entry.getKey();
                colaEspera.remove(sId);
                if (autorizados.remove(sId)) {
                    compradoresActivos--;
                }
                System.out.println("[ColaService] Sesión inactiva expulsada: " + sId);
            }
            return inactivo;
        });
    }
}