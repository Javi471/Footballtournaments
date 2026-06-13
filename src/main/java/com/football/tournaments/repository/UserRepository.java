package com.football.tournaments.repository;

import com.football.tournaments.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);   // usado por OAuth para buscar usuario por email
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
