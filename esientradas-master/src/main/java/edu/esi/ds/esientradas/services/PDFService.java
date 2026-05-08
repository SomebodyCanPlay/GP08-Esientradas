package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.esi.ds.esientradas.dao.PdfDao;
import edu.esi.ds.esientradas.model.Configuracion;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.PDFEntidad;

// ============================================================
// SERVICIO DE PDF — genera el recibo de compra (SIMULADO)
// ============================================================
// En producción real usaría iText o Apache PDFBox para crear un PDF real.
// En nuestra versión académica: guarda una URL ficticia en BD y llama
// a EmailService para simular el envío por consola.
//
// Flujo: PagosService → PDFService.generarYEnviar() → EmailService
// ============================================================
@Service
public class PDFService {

    // DAO para guardar el registro del PDF en la tabla "pdf"
    @Autowired
    private PdfDao pdfDao;

    // Genera (simula) el PDF del recibo y lo registra en BD
    // Parámetros:
    // - entrada → la entrada comprada (para saber ID y precio)
    // - config → datos de la empresa (nombre, URL) para el recibo
    public void generarYEnviar(Entrada entrada, Configuracion config) {

        // Si config es null (no hay configuración en BD), usamos valores por defecto
        String empName = (config != null && config.getNombre() != null) ? config.getNombre() : "Empresa Desconocida";
        String empUrl = (config != null && config.getUrl() != null) ? config.getUrl() : "Sin URL";
        String empVend = (config != null && config.getVendedores() != null) ? config.getVendedores()
                : "Vendedor Predeterminado";

        System.out.println("[PDFService] Generando recibo PDF para entrada ID: " + entrada.getId());
        System.out.println("[PDFService] Empresa: " + empName + " | URL: " + empUrl + " | Vendedores: " + empVend);

        // Registrar el PDF en la BD con una URL simulada
        PDFEntidad pdf = new PDFEntidad();
        pdf.setEntradaId(entrada.getId());
        pdf.setUrlPDF("/storage/pdfs/recibo_" + entrada.getId() + ".pdf");
        pdfDao.save(pdf);

        System.out.println("[PDFService] PDF registrado en BD. Delegando envío a EmailService.");

    }
}
