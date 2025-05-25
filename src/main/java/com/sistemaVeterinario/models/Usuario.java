package com.sistemaVeterinario.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="usuarios")
@Schema(description = "Entidad que representa a un usuario del sistema veterinario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    @Schema(description = "ID autogenerado del usuario", example = "1")
    private Integer idUsuario;

    @Column(length = 50, nullable = false)
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    @Column(length = 50, nullable = false)
    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String apellido;

    @Column(length = 255, nullable = false, unique = true)
    @Schema(description = "Correo electrónico del usuario. Debe ser único.", example = "juan.perez@example.com")
    private String email;

    @Column(length = 10, nullable = false, unique = true)
    @Schema(description = "Número de teléfono del usuario. Debe ser único y tener máximo 10 dígitos.")
    private String telefono;

    @Column(length = 255, nullable = false)
    @Schema(description = "Contraseña cifrada del usuario")
    private String contrasena;

    @Column(name = "fecha_registro")
    @Schema(description = "Fecha y hora en que se registró el usuario")
    private LocalDateTime fechaRegistro;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    @Schema(description = "Roles asignados al usuario")
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL)
    @Schema(description = "Mascotas registradas por el usuario")
    private Set<Mascota> mascotas = new HashSet<>();

    @PrePersist
    public void prePersist(){
        fechaRegistro = LocalDateTime.now();
    }
}
