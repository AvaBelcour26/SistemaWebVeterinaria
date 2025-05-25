package com.sistemaVeterinario.controller;

import com.sistemaVeterinario.dto.UsuarioDTO;
import com.sistemaVeterinario.models.Role;
import com.sistemaVeterinario.models.Usuario;
import com.sistemaVeterinario.service.AdminUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin/usuarios")
@Tag(name = "Admin Usuario Controller", description = "Controlador administrativo para la gestión completa de usuarios del sistema")
public class AdminUsuarioController {

    @Autowired
    private AdminUsuarioService adminUsuarioService;

    /**
     * Muestra el listado de usuarios con opción de búsqueda
     */
    @GetMapping
    @Operation(
            summary = "Listar usuarios",
            description = "Muestra el listado completo de usuarios del sistema con funcionalidad de búsqueda opcional por nombre o email"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String listarUsuarios(
            @Parameter(
                    description = "Término de búsqueda para filtrar usuarios por nombre o email",
                    required = false,
                    example = "juan@ejemplo.com"
            )
            @RequestParam(required = false) String search,

            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model) {
        List<Usuario> usuarios;
        if (search != null && !search.isEmpty()) {
            usuarios = adminUsuarioService.searchUsuarios(search);
            model.addAttribute("search", search);
        } else {
            usuarios = adminUsuarioService.getAllUsuarios();
        }
        model.addAttribute("usuarios", usuarios);
        return "admin/lista";
    }

    /**
     * Muestra el formulario para crear un nuevo usuario
     */
    @GetMapping("/nuevo")
    @Operation(
            summary = "Mostrar formulario de nuevo usuario",
            description = "Presenta el formulario para crear un nuevo usuario con todos los roles disponibles para asignar"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Formulario de creación mostrado correctamente",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String mostrarFormularioNuevo(
            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model) {
        model.addAttribute("usuario", new UsuarioDTO());
        model.addAttribute("roles", adminUsuarioService.getAllRoles());
        model.addAttribute("esNuevo", true);
        return "admin/formulario";
    }

    /**
     * Procesa la creación de un nuevo usuario
     */
    @PostMapping("/nuevo")
    @Operation(
            summary = "Crear nuevo usuario",
            description = "Procesa los datos del formulario para crear un nuevo usuario en el sistema. Valida los datos y asigna los roles seleccionados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Usuario creado exitosamente, redirige a la lista de usuarios",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Error en la validación o creación, muestra el formulario con errores",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o duplicados (email/teléfono existente)",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String crearUsuario(
            @Parameter(
                    description = "Datos del usuario a crear",
                    required = true,
                    schema = @Schema(implementation = UsuarioDTO.class)
            )
            @Valid @ModelAttribute("usuario") UsuarioDTO usuario,

            @Parameter(description = "Resultado de la validación de datos", hidden = true)
            BindingResult result,

            @Parameter(
                    description = "IDs de los roles a asignar al usuario",
                    required = false,
                    example = "[1, 2]"
            )
            @RequestParam(required = false) Set<Integer> rolesIds,

            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model,

            @Parameter(description = "Atributos para redirección", hidden = true)
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("roles", adminUsuarioService.getAllRoles());
            model.addAttribute("esNuevo", true);
            return "admin/formulario";
        }

        if (rolesIds == null || rolesIds.isEmpty()) {
            model.addAttribute("roles", adminUsuarioService.getAllRoles());
            model.addAttribute("esNuevo", true);
            model.addAttribute("errorRoles", "Debe seleccionar al menos un rol");
            return "admin/formulario";
        }

        try {
            adminUsuarioService.createUsuario(usuario, rolesIds);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado exitosamente");
            return "redirect:/admin/usuarios";
        } catch (DataIntegrityViolationException e) {
            // Manejo específico para violación de datos (ej: duplicados)
            String errorMessage = "Error de datos: ";
            if (e.getMessage().contains(usuario.getTelefono())) {
                errorMessage += "El teléfono ya está registrado";
            } else if (e.getMessage().contains(usuario.getEmail())) {
                errorMessage += "El email ya está registrado";
            } else {
                errorMessage += "Datos inválidos o duplicados";
            }
            model.addAttribute("error", errorMessage);
        } catch (IllegalArgumentException e) {
            // Manejo para parámetros inválidos
            model.addAttribute("error", "Datos inválidos: " + e.getMessage());
        } catch (Exception e) {
            // Manejo genérico para otros errores
            model.addAttribute("error", "Error inesperado al crear el usuario");
        } finally {
            // Prepara el modelo para volver a mostrar el formulario
            model.addAttribute("roles", adminUsuarioService.getAllRoles());
            model.addAttribute("esNuevo", true);
            model.addAttribute("usuario", usuario); // Mantener los datos ingresados
        }

        return "admin/formulario";
    }

    /**
     * Muestra el formulario para editar un usuario existente
     */
    @GetMapping("/editar/{id}")
    @Operation(
            summary = "Mostrar formulario de edición",
            description = "Presenta el formulario precargado con los datos del usuario para su edición, incluyendo roles actuales asignados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Formulario de edición mostrado correctamente",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "302",
                    description = "Usuario no encontrado, redirige a la lista de usuarios",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String mostrarFormularioEditar(
            @Parameter(
                    description = "ID único del usuario a editar",
                    required = true,
                    example = "1"
            )
            @PathVariable Integer id,

            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model) {
        Optional<Usuario> optionalUsuario = adminUsuarioService.getUsuarioById(id);

        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            UsuarioDTO usuarioDTO = adminUsuarioService.convertToDto(usuario);

            model.addAttribute("usuario", usuarioDTO);
            model.addAttribute("usuarioId", id);
            model.addAttribute("roles", adminUsuarioService.getAllRoles());
            model.addAttribute("usuarioRoles", usuario.getRoles());
            model.addAttribute("esNuevo", false);

            return "admin/formulario";
        } else {
            return "redirect:/admin/usuarios";
        }
    }

    /**
     * Procesa la actualización de un usuario existente
     */
    @PostMapping("/editar/{id}")
    @Operation(
            summary = "Actualizar usuario existente",
            description = "Procesa los datos del formulario para actualizar la información de un usuario existente, incluyendo la reasignación de roles"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Usuario actualizado exitosamente, redirige a la lista de usuarios",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Error en la validación o actualización, muestra el formulario con errores",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o error en la actualización",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String actualizarUsuario(
            @Parameter(
                    description = "ID único del usuario a actualizar",
                    required = true,
                    example = "1"
            )
            @PathVariable Integer id,

            @Parameter(
                    description = "Datos actualizados del usuario",
                    required = true,
                    schema = @Schema(implementation = Usuario.class)
            )
            @Valid @ModelAttribute("usuario") Usuario usuario,

            @Parameter(description = "Resultado de la validación de datos", hidden = true)
            BindingResult result,

            @Parameter(
                    description = "IDs de los roles a asignar al usuario",
                    required = false,
                    example = "[1, 3]"
            )
            @RequestParam(required = false) Set<Integer> rolesIds,

            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model,

            @Parameter(description = "Atributos para redirección", hidden = true)
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("usuarioId", id);
            model.addAttribute("roles", adminUsuarioService.getAllRoles());
            model.addAttribute("esNuevo", false);
            return "admin/formulario";
        }

        if (rolesIds == null || rolesIds.isEmpty()) {
            model.addAttribute("usuarioId", id);
            model.addAttribute("roles", adminUsuarioService.getAllRoles());
            model.addAttribute("esNuevo", false);
            model.addAttribute("errorRoles", "Debe seleccionar al menos un rol");
            return "admin/formulario";
        }

        try {
            adminUsuarioService.updateUsuario(id, usuario, rolesIds);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el usuario: " + e.getMessage());
            model.addAttribute("usuarioId", id);
            model.addAttribute("roles", adminUsuarioService.getAllRoles());
            model.addAttribute("esNuevo", false);
            return "admin/formulario";
        }
    }
}