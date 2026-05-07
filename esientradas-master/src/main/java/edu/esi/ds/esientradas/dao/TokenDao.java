package edu.esi.ds.esientradas.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import edu.esi.ds.esientradas.model.Token;

// ============================================================
// DAO de Token — implementación manual (no JpaRepository)
// ============================================================
// A diferencia del resto de DAOs, este NO extiende JpaRepository.
// ¿Por qué? Porque el ID del Token es un String (no un Long),
// y el método delete() de JpaRepository espera el objeto entero.
// Aquí necesitamos borrar por valor (String), así que lo hacemos a mano.
//
// @Repository → le dice a Spring que esta clase es un DAO (capa de datos)
//
// EntityManager es la herramienta de JPA para hablar con la BD directamente.
// Es como un "intérprete" entre Java y la base de datos:
//   - em.persist(token)  → INSERT en BD
//   - em.merge(token)    → UPDATE en BD
//   - em.find(...)       → SELECT por ID
//   - em.remove(token)   → DELETE en BD
//   - em.createQuery()   → SELECT personalizado con JPQL (lenguaje parecido a SQL pero con clases Java)
// ============================================================
@Repository
public class TokenDao {

    // @PersistenceContext → Spring inyecta el EntityManager automáticamente
    @PersistenceContext(unitName = "default")
    private EntityManager em;

    // Guarda un Token nuevo en la BD (INSERT)
    @Transactional
    public void save(Token token) {
        em.persist(token);
    }

    // Actualiza un Token existente en la BD (UPDATE)
    @Transactional
    public Token update(Token token) {
        return em.merge(token);
    }

    // Borra un Token de la BD buscándolo por su valor (String UUID)
    // Si no existe, no hace nada (no lanza error)
    @Transactional
    public void delete(String valor) {
        Token t = em.find(Token.class, valor);
        if (t != null) {
            em.remove(t);
        }
    }

    // Busca un Token por su valor (UUID)
    // Devuelve Optional → si no existe, devuelve Optional.empty() en vez de null
    // (evita NullPointerException)
    public Optional<Token> findByValor(String valor) {
        return Optional.ofNullable(em.find(Token.class, valor));
    }

    // Busca el token de una sesión de navegador concreta
    // JPQL: SELECT t FROM Token t WHERE t.sessionId = ?
    // findFirst() → devuelve solo el primero si hay varios (no debería haber más de uno por sesión)
    public Optional<Token> findBySessionId(String sessionId) {
        TypedQuery<Token> q = em.createQuery("SELECT t FROM Token t WHERE t.sessionId = :sid", Token.class);
        q.setParameter("sid", sessionId);
        return q.getResultStream().findFirst();
    }

    // Devuelve TODOS los tokens de una sesión (el usuario puede tener varias entradas en el carrito)
    // Usado por PagosService en firmarPagosPorSession()
    public List<Token> findAllBySessionId(String sessionId) {
        TypedQuery<Token> q = em.createQuery("SELECT t FROM Token t WHERE t.sessionId = :sid", Token.class);
        q.setParameter("sid", sessionId);
        return q.getResultList();
    }

    // Busca el token asociado a una entrada concreta
    public Optional<Token> findByEntradaId(Long entradaId) {
        TypedQuery<Token> q = em.createQuery("SELECT t FROM Token t WHERE t.entrada.id = :eid", Token.class);
        q.setParameter("eid", entradaId);
        return q.getResultStream().findFirst();
    }

    // Devuelve todos los tokens creados ANTES de cutoffMillis
    // Lo usa ReservaCleanUpTask para encontrar tokens caducados:
    //   cutoffMillis = ahora - 10 minutos
    //   JPQL: SELECT t FROM Token t WHERE t.hora < cutoffMillis
    public List<Token> findAllOlderThan(long cutoffMillis) {
        TypedQuery<Token> q = em.createQuery("SELECT t FROM Token t WHERE t.hora < :cutoff", Token.class);
        q.setParameter("cutoff", cutoffMillis);
        return q.getResultList();
    }
}