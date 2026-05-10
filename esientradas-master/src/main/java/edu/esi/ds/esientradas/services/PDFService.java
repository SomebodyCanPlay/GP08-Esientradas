package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.esi.ds.esientradas.dao.PdfDao;
import edu.esi.ds.esientradas.model.Configuracion;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.PDFEntidad;

// Servicio de PDF (simulado): genera y registra la URL del recibo.
@Service
public class PDFService {

    // DAO para guardar el registro del PDF en la tabla "pdf"
    @Autowired
    private PdfDao pdfDao;

    // Genera (simula) el PDF del recibo y lo registra en BD.
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
