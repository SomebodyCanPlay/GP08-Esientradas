package edu.esi.ds.esientradas.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import edu.esi.ds.esientradas.model.Configuracion;
import edu.esi.ds.esientradas.model.DeZona;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.model.Precisa;

@Service
public class PDFService {

    public byte[] generarPdf(Entrada entrada, Configuracion config) throws IOException {
        String empName = (config != null && config.getNombre() != null) ? config.getNombre() : "ESIentradas";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // ==========================================
        // 1. CABECERA DEL DOCUMENTO
        // ==========================================
        document.add(new Paragraph(empName)
                .setBold().setFontSize(26).setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(0));
        
        document.add(new Paragraph("ENTRADA OFICIAL")
                .setBold().setFontSize(12).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(30));

        Espectaculo espectaculo = entrada.getEspectaculo();

        // ==========================================
        // 2. DETALLES DEL ESPECTÁCULO
        // ==========================================
        Div showInfo = new Div().setMarginBottom(30).setPaddingLeft(20);
        if (espectaculo != null) {
            showInfo.add(new Paragraph(espectaculo.getArtista())
                    .setBold().setFontSize(20).setMarginBottom(10).setFontColor(ColorConstants.BLACK));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm'h'");
            if (espectaculo.getFecha() != null) {
                showInfo.add(new Paragraph("📅 Fecha: " + espectaculo.getFecha().format(formatter))
                        .setFontSize(12).setMarginBottom(2));
            }
            if (espectaculo.getEscenario() != null) {
                showInfo.add(new Paragraph("📍 Lugar: " + espectaculo.getEscenario().getNombre())
                        .setFontSize(12).setMarginBottom(2));
            }
        }
        document.add(showInfo);

        // ==========================================
        // 3. DETALLES DE LA ENTRADA (En forma de tabla)
        // ==========================================
        document.add(new Paragraph("DETALLES DE TU COMPRA")
                .setBold().setFontSize(12).setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setPadding(5).setMarginBottom(10).setTextAlignment(TextAlignment.CENTER));

        // Creamos una tabla con 2 columnas (30% para la etiqueta, 70% para el valor)
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
        table.setMarginBottom(20).setPaddingLeft(20);

        // Usamos un método auxiliar para añadir las filas limpiamente
        addDetailRow(table, "Ticket ID:", String.valueOf(entrada.getId()), true);

        if (entrada instanceof Precisa p) {
            addDetailRow(table, "Tipo de entrada:", "Butaca Asignada", false);
            addDetailRow(table, "Ubicación:", "Planta: " + p.getPlanta() + " | Fila: " + p.getFila() + " | Asiento: " + p.getColumna(), true);
        } else if (entrada instanceof DeZona z) {
            addDetailRow(table, "Tipo de entrada:", "Acceso General", false);
        }

        double precioEnEuros = entrada.getPrecio() / 100.0;
        addDetailRow(table, "Precio Total:", String.format("%.2f €", precioEnEuros), true);

        document.add(table);

        // ==========================================
        // 4. PIE DE PÁGINA
        // ==========================================
        document.add(new Paragraph("---------------------------------------------------------")
                .setFontColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER).setMarginTop(30));
        document.add(new Paragraph("Por favor, lleva esta entrada impresa o descargada en tu móvil el día del evento.")
                .setItalic().setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        document.close();
        return baos.toByteArray();
    }

    // Método auxiliar para construir las filas de la tabla sin bordes visibles
    // Método auxiliar para construir las filas de la tabla sin bordes visibles
    private void addDetailRow(Table table, String label, String value, boolean highlightValue) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFontColor(ColorConstants.DARK_GRAY))
                .setBorder(Border.NO_BORDER).setPadding(4);
        
        // --- CORRECCIÓN AQUÍ ---
        Paragraph valueParagraph = new Paragraph(value);
        if (highlightValue) {
            valueParagraph.setBold(); // Solo llamamos a setBold() si es true, sin pasarle el boolean
        }
        
        Cell valueCell = new Cell().add(valueParagraph)
                .setBorder(Border.NO_BORDER).setPadding(4);
        // -----------------------
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}