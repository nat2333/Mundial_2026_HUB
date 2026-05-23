package co.edu.unbosque.service.api;

import java.util.Optional;

import co.edu.unbosque.dto.FacturaDTO;
import co.edu.unbosque.entity.FacturaEntrada;
import co.edu.unbosque.utils.GenericServiceAPI;

public interface FacturaEntradaServiceAPI extends GenericServiceAPI<FacturaEntrada, Long> {

    FacturaEntrada generarFactura(Long idEntrada);

    Optional<FacturaEntrada> obtenerFacturaPorEntrada(Long idEntrada);

    FacturaDTO construirDTO(FacturaEntrada factura);

    byte[] generarPdfBytes(Long idFactura);

    void enviarFacturaCorreo(Long idFactura);
}
