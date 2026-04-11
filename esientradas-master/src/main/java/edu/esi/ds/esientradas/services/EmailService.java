package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import edu.esi.ds.esientradas.model.Entrada;

@Service
public class EmailService {

	// Simula el envío de correo mostrando detalles de la compra por consola
	public void enviarConfirmacion(Entrada entrada) {
		if (entrada == null) return;
		// Se asumen getters típicos en Entrada; adapta si tu modelo usa otros nombres.
		System.out.println("[EmailService] Simulando envío de correo al cliente con el recibo adjunto...");
		System.out.println(" - Entrada ID: " + entrada.getId());
	}
}
