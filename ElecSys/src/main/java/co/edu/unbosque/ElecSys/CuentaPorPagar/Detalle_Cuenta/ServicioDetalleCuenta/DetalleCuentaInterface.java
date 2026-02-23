package co.edu.unbosque.ElecSys.CuentaPorPagar.Detalle_Cuenta.ServicioDetalleCuenta;


import co.edu.unbosque.ElecSys.CuentaPorPagar.Detalle_Cuenta.DetalleDTO.Detalle_CuentaDTO;

import java.util.List;

public interface DetalleCuentaInterface {

    public Detalle_CuentaDTO agregarDetalleCuenta(Detalle_CuentaDTO detalle);
    public String borrarDetalleCuenta(int id);
    public List<Detalle_CuentaDTO> listarDetallesCuentas();
    public String actualizarDetalleCuenta(int id, Detalle_CuentaDTO detalle);
    public Detalle_CuentaDTO buscarDetallesCuentas(int id);
}
