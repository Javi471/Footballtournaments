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

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. Pedimos a GitHub los datos del usuario (nombre, email, avatar, etc.)
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. GitHub devuelve "login" como nombre de usuario y "email" como correo
        String githubUsername = (String) attributes.get("login");
        String email          = (String) attributes.get("email");

        // 3. GitHub permite ocultar el email público → usamos fallback
        if (email == null || email.isBlank()) {
            email = githubUsername + "@github.oauth";
        }
        final String emailFinal = email.toLowerCase();

        // 4. Buscar el usuario en nuestra BD por email
        //    → Si ya existe (se logó antes con OAuth), lo reutilizamos
        //    → Si es nuevo, lo creamos sin contraseña
        User user = userRepository.findByEmail(emailFinal).orElseGet(() -> {
            User nuevo = new User();
            nuevo.setUsername(generarUsername(githubUsername));
            nuevo.setEmail(emailFinal);
            // Placeholder que NO es un hash BCrypt válido → nadie puede entrar
            // con este usuario por el formulario normal (BCrypt.matches devuelve false)
            nuevo.setPassword("OAUTH2_NO_PASSWORD");
            nuevo.setRole(UserRole.USER);
            return userRepository.save(nuevo);
        });

        // 5. Devolvemos el usuario a Spring Security con su rol real de nuestra BD
        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
            attributes,
            "login"  // atributo de GitHub que usamos como nombre principal
        );
    }

    // Si el username de GitHub ya existe en la BD, añade un número al final
    private String generarUsername(String base) {
        String username = base;
        int i = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + i;
            i++;
        }
        return username;
    }
}
