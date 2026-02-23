package co.edu.unbosque.ElecSys.CuentaPorPagar.ServicioCuen;

import co.edu.unbosque.ElecSys.Cotizacion.DTOCot.CotizacionDTO;
import co.edu.unbosque.ElecSys.CuentaPorPagar.DTOCuen.CuentaPorPagarDTO;

import java.util.List;

public interface CuentaPorPagarInterface {
    public CuentaPorPagarDTO agregarCuentaPagar(CuentaPorPagarDTO cuenta);
    //public CotizacionDTO buscarCotizacion(int id);
    public String borrarCuentaPagar(int id);
    public List<CuentaPorPagarDTO> listarCuentasPagar();
    public String actualizarCuenta(int id, CuentaPorPagarDTO cuentaPorPagarDTO);
    public Boolean existeCuenta(int id);
    public CuentaPorPagarDTO buscarCuenta(int id);
}
