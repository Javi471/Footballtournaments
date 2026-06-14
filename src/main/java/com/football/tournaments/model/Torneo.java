package com.football.tournaments.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @NotNull
    @Column(nullable = false)
    private Integer anno;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "torneo_squadra",
        joinColumns = @JoinColumn(name = "torneo_id"),
        inverseJoinColumns = @JoinColumn(name = "squadra_id")
    )
    private List<Squadra> squadre = new ArrayList<>();

    // @JsonIgnore: evita el bucle Torneo → Partita → Torneo → ... al convertir a JSON
    @JsonIgnore
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Partita> partite = new ArrayList<>();

    public Torneo(String nome, Integer anno, String descrizione) {
        this.nome = nome;
        this.anno = anno;
        this.descrizione = descrizione;
    }
}
