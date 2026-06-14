package com.football.tournaments.service;

import com.football.tournaments.model.User;
import com.football.tournaments.model.UserRole;
import com.football.tournaments.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

// @Service: le dice a Spring que esta clase contiene lógica de negocio
// implements OAuth2UserService: contrato de Spring Security para login con OAuth2 (GitHub)
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    // @Autowired: Spring inyecta automáticamente el repositorio de usuarios
    @Autowired
    private UserRepository userRepository;

    // Spring Security llama a este método automáticamente cuando alguien hace login con GitHub
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. Pide a GitHub los datos del usuario (nombre, email, avatar, etc.)
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes(); // mapa con todos los datos de GitHub

        // 2. GitHub usa "login" como nombre de usuario y "email" como correo
        String githubUsername = (String) attributes.get("login");
        String email          = (String) attributes.get("email");

        // 3. GitHub permite ocultar el email público — si no hay email, usamos fallback
        if (email == null || email.isBlank()) {
            email = githubUsername + "@github.oauth"; // email ficticio para que la BD no quede vacía
        }
        final String emailFinal = email.toLowerCase(); // guardamos en minúsculas

        // 4. Busca el usuario en nuestra BD por email:
        //    - Si ya existe (entró antes con GitHub), lo reutiliza sin crear otro
        //    - Si es nuevo, crea un usuario sin contraseña (no puede entrar con formulario)
        User user = userRepository.findByEmail(emailFinal).orElseGet(() -> {
            User nuevo = new User();
            nuevo.setUsername(generarUsername(githubUsername)); // asegura username único
            nuevo.setEmail(emailFinal);
            // "OAUTH2_NO_PASSWORD": placeholder que NO es un hash BCrypt válido
            // → BCrypt.matches() devuelve false siempre → este usuario no puede entrar con contraseña
            nuevo.setPassword("OAUTH2_NO_PASSWORD");
            nuevo.setRole(UserRole.USER); // por defecto es usuario normal
            return userRepository.save(nuevo); // guarda en BD
        });

        // 5. Devuelve el usuario a Spring Security con su rol real de nuestra BD
        // Spring Security usa esto para saber qué puede hacer el usuario
        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())), // rol de nuestra BD
            attributes,  // datos de GitHub (nombre, avatar, etc.)
            "login"      // atributo de GitHub que se usa como nombre principal del usuario
        );
    }

    // Si el username de GitHub ya está en la BD, añade un número al final para hacerlo único
    // Ejemplo: "javier" → "javier1" → "javier2" hasta encontrar uno libre
    private String generarUsername(String base) {
        String username = base;
        int i = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + i; // intenta: javier1, javier2, javier3...
            i++;
        }
        return username; // devuelve el primero que no esté ocupado
    }
}
