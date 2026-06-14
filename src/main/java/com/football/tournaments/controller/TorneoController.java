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

// @Controller: esta clase recibe peticiones del navegador y devuelve páginas HTML
@Controller
public class TorneoController {

    // Spring inyecta los servicios automáticamente con @Autowired
    @Autowired private TorneoService torneoService;
    @Autowired private SquadraService squadraService;
    @Autowired private PartitaService partitaService;

    // ── Rutas públicas (cualquier usuario puede acceder) ──────────────────────

    // GET /tornei → muestra la lista de todos los torneos
    @GetMapping("/tornei")
    public String lista(Model model) {
        model.addAttribute("tornei", torneoService.findAll()); // mete la lista en el modelo
        return "torneo/lista"; // abre templates/torneo/lista.html
    }

    // GET /tornei/{id} → muestra el detalle de un torneo concreto
    // @PathVariable: extrae el {id} de la URL
    @GetMapping("/tornei/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        // Busca el torneo con sus equipos — lanza error si no existe
        Torneo torneo = torneoService.findByIdWithSquadre(id)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        List<Partita> all = partitaService.findByTorneo(torneo); // todos los partidos del torneo
        model.addAttribute("torneo", torneo);
        // Filtra los partidos ya jugados (resultado conocido)
        model.addAttribute("risultati",       all.stream().filter(p -> p.getStato() == StatoPartita.PLAYED)   .collect(Collectors.toList()));
        // Filtra los partidos programados (aún sin resultado)
        model.addAttribute("prossimePartite", all.stream().filter(p -> p.getStato() == StatoPartita.SCHEDULED).collect(Collectors.toList()));
        return "torneo/dettaglio"; // abre templates/torneo/dettaglio.html
    }

    // GET /tornei/{id}/classifica → muestra la página de clasificación
    // Pasa los datos al modelo para que Thymeleaf renderice el div y React monte la tabla
    @GetMapping("/tornei/{id}/classifica")
    public String classifica(@PathVariable Long id, Model model) {
        Torneo torneo = torneoService.findByIdWithSquadre(id)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        model.addAttribute("torneo", torneo);
        // calcolaClassifica calcula puntos, victorias, etc. — React también lo llama por API
        model.addAttribute("classifica", partitaService.calcolaClassifica(torneo));
        model.addAttribute("partite", partitaService.findByTorneo(torneo));
        return "torneo/classifica"; // abre templates/torneo/classifica.html (que carga React)
    }

    // ── Rutas de administrador (solo ADMIN puede acceder) ────────────────────

    // @PreAuthorize("hasRole('ADMIN')"): Spring Security bloquea el acceso si no es ADMIN
    // Si un usuario normal intenta entrar, Spring lo redirige a /tornei (según SecurityConfig)

    // GET /admin/tornei → lista de torneos para el admin
    @GetMapping("/admin/tornei")
    @PreAuthorize("hasRole('ADMIN')")
    public String listaAdmin(Model model) {
        model.addAttribute("tornei", torneoService.findAll());
        return "torneo/lista";
    }

    // GET /admin/tornei/{id} → detalle de un torneo para el admin
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

    // GET /admin/tornei/nuevo → muestra el formulario vacío para crear un torneo
    @GetMapping("/admin/tornei/nuovo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuovoForm(Model model) {
        model.addAttribute("torneo", new Torneo()); // objeto vacío para que Thymeleaf rellene el form
        model.addAttribute("tutteLeSquadre", squadraService.findAll()); // lista de equipos para el selector
        return "torneo/form"; // abre templates/torneo/form.html
    }

    // POST /admin/tornei/nuevo → procesa el formulario y guarda el nuevo torneo
    // @Valid: valida los campos del formulario según las anotaciones del modelo (@NotBlank, @NotNull...)
    // BindingResult: contiene los errores de validación si los hay
    // @RequestParam: recibe la lista de ids de equipos seleccionados en el formulario
    @PostMapping("/admin/tornei/nuovo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuovoSalva(@Valid @ModelAttribute Torneo torneo,
                              BindingResult result,
                              @RequestParam(required = false) List<Long> squadraIds,
                              Model model) {
        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario con los errores
            model.addAttribute("tutteLeSquadre", squadraService.findAll());
            return "torneo/form";
        }
        Torneo salvato = torneoService.save(torneo); // guarda el torneo en BD
        if (squadraIds != null) {
            torneoService.aggiornaSquadre(salvato.getId(), squadraIds); // asigna los equipos al torneo
        }
        return "redirect:/admin/tornei/" + salvato.getId(); // redirige al detalle del torneo creado
    }

    // GET /admin/tornei/{id}/modifica → muestra el formulario con los datos actuales para editar
    @GetMapping("/admin/tornei/{id}/modifica")
    @PreAuthorize("hasRole('ADMIN')")
    public String modificaForm(@PathVariable Long id, Model model) {
        Torneo torneo = torneoService.findByIdWithSquadre(id)
            .orElseThrow(() -> new RuntimeException("Torneo non trovato"));
        model.addAttribute("torneo", torneo); // el formulario se rellena con los datos actuales
        model.addAttribute("tutteLeSquadre", squadraService.findAll());
        return "torneo/form";
    }

    // POST /admin/tornei/{id}/modifica → guarda los cambios del formulario de edición
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
        torneo.setId(id); // necesario para que JPA sepa que es un UPDATE, no un INSERT
        torneoService.save(torneo);
        if (squadraIds != null) {
            torneoService.aggiornaSquadre(id, squadraIds);
        }
        return "redirect:/admin/tornei/" + id;
    }

    // POST /admin/tornei/{id}/elimina → borra el torneo y redirige a la lista
    @PostMapping("/admin/tornei/{id}/elimina")
    @PreAuthorize("hasRole('ADMIN')")
    public String elimina(@PathVariable Long id) {
        torneoService.deleteById(id);
        return "redirect:/admin/tornei"; // vuelve a la lista de torneos
    }
}
