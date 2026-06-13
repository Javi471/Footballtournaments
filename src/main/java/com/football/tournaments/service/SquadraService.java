package com.football.tournaments.service;

import com.football.tournaments.model.Squadra;
import com.football.tournaments.model.Torneo;
import com.football.tournaments.repository.SquadraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SquadraService {

    @Autowired
    private SquadraRepository squadraRepository;

    @Transactional(readOnly = true)
    public List<Squadra> findAll() {
        return squadraRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Squadra> findPage(int page, int size) {
        return squadraRepository.findAll(PageRequest.of(page, size, Sort.by("nome")));
    }

    @Transactional(readOnly = true)
    public Optional<Squadra> findById(Long id) {
        return squadraRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Squadra> findByIdWithGiocatori(Long id) {
        return squadraRepository.findByIdWithGiocatori(id);
    }

    @Transactional(readOnly = true)
    public List<Squadra> findByTorneo(Torneo torneo) {
        return squadraRepository.findByTorneo(torneo);
    }

    @Transactional
    public Squadra save(Squadra squadra) {
        return squadraRepository.save(squadra);
    }

    @Transactional
    public void deleteById(Long id) {
        squadraRepository.deleteById(id);
    }
}
