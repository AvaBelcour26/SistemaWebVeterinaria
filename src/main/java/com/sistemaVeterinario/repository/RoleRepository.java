package com.sistemaVeterinario.repository;

import com.sistemaVeterinario.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio para la gestión de roles de usuario en el sistema veterinario.
 * Proporciona métodos para acceder y manipular los datos de roles en la base de datos.
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Busca un rol por su nombre exacto.
     *
     * @param nombreRol El nombre del rol a buscar (case-sensitive)
     * @return Un {@link Optional} que contiene el rol si se encuentra,
     *         o vacío si no existe ningún rol con ese nombre
     */
    Optional<Role> findByNombreRol(String nombreRol);
}