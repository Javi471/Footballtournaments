package com.football.tournaments.service;

import com.football.tournaments.model.Arbitro;
import com.football.tournaments.repository.ArbitroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// @Service: le dice a Spring que esta clase contiene lógica de negocio
@Service
public class ArbitroService {

    // @Autowired: Spring inyecta automáticamente el repositorio
    @Autowired
    private ArbitroRepository arbitroRepository;

    // Devuelve todos los árbitros de la BD
    // Usado en ArbitroController y en PartitaController (para el formulario de crear partido)
    @Transactional(readOnly = true)
    public List<Arbitro> findAll() {
        return arbitroRepository.findAll();
    }

    // Busca un árbitro por su id — Optional porque puede no existir
    @Transactional(readOnly = true)
    public Optional<Arbitro> findById(Long id) {
        return arbitroRepository.findById(id);
    }

    // Guarda o actualiza un árbitro en la BD
    @Transactional
    public Arbitro save(Arbitro arbitro) {
        return arbitroRepository.save(arbitro);
    }

    // Borra un árbitro por su id
    @Transactional
    public void deleteById(Long id) {
        arbitroRepository.deleteById(id);
    }
}
