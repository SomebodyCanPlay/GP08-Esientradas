package edu.esi.ds.esientradas.services;

import org.springframework.stereotype.Service;
import edu.esi.ds.esientradas.model.Entrada;

@Service
public class EmailService {

	// Simula el envío de correo mostrando detalles de la compra por consola
	public void enviarConfirmacion(Entrada entrada) {
		if (entrada == null) return;
		// Se asumen getters típicos en Entrada; adapta si tu modelo usa otros nombres.
		System.out.println("Enviando email de confirmación:");
		System.out.println(" - Entrada ID: " + entrada.getId());
		System.out.println(" - Espectáculo: " + entrada.getEspectaculo());
		System.out.println(" - Fecha: " + entrada.getFecha());
		System.out.println(" - Precio: " + entrada.getPrecio());
	}
}
