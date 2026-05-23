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
@Table(name = "lamina_usuario")
public class LaminaUsuario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_lamina", nullable = false)
    private Long idLamina;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "en_intercambio")
    private byte enIntercambio;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_obtencion")
    private Date fechaObtencion;

    public LaminaUsuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Long getIdLamina() { return idLamina; }
    public void setIdLamina(Long idLamina) { this.idLamina = idLamina; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public byte getEnIntercambio() { return enIntercambio; }
    public void setEnIntercambio(byte enIntercambio) { this.enIntercambio = enIntercambio; }
    public Date getFechaObtencion() { return fechaObtencion; }
    public void setFechaObtencion(Date fechaObtencion) { this.fechaObtencion = fechaObtencion; }
}