package com.sistemaVeterinario.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="servicios")
@Schema(description = "Entidad que representa un servicio veterinario ofrecido en el sistema")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    @Schema(description = "ID autogenerado del servicio", example = "1")
    private Integer idServicio;

    @Column(length = 45, nullable = false)
    @Schema(description = "Nombre del servicio", example = "Consulta General")
    private String nombre;

    @Column(columnDefinition = "TEXT", nullable = false)
    @Schema(description = "Descripción detallada del servicio", example = "Consulta médica general para mascotas.")
    private String descripcion;

    @Column(name = "activo")
    @Schema(description = "Indica si el servicio está activo", example = "true")
    private Boolean activo = true;


    @OneToMany(mappedBy = "servicio")
    @Schema(description = "Conjunto de citas asociadas a este servicio")
    private Set<Cita> citas = new HashSet<>();
}
