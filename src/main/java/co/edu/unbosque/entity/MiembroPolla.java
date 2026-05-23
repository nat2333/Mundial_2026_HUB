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
@Table(name = "miembro_polla")
public class MiembroPolla implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_polla", nullable = false)
    private Long idPolla;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "puntaje_total")
    private Integer puntajeTotal;

    @Column(name = "posicion_ranking")
    private Integer posicionRanking;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_union")
    private Date fechaUnion;

    public MiembroPolla() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdPolla() { return idPolla; }
    public void setIdPolla(Long idPolla) { this.idPolla = idPolla; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Integer getPuntajeTotal() { return puntajeTotal; }
    public void setPuntajeTotal(Integer puntajeTotal) { this.puntajeTotal = puntajeTotal; }
    public Integer getPosicionRanking() { return posicionRanking; }
    public void setPosicionRanking(Integer posicionRanking) { this.posicionRanking = posicionRanking; }
    public Date getFechaUnion() { return fechaUnion; }
    public void setFechaUnion(Date fechaUnion) { this.fechaUnion = fechaUnion; }
}