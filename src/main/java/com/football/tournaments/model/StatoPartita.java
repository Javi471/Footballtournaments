package com.football.tournaments.model;

// Enum: no es una tabla en BD, es una lista de valores fijos posibles
// Se usa en Partita.stato para saber en qué estado está el partido
public enum StatoPartita {
    SCHEDULED,   // partido programado pero aún no jugado
    PLAYED,      // partido ya jugado (tiene resultado: goalsHome y goalsAway)
    CANCELLED    // partido cancelado
}
