package com.sistemaVeterinario.Scheduler;

import com.sistemaVeterinario.service.CitaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Componente programado para ejecutar tareas automáticas relacionadas con citas veterinarias.
 *
 * <p>Realiza operaciones periódicas de mantenimiento sobre el estado de las citas.</p>
 *
 * <p>Anotado con {@link Component} para ser detectado por el contenedor de Spring.</p>
 */
@Component
public class CitaScheduler {

    // Registro de eventos (log)
    private static final Logger logger = LoggerFactory.getLogger(CitaScheduler.class);

    // Conexión al servicio de citas
    @Autowired
    private CitaService citaActualizacionService;

    /**
     * Tarea automática que se ejecuta cada 30 minutos.
     * Busca citas pasadas y las marca como "Completadas".
     * Muestra en el log cuántas citas se actualizaron.
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // Cada 30 minutos
    public void actualizarEstadoCitas() {
        logger.info("Comenzando actualización de citas...");

        int citasActualizadas = citaActualizacionService.actualizarCitasPasadas();

        logger.info("Listo! Se actualizaron {} citas.", citasActualizadas);
    }
}