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
@Table(name = "intercambio_lamina")
public class IntercambioLamina implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_usuario_oferta", nullable = false)
    private Long idUsuarioOferta;

    @Column(name = "id_usuario_receptor", nullable = false)
    private Long idUsuarioReceptor;

    @Column(name = "id_lamina_oferta", nullable = false)
    private Long idLaminaOferta;

    @Column(name = "id_lamina_receptor", nullable = false)
    private Long idLaminaReceptor;

    @Column(name = "estado")
    private String estado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_solicitud")
    private Date fechaSolicitud;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_resolucion")
    private Date fechaResolucion;

    @Column(name = "id_correlacion")
    private String idCorrelacion;

    public IntercambioLamina() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUsuarioOferta() { return idUsuarioOferta; }
    public void setIdUsuarioOferta(Long idUsuarioOferta) { this.idUsuarioOferta = idUsuarioOferta; }
    public Long getIdUsuarioReceptor() { return idUsuarioReceptor; }
    public void setIdUsuarioReceptor(Long idUsuarioReceptor) { this.idUsuarioReceptor = idUsuarioReceptor; }
    public Long getIdLaminaOferta() { return idLaminaOferta; }
    public void setIdLaminaOferta(Long idLaminaOferta) { this.idLaminaOferta = idLaminaOferta; }
    public Long getIdLaminaReceptor() { return idLaminaReceptor; }
    public void setIdLaminaReceptor(Long idLaminaReceptor) { this.idLaminaReceptor = idLaminaReceptor; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Date getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Date fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public Date getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Date fechaResolucion) { this.fechaResolucion = fechaResolucion; }
    public String getIdCorrelacion() { return idCorrelacion; }
    public void setIdCorrelacion(String idCorrelacion) { this.idCorrelacion = idCorrelacion; }
}