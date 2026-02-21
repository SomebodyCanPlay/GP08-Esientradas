package edu.esi.ds.esientradas.http;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import edu.esi.ds.esientradas.model.Escenario;

@RestController
@RequestMapping("/escenarios")
public class EscenarioController {

    @PostMapping("/insertar")
    public void insertar(@RequestBody Escenario escenario){
        System.out.println(escenario.getNombre());
        System.out.println(escenario.getDescripcion());
    }
}
