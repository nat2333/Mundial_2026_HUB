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
@Table(name = "pronostico")
public class Pronostico implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_polla", nullable = false)
    private Long idPolla;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_partido", nullable = false)
    private Long idPartido;

    @Column(name = "resultado_predicho")
    private String resultadoPredicho;

    @Column(name = "goles_local_predichos")
    private Integer golesLocalPredichos;

    @Column(name = "goles_visitante_predichos")
    private Integer golesVisitantePredichos;

    @Column(name = "puntaje_obtenido")
    private Integer puntajeObtenido;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_registro")
    private Date fechaRegistro;

    @Column(name = "cerrado")
    private byte cerrado;

    public Pronostico() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdPolla() { return idPolla; }
    public void setIdPolla(Long idPolla) { this.idPolla = idPolla; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Long getIdPartido() { return idPartido; }
    public void setIdPartido(Long idPartido) { this.idPartido = idPartido; }
    public String getResultadoPredicho() { return resultadoPredicho; }
    public void setResultadoPredicho(String resultadoPredicho) { this.resultadoPredicho = resultadoPredicho; }
    public Integer getGolesLocalPredichos() { return golesLocalPredichos; }
    public void setGolesLocalPredichos(Integer golesLocalPredichos) { this.golesLocalPredichos = golesLocalPredichos; }
    public Integer getGolesVisitantePredichos() { return golesVisitantePredichos; }
    public void setGolesVisitantePredichos(Integer golesVisitantePredichos) { this.golesVisitantePredichos = golesVisitantePredichos; }
    public Integer getPuntajeObtenido() { return puntajeObtenido; }
    public void setPuntajeObtenido(Integer puntajeObtenido) { this.puntajeObtenido = puntajeObtenido; }
    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public byte getCerrado() { return cerrado; }
    public void setCerrado(byte cerrado) { this.cerrado = cerrado; }
}