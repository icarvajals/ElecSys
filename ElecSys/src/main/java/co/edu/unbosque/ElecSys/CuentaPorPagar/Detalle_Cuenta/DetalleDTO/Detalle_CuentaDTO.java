package co.edu.unbosque.ElecSys.CuentaPorPagar.Detalle_Cuenta.DetalleDTO;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Detalle_CuentaDTO {

    private int id_detalle_cuenta;
    private int id_cuenta_pagar;
    private String descripcion;
    private BigDecimal valor;

}
