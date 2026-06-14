package com.football.tournaments.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// @Entity: le dice a Spring que esta clase es una tabla en PostgreSQL (tabla: "arbitro")
@Entity
// @Data: Lombok genera automáticamente los métodos getId(), setId(), getNome()... sin que los escribas
@Data
// @NoArgsConstructor: Lombok genera un constructor vacío — new Arbitro() — que JPA necesita
@NoArgsConstructor
public class Arbitro {

    // @Id: esta es la clave primaria de la tabla (columna "id")
    @Id
    // @GeneratedValue: PostgreSQL asigna el id automáticamente (1, 2, 3...)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank: Spring no permite guardar un árbitro si el nombre está vacío
    @NotBlank
    private String nome;       // nombre del árbitro

    @NotBlank
    private String cognome;    // apellido del árbitro

    // @Column(unique=true): no pueden existir dos árbitros con el mismo código
    @NotBlank
    @Column(unique = true, nullable = false)
    private String codiceArbitrale; // código único del árbitro (ej: "ES-001")

    // Un árbitro puede dirigir muchos partidos (relación 1 árbitro → muchos partidos)
    // fetch = LAZY: los partidos NO se cargan de la BD hasta que los necesitas
    @OneToMany(mappedBy = "arbitro", fetch = FetchType.LAZY)
    private List<Partita> partite; // lista de partidos que ha dirigido

    // Constructor para crear árbitros con datos desde DataInitializer
    public Arbitro(String nome, String cognome, String codiceArbitrale) {
        this.nome = nome;
        this.cognome = cognome;
        this.codiceArbitrale = codiceArbitrale;
    }
}
