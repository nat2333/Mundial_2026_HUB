package co.edu.unbosque.dto;

public class SedeDTO {

    private Long id;
    private String nombreEstadio;
    private String ciudad;
    private String pais;
    private Double latitud;
    private Double longitud;

    public SedeDTO() {}

    public SedeDTO(Long id, String nombreEstadio, String ciudad, String pais,
                   Double latitud, Double longitud) {
        this.id = id;
        this.nombreEstadio = nombreEstadio;
        this.ciudad = ciudad;
        this.pais = pais;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreEstadio() { return nombreEstadio; }
    public void setNombreEstadio(String nombreEstadio) { this.nombreEstadio = nombreEstadio; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}
