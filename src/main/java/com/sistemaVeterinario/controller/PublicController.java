package com.sistemaVeterinario.controller;

import com.sistemaVeterinario.dto.UsuarioDTO;
import com.sistemaVeterinario.models.Servicio;
import com.sistemaVeterinario.service.AuthService;
import com.sistemaVeterinario.service.ServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Tag(name = "Public Controller", description = "Controlador público para páginas de acceso general, registro y autenticación")
public class PublicController {

    @Autowired
    private ServicioService servicioService;

    private final AuthService authService;
    private final MessageSource messageSource;

    public PublicController(AuthService authService, MessageSource messageSource) {
        this.authService = authService;
        this.messageSource = messageSource;
    }

    @GetMapping("/error/403")
    @Operation(
            summary = "Página de error 403",
            description = "Muestra la página de error cuando el usuario no tiene permisos para acceder a un recurso"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de error 403 mostrada correctamente",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String error403() {
        return "error/403";
    }

    @GetMapping("/")
    @Operation(
            summary = "Página principal",
            description = "Muestra la página de inicio con la lista de servicios activos disponibles"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página principal cargada exitosamente",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String home(
            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true) Model model
    ) {
        List<Servicio> servicios = servicioService.findAllActive();
        model.addAttribute("servicios", servicios);
        return "public/home";
    }

    @GetMapping("/about-us")
    @Operation(
            summary = "Página Acerca de Nosotros",
            description = "Muestra la página con información sobre la empresa o clínica veterinaria"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página 'Acerca de Nosotros' mostrada correctamente",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String aboutUs() {
        return "public/aboutUs";
    }

    @GetMapping("/login")
    @Operation(
            summary = "Formulario de inicio de sesión",
            description = "Muestra el formulario para que los usuarios puedan iniciar sesión en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Formulario de login mostrado correctamente",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String mostrarLogin() {
        return "public/loginForm";
    }

    @GetMapping("/register")
    @Operation(
            summary = "Formulario de registro",
            description = "Muestra el formulario de registro para que nuevos usuarios puedan crear una cuenta"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Formulario de registro mostrado correctamente",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String mostrarRegistro(
            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true) Model model
    ) {
        model.addAttribute("usuario", new UsuarioDTO());
        return "/public/registrationForm";
    }

    @PostMapping("/register")
    @Operation(
            summary = "Procesar registro de usuario",
            description = "Procesa el formulario de registro de un nuevo usuario. Valida los datos y crea la cuenta si es válida."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Usuario registrado exitosamente, redirige a la página de login",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Error en la validación o registro, muestra el formulario con errores",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String procesarRegistro(
            @Parameter(
                    description = "Datos del usuario a registrar",
                    required = true,
                    schema = @Schema(implementation = UsuarioDTO.class)
            )
            @Valid @ModelAttribute("usuario") UsuarioDTO usuarioDTO,

            @Parameter(description = "Resultado de la validación de datos", hidden = true)
            BindingResult bindingResult,

            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model
    ) {
        // Si hay errores de validación, volvemos al formulario
        if (bindingResult.hasErrors()) {
            return "public/registrationForm";
        }

        try {
            authService.registrarUsuario(usuarioDTO);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            // Manejo de errores específicos
            String mensaje = e.getMessage();

            if (mensaje.contains("correo")) {
                model.addAttribute("emailError",
                        messageSource.getMessage("error.email.existing", null, LocaleContextHolder.getLocale()));
            } else if (mensaje.contains("teléfono")) {
                model.addAttribute("telefonoError",
                        messageSource.getMessage("error.phone.existing", null, LocaleContextHolder.getLocale()));
            }
            return "public/registrationForm";
        }
    }
}