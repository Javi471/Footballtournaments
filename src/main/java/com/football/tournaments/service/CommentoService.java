package com.football.tournaments.service;

import com.football.tournaments.model.Commento;
import com.football.tournaments.model.Partita;
import com.football.tournaments.model.User;
import com.football.tournaments.repository.CommentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// @Service: le dice a Spring que esta clase contiene lógica de negocio
@Service
public class CommentoService {

    // @Autowired: Spring inyecta automáticamente el repositorio
    @Autowired
    private CommentoRepository commentoRepository;

    // Devuelve todos los comentarios de un partido concreto
    // Usado en PartitaController para mostrar los comentarios en el detalle del partido
    @Transactional(readOnly = true)
    public List<Commento> findByPartita(Partita partita) {
        return commentoRepository.findByPartita(partita);
    }

    // Busca un comentario por id y verifica que pertenece al usuario actual
    // Si el comentario no es del usuario, devuelve Optional vacío
    // Usado para que solo el autor pueda editar su propio comentario
    @Transactional(readOnly = true)
    public Optional<Commento> findByIdAndUtente(Long id, User utente) {
        return commentoRepository.findByIdAndUtente(id, utente);
    }

    // Busca un comentario por su id (sin verificar el usuario)
    @Transactional(readOnly = true)
    public Optional<Commento> findById(Long id) {
        return commentoRepository.findById(id);
    }

    // Guarda o actualiza un comentario en la BD
    @Transactional
    public Commento save(Commento commento) {
        return commentoRepository.save(commento);
    }

    // Borra un comentario por su id
    @Transactional
    public void deleteById(Long id) {
        commentoRepository.deleteById(id);
    }
}
