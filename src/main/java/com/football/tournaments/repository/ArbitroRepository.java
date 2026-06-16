package com.football.tournaments.repository;

import com.football.tournaments.model.Arbitro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository: le dice a Spring que este archivo es un repositorio (acceso a BD)
// extends JpaRepository<Arbitro, Long>: hereda findAll(), findById(), save(), delete()...

@Repository
public interface ArbitroRepository extends JpaRepository<Arbitro, Long> {
    Optional<Arbitro> findByCodiceArbitrale(String codiceArbitrale);
}
