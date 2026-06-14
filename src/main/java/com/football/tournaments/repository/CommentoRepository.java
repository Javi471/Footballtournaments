package com.football.tournaments.repository;

import com.football.tournaments.model.Commento;
import com.football.tournaments.model.Partita;
import com.football.tournaments.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository: le dice a Spring que este archivo es un repositorio (acceso a BD)
// extends JpaRepository<Commento, Long>: hereda findAll(), findById(), save(), delete()...
@Repository
public interface CommentoRepository extends JpaRepository<Commento, Long> {

    // Spring genera: SELECT * FROM commento WHERE partita_id = ?
    // Devuelve todos los comentarios de un partido concreto
    List<Commento> findByPartita(Partita partita);

    // Spring genera: SELECT * FROM commento WHERE utente_id = ? ORDER BY data_creazione DESC
    // Devuelve todos los comentarios de un usuario, del más reciente al más antiguo
    List<Commento> findByUtenteOrderByDataCreazioneDesc(User utente);

    // Spring genera: SELECT * FROM commento WHERE id = ? AND utente_id = ?
    // Busca un comentario por id y verifica que pertenece al usuario actual
    // Usado en CommentoController para que un usuario solo pueda editar sus propios comentarios
    Optional<Commento> findByIdAndUtente(Long id, User utente);
}
