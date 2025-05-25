package com.sistemaVeterinario.service;

import com.sistemaVeterinario.models.Cita;
import com.sistemaVeterinario.models.Usuario;
import com.sistemaVeterinario.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de citas veterinarias.
 * Proporciona operaciones CRUD básicas y lógica de negocio para citas.
 */
@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    /**
     * Guarda una cita en la base de datos.
     * @param cita La entidad Cita a guardar
     * @return La cita guardada
     */
    public Cita save(Cita cita) {
        return citaRepository.save(cita);
    }

    /**
     * Busca una cita por su ID.
     * @param id El ID de la cita
     * @return La cita encontrada o null si no existe
     */
    public Cita findById(Integer id) {
        return citaRepository.findById(id).orElse(null);
    }

    /**
     * Obtiene las citas asociadas a un propietario específico.
     * @param propietario El propietario de las mascotas (opcional)
     * @return Lista de citas del propietario
     */
    public List<Cita> findByPropietario(Optional<Usuario> propietario) {
        return citaRepository.findByMascotaPropietario(propietario);
    }

    /**
     * Obtiene los horarios disponibles para agendar citas en una fecha específica.
     * @param fecha La fecha para consultar disponibilidad
     * @param servicioId El ID del servicio (opcional para filtrar por servicio)
     * @return Lista de horarios disponibles
     */
    public List<LocalTime> obtenerHorariosDisponibles(LocalDate fecha, Integer servicioId) {
        // Generar todos los horarios posibles (cada 30 minutos de 8:00 a 17:00)
        List<LocalTime> todosLosHorarios = new ArrayList<>();
        for (int hora = 8; hora < 17; hora++) {
            todosLosHorarios.add(LocalTime.of(hora, 0));
            todosLosHorarios.add(LocalTime.of(hora, 30));
        }

        // Obtiene el rango completo del día
        LocalDateTime inicioDia = LocalDateTime.of(fecha, LocalTime.of(0, 0));
        LocalDateTime finDia = LocalDateTime.of(fecha, LocalTime.of(23, 59));

        // Consulta citas existentes
        List<Cita> citasProgramadas;
        if (servicioId != null) {
            citasProgramadas = citaRepository.findByFechaHoraBetweenAndServicioIdServicio(inicioDia, finDia, servicioId);
        } else {
            citasProgramadas = citaRepository.findByFechaHoraBetween(inicioDia, finDia);
        }

        // Extrae horarios ocupados
        List<LocalTime> horariosOcupados = citasProgramadas.stream()
                .map(cita -> cita.getFechaHora().toLocalTime())
                .collect(Collectors.toList());

        // Filtra horarios disponibles
        return todosLosHorarios.stream()
                .filter(horario -> !horariosOcupados.contains(horario))
                .collect(Collectors.toList());
    }

    /**
     * Actualiza automáticamente el estado de citas pasadas a "Completada".
     * @return Número de citas actualizadas
     */
    @Transactional
    public int actualizarCitasPasadas() {
        LocalDateTime ahora = LocalDateTime.now();

        return citaRepository.actualizarCitasPasadas(
                Cita.EstadoCita.Completada,
                Cita.EstadoCita.Programada,
                ahora);
    }
}