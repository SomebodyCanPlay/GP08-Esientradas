package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.dao.*;

@Service
public class BusquedaService {
    
    @Autowired
    private EscenarioDao dao;

    @Autowired
    private EspectaculoDao espectaculoDao;

    public List<Escenario> getEscenarios() {
        return this.dao.findAll();
    }

    public List<Espectaculo> getEspectaculos(String artista) {
        return this.espectaculoDao.findByArtista(artista);
    }

    public List<Entrada> getEntradas(String espectaculoid) {
        return this.espectaculoDao.findById(Long.parseLong(espectaculoid)).orElseThrow().getEntradas();
    }
}
