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
@Table(name = "evento_auditoria")
public class EventoAuditoria implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_correlacion", nullable = false)
    private String idCorrelacion;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(name = "modulo_origen")
    private String moduloOrigen;

    @Column(name = "detalle", length = 1000)
    private String detalle;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp_evento", nullable = false)
    private Date timestampEvento;

    @Column(name = "estado_resultado")
    private String estadoResultado;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "entidad_afectada", length = 60)
    private String entidadAfectada;

    @Column(name = "id_entidad")
    private Long idEntidad;

    public EventoAuditoria() {}

    public Long getId() {
    	return id;
    }

    public void setId(Long id) {
    	this.id = id;
    }

    public String getIdCorrelacion() {
    	return idCorrelacion;
    }

    public void setIdCorrelacion(String idCorrelacion) {
    	this.idCorrelacion = idCorrelacion;
    }

    public Long getIdUsuario() {
    	return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
    	this.idUsuario = idUsuario;
    }

    public String getTipoEvento() {
    	return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
    	this.tipoEvento = tipoEvento;
    }

    public String getModuloOrigen() {
    	return moduloOrigen;
    }

    public void setModuloOrigen(String moduloOrigen) {
    	this.moduloOrigen = moduloOrigen;
    }

    public String getDetalle() {
    	return detalle;
    }

    public void setDetalle(String detalle) {
    	this.detalle = detalle;
    }

    public Date getTimestampEvento() {
    	return timestampEvento;
    }

    public void setTimestampEvento(Date timestampEvento) {
    	this.timestampEvento = timestampEvento;
    }

    public String getEstadoResultado() {
    	return estadoResultado;
    }

    public void setEstadoResultado(String estadoResultado) {
    	this.estadoResultado = estadoResultado;
    }

    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }

    public String getEntidadAfectada() { return entidadAfectada; }
    public void setEntidadAfectada(String entidadAfectada) { this.entidadAfectada = entidadAfectada; }

    public Long getIdEntidad() { return idEntidad; }
    public void setIdEntidad(Long idEntidad) { this.idEntidad = idEntidad; }
}