package com.football.tournaments.controller;

import com.football.tournaments.model.*;
import com.football.tournaments.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TorneoController {

    @Autowired private TorneoService torneoService;
    @Autowired private SquadraService squadraService;
    @Autowired private PartitaService partitaService;

    // ── Rutas públicas ──

    @GetMapping("/tornei")
    public String lista(Model model) {
        model.addAttribute("tornei", torneoService.findAll());
        return "torneo/lista";
    }

    @GetMapping("/tornei/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        Torneo torneo = torneoService.findByIdWithSquadre(id)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        List<Partita> all = partitaService.findByTorneo(torneo);

        // Separo los partidos jugados de los que aún están programados
        List<Partita> risultati = new ArrayList<>();
        List<Partita> prossime  = new ArrayList<>();
        for (Partita p : all) {
            if (p.getStato() == StatoPartita.PLAYED)         risultati.add(p);
            else if (p.getStato() == StatoPartita.SCHEDULED) prossime.add(p);
        }

        model.addAttribute("torneo", torneo);
        model.addAttribute("risultati", risultati);
        model.addAttribute("prossimePartite", prossime);
        return "torneo/dettaglio";
    }

    // Página de clasificación (el div lo rellena React)
    @GetMapping("/tornei/{id}/classifica")
    public String classifica(@PathVariable Long id, Model model) {
        Torneo torneo = torneoService.findByIdWithSquadre(id)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        model.addAttribute("torneo", torneo);
        model.addAttribute("classifica", partitaService.calcolaClassifica(torneo));
        model.addAttribute("partite", partitaService.findByTorneo(torneo));
        return "torneo/classifica";
    }

    // ── Rutas de administrador (solo ADMIN) ──

    @GetMapping("/admin/tornei")
    @PreAuthorize("hasRole('ADMIN')")
    public String listaAdmin(Model model) {
        model.addAttribute("tornei", torneoService.findAll());
        return "torneo/lista";
    }

    @GetMapping("/admin/tornei/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String dettaglioAdmin(@PathVariable Long id, Model model) {
        Torneo torneo = torneoService.findByIdWithSquadre(id)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        List<Partita> all = partitaService.findByTorneo(torneo);

        List<Partita> risultati = new ArrayList<>();
        List<Partita> prossime  = new ArrayList<>();
        for (Partita p : all) {
            if (p.getStato() == StatoPartita.PLAYED)         risultati.add(p);
            else if (p.getStato() == StatoPartita.SCHEDULED) prossime.add(p);
        }

        model.addAttribute("torneo", torneo);
        model.addAttribute("risultati", risultati);
        model.addAttribute("prossimePartite", prossime);
        return "torneo/dettaglio";
    }

    // Formulario vacío para crear un torneo
    @GetMapping("/admin/tornei/nuovo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuovoForm(Model model) {
        model.addAttribute("torneo", new Torneo());
        model.addAttribute("tutteLeSquadre", squadraService.findAll());
        return "torneo/form";
    }

    // Guarda el torneo nuevo. @Valid comprueba los campos del formulario
    @PostMapping("/admin/tornei/nuovo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuovoSalva(@Valid @ModelAttribute Torneo torneo,
                              BindingResult result,
                              @RequestParam(required = false) List<Long> squadraIds,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tutteLeSquadre", squadraService.findAll());
            return "torneo/form";
        }
        Torneo salvato = torneoService.save(torneo);
        if (squadraIds != null) {
            torneoService.aggiornaSquadre(salvato.getId(), squadraIds);
        }
        return "redirect:/admin/tornei/" + salvato.getId();
    }

    // Formulario relleno con los datos actuales para editar
    @GetMapping("/admin/tornei/{id}/modifica")
    @PreAuthorize("hasRole('ADMIN')")
    public String modificaForm(@PathVariable Long id, Model model) {
        Torneo torneo = torneoService.findByIdWithSquadre(id)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        model.addAttribute("torneo", torneo);
        model.addAttribute("tutteLeSquadre", squadraService.findAll());
        return "torneo/form";
    }

    @PostMapping("/admin/tornei/{id}/modifica")
    @PreAuthorize("hasRole('ADMIN')")
    public String modificaSalva(@PathVariable Long id,
                                 @Valid @ModelAttribute Torneo torneo,
                                 BindingResult result,
                                 @RequestParam(required = false) List<Long> squadraIds,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tutteLeSquadre", squadraService.findAll());
            return "torneo/form";
        }
        torneo.setId(id); // con el id, JPA hace UPDATE en vez de INSERT
        torneoService.save(torneo);
        if (squadraIds != null) {
            torneoService.aggiornaSquadre(id, squadraIds);
        }
        return "redirect:/admin/tornei/" + id;
    }

    @PostMapping("/admin/tornei/{id}/elimina")
    @PreAuthorize("hasRole('ADMIN')")
    public String elimina(@PathVariable Long id) {
        torneoService.deleteById(id);
        return "redirect:/admin/tornei";
    }
}
