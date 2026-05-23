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
@Table(name = "caso_soporte")
public class CasoSoporte implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "id_agente")
    private Long idAgente;

    @Column(name = "tipo_caso")
    private String tipoCaso;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "estado")
    private String estado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_apertura")
    private Date fechaApertura;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_cierre")
    private Date fechaCierre;

    @Column(name = "id_correlacion")
    private String idCorrelacion;

    public CasoSoporte() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Long getIdAgente() { return idAgente; }
    public void setIdAgente(Long idAgente) { this.idAgente = idAgente; }
    public String getTipoCaso() { return tipoCaso; }
    public void setTipoCaso(String tipoCaso) { this.tipoCaso = tipoCaso; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Date getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(Date fechaApertura) { this.fechaApertura = fechaApertura; }
    public Date getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(Date fechaCierre) { this.fechaCierre = fechaCierre; }
    public String getIdCorrelacion() { return idCorrelacion; }
    public void setIdCorrelacion(String idCorrelacion) { this.idCorrelacion = idCorrelacion; }
}