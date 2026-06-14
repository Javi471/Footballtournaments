package com.football.tournaments.service;

import com.football.tournaments.model.*;
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

// Maneja el login con GitHub: guarda o reutiliza el usuario en nuestra BD
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserRepository userRepository;

    // Spring Security llama a esto cuando alguien entra con GitHub
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // Datos que devuelve GitHub
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String githubUsername = (String) attributes.get("login");
        String email          = (String) attributes.get("email");

        // GitHub permite ocultar el email; si no viene, me invento uno
        if (email == null || email.isBlank()) {
            email = githubUsername + "@github.oauth";
        }
        final String emailFinal = email.toLowerCase();

        // Si el usuario ya existe lo reutilizo; si no, lo creo
        User user = userRepository.findByEmail(emailFinal).orElseGet(() -> {
            User nuevo = new User();
            nuevo.setUsername(generarUsername(githubUsername));
            nuevo.setEmail(emailFinal);
            // Placeholder: no es un hash válido, así que nunca podrá entrar con contraseña
            nuevo.setPassword("OAUTH2_NO_PASSWORD");
            nuevo.setRole(UserRole.USER);
            return userRepository.save(nuevo);
        });

        // Devuelvo el usuario con su rol de nuestra BD
        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
            attributes,
            "login"
        );
    }

    // Si el nombre de GitHub ya existe, le añade un número: javier, javier1, javier2...
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
