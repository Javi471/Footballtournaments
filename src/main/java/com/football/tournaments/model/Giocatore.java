package com.football.tournaments.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity

@Data

@NoArgsConstructor
public class Giocatore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;     

    @NotBlank
    private String cognome;  

    @NotNull
    private LocalDate dataNascita; 

    @NotBlank
    private String ruolo; 

    private Integer altezza; 

    // @ManyToOne: muchos jugadores pertenecen a un mismo equipo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squadra_id", nullable = false)
    private Squadra squadra; 

    public Giocatore(String nome, String cognome, LocalDate dataNascita, String ruolo, Integer altezza, Squadra squadra) {
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.ruolo = ruolo;
        this.altezza = altezza;
        this.squadra = squadra;
    }
}
