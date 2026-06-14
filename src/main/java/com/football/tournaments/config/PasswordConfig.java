package com.football.tournaments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// @Configuration: le dice a Spring que este archivo define configuración (beans)
// Spring lo procesa al arrancar la aplicación
@Configuration
public class PasswordConfig {

    // @Bean: Spring crea este objeto una sola vez y lo comparte en toda la app
    // PasswordEncoder: interfaz que encripta y verifica contraseñas
    // BCryptPasswordEncoder: algoritmo de hash seguro para contraseñas
    //   - Convierte "Admin123" → "$2a$10$xyz..." (irreversible)
    //   - Cada vez que encripta genera un resultado diferente (salt aleatorio)
    //   - Para verificar, usa BCrypt.matches("Admin123", "$2a$10$xyz...") → true/false
    // Se inyecta con @Autowired en UserService y SecurityConfig
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
