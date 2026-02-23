package co.edu.unbosque.ElecSys.CuentaPorPagar.ControladorCuen;

import co.edu.unbosque.ElecSys.CuentaPorPagar.Archivo.PDF_Archivo_Cuenta;
import co.edu.unbosque.ElecSys.CuentaPorPagar.DTOCuen.CuentaPorPagarDTO;
import co.edu.unbosque.ElecSys.CuentaPorPagar.DTOCuen.CuentaPorPagarRequest;
import co.edu.unbosque.ElecSys.CuentaPorPagar.Detalle_Cuenta.DetalleDTO.Detalle_CuentaDTO;
import co.edu.unbosque.ElecSys.CuentaPorPagar.Detalle_Cuenta.ServicioDetalleCuenta.DetalleCuentaService;
import co.edu.unbosque.ElecSys.CuentaPorPagar.EntidadCuen.CuentaPorPagarEntidad;
import co.edu.unbosque.ElecSys.CuentaPorPagar.ServicioCuen.CuentaPorPagarImplService;
import co.edu.unbosque.ElecSys.Usuario.Cliente.DTOClie.ClienteDTO;
import co.edu.unbosque.ElecSys.Usuario.Cliente.ServicioClie.ClienteServiceImpl;
import co.edu.unbosque.ElecSys.Usuario.Trabajador.ServicioTra.TrabajadorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import co.edu.unbosque.ElecSys.Config.Excepcion.*;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/cuentas_pagar")
public class CuentaPorPagarControlador {

    @Autowired
    private CuentaPorPagarImplService cuentaPorPagarService;

    @Autowired
    private DetalleCuentaService detalleCuentaService;

    @Autowired
    private ClienteServiceImpl clienteService;

    @Autowired
    private TrabajadorServiceImpl trabajadorService;

    @Autowired
    private PDF_Archivo_Cuenta pdfService;

    private static final List<String> ESTADOS_VALIDOS =
            List.of("PAGADO", "PENDIENTE", "EN_PROCESO");

    // ----------------------------------------------------
    // AGREGAR
    // ----------------------------------------------------
    @PostMapping("/agregar")
    public ResponseEntity<byte[]> agregarCuenta(@RequestBody CuentaPorPagarRequest solitud) throws IOException {

        if (solitud.getCuentaPorPagarDTO() == null ||  solitud.getDetalleCuentaDTOS() == null
                || solitud.getDetalleCuentaDTOS().isEmpty()){
            throw new InvalidFieldException("La solicitud no contiene datos de cuenta de cobro");
        }
        CuentaPorPagarDTO cuenta = solitud.getCuentaPorPagarDTO();

        // Validar campos obligatorios
        if (cuenta.getId_trabajador() <= 0) {
            throw new InvalidFieldException("El id_trabajador es obligatorio y relacionarse con la base de datos");
        }

        ClienteDTO cliente = clienteService.buscarCliente(cuenta.getId_cliente());
        if (cliente == null){
            throw new ResourceNotFoundException("El cliente no existe");
        }

        if ("Deshabilitado".equals(cliente.getEstado())){
            throw new InvalidFieldException("El cliente esta deshabilitado no puede asociarse a ninguna cuenta de cobro");
        }

        if (cuenta.getMonto() == null || cuenta.getMonto().doubleValue() <= 0) {
            throw new InvalidFieldException("El monto debe ser mayor que 0.");
        }

        List<Detalle_CuentaDTO> detalles = solitud.getDetalleCuentaDTOS();
        if (detalles == null || detalles.isEmpty()){
            throw new InvalidFieldException("Debe enviar al menos un detalle");
        }

        // Validar estado
        if (cuenta.getEstado() == null || !ESTADOS_VALIDOS.contains(cuenta.getEstado())) {
            throw new InvalidFieldException("Estado inválido. Debe ser: " + ESTADOS_VALIDOS);
        }

        //Guardar Informacion en la base de datos
        CuentaPorPagarDTO cuentaActual = new CuentaPorPagarDTO(
                0,cuenta.getId_trabajador(), cuenta.getId_cliente(), cuenta.getNota(),
                cuenta.getFecha_realizacion(), cuenta.getMonto(), cuenta.getEstado());

        CuentaPorPagarDTO cuentaAguardar = cuentaPorPagarService.agregarCuentaPagar(cuentaActual);

        for (Detalle_CuentaDTO detalleCuentaDTO: detalles){
            detalleCuentaDTO.setId_cuenta_pagar(cuentaAguardar.getId_cuenta_pagar());
            detalleCuentaService.agregarDetalleCuenta(detalleCuentaDTO);
        }

        byte[] pdf;
        try{
            pdf = pdfService.generarArchivoCuenta(cuentaAguardar, cliente, detalles);
        } catch (Exception e) {
            throw new PdfGenerationException("Error al generar el PDF: " + e.getMessage());
        }

        String nombreArchivo = pdfService.descargarCuentaPDF(cuentaAguardar, solitud.getReferencia(), pdf);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+nombreArchivo)
                .contentType(MediaType.APPLICATION_PDF).body(pdf);

    }

    // ----------------------------------------------------
    // LISTAR
    // ----------------------------------------------------
    @GetMapping("/listar")
    public ResponseEntity<List<CuentaPorPagarDTO>> listarCuentas() {
        List<CuentaPorPagarDTO> lista = cuentaPorPagarService.listarCuentasPagar();

        if (lista.isEmpty()) {
            throw new ResourceNotFoundException("No hay cuentas por pagar registradas.");
        }

        return ResponseEntity.ok(lista);
    }

    // ----------------------------------------------------
    // BORRAR CUENTA DE COBRO + DETALLES
    // ----------------------------------------------------
    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<String> borrarCuenta(@PathVariable int id) {

        if (!cuentaPorPagarService.existeCuenta(id)) {
            throw new ResourceNotFoundException("No existe la cuenta por pagar con ID: " + id);
        }

        detalleCuentaService.listarDetallesCuentas()
                .stream()
                .filter((d -> d.getId_cuenta_pagar() == id))
                .forEach(d -> detalleCuentaService.borrarDetalleCuenta(d.getId_detalle_cuenta()));

        String mensaje = cuentaPorPagarService.borrarCuentaPagar(id);
        return ResponseEntity.ok(mensaje);
    }

    // ----------------------------------------------------
    // ACTUALIZAR
    // ----------------------------------------------------
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizarCuenta(
            @PathVariable int id,
            @RequestBody CuentaPorPagarDTO dto) {

        System.out.println("CLIENTE RECIBIDO: " + dto.getId_cliente());

        if (!cuentaPorPagarService.existeCuenta(id)) {
            throw new ResourceNotFoundException("No existe la cuenta por pagar con ID: " + id);
        }

        // Validaciones
        if (dto.getId_cuenta_pagar() != id) {
            throw new InvalidFieldException("El id_cuenta_pagar no se puede modificar.");
        }

        if (dto.getMonto() == null || dto.getMonto().doubleValue() <= 0) {
            throw new InvalidFieldException("El monto debe ser mayor que 0.");
        }

        if (dto.getEstado() == null || !ESTADOS_VALIDOS.contains(dto.getEstado())) {
            throw new InvalidFieldException("Estado inválido. Debe ser: " + ESTADOS_VALIDOS);
        }

        String mensaje = cuentaPorPagarService.actualizarCuenta(id, dto);
        return ResponseEntity.ok(mensaje);
    }

    // ----------------------------------------------------
    // BUSCAR POR ID
    // ----------------------------------------------------
        @GetMapping("/buscar/{id}")
        public ResponseEntity<CuentaPorPagarDTO> buscarCuenta(@PathVariable int id) {

            CuentaPorPagarDTO dto = cuentaPorPagarService.buscarCuenta(id);

            if (dto == null) {
                throw new ResourceNotFoundException("No existe la cuenta por pagar con ID: " + id);
            }

            return ResponseEntity.ok(dto);
        }

    // ----------------------------------------------------
    // LISTAR DETALLES DE UNA CUENTA
    // ----------------------------------------------------
    @GetMapping("/{id}/detallesCuentas")
    public List<Detalle_CuentaDTO> listarDetallesCuentas(@PathVariable int id){
        if (!cuentaPorPagarService.existeCuenta(id)){
            throw new ResourceNotFoundException("No existe la cuenta de cobro con ID: "+ id );
        }

        return detalleCuentaService.listarDetallesCuentas().stream()
                .filter(detalle -> detalle.getId_cuenta_pagar() == id)
                .toList();
    }


    // ----------------------------------------------------
    // BORRAR DETALLE CUENTA
    // ----------------------------------------------------
    @DeleteMapping("/borrar/{idCuenta}/detalle/{idDetalle}")
    public String borrarDetalleCuenta(@PathVariable int idCuenta, @PathVariable int idDetalle){

        if (!cuentaPorPagarService.existeCuenta(idCuenta)){
            throw  new ResourceNotFoundException("No existe la Cotizacion con ID: "+ idCuenta);
        }

        Detalle_CuentaDTO detalle = detalleCuentaService.listarDetallesCuentas().stream()
                .filter( d -> d.getId_detalle_cuenta() == idDetalle)
                .findFirst().orElseThrow( () -> new ResourceNotFoundException("El detalle no existe"));

        if (detalle.getId_cuenta_pagar() != idCuenta){
            throw  new InvalidFieldException("Ese detalle no pertenece a la Cuenta por pagar indicada");
        }

        detalleCuentaService.borrarDetalleCuenta(idDetalle);
        return "Detalle Cuenta eliminado Correctamente";
    }


    // ----------------------------------------------------
    // CREAR DETALLE EN CUENTA DE COBRO
    // ----------------------------------------------------
    @PostMapping("Crear/{idCuenta}/detalle")
    public ResponseEntity<String> agregarDetalle(@PathVariable int idCuenta, @RequestBody Detalle_CuentaDTO detalledto){

        if (!cuentaPorPagarService.existeCuenta(idCuenta)){
            throw  new ResourceNotFoundException("No existe la cuenta con ID: "+ idCuenta);
        }

        if (detalledto.getDescripcion() == null){
            throw  new InvalidFieldException("La descripcion no puede estar vacia");
        }

        detalledto.setId_cuenta_pagar(idCuenta);

        detalleCuentaService.agregarDetalleCuenta(detalledto);

        return ResponseEntity.ok("Detalle agregado correctamente");
    }

    // --------------------------------------------------
    // ACTUALIZAR DETALLE
    // --------------------------------------------------
    @PutMapping("actualizar/{idCuenta}/detalle-cuenta/{idDetalle}")
    public String actualizarDetalleCuenta(@PathVariable int idCuenta, @PathVariable int idDetalle, @RequestBody Detalle_CuentaDTO detalle){

        if (!cuentaPorPagarService.existeCuenta(idCuenta)){
            throw new ResourceNotFoundException("No existe la cuenta por pagar con ID: " + idCuenta);
        }

        Detalle_CuentaDTO actual = detalleCuentaService.listarDetallesCuentas().stream()
                .filter(d -> d.getId_detalle_cuenta() == idDetalle)
                .findFirst().orElseThrow( () -> new ResourceNotFoundException("No existe el detalle con ID: " + idDetalle));

        if (detalle.getDescripcion() == null || detalle.getDescripcion().isBlank()){
            throw  new InvalidFieldException("El Detalle no puede estar en vacio");
        }

        String mensaje = detalleCuentaService.actualizarDetalleCuenta(idDetalle, detalle);
        return mensaje;
    }

}
