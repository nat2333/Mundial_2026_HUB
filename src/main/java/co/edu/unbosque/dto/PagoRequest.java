package co.edu.unbosque.dto;

public class PagoRequest {

    private Long idEntrada;
    private Long idUsuario;

    public PagoRequest() {}

    public Long getIdEntrada() { return idEntrada; }
    public void setIdEntrada(Long idEntrada) { this.idEntrada = idEntrada; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
}
