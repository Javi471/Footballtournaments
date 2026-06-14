package com.football.tournaments.repository;

import com.football.tournaments.model.Giocatore;
import com.football.tournaments.model.Squadra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// @Repository: le dice a Spring que este archivo es un repositorio (acceso a BD)
// extends JpaRepository<Giocatore, Long>: hereda findAll(), findById(), save(), delete()...
@Repository
public interface GiocatoreRepository extends JpaRepository<Giocatore, Long> {

    // Spring genera: SELECT * FROM giocatore WHERE squadra_id = ?
    // Usado para mostrar los jugadores de un equipo concreto
    List<Giocatore> findBySquadra(Squadra squadra);

    // Spring genera: SELECT * FROM giocatore WHERE ruolo = ?
    // Por ejemplo: findByRuolo("Portiere") → todos los porteros
    List<Giocatore> findByRuolo(String ruolo);

    // Busca jugadores cuyo nombre O apellido contenga el texto buscado, ignorando mayúsculas
    // Spring genera: SELECT * FROM giocatore WHERE LOWER(nome) LIKE ? OR LOWER(cognome) LIKE ?
    // Usado para el buscador de jugadores
    List<Giocatore> findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(String nome, String cognome);
}
