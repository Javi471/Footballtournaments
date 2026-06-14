package com.football.tournaments.config;

import com.football.tournaments.service.CustomOAuth2UserService;
import com.football.tournaments.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // activa @PreAuthorize en los controllers
public class SecurityConfig {

    // @Lazy evita un bucle: SecurityConfig usa UserService y UserService usa SecurityConfig
    @Autowired @Lazy
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    // Todas las peticiones pasan por aquí antes de llegar al controller
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Genera el token CSRF antes de renderizar el formulario.
            // Sin esto las páginas con formulario salían en blanco. React (/api) no lo usa
            .csrf(csrf -> csrf
                .csrfTokenRequestHandler((request, response, csrfToken) -> {
                    CsrfToken token = csrfToken.get();
                    request.setAttribute(CsrfToken.class.getName(), token);
                    request.setAttribute(token.getParameterName(), token);
                })
                .ignoringRequestMatchers("/api/**")
            )

            // Permite que React (puerto 5173) llame a la API del 8080
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:5173"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))

            // Quién puede entrar a cada URL (se mira de arriba a abajo)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/react/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tornei/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/tornei/**", "/squadre/**", "/partite/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/commenti/**").authenticated()
                .anyRequest().permitAll()
            )

            // Login con usuario y contraseña
            .formLogin(login -> login
                .loginPage("/login")
                .successHandler(roleBasedSuccessHandler())
                .failureUrl("/login?error")
                .permitAll()
            )

            // Login con GitHub
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(ui -> ui.userService(customOAuth2UserService))
                .successHandler(roleBasedSuccessHandler())
            )

            // Si entras sin permiso a una ruta protegida, te manda a /tornei
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> res.sendRedirect("/tornei"))
            )

            // Uso mi propio /logout en AuthController, desactivo el de Spring
            .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    // Tras el login redirige: ADMIN → /admin/tornei, USER → /tornei
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, auth) -> {
            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(isAdmin ? "/admin/tornei" : "/tornei");
        };
    }

    // Carga el usuario de la BD y compara la contraseña con BCrypt
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userService).passwordEncoder(passwordEncoder);
        return builder.build();
    }
}
