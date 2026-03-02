package edu.esi.ds.esiusuarios.http;

import org.springframework.web.bind.annotation.RestController;

import edu.esi.ds.esiusuarios.services.UserService;

@RestController
public class ExternalController {

    @Autowired
    private UserService service;

    @GetMapping("/checkToken/{token}")
    public String checkToken(@PathVariable String token) {
        // Example logic to check token validity
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason: "Se necesita el token");
        }
        return "Token is invalid";
    }
}