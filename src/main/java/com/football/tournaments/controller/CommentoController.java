package com.football.tournaments.controller;

import com.football.tournaments.model.*;
import com.football.tournaments.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/commenti")
public class CommentoController {

    @Autowired private CommentoService commentoService;
    @Autowired private PartitaService partitaService;
    @Autowired private UserService userService;

    // Añade un comentario a un partido. @AuthenticationPrincipal da el usuario logueado
    @PostMapping("/aggiungi")
    public String aggiungi(@RequestParam Long partitaId,
                            @RequestParam String testo,
                            @AuthenticationPrincipal UserDetails principal) {
        Partita partita = partitaService.findByIdWithDetails(partitaId)
            .orElseThrow(() -> new RuntimeException("Partita non trovata"));
        User utente = userService.findByUsername(principal.getUsername());
        commentoService.save(new Commento(testo, utente, partita));
        return "redirect:/partite/" + partitaId;
    }

    @GetMapping("/{id}/modifica")
    public String modificaForm(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails principal,
                                Model model) {
        User utente = userService.findByUsername(principal.getUsername());
        // findByIdAndUtente solo lo encuentra si el comentario es de este usuario
        Commento commento = commentoService.findByIdAndUtente(id, utente)
            .orElseThrow(() -> new RuntimeException("Commento non trovato o non autorizzato"));
        model.addAttribute("commento", commento);
        return "commento/form";
    }

    @PostMapping("/{id}/modifica")
    public String modificaSalva(@PathVariable Long id,
                                 @RequestParam String testo,
                                 @AuthenticationPrincipal UserDetails principal) {
        User utente = userService.findByUsername(principal.getUsername());
        // Vuelvo a comprobar que el comentario es suyo
        Commento commento = commentoService.findByIdAndUtente(id, utente)
            .orElseThrow(() -> new RuntimeException("Commento non trovato o non autorizzato"));
        commento.setTesto(testo);
        commentoService.save(commento);
        return "redirect:/partite/" + commento.getPartita().getId();
    }
}
