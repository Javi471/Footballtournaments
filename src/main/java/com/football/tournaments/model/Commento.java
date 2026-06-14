package com.football.tournaments.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// @Entity: esta clase es la tabla "commento" en PostgreSQL
@Entity
// @Data: Lombok genera getters, setters, equals, hashCode y toString automáticamente
@Data
// @NoArgsConstructor: genera constructor vacío que JPA necesita para crear objetos desde BD
@NoArgsConstructor
public class Commento {

    // Clave primaria generada automáticamente por PostgreSQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(columnDefinition = "TEXT"): permite textos largos (más de 255 caracteres)
    // nullable = false: el comentario no puede estar vacío en la BD
    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String testo; // texto del comentario escrito por el usuario

    // Fecha y hora en que se creó el comentario — se asigna automáticamente al crear
    @Column(nullable = false)
    private LocalDateTime dataCreazione = LocalDateTime.now();

    // @ManyToOne: muchos comentarios pertenecen a un mismo usuario
    // @JoinColumn: en la tabla "commento" hay una columna "utente_id" con el id del usuario
    // fetch = LAZY: el usuario no se carga de BD hasta que lo necesitas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_id", nullable = false)
    private User utente; // usuario que escribió el comentario

    // @ManyToOne: muchos comentarios pertenecen a un mismo partido
    // @JoinColumn: columna "partita_id" en la tabla "commento"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partita_id", nullable = false)
    private Partita partita; // partido sobre el que se comenta

    // Constructor para crear comentarios desde el controller
    public Commento(String testo, User utente, Partita partita) {
        this.testo = testo;
        this.utente = utente;
        this.partita = partita;
        this.dataCreazione = LocalDateTime.now(); // guarda la fecha actual
    }
}
