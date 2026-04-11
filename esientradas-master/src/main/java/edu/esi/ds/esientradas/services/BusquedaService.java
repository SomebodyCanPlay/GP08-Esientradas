package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.dao.*;

import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.dto.DtoEntradas;
import java.util.Optional;

@Service
public class BusquedaService {
    
    @Autowired
    private EntradaDao entradaDao;
    
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
        if (o == null) return new DtoEntradas(0,0,0,0);
        Object[] arr = (Object[]) o;
        
        Integer total = arr[0] != null ? ((Number) arr[0]).intValue() : 0;
        Integer libres = arr[1] != null ? ((Number) arr[1]).intValue() : 0;
        Integer reservadas = arr[2] != null ? ((Number) arr[2]).intValue() : 0;
        Integer vendidas = arr[3] != null ? ((Number) arr[3]).intValue() : 0;
        
        DtoEntradas dto = new DtoEntradas(total, libres, reservadas, vendidas);
        return dto;
    }
}
