package com.football.tournaments.controller;

import com.football.tournaments.model.Partita;
import com.football.tournaments.model.StatoPartita;
import com.football.tournaments.model.Torneo;
import com.football.tournaments.service.PartitaService;
import com.football.tournaments.service.SquadraService;
import com.football.tournaments.service.TorneoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TorneoController {

    @Autowired private TorneoService torneoService;
    @Autowired private SquadraService squadraService;
    @Autowired private PartitaService partitaService;

    // ── Public ────────────────────────────────────────────────────────────

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
        model.addAttribute("torneo", torneo);
        model.addAttribute("risultati",       all.stream().filter(p -> p.getStato() == StatoPartita.PLAYED)   .collect(Collectors.toList()));
        model.addAttribute("prossimePartite", all.stream().filter(p -> p.getStato() == StatoPartita.SCHEDULED).collect(Collectors.toList()));
        return "torneo/dettaglio";
    }

    @GetMapping("/tornei/{id}/classifica")
    public String classifica(@PathVariable Long id, Model model) {
        Torneo torneo = torneoService.findByIdWithSquadre(id)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        model.addAttribute("torneo", torneo);
        model.addAttribute("classifica", partitaService.calcolaClassifica(torneo));
        model.addAttribute("partite", partitaService.findByTorneo(torneo));
        return "torneo/classifica";
    }

    // ── Admin ─────────────────────────────────────────────────────────────

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
        model.addAttribute("torneo", torneo);
        model.addAttribute("risultati",       all.stream().filter(p -> p.getStato() == StatoPartita.PLAYED)   .collect(Collectors.toList()));
        model.addAttribute("prossimePartite", all.stream().filter(p -> p.getStato() == StatoPartita.SCHEDULED).collect(Collectors.toList()));
        return "torneo/dettaglio";
    }

    @GetMapping("/admin/tornei/nuovo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuovoForm(Model model) {
        model.addAttribute("torneo", new Torneo());
        model.addAttribute("tutteLeSquadre", squadraService.findAll());
        return "torneo/form";
    }

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
        torneo.setId(id);
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
