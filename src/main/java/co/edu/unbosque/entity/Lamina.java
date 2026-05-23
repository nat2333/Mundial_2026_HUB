package co.edu.unbosque.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lamina")
public class Lamina implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "seleccion")
    private String seleccion;

    @Column(name = "rareza")
    private String rareza;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "numero_album")
    private Integer numeroAlbum;

    public Lamina() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getSeleccion() { return seleccion; }
    public void setSeleccion(String seleccion) { this.seleccion = seleccion; }
    public String getRareza() { return rareza; }
    public void setRareza(String rareza) { this.rareza = rareza; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public Integer getNumeroAlbum() { return numeroAlbum; }
    public void setNumeroAlbum(Integer numeroAlbum) { this.numeroAlbum = numeroAlbum; }
}