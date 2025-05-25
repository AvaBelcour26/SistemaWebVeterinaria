package com.sistemaVeterinario.repository;

import com.sistemaVeterinario.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de usuarios del sistema veterinario.
 * Proporciona métodos para operaciones CRUD y consultas personalizadas de usuarios.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por su dirección de email.
     *
     * @param email La dirección de email a buscar (case-sensitive)
     * @return {@link Optional} conteniendo el usuario si existe,
     *         o vacío si no se encuentra ningún usuario con ese email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario registrado con el email especificado.
     *
     * @param email El email a verificar
     * @return true si existe un usuario con ese email, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario registrado con el número de teléfono especificado.
     *
     * @param telefono El número de teléfono a verificar
     * @return true si existe un usuario con ese teléfono, false en caso contrario
     */
    boolean existsByTelefono(String telefono);
}