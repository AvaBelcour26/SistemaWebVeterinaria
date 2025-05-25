package com.sistemaVeterinario.service;

import com.sistemaVeterinario.dto.UsuarioDTO;
import com.sistemaVeterinario.models.Role;
import com.sistemaVeterinario.models.Usuario;
import com.sistemaVeterinario.repository.RoleRepository;
import com.sistemaVeterinario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de usuarios administrativos
 * Maneja operaciones CRUD para usuarios y asignación de roles
 */
@Service
public class AdminUsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios registrados
     * @return Lista completa de usuarios
     */
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca usuarios por nombre o email
     * @param searchTerm Término de búsqueda
     * @return Lista de usuarios que coinciden con el término
     */
    public List<Usuario> searchUsuarios(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return getAllUsuarios();
        }

        String term = searchTerm.toLowerCase();
        return usuarioRepository.findAll().stream()
                .filter(usuario ->
                        usuario.getNombre().toLowerCase().contains(term) ||
                                usuario.getEmail().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un usuario específico por ID
     * @param id ID del usuario
     * @return Usuario encontrado (opcional)
     */
    public Optional<Usuario> getUsuarioById(Integer id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Convierte un Usuario a DTO para transferencia segura
     * @param usuario Entidad Usuario
     * @return DTO sin información sensible
     */
    public UsuarioDTO convertToDto(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setTelefono(usuario.getTelefono());
        return dto;
    }

    /**
     * Crea un nuevo usuario con roles asignados
     * @param usuarioDTO Datos del usuario
     * @param rolesIds IDs de roles a asignar
     * @return Usuario creado
     */
    @Transactional
    public Usuario createUsuario(UsuarioDTO usuarioDTO, Set<Integer> rolesIds) {
        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setTelefono(usuarioDTO.getTelefono());
        usuario.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena()));

        Set<Role> roles = new HashSet<>();
        if (rolesIds != null && !rolesIds.isEmpty()) {
            for (Integer roleId : rolesIds) {
                Optional<Role> role = roleRepository.findById(roleId);
                role.ifPresent(roles::add);
            }
        }
        usuario.setRoles(roles);

        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza un usuario existente
     * @param id ID del usuario a actualizar
     * @param usuarioDTO Nuevos datos del usuario
     * @param rolesIds Nuevos roles a asignar
     * @return Usuario actualizado o null si no existe
     */
    @Transactional
    public Usuario updateUsuario(Integer id, Usuario usuarioDTO, Set<Integer> rolesIds) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);
        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            usuario.setNombre(usuarioDTO.getNombre());
            usuario.setApellido(usuarioDTO.getApellido());
            usuario.setEmail(usuarioDTO.getEmail());
            usuario.setTelefono(usuarioDTO.getTelefono());

            if (usuarioDTO.getContrasena() != null && !usuarioDTO.getContrasena().isEmpty()) {
                usuario.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena()));
            }

            if (rolesIds != null && !rolesIds.isEmpty()) {
                Set<Role> roles = new HashSet<>();
                for (Integer roleId : rolesIds) {
                    Optional<Role> role = roleRepository.findById(roleId);
                    role.ifPresent(roles::add);
                }
                usuario.setRoles(roles);
            }

            return usuarioRepository.save(usuario);
        }
        return null;
    }

    /**
     * Obtiene todos los roles disponibles
     * @return Lista completa de roles
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}