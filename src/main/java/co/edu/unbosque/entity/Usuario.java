package co.edu.unbosque.entity;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "correo_usuario", unique = true, nullable = false)
    private String correoUsuario;

    @Column(name = "clave_usuario", nullable = false)
    private String claveUsuario;

    @Column(name = "nombres")
    private String nombres;

    @Column(name = "apellidos")
    private String apellidos;

    @Column(name = "rol")
    private String rol;

    @Column(name = "estado")
    private byte estado;

    @Column(name = "intentos")
    private int intentos;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_registro")
    private Date fechaRegistro;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_ultima_clave")
    private Date fechaUltimaClave;

    @Column(name = "correo_verificado")
    private boolean correoVerificado = false;

    @Column(name = "token_verificacion", length = 200)
    private String tokenVerificacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_expiracion_token")
    private Date fechaExpiracionToken;

    public Usuario() {}

    public Long getId() { 
    	return id; 
    }
    
    public void setId(Long id) { 
    	this.id = id; 
    }
    
    public String getCorreoUsuario() { 
    	return correoUsuario; 
    }
    
    public void setCorreoUsuario(String correoUsuario) { 
    	this.correoUsuario = correoUsuario; 
    }
    
    public String getClaveUsuario() { 
    	return claveUsuario; 
    }
    
    public void setClaveUsuario(String claveUsuario) { 
    	this.claveUsuario = claveUsuario; 
    }
    
    public String getNombres() { 
    	return nombres; 
    }
    
    public void setNombres(String nombres) { 
    	this.nombres = nombres; 
    }
    
    public String getApellidos() { 
    	return apellidos; 
    }
    
    public void setApellidos(String apellidos) { 
    	this.apellidos = apellidos; 
    }

    public String getRol() { 
    	return rol; 
    }
    
    public void setRol(String rol) { 
    	this.rol = rol; 
    }
    
    public byte getEstado() { 
    	return estado; 
    }
    
    public void setEstado(byte estado) { 
    	this.estado = estado; 
    }
    
    public int getIntentos() { 
    	return intentos; 
    }
    
    public void setIntentos(int intentos) { 
    	this.intentos = intentos; 
    }
    
    public Date getFechaRegistro() { 
    	return fechaRegistro; 
    }
    
    public void setFechaRegistro(Date fechaRegistro) { 
    	this.fechaRegistro = fechaRegistro; 
    }
    
    public Date getFechaUltimaClave() {
    	return fechaUltimaClave;
    }

    public void setFechaUltimaClave(Date fechaUltimaClave) {
    	this.fechaUltimaClave = fechaUltimaClave;
    }

    public boolean isCorreoVerificado() {
    	return correoVerificado;
    }

    public void setCorreoVerificado(boolean correoVerificado) {
    	this.correoVerificado = correoVerificado;
    }

    public String getTokenVerificacion() {
    	return tokenVerificacion;
    }

    public void setTokenVerificacion(String tokenVerificacion) {
    	this.tokenVerificacion = tokenVerificacion;
    }

    public Date getFechaExpiracionToken() {
    	return fechaExpiracionToken;
    }

    public void setFechaExpiracionToken(Date fechaExpiracionToken) {
    	this.fechaExpiracionToken = fechaExpiracionToken;
    }
}