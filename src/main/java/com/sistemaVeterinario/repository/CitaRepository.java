package com.sistemaVeterinario.repository;

import com.sistemaVeterinario.models.Cita;
import com.sistemaVeterinario.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar las operaciones de base de datos relacionadas con las citas veterinarias.
 */
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    /**
     * Busca citas por el propietario de la mascota asociada.
     *
     * @param propietario El propietario de la mascota (opcional)
     * @return Lista de citas asociadas al propietario
     */
    List<Cita> findByMascotaPropietario(Optional<Usuario> propietario);

    /**
     * Busca citas por fecha y hora exactas.
     *
     * @param fechaHora La fecha y hora exacta de la cita
     * @return Lista de citas programadas para la fecha y hora especificada
     */
    List<Cita> findByFechaHora(LocalDateTime fechaHora);

    /**
     * Busca citas por estado y que tengan una fecha/hora anterior a la especificada.
     *
     * @param estado El estado de la cita a buscar
     * @param fechaHora La fecha/hora límite para la búsqueda
     * @return Lista de citas que cumplen con los criterios
     */
    List<Cita> findByEstadoAndFechaHoraBefore(Cita.EstadoCita estado, LocalDateTime fechaHora);

    /**
     * Busca citas dentro de un rango de fechas, excluyendo las canceladas.
     *
     * @param inicio Fecha/hora de inicio del rango
     * @param fin Fecha/hora de fin del rango
     * @return Lista de citas no canceladas dentro del rango especificado
     */
    @Query("SELECT c FROM Cita c WHERE c.fechaHora BETWEEN :inicio AND :fin AND c.estado != 'Cancelada'")
    List<Cita> findByFechaHoraBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * Busca citas por rango de fechas y ID de servicio, excluyendo las canceladas.
     *
     * @param inicio Fecha/hora de inicio del rango
     * @param fin Fecha/hora de fin del rango
     * @param servicioId ID del servicio a filtrar
     * @return Lista de citas no canceladas para el servicio en el rango especificado
     */
    @Query("SELECT c FROM Cita c WHERE c.fechaHora BETWEEN :inicio AND :fin " +
            "AND c.servicio.idServicio = :servicioId AND c.estado != 'Cancelada'")
    List<Cita> findByFechaHoraBetweenAndServicioIdServicio(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("servicioId") Integer servicioId);

    /**
     * Actualiza el estado de citas que cumplen con los criterios especificados.
     *
     * @param nuevoEstado Nuevo estado a asignar
     * @param estadoActual Estado actual que deben tener las citas
     * @param fechaReferencia Fecha límite para considerar las citas
     * @return Número de registros actualizados
     */
    @Modifying
    @Query("UPDATE Cita c SET c.estado = :nuevoEstado WHERE c.estado = :estadoActual AND c.fechaHora < :fechaReferencia")
    int actualizarCitasPasadas(@Param("nuevoEstado") Cita.EstadoCita nuevoEstado,
                               @Param("estadoActual") Cita.EstadoCita estadoActual,
                               @Param("fechaReferencia") LocalDateTime fechaReferencia);
}