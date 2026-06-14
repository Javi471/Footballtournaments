package com.football.tournaments.service;

import com.football.tournaments.model.*;
import com.football.tournaments.repository.PartitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

// @Service: le dice a Spring que esta clase contiene lógica de negocio
@Service
public class PartitaService {

    // @Autowired: Spring inyecta automáticamente el repositorio
    @Autowired
    private PartitaRepository partitaRepository;

    // Devuelve todos los partidos de un torneo, con equipos y árbitro ya cargados
    @Transactional(readOnly = true)
    public List<Partita> findByTorneo(Torneo torneo) {
        return partitaRepository.findByTorneoWithTeamsAndReferee(torneo);
    }

    // Busca un partido con todos sus datos (equipos, árbitro, torneo) por su id
    @Transactional(readOnly = true)
    public Optional<Partita> findByIdWithDetails(Long id) {
        return partitaRepository.findByIdWithDetails(id);
    }

    // Busca un partido con todos sus comentarios (y el usuario de cada comentario)
    @Transactional(readOnly = true)
    public Optional<Partita> findByIdWithCommenti(Long id) {
        return partitaRepository.findByIdWithCommenti(id);
    }

    // Guarda o actualiza un partido en la BD
    @Transactional
    public Partita save(Partita partita) {
        return partitaRepository.save(partita);
    }

    // Registra el resultado de un partido:
    // - guarda los goles de local y visitante
    // - cambia el estado de SCHEDULED a PLAYED
    @Transactional
    public Partita registraRisultato(Long partitaId, int goalsHome, int goalsAway) {
        Partita partita = partitaRepository.findById(partitaId)
            .orElseThrow(() -> new RuntimeException("Partita non trovata")); // error si no existe
        partita.setGoalsHome(goalsHome);   // goles del equipo local
        partita.setGoalsAway(goalsAway);   // goles del equipo visitante
        partita.setStato(StatoPartita.PLAYED); // marca el partido como jugado
        return partitaRepository.save(partita); // guarda en BD
    }

    // Borra un partido por su id
    @Transactional
    public void deleteById(Long id) {
        partitaRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // calcolaClassifica: calcula la clasificación del torneo
    // Recorre todos los partidos PLAYED y acumula puntos, goles, victorias...
    // 3 puntos por victoria, 1 por empate, 0 por derrota
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<Map<String, Object>> calcolaClassifica(Torneo torneo) {

        // Solo los partidos ya jugados (PLAYED) cuentan para la clasificación
        List<Partita> partite = partitaRepository.findByTorneoAndStato(torneo, StatoPartita.PLAYED);

        // Mapa donde la clave es el id del equipo y el valor es su fila en la clasificación
        // LinkedHashMap mantiene el orden de inserción
        Map<Long, Map<String, Object>> classifica = new LinkedHashMap<>();

        // Inicializa la fila de cada equipo con todos los contadores a 0
        for (Squadra s : torneo.getSquadre()) {
            Map<String, Object> riga = new LinkedHashMap<>();
            riga.put("squadra", s);            // objeto Squadra (nombre, ciudad...)
            riga.put("punti", 0);              // puntos totales
            riga.put("vittorie", 0);           // victorias
            riga.put("pareggi", 0);            // empates
            riga.put("sconfitte", 0);          // derrotas
            riga.put("golFatti", 0);           // goles a favor
            riga.put("golSubiti", 0);          // goles en contra
            riga.put("partiteGiocate", 0);     // partidos jugados
            classifica.put(s.getId(), riga);
        }

        // Recorre cada partido y actualiza los datos de ambos equipos
        for (Partita p : partite) {
            Long homeId = p.getSquadraHome().getId(); // id del equipo local
            Long awayId = p.getSquadraAway().getId(); // id del equipo visitante
            int gh = p.getGoalsHome(); // goles del local
            int ga = p.getGoalsAway(); // goles del visitante

            // Actualiza el equipo local (para él: golFatti=gh, golSubiti=ga)
            if (classifica.containsKey(homeId)) {
                aggiornaDati(classifica.get(homeId), gh, ga);
            }
            // Actualiza el equipo visitante (para él: golFatti=ga, golSubiti=gh — invertidos)
            if (classifica.containsKey(awayId)) {
                aggiornaDati(classifica.get(awayId), ga, gh);
            }
        }

        // Convierte el mapa a lista y la ordena:
        // 1º por puntos (mayor primero)
        // 2º por diferencia de goles si hay empate a puntos
        List<Map<String, Object>> risultato = new ArrayList<>(classifica.values());
        risultato.sort((a, b) -> {
            int pA = (int) a.get("punti");
            int pB = (int) b.get("punti");
            if (pA != pB) return pB - pA; // más puntos primero
            int diffA = (int) a.get("golFatti") - (int) a.get("golSubiti"); // diferencia de goles A
            int diffB = (int) b.get("golFatti") - (int) b.get("golSubiti"); // diferencia de goles B
            return diffB - diffA; // mejor diferencia de goles primero
        });
        return risultato;
    }

    // Método auxiliar: actualiza los datos de un equipo con el resultado de un partido
    // golFatti: goles que metió este equipo en este partido
    // golSubiti: goles que encajó este equipo en este partido
    private void aggiornaDati(Map<String, Object> riga, int golFatti, int golSubiti) {
        riga.put("golFatti",       (int) riga.get("golFatti")       + golFatti);
        riga.put("golSubiti",      (int) riga.get("golSubiti")      + golSubiti);
        riga.put("partiteGiocate", (int) riga.get("partiteGiocate") + 1); // suma 1 partido jugado
        if (golFatti > golSubiti) {
            // Victoria: +3 puntos
            riga.put("vittorie", (int) riga.get("vittorie") + 1);
            riga.put("punti",    (int) riga.get("punti")    + 3);
        } else if (golFatti == golSubiti) {
            // Empate: +1 punto
            riga.put("pareggi", (int) riga.get("pareggi") + 1);
            riga.put("punti",   (int) riga.get("punti")   + 1);
        } else {
            // Derrota: +0 puntos, solo suma la derrota
            riga.put("sconfitte", (int) riga.get("sconfitte") + 1);
        }
    }
}
