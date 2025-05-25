package com.sistemaVeterinario.controller;

import com.sistemaVeterinario.models.Mascota;
import com.sistemaVeterinario.models.Usuario;
import com.sistemaVeterinario.service.MascotaService;
import com.sistemaVeterinario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/mascotas")
@Tag(name = "Mascota Controller", description = "Controlador para la gestión completa de mascotas por parte de los usuarios propietarios")
public class MascotaController {

    @Autowired
    private MascotaService mascotaService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @Operation(
            summary = "Listar mis mascotas",
            description = "Muestra la lista completa de mascotas que pertenecen al usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de mascotas obtenida exitosamente",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String listarMascotas(
            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model) {
        // Obtener el usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioActual = usuarioService.findByEmail(email);

        List<Mascota> mascotas = mascotaService.findByPropietario(usuarioActual);
        model.addAttribute("mascotas", mascotas);
        return "mascotas/listaMascota";
    }

    @GetMapping("/nueva")
    @Operation(
            summary = "Mostrar formulario de nueva mascota",
            description = "Presenta el formulario para registrar una nueva mascota en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Formulario de registro mostrado correctamente",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String mostrarFormularioNuevaMascota(
            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model) {
        model.addAttribute("mascota", new Mascota());
        model.addAttribute("titulo", "Agregar Nueva Mascota");
        return "mascotas/formMascota";
    }

    @PostMapping("/guardar")
    @Operation(
            summary = "Guardar nueva mascota",
            description = "Procesa el formulario para registrar una nueva mascota asignándola automáticamente al usuario autenticado como propietario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Mascota guardada exitosamente, redirige a la lista con mensaje de éxito",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Error en la validación, muestra el formulario con errores",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String guardarMascota(
            @Parameter(
                    description = "Datos de la mascota a registrar",
                    required = true,
                    schema = @Schema(implementation = Mascota.class)
            )
            @ModelAttribute Mascota mascota,

            @Parameter(
                    description = "Fecha de nacimiento de la mascota en formato dd/MM/yyyy",
                    required = true,
                    example = "15/03/2020"
            )
            @RequestParam("fechaNacimientoStr")
            @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fechaNacimiento,

            @Parameter(description = "Resultado de la validación de datos", hidden = true)
            BindingResult result,

            @Parameter(description = "Atributos para redirección", hidden = true)
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "mascotas/formMascota";
        }

        // Obtener el usuario autenticado y asignarlo como propietario
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioActual = usuarioService.findByEmail(email);

        if (!usuarioActual.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la mascota: usuario no autenticado");
            return "redirect:/mascotas";
        }

        mascota.setPropietario(usuarioActual.get());
        mascota.setFechaNacimiento(fechaNacimiento);

        mascotaService.save(mascota);
        redirectAttributes.addFlashAttribute("success", "Mascota guardada con éxito");
        return "redirect:/mascotas";
    }

    @GetMapping("/editar/{id}")
    @Operation(
            summary = "Mostrar formulario de edición",
            description = "Presenta el formulario precargado con los datos de la mascota para su edición. Solo el propietario puede editar su mascota"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Formulario de edición mostrado correctamente",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirige con error si la mascota no existe o no pertenece al usuario",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos para editar esta mascota",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mascota no encontrada",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String mostrarFormularioEditarMascota(
            @Parameter(
                    description = "ID único de la mascota a editar",
                    required = true,
                    example = "1"
            )
            @PathVariable Integer id,

            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model,

            @Parameter(description = "Atributos para redirección", hidden = true)
            RedirectAttributes redirectAttributes) {
        // Obtener el usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioActual = usuarioService.findByEmail(email);

        Mascota mascota = mascotaService.findById(id);

        // Verificar que la mascota exista y pertenezca al usuario
        if (mascota == null) {
            redirectAttributes.addFlashAttribute("error", "La mascota no existe");
            return "redirect:/mascotas";
        }

        if (!usuarioActual.isPresent() || !mascota.getPropietario().getIdUsuario().equals(usuarioActual.get().getIdUsuario())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar esta mascota");
            return "redirect:/mascotas";
        }

        model.addAttribute("mascota", mascota);
        model.addAttribute("titulo", "Editar Mascota");
        model.addAttribute("fechaNacimientoStr", mascota.getFechaNacimiento());
        return "mascotas/formMascota";
    }

    @GetMapping("/eliminar/{id}")
    @Operation(
            summary = "Eliminar mascota",
            description = "Elimina permanentemente una mascota del sistema. Solo el propietario puede eliminar su mascota"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Mascota eliminada exitosamente, redirige con mensaje de confirmación",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "302",
                    description = "Error al eliminar - mascota no encontrada o sin permisos, redirige con mensaje de error",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos para eliminar esta mascota",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mascota no encontrada",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String eliminarMascota(
            @Parameter(
                    description = "ID único de la mascota a eliminar",
                    required = true,
                    example = "3"
            )
            @PathVariable Integer id,

            @Parameter(description = "Atributos para redirección", hidden = true)
            RedirectAttributes redirectAttributes) {
        // Obtener el usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioActual = usuarioService.findByEmail(email);

        Mascota mascota = mascotaService.findById(id);

        // Verificar que la mascota exista y pertenezca al usuario
        if (mascota == null) {
            redirectAttributes.addFlashAttribute("error", "La mascota no existe");
            return "redirect:/mascotas";
        }

        if (!usuarioActual.isPresent() || !mascota.getPropietario().getIdUsuario().equals(usuarioActual.get().getIdUsuario())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar esta mascota");
            return "redirect:/mascotas";
        }

        mascotaService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Mascota eliminada con éxito");
        return "redirect:/mascotas";
    }

    @GetMapping("/detalles/{id}")
    @Operation(
            summary = "Ver detalles de mascota",
            description = "Muestra la información detallada de una mascota específica. Solo el propietario puede ver los detalles de su mascota"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalles de la mascota mostrados correctamente",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirige con error si la mascota no existe o no pertenece al usuario",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos para ver esta mascota",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mascota no encontrada",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String verDetallesMascota(
            @Parameter(
                    description = "ID único de la mascota a consultar",
                    required = true,
                    example = "2"
            )
            @PathVariable Integer id,

            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model,

            @Parameter(description = "Atributos para redirección", hidden = true)
            RedirectAttributes redirectAttributes) {
        // Obtener el usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioActual = usuarioService.findByEmail(email);

        Mascota mascota = mascotaService.findById(id);

        // Verificar que la mascota exista y pertenezca al usuario
        if (mascota == null) {
            redirectAttributes.addFlashAttribute("error", "La mascota no existe");
            return "redirect:/mascotas";
        }

        if (!usuarioActual.isPresent() || !mascota.getPropietario().getIdUsuario().equals(usuarioActual.get().getIdUsuario())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver esta mascota");
            return "redirect:/mascotas";
        }

        model.addAttribute("mascota", mascota);
        return "mascotas/detallesMascota";
    }
}