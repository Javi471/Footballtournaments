package com.football.tournaments.service;

import com.football.tournaments.model.*;
import com.football.tournaments.repository.PartitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PartitaService {

    @Autowired
    private PartitaRepository partitaRepository;

    // Partidos de un torneo, con equipos y árbitro ya cargados
    @Transactional(readOnly = true)
    public List<Partita> findByTorneo(Torneo torneo) {
        return partitaRepository.findByTorneoWithTeamsAndReferee(torneo);
    }

    @Transactional(readOnly = true)
    public Optional<Partita> findByIdWithDetails(Long id) {
        return partitaRepository.findByIdWithDetails(id);
    }

    @Transactional(readOnly = true)
    public Optional<Partita> findByIdWithCommenti(Long id) {
        return partitaRepository.findByIdWithCommenti(id);
    }

    @Transactional
    public Partita save(Partita partita) {
        return partitaRepository.save(partita);
    }

    // Guarda los goles y marca el partido como jugado
    @Transactional
    public Partita registraRisultato(Long partitaId, int goalsHome, int goalsAway) {
        Partita partita = partitaRepository.findById(partitaId)
            .orElseThrow(() -> new RuntimeException("Partita non trovata"));
        partita.setGoalsHome(goalsHome);
        partita.setGoalsAway(goalsAway);
        partita.setStato(StatoPartita.PLAYED);
        return partitaRepository.save(partita);
    }

    @Transactional
    public void deleteById(Long id) {
        partitaRepository.deleteById(id);
    }

    // Calcula la clasificación: 3 puntos por victoria, 1 por empate, 0 por derrota
    @Transactional(readOnly = true)
    public List<Map<String, Object>> calcolaClassifica(Torneo torneo) {

        // Solo cuentan los partidos jugados
        List<Partita> partite = partitaRepository.findByTorneoAndStato(torneo, StatoPartita.PLAYED);

        // Una fila por equipo (la clave es el id del equipo)
        Map<Long, Map<String, Object>> classifica = new LinkedHashMap<>();

        // Empieza todos los contadores a 0
        for (Squadra s : torneo.getSquadre()) {
            Map<String, Object> riga = new LinkedHashMap<>();
            riga.put("squadra", s);
            riga.put("punti", 0);
            riga.put("vittorie", 0);
            riga.put("pareggi", 0);
            riga.put("sconfitte", 0);
            riga.put("golFatti", 0);
            riga.put("golSubiti", 0);
            riga.put("partiteGiocate", 0);
            classifica.put(s.getId(), riga);
        }

        // Por cada partido actualiza a los dos equipos
        for (Partita p : partite) {
            Long homeId = p.getSquadraHome().getId();
            Long awayId = p.getSquadraAway().getId();
            int gh = p.getGoalsHome();
            int ga = p.getGoalsAway();

            if (classifica.containsKey(homeId)) {
                aggiornaDati(classifica.get(homeId), gh, ga);
            }
            // Para el visitante los goles van al revés
            if (classifica.containsKey(awayId)) {
                aggiornaDati(classifica.get(awayId), ga, gh);
            }
        }

        // Ordena por puntos y, si empatan, por diferencia de goles
        List<Map<String, Object>> risultato = new ArrayList<>(classifica.values());
        risultato.sort((a, b) -> {
            int pA = (int) a.get("punti");
            int pB = (int) b.get("punti");
            if (pA != pB) return pB - pA;
            int diffA = (int) a.get("golFatti") - (int) a.get("golSubiti");
            int diffB = (int) b.get("golFatti") - (int) b.get("golSubiti");
            return diffB - diffA;
        });
        return risultato;
    }

    // Suma a un equipo el resultado de un partido (golFatti = metidos, golSubiti = encajados)
    private void aggiornaDati(Map<String, Object> riga, int golFatti, int golSubiti) {
        riga.put("golFatti",       (int) riga.get("golFatti")       + golFatti);
        riga.put("golSubiti",      (int) riga.get("golSubiti")      + golSubiti);
        riga.put("partiteGiocate", (int) riga.get("partiteGiocate") + 1);
        if (golFatti > golSubiti) {
            riga.put("vittorie", (int) riga.get("vittorie") + 1);
            riga.put("punti",    (int) riga.get("punti")    + 3);
        } else if (golFatti == golSubiti) {
            riga.put("pareggi", (int) riga.get("pareggi") + 1);
            riga.put("punti",   (int) riga.get("punti")   + 1);
        } else {
            riga.put("sconfitte", (int) riga.get("sconfitte") + 1);
        }
    }
}
