package com.football.tournaments.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// @Entity: esta clase es la tabla "giocatore" (jugador) en PostgreSQL
@Entity
// @Data: Lombok genera getters, setters y demás métodos automáticamente
@Data
// @NoArgsConstructor: genera el constructor vacío que JPA necesita
@NoArgsConstructor
public class Giocatore {

    // Clave primaria — PostgreSQL la genera automáticamente (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank: el nombre no puede ser vacío ni solo espacios
    @NotBlank
    private String nome;     // nombre del jugador

    @NotBlank
    private String cognome;  // apellido del jugador

    // @NotNull: la fecha de nacimiento es obligatoria
    @NotNull
    private LocalDate dataNascita; // fecha de nacimiento (ej: 1997-01-13)

    // @NotBlank: la posición es obligatoria (Portiere, Difensore, Centrocampista, Attaccante)
    @NotBlank
    private String ruolo; // posición en el campo

    // La altura en cm es opcional (puede ser null)
    private Integer altezza; // altura en centímetros

    // @ManyToOne: muchos jugadores pertenecen a un mismo equipo
    // fetch = LAZY: el equipo no se carga de BD hasta que lo necesitas
    // @JoinColumn: en la tabla "giocatore" hay una columna "squadra_id" con el id del equipo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squadra_id", nullable = false)
    private Squadra squadra; // equipo al que pertenece el jugador

    // Constructor para crear jugadores desde DataInitializer
    public Giocatore(String nome, String cognome, LocalDate dataNascita, String ruolo, Integer altezza, Squadra squadra) {
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.ruolo = ruolo;
        this.altezza = altezza;
        this.squadra = squadra;
    }
}
