package edu.esi.ds.esientradas.services;

// ============================================================
// CLASE DESACTIVADA — su funcionalidad está en ReservaCleanUpTask
// ============================================================
// Esta clase hacía lo mismo que ReservaCleanUpTask (limpiar reservas caducadas)
// pero con un tiempo límite diferente (2 min vs 10 min), lo que causaba
// comportamiento inconsistente: una liberaba antes que la otra.
//
// La lógica completa está ahora en:
//   cleanuptask/ReservaCleanUpTask.java
// que además añade el caso especial de entradas RESERVADAS sin token.
//
// Mantenemos este archivo vacío para no romper imports si hubiera alguna
// referencia en otros archivos.
// ============================================================

import org.springframework.stereotype.Service;

@Service
public class LiberadorReservasJob {
    // Vacío intencionalmente — ver ReservaCleanUpTask.java
}

