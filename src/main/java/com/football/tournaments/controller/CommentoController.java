package com.football.tournaments.controller;

import com.football.tournaments.model.Commento;
import com.football.tournaments.model.Partita;
import com.football.tournaments.model.User;
import com.football.tournaments.service.CommentoService;
import com.football.tournaments.service.PartitaService;
import com.football.tournaments.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// @Controller: esta clase recibe peticiones del navegador y devuelve páginas HTML
// @RequestMapping("/commenti"): prefijo base — todas las rutas empiezan con /commenti
@Controller
@RequestMapping("/commenti")
public class CommentoController {

    // Spring inyecta los servicios automáticamente
    @Autowired private CommentoService commentoService;
    @Autowired private PartitaService partitaService;
    @Autowired private UserService userService;

    // POST /commenti/aggiungi → añade un nuevo comentario a un partido
    // @AuthenticationPrincipal: inyecta el usuario que ha iniciado sesión
    // Solo usuarios autenticados pueden llegar aquí (SecurityConfig lo obliga)
    @PostMapping("/aggiungi")
    public String aggiungi(@RequestParam Long partitaId,    // id del partido sobre el que se comenta
                            @RequestParam String testo,      // texto del comentario
                            @AuthenticationPrincipal UserDetails principal) { // usuario actual
        // Busca el partido y el usuario en BD
        Partita partita = partitaService.findByIdWithDetails(partitaId)
            .orElseThrow(() -> new RuntimeException("Partita non trovata"));
        User utente = userService.findByUsername(principal.getUsername()); // usuario autenticado
        commentoService.save(new Commento(testo, utente, partita)); // crea y guarda el comentario
        return "redirect:/partite/" + partitaId; // vuelve a la página del partido
    }

    // GET /commenti/{id}/modifica → muestra el formulario para editar un comentario
    // Solo el autor del comentario puede editarlo (findByIdAndUtente lo verifica)
    @GetMapping("/{id}/modifica")
    public String modificaForm(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails principal,
                                Model model) {
        User utente = userService.findByUsername(principal.getUsername());
        // Busca el comentario y verifica que pertenece al usuario actual
        // Si no es suyo, lanza error (evita que un usuario edite comentarios ajenos)
        Commento commento = commentoService.findByIdAndUtente(id, utente)
            .orElseThrow(() -> new RuntimeException("Commento non trovato o non autorizzato"));
        model.addAttribute("commento", commento);
        return "commento/form"; // abre templates/commento/form.html con el comentario a editar
    }

    // POST /commenti/{id}/modifica → guarda el texto editado del comentario
    @PostMapping("/{id}/modifica")
    public String modificaSalva(@PathVariable Long id,
                                 @RequestParam String testo, // nuevo texto del comentario
                                 @AuthenticationPrincipal UserDetails principal) {
        User utente = userService.findByUsername(principal.getUsername());
        // Verifica de nuevo que el comentario pertenece al usuario (seguridad)
        Commento commento = commentoService.findByIdAndUtente(id, utente)
            .orElseThrow(() -> new RuntimeException("Commento non trovato o non autorizzato"));
        commento.setTesto(testo); // actualiza el texto
        commentoService.save(commento); // guarda en BD
        return "redirect:/partite/" + commento.getPartita().getId(); // vuelve al partido
    }
}
