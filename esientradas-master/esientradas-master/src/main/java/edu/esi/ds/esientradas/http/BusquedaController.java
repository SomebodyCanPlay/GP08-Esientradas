package edu.esi.ds.esientradas.http;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.esi.ds.esientradas.services.BusquedaService;
import edu.esi.ds.esientradas.model.Escenario;

import java.util.List;

@RestController
@RequestMapping("/busqueda")
public class BusquedaController {

    @Autowired
    private BusquedaService service;

    @GetMapping("/getEscenarios")
    public List<Escenario> getEscenarios(){
        return this.service.getEscenarios(); 
    }

    @GetMapping("/saludar/{nombre}")
    public String saludar(@PathVariable String nombre, @RequestParam String apellido){
        return "Hola " + nombre + " " + apellido + ", esta es la búsqueda de entradas.";
    }
}
