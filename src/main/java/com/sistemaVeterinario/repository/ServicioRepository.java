package com.sistemaVeterinario.repository;

import com.sistemaVeterinario.models.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio para la gestión de servicios veterinarios.
 * Proporciona operaciones de acceso a datos para entidades de tipo {@link Servicio}.
 */
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    /**
     * Obtiene todos los servicios marcados como activos en el sistema.
     *
     * @return Lista de servicios activos. Retorna una lista vacía si no existen servicios activos.
     */
    List<Servicio> findByActivoTrue();
}