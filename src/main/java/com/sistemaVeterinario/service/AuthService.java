package com.sistemaVeterinario.service;

import com.sistemaVeterinario.dto.UsuarioDTO;
import com.sistemaVeterinario.models.Role;
import com.sistemaVeterinario.models.Usuario;
import com.sistemaVeterinario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación que implementa la interfaz UserDetailsService de Spring Security.
 * Maneja el registro de usuarios y la carga de detalles de usuario para autenticación.
 */
@Service
public class AuthService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final MessageSource messageSource;

    /**
     * Constructor para inyección de dependencias
     * @param usuarioRepository Repositorio de usuarios
     * @param passwordEncoder Codificador de contraseñas
     * @param roleService Servicio de roles
     * @param messageSource Fuente de mensajes internacionalizados
     */
    @Autowired
    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            RoleService roleService,
            MessageSource messageSource) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.messageSource = messageSource;
    }

    /**
     * Carga los detalles del usuario por email para la autenticación
     * @param email Email del usuario a buscar
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException Si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageSource.getMessage("error.user.notFound",
                                new Object[]{email},
                                LocaleContextHolder.getLocale())));

        List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNombreRol()))
                .collect(Collectors.toList());

        return new User(usuario.getEmail(), usuario.getContrasena(), authorities);
    }

    /**
     * Registra un nuevo usuario en el sistema
     * @param registroDTO DTO con los datos de registro
     * @return Usuario registrado
     * @throws IllegalArgumentException Si el email o teléfono ya existen
     */
    @Transactional
    public Usuario registrarUsuario(UsuarioDTO registroDTO) {
        // Verifica si el email ya existe
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("error.email.existing",
                            null,
                            LocaleContextHolder.getLocale()));
        }

        // Verifica si el teléfono ya existe
        if (usuarioRepository.existsByTelefono(registroDTO.getTelefono())) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("error.phone.existing",
                            null,
                            LocaleContextHolder.getLocale()));
        }

        // Crea nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(registroDTO.getNombre());
        usuario.setApellido(registroDTO.getApellido());
        usuario.setEmail(registroDTO.getEmail());
        usuario.setTelefono(registroDTO.getTelefono());

        // Encripta la contraseña
        usuario.setContrasena(passwordEncoder.encode(registroDTO.getContrasena()));

        // Asigna rol de usuario por defecto
        Role rolUsuario = roleService.obtenerRolUsuario();
        usuario.setRoles(new HashSet<>(Collections.singletonList(rolUsuario)));

        // Guarda en la base de datos
        return usuarioRepository.save(usuario);
    }
}