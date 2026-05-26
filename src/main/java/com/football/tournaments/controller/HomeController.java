package com.football.tournaments.controller;

import com.football.tournaments.service.TorneoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private TorneoService torneoService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("tornei", torneoService.findAll());
        return "index";
    }
}
