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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRequestHandler((request, response, csrfToken) -> {
                    // Force eager CSRF token initialization inside CsrfFilter, before any
                    // response body is written. The default handlers defer this lazily, which
                    // causes "Cannot create a session after the response has been committed"
                    // when Thymeleaf renders a <form th:action> mid-stream.
                    CsrfToken token = csrfToken.get();
                    request.setAttribute(CsrfToken.class.getName(), token);
                    request.setAttribute(token.getParameterName(), token);
                })
                .ignoringRequestMatchers("/api/**")
            )
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
                config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(java.util.List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                // Página raíz y recursos estáticos
                .requestMatchers("/", "/login", "/register", "/logout").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/react/**").permitAll()
                // API pública (React classifica)
                .requestMatchers(HttpMethod.GET, "/api/tornei/**").permitAll()
                // Vistas públicas de solo lectura
                .requestMatchers(HttpMethod.GET, "/tornei/**", "/squadre/**", "/partite/**").permitAll()
                // Admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Comentarios requieren login
                .requestMatchers("/commenti/**").authenticated()
                // Todo lo demás es público
                .anyRequest().permitAll()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(roleBasedSuccessHandler())
                .failureUrl("/login?error")
                .permitAll()
            )
            // OAuth2: login con GitHub
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")                          // usa nuestra página de login personalizada
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)     // nuestro servicio que guarda el usuario en BD
                )
                .successHandler(roleBasedSuccessHandler())   // mismo redirect que el login normal
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendRedirect("/tornei"))
            )
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication auth) -> {
            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(isAdmin ? "/admin/tornei" : "/tornei");
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userService).passwordEncoder(passwordEncoder);
        return builder.build();
    }
}
