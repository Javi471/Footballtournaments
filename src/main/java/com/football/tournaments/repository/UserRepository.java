package com.football.tournaments.repository;

import com.football.tournaments.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository: le dice a Spring que este archivo es un repositorio (acceso a BD)
// extends JpaRepository<User, Long>: hereda findAll(), findById(), save(), delete()...
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring genera: SELECT * FROM app_user WHERE username = ?
    // Usado por UserService.loadUserByUsername() cuando alguien hace login con usuario
    Optional<User> findByUsername(String username);

    // Spring genera: SELECT * FROM app_user WHERE email = ?
    // Usado por CustomOAuth2UserService para buscar usuario al hacer login con GitHub
    Optional<User> findByEmail(String email);

    // Spring genera: SELECT COUNT(*) > 0 FROM app_user WHERE username = ?
    // Devuelve true si ya existe un usuario con ese nombre — evita duplicados al registrarse
    boolean existsByUsername(String username);

    // Spring genera: SELECT COUNT(*) > 0 FROM app_user WHERE email = ?
    // Devuelve true si ya existe una cuenta con ese email — evita duplicados al registrarse
    boolean existsByEmail(String email);
}
