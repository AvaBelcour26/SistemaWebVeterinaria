package com.sistemaVeterinario.service;

import com.sistemaVeterinario.models.Mascota;
import com.sistemaVeterinario.models.Usuario;
import com.sistemaVeterinario.repository.MascotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de mascotas en el sistema veterinario.
 * Proporciona operaciones CRUD básicas y búsquedas específicas.
 */
@Service
public class MascotaService {

    @Autowired
    private MascotaRepository mascotaRepository;

    /**
     * Busca mascotas por propietario.
     * @param propietario El propietario de las mascotas (como Optional)
     * @return Lista de mascotas del propietario. Lista vacía si el Optional está vacío.
     */
    public List<Mascota> findByPropietario(Optional<Usuario> propietario) {
        if (!propietario.isPresent()) {
            return new ArrayList<>();
        }
        return mascotaRepository.findByPropietario(propietario.get());
    }

    /**
     * Busca una mascota por su ID.
     * @param id El ID de la mascota a buscar
     * @return La mascota encontrada o null si no existe
     */
    public Mascota findById(Integer id) {
        return mascotaRepository.findById(id).orElse(null);
    }

    /**
     * Guarda o actualiza una mascota en la base de datos.
     * @param mascota La entidad Mascota a guardar
     * @return La mascota guardada/actualizada
     */
    public Mascota save(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }

    /**
     * Elimina una mascota por su ID.
     * @param id El ID de la mascota a eliminar
     */
    public void delete(Integer id) {
        mascotaRepository.deleteById(id);
    }

    /**
     * Obtiene todas las mascotas registradas en el sistema.
     * @return Lista completa de mascotas
     */
    public List<Mascota> findAll() {
        return mascotaRepository.findAll();
    }
}