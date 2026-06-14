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
 * Test de rendimiento que compara 3 formas distintas de cargar datos de la BD.
 * Corresponde al punto 8.2 del proyecto (analisis de prestaciones JPA).
 *
 * El problema que resuelve: cuando cargamos partidos con sus equipos y arbitro,
 * podemos hacerlo de 3 formas con rendimientos muy distintos.
 *
 * Para ejecutarlo manualmente: ./mvnw test -Dtest=PerformanceAnalysisTest
 * Los resultados aparecen en consola con el bloque de tabla al final.
 */
// Arranca Spring Boot completo para el test
@SpringBootTest
// Usa application-test.properties (H2 en RAM) en lugar de PostgreSQL
@ActiveProfiles("test")
public class PerformanceAnalysisTest {

    // Inyectamos los repositorios que necesitamos para consultar la BD
    @Autowired
    private PartitaRepository partitaRepository;

    @Autowired
    private TorneoRepository torneoRepository;

    // EntityManager = herramienta de JPA para gestionar entidades directamente
    // Lo usamos para limpiar la cache L1 entre ejecuciones (em.clear())
    // asi forzamos que cada consulta vaya de verdad a la BD y no use cache
    @PersistenceContext
    private EntityManager em;

    // Numero de veces que calentamos la JVM antes de medir (para resultados fiables)
    private static final int WARMUP = 5;
    // Numero de veces que repetimos cada estrategia para calcular el tiempo medio
    private static final int RUNS = 30;

    @Test
    @Transactional // necesario para poder acceder a colecciones LAZY dentro del test
    void confrontoStrategieFetch() {

        // ── Preparacion ──────────────────────────────────────────────────────
        // Cogemos el primer torneo de la BD para usarlo como caso de prueba
        List<Torneo> tornei = torneoRepository.findAll();
        if (tornei.isEmpty()) {
            System.out.println("[PerformanceAnalysisTest] No hay torneos — ejecutar DataInitializer primero.");
            return;
        }
        Torneo torneo = tornei.get(0);

        // Calculamos cuantas consultas SQL genera el problema N+1
        // 1 consulta para la lista + N consultas por cada asociacion accedida (home, away, arbitro)
        em.clear();
        List<Partita> sample = partitaRepository.findByTorneoLazy(torneo);
        int n = sample.size();
        int querieNPlus1 = 1 + (n * 3); // 1 lista + N*squadraHome + N*squadraAway + N*arbitro

        // ── Calentamiento ────────────────────────────────────────────────────
        // Ejecutamos varias veces sin medir para que la JVM y el pool de
        // conexiones esten "calientes" y los tiempos sean mas fiables
        for (int i = 0; i < WARMUP; i++) {
            em.clear();
            partitaRepository.findByTorneoWithTeamsAndReferee(torneo);
        }

        // ── Estrategia 1: LAZY — el problema N+1 ─────────────────────────────
        // Carga la lista de partidos con 1 consulta, pero luego por cada partido
        // hace 3 consultas mas (squadraHome, squadraAway, arbitro) → muy ineficiente
        long t0 = System.nanoTime(); // guardamos el tiempo de inicio en nanosegundos
        for (int i = 0; i < RUNS; i++) {
            em.clear(); // vaciamos la cache L1 para que cada vuelta vaya a la BD de verdad
            List<Partita> list = partitaRepository.findByTorneoLazy(torneo);
            for (Partita p : list) {
                // Al acceder a estas propiedades LAZY, Hibernate lanza una consulta
                // SQL extra por cada una → aqui es donde se genera el problema N+1
                p.getSquadraHome().getNome();
                p.getSquadraAway().getNome();
                p.getArbitro().getNome();
            }
        }
        // Calculamos el tiempo medio por ejecucion en milisegundos
        double lazyMs = (System.nanoTime() - t0) / 1_000_000.0 / RUNS;

        // ── Estrategia 2: JOIN FETCH ──────────────────────────────────────────
        // Carga todo en UNA sola consulta SQL usando JOIN FETCH en JPQL
        // Hibernate une las tablas y trae todos los datos de golpe → mucho mas rapido
        long t1 = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            em.clear();
            List<Partita> list = partitaRepository.findByTorneoWithTeamsAndReferee(torneo);
            for (Partita p : list) {
                // Ya estan cargados del JOIN, no genera consultas extra
                p.getSquadraHome().getNome();
                p.getSquadraAway().getNome();
                p.getArbitro().getNome();
            }
        }
        double joinFetchMs = (System.nanoTime() - t1) / 1_000_000.0 / RUNS;

        // ── Estrategia 3: EntityGraph ─────────────────────────────────────────
        // Alternativa declarativa a JOIN FETCH — mismo resultado pero se configura
        // con anotaciones en lugar de escribir JPQL manualmente
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

        // ── Calculo del speedup ───────────────────────────────────────────────
        // Cuantas veces mas rapido es JOIN FETCH y EntityGraph respecto a LAZY
        double speedupJF = lazyMs / joinFetchMs;
        double speedupEG = lazyMs / entityGraphMs;

        // ── Impresion de resultados en consola ────────────────────────────────
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║         ANALISI PRESTAZIONI — Strategie JPA Fetch (8.2)          ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf( "║  Torneo: %-20s  Partite: %2d  Esecuzioni: %2d          ║%n",
                torneo.getNome(), n, RUNS);
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf( "║  Estrategia      │  Tiempo medio │  Consultas   │  Speedup      ║%n");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf( "║  LAZY (N+1)      │  %8.3f ms  │  %3d         │  1.00x (base) ║%n",
                lazyMs, querieNPlus1);
        System.out.printf( "║  JOIN FETCH      │  %8.3f ms  │    1         │  %5.2fx        ║%n",
                joinFetchMs, speedupJF);
        System.out.printf( "║  EntityGraph     │  %8.3f ms  │    1         │  %5.2fx        ║%n",
                entityGraphMs, speedupEG);
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf( "║  Ahorro de consultas: de %d a 1 por carga (-%d consultas/vez)%n",
                querieNPlus1, querieNPlus1 - 1);
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
