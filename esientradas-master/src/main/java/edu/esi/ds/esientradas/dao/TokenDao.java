package edu.esi.ds.esientradas.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import edu.esi.ds.esientradas.model.Token;

// DAO de Token — implementación manual (no JpaRepository).
// Se implementa manualmente porque el ID es String y necesitamos borrar por valor.
@Repository
public class TokenDao {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    // Inserta un token.
    @Transactional
    public void save(Token token) {
        em.persist(token);
    }

    // Actualiza un token.
    @Transactional
    public Token update(Token token) {
        return em.merge(token);
    }

    // Borra un token por su valor (si existe).
    @Transactional
    public void delete(String valor) {
        Token t = em.find(Token.class, valor);
        if (t != null) {
            em.remove(t);
        }
    }

    // Busca por valor (UUID).
    public Optional<Token> findByValor(String valor) {
        return Optional.ofNullable(em.find(Token.class, valor));
    }

    // Busca el token por sessionId (primero si hay varios).
    public Optional<Token> findBySessionId(String sessionId) {
        TypedQuery<Token> q = em.createQuery("SELECT t FROM Token t WHERE t.sessionId = :sid", Token.class);
        q.setParameter("sid", sessionId);
        return q.getResultStream().findFirst();
    }

    // Devuelve todos los tokens de una sesión.
    public List<Token> findAllBySessionId(String sessionId) {
        TypedQuery<Token> q = em.createQuery("SELECT t FROM Token t WHERE t.sessionId = :sid", Token.class);
        q.setParameter("sid", sessionId);
        return q.getResultList();
    }

    // Busca token por entrada asociada.
    public Optional<Token> findByEntradaId(Long entradaId) {
        TypedQuery<Token> q = em.createQuery("SELECT t FROM Token t WHERE t.entrada.id = :eid", Token.class);
        q.setParameter("eid", entradaId);
        return q.getResultStream().findFirst();
    }

    // Tokens creados antes de cutoffMillis (usado para cleanup).
    public List<Token> findAllOlderThan(long cutoffMillis) {
        TypedQuery<Token> q = em.createQuery("SELECT t FROM Token t WHERE t.hora < :cutoff", Token.class);
        q.setParameter("cutoff", cutoffMillis);
        return q.getResultList();
    }
}