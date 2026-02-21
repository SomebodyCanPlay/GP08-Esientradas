package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.dao.*;

@Service
public class BusquedaService {
    
    @Autowired
    private EscenarioDao dao;


    public List<Escenario> getEscenarios(){
        return this.dao.findAll();
    }
}
