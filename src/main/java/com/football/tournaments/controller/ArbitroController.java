package com.football.tournaments.controller;

import com.football.tournaments.model.Arbitro;
import com.football.tournaments.service.ArbitroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

// @Controller: esta clase recibe peticiones del navegador y devuelve páginas HTML
// @RequestMapping("/admin/arbitri"): prefijo base — todas las rutas empiezan con /admin/arbitri
// @PreAuthorize("hasRole('ADMIN')"): toda la clase requiere rol ADMIN (aplica a todos los métodos)
@Controller
@RequestMapping("/admin/arbitri")
@PreAuthorize("hasRole('ADMIN')")
public class ArbitroController {

    // Spring inyecta el servicio de árbitros automáticamente
    @Autowired
    private ArbitroService arbitroService;

    // GET /admin/arbitri → muestra la lista de todos los árbitros
    @GetMapping
    public String lista(Model model) {
        model.addAttribute("arbitri", arbitroService.findAll()); // mete la lista en el modelo
        return "arbitro/lista"; // abre templates/arbitro/lista.html
    }

    // GET /admin/arbitri/nuovo → muestra el formulario vacío para crear un árbitro
    @GetMapping("/nuovo")
    public String nuovoForm(Model model) {
        model.addAttribute("arbitro", new Arbitro()); // objeto vacío para el formulario
        return "arbitro/form"; // abre templates/arbitro/form.html
    }

    // POST /admin/arbitri/nuovo → procesa el formulario y guarda el nuevo árbitro
    // @Valid: valida los campos según las anotaciones del modelo (@NotBlank en nome, cognome...)
    // BindingResult: contiene los errores de validación si los hay
    @PostMapping("/nuovo")
    public String nuovoSalva(@Valid @ModelAttribute Arbitro arbitro, BindingResult result) {
        if (result.hasErrors()) return "arbitro/form"; // vuelve al formulario si hay errores
        arbitroService.save(arbitro); // guarda en BD
        return "redirect:/admin/arbitri"; // redirige a la lista de árbitros
    }

    // GET /admin/arbitri/{id}/modifica → muestra el formulario con los datos actuales
    @GetMapping("/{id}/modifica")
    public String modificaForm(@PathVariable Long id, Model model) {
        // Busca el árbitro — lanza error si no existe
        model.addAttribute("arbitro", arbitroService.findById(id)
            .orElseThrow(() -> new RuntimeException("Arbitro non trovato")));
        return "arbitro/form";
    }

    // POST /admin/arbitri/{id}/modifica → guarda los cambios del árbitro
    @PostMapping("/{id}/modifica")
    public String modificaSalva(@PathVariable Long id,
                                 @Valid @ModelAttribute Arbitro arbitro,
                                 BindingResult result) {
        if (result.hasErrors()) return "arbitro/form";
        arbitro.setId(id); // necesario para que JPA sepa que es un UPDATE, no un INSERT
        arbitroService.save(arbitro);
        return "redirect:/admin/arbitri";
    }
}
