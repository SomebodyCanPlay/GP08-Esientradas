package edu.esi.ds.esientradas.dao;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import edu.esi.ds.esientradas.model.Token;

@ApplicationScoped
public class TokenDao {
    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Transactional
    public void save(Token token) {
        em.persist(token);
    }

    @Transactional
    public Token update(Token token) {
        return em.merge(token);
    }

    @Transactional
    public void delete(String valor) {
        Token t = em.find(Token.class, valor);
        if (t != null) {
            em.remove(t);
        }
    }

    public Optional<Token> findByValor(String valor) {
        return Optional.ofNullable(em.find(Token.class, valor));
    }

    public Optional<Token> findBySessionId(String sessionId) {
        TypedQuery<Token> q = em.createQuery("SELECT t FROM Token t WHERE t.sessionId = :sid", Token.class);
        q.setParameter("sid", sessionId);
        return q.getResultStream().findFirst();
    }

    public Optional<Token> findByEntradaId(Long entradaId) {
        TypedQuery<Token> q = em.createQuery("SELECT t FROM Token t WHERE t.entrada.id = :eid", Token.class);
        q.setParameter("eid", entradaId);
        return q.getResultStream().findFirst();
    }
}