package com.football.tournaments.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// @Entity: esta clase es la tabla "torneo" en PostgreSQL
// @Data: Lombok genera getters, setters y demás métodos automáticamente
// @NoArgsConstructor: genera el constructor vacío que JPA necesita
@Entity
@Data
@NoArgsConstructor
public class Torneo {

    // Clave primaria — PostgreSQL la genera automáticamente (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank: el nombre no puede estar vacío
    @NotBlank
    @Column(nullable = false)
    private String nome;

    // @NotNull: el año es obligatorio
    @NotNull
    @Column(nullable = false)
    private Integer anno;

    // Descripción larga del torneo — TEXT permite más de 255 caracteres
    @Column(columnDefinition = "TEXT")
    private String descrizione;

    // @ManyToMany: un torneo tiene muchos equipos y un equipo puede estar en muchos torneos
    // @JoinTable: crea la tabla intermedia "torneo_squadra" con las columnas torneo_id y squadra_id
    // fetch = LAZY: los equipos no se cargan de BD hasta que los necesitas
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "torneo_squadra",
        joinColumns = @JoinColumn(name = "torneo_id"),
        inverseJoinColumns = @JoinColumn(name = "squadra_id")
    )
    private List<Squadra> squadre = new ArrayList<>();

    // @JsonIgnore: evita el bucle Torneo → Partita → Torneo → ... al convertir a JSON
    // cascade = ALL: si se borra el torneo, se borran sus partidos también
    @JsonIgnore
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Partita> partite = new ArrayList<>();

    // Constructor para crear torneos desde DataInitializer
    public Torneo(String nome, Integer anno, String descrizione) {
        this.nome = nome;
        this.anno = anno;
        this.descrizione = descrizione;
    }
}
