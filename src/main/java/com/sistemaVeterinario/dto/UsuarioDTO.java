package com.sistemaVeterinario.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "DTO para la transferencia de datos de usuario")
public class UsuarioDTO {

    @Schema(
            description = "Nombre del usuario (3-50 caracteres, solo letras y espacios)",
            example = "Juan"
    )
    @Pattern(regexp = "^(?!\\s)([a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{3,50})(?<!\\s)$", message = "{error.name.invalid}")
    private String nombre;

    @Schema(
            description = "Apellido del usuario (3-50 caracteres, solo letras y espacios)",
            example = "Pérez"
    )
    @Pattern(regexp = "^(?!\\s)([a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{3,50})(?<!\\s)$", message = "{error.lastname.invalid}")
    private String apellido;

    @Schema(
            description = "Correo electrónico válido",
            example = "juan.perez@example.com"
    )
    @Pattern(regexp =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$",
            message="{error.email.invalid}"
    )
    private String email;

    @Schema(
            description = "Número de teléfono (debe comenzar con 3 y tener 10 dígitos)",
            example = "3xxxxxxxxx"
    )
    @Pattern(regexp = "^3\\d{9}$", message = "{error.phone.invalid}")
    private String telefono;

    @Schema(description = "Contraseña (mínimo 8 caracteres, al menos una mayúscula, una minúscula y un número)",
            example = "Password123"
    )
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@_-]{8,}$", message = "{error.password.invalid}")
    private String contrasena;
}