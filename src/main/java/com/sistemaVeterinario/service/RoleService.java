package com.sistemaVeterinario.service;

import com.sistemaVeterinario.models.Role;
import com.sistemaVeterinario.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio para la gestión de roles de usuario en el sistema veterinario.
 * Proporciona métodos para obtener roles específicos del sistema.
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * Constructor para inyección de dependencias.
     * @param roleRepository Repositorio de roles
     */
    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Obtiene el rol básico de usuario (USER).
     * @return Entidad Role correspondiente al rol USER
     * @throws IllegalStateException Si el rol USER no existe en la base de datos
     */
    public Role obtenerRolUsuario() {
        return roleRepository.findByNombreRol("USER")
                .orElseThrow(() -> new IllegalStateException("El rol de usuario no existe"));
    }
}