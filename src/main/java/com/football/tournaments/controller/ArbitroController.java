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

@Controller
@RequestMapping("/admin/arbitri")
@PreAuthorize("hasRole('ADMIN')")
public class ArbitroController {

    @Autowired
    private ArbitroService arbitroService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("arbitri", arbitroService.findAll());
        return "arbitro/lista";
    }

    @GetMapping("/nuovo")
    public String nuovoForm(Model model) {
        model.addAttribute("arbitro", new Arbitro());
        return "arbitro/form";
    }

    @PostMapping("/nuovo")
    public String nuovoSalva(@Valid @ModelAttribute Arbitro arbitro, BindingResult result) {
        if (result.hasErrors()) return "arbitro/form";
        arbitroService.save(arbitro);
        return "redirect:/admin/arbitri";
    }

    @GetMapping("/{id}/modifica")
    public String modificaForm(@PathVariable Long id, Model model) {
        model.addAttribute("arbitro", arbitroService.findById(id)
            .orElseThrow(() -> new RuntimeException("Arbitro non trovato")));
        return "arbitro/form";
    }

    @PostMapping("/{id}/modifica")
    public String modificaSalva(@PathVariable Long id,
                                 @Valid @ModelAttribute Arbitro arbitro,
                                 BindingResult result) {
        if (result.hasErrors()) return "arbitro/form";
        arbitro.setId(id);
        arbitroService.save(arbitro);
        return "redirect:/admin/arbitri";
    }
}
