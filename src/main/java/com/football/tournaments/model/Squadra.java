package com.football.tournaments.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Squadra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @NotNull
    private Integer annoFondazione;

    @NotBlank
    private String citta;

    // Ruta de la imagen del equipo (guardada en /images/squadre/)
    private String imagePath;

    @OneToMany(mappedBy = "squadra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Giocatore> giocatori = new ArrayList<>();

    @ManyToMany(mappedBy = "squadre")
    private List<Torneo> tornei = new ArrayList<>();

    public Squadra(String nome, Integer annoFondazione, String citta) {
        this.nome = nome;
        this.annoFondazione = annoFondazione;
        this.citta = citta;
    }
}
