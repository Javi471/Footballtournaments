package com.football.tournaments.repository;

import com.football.tournaments.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// @Repository: le dice a Spring que este archivo es un repositorio (acceso a BD)
// extends JpaRepository<Torneo, Long>: hereda findAll(), findById(), save(), delete()...
//   - Torneo: la entidad que gestiona
//   - Long: el tipo del id (columna primaria)
@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long> {

    // Spring genera el SQL automáticamente leyendo el nombre del método:
    // findBy + Anno + OrderBy + Nome + Asc
    // → SELECT * FROM torneo WHERE anno = ? ORDER BY nome ASC
    List<Torneo> findByAnnoOrderByNomeAsc(Integer anno);

    // @Query: SQL propio porque necesita cargar los equipos (squadre) junto al torneo
    // LEFT JOIN FETCH: trae el torneo Y sus equipos en una sola consulta (evita N+1)
    // :id es el parámetro que se pasa al llamar al método
    // Optional: devuelve el torneo si existe, o vacío si no existe
    @Query("SELECT t FROM Torneo t LEFT JOIN FETCH t.squadre WHERE t.id = :id")
    Optional<Torneo> findByIdWithSquadre(Long id);

    // Igual que el anterior pero trae los partidos (partite) en vez de los equipos
    // Usado cuando necesitas ver los partidos de un torneo
    @Query("SELECT t FROM Torneo t LEFT JOIN FETCH t.partite WHERE t.id = :id")
    Optional<Torneo> findByIdWithPartite(Long id);

    // @Modifying: indica que esta query modifica datos (DELETE en vez de SELECT)
    // @Transactional: necesario para operaciones de escritura
    // nativeQuery = true: SQL nativo de PostgreSQL (no JPQL)
    // Borra todas las filas de la tabla intermedia torneo_squadra
    // Se usa en DataInitializer para limpiar los datos de prueba antes de reiniciarlos
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM torneo_squadra", nativeQuery = true)
    void clearAllLinks();
}
