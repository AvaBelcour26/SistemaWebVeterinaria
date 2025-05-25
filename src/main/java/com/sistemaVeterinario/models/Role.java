package com.sistemaVeterinario.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="roles")
@Schema(description = "Entidad que representa un rol del sistema, como ADMIN o CLIENTE")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    @Schema(description = "ID autogenerado del rol", example = "1")
    private Integer idRol;

    @Column(name = "nombre_rol", length = 50, nullable = false, unique = true)
    @Schema(description = "Nombre único del rol", example = "ADMIN")
    private String nombreRol;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Descripción del rol y sus permisos", example = "Rol con permisos administrativos")
    private String descripcion;

    @ManyToMany(mappedBy = "roles")
    @Schema(description = "Usuarios que poseen este rol")
    private Set<Usuario> usuarios =new HashSet<>();
}