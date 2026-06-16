package com.football.tournaments.repository;

import com.football.tournaments.model.Squadra;
import com.football.tournaments.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository: le dice a Spring que este archivo es un repositorio (acceso a BD)
// extends JpaRepository<Squadra, Long>: hereda findAll(), findById(), save(), delete()...
@Repository
public interface SquadraRepository extends JpaRepository<Squadra, Long> {

    // Spring genera: SELECT * FROM squadra WHERE citta = ?
    // Usado para buscar todos los equipos de una ciudad concreta
    List<Squadra> findByCitta(String citta);

    // @Query propio: carga el equipo junto a sus jugadores en una sola consulta
    // LEFT JOIN FETCH: evita el problema N+1 (no lanza una query extra por cada jugador)
    @Query("SELECT s FROM Squadra s LEFT JOIN FETCH s.giocatori WHERE s.id = :id")
    Optional<Squadra> findByIdWithGiocatori(Long id);

    // Busca todos los equipos que participan en un torneo concreto
    // MEMBER OF: comprueba si el torneo está en la lista de torneos del equipo
    @Query("SELECT s FROM Squadra s WHERE :torneo MEMBER OF s.tornei")
    List<Squadra> findByTorneo(Torneo torneo);
}
