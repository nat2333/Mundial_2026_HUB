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
@Table(name = "reembolso_entrada")
public class ReembolsoEntrada implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_entrada", nullable = false)
    private Long idEntrada;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "motivo")
    private String motivo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_solicitud")
    private Date fechaSolicitud;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_resolucion")
    private Date fechaResolucion;

    @Column(name = "estado")
    private String estado;

    @Column(name = "id_correlacion")
    private String idCorrelacion;

    @Column(name = "id_transaccion_reembolso")
    private String idTransaccionReembolso;

    public ReembolsoEntrada() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdEntrada() { return idEntrada; }
    public void setIdEntrada(Long idEntrada) { this.idEntrada = idEntrada; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Date getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Date fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public Date getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Date fechaResolucion) { this.fechaResolucion = fechaResolucion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getIdCorrelacion() { return idCorrelacion; }
    public void setIdCorrelacion(String idCorrelacion) { this.idCorrelacion = idCorrelacion; }
    public String getIdTransaccionReembolso() { return idTransaccionReembolso; }
    public void setIdTransaccionReembolso(String idTransaccionReembolso) { this.idTransaccionReembolso = idTransaccionReembolso; }
}