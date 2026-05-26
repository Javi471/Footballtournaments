package com.football.tournaments.config;

import com.football.tournaments.model.*;
import com.football.tournaments.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private TorneoRepository torneoRepository;
    @Autowired private SquadraRepository squadraRepository;
    @Autowired private GiocatoreRepository giocatoreRepository;
    @Autowired private ArbitroRepository arbitroRepository;
    @Autowired private PartitaRepository partitaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // Utenti
        userRepository.save(new User("admin", "admin@football.com", passwordEncoder.encode("Admin123"), UserRole.ADMIN));
        userRepository.save(new User("user1", "user1@football.com", passwordEncoder.encode("User1234"), UserRole.USER));

        // Torneo
        Torneo torneo = torneoRepository.save(new Torneo("Serie A Amatoriale", 2026, "Torneo amatoriale primaverile"));

        // Squadre
        Squadra roma = squadraRepository.save(new Squadra("FC Roma Amatori", 2010, "Roma"));
        Squadra milano = squadraRepository.save(new Squadra("Milano United", 2008, "Milano"));
        Squadra napoli = squadraRepository.save(new Squadra("Napoli Stars", 2012, "Napoli"));
        Squadra torino = squadraRepository.save(new Squadra("Torino FC Amatori", 2015, "Torino"));

        torneo.getSquadre().addAll(java.util.List.of(roma, milano, napoli, torino));
        torneoRepository.save(torneo);

        // Giocatori
        giocatoreRepository.save(new Giocatore("Marco", "Rossi", LocalDate.of(1995, 3, 15), "Portiere", 185, roma));
        giocatoreRepository.save(new Giocatore("Luca", "Bianchi", LocalDate.of(1998, 7, 22), "Difensore", 180, roma));
        giocatoreRepository.save(new Giocatore("Alessandro", "Ferrari", LocalDate.of(1993, 11, 8), "Attaccante", 178, milano));
        giocatoreRepository.save(new Giocatore("Giovanni", "Esposito", LocalDate.of(2000, 1, 30), "Centrocampista", 175, napoli));

        // Arbitro
        Arbitro arbitro = arbitroRepository.save(new Arbitro("Carlo", "Verdi", "ARB-001"));

        // Partite
        partitaRepository.save(new Partita(
            LocalDateTime.of(2026, 6, 1, 15, 0), "Stadio Comunale Roma",
            torneo, roma, milano, arbitro));
        Partita giocata = new Partita(
            LocalDateTime.of(2026, 5, 20, 16, 0), "Campo Nord Milano",
            torneo, napoli, torino, arbitro);
        giocata.setGoalsHome(2);
        giocata.setGoalsAway(1);
        giocata.setStato(StatoPartita.PLAYED);
        partitaRepository.save(giocata);
    }
}
