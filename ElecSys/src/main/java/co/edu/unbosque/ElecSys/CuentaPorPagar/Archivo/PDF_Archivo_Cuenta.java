package co.edu.unbosque.ElecSys.CuentaPorPagar.Archivo;

import co.edu.unbosque.ElecSys.CuentaPorPagar.DTOCuen.CuentaPorPagarDTO;
import co.edu.unbosque.ElecSys.CuentaPorPagar.Detalle_Cuenta.DetalleDTO.Detalle_CuentaDTO;
import co.edu.unbosque.ElecSys.Usuario.Cliente.DTOClie.ClienteDTO;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
public class PDF_Archivo_Cuenta {

    private ContenidoArchivo_Cuentas contenidoArchivoCuentas;

    public byte[] generarArchivoCuenta(CuentaPorPagarDTO cuenta, ClienteDTO cliente, List<Detalle_CuentaDTO> detallesCuentas) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 45, 36, 30, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            contenidoArchivoCuentas = new ContenidoArchivo_Cuentas();

            contenidoArchivoCuentas.encabezadoArchivo(doc, cuenta);

            contenidoArchivoCuentas.cuerpoDocumento(doc, cuenta, cliente);

            contenidoArchivoCuentas.listarDetalles(doc, detallesCuentas, cuenta);

            contenidoArchivoCuentas.firmaDocumento(doc);

            contenidoArchivoCuentas.pieDePagina(doc);

            doc.close();
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String descargarCuentaPDF(CuentaPorPagarDTO cuenta, String referencia, byte[] pdf) throws IOException{
        String nombreArchivo = cuenta.getId_cuenta_pagar() + ".Cuenta de Cobro " + referencia +".pdf";

        String carpetaDescargas = System.getProperty("user.home") + File.separator + "Downloads";

        Path carpetaCuentas = Paths.get(carpetaDescargas, "Cuentas de Cobro");

        int añoActual = LocalDate.now().getYear();
        Path carpetaAnual = carpetaCuentas.resolve("CT-" + añoActual);

        if (!Files.exists(carpetaCuentas)){
            Files.createDirectory(carpetaCuentas);
            System.out.println("Carpeta Creada" + carpetaCuentas);
        }

        if (!Files.exists((carpetaAnual))){
            Files.createDirectory(carpetaAnual);
            System.out.println("Subcarpeta anual creada" + carpetaAnual);
        }

        Path rutaArchivo = carpetaAnual.resolve(nombreArchivo);

        if (Files.exists(rutaArchivo)){
            System.out.println("Ya existe un archivo con el mismo nombre:" + rutaArchivo);
        }

        Files.write(rutaArchivo, pdf);
        System.out.println("PDF generado y guardado en: " + rutaArchivo.toAbsolutePath());

        return rutaArchivo.getFileName().toString();
    }
}
