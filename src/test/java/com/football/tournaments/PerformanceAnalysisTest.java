package com.football.tournaments;

import com.football.tournaments.model.Partita;
import com.football.tournaments.model.Torneo;
import com.football.tournaments.repository.PartitaRepository;
import com.football.tournaments.repository.TorneoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Analisi sperimentale delle prestazioni JPA — Punto 8.2 del progetto.
 *
 * Confronta tre strategie di fetch per il caso d'uso:
 *   "Caricare tutte le partite di un torneo con squadra home, away e arbitro"
 *
 * Strategie:
 *   1. LAZY  — nessun fetch anticipato  → genera il problema N+1
 *   2. JOIN FETCH — una sola query JPQL con JOIN FETCH esplicito
 *   3. EntityGraph — dichiarativo, stesso risultato di JOIN FETCH
 *
 * Per eseguire: ./mvnw test -Dtest=PerformanceAnalysisTest -pl .
 * I risultati appaiono nella console con il blocco ═══ RISULTATI ═══.
 */
@SpringBootTest
@ActiveProfiles("test")
public class PerformanceAnalysisTest {

    @Autowired
    private PartitaRepository partitaRepository;

    @Autowired
    private TorneoRepository torneoRepository;

    @PersistenceContext
    private EntityManager em;

    private static final int WARMUP = 5;
    private static final int RUNS   = 30;

    @Test
    @Transactional
    void confrontoStrategieFetch() {
        // ── Setup ────────────────────────────────────────────────────────────
        List<Torneo> tornei = torneoRepository.findAll();
        if (tornei.isEmpty()) {
            System.out.println("[PerformanceAnalysisTest] Nessun torneo trovato — eseguire DataInitializer prima.");
            return;
        }
        Torneo torneo = tornei.get(0);

        // Conta le partite per calcolare il numero atteso di query con N+1
        em.clear();
        List<Partita> sample = partitaRepository.findByTorneoLazy(torneo);
        int n = sample.size();
        int querieNPlus1 = 1 + (n * 3); // 1 lista + N*squadraHome + N*squadraAway + N*arbitro

        // ── Warmup — scalda JVM e pool connessioni ───────────────────────────
        for (int i = 0; i < WARMUP; i++) {
            em.clear();
            partitaRepository.findByTorneoWithTeamsAndReferee(torneo);
        }

        // ── Strategia 1: LAZY (N+1) ──────────────────────────────────────────
        // Una query per la lista + una query per ogni associazione LAZY acceduta
        long t0 = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            em.clear(); // svuota L1 cache per forzare hit su DB ogni volta
            List<Partita> list = partitaRepository.findByTorneoLazy(torneo);
            for (Partita p : list) {
                // Accedere alle associazioni LAZY le carica una alla volta → N+1
                p.getSquadraHome().getNome();
                p.getSquadraAway().getNome();
                p.getArbitro().getNome();
            }
        }
        double lazyMs = (System.nanoTime() - t0) / 1_000_000.0 / RUNS;

        // ── Strategia 2: JOIN FETCH ───────────────────────────────────────────
        // JPQL con JOIN FETCH: Hibernate produce una sola query con LEFT JOIN
        long t1 = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            em.clear();
            List<Partita> list = partitaRepository.findByTorneoWithTeamsAndReferee(torneo);
            for (Partita p : list) {
                p.getSquadraHome().getNome(); // già caricato, nessuna query extra
                p.getSquadraAway().getNome();
                p.getArbitro().getNome();
            }
        }
        double joinFetchMs = (System.nanoTime() - t1) / 1_000_000.0 / RUNS;

        // ── Strategia 3: EntityGraph ──────────────────────────────────────────
        // Approccio dichiarativo: stessa ottimizzazione di JOIN FETCH, più flessibile
        long t2 = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            em.clear();
            List<Partita> list = partitaRepository.findByTorneoWithEntityGraph(torneo);
            for (Partita p : list) {
                p.getSquadraHome().getNome();
                p.getSquadraAway().getNome();
                p.getArbitro().getNome();
            }
        }
        double entityGraphMs = (System.nanoTime() - t2) / 1_000_000.0 / RUNS;

        // ── Risultati ─────────────────────────────────────────────────────────
        double speedupJF = lazyMs / joinFetchMs;
        double speedupEG = lazyMs / entityGraphMs;

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║         ANALISI PRESTAZIONI — Strategie JPA Fetch (8.2)          ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf( "║  Torneo: %-20s  Partite: %2d  Esecuzioni: %2d          ║%n",
                torneo.getNome(), n, RUNS);
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf( "║  Strategia       │  Tempo medio  │  Query/esec  │  Speedup      ║%n");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf( "║  LAZY (N+1)      │  %8.3f ms  │  %3d         │  1.00x (base) ║%n",
                lazyMs, querieNPlus1);
        System.out.printf( "║  JOIN FETCH      │  %8.3f ms  │    1         │  %5.2fx        ║%n",
                joinFetchMs, speedupJF);
        System.out.printf( "║  EntityGraph     │  %8.3f ms  │    1         │  %5.2fx        ║%n",
                entityGraphMs, speedupEG);
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf( "║  Risparmio query: da %d a 1 per caricamento (-%d query/esec)%n",
                querieNPlus1, querieNPlus1 - 1);
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
