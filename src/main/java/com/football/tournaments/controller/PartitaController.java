package com.football.tournaments.controller;

import com.football.tournaments.model.*;
import com.football.tournaments.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// @Controller: esta clase recibe peticiones del navegador y devuelve páginas HTML
@Controller
public class PartitaController {

    // Spring inyecta todos los servicios que este controller necesita
    @Autowired private PartitaService partitaService;
    @Autowired private TorneoService torneoService;
    @Autowired private SquadraService squadraService;
    @Autowired private ArbitroService arbitroService;
    @Autowired private CommentoService commentoService;

    // ── Rutas públicas ────────────────────────────────────────────────────────

    // GET /partite/{id} → muestra el detalle de un partido con sus comentarios
    // @AuthenticationPrincipal: inyecta el usuario actual (null si no ha iniciado sesión)
    @GetMapping("/partite/{id}")
    public String dettaglio(@PathVariable Long id, Model model,
                             @AuthenticationPrincipal UserDetails principal) {
        // Busca el partido con todos sus datos (equipos, árbitro, torneo)
        Partita partita = partitaService.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("Partita non trovata"));
        model.addAttribute("partita", partita);
        model.addAttribute("commenti", commentoService.findByPartita(partita)); // comentarios del partido

        // Si el usuario ha iniciado sesión, añade el formulario de nuevo comentario
        if (principal != null) {
            model.addAttribute("nuovoCommento", new Commento()); // objeto vacío para el form
            model.addAttribute("utenteCorrente", principal.getUsername()); // nombre del usuario actual
        }
        return "partita/dettaglio"; // abre templates/partita/dettaglio.html
    }

    // ── Rutas de administrador ────────────────────────────────────────────────

    // GET /admin/partite/nuova → formulario para crear un nuevo partido
    @GetMapping("/admin/partite/nuova")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuovaForm(Model model) {
        model.addAttribute("partita", new Partita()); // objeto vacío para el formulario
        model.addAttribute("tornei", torneoService.findAll());    // lista de torneos para el selector
        model.addAttribute("squadre", squadraService.findAll());  // lista de equipos para el selector
        model.addAttribute("arbitri", arbitroService.findAll());  // lista de árbitros para el selector
        return "partita/form"; // abre templates/partita/form.html
    }

    // POST /admin/partite/nuova → guarda el nuevo partido
    // Los ids de torneo, equipos y árbitro llegan como parámetros separados (no en el objeto Partita)
    @PostMapping("/admin/partite/nuova")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuovaSalva(@ModelAttribute Partita partita,
                              @RequestParam Long torneoId,
                              @RequestParam Long squadraHomeId,
                              @RequestParam Long squadraAwayId,
                              @RequestParam Long arbitroId,
                              Model model) {
        // Busca los objetos completos en BD a partir de sus ids
        Torneo torneo = torneoService.findById(torneoId)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        Squadra home = squadraService.findById(squadraHomeId)
            .orElseThrow(() -> new RuntimeException("Squadra non trovata"));
        Squadra away = squadraService.findById(squadraAwayId)
            .orElseThrow(() -> new RuntimeException("Squadra non trovata"));
        Arbitro arbitro = arbitroService.findById(arbitroId)
            .orElseThrow(() -> new RuntimeException("Arbitro non trovato"));

        // Asigna las relaciones al partido
        partita.setTorneo(torneo);
        partita.setSquadraHome(home);
        partita.setSquadraAway(away);
        partita.setArbitro(arbitro);
        partitaService.save(partita); // guarda en BD
        return "redirect:/admin/tornei/" + partita.getTorneo().getId(); // vuelve al torneo
    }

    // GET /admin/partite/{id}/risultato → formulario para registrar el resultado de un partido
    @GetMapping("/admin/partite/{id}/risultato")
    @PreAuthorize("hasRole('ADMIN')")
    public String risultatoForm(@PathVariable Long id, Model model) {
        Partita partita = partitaService.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("Partita non trovata"));
        model.addAttribute("partita", partita);
        return "partita/risultato"; // abre templates/partita/risultato.html
    }

    // POST /admin/partite/{id}/risultato → guarda el resultado y cambia el estado a PLAYED
    @PostMapping("/admin/partite/{id}/risultato")
    @PreAuthorize("hasRole('ADMIN')")
    public String risultatoSalva(@PathVariable Long id,
                                  @RequestParam int goalsHome,
                                  @RequestParam int goalsAway) {
        partitaService.registraRisultato(id, goalsHome, goalsAway); // guarda resultado y pone PLAYED
        Partita p = partitaService.findByIdWithDetails(id).orElseThrow();
        return "redirect:/admin/tornei/" + p.getTorneo().getId(); // vuelve al torneo
    }

    // POST /admin/partite/{id}/elimina → borra el partido
    @PostMapping("/admin/partite/{id}/elimina")
    @PreAuthorize("hasRole('ADMIN')")
    public String elimina(@PathVariable Long id) {
        // Guarda el id del torneo ANTES de borrar (para la redirección)
        Partita partita = partitaService.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("Partita non trovata"));
        Long torneoId = partita.getTorneo().getId();
        partitaService.deleteById(id); // borra el partido de BD
        return "redirect:/admin/tornei/" + torneoId; // vuelve al torneo
    }
}
