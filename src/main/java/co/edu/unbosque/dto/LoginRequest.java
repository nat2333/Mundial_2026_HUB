package co.edu.unbosque.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginRequest {

    @NotNull(message = "El correo no puede ser nulo.")
    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "El correo debe tener un formato válido.")
    @JsonProperty("correo")
    private String correoUsuario;

    @NotNull(message = "La clave no puede ser nula.")
    @NotBlank(message = "La clave es obligatoria.")
    @JsonProperty("clave")
    private String claveUsuario;

    public LoginRequest() {}

    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String c) { this.correoUsuario = c; }
    public String getClaveUsuario() { return claveUsuario; }
    public void setClaveUsuario(String c) { this.claveUsuario = c; }
}