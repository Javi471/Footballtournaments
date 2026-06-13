package com.football.tournaments.controller;

import com.football.tournaments.model.*;
import com.football.tournaments.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class PartitaController {

    @Autowired private PartitaService partitaService;
    @Autowired private TorneoService torneoService;
    @Autowired private SquadraService squadraService;
    @Autowired private ArbitroService arbitroService;
    @Autowired private CommentoService commentoService;
    @Autowired private UserService userService;

    // ── Public ────────────────────────────────────────────────────────────

    @GetMapping("/partite/{id}")
    public String dettaglio(@PathVariable Long id, Model model,
                             @AuthenticationPrincipal UserDetails principal) {
        Partita partita = partitaService.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("Partita non trovata"));
        model.addAttribute("partita", partita);
        model.addAttribute("commenti", commentoService.findByPartita(partita));

        if (principal != null) {
            model.addAttribute("nuovoCommento", new Commento());
            model.addAttribute("utenteCorrente", principal.getUsername());
        }
        return "partita/dettaglio";
    }

    // ── Admin ─────────────────────────────────────────────────────────────

    @GetMapping("/admin/partite/nuova")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuovaForm(Model model) {
        model.addAttribute("partita", new Partita());
        model.addAttribute("tornei", torneoService.findAll());
        model.addAttribute("squadre", squadraService.findAll());
        model.addAttribute("arbitri", arbitroService.findAll());
        return "partita/form";
    }

    @PostMapping("/admin/partite/nuova")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuovaSalva(@ModelAttribute Partita partita,
                              @RequestParam Long torneoId,
                              @RequestParam Long squadraHomeId,
                              @RequestParam Long squadraAwayId,
                              @RequestParam Long arbitroId,
                              Model model) {
        Torneo torneo = torneoService.findById(torneoId)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        Squadra home = squadraService.findById(squadraHomeId)
            .orElseThrow(() -> new RuntimeException("Squadra non trovata"));
        Squadra away = squadraService.findById(squadraAwayId)
            .orElseThrow(() -> new RuntimeException("Squadra non trovata"));
        Arbitro arbitro = arbitroService.findById(arbitroId)
            .orElseThrow(() -> new RuntimeException("Arbitro non trovato"));

        partita.setTorneo(torneo);
        partita.setSquadraHome(home);
        partita.setSquadraAway(away);
        partita.setArbitro(arbitro);
        Partita salvata = partitaService.save(partita);
        return "redirect:/admin/tornei/" + partita.getTorneo().getId();
    }

    @GetMapping("/admin/partite/{id}/risultato")
    @PreAuthorize("hasRole('ADMIN')")
    public String risultatoForm(@PathVariable Long id, Model model) {
        Partita partita = partitaService.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("Partita non trovata"));
        model.addAttribute("partita", partita);
        return "partita/risultato";
    }

    @PostMapping("/admin/partite/{id}/risultato")
    @PreAuthorize("hasRole('ADMIN')")
    public String risultatoSalva(@PathVariable Long id,
                                  @RequestParam int goalsHome,
                                  @RequestParam int goalsAway) {
        partitaService.registraRisultato(id, goalsHome, goalsAway);
        Partita p = partitaService.findByIdWithDetails(id).orElseThrow();
        return "redirect:/admin/tornei/" + p.getTorneo().getId();
    }

    @PostMapping("/admin/partite/{id}/elimina")
    @PreAuthorize("hasRole('ADMIN')")
    public String elimina(@PathVariable Long id) {
        Partita partita = partitaService.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("Partita non trovata"));
        Long torneoId = partita.getTorneo().getId();
        partitaService.deleteById(id);
        return "redirect:/admin/tornei/" + torneoId;
    }
}
