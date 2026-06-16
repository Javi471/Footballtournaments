package com.football.tournaments.controller;

import com.football.tournaments.model.Squadra;
import com.football.tournaments.service.SquadraService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.*;

import static java.lang.Math.max;

@Controller
public class SquadraController {

    @Autowired
    private SquadraService squadraService;

    // Método auxiliar que guarda el archivo en disco y devuelve la ruta web
    private String salvaImmagine(MultipartFile file) {
        try {
            // Ruta ABSOLUTA basada en el directorio de trabajo actual del proyecto
            Path dir = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", "images", "squadre");
            Files.createDirectories(dir);

            // Obtiene el nombre original de forma segura (puede ser null en algunos sistemas)
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "imagen";
            }
            // Limpia la ruta (en Windows puede venir con barras invertidas)
            originalFilename = StringUtils.cleanPath(originalFilename);
            // Si la ruta contiene barras, nos quedamos solo con el nombre del archivo
            if (originalFilename.contains("/")) {
                originalFilename = originalFilename.substring(originalFilename.lastIndexOf('/') + 1);
            }

            // Nombre único: timestamp + nombre original limpio
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            Path destino = dir.resolve(filename);

            // Files.copy con InputStream es más fiable que transferTo en Windows
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            // Devuelve la ruta web para usar en <img src="...">
            return "/images/squadre/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Errore nel salvataggio dell'immagine", e);
        }
    }

    // ── Public ────────────────────────────────────────────────────────────

    // GET /squadre → lista paginada de equipos (4 por página)
    @GetMapping("/squadre")
    public String lista(@RequestParam(defaultValue = "0") int page, Model model) {
        page = max(0, page);
        var result = squadraService.findPage(page, 4);
        model.addAttribute("page", result);
        model.addAttribute("baseUrl", "/squadre");
        return "squadra/lista";
    }

    // GET /squadre/{id} → detalle del equipo con su lista de jugadores
    @GetMapping("/squadre/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        Squadra squadra = squadraService.findByIdWithGiocatori(id)
            .orElseThrow(() -> new RuntimeException("Squadra non trovata"));
        model.addAttribute("squadra", squadra);
        return "squadra/dettaglio";
    }

    // ── Admin ─────────────────────────────────────────────────────────────

    // GET /admin/squadre → lista paginada de equipos para el ADMIN
    @GetMapping("/admin/squadre")
    @PreAuthorize("hasRole('ADMIN')")
    public String listaAdmin(@RequestParam(defaultValue = "0") int page, Model model) {
        page = max(0, page);
        var result = squadraService.findPage(page, 4);
        model.addAttribute("page", result);
        model.addAttribute("baseUrl", "/admin/squadre");
        return "squadra/lista";
    }

    // GET /admin/squadre/{id} → detalle del equipo para el ADMIN
    @GetMapping("/admin/squadre/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String dettaglioAdmin(@PathVariable Long id, Model model) {
        Squadra squadra = squadraService.findByIdWithGiocatori(id)
            .orElseThrow(() -> new RuntimeException("Squadra non trovata"));
        model.addAttribute("squadra", squadra);
        return "squadra/dettaglio";
    }

    // GET /admin/squadre/nuova → formulario vacío para crear un equipo nuevo
    @GetMapping("/admin/squadre/nuova")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuovaForm(Model model) {
        model.addAttribute("squadra", new Squadra());
        return "squadra/form";
    }

    // POST /admin/squadre/nuova → guarda el equipo nuevo con su imagen opcional
    @PostMapping("/admin/squadre/nuova")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuovaSalva(@Valid @ModelAttribute Squadra squadra,
                              BindingResult result,
                              @RequestParam(value = "immagine", required = false) MultipartFile immagine) {
        if (result.hasErrors()) return "squadra/form";
        // Si el admin subió una imagen, la guardamos
        if (immagine != null && !immagine.isEmpty()) {
            squadra.setImagePath(salvaImmagine(immagine));
        }
        Squadra salvata = squadraService.save(squadra);
        return "redirect:/admin/squadre/" + salvata.getId();
    }

    // GET /admin/squadre/{id}/modifica → formulario relleno para editar el equipo
    @GetMapping("/admin/squadre/{id}/modifica")
    @PreAuthorize("hasRole('ADMIN')")
    public String modificaForm(@PathVariable Long id, Model model) {
        model.addAttribute("squadra", squadraService.findById(id)
            .orElseThrow(() -> new RuntimeException("Squadra non trovata")));
        return "squadra/form";
    }

    // POST /admin/squadre/{id}/modifica → guarda los cambios del equipo editado
    @PostMapping("/admin/squadre/{id}/modifica")
    @PreAuthorize("hasRole('ADMIN')")
    public String modificaSalva(@PathVariable Long id,
                                 @Valid @ModelAttribute Squadra squadra,
                                 BindingResult result,
                                 @RequestParam(value = "immagine", required = false) MultipartFile immagine) {
        if (result.hasErrors()) return "squadra/form";
        squadra.setId(id);
        if (immagine != null && !immagine.isEmpty()) {
            // Admin subió una nueva imagen → la guardamos
            squadra.setImagePath(salvaImmagine(immagine));
        } else {
            // Admin NO subió imagen nueva → conservamos la imagen anterior
            Squadra esistente = squadraService.findById(id).orElseThrow();
            squadra.setImagePath(esistente.getImagePath());
        }
        squadraService.save(squadra);
        return "redirect:/admin/squadre/" + id;
    }

    // POST /admin/squadre/{id}/elimina → borra el equipo y sus jugadores
    @PostMapping("/admin/squadre/{id}/elimina")
    @PreAuthorize("hasRole('ADMIN')")
    public String elimina(@PathVariable Long id) {
        squadraService.deleteById(id);
        return "redirect:/admin/squadre";
    }
}
