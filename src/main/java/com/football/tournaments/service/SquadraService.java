package com.football.tournaments.service;

import com.football.tournaments.model.Squadra;
import com.football.tournaments.model.Torneo;
import com.football.tournaments.repository.SquadraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// @Service: le dice a Spring que esta clase contiene lógica de negocio
@Service
public class SquadraService {

    // @Autowired: Spring inyecta automáticamente el repositorio
    @Autowired
    private SquadraRepository squadraRepository;

    // Devuelve todos los equipos de la BD (sin paginación)
    @Transactional(readOnly = true)
    public List<Squadra> findAll() {
        return squadraRepository.findAll();
    }

    // Devuelve una página de equipos (paginación)
    // Page: objeto que contiene la lista de equipos + info de paginación (total, páginas...)
    // PageRequest.of(page, size, Sort.by("nome")): página número X, Y equipos por página, ordenados por nombre
    // Usado en SquadraController para mostrar los equipos de 4 en 4
    @Transactional(readOnly = true)
    public Page<Squadra> findPage(int page, int size) {
        return squadraRepository.findAll(PageRequest.of(page, size, Sort.by("nome")));
    }

    // Busca un equipo por su id — devuelve Optional porque puede no existir
    @Transactional(readOnly = true)
    public Optional<Squadra> findById(Long id) {
        return squadraRepository.findById(id);
    }

    // Busca un equipo Y carga también sus jugadores (giocatori) en la misma consulta
    // Usado en la página de detalle del equipo para mostrar la plantilla
    @Transactional(readOnly = true)
    public Optional<Squadra> findByIdWithGiocatori(Long id) {
        return squadraRepository.findByIdWithGiocatori(id);
    }

    // Busca todos los equipos que pertenecen a un torneo concreto
    // Consulta la tabla intermedia torneo_squadra
    @Transactional(readOnly = true)
    public List<Squadra> findByTorneo(Torneo torneo) {
        return squadraRepository.findByTorneo(torneo);
    }

    // Guarda o actualiza un equipo en la BD
    @Transactional
    public Squadra save(Squadra squadra) {
        return squadraRepository.save(squadra); // INSERT si es nuevo, UPDATE si ya existe
    }

    // Borra un equipo por su id
    @Transactional
    public void deleteById(Long id) {
        squadraRepository.deleteById(id);
    }
}
