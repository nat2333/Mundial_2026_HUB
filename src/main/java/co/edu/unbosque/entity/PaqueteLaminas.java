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
@Table(name = "paquete_laminas")
public class PaqueteLaminas implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "tipo_origen")
    private String tipoOrigen;

    @Column(name = "codigo_promo")
    private String codigoPromo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_apertura")
    private Date fechaApertura;

    @Column(name = "abierto")
    private byte abierto;

    public PaqueteLaminas() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getTipoOrigen() { return tipoOrigen; }
    public void setTipoOrigen(String tipoOrigen) { this.tipoOrigen = tipoOrigen; }
    public String getCodigoPromo() { return codigoPromo; }
    public void setCodigoPromo(String codigoPromo) { this.codigoPromo = codigoPromo; }
    public Date getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(Date fechaApertura) { this.fechaApertura = fechaApertura; }
    public byte getAbierto() { return abierto; }
    public void setAbierto(byte abierto) { this.abierto = abierto; }
}