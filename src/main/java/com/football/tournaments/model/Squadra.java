package com.football.tournaments.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// @Entity: esta clase es la tabla "squadra" (equipo) en PostgreSQL
// @Data: Lombok genera getters, setters y demás métodos automáticamente
// @NoArgsConstructor: genera el constructor vacío que JPA necesita
@Entity
@Data
@NoArgsConstructor
public class Squadra {

    // Clave primaria — PostgreSQL la genera automáticamente (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank: el nombre no puede estar vacío
    @NotBlank
    @Column(nullable = false)
    private String nome;

    // @NotNull: el año de fundación es obligatorio
    @NotNull
    private Integer annoFondazione;

    // @NotBlank: la ciudad no puede estar vacía
    @NotBlank
    private String citta;

    // Ruta de la imagen del equipo (guardada en /images/squadre/)
    private String imagePath;

    // @JsonIgnore: evita el bucle infinito al convertir a JSON (Squadra → Giocatore → Squadra...)
    // cascade = ALL: si se borra el equipo, se borran sus jugadores también
    @JsonIgnore
    @OneToMany(mappedBy = "squadra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Giocatore> giocatori = new ArrayList<>();

    // @JsonIgnore: evita el bucle infinito al convertir a JSON (Squadra → Torneo → Squadra...)
    // mappedBy = "squadre": la relación ya está definida en Torneo.java, aquí solo la leemos
    @JsonIgnore
    @ManyToMany(mappedBy = "squadre")
    private List<Torneo> tornei = new ArrayList<>();

    // Constructor para crear equipos desde DataInitializer
    public Squadra(String nome, Integer annoFondazione, String citta) {
        this.nome = nome;
        this.annoFondazione = annoFondazione;
        this.citta = citta;
    }
}
