package com.sistemaVeterinario.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "mascotas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa una mascota registrada por un usuario")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mascota")
    @Schema(description = "ID autogenerado de la mascota", example = "1")
    private Integer idMascota;

    @Column(length = 45, nullable = false)
    @Schema(description = "Nombre de la mascota", example = "Firulais")
    private String nombre;

    @Column(length = 45, nullable = false)
    @Schema(description = "Especie de la mascota", example = "Perro")
    private String especie;

    @Column(length = 45, nullable = false)
    @Schema(description = "Raza de la mascota", example = "Labrador")
    private String raza;

    @Column(name = "fecha_nacimiento", nullable = false)
    @Schema(description = "Fecha de nacimiento de la mascota", example = "2020-03-15")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    @Schema(description = "Sexo de la mascota", example = "Macho")
    private SexoMascota sexo;

    @Schema(description = "Enumeración para representar el sexo de la mascota")
    public enum SexoMascota {
        Macho, Hembra
    }

    @ManyToOne
    @JoinColumn(name = "id_propietario", nullable = false)
    @Schema(description = "Usuario propietario de la mascota")
    private Usuario propietario;

    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL)
    @Schema(description = "Conjunto de citas asociadas a esta mascota")
    private Set<Cita> citas = new HashSet<>();

}