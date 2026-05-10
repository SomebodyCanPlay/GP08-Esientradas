package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import edu.esi.ds.esientradas.dao.EscenarioDao;
import edu.esi.ds.esientradas.model.Escenario;

// Servicio de escenario: inserta escenarios y gestiona errores de integridad.
@Service
public class EscenarioService {

    @Autowired
    private EscenarioDao dao;

    // Inserta escenario; convierte errores de integridad en 400 Bad Request.
    public void insertar(Escenario escenario) {
        try {
            this.dao.save(escenario);
        } catch (DataIntegrityViolationException e) {
            // Violación de restricción DB → 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            // Error inesperado → 400 con descripción
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Error al insertar el escenario: " + e.getMessage());
        }
    }
}
