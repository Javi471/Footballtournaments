package com.football.tournaments.repository;

import com.football.tournaments.model.Partita;
import com.football.tournaments.model.StatoPartita;
import com.football.tournaments.model.Torneo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository: le dice a Spring que este archivo es un repositorio (acceso a BD)
// extends JpaRepository<Partita, Long>: hereda findAll(), findById(), save(), delete()...
@Repository
public interface PartitaRepository extends JpaRepository<Partita, Long> {

    // Spring genera el SQL por el nombre: findBy + Torneo + OrderBy + DataOra + Asc
    // → SELECT * FROM partita WHERE torneo_id = ? ORDER BY data_ora ASC
    List<Partita> findByTorneoOrderByDataOraAsc(Torneo torneo);

    // Spring genera: SELECT * FROM partita WHERE torneo_id = ? AND stato = ?
    // Usado en calcolaClassifica para obtener solo los partidos PLAYED
    List<Partita> findByTorneoAndStato(Torneo torneo, StatoPartita stato);

    // @Query propio: carga el partido junto a equipo local, visitante y árbitro en una sola consulta
    // JOIN FETCH: evita el problema N+1 (no lanza una query extra por cada partido)
    // Ordenado por fecha ascendente
    @Query("SELECT p FROM Partita p JOIN FETCH p.squadraHome JOIN FETCH p.squadraAway JOIN FETCH p.arbitro WHERE p.torneo = :torneo ORDER BY p.dataOra ASC")
    List<Partita> findByTorneoWithTeamsAndReferee(Torneo torneo);

    // Busca un partido por id y carga todos sus datos relacionados en una sola consulta
    // Usado en PartitaController para mostrar el detalle de un partido
    @Query("SELECT p FROM Partita p JOIN FETCH p.squadraHome JOIN FETCH p.squadraAway JOIN FETCH p.arbitro JOIN FETCH p.torneo WHERE p.id = :id")
    Optional<Partita> findByIdWithDetails(Long id);

    // Busca un partido con todos sus comentarios y el usuario de cada comentario
    // Usado para mostrar los comentarios en la página de detalle del partido
    @Query("SELECT p FROM Partita p JOIN FETCH p.commenti c JOIN FETCH c.utente WHERE p.id = :id")
    Optional<Partita> findByIdWithCommenti(Long id);

    // ── Métodos usados solo en el análisis experimental de rendimiento (test PerformanceAnalysisTest) ──

    // Estrategia 1: LAZY puro — no carga ninguna relación, genera N+1 queries al primer acceso
    @Query("SELECT p FROM Partita p WHERE p.torneo = :torneo ORDER BY p.dataOra ASC")
    List<Partita> findByTorneoLazy(@Param("torneo") Torneo torneo);

    // Estrategia 3: EntityGraph — equivalente a JOIN FETCH pero declarativo
    // attributePaths: las relaciones que se quieren cargar junto al partido
    @EntityGraph(attributePaths = {"squadraHome", "squadraAway", "arbitro"})
    @Query("SELECT p FROM Partita p WHERE p.torneo = :torneo ORDER BY p.dataOra ASC")
    List<Partita> findByTorneoWithEntityGraph(@Param("torneo") Torneo torneo);
}
