package com.sistemaVeterinario.service;

import com.sistemaVeterinario.models.Servicio;
import com.sistemaVeterinario.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para la gestión de servicios veterinarios.
 * Proporciona métodos para consultar servicios activos y buscar por ID.
 */
@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    /**
     * Obtiene todos los servicios veterinarios activos en el sistema.
     * @return Lista de servicios con estado activo (activo = true)
     */
    public List<Servicio> findAllActive() {
        return servicioRepository.findByActivoTrue();
    }

    /**
     * Busca un servicio veterinario por su ID.
     * @param id Identificador único del servicio
     * @return El servicio encontrado o null si no existe
     */
    public Servicio findById(Integer id) {
        return servicioRepository.findById(id).orElse(null);
    }
}