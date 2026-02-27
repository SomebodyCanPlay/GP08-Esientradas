package edu.esi.ds.esientradas.http;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.esi.ds.esientradas.services.BusquedaService;
import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dto.DtoEspectaculo;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;

import java.util.List;

@RestController
@RequestMapping("/busqueda")
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
    public List<DtoEspectaculo> getEspectaculos(@RequestParam String artista){
        List <Espectaculo> espectaculos = this.service.getEspectaculos(artista);
        List<DtoEspectaculo> espectaculosDto = espectaculos.stream().map(e -> {
            DtoEspectaculo dto = new DtoEspectaculo();
            dto.setId(e.getId());
            dto.setArtista(e.getArtista());
            dto.setFecha(e.getFecha());
            dto.setEscenario(e.getEscenario().getNombre());
            return dto;
        }).toList();
    return espectaculosDto;
    }

    
    @GetMapping("/saludar/{nombre}")
    public String saludar(@PathVariable String nombre, @RequestParam String apellido){
        return "Hola " + nombre + " " + apellido + ", esta es la búsqueda de entradas.";
    }
}

