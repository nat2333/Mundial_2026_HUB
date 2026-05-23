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
@Table(name = "discrepancia_datos")
public class DiscrepanciaDatos implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_partido")
    private Long idPartido;

    @Column(name = "fuente_principal")
    private String fuentePrincipal;

    @Column(name = "fuente_secundaria")
    private String fuenteSecundaria;

    @Column(name = "campo_discrepancia")
    private String campoDiscrepancia;

    @Column(name = "valor_principal")
    private String valorPrincipal;

    @Column(name = "valor_secundario")
    private String valorSecundario;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_deteccion")
    private Date fechaDeteccion;

    @Column(name = "resuelto")
    private byte resuelto;

    public DiscrepanciaDatos() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdPartido() { return idPartido; }
    public void setIdPartido(Long idPartido) { this.idPartido = idPartido; }
    public String getFuentePrincipal() { return fuentePrincipal; }
    public void setFuentePrincipal(String fuentePrincipal) { this.fuentePrincipal = fuentePrincipal; }
    public String getFuenteSecundaria() { return fuenteSecundaria; }
    public void setFuenteSecundaria(String fuenteSecundaria) { this.fuenteSecundaria = fuenteSecundaria; }
    public String getCampoDiscrepancia() { return campoDiscrepancia; }
    public void setCampoDiscrepancia(String campoDiscrepancia) { this.campoDiscrepancia = campoDiscrepancia; }
    public String getValorPrincipal() { return valorPrincipal; }
    public void setValorPrincipal(String valorPrincipal) { this.valorPrincipal = valorPrincipal; }
    public String getValorSecundario() { return valorSecundario; }
    public void setValorSecundario(String valorSecundario) { this.valorSecundario = valorSecundario; }
    public Date getFechaDeteccion() { return fechaDeteccion; }
    public void setFechaDeteccion(Date fechaDeteccion) { this.fechaDeteccion = fechaDeteccion; }
    public byte getResuelto() { return resuelto; }
    public void setResuelto(byte resuelto) { this.resuelto = resuelto; }
}