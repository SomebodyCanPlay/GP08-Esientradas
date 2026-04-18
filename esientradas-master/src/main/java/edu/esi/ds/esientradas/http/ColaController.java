package edu.esi.ds.esientradas.http;

import edu.esi.ds.esientradas.services.ColaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/cola")
public class ColaController {

	private final ColaService colaService;

	public ColaController(ColaService colaService) {
		this.colaService = colaService;
	}

	/**
	 * GET /cola/check?sessionId=...
	 * Devuelve JSON { "canPass": true } si el usuario puede pasar, o { "canPass": false } si debe esperar.
	 * Se delega la lógica de conteo/cola a ColaService.canPass(sessionId).
	 */
	@GetMapping("/check")
	public ResponseEntity<Map<String, Boolean>> check(@RequestParam String sessionId) {
		boolean canPass = colaService.canPass(sessionId);
		return ResponseEntity.ok(Collections.singletonMap("canPass", canPass));
	}
}