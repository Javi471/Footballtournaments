package com.football.tournaments.model;

// Enum: lista de roles posibles para un usuario
// No es una tabla en BD — se guarda como texto dentro de la tabla "app_user"
public enum UserRole {
    USER,   // usuario normal: puede ver contenido y escribir comentarios
    ADMIN   // administrador: puede crear/editar/borrar torneos, equipos, partidos y árbitros
}
