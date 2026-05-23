package co.edu.unbosque.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sede")
public class Sede implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nombre_estadio", nullable = false)
    private String nombreEstadio;

    @Column(name = "ciudad", nullable = false)
    private String ciudad;

    @Column(name = "pais", nullable = false)
    private String pais;

    @Column(name = "capacidad")
    private Integer capacidad;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    // URLs separadas por coma: "url1,url2,url3"
    @Column(name = "imagen_url", length = 2000)
    private String imagenUrl;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    public Sede() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreEstadio() { return nombreEstadio; }
    public void setNombreEstadio(String nombreEstadio) { this.nombreEstadio = nombreEstadio; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
