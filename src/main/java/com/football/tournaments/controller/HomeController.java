package com.football.tournaments.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // La raíz "/" redirige a la lista de torneos
    @GetMapping("/")
    public String home() {
        return "redirect:/tornei";
    }
}
