package com.football.tournaments.service;

import com.football.tournaments.model.*;
import com.football.tournaments.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Carga usuarios para Spring Security y maneja el registro
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Spring Security llama a esto al hacer login. Devuelve el usuario en su formato
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    // Registra un usuario nuevo. Si algo no vale, lanza error y el AuthController lo muestra
    @Transactional
    public User registra(String username, String email, String password) {

        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be empty.");

        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be empty.");

        // Comprueba que el email tenga formato válido (algo@algo.algo)
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Please enter a valid email address.");

        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password cannot be empty.");

        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("Username already taken. Please choose a different one.");

        if (userRepository.existsByEmail(email.toLowerCase()))
            throw new IllegalArgumentException("An account with this email already exists.");

        if (password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters long.");

        if (!password.matches(".*[A-Z].*"))
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");

        if (!password.matches(".*[0-9].*"))
            throw new IllegalArgumentException("Password must contain at least one number.");

        // Guarda la contraseña encriptada y el email en minúsculas
        User user = new User(username, email.toLowerCase(), passwordEncoder.encode(password), UserRole.USER);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
