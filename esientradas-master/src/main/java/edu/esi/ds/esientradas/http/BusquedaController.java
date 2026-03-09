package edu.esi.ds.esientradas.http;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.esi.ds.esientradas.services.BusquedaService;
import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dto.DtoEspectaculo;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/busqueda")
@CrossOrigin(origins = "*")
public class BusquedaController {

    @Autowired
    private BusquedaService service;

    @Autowired
    private EntradaDao entradaDao;

    @GetMapping("/getEntradas")
    public List<Entrada> getEntradas(@RequestParam Long espectaculoid){
        return this.entradaDao.findByEspectaculoId(espectaculoid);
    }

    @GetMapping("/getEscenarios")
    public List<Escenario> getEscenarios(){
        return this.service.getEscenarios(); 
    }
    
    @GetMapping("/getEspectaculos")
    public DtoEntradas getNumeroDeEntradasComoDto() {
        return this.service.getEspectaculos();
    }

    @GetMapping("/getNumeroEntradas")
    public Integer getNumeroEntradas(@RequestParam Long espectaculoId) {
        return this.service.getNumeroEntradas(espectaculoId);
    }

    @GetMapping("/saludar/{nombre}")
    public String saludar(@PathVariable String nombre, @RequestParam String apellido){
        return "Hola " + nombre + " " + apellido + ", esta es la búsqueda de entradas.";
    }

}

