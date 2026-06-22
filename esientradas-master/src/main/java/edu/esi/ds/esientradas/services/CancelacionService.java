package edu.esi.ds.esientradas.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Pago;
import jakarta.transaction.Transactional;

@Service
public class CancelacionService {

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private UsuarioService usuarioService;

    @Transactional
    public String cancelarEntrada(String tokenCancelacion) {
        Optional<Entrada> opt = entradaDao.findByTokenCancelacion(tokenCancelacion);

        if (opt.isEmpty()) {
            return "El enlace no es válido o la entrada ya ha sido cancelada.";
        }

        Entrada entrada = opt.get();
        Pago pago = entrada.getPago();

        if (pago == null || pago.getFechaCompra() == null) {
            return "No se encontró la información de compra de esta entrada.";
        }

        // 1. REGLA DE NEGOCIO: Comprobar si han pasado menos de 15 minutos
        LocalDateTime limite = pago.getFechaCompra().plusMinutes(15);
        if (LocalDateTime.now().isAfter(limite)) {
            return "El plazo de 15 minutos para cancelar ha expirado.";
        }

        // 2. DEVOLVER EL DINERO AL MONEDERO REUTILIZANDO EL USUARIOSERVICE
        double cantidadEuros = entrada.getPrecio() / 100.0; 
        String emailUsuario = pago.getUsuarioEmail();

        // Sumamos el saldo al monedero del usuario
        boolean reembolsoOk = usuarioService.sumarSaldo(emailUsuario, cantidadEuros);

        if (!reembolsoOk) {
            return "Hubo un problema al contactar con el sistema de monederos. Inténtalo de nuevo.";
        }

        // 3. ACTUALIZAR BASE DE DATOS: Liberar la butaca
        entrada.setEstado(Estado.DISPONIBLE);
        entrada.setTokenCancelacion(null); 
        entrada.setPago(null); 
        entradaDao.save(entrada);

        return "OK";
    }
}