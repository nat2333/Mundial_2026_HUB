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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "agenda_usuario",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_agenda_usuario_partido",
           columnNames = {"id_usuario", "id_partido"}
       ))
public class AgendaUsuario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_partido", nullable = false)
    private Long idPartido;

    @Column(name = "recordatorio")
    private byte recordatorio;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_agendado")
    private Date fechaAgendado;

    public AgendaUsuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Long getIdPartido() { return idPartido; }
    public void setIdPartido(Long idPartido) { this.idPartido = idPartido; }
    public byte getRecordatorio() { return recordatorio; }
    public void setRecordatorio(byte recordatorio) { this.recordatorio = recordatorio; }
    public Date getFechaAgendado() { return fechaAgendado; }
    public void setFechaAgendado(Date fechaAgendado) { this.fechaAgendado = fechaAgendado; }
}