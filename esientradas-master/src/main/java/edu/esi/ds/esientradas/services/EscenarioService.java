package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import edu.esi.ds.esientradas.dao.EscenarioDao;
import edu.esi.ds.esientradas.model.Escenario;

// ============================================================
// SERVICIO DE ESCENARIO — gestiona los recintos del sistema
// ============================================================
// Permite crear nuevos escenarios (recintos de espectáculos).
// Normalmente solo el administrador crea escenarios.
// Los usuarios finales solo los consultan a través de BusquedaService.
// ============================================================
@Service
public class EscenarioService {

    @Autowired
    private EscenarioDao dao;

    // Inserta un nuevo escenario en la BD
    // Si hay un error de integridad (ej: nombre duplicado con restricción UNIQUE),
    // lanza 400 Bad Request en vez de dejar que Spring muestre un error genérico 500
    public void insertar(Escenario escenario) {
        try {
            this.dao.save(escenario);
        } catch (DataIntegrityViolationException e) {
            // DataIntegrityViolationException → el dato viola una restricción de la BD
            // (ej: el campo "nombre" tiene una restricción UNIQUE y ya existe ese nombre)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            // Cualquier otro error inesperado → también 400 con mensaje descriptivo
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Error al insertar el escenario: " + e.getMessage());
        }
    }
}
