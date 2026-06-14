package com.football.tournaments.controller;

import com.football.tournaments.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// @Controller: esta clase recibe peticiones del navegador y devuelve páginas HTML
@Controller
public class AuthController {

    // Spring inyecta el servicio de usuarios automáticamente
    @Autowired
    private UserService userService;

    // GET /login → muestra la página de login
    // Si el usuario navega directamente a /login sin parámetros, lo manda a /tornei
    // Si llega con parámetros (?show, ?error, ?registered), muestra el formulario
    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        if (request.getQueryString() == null) {
            return "redirect:/tornei"; // navegación directa → redirige
        }
        return "auth/login"; // abre templates/auth/login.html
    }

    // GET /logout → cierra la sesión del usuario
    // Limpia el contexto de seguridad, invalida la sesión y borra la cookie JSESSIONID
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext(); // borra el usuario autenticado de memoria
        var session = request.getSession(false); // obtiene la sesión sin crear una nueva
        if (session != null) session.invalidate(); // invalida la sesión del servidor
        String path = request.getContextPath();
        if (path == null || path.isEmpty()) path = "/";
        Cookie cookie = new Cookie("JSESSIONID", null); // crea cookie vacía
        cookie.setPath(path);
        cookie.setMaxAge(0); // maxAge=0 → el navegador borra la cookie inmediatamente
        response.addCookie(cookie);
        return "redirect:/tornei"; // redirige a la lista de torneos
    }

    // GET /register → muestra el formulario de registro
    @GetMapping("/register")
    public String registerForm() {
        return "auth/register"; // abre templates/auth/register.html
    }

    // POST /register → procesa el formulario de registro
    // @RequestParam: recibe los campos del formulario (username, email, password)
    // defaultValue="": si el campo llega vacío, usa "" en vez de null
    @PostMapping("/register")
    public String register(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String password,
            Model model) {
        try {
            userService.registra(username, email, password); // valida y crea el usuario
            return "redirect:/login?registered"; // redirige al login con mensaje de éxito
        } catch (IllegalArgumentException e) {
            // Error de validación (email inválido, contraseña débil, usuario duplicado...)
            // Vuelve al formulario con el mensaje de error y los campos rellenos
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "auth/register";
        } catch (DataIntegrityViolationException e) {
            // Error de BD: constraint UNIQUE violado (race condition o validación JS saltada)
            model.addAttribute("error", "Username or email already in use. Please try again.");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "auth/register";
        }
    }
}
