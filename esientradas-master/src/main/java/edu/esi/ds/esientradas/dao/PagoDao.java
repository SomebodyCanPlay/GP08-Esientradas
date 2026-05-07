package edu.esi.ds.esientradas.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import edu.esi.ds.esientradas.model.Pago;

// ============================================================
// DAO de Pago
// ============================================================
// Gestiona la tabla "pago" de la base de datos.
// Extiende JpaRepository → save(), findById(), findAll(), delete() gratis.
// ============================================================
public interface PagoDao extends JpaRepository<Pago, Long> {

    // Busca el pago asociado a una entrada concreta
    // SQL: SELECT * FROM pago WHERE entrada_id = ?
    // Útil si necesitamos consultar si ya se pagó una entrada
    Optional<Pago> findByEntradaId(Long entradaId);

    // Busca un pago por su ID de intento en Stripe (empieza por "pi_...")
    // SQL: SELECT * FROM pago WHERE id_intento_pago = ?
    // Lo usamos en firmarPago() para no crear un Pago duplicado si Stripe
    // confirma el pago dos veces (webhook doble, por ejemplo)
    Optional<Pago> findByIdIntentoPago(String idIntentoPago);

    // Busca todos los pagos con un estado concreto
    // SQL: SELECT * FROM pago WHERE estado = ?
    // Ejemplo: findByEstado("COMPLETADO") → todos los pagos exitosos
    List<Pago> findByEstado(String estado);
}
