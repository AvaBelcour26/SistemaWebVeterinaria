package com.sistemaVeterinario.controller;

import com.sistemaVeterinario.models.*;
import com.sistemaVeterinario.repository.*;
import com.sistemaVeterinario.service.*;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/citas")
@Tag(name = "Appointment Controller", description = "Controlador para la gestión completa de citas veterinarias por parte de los usuarios")
public class AppointmentController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MascotaService mascotaService;

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private CitaService citaService;

    @GetMapping("/agendar")
    @Operation(
            summary = "Mostrar formulario de agendamiento",
            description = "Presenta el formulario para agendar una nueva cita con las mascotas del usuario, servicios disponibles y fechas válidas para los próximos 14 días"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Formulario de agendamiento mostrado correctamente",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String mostrarFormularioCita(
            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model) {
        // Obtener usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioActual = usuarioService.findByEmail(email);

        // Obtener mascotas del usuario
        List<Mascota> mascotas = mascotaService.findByPropietario(usuarioActual);

        // Obtener servicios disponibles
        List<Servicio> servicios = servicioService.findAllActive();

        // Crear lista de fechas disponibles (2 semanas desde mañana)
        List<LocalDate> fechasDisponibles = generarFechasDisponibles();

        model.addAttribute("mascotas", mascotas);
        model.addAttribute("servicios", servicios);
        model.addAttribute("fechasDisponibles", fechasDisponibles);
        model.addAttribute("nuevaCita", new Cita());

        return "citas/agendar";
    }

    @GetMapping("/horarios/{fecha}")
    @ResponseBody
    @Operation(
            summary = "Obtener horarios disponibles",
            description = "Devuelve los horarios disponibles para una fecha específica en formato JSON. Los horarios van de 8:00 a 12:00 y de 13:00 a 17:00 en intervalos de 30 minutos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Horarios obtenidos exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Objeto con horas disponibles y su estado de disponibilidad")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Fecha inválida",
                    content = @Content(mediaType = "application/json")
            )
    })
    public Map<String, Object> obtenerHorariosDisponibles(
            @Parameter(
                    description = "Fecha para consultar horarios disponibles en formato ISO (YYYY-MM-DD)",
                    required = true,
                    example = "2025-06-15"
            )
            @PathVariable("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,

            @Parameter(
                    description = "ID del servicio para filtrar horarios específicos (opcional)",
                    required = false,
                    example = "1"
            )
            @RequestParam(required = false) Integer servicioId) {

        // Obtener horarios disponibles para la fecha seleccionada
        List<LocalTime> horariosDisponibles = citaService.obtenerHorariosDisponibles(fecha, servicioId);

        // Formatear horas para mostrar en la interfaz
        List<String> horasFormateadas = new ArrayList<>();
        Map<String, Boolean> disponibilidad = new HashMap<>();

        for (int i = 8; i < 12; i++) { // 8am a 12pm
            for (int m = 0; m < 60; m += 30) { // Intervalos de 30 minutos
                LocalTime hora = LocalTime.of(i, m);
                String horaStr = hora.toString();
                horasFormateadas.add(horaStr);
                disponibilidad.put(horaStr, horariosDisponibles.contains(hora));
            }
        }
        for (int i = 13; i < 17; i++) { // 1pm a 5pm
            for (int m = 0; m < 60; m += 30) { // Intervalos de 30 minutos
                LocalTime hora = LocalTime.of(i, m);
                String horaStr = hora.toString();
                horasFormateadas.add(horaStr);
                disponibilidad.put(horaStr, horariosDisponibles.contains(hora));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("horas", horasFormateadas);
        result.put("disponibilidad", disponibilidad);

        return result;
    }

    @PostMapping("/guardar")
    @Operation(
            summary = "Guardar nueva cita",
            description = "Procesa y guarda una nueva cita con la fecha, hora, mascota y servicio seleccionados. La cita se crea en estado 'Programada'"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Cita guardada exitosamente, redirige a mis citas con mensaje de éxito",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o horario no disponible",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String guardarCita(
            @Parameter(
                    description = "Objeto cita con información adicional",
                    schema = @Schema(implementation = Cita.class)
            )
            @ModelAttribute("nuevaCita") Cita cita,

            @Parameter(
                    description = "Fecha seleccionada para la cita en formato ISO",
                    required = true,
                    example = "2025-06-15"
            )
            @RequestParam("fechaSeleccionada") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,

            @Parameter(
                    description = "Hora seleccionada en formato HH:MM",
                    required = true,
                    example = "14:30"
            )
            @RequestParam("horaSeleccionada") String hora,

            @Parameter(
                    description = "ID de la mascota para la cita",
                    required = true,
                    example = "1"
            )
            @RequestParam("mascotaId") Integer mascotaId,

            @Parameter(
                    description = "ID del servicio veterinario",
                    required = true,
                    example = "2"
            )
            @RequestParam("servicioId") Integer servicioId) {

        // Construir fecha y hora completa
        String[] partesHora = hora.split(":");
        LocalTime horaLocal = LocalTime.of(Integer.parseInt(partesHora[0]), Integer.parseInt(partesHora[1]));
        LocalDateTime fechaHora = LocalDateTime.of(fecha, horaLocal);

        // Buscar mascota y servicio
        Mascota mascota = mascotaService.findById(mascotaId);
        Servicio servicio = servicioService.findById(servicioId);

        // Configurar cita
        cita.setMascota(mascota);
        cita.setServicio(servicio);
        cita.setFechaHora(fechaHora);
        cita.setEstado(Cita.EstadoCita.Programada);

        // Guardar cita
        citaService.save(cita);

        return "redirect:/citas/mis-citas?exito";
    }

    @GetMapping("/mis-citas")
    @Operation(
            summary = "Ver mis citas",
            description = "Muestra todas las citas del usuario autenticado organizadas por sus mascotas, incluyendo citas pasadas, presentes y futuras"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de citas mostrada correctamente",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String misCitas(
            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model) {
        // Obtener usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioActual = usuarioService.findByEmail(email);

        // Obtener citas del usuario a través de sus mascotas
        List<Cita> citas = citaService.findByPropietario(usuarioActual);
        List<Mascota> mascotas = mascotaService.findByPropietario(usuarioActual);

        model.addAttribute("citas", citas);
        model.addAttribute("mascotas", mascotas);

        return "citas/mis-citas";
    }

    @GetMapping("/cancelar/{id}")
    @Operation(
            summary = "Cancelar cita",
            description = "Cancela una cita específica cambiando su estado a 'Cancelada'. Solo el propietario de la mascota puede cancelar la cita"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Cita cancelada exitosamente, redirige con mensaje de confirmación",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "302",
                    description = "Error al cancelar - cita no encontrada o sin permisos, redirige con mensaje de error",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos para cancelar esta cita",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String cancelarCita(
            @Parameter(
                    description = "ID único de la cita a cancelar",
                    required = true,
                    example = "5"
            )
            @PathVariable("id") Integer idCita) {
        // Verificar que la cita pertenezca al usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioActual = usuarioService.findByEmail(email);

        Cita cita = citaService.findById(idCita);

        // Verificar que la cita exista y pertenezca al usuario
        if (cita != null && usuarioActual.isPresent() &&
                cita.getMascota().getPropietario().getIdUsuario().equals(usuarioActual.get().getIdUsuario())) {
            // Cambiar estado a cancelada
            cita.setEstado(Cita.EstadoCita.Cancelada);
            citaService.save(cita);
            return "redirect:/citas/mis-citas?cancelada";
        }

        return "redirect:/citas/mis-citas?error";
    }

    @GetMapping("/editar/{id}")
    @Operation(
            summary = "Mostrar formulario de edición",
            description = "Presenta el formulario para editar una cita existente. Solo se pueden editar citas en estado 'Programada' y que pertenezcan al usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Formulario de edición mostrado correctamente",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirige con error si la cita no existe, no pertenece al usuario o no es editable",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos para editar esta cita",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String mostrarFormularioEditar(
            @Parameter(
                    description = "ID único de la cita a editar",
                    required = true,
                    example = "3"
            )
            @PathVariable("id") Integer idCita,

            @Parameter(description = "Modelo para pasar datos a la vista", hidden = true)
            Model model) {
        // Verificar que la cita pertenezca al usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioActual = usuarioService.findByEmail(email);

        Cita cita = citaService.findById(idCita);

        // Verificar que la cita exista y pertenezca al usuario
        if (cita == null || !usuarioActual.isPresent() ||
                !cita.getMascota().getPropietario().getIdUsuario().equals(usuarioActual.get().getIdUsuario())) {
            return "redirect:/citas/mis-citas?error";
        }

        // Si la cita no está programada, no se puede editar
        if (cita.getEstado() != Cita.EstadoCita.Programada) {
            return "redirect:/citas/mis-citas?noeditable";
        }

        // Obtener mascotas del usuario
        List<Mascota> mascotas = mascotaService.findByPropietario(usuarioActual);

        // Obtener servicios disponibles
        List<Servicio> servicios = servicioService.findAllActive();

        // Crear lista de fechas disponibles (2 semanas desde mañana)
        List<LocalDate> fechasDisponibles = generarFechasDisponibles();

        model.addAttribute("mascotas", mascotas);
        model.addAttribute("servicios", servicios);
        model.addAttribute("fechasDisponibles", fechasDisponibles);
        model.addAttribute("cita", cita);

        return "citas/editar";
    }

    @PostMapping("/actualizar/{id}")
    @Operation(
            summary = "Actualizar cita existente",
            description = "Procesa la actualización de una cita existente con nueva fecha, hora, mascota y/o servicio. Solo se pueden actualizar citas en estado 'Programada'"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Cita actualizada exitosamente, redirige con mensaje de confirmación",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "302",
                    description = "Error en la actualización - cita no editable o sin permisos, redirige con mensaje de error",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de actualización inválidos",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos para actualizar esta cita",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String actualizarCita(
            @Parameter(
                    description = "ID único de la cita a actualizar",
                    required = true,
                    example = "3"
            )
            @PathVariable("id") Integer idCita,

            @Parameter(
                    description = "Nueva fecha para la cita en formato ISO",
                    required = true,
                    example = "2025-06-20"
            )
            @RequestParam("fechaSeleccionada") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,

            @Parameter(
                    description = "Nueva hora en formato HH:MM",
                    required = true,
                    example = "10:00"
            )
            @RequestParam("horaSeleccionada") String hora,

            @Parameter(
                    description = "ID de la mascota (puede ser diferente a la original)",
                    required = true,
                    example = "2"
            )
            @RequestParam("mascotaId") Integer mascotaId,

            @Parameter(
                    description = "ID del servicio (puede ser diferente al original)",
                    required = true,
                    example = "1"
            )
            @RequestParam("servicioId") Integer servicioId) {

        // Verificar que la cita pertenezca al usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Usuario> usuarioActual = usuarioService.findByEmail(email);

        Cita cita = citaService.findById(idCita);

        // Verificar que la cita exista y pertenezca al usuario
        if (cita == null || !usuarioActual.isPresent() ||
                !cita.getMascota().getPropietario().getIdUsuario().equals(usuarioActual.get().getIdUsuario())) {
            return "redirect:/citas/mis-citas?error";
        }

        // Si la cita no está programada, no se puede editar
        if (cita.getEstado() != Cita.EstadoCita.Programada) {
            return "redirect:/citas/mis-citas?noeditable";
        }

        // Construir fecha y hora completa
        String[] partesHora = hora.split(":");
        LocalTime horaLocal = LocalTime.of(Integer.parseInt(partesHora[0]), Integer.parseInt(partesHora[1]));
        LocalDateTime fechaHora = LocalDateTime.of(fecha, horaLocal);

        // Buscar mascota y servicio
        Mascota mascota = mascotaService.findById(mascotaId);
        Servicio servicio = servicioService.findById(servicioId);

        // Configurar cita
        cita.setMascota(mascota);
        cita.setServicio(servicio);
        cita.setFechaHora(fechaHora);

        // Guardar cita
        citaService.save(cita);

        return "redirect:/citas/mis-citas?actualizada";
    }

    /**
     * Metodo privado para generar fechas disponibles
     */
    private List<LocalDate> generarFechasDisponibles() {
        List<LocalDate> fechas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        LocalDate manana = hoy.plusDays(1);

        // Generar 14 días desde mañana
        for (int i = 0; i < 14; i++) {
            fechas.add(manana.plusDays(i));
        }

        return fechas;
    }
}