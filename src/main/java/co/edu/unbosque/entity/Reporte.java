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
@Table(name = "reporte")
public class Reporte implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tipo_reporte", nullable = false)
    private String tipoReporte;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "parametros")
    private String parametros;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_generacion")
    private Date fechaGeneracion;

    @Column(name = "id_usuario_genera")
    private Long idUsuarioGenera;

    @Column(name = "estado")
    private String estado;

    public Reporte() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getParametros() { return parametros; }
    public void setParametros(String parametros) { this.parametros = parametros; }
    public Date getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(Date fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public Long getIdUsuarioGenera() { return idUsuarioGenera; }
    public void setIdUsuarioGenera(Long idUsuarioGenera) { this.idUsuarioGenera = idUsuarioGenera; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}