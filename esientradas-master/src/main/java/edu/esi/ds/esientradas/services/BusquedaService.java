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

    public List<Entrada> getEntradas(String espectaculoId) {
        return this.espectaculoDao.findById(Long.parseLong(espectaculoId)).orElseThrow().getEntradas();
    }

    public Integer getNumeroEntradasDisponibles(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoId(espectaculoId);
    }

    public Integer getEntradasLibres(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
    }

    public DtoEntradas getNumeroDeEntradasComoDto(Long espectaculoId) {
        Object o = this.entradaDao.getNumeroEntradasComoDto(espectaculoId);
        Object[] arr = (Object[]) o;
        DtoEntradas dto = new DtoEntradas();
            ((Number) arr[0]).intValue();
            ((Number) arr[1]).intValue();
            ((Number) arr[2]).intValue();
            ((Number) arr[3]).intValue();
        return dto;
    }
}
