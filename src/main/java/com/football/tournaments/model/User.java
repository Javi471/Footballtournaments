package com.football.tournaments.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// @Entity: esta clase es una tabla en PostgreSQL
// @Table(name="app_user"): la tabla se llama "app_user" porque "user" es palabra reservada en PostgreSQL
@Entity
@Table(name = "app_user")
// @Data: Lombok genera getters, setters y demás métodos automáticamente
@Data
// @NoArgsConstructor: genera el constructor vacío que JPA necesita
@NoArgsConstructor
public class User {

    // Clave primaria — PostgreSQL la genera automáticamente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El nombre de usuario debe ser único — no pueden existir dos "admin"
    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    // @Email: valida que el formato sea correcto (contiene @ y .)
    // unique = true: no pueden existir dos cuentas con el mismo email
    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    // La contraseña se guarda encriptada con BCrypt (PasswordConfig.java)
    // Nunca se guarda en texto plano
    @Column(nullable = false)
    private String password;

    // @Enumerated(STRING): guarda el rol como texto en BD ("USER" o "ADMIN")
    // Por defecto todo usuario nuevo es USER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    // Un usuario puede escribir muchos comentarios
    // cascade = ALL: si se borra el usuario, se borran sus comentarios también
    // orphanRemoval = true: si se quita un comentario de la lista, se borra de BD
    @OneToMany(mappedBy = "utente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commento> commenti;

    // Constructor para crear usuarios desde DataInitializer y UserService
    public User(String username, String email, String password, UserRole role) {
        this.username = username;
        this.email = email;
        this.password = password; // debe llegar ya encriptada desde UserService
        this.role = role;
    }
}
