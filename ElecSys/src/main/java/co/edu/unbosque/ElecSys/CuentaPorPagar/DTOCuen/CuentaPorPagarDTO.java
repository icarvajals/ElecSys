package co.edu.unbosque.ElecSys.CuentaPorPagar.DTOCuen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaPorPagarDTO {

    private int id_cuenta_pagar;
    private int id_trabajador;
    private int id_cliente;
    private String nota;
    private LocalDate fecha_realizacion;
    private BigDecimal monto;
    private String estado;
}
