package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.esi.ds.esientradas.dao.PdfDao;
import edu.esi.ds.esientradas.model.Configuracion;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.PDFEntidad;

@Service
public class PDFService {

    @Autowired
    private PdfDao pdfDao;

    @Autowired
    private EmailService emailService;

    public void generarYEnviar(Entrada entrada, Configuracion config) {
        String empName = (config != null && config.getNombre() != null) ? config.getNombre() : "Empresa Desconocida";
        String empUrl = (config != null && config.getUrl() != null) ? config.getUrl() : "Sin URL";
        String empVend = (config != null && config.getVendedores() != null) ? config.getVendedores() : "Vendedor Predeterminado";

        System.out.println("[PDFService] Generando recibo PDF de entrada ID: " + entrada.getId());
        System.out.println("[PDFService] Imprimiendo metadatos de empresa: Nombre(" + empName + "), URL(" + empUrl + "), Vendedores(" + empVend + ")");

        PDFEntidad pdf = new PDFEntidad();
        pdf.setEntradaId(entrada.getId());
        pdf.setUrlPDF("/storage/pdfs/recibo_" + entrada.getId() + ".pdf");
        
        pdfDao.save(pdf);

        System.out.println("[PDFService] PDF guardado en BD. Delegando el envío a EmailService.");
        
        emailService.enviarConfirmacion(entrada);
    }
}
