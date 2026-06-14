package com.football.tournaments;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// @SpringBootTest = arranca Spring Boot completo para ejecutar el test
// (carga todos los beans, configuracion, base de datos, etc.)
@SpringBootTest
// @ActiveProfiles("test") = usa application-test.properties en vez de application.properties
// es decir, usa H2 en memoria en lugar de PostgreSQL
@ActiveProfiles("test")
class FootballTournamentsApplicationTests {

    // @Test = marca este metodo como un test que JUnit debe ejecutar
    @Test
    void contextLoads() {
        // Este metodo esta vacio a proposito
        // Solo comprueba que Spring Boot arranca sin ningun error de configuracion
        // Si algun @Autowired falla, algun Bean no se puede crear, o hay un error
        // en application.properties, este test lo detecta y falla
        // Es como encender el coche y comprobar que no hay humo antes de conducir
    }
}
