package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Token;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservasService {

    @Autowired
    private EntradaDao dao;
    
    @Transactional
    public Long reservar(Long idEntrada, String sessionId) {
        Entrada entrada = this.dao.findById(idEntrada).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Entrada no encontrada"));
        if(entrada.getEstado() != Estado.DISPONIBLE){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La entrada no esta disponible");
        }
        // Asignar Token asociado a la sesión
        Token token = new Token();
        token.setSessionId(sessionId);
        // vínculo bidireccional: token es dueña de la FK
        token.setEntrada(entrada);
        entrada.setToken(token);

        this.dao.updateEstado(idEntrada, Estado.RESERVADA);
        // al guardar la entrada se persistirá también el token (cascade si está configurado)
        this.dao.save(entrada); // Guardar entrada y token
        return entrada.getPrecio();
    }

}
