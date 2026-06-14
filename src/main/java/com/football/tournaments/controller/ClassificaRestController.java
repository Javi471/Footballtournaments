package com.football.tournaments.controller;

import com.football.tournaments.model.Torneo;
import com.football.tournaments.service.PartitaService;
import com.football.tournaments.service.TorneoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Devuelve JSON en vez de HTML. Es la API que consume React (classifica.jsx)
@RestController
@RequestMapping("/api/tornei")
public class ClassificaRestController {

    @Autowired private TorneoService torneoService;
    @Autowired private PartitaService partitaService;

    // GET /api/tornei/{id}/classifica → clasificación del torneo en JSON
    @GetMapping("/{id}/classifica")
    public List<Map<String, Object>> getClassifica(@PathVariable Long id) {
        Torneo torneo = torneoService.findByIdWithSquadre(id)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        return partitaService.calcolaClassifica(torneo);
    }

    // GET /api/tornei → lista de todos los torneos en JSON
    @GetMapping
    public List<Torneo> getTornei() {
        return torneoService.findAll();
    }
}
