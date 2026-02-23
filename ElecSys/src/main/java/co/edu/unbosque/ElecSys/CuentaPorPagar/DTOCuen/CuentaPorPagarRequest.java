package co.edu.unbosque.ElecSys.CuentaPorPagar.DTOCuen;

import co.edu.unbosque.ElecSys.CuentaPorPagar.Detalle_Cuenta.DetalleDTO.Detalle_CuentaDTO;
import co.edu.unbosque.ElecSys.Usuario.Cliente.DTOClie.ClienteDTO;
import co.edu.unbosque.ElecSys.Usuario.Cliente.EntidadClie.ClienteEntidad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CuentaPorPagarRequest {

    private CuentaPorPagarDTO cuentaPorPagarDTO;
    private String referencia;
    private List<Detalle_CuentaDTO> detalleCuentaDTOS;

}
