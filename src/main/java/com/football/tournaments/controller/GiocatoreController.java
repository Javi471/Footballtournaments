package com.football.tournaments.controller;

import com.football.tournaments.model.Giocatore;
import com.football.tournaments.model.Squadra;
import com.football.tournaments.service.GiocatoreService;
import com.football.tournaments.service.SquadraService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

// @Controller: esta clase recibe peticiones del navegador y devuelve páginas HTML
// @RequestMapping: prefijo base → todas las rutas empiezan con /admin/giocatori
// @PreAuthorize: solo ADMIN puede acceder — aplica a todos los métodos de la clase
@Controller
@RequestMapping("/admin/giocatori")
@PreAuthorize("hasRole('ADMIN')")
public class GiocatoreController {

    // Spring inyecta los servicios automáticamente
    @Autowired private GiocatoreService giocatoreService;
    @Autowired private SquadraService squadraService;

    // GET /admin/giocatori/nuevo → muestra el formulario para añadir un jugador
    // @RequestParam(required = false): el squadraId es opcional — si viene, preselecciona el equipo
    @GetMapping("/nuovo")
    public String nuovoForm(@RequestParam(required = false) Long squadraId, Model model) {
        Giocatore g = new Giocatore(); // objeto vacío para el formulario
        if (squadraId != null) {
            // Si viene desde la página de un equipo, preselecciona ese equipo en el formulario
            squadraService.findById(squadraId).ifPresent(g::setSquadra);
        }
        model.addAttribute("giocatore", g);
        model.addAttribute("squadre", squadraService.findAll()); // lista de equipos para el selector
        return "giocatore/form"; // abre templates/giocatore/form.html
    }

    // POST /admin/giocatori/nuevo → guarda el nuevo jugador
    // @RequestParam Long squadraId: el id del equipo al que pertenece el jugador
    @PostMapping("/nuovo")
    public String nuovoSalva(@Valid @ModelAttribute Giocatore giocatore,
                              BindingResult result,
                              @RequestParam Long squadraId,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("squadre", squadraService.findAll()); // necesario para re-renderizar el form
            return "giocatore/form";
        }
        // Asigna el equipo al jugador (busca el objeto Squadra completo por su id)
        Squadra squadra = squadraService.findById(squadraId)
            .orElseThrow(() -> new RuntimeException("Squadra non trovata"));
        giocatore.setSquadra(squadra);
        giocatoreService.save(giocatore); // guarda en BD
        return "redirect:/admin/squadre/" + squadraId; // vuelve a la página del equipo
    }

    // GET /admin/giocatori/{id}/modifica → formulario con los datos actuales del jugador
    @GetMapping("/{id}/modifica")
    public String modificaForm(@PathVariable Long id, Model model) {
        model.addAttribute("giocatore", giocatoreService.findById(id)
            .orElseThrow(() -> new RuntimeException("Giocatore non trovato")));
        model.addAttribute("squadre", squadraService.findAll());
        return "giocatore/form";
    }

    // POST /admin/giocatori/{id}/modifica → guarda los cambios del jugador
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
        giocatore.setId(id); // necesario para UPDATE en vez de INSERT
        Squadra squadra = squadraService.findById(squadraId)
            .orElseThrow(() -> new RuntimeException("Squadra non trovata"));
        giocatore.setSquadra(squadra);
        giocatoreService.save(giocatore);
        return "redirect:/admin/squadre/" + squadraId;
    }

    // POST /admin/giocatori/{id}/elimina → borra el jugador
    @PostMapping("/{id}/elimina")
    public String elimina(@PathVariable Long id) {
        // Guarda el id del equipo ANTES de borrar el jugador (para la redirección)
        Giocatore g = giocatoreService.findById(id)
            .orElseThrow(() -> new RuntimeException("Giocatore non trovato"));
        Long squadraId = g.getSquadra().getId();
        giocatoreService.deleteById(id); // borra el jugador de BD
        return "redirect:/admin/squadre/" + squadraId; // vuelve al equipo
    }
}
