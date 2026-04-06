package co.edu.unbosque.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistroRequest {

    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 100)
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Formato de correo inválido.")
    private String correoUsuario;

    @NotBlank(message = "La clave es obligatoria.")
    private String claveUsuario;

    public RegistroRequest() {}

    public String getNombres() { return nombres; }
    public void setNombres(String n) { this.nombres = n; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String a) { this.apellidos = a; }
    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String c) { this.correoUsuario = c; }
    public String getClaveUsuario() { return claveUsuario; }
    public void setClaveUsuario(String c) { this.claveUsuario = c; }
}