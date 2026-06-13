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

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        // Sin parámetros = navegación directa → redirige a torneos
        // Con parámetros (?show, ?error, ?registered) = acceso intencional → muestra el form
        if (request.getQueryString() == null) {
            return "redirect:/tornei";
        }
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        String path = request.getContextPath();
        if (path == null || path.isEmpty()) path = "/";
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/tornei";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String password,
            Model model) {
        try {
            userService.registra(username, email, password);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "auth/register";
        } catch (DataIntegrityViolationException e) {
            // DB unique constraint violated (race condition or bypassed JS validation)
            model.addAttribute("error", "Username or email already in use. Please try again.");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "auth/register";
        }
    }
}
