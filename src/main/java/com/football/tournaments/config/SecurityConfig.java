package com.football.tournaments.config;

import com.football.tournaments.service.CustomOAuth2UserService;
import com.football.tournaments.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;

// @Configuration: define configuración de Spring
// @EnableWebSecurity: activa el sistema de seguridad de Spring Security
// @EnableMethodSecurity: activa las anotaciones @PreAuthorize en los controllers
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // @Lazy: evita un ciclo de dependencias circular entre SecurityConfig y UserService
    @Autowired
    @Lazy
    private UserService userService;

    // PasswordEncoder definido en PasswordConfig.java (BCrypt)
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Servicio para manejar login con GitHub (OAuth2)
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    // @Bean: Spring crea y registra este objeto como el filtro principal de seguridad
    // Aquí se define TODO lo que Spring Security hace: quién puede acceder a qué URL
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF: protección contra ataques Cross-Site Request Forgery
            // Spring genera un token secreto en cada formulario y lo verifica al enviar
            .csrf(csrf -> csrf
                .csrfTokenRequestHandler((request, response, csrfToken) -> {
                    // Fuerza la inicialización del token CSRF antes de que Thymeleaf renderice el form
                    // Sin esto aparece error "Cannot create a session after the response has been committed"
                    CsrfToken token = csrfToken.get();
                    request.setAttribute(CsrfToken.class.getName(), token);
                    request.setAttribute(token.getParameterName(), token);
                })
                // Las rutas /api/** no necesitan CSRF (React llama directamente, no desde un form HTML)
                .ignoringRequestMatchers("/api/**")
            )
            // CORS: permite que React en localhost:5173 llame a las APIs de localhost:8080
            // Sin CORS el navegador bloquea las peticiones entre puertos diferentes
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(java.util.List.of("http://localhost:5173")); // solo React dev
                config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(java.util.List.of("*")); // acepta cualquier cabecera
                config.setAllowCredentials(true); // permite enviar cookies (sesión)
                return config;
            }))
            // Reglas de autorización: quién puede acceder a qué URL
            .authorizeHttpRequests(auth -> auth
                // Página raíz y páginas de autenticación: acceso libre para todos
                .requestMatchers("/", "/login", "/register", "/logout").permitAll()
                // Recursos estáticos (CSS, imágenes, React): acceso libre para todos
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/react/**").permitAll()
                // API de clasificación (React la llama sin login)
                .requestMatchers(HttpMethod.GET, "/api/tornei/**").permitAll()
                // Páginas de solo lectura (torneos, equipos, partidos): acceso libre
                .requestMatchers(HttpMethod.GET, "/tornei/**", "/squadre/**", "/partite/**").permitAll()
                // Rutas de administración: solo ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Comentarios: requiere estar autenticado (cualquier rol)
                .requestMatchers("/commenti/**").authenticated()
                // Todo lo demás: libre (OAuth2 callbacks, etc.)
                .anyRequest().permitAll()
            )
            // Configuración del formulario de login estándar (usuario + contraseña)
            .formLogin(login -> login
                .loginPage("/login")             // URL de la página de login personalizada
                .loginProcessingUrl("/login")    // URL donde se envía el formulario (POST)
                .successHandler(roleBasedSuccessHandler()) // redirige según el rol al entrar
                .failureUrl("/login?error")      // si falla el login, muestra error en el form
                .permitAll()
            )
            // Configuración del login con GitHub (OAuth2)
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")             // usa nuestra página de login personalizada
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService) // nuestro servicio que guarda el usuario en BD
                )
                .successHandler(roleBasedSuccessHandler()) // mismo redirect que el login normal
            )
            // Si alguien no autenticado intenta acceder a una ruta protegida, lo manda a /tornei
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendRedirect("/tornei"))
            )
            // Desactiva el logout por defecto de Spring (usamos nuestro propio GET /logout)
            .logout(AbstractHttpConfigurer::disable)
            // Gestión de sesiones: crea sesión solo si es necesario (no crea una por cada petición)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

        return http.build();
    }

    // Handler que redirige al usuario según su rol después del login:
    // - ADMIN → /admin/tornei (panel de administración)
    // - USER  → /tornei (lista pública de torneos)
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication auth) -> {
            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); // comprueba si tiene rol ADMIN
            response.sendRedirect(isAdmin ? "/admin/tornei" : "/tornei");
        };
    }

    // AuthenticationManager: componente de Spring Security que verifica usuario y contraseña
    // Lo configuramos con nuestro UserService (carga el usuario) y BCrypt (verifica la contraseña)
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userService).passwordEncoder(passwordEncoder);
        return builder.build();
    }
}
