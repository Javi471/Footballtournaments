package com.football.tournaments.service;

import com.football.tournaments.model.*;
import com.football.tournaments.repository.GiocatoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// @Service: le dice a Spring que esta clase contiene lógica de negocio
@Service
public class GiocatoreService {

    // @Autowired: Spring inyecta automáticamente el repositorio
    @Autowired
    private GiocatoreRepository giocatoreRepository;

    // Devuelve todos los jugadores de la BD
    @Transactional(readOnly = true)
    public List<Giocatore> findAll() {
        return giocatoreRepository.findAll();
    }

    // Busca un jugador por su id — Optional porque puede no existir
    @Transactional(readOnly = true)
    public Optional<Giocatore> findById(Long id) {
        return giocatoreRepository.findById(id);
    }

    // Devuelve todos los jugadores de un equipo concreto
    // Usado para mostrar la plantilla en la página de detalle del equipo
    @Transactional(readOnly = true)
    public List<Giocatore> findBySquadra(Squadra squadra) {
        return giocatoreRepository.findBySquadra(squadra);
    }

    // Busca jugadores cuyo nombre O apellido contenga el texto (sin distinguir mayúsculas)
    // El mismo texto "query" se pasa dos veces porque el método del repositorio
    // busca tanto por nombre como por apellido
    @Transactional(readOnly = true)
    public List<Giocatore> cerca(String query) {
        return giocatoreRepository.findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(query, query);
    }

    // Guarda o actualiza un jugador en la BD
    @Transactional
    public Giocatore save(Giocatore giocatore) {
        return giocatoreRepository.save(giocatore);
    }

    // Borra un jugador por su id
    @Transactional
    public void deleteById(Long id) {
        giocatoreRepository.deleteById(id);
    }
}
