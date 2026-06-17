package edu.esi.ds.esientradas.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.esi.ds.esientradas.model.Pago;

// DAO de Pago: gestiona la entidad Pago y añade consultas útiles.
public interface PagoDao extends JpaRepository<Pago, Long> {

    // Pago por id de intento de Stripe.
    Optional<Pago> findByIdIntentoPago(String idIntentoPago);

    // Pagos por estado.
    List<Pago> findByEstado(String estado);
}