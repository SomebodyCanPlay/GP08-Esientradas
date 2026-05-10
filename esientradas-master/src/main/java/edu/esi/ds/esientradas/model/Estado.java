package edu.esi.ds.esientradas.model;

public enum Estado {

    // La entrada está libre — cualquier usuario puede comprarla
    DISPONIBLE,

    // El usuario ha seleccionado la entrada y está en proceso de pago.
   // Nadie más puede comprarla mientras está RESERVADA.
    RESERVADA,

    // El pago se completó. La entrada ya no puede volver atrás.
    VENDIDA
}
