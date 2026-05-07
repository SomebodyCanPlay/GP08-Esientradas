package edu.esi.ds.esientradas.model;

// ¿Qué es un enum?
// Es una lista fija de valores posibles para una variable.
// Es como decir: "el estado de una entrada SOLO puede ser uno de estos tres valores"
// Java no deja poner otro valor distinto — eso evita errores.
//
// Ciclo de vida de una entrada:
//
//   DISPONIBLE ──→ RESERVADA ──→ VENDIDA
//        ↑              │
//        └──────────────┘
//   (si el usuario no paga en 10 min, ReservaCleanUpTask la devuelve a DISPONIBLE)
//
public enum Estado {

    // La entrada está libre — cualquier usuario puede comprarla
    DISPONIBLE,

    // El usuario ha seleccionado la entrada y está en proceso de pago.
    // Tiene un Token asociado que caduca en 10 minutos.
    // Nadie más puede comprarla mientras está RESERVADA.
    RESERVADA,

    // El pago se completó. La entrada ya no puede volver atrás.
    VENDIDA
}
