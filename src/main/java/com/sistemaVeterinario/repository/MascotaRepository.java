package com.sistemaVeterinario.repository;

import com.sistemaVeterinario.models.Mascota;
import com.sistemaVeterinario.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar las operaciones de persistencia relacionadas con las mascotas.
 * Proporciona métodos para acceder y manipular datos de mascotas en la base de datos.
 */
public interface MascotaRepository extends JpaRepository<Mascota, Integer> {

    /**
     * Busca todas las mascotas asociadas a un propietario específico.
     *
     * @param propietario El objeto Usuario que representa al propietario de las mascotas
     * @return Lista de mascotas pertenecientes al propietario especificado.
     *         La lista puede estar vacía si el propietario no tiene mascotas registradas.
     */
    List<Mascota> findByPropietario(Usuario propietario);
}