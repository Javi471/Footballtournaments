package com.football.tournaments.service;

import com.football.tournaments.model.User;
import com.football.tournaments.model.UserRole;
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

// @Service: le dice a Spring que esta clase contiene lógica de negocio
// implements UserDetailsService: contrato de Spring Security para cargar usuarios al hacer login
@Service
public class UserService implements UserDetailsService {

    // @Autowired: Spring inyecta automáticamente el repositorio de usuarios
    @Autowired
    private UserRepository userRepository;

    // PasswordEncoder: encripta contraseñas con BCrypt (configurado en PasswordConfig.java)
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Spring Security llama a este método automáticamente cuando alguien hace login
    // Busca el usuario por su nombre y devuelve un objeto que Spring Security entiende
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca el usuario en BD — si no existe, lanza error y Spring muestra "usuario incorrecto"
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));

        // Devuelve un UserDetails de Spring Security con:
        // - el nombre de usuario
        // - la contraseña encriptada (Spring la compara con la que escribe el usuario)
        // - el rol (ej: "ROLE_ADMIN" o "ROLE_USER")
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    // Registra un nuevo usuario validando todos los campos primero
    @Transactional
    public User registra(String username, String email, String password) {

        // Validaciones — si algo falla, lanza IllegalArgumentException con mensaje de error
        // El AuthController lo captura y lo muestra al usuario en el formulario

        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be empty.");

        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email cannot be empty.");

        // Expresión regular que valida el formato del email (contiene @ y .)
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Please enter a valid email address.");

        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password cannot be empty.");

        // Comprueba que el nombre de usuario no esté ya en uso
        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("Username already taken. Please choose a different one.");

        // Comprueba que el email no esté ya registrado (comparación en minúsculas)
        if (userRepository.existsByEmail(email.toLowerCase()))
            throw new IllegalArgumentException("An account with this email already exists.");

        // Contraseña: mínimo 6 caracteres
        if (password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters long.");

        // Contraseña: al menos una letra mayúscula
        if (!password.matches(".*[A-Z].*"))
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");

        // Contraseña: al menos un número
        if (!password.matches(".*[0-9].*"))
            throw new IllegalArgumentException("Password must contain at least one number.");

        // Crea el usuario con la contraseña encriptada (passwordEncoder.encode)
        // El email se guarda en minúsculas para evitar duplicados por mayúsculas
        User user = new User(username, email.toLowerCase(), passwordEncoder.encode(password), UserRole.USER);
        return userRepository.save(user); // guarda en BD y devuelve el usuario creado
    }

    // Busca un usuario por su nombre — lanza error si no existe
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));
    }

    // Comprueba si ya existe un usuario con ese nombre (devuelve true o false)
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
