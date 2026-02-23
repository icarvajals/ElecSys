package co.edu.unbosque.ElecSys.CuentaPorPagar.Detalle_Cuenta.ServicioDetalleCuenta;

import co.edu.unbosque.ElecSys.CuentaPorPagar.Detalle_Cuenta.EntidadDetalleCuenta.DetalleCuentaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleCuentaRepository extends JpaRepository<DetalleCuentaEntidad, Integer> {
}
