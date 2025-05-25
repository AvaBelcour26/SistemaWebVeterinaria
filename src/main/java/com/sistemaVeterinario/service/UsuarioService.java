package com.sistemaVeterinario.service;

import com.sistemaVeterinario.models.Usuario;
import com.sistemaVeterinario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio para operaciones relacionadas con usuarios del sistema veterinario.
 * Proporciona métodos para buscar usuarios por sus credenciales.
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param email La dirección de email a buscar (case-sensitive)
     * @return Un Optional conteniendo el usuario si existe, o vacío si no se encuentra
     */
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}