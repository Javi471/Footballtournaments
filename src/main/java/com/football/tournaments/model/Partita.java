package com.football.tournaments.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// @Entity: esta clase es la tabla "partita" (partido) en PostgreSQL
@Entity
// @Data: Lombok genera getters, setters y demás métodos automáticamente
@Data
// @NoArgsConstructor: genera el constructor vacío que JPA necesita
@NoArgsConstructor
public class Partita {

    // Clave primaria — PostgreSQL la genera automáticamente
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fecha y hora del partido (ej: 2024-10-26T16:15)
    @NotNull
    private LocalDateTime dataOra;

    // Lugar donde se juega el partido (ej: "Camp Nou, Barcelona")
    private String luogo;

    // Goles del equipo local — empieza en 0 hasta que se registra el resultado
    private Integer goalsHome = 0;

    // Goles del equipo visitante — empieza en 0 hasta que se registra el resultado
    private Integer goalsAway = 0;

    // @Enumerated(STRING): guarda el estado como texto en BD ("SCHEDULED", "PLAYED", "CANCELLED")
    // Por defecto el partido está programado (SCHEDULED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoPartita stato = StatoPartita.SCHEDULED;

    // @ManyToOne: muchos partidos pertenecen a un mismo torneo
    // @JoinColumn: columna "torneo_id" en la tabla "partita"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo; // torneo al que pertenece este partido

    // Equipo que juega en casa — columna "squadra_home_id"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squadra_home_id", nullable = false)
    private Squadra squadraHome; // equipo local

    // Equipo visitante — columna "squadra_away_id"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squadra_away_id", nullable = false)
    private Squadra squadraAway; // equipo visitante

    // Árbitro que dirige el partido — columna "arbitro_id"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arbitro_id", nullable = false)
    private Arbitro arbitro; // árbitro del partido

    // Un partido puede tener muchos comentarios
    // cascade = ALL: si se borra el partido, se borran sus comentarios también
    // orphanRemoval = true: si se quita un comentario de la lista, se borra de BD también
    @OneToMany(mappedBy = "partita", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commento> commenti = new ArrayList<>(); // comentarios del partido

    // Constructor para crear partidos desde DataInitializer
    public Partita(LocalDateTime dataOra, String luogo, Torneo torneo, Squadra squadraHome, Squadra squadraAway, Arbitro arbitro) {
        this.dataOra = dataOra;
        this.luogo = luogo;
        this.torneo = torneo;
        this.squadraHome = squadraHome;
        this.squadraAway = squadraAway;
        this.arbitro = arbitro;
        // stato y goles se quedan en sus valores por defecto (SCHEDULED, 0, 0)
    }
}
