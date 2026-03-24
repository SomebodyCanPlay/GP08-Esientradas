package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Servicio simple de cola que controla cuántas sesiones activas hay y
 * decide si una sesión puede pasar o debe esperar.
 */
@Service
public class ColaService {

	// Mapa sessionId -> lastAccessMillis
	private final ConcurrentMap<String, Long> activeSessions = new ConcurrentHashMap<>();

	// Valor configurable en application.properties: cola.maxActive (por defecto 10)
	private final int maxActive;

	// Tiempo en ms tras el cual una sesión se considera inactiva (por defecto 5 minutos)
	private final long sessionTimeoutMillis;

	// Intervalo de limpieza programada en ms (por defecto 1 minuto)
	private final long cleanupIntervalMillis;

	public ColaService(
			@Value("${cola.maxActive:10}") int maxActive,
			@Value("${cola.sessionTimeoutMillis:300000}") long sessionTimeoutMillis,
			@Value("${cola.cleanupIntervalMillis:60000}") long cleanupIntervalMillis) {
		this.maxActive = maxActive;
		this.sessionTimeoutMillis = sessionTimeoutMillis;
		this.cleanupIntervalMillis = cleanupIntervalMillis;
	}

	/**
	 * Comprueba si la sesión puede pasar.
	 * - Si sessionId ya está registrada, actualiza su timestamp y devuelve true.
	 * - Si hay menos de maxActive sesiones activas, registra la sesión y devuelve true.
	 * - En caso contrario devuelve false (debe esperar).
	 */
	public boolean canPass(String sessionId) {
		if (sessionId == null || sessionId.isEmpty()) {
			return false;
		}
		long now = System.currentTimeMillis();

		// Si ya está activa, actualizar timestamp y permitir paso
		if (activeSessions.containsKey(sessionId)) {
			activeSessions.put(sessionId, now);
			return true;
		}

		// Controlar concurrencia al añadir nueva sesión
		synchronized (this) {
			if (activeSessions.containsKey(sessionId)) {
				activeSessions.put(sessionId, now);
				return true;
			}
			if (activeSessions.size() < maxActive) {
				activeSessions.put(sessionId, now);
				return true;
			}
			return false;
		}
	}

	/**
	 * Libera la sesión de la cola (cuando el usuario sale).
	 */
	public void release(String sessionId) {
		if (sessionId == null) return;
		activeSessions.remove(sessionId);
	}

	/**
	 * Devuelve el número de sesiones activas (útil para métricas/tests).
	 */
	public int getActiveCount() {
		return activeSessions.size();
	}

	/**
	 * Limpieza automática programada: elimina sesiones que no han interactuado
	 * en los últimos sessionTimeoutMillis.
	 *
	 * Nota: Para que esto funcione, la aplicación debe tener @EnableScheduling en la configuración.
	 */
	@Scheduled(fixedDelayString = "${cola.cleanupIntervalMillis:60000}")
	public void cleanupInactiveSessions() {
		long cutoff = System.currentTimeMillis() - sessionTimeoutMillis;
		// removeIf es seguro en ConcurrentHashMap.entrySet()
		activeSessions.entrySet().removeIf(entry -> entry.getValue() < cutoff);
	}
}