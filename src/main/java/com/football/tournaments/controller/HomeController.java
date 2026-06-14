package com.football.tournaments.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// @Controller: le dice a Spring que esta clase recibe peticiones del navegador y devuelve páginas HTML
@Controller
public class HomeController {

    // @GetMapping("/"): cuando el usuario entra en localhost:8080/ (raíz)
    // redirige automáticamente a /tornei (la lista de torneos)
    @GetMapping("/")
    public String home() {
        return "redirect:/tornei"; // redirige al navegador a localhost:8080/tornei
    }
}
