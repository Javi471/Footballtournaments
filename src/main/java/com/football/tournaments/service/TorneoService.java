package com.football.tournaments.service;

import com.football.tournaments.model.*;
import com.football.tournaments.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// @Service: le dice a Spring que esta clase contiene lógica de negocio
// Spring la crea automáticamente y la inyecta donde se necesite con @Autowired
@Service
public class TorneoService {

    // @Autowired: Spring inyecta automáticamente el repositorio — no necesitas hacer "new"
    @Autowired
    private TorneoRepository torneoRepository;

    // También necesitamos el repositorio de equipos para la función aggiornaSquadre
    @Autowired
    private SquadraRepository squadraRepository;

    // @Transactional(readOnly = true): abre una transacción de solo lectura
    // readOnly = true es una optimización: PostgreSQL sabe que no va a modificar nada
    @Transactional(readOnly = true)
    public List<Torneo> findAll() {
        return torneoRepository.findAll(); // devuelve todos los torneos de la BD
    }

    // Busca un torneo por su id — devuelve Optional porque puede no existir
    @Transactional(readOnly = true)
    public Optional<Torneo> findById(Long id) {
        return torneoRepository.findById(id);
    }

    // Busca un torneo y carga también sus equipos (squadre) en la misma consulta
    // Usado cuando necesitas mostrar los equipos de un torneo
    @Transactional(readOnly = true)
    public Optional<Torneo> findByIdWithSquadre(Long id) {
        return torneoRepository.findByIdWithSquadre(id);
    }

    // Busca un torneo y carga también sus partidos (partite) en la misma consulta
    // Usado cuando necesitas mostrar los partidos de un torneo
    @Transactional(readOnly = true)
    public Optional<Torneo> findByIdWithPartite(Long id) {
        return torneoRepository.findByIdWithPartite(id);
    }

    // @Transactional: abre una transacción de escritura
    // Si algo falla, la transacción se deshace automáticamente (rollback)
    @Transactional
    public Torneo save(Torneo torneo) {
        return torneoRepository.save(torneo); // INSERT si es nuevo, UPDATE si ya existe
    }

    // Actualiza los equipos de un torneo
    // Recibe el id del torneo y una lista de ids de equipos
    // Busca el torneo con sus equipos, sustituye la lista y guarda
    @Transactional
    public Torneo aggiornaSquadre(Long torneoId, List<Long> squadraIds) {
        Torneo torneo = torneoRepository.findByIdWithSquadre(torneoId)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato")); // lanza error si no existe
        List<Squadra> squadre = squadraRepository.findAllById(squadraIds); // busca los equipos por ids
        torneo.setSquadre(squadre); // reemplaza la lista de equipos
        return torneoRepository.save(torneo); // guarda el torneo actualizado en BD
    }

    // Borra un torneo y todo lo relacionado (CASCADE en la BD)
    @Transactional
    public void deleteById(Long id) {
        torneoRepository.deleteById(id);
    }
}
