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
@Table(name = "transferencia_entrada")
public class TransferenciaEntrada implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_entrada", nullable = false)
    private Long idEntrada;

    @Column(name = "id_usuario_origen", nullable = false)
    private Long idUsuarioOrigen;

    @Column(name = "id_usuario_destino", nullable = false)
    private Long idUsuarioDestino;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_transferencia")
    private Date fechaTransferencia;

    @Column(name = "id_correlacion")
    private String idCorrelacion;

    @Column(name = "estado")
    private String estado;

    public TransferenciaEntrada() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdEntrada() { return idEntrada; }
    public void setIdEntrada(Long idEntrada) { this.idEntrada = idEntrada; }
    public Long getIdUsuarioOrigen() { return idUsuarioOrigen; }
    public void setIdUsuarioOrigen(Long idUsuarioOrigen) { this.idUsuarioOrigen = idUsuarioOrigen; }
    public Long getIdUsuarioDestino() { return idUsuarioDestino; }
    public void setIdUsuarioDestino(Long idUsuarioDestino) { this.idUsuarioDestino = idUsuarioDestino; }
    public Date getFechaTransferencia() { return fechaTransferencia; }
    public void setFechaTransferencia(Date fechaTransferencia) { this.fechaTransferencia = fechaTransferencia; }
    public String getIdCorrelacion() { return idCorrelacion; }
    public void setIdCorrelacion(String idCorrelacion) { this.idCorrelacion = idCorrelacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}