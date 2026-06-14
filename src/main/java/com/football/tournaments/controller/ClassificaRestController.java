package com.football.tournaments.controller;

import com.football.tournaments.model.Torneo;
import com.football.tournaments.service.PartitaService;
import com.football.tournaments.service.TorneoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// @RestController: como @Controller pero devuelve JSON en vez de HTML
// Cada método devuelve datos directamente, no el nombre de una plantilla HTML
// Es el que usa React (classifica.jsx) para obtener los datos de la clasificación
@RestController
// @RequestMapping: prefijo base de todas las URLs de este controller
// Todas las rutas de aquí empiezan por /api/tornei
@RequestMapping("/api/tornei")
public class ClassificaRestController {

    // Spring inyecta los servicios automáticamente
    @Autowired private TorneoService torneoService;
    @Autowired private PartitaService partitaService;

    // GET /api/tornei/{id}/classifica → devuelve la clasificación del torneo como JSON
    // React llama a esta URL desde classifica.jsx con fetch("/api/tornei/4/classifica")
    // Spring convierte la lista automáticamente a JSON con Jackson
    @GetMapping("/{id}/classifica")
    public List<Map<String, Object>> getClassifica(@PathVariable Long id) {
        // Busca el torneo con sus equipos (necesario para calcolaClassifica)
        Torneo torneo = torneoService.findByIdWithSquadre(id)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        // Calcula y devuelve la lista de equipos con sus puntos, victorias, etc.
        return partitaService.calcolaClassifica(torneo);
    }

    // GET /api/tornei → devuelve la lista de todos los torneos como JSON
    // Usado por React en localhost:5173 (App.jsx) para mostrar el selector de torneos
    @GetMapping
    public List<Torneo> getTornei() {
        return torneoService.findAll();
    }
}
