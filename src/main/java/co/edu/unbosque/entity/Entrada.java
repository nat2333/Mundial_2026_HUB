package co.edu.unbosque.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "entrada")
public class Entrada implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_partido", nullable = false)
    private Long idPartido;

    @Column(name = "id_titular")
    private Long idTitular;

    @Column(name = "estado")
    private String estado;

    @Column(name = "precio")
    private Double precio;

    @Column(name = "tribuna")
    private String tribuna;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_reserva")
    private Date fechaReserva;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_expiracion_reserva")
    private Date fechaExpiracionReserva;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_pago")
    private Date fechaPago;

    @Column(name = "id_correlacion")
    private String idCorrelacion;

    @Column(name = "id_transaccion_pago")
    private String idTransaccionPago;

    public Entrada() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdPartido() { return idPartido; }
    public void setIdPartido(Long idPartido) { this.idPartido = idPartido; }
    public Long getIdTitular() { return idTitular; }
    public void setIdTitular(Long idTitular) { this.idTitular = idTitular; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public String getTribuna() { return tribuna; }
    public void setTribuna(String tribuna) { this.tribuna = tribuna; }
    public Date getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(Date fechaReserva) { this.fechaReserva = fechaReserva; }
    public Date getFechaExpiracionReserva() { return fechaExpiracionReserva; }
    public void setFechaExpiracionReserva(Date fechaExpiracionReserva) { this.fechaExpiracionReserva = fechaExpiracionReserva; }
    public Date getFechaPago() { return fechaPago; }
    public void setFechaPago(Date fechaPago) { this.fechaPago = fechaPago; }
    public String getIdCorrelacion() { return idCorrelacion; }
    public void setIdCorrelacion(String idCorrelacion) { this.idCorrelacion = idCorrelacion; }
    public String getIdTransaccionPago() { return idTransaccionPago; }
    public void setIdTransaccionPago(String idTransaccionPago) { this.idTransaccionPago = idTransaccionPago; }
}