package co.edu.unbosque.dto;

import java.math.BigDecimal;
import java.util.Date;

public class FacturaDTO {

    private Long id;
    private Long idEntrada;
    private String numeroFactura;
    private Date fechaEmision;
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal total;
    private Short enviadaCorreo;
    private Date fechaEnvio;

    // Datos del titular (enriquecidos)
    private String nombreTitular;
    private String correoTitular;

    // Datos del partido (enriquecidos)
    private String equipoLocal;
    private String equipoVisitante;
    private Date fechaPartido;
    private String estadio;
    private String ciudad;
    private String fase;

    // Datos de la entrada
    private String tribuna;
    private String estadoEntrada;
    private String idTransaccionPago;
    private String idCorrelacion;

    public FacturaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdEntrada() { return idEntrada; }
    public void setIdEntrada(Long idEntrada) { this.idEntrada = idEntrada; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public Date getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Date fechaEmision) { this.fechaEmision = fechaEmision; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getImpuestos() { return impuestos; }
    public void setImpuestos(BigDecimal impuestos) { this.impuestos = impuestos; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Short getEnviadaCorreo() { return enviadaCorreo; }
    public void setEnviadaCorreo(Short enviadaCorreo) { this.enviadaCorreo = enviadaCorreo; }

    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public String getNombreTitular() { return nombreTitular; }
    public void setNombreTitular(String nombreTitular) { this.nombreTitular = nombreTitular; }

    public String getCorreoTitular() { return correoTitular; }
    public void setCorreoTitular(String correoTitular) { this.correoTitular = correoTitular; }

    public String getEquipoLocal() { return equipoLocal; }
    public void setEquipoLocal(String equipoLocal) { this.equipoLocal = equipoLocal; }

    public String getEquipoVisitante() { return equipoVisitante; }
    public void setEquipoVisitante(String equipoVisitante) { this.equipoVisitante = equipoVisitante; }

    public Date getFechaPartido() { return fechaPartido; }
    public void setFechaPartido(Date fechaPartido) { this.fechaPartido = fechaPartido; }

    public String getEstadio() { return estadio; }
    public void setEstadio(String estadio) { this.estadio = estadio; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }

    public String getTribuna() { return tribuna; }
    public void setTribuna(String tribuna) { this.tribuna = tribuna; }

    public String getEstadoEntrada() { return estadoEntrada; }
    public void setEstadoEntrada(String estadoEntrada) { this.estadoEntrada = estadoEntrada; }

    public String getIdTransaccionPago() { return idTransaccionPago; }
    public void setIdTransaccionPago(String idTransaccionPago) { this.idTransaccionPago = idTransaccionPago; }

    public String getIdCorrelacion() { return idCorrelacion; }
    public void setIdCorrelacion(String idCorrelacion) { this.idCorrelacion = idCorrelacion; }
}
