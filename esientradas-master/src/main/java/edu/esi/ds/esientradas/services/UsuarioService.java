package edu.esi.ds.esientradas.controllers;

import edu.esi.ds.esientradas.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external")
public class ExternalController {

    private final UsuarioService usuarioService;

    public ExternalController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/checkToken")
    public ResponseEntity<String> checkToken(@RequestParam(required = false) String token) {
        try {
            String userName = usuarioService.checkToken(token);
            if (userName == null || userName.isEmpty()) {
                throw new ResponseStatusException.status(HttpStatus.UNAUTHORIZED, reason: "Token invalido");
            }
        } catch (RestClientException ex) {
            throw new ResponseStatusException.status(HttpStatus.UNAUTHORIZED, reason: "Token invalido");
        }
    }
}