package co.edu.unbosque.entity;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name = "preferencia_usuario")
public class PreferenciaUsuario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "selecciones_favoritas")
    private String seleccionesFavoritas;

    @Column(name = "ciudades_interes")
    private String ciudadesInteres;

    @Column(name = "notif_push")
    private byte notifPush;

    @Column(name = "notif_email")
    private byte notifEmail;

    public PreferenciaUsuario() {}

    public Long getId() { 
    	return id; 
    }
    
    public void setId(Long id) { 
    	this.id = id; 
    }
    
    public Long getIdUsuario() { 
    	return idUsuario; 
    }
    
    public void setIdUsuario(Long idUsuario) { 
    	this.idUsuario = idUsuario; 
    }
    
    public String getSeleccionesFavoritas() { 
    	return seleccionesFavoritas; 
    }
    
    public void setSeleccionesFavoritas(String seleccionesFavoritas) { 
    	this.seleccionesFavoritas = seleccionesFavoritas; 
    }
    
    public String getCiudadesInteres() { 
    	return ciudadesInteres; 
    }
    
    public void setCiudadesInteres(String ciudadesInteres) { 
    	this.ciudadesInteres = ciudadesInteres; 
    }
    
    public byte getNotifPush() { 
    	return notifPush; 
    }
    
    public void setNotifPush(byte notifPush) { 
    	this.notifPush = notifPush; 
    }
    
    public byte getNotifEmail() { 
    	return notifEmail; 
    }
    
    public void setNotifEmail(byte notifEmail) { 
    	this.notifEmail = notifEmail; 
    }
}