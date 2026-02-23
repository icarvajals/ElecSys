package co.edu.unbosque.ElecSys.Cotizacion.Archivo;

import co.edu.unbosque.ElecSys.Cotizacion.DTOCot.CotizacionDTO;
import co.edu.unbosque.ElecSys.Cotizacion.DetalleCotizacion.DTODetCot.DetalleCotizacionDTO;
import co.edu.unbosque.ElecSys.LugarTrabajo.DTOLug.LugarTrabajoDTO;
import co.edu.unbosque.ElecSys.Usuario.Cliente.DTOClie.ClienteDTO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ContenidoArchivo {

    public void encabezadoArchivo(Document documento, CotizacionDTO cotizacionDTO) throws DocumentException {
        Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font fontNormal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        // Crear una tabla con dos columnas
        PdfPTable encabezado = new PdfPTable(2);
        encabezado.setWidthPercentage(100); // ancho total
        encabezado.setWidths(new float[]{70, 30}); // proporciones

        PdfPCell celdaEmpresa = new PdfPCell(new Phrase("VC ELECTRICOS CONSTRUCCIONES S.A.S.", fontTitulo));
        celdaEmpresa.setBorder(Rectangle.NO_BORDER);
        celdaEmpresa.setHorizontalAlignment(Element.ALIGN_LEFT);
        encabezado.addCell(celdaEmpresa);

        // Celda derecha: Número de cotización
        PdfPCell celdaNumero = new PdfPCell(new Phrase("Cotización No. " + cotizacionDTO.getId_cotizacion(), fontNormal));
        celdaNumero.setBorder(Rectangle.NO_BORDER);
        celdaNumero.setHorizontalAlignment(Element.ALIGN_RIGHT);
        encabezado.addCell(celdaNumero);

        // Agregar la tabla al documento
        documento.add(encabezado);

        // Línea divisoria
        LineSeparator linea = new LineSeparator();
        linea.setOffset(-2);
        documento.add(new Chunk(linea));
    }

    public void dirigidoCotizacion(Document documento,
                                   CotizacionDTO cotizacionDTO,
                                   ClienteDTO clienteDTO,
                                   LugarTrabajoDTO lugar) throws DocumentException, IOException {

        Font fontNormal = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font fontBold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);

        // 📅 Fecha
        Paragraph fecha = new Paragraph(
                "Bogotá D.C., " + cotizacionDTO.getFecha_realizacion(),
                fontNormal
        );
        fecha.setSpacingBefore(7);
        fecha.setSpacingAfter(10);
        documento.add(fecha);

        PdfPTable tablaHeader = new PdfPTable(2);
        tablaHeader.setWidthPercentage(100);
        tablaHeader.setWidths(new float[]{70, 30});

        // ===== COLUMNA IZQUIERDA (cliente)
        Paragraph clienteInfo = new Paragraph("Señores:\n", fontNormal);
        clienteInfo.add(new Phrase(clienteDTO.getNombre().toUpperCase() + "\n", fontBold));
        clienteInfo.add(new Phrase(lugar.getNombreLugar().toUpperCase(), fontBold));

        PdfPCell cellCliente = new PdfPCell(clienteInfo);
        cellCliente.setBorder(Rectangle.NO_BORDER);
        cellCliente.setPaddingLeft(40f);

        // ===== COLUMNA DERECHA (logo)
        Image logo = Image.getInstance("src/main/resources/static/LogoEmpresa.jpeg"); // 🔥 AJUSTA ESTA RUTA
        logo.scaleToFit(120, 90);
        logo.setAlignment(Image.ALIGN_RIGHT);

        PdfPCell cellLogo = new PdfPCell(logo);
        cellLogo.setBorder(Rectangle.NO_BORDER);
        cellLogo.setHorizontalAlignment(Element.ALIGN_RIGHT);

        // agregar a tabla
        tablaHeader.addCell(cellCliente);
        tablaHeader.addCell(cellLogo);

        tablaHeader.setSpacingAfter(15);
        documento.add(tablaHeader);

        // ===== Ciudad
        Paragraph ciudad = new Paragraph("Ciudad", fontNormal);
        ciudad.setSpacingAfter(5);
        documento.add(ciudad);

        // ===== Referencia centrada
        Paragraph ref = new Paragraph("Ref. " + cotizacionDTO.getReferencia(), fontBold);
        ref.setAlignment(Element.ALIGN_CENTER);
        ref.setSpacingAfter(11);
        documento.add(ref);

        // ===== Saludo
        Paragraph saludo = new Paragraph(
                "Cordial saludo.\nA continuación, someto a su consideración la siguiente cotización:",
                fontNormal
        );
        saludo.setSpacingBefore(10);
        documento.add(saludo);
    }

    public void tablaCotizacion(Document documento, java.util.List<DetalleCotizacionDTO> detalles) throws DocumentException {
        // Definir fuentes
        Font fontHeader = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font fontBody = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font fontHeaderWhite = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);

        // Crear tabla con 5 columnas
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100); // ocupa todo el ancho de la página
        tabla.setWidths(new float[]{10, 45, 15, 15, 15}); // proporciones entre columnas
        tabla.setSpacingBefore(20f); // espacio antes de la tabla
        tabla.setSpacingAfter(10f);

// 🎨 COLOR DE ENCABEZADO PERSONALIZADO (gris)
        BaseColor headerColor = new BaseColor(164, 0, 0);

        String[] headers = {"Ítem", "Descripción", "Cantidad", "Valor Unitario", "Subtotal"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontHeaderWhite));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(headerColor);
            cell.setPadding(7);
            cell.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(cell);
        }

        int item = 1;
        for (DetalleCotizacionDTO detalle : detalles) {
            // Ítem
            PdfPCell itemCell = new PdfPCell(new Phrase(String.valueOf(item++), fontBody));
            itemCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tabla.addCell(itemCell);

            // Descripción (alineada a la izquierda)
            PdfPCell descCell = new PdfPCell(new Phrase(detalle.getDescripcion(), fontBody));
            descCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            descCell.setVerticalAlignment(Element.ALIGN_TOP); // 🔥 clave
            descCell.setPadding(6);
            descCell.setNoWrap(false); // 🔥 permite salto bonito
            tabla.addCell(descCell);

            // Cantidad
            PdfPCell cantCell = new PdfPCell(new Phrase(String.valueOf(detalle.getCantidad()), fontBody));
            cantCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cantCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tabla.addCell(cantCell);

            // Valor unitario
            PdfPCell valUniCell = new PdfPCell(new Phrase(formatoMoneda(detalle.getValor_unitario()), fontBody));
            valUniCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            valUniCell.setPadding(5);
            valUniCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tabla.addCell(valUniCell);

            // Subtotal
            PdfPCell subCell = new PdfPCell(new Phrase(formatoMoneda(detalle.getSubtotal()), fontBody));
            subCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subCell.setPadding(5);
            subCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tabla.addCell(subCell);
        }

        documento.add(tabla);
    }

    public void tablaTotales(Document documento, CotizacionDTO cot) throws DocumentException {

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(40); //
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setSpacingBefore(10f);
        tabla.setWidths(new float[]{65f, 35f}); //  mejora visual

        Font fBold = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        Font fGray = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, new BaseColor(60,60,60));

        addRow(tabla, "SUBTOTAL", cot.getValor_total(), fBold, fGray, false);

        if(cot.getAdministracion()!=null)
            addRow(tabla, "ADMINISTRACIÓN ", cot.getAdministracion(), fBold, fGray, false);

        if(cot.getImprevistos()!=null)
            addRow(tabla, "IMPREVISTOS", cot.getImprevistos(), fBold, fGray, false);

        if(cot.getUtilidad()!=null)
            addRow(tabla, "UTILIDADES", cot.getUtilidad(), fBold, fGray, false);

        if(cot.getIva()!=null && cot.getIva().compareTo(BigDecimal.ZERO)>0)
            addRow(tabla, "IVA 19%", cot.getIva(), fBold, fGray, false);

        addRow(tabla, "TOTAL", cot.getTotal_pagar(), fBold, fGray, true);

        documento.add(tabla);
    }


    private void addRow(PdfPTable tabla, String label, BigDecimal value,
                        Font fBold, Font fGray, boolean esTotal) {

        BaseColor rojoCorporativo = new BaseColor(164, 0, 0);
        BaseColor grisBorde = new BaseColor(220,220,220);

        PdfPCell c1 = new PdfPCell(new Phrase(label, esTotal ?
                new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE) : fBold));

        PdfPCell c2 = new PdfPCell(new Phrase(formatoMoneda(value), esTotal ?
                new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE) : fBold));

        c1.setPadding(6);
        c2.setPadding(6);

        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        if(esTotal){
            c1.setBackgroundColor(rojoCorporativo);
            c2.setBackgroundColor(rojoCorporativo);
            c1.setBorder(Rectangle.NO_BORDER);
            c2.setBorder(Rectangle.NO_BORDER);
        } else {
            c1.setBorderColor(grisBorde);
            c2.setBorderColor(grisBorde);
        }

        tabla.addCell(c1);
        tabla.addCell(c2);
    }

    private String formatoMoneda(BigDecimal valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat formato = new DecimalFormat("#,###.00", symbols);
        return "$ " + formato.format(valor);
    }

    public void seccionNotas(Document documento) throws DocumentException {
        Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font fontTexto = new Font(Font.FontFamily.HELVETICA, 9);

        Paragraph titulo = new Paragraph("NOTA.", fontTitulo);
        titulo.setSpacingBefore(15);
        titulo.setSpacingAfter(5);
        documento.add(titulo);

        com.itextpdf.text.List lista = new com.itextpdf.text.List(com.itextpdf.text.List.ORDERED);
        lista.setSymbolIndent(12);

        lista.add(new ListItem("Las reformas se cobrarán como adicional dependiendo del ítem correspondiente.", fontTexto));
        lista.add(new ListItem("Todo el presupuesto está cotizado con las especificaciones de cada ítem.", fontTexto));
        lista.add(new ListItem("El material suministrado cuenta con certificación RETIE, RETLAP y CIDET.", fontTexto));

        ListItem formaPago = new ListItem("Forma de pago:", fontTexto);
        com.itextpdf.text.List sublista = new com.itextpdf.text.List(com.itextpdf.text.List.ORDERED);
        sublista.setSymbolIndent(15);
        sublista.add(new ListItem("50% de anticipo.", fontTexto));
        sublista.add(new ListItem("Restante a cortes de obra.", fontTexto));
        formaPago.add(sublista);

        lista.add(formaPago);
        lista.add(new ListItem("Vigencia de la cotización: 30 días.", fontTexto));

        documento.add(lista);
    }

    public void seccionFirma(Document documento) throws DocumentException, IOException {
        Font bold = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normal = new Font(Font.FontFamily.HELVETICA, 11);

        Paragraph cordial = new Paragraph("Cordialmente:", bold);
        cordial.setSpacingBefore(28);
        cordial.setSpacingAfter(20);
        documento.add(cordial);

        // Tabla contenedora de firma
        PdfPTable tablaFirma = new PdfPTable(1);
        tablaFirma.setWidthPercentage(40); // ancho del bloque
        tablaFirma.setHorizontalAlignment(Element.ALIGN_LEFT);

        // ===== IMAGEN =====
        URL resource = Thread.currentThread()
                .getContextClassLoader()
                .getResource("static/FirmaVictor.png");

        if (resource == null) {
            throw new RuntimeException("No se encontró la imagen");
        }

        Image firma = Image.getInstance(resource);
        firma.scaleToFit(120, 60);

        PdfPCell celdaImagen = new PdfPCell(firma);
        celdaImagen.setBorder(Rectangle.NO_BORDER);
        celdaImagen.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaFirma.addCell(celdaImagen);

        // ===== LÍNEA =====
        PdfPCell celdaLinea = new PdfPCell();
        celdaLinea.setBorder(Rectangle.TOP);
        celdaLinea.setFixedHeight(10);
        celdaLinea.setBorderWidthTop(1f);
        tablaFirma.addCell(celdaLinea);

        // ===== NOMBRE =====
        PdfPCell celdaNombre = new PdfPCell(
                new Phrase("Víctor Julio Carvajal Rincón", bold));
        celdaNombre.setBorder(Rectangle.NO_BORDER);
        celdaNombre.setHorizontalAlignment(Element.ALIGN_LEFT);
        tablaFirma.addCell(celdaNombre);

        // ===== CARGO =====
        PdfPCell celdaCargo = new PdfPCell(
                new Phrase("Representante legal", normal));
        celdaCargo.setBorder(Rectangle.NO_BORDER);
        celdaCargo.setHorizontalAlignment(Element.ALIGN_LEFT);
        tablaFirma.addCell(celdaCargo);

        documento.add(tablaFirma);
    }

    public void pieDePagina(Document documento) throws DocumentException {
        Font fontPie = new Font(Font.FontFamily.HELVETICA, 8);

        Paragraph pie = new Paragraph(
                "Dirección. Cll 143 # 149 B – 15\n"
                        + "Suba – Bilbao\n"
                        + "Cel. 311 868 14 05 – 535 73 38\n"
                        + "vcelectricos@hotmail.com\n\n",
                fontPie
        );
        pie.setAlignment(Element.ALIGN_CENTER);
        documento.add(pie);
    }

}
