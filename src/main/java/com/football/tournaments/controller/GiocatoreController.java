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

// Todas las rutas requieren ADMIN y empiezan por /admin/giocatori
@Controller
@RequestMapping("/admin/giocatori")
@PreAuthorize("hasRole('ADMIN')")
public class GiocatoreController {

    @Autowired private GiocatoreService giocatoreService;
    @Autowired private SquadraService squadraService;

    // Formulario para añadir un jugador. squadraId es opcional: si viene, preselecciona el equipo
    @GetMapping("/nuovo")
    public String nuovoForm(@RequestParam(required = false) Long squadraId, Model model) {
        Giocatore g = new Giocatore();
        if (squadraId != null) {
            squadraService.findById(squadraId).ifPresent(g::setSquadra);
        }
        model.addAttribute("giocatore", g);
        model.addAttribute("squadre", squadraService.findAll());
        return "giocatore/form";
    }

    @PostMapping("/nuovo")
    public String nuovoSalva(@Valid @ModelAttribute Giocatore giocatore,
                              BindingResult result,
                              @RequestParam Long squadraId,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("squadre", squadraService.findAll());
            return "giocatore/form";
        }
        // Busca el equipo por su id y se lo asigna al jugador
        Squadra squadra = squadraService.findById(squadraId)
            .orElseThrow(() -> new RuntimeException("Squadra non trovata"));
        giocatore.setSquadra(squadra);
        giocatoreService.save(giocatore);
        return "redirect:/admin/squadre/" + squadraId;
    }

    @GetMapping("/{id}/modifica")
    public String modificaForm(@PathVariable Long id, Model model) {
        model.addAttribute("giocatore", giocatoreService.findById(id)
            .orElseThrow(() -> new RuntimeException("Giocatore non trovato")));
        model.addAttribute("squadre", squadraService.findAll());
        return "giocatore/form";
    }

    @PostMapping("/{id}/modifica")
    public String modificaSalva(@PathVariable Long id,
                                 @Valid @ModelAttribute Giocatore giocatore,
                                 BindingResult result,
                                 @RequestParam Long squadraId,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("squadre", squadraService.findAll());
            return "giocatore/form";
        }
        giocatore.setId(id);
        Squadra squadra = squadraService.findById(squadraId)
            .orElseThrow(() -> new RuntimeException("Squadra non trovata"));
        giocatore.setSquadra(squadra);
        giocatoreService.save(giocatore);
        return "redirect:/admin/squadre/" + squadraId;
    }

    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id) {
        // Guardo el id del equipo antes de borrar, para volver a su página
        Giocatore g = giocatoreService.findById(id)
            .orElseThrow(() -> new RuntimeException("Giocatore non trovato"));
        Long squadraId = g.getSquadra().getId();
        giocatoreService.deleteById(id);
        return "redirect:/admin/squadre/" + squadraId;
    }
}
