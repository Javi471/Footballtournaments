package com.football.tournaments.config;

import com.football.tournaments.model.*;
import com.football.tournaments.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository       userRepository;
    @Autowired private TorneoRepository     torneoRepository;
    @Autowired private SquadraRepository    squadraRepository;
    @Autowired private GiocatoreRepository  giocatoreRepository;
    @Autowired private ArbitroRepository    arbitroRepository;
    @Autowired private PartitaRepository    partitaRepository;
    @Autowired private CommentoRepository   commentoRepository;
    @Autowired private PasswordEncoder      passwordEncoder;

    @Override
    public void run(String... args) {

        // Users — created only once
        if (userRepository.count() == 0) {
            userRepository.save(new User("admin", "admin@football.com",
                    passwordEncoder.encode("Admin123"), UserRole.ADMIN));
            userRepository.save(new User("user1", "user1@football.com",
                    passwordEncoder.encode("User123"), UserRole.USER));
        }

        // Seed real data only if not already present
        boolean seeded = torneoRepository.findAll().stream()
                .anyMatch(t -> t.getNome().equals("La Liga 2024-25"));
        if (!seeded) {
            commentoRepository.deleteAll();
            partitaRepository.deleteAll();
            torneoRepository.clearAllLinks();
            torneoRepository.deleteAll();
            giocatoreRepository.deleteAll();
            arbitroRepository.deleteAll();
            squadraRepository.deleteAll();
            seedData();
        }

        // Startup banner
        String line = "═".repeat(52);
        System.out.println("\n\033[36m╔" + line + "╗");
        System.out.println("║        ⚽  FOOTBALL TOURNAMENTS — READY          ║");
        System.out.println("╠" + line + "╣");
        System.out.println("║  🌐  http://localhost:8080                       ║");
        System.out.println("╠" + line + "╣");
        System.out.println("║  👤  ADMIN     user: admin    pass: Admin123     ║");
        System.out.println("║  👤  USER      user: user1    pass: User123      ║");
        System.out.println("╚" + line + "╝\033[0m\n");
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void seedData() {

        // ── Referees (one per nationality) ───────────────────────────────────
        Arbitro cerro  = arb("Carlos",  "Del Cerro Grande", "ES-001"); // Spanish
        Arbitro oliver = arb("Michael", "Oliver",           "EN-001"); // English
        Arbitro massa  = arb("Davide",  "Massa",            "IT-001"); // Italian

        // ═════════════════════════════════════════════════════════════════════
        // LA LIGA 2024-25  (Final standings: Barça, Real Madrid, Atlético,
        //                   Athletic Club, Villarreal, Real Betis)
        // ═════════════════════════════════════════════════════════════════════
        Torneo laLiga = torneo("La Liga 2024-25", 2025,
                "Spanish Primera División — 2024-25 season");

        Squadra barca = club("FC Barcelona", 1899, "Barcelona");
        p(barca, "Marc-André",  "ter Stegen",    1992,  4, 30, "Portiere",       187);
        p(barca, "Iñaki",       "Peña",           1999,  3,  2, "Portiere",       190);
        p(barca, "Jules",       "Koundé",         1998, 11, 12, "Difensore",      178);
        p(barca, "Pau",         "Cubarsí",        2007,  1, 22, "Difensore",      182);
        p(barca, "Alejandro",   "Balde",          2003, 10, 18, "Difensore",      175);
        p(barca, "Iñigo",       "Martínez",       1991,  5, 17, "Difensore",      181);
        p(barca, "Ronald",      "Araújo",         1999,  3,  7, "Difensore",      188);
        p(barca, "Eric",        "García",         2001,  1,  9, "Difensore",      182);
        p(barca, "Pedri",       "González",       2002, 11, 25, "Centrocampista", 174);
        p(barca, "Frenkie",     "de Jong",        1997,  5, 12, "Centrocampista", 180);
        p(barca, "Gavi",        "Páez",           2004,  8,  5, "Centrocampista", 173);
        p(barca, "Dani",        "Olmo",           2000,  5,  7, "Centrocampista", 180);
        p(barca, "Marc",        "Casadó",         2003,  8, 30, "Centrocampista", 180);
        p(barca, "Fermín",      "López",          2003,  9, 15, "Centrocampista", 175);
        p(barca, "Pablo",       "Torre",          2003,  6, 24, "Centrocampista", 180);
        p(barca, "Robert",      "Lewandowski",    1988,  8, 21, "Attaccante",     184);
        p(barca, "Raphinha",    "Belloli",        1996, 12, 14, "Attaccante",     176);
        p(barca, "Lamine",      "Yamal",          2007,  7, 16, "Attaccante",     176);
        p(barca, "Ferran",      "Torres",         2000,  2, 29, "Attaccante",     184);
        p(barca, "Ansu",        "Fati",           2002, 10, 31, "Attaccante",     177);
        p(barca, "Vitor",       "Roque",          2005,  2, 28, "Attaccante",     172);

        Squadra realMadrid = club("Real Madrid CF", 1902, "Madrid");
        p(realMadrid, "Thibaut",   "Courtois",    1992,  5, 11, "Portiere",       199);
        p(realMadrid, "Andriy",    "Lunin",       1999,  2, 11, "Portiere",       191);
        p(realMadrid, "Éder",      "Militão",     1998,  1, 18, "Difensore",      186);
        p(realMadrid, "Antonio",   "Rüdiger",     1993,  3,  3, "Difensore",      190);
        p(realMadrid, "Ferland",   "Mendy",       1995,  6,  8, "Difensore",      180);
        p(realMadrid, "Dani",      "Carvajal",    1992,  1, 11, "Difensore",      173);
        p(realMadrid, "Lucas",     "Vázquez",     1991,  7,  1, "Difensore",      173);
        p(realMadrid, "Raúl",      "Asencio",     2003,  4, 20, "Difensore",      186);
        p(realMadrid, "Fran",      "García",      2001,  8, 14, "Difensore",      170);
        p(realMadrid, "Luka",      "Modrić",      1985,  9,  9, "Centrocampista", 172);
        p(realMadrid, "Federico",  "Valverde",    1998,  7, 22, "Centrocampista", 182);
        p(realMadrid, "Eduardo",   "Camavinga",   2002, 11, 10, "Centrocampista", 182);
        p(realMadrid, "Aurélien",  "Tchouaméni",  2000,  1, 27, "Centrocampista", 188);
        p(realMadrid, "Jude",      "Bellingham",  2003,  6, 29, "Centrocampista", 181);
        p(realMadrid, "Brahim",    "Díaz",        1999,  8,  3, "Centrocampista", 172);
        p(realMadrid, "Dani",      "Ceballos",    1996,  8,  7, "Centrocampista", 180);
        p(realMadrid, "Kylian",    "Mbappé",      1998, 12, 20, "Attaccante",     178);
        p(realMadrid, "Vinícius",  "Júnior",      2000,  7, 12, "Attaccante",     176);
        p(realMadrid, "Rodrygo",   "Goes",        2001,  1,  9, "Attaccante",     174);
        p(realMadrid, "Endrick",   "Felipe",      2006,  7, 21, "Attaccante",     174);
        p(realMadrid, "Arda",      "Güler",       2005,  2, 25, "Attaccante",     175);

        Squadra atletico = club("Atlético de Madrid", 1903, "Madrid");
        p(atletico, "Jan",         "Oblak",       1993,  1,  7, "Portiere",       188);
        p(atletico, "Antonio",     "Grbić",       1998,  9,  4, "Portiere",       193);
        p(atletico, "José María",  "Giménez",     1995,  1, 20, "Difensore",      185);
        p(atletico, "Robin",       "Le Normand",  1996, 11, 11, "Difensore",      186);
        p(atletico, "Reinildo",    "Mandava",     1994,  1, 21, "Difensore",      182);
        p(atletico, "Nahuel",      "Molina",      1998,  4,  6, "Difensore",      176);
        p(atletico, "Javi",        "Galán",       1997, 11, 19, "Difensore",      176);
        p(atletico, "Marcos",      "Llorente",    1995,  1, 30, "Centrocampista", 182);
        p(atletico, "Rodrigo",     "De Paul",     1994,  5, 24, "Centrocampista", 179);
        p(atletico, "Koke",        "Resurrección",1992,  1,  8, "Centrocampista", 176);
        p(atletico, "Conor",       "Gallagher",   2000,  2,  6, "Centrocampista", 181);
        p(atletico, "Pablo",       "Barrios",     2003,  6,  7, "Centrocampista", 177);
        p(atletico, "Axel",        "Witsel",      1989,  1, 12, "Centrocampista", 185);
        p(atletico, "Thomas",      "Lemar",       1995, 11, 12, "Centrocampista", 172);
        p(atletico, "Antoine",     "Griezmann",   1991,  3, 21, "Attaccante",     176);
        p(atletico, "Julián",      "Álvarez",     2000,  1, 31, "Attaccante",     170);
        p(atletico, "Alexander",   "Sørloth",     1995, 12,  5, "Attaccante",     194);
        p(atletico, "Samuel",      "Lino",        2000,  4,  1, "Attaccante",     178);
        p(atletico, "Ángel",       "Correa",      1995,  3,  9, "Attaccante",     174);
        p(atletico, "Giuliano",    "Simeone",     2003,  1, 25, "Attaccante",     186);
        p(atletico, "Samu",        "Omorodion",   2004,  7, 23, "Attaccante",     188);

        Squadra athletic = club("Athletic Club", 1901, "Bilbao");
        p(athletic, "Unai",    "Simón",              1997,  6, 11, "Portiere",       190);
        p(athletic, "Julen",   "Agirrezabala",       2002,  9, 12, "Portiere",       188);
        p(athletic, "Dani",    "Vivian",             1999,  7,  5, "Difensore",      188);
        p(athletic, "Yeray",   "Álvarez",            1995,  1, 24, "Difensore",      185);
        p(athletic, "Aitor",   "Paredes",            1999,  2, 19, "Difensore",      182);
        p(athletic, "Oscar",   "de Marcos",          1989,  4, 14, "Difensore",      182);
        p(athletic, "Andoni",  "Gorosabel",          1997,  3, 29, "Difensore",      180);
        p(athletic, "Mikel",   "Balenziaga",         1988,  2, 29, "Difensore",      178);
        p(athletic, "Ander",   "Herrera",            1989,  8, 14, "Centrocampista", 181);
        p(athletic, "Mikel",   "Vesga",              1993,  5,  8, "Centrocampista", 183);
        p(athletic, "Oihan",   "Sancet",             2002,  3, 25, "Centrocampista", 184);
        p(athletic, "Beñat",   "Prados",             1999,  2, 16, "Centrocampista", 180);
        p(athletic, "Mikel",   "Jauregizar",         2003,  9,  8, "Centrocampista", 176);
        p(athletic, "Íñigo",   "Ruiz de Galarreta",  1998,  1, 17, "Centrocampista", 183);
        p(athletic, "Nico",    "Williams",           2002,  7, 12, "Attaccante",     180);
        p(athletic, "Iñaki",   "Williams",           1994,  6, 15, "Attaccante",     183);
        p(athletic, "Gorka",   "Guruzeta",           1996, 11, 12, "Attaccante",     191);
        p(athletic, "Asier",   "Villalibre",         1997,  9,  4, "Attaccante",     188);
        p(athletic, "Jon",     "Morcillo",           2002,  3, 29, "Attaccante",     172);
        p(athletic, "Unai",    "Gómez",              1997, 11,  9, "Attaccante",     175);
        p(athletic, "Álvaro",  "Djaló",              2003,  1,  6, "Attaccante",     183);

        Squadra villarreal = club("Villarreal CF", 1923, "Villarreal");
        p(villarreal, "Diego",     "Conde",      1998, 12,  5, "Portiere",       187);
        p(villarreal, "Paulo",     "Gazzaniga",  1992,  1,  2, "Portiere",       193);
        p(villarreal, "Juan",      "Foyth",      1998,  7, 20, "Difensore",      182);
        p(villarreal, "Kiko",      "Femenía",    1991,  2,  2, "Difensore",      175);
        p(villarreal, "Raúl",      "Albiol",     1985,  9,  4, "Difensore",      187);
        p(villarreal, "Eric",      "Bailly",     1994,  4, 12, "Difensore",      186);
        p(villarreal, "Alfonso",   "Pedraza",    1996,  4,  9, "Difensore",      178);
        p(villarreal, "Johan",     "Mojica",     1992,  8, 21, "Difensore",      178);
        p(villarreal, "Sergi",     "Cardona",    2001,  4, 28, "Difensore",      181);
        p(villarreal, "Dani",      "Parejo",     1989,  4, 16, "Centrocampista", 179);
        p(villarreal, "Étienne",   "Capoue",     1988,  7, 11, "Centrocampista", 185);
        p(villarreal, "Alex",      "Baena",      2001,  6, 17, "Centrocampista", 177);
        p(villarreal, "Álex",      "Caparrós",   2001, 12,  5, "Centrocampista", 175);
        p(villarreal, "Santi",     "Comesaña",   1997,  8, 28, "Centrocampista", 183);
        p(villarreal, "Manu",      "Morlanes",   1997,  1, 25, "Centrocampista", 175);
        p(villarreal, "Yeremy",    "Pino",       2002, 10, 20, "Attaccante",     172);
        p(villarreal, "Gérard",    "Moreno",     1992,  4,  7, "Attaccante",     179);
        p(villarreal, "Ayoze",     "Pérez",      1993,  7, 29, "Attaccante",     179);
        p(villarreal, "Thierno",   "Barry",      2001,  7,  8, "Attaccante",     188);
        p(villarreal, "Ilias",     "Akhomach",   2004,  5,  7, "Attaccante",     172);
        p(villarreal, "José Luis", "Morales",    1987, 10, 23, "Attaccante",     172);

        Squadra betis = club("Real Betis", 1907, "Seville");
        p(betis, "Rui",      "Silva",       1994,  2,  7, "Portiere",       191);
        p(betis, "Fran",     "Vieites",     2000, 10, 18, "Portiere",       190);
        p(betis, "Héctor",   "Bellerín",    1995,  3, 19, "Difensore",      178);
        p(betis, "Germán",   "Pezzella",    1991,  6, 27, "Difensore",      187);
        p(betis, "Natan",    "de Souza",    2001,  9,  3, "Difensore",      183);
        p(betis, "Ricardo",  "Rodríguez",   1992,  8, 25, "Difensore",      179);
        p(betis, "Aitor",    "Ruibal",      1997,  5, 29, "Difensore",      178);
        p(betis, "Pablo",    "Navarro",     2003, 10, 17, "Difensore",      186);
        p(betis, "Isco",     "Alarcón",     1992,  4, 21, "Centrocampista", 176);
        p(betis, "Marc",     "Roca",        1996, 11, 26, "Centrocampista", 183);
        p(betis, "Johnny",   "Cardoso",     2001,  6, 23, "Centrocampista", 185);
        p(betis, "Giovani",  "Lo Celso",    1996,  4,  9, "Centrocampista", 177);
        p(betis, "William",  "Carvalho",    1992,  4,  7, "Centrocampista", 188);
        p(betis, "Pablo",    "Fornals",     1996,  5, 22, "Centrocampista", 176);
        p(betis, "Sergi",    "Altimira",    2000,  1, 15, "Centrocampista", 179);
        p(betis, "Borja",    "Iglesias",    1993,  1, 17, "Attaccante",     186);
        p(betis, "Ez",       "Abde",        2001, 12, 17, "Attaccante",     172);
        p(betis, "Chimy",    "Ávila",       1994,  7,  1, "Attaccante",     174);
        p(betis, "Nabil",    "Fekir",       1993,  7, 18, "Attaccante",     174);
        p(betis, "Antony",   "Matheus",     2000,  6, 24, "Attaccante",     172);
        p(betis, "Willian",  "José",        1991, 11, 23, "Attaccante",     189);

        laLiga.getSquadre().addAll(List.of(barca, realMadrid, atletico, athletic, villarreal, betis));
        torneoRepository.save(laLiga);

        // La Liga fixtures
        fixture(laLiga, barca,     realMadrid, cerro,
                LocalDateTime.of(2024, 10, 26, 16, 15), "Estadi Olímpic Lluís Companys, Barcelona",
                4, 0, StatoPartita.PLAYED);
        fixture(laLiga, atletico,  athletic,   cerro,
                LocalDateTime.of(2024, 11, 10, 21,  0), "Cívitas Metropolitano, Madrid",
                2, 1, StatoPartita.PLAYED);
        fixture(laLiga, villarreal, betis,      cerro,
                LocalDateTime.of(2025,  8, 17, 19,  0), "Estadio de la Cerámica, Villarreal",
                null, null, StatoPartita.SCHEDULED);

        // ═════════════════════════════════════════════════════════════════════
        // PREMIER LEAGUE 2024-25  (Final standings: Liverpool, Arsenal, Chelsea,
        //                          Nottingham Forest, Manchester City, Newcastle)
        // ═════════════════════════════════════════════════════════════════════
        Torneo premierLeague = torneo("Premier League 2024-25", 2025,
                "English top division — 2024-25 season");

        Squadra liverpool = club("Liverpool FC", 1892, "Liverpool");
        p(liverpool, "Alisson",   "Becker",           1992, 10,  2, "Portiere",       191);
        p(liverpool, "Caoimhín",  "Kelleher",         1998, 11, 23, "Portiere",       188);
        p(liverpool, "Trent",     "Alexander-Arnold", 1998, 10,  7, "Difensore",      175);
        p(liverpool, "Virgil",    "van Dijk",         1991,  7,  8, "Difensore",      193);
        p(liverpool, "Ibrahima",  "Konaté",           1999,  5, 25, "Difensore",      194);
        p(liverpool, "Andrew",    "Robertson",        1994,  3, 11, "Difensore",      178);
        p(liverpool, "Joe",       "Gomez",            1997,  5, 23, "Difensore",      185);
        p(liverpool, "Conor",     "Bradley",          2003,  7,  9, "Difensore",      177);
        p(liverpool, "Kostas",    "Tsimikas",         1996,  5, 12, "Difensore",      177);
        p(liverpool, "Alexis",    "Mac Allister",     1998, 12, 24, "Centrocampista", 177);
        p(liverpool, "Dominik",   "Szoboszlai",       2000, 10, 25, "Centrocampista", 186);
        p(liverpool, "Ryan",      "Gravenberch",      2002,  5, 16, "Centrocampista", 187);
        p(liverpool, "Harvey",    "Elliott",          2003,  4,  4, "Centrocampista", 170);
        p(liverpool, "Curtis",    "Jones",            2001,  1, 30, "Centrocampista", 182);
        p(liverpool, "Wataru",    "Endo",             1993,  2,  9, "Centrocampista", 178);
        p(liverpool, "Mohamed",   "Salah",            1992,  6, 15, "Attaccante",     175);
        p(liverpool, "Luis",      "Díaz",             1997,  1, 13, "Attaccante",     178);
        p(liverpool, "Darwin",    "Núñez",            1999,  6, 24, "Attaccante",     187);
        p(liverpool, "Diogo",     "Jota",             1996, 12,  4, "Attaccante",     178);
        p(liverpool, "Cody",      "Gakpo",            2000,  5,  7, "Attaccante",     189);
        p(liverpool, "Federico",  "Chiesa",           1997, 10, 25, "Attaccante",     175);

        Squadra arsenal = club("Arsenal FC", 1886, "London");
        p(arsenal, "David",       "Raya",          1995,  9, 15, "Portiere",       183);
        p(arsenal, "Karl",        "Hein",           2002,  4, 13, "Portiere",       196);
        p(arsenal, "Ben",         "White",          1997, 10,  8, "Difensore",      184);
        p(arsenal, "William",     "Saliba",         2001,  3, 24, "Difensore",      192);
        p(arsenal, "Gabriel",     "Magalhães",      1997, 12, 19, "Difensore",      190);
        p(arsenal, "Oleksandr",   "Zinchenko",      1996, 12, 15, "Difensore",      175);
        p(arsenal, "Jurrien",     "Timber",         2001,  6, 17, "Difensore",      181);
        p(arsenal, "Takehiro",    "Tomiyasu",       1998, 11,  5, "Difensore",      187);
        p(arsenal, "Kieran",      "Tierney",        1997,  6,  5, "Difensore",      175);
        p(arsenal, "Martin",      "Ødegaard",       1998, 12, 17, "Centrocampista", 178);
        p(arsenal, "Declan",      "Rice",           1999,  1, 14, "Centrocampista", 185);
        p(arsenal, "Thomas",      "Partey",         1993,  6, 13, "Centrocampista", 185);
        p(arsenal, "Mikel",       "Merino",         1996,  6, 22, "Centrocampista", 189);
        p(arsenal, "Leandro",     "Trossard",       1994, 12,  4, "Centrocampista", 173);
        p(arsenal, "Ethan",       "Nwaneri",        2007,  3, 21, "Centrocampista", 177);
        p(arsenal, "Bukayo",      "Saka",           2001,  9,  5, "Attaccante",     178);
        p(arsenal, "Gabriel",     "Martinelli",     2001,  6, 18, "Attaccante",     175);
        p(arsenal, "Kai",         "Havertz",        1999,  6, 11, "Attaccante",     189);
        p(arsenal, "Gabriel",     "Jesus",          1997,  4,  3, "Attaccante",     175);
        p(arsenal, "Raheem",      "Sterling",       1994, 12,  8, "Attaccante",     170);
        p(arsenal, "Reiss",       "Nelson",         1999, 12, 10, "Attaccante",     174);

        Squadra chelsea = club("Chelsea FC", 1905, "London");
        p(chelsea, "Robert",    "Sánchez",       1997, 11, 18, "Portiere",       197);
        p(chelsea, "Filip",     "Jörgensen",     2002,  1, 30, "Portiere",       192);
        p(chelsea, "Reece",     "James",         1999, 12,  8, "Difensore",      180);
        p(chelsea, "Levi",      "Colwill",       2003,  2, 26, "Difensore",      188);
        p(chelsea, "Wesley",    "Fofana",        2000, 12, 17, "Difensore",      184);
        p(chelsea, "Marc",      "Cucurella",     1998,  7, 22, "Difensore",      172);
        p(chelsea, "Malo",      "Gusto",         2003,  5, 19, "Difensore",      178);
        p(chelsea, "Axel",      "Disasi",        2000,  8,  1, "Difensore",      188);
        p(chelsea, "Benoît",    "Badiashile",    2001,  3, 26, "Difensore",      194);
        p(chelsea, "Ben",       "Chilwell",      1996, 12, 21, "Difensore",      178);
        p(chelsea, "Enzo",      "Fernández",     2001,  1, 17, "Centrocampista", 180);
        p(chelsea, "Moisés",    "Caicedo",       2001, 11,  2, "Centrocampista", 178);
        p(chelsea, "Romeo",     "Lavia",         2004,  1,  6, "Centrocampista", 183);
        p(chelsea, "Cole",      "Palmer",        2002,  5,  6, "Centrocampista", 185);
        p(chelsea, "Nicolás",   "Jackson",       2001,  6, 20, "Attaccante",     181);
        p(chelsea, "Pedro",     "Neto",          2000,  3,  9, "Attaccante",     171);
        p(chelsea, "Mykhailo",  "Mudryk",        2001,  1,  5, "Attaccante",     175);
        p(chelsea, "João",      "Félix",         2000, 11, 10, "Attaccante",     181);
        p(chelsea, "Christopher","Nkunku",       1997, 11, 14, "Attaccante",     175);
        p(chelsea, "Jadon",     "Sancho",        2000,  3, 25, "Attaccante",     178);
        p(chelsea, "Noni",      "Madueke",       2002,  3, 10, "Attaccante",     179);

        Squadra forest = club("Nottingham Forest FC", 1865, "Nottingham");
        p(forest, "Matz",      "Sels",            1992,  2, 26, "Portiere",       193);
        p(forest, "Carlos",    "Miguel",          1998,  7, 13, "Portiere",       193);
        p(forest, "Murillo",   "Santos",          2002,  2,  3, "Difensore",      186);
        p(forest, "Nikola",    "Milenković",      1997, 10, 12, "Difensore",      193);
        p(forest, "Neco",      "Williams",        2001,  4, 13, "Difensore",      176);
        p(forest, "Ola",       "Aina",            1996, 10,  8, "Difensore",      180);
        p(forest, "Andrew",    "Omobamidele",     2002,  5, 23, "Difensore",      193);
        p(forest, "Harry",     "Toffolo",         1995,  8, 19, "Difensore",      175);
        p(forest, "Willy",     "Boly",            1991,  2,  3, "Difensore",      194);
        p(forest, "Ryan",      "Yates",           1997, 11, 21, "Centrocampista", 188);
        p(forest, "Elliot",    "Anderson",        2002, 11,  6, "Centrocampista", 180);
        p(forest, "Morgan",    "Gibbs-White",     2000,  1, 27, "Centrocampista", 175);
        p(forest, "Ibrahim",   "Sangaré",         1997, 12,  2, "Centrocampista", 188);
        p(forest, "Nicolás",   "Domínguez",       1998,  6, 28, "Centrocampista", 179);
        p(forest, "Danilo",    "Barbosa",         2001,  2,  7, "Centrocampista", 175);
        p(forest, "Chris",     "Wood",            1991, 12,  7, "Attaccante",     191);
        p(forest, "Anthony",   "Elanga",          2002,  4, 27, "Attaccante",     179);
        p(forest, "Taiwo",     "Awoniyi",         1997,  8, 12, "Attaccante",     182);
        p(forest, "Callum",    "Hudson-Odoi",     2000, 11,  7, "Attaccante",     177);
        p(forest, "Ramón",     "Sosa",            2000,  9, 17, "Attaccante",     170);
        p(forest, "Jota",      "Silva",           1999,  5, 16, "Attaccante",     167);

        Squadra manCity = club("Manchester City FC", 1880, "Manchester");
        p(manCity, "Ederson",   "Moraes",      1993,  8, 17, "Portiere",       188);
        p(manCity, "Stefan",    "Ortega",      1992, 11,  6, "Portiere",       186);
        p(manCity, "Kyle",      "Walker",      1990,  5, 28, "Difensore",      178);
        p(manCity, "Rúben",     "Dias",        1997,  5, 14, "Difensore",      187);
        p(manCity, "Manuel",    "Akanji",      1995,  7, 19, "Difensore",      187);
        p(manCity, "Joško",     "Gvardiol",    2002,  1, 23, "Difensore",      185);
        p(manCity, "John",      "Stones",      1994,  5, 28, "Difensore",      187);
        p(manCity, "Nathan",    "Aké",         1995,  2, 18, "Difensore",      181);
        p(manCity, "Rico",      "Lewis",       2004, 11, 21, "Difensore",      170);
        p(manCity, "Kevin",     "De Bruyne",   1991,  6, 28, "Centrocampista", 181);
        p(manCity, "Bernardo",  "Silva",       1994,  8, 10, "Centrocampista", 173);
        p(manCity, "Rodri",     "Hernández",   1996,  6, 22, "Centrocampista", 191);
        p(manCity, "Mateo",     "Kovačić",     1994,  5,  6, "Centrocampista", 177);
        p(manCity, "İlkay",     "Gündogan",    1990, 10, 24, "Centrocampista", 180);
        p(manCity, "James",     "McAtee",      2002, 10, 18, "Centrocampista", 180);
        p(manCity, "Phil",      "Foden",       2000,  5, 28, "Attaccante",     171);
        p(manCity, "Jack",      "Grealish",    1995,  9, 10, "Attaccante",     180);
        p(manCity, "Jeremy",    "Doku",        2002,  5, 12, "Attaccante",     170);
        p(manCity, "Erling",    "Haaland",     2000,  7, 21, "Attaccante",     194);
        p(manCity, "Savinho",   "Moreira",     2004,  8,  5, "Attaccante",     172);
        p(manCity, "Oscar",     "Bobb",        2003,  7, 12, "Attaccante",     175);

        Squadra newcastle = club("Newcastle United FC", 1892, "Newcastle upon Tyne");
        p(newcastle, "Nick",      "Pope",        1992,  4, 19, "Portiere",       198);
        p(newcastle, "Martin",    "Dúbravka",    1989,  1, 15, "Portiere",       196);
        p(newcastle, "Kieran",    "Trippier",    1990,  9, 19, "Difensore",      173);
        p(newcastle, "Sven",      "Botman",      2000,  1, 12, "Difensore",      193);
        p(newcastle, "Fabian",    "Schär",       1991, 12, 20, "Difensore",      184);
        p(newcastle, "Dan",       "Burn",        1992,  5,  9, "Difensore",      198);
        p(newcastle, "Tino",      "Livramento",  2002, 12, 12, "Difensore",      174);
        p(newcastle, "Lewis",     "Hall",        2004,  9,  8, "Difensore",      182);
        p(newcastle, "Jamaal",    "Lascelles",   1993, 11, 11, "Difensore",      188);
        p(newcastle, "Bruno",     "Guimarães",   1997, 11, 16, "Centrocampista", 180);
        p(newcastle, "Joelinton", "Cássio",      1996,  8, 14, "Centrocampista", 183);
        p(newcastle, "Sean",      "Longstaff",   1997, 10, 30, "Centrocampista", 182);
        p(newcastle, "Sandro",    "Tonali",      2000,  5,  8, "Centrocampista", 177);
        p(newcastle, "Joe",       "Willock",     1999,  8, 20, "Centrocampista", 183);
        p(newcastle, "Miguel",    "Almirón",     1994,  2, 10, "Centrocampista", 175);
        p(newcastle, "Alexander", "Isak",        2000,  9, 21, "Attaccante",     190);
        p(newcastle, "Harvey",    "Barnes",      1997, 12,  9, "Attaccante",     178);
        p(newcastle, "Anthony",   "Gordon",      2001,  2, 24, "Attaccante",     178);
        p(newcastle, "Callum",    "Wilson",      1992,  2, 27, "Attaccante",     185);
        p(newcastle, "Jacob",     "Murphy",      1995,  2, 24, "Attaccante",     181);
        p(newcastle, "Yankuba",   "Minteh",      2004,  5,  8, "Attaccante",     175);

        premierLeague.getSquadre().addAll(List.of(liverpool, arsenal, chelsea, forest, manCity, newcastle));
        torneoRepository.save(premierLeague);

        // Premier League fixtures
        fixture(premierLeague, liverpool, arsenal,   oliver,
                LocalDateTime.of(2025,  2,  9, 16, 30), "Anfield, Liverpool",
                2, 2, StatoPartita.PLAYED);
        fixture(premierLeague, chelsea,   manCity,   oliver,
                LocalDateTime.of(2025,  4, 12, 17, 30), "Stamford Bridge, London",
                1, 0, StatoPartita.PLAYED);
        fixture(premierLeague, forest,    newcastle, oliver,
                LocalDateTime.of(2025,  8, 16, 15,  0), "The City Ground, Nottingham",
                null, null, StatoPartita.SCHEDULED);

        // ═════════════════════════════════════════════════════════════════════
        // SERIE A 2024-25  (Final standings: Napoli, Inter, Atalanta,
        //                   Juventus, AC Milan, Lazio)
        // ═════════════════════════════════════════════════════════════════════
        Torneo serieA = torneo("Serie A 2024-25", 2025,
                "Italian top division — 2024-25 season");

        Squadra napoli = club("SSC Napoli", 1926, "Naples");
        p(napoli, "Alex",       "Meret",        1997,  3, 22, "Portiere",       190);
        p(napoli, "Elia",       "Caprile",      2001,  7, 26, "Portiere",       190);
        p(napoli, "Giovanni",   "Di Lorenzo",   1993,  8,  4, "Difensore",      183);
        p(napoli, "Leo",        "Østigård",     1999, 11, 26, "Difensore",      189);
        p(napoli, "Rafa",       "Marín",        2002,  5, 23, "Difensore",      194);
        p(napoli, "Mathías",    "Olivera",      1997, 10, 31, "Difensore",      178);
        p(napoli, "Pasquale",   "Mazzocchi",    1995,  2,  4, "Difensore",      176);
        p(napoli, "Leonardo",   "Spinazzola",   1993,  3, 25, "Difensore",      180);
        p(napoli, "Juan",       "Jesus",        1991,  6, 10, "Difensore",      184);
        p(napoli, "Stanislav",  "Lobotka",      1994, 11, 25, "Centrocampista", 172);
        p(napoli, "Scott",      "McTominay",    1996, 12,  8, "Centrocampista", 188);
        p(napoli, "André",      "Anguissa",     1995, 11, 16, "Centrocampista", 188);
        p(napoli, "Billy",      "Gilmour",      2001,  6, 11, "Centrocampista", 174);
        p(napoli, "Marco",      "Folorunsho",   1998,  6, 10, "Centrocampista", 183);
        p(napoli, "Giacomo",    "Raspadori",    2000, 12, 18, "Attaccante",     178);
        p(napoli, "Khvicha",    "Kvaratskhelia",2001,  2, 12, "Attaccante",     183);
        p(napoli, "David",      "Neres",        1997,  3, 28, "Attaccante",     170);
        p(napoli, "Romelu",     "Lukaku",       1993,  5, 13, "Attaccante",     191);
        p(napoli, "Matteo",     "Politano",     1993,  8,  3, "Attaccante",     170);
        p(napoli, "Cyril",      "Ngonge",       2000,  5, 26, "Attaccante",     176);
        p(napoli, "Jesper",     "Lindstrøm",    2000,  3, 29, "Attaccante",     181);

        Squadra inter = club("FC Internazionale", 1908, "Milan");
        p(inter, "Yann",        "Sommer",       1988, 12, 17, "Portiere",       183);
        p(inter, "Josep",       "Martínez",     1998,  6, 25, "Portiere",       186);
        p(inter, "Alessandro",  "Bastoni",      1999,  4, 13, "Difensore",      190);
        p(inter, "Francesco",   "Acerbi",       1988,  2, 10, "Difensore",      192);
        p(inter, "Benjamin",    "Pavard",       1996,  3, 28, "Difensore",      183);
        p(inter, "Denzel",      "Dumfries",     1996,  4, 18, "Difensore",      185);
        p(inter, "Federico",    "Dimarco",      1997, 11, 10, "Difensore",      177);
        p(inter, "Carlos",      "Augusto",      1999,  3,  6, "Difensore",      182);
        p(inter, "Stefan",      "de Vrij",      1992,  2,  5, "Difensore",      190);
        p(inter, "Matteo",      "Darmian",      1989, 12,  2, "Difensore",      181);
        p(inter, "Nicolò",      "Barella",      1997,  2,  7, "Centrocampista", 172);
        p(inter, "Hakan",       "Çalhanoğlu",   1994,  2,  8, "Centrocampista", 179);
        p(inter, "Henrikh",     "Mkhitaryan",   1989,  1, 21, "Centrocampista", 177);
        p(inter, "Kristjan",    "Asllani",      2002,  3,  9, "Centrocampista", 177);
        p(inter, "Davide",      "Frattesi",     1999,  9, 22, "Centrocampista", 177);
        p(inter, "Piotr",       "Zieliński",    1994,  5, 20, "Centrocampista", 178);
        p(inter, "Marcus",      "Thuram",       1997,  8,  6, "Attaccante",     188);
        p(inter, "Lautaro",     "Martínez",     1997,  8, 22, "Attaccante",     174);
        p(inter, "Mehdi",       "Taremi",       1992,  7, 18, "Attaccante",     181);
        p(inter, "Valentin",    "Carboni",      2005,  1, 16, "Attaccante",     178);
        p(inter, "Tajon",       "Buchanan",     1999, 12,  8, "Attaccante",     182);

        Squadra atalanta = club("Atalanta BC", 1907, "Bergamo");
        p(atalanta, "Marco",    "Carnesecchi",  2000,  7,  1, "Portiere",       194);
        p(atalanta, "Juan",     "Musso",        1994,  5,  6, "Portiere",       192);
        p(atalanta, "Berat",    "Djimsiti",     1993,  2, 19, "Difensore",      186);
        p(atalanta, "Isak",     "Hien",         2000,  1, 13, "Difensore",      190);
        p(atalanta, "Giorgio",  "Scalvini",     2003, 12, 11, "Difensore",      192);
        p(atalanta, "Raoul",    "Bellanova",    2000,  5, 17, "Difensore",      183);
        p(atalanta, "Matteo",   "Ruggeri",      2002, 10,  4, "Difensore",      180);
        p(atalanta, "Sead",     "Kolašinac",    1993,  6, 20, "Difensore",      183);
        p(atalanta, "Hans",     "Hateboer",     1994,  1,  9, "Difensore",      185);
        p(atalanta, "Ben",      "Godfrey",      1998,  1, 15, "Difensore",      186);
        p(atalanta, "Marten",   "de Roon",      1991,  3, 29, "Centrocampista", 185);
        p(atalanta, "Éderson",  "Lourenço",     1999,  7,  7, "Centrocampista", 185);
        p(atalanta, "Mario",    "Pašalić",      1995,  2,  9, "Centrocampista", 185);
        p(atalanta, "Lazar",    "Samardžić",    2002,  2, 24, "Centrocampista", 183);
        p(atalanta, "Aleksei",  "Miranchuk",    1995, 10, 17, "Centrocampista", 176);
        p(atalanta, "Charles",  "De Ketelaere", 2001,  3, 10, "Attaccante",     190);
        p(atalanta, "Ademola",  "Lookman",      1997, 10, 20, "Attaccante",     172);
        p(atalanta, "Mateo",    "Retegui",      2000,  4, 29, "Attaccante",     181);
        p(atalanta, "Gianluca", "Scamacca",     2000,  7,  1, "Attaccante",     193);
        p(atalanta, "El Bilal", "Touré",        2001, 11,  3, "Attaccante",     182);
        p(atalanta, "Nicolò",   "Zaniolo",      2000,  7,  2, "Attaccante",     185);

        Squadra juve = club("Juventus FC", 1897, "Turin");
        p(juve, "Michele",   "Di Gregorio",  1997,  7, 17, "Portiere",       191);
        p(juve, "Mattia",    "Perin",        1992, 11, 10, "Portiere",       189);
        p(juve, "Gleison",   "Bremer",       1997,  3, 18, "Difensore",      187);
        p(juve, "Andrea",    "Cambiaso",     2000,  2, 20, "Difensore",      179);
        p(juve, "Pierre",    "Kalulu",       2000,  6,  5, "Difensore",      180);
        p(juve, "Nicolás",   "González",     1998,  5,  8, "Difensore",      183);
        p(juve, "Danilo",    "Luiz",         1991,  7, 15, "Difensore",      183);
        p(juve, "Juan",      "Cabal",        2001,  1, 28, "Difensore",      186);
        p(juve, "Weston",    "McKennie",     1998,  8, 28, "Centrocampista", 183);
        p(juve, "Manuel",    "Locatelli",    1998,  1,  8, "Centrocampista", 183);
        p(juve, "Khéphren",  "Thuram",       2001,  3, 26, "Centrocampista", 186);
        p(juve, "Teun",      "Koopmeiners",  1998,  2, 28, "Centrocampista", 180);
        p(juve, "Douglas",   "Luiz",         1998,  5,  9, "Centrocampista", 178);
        p(juve, "Nicolò",    "Fagioli",      2001,  2, 12, "Centrocampista", 181);
        p(juve, "Enzo",      "Barrenechea",  2001,  5, 22, "Centrocampista", 180);
        p(juve, "Dušan",     "Vlahović",     2000,  1, 28, "Attaccante",     190);
        p(juve, "Arkadiusz", "Milik",        1994,  2, 28, "Attaccante",     186);
        p(juve, "Kenan",     "Yıldız",       2005,  5,  4, "Attaccante",     181);
        p(juve, "Francisco", "Conceição",    2002, 12, 12, "Attaccante",     177);
        p(juve, "Timothy",   "Weah",         2000,  2, 22, "Attaccante",     183);
        p(juve, "Samuel",    "Mbangula",     2004,  5, 24, "Attaccante",     178);

        Squadra milan = club("AC Milan", 1899, "Milan");
        p(milan, "Mike",        "Maignan",       1995,  7,  3, "Portiere",       191);
        p(milan, "Marco",       "Sportiello",    1992,  5, 10, "Portiere",       193);
        p(milan, "Theo",        "Hernández",     1997, 10,  6, "Difensore",      182);
        p(milan, "Fikayo",      "Tomori",        1997, 12, 19, "Difensore",      185);
        p(milan, "Malick",      "Thiaw",         2001,  8,  8, "Difensore",      192);
        p(milan, "Strahinja",   "Pavlović",      2001,  5, 24, "Difensore",      192);
        p(milan, "Emerson",     "Royal",         1999,  1, 14, "Difensore",      179);
        p(milan, "Davide",      "Calabria",      1996, 12,  6, "Difensore",      177);
        p(milan, "Matteo",      "Gabbia",        1999, 10, 21, "Difensore",      187);
        p(milan, "Yunus",       "Musah",         2002, 11, 29, "Centrocampista", 178);
        p(milan, "Tijjani",     "Reijnders",     1998,  7, 29, "Centrocampista", 181);
        p(milan, "Ruben",       "Loftus-Cheek",  1996,  1, 23, "Centrocampista", 190);
        p(milan, "Youssouf",    "Fofana",        1999,  1, 10, "Centrocampista", 183);
        p(milan, "Ismaël",      "Bennacer",      1997, 12,  1, "Centrocampista", 175);
        p(milan, "Christian",   "Pulisic",       1998,  9, 18, "Attaccante",     177);
        p(milan, "Samuel",      "Chukwueze",     1999,  5, 22, "Attaccante",     170);
        p(milan, "Rafael",      "Leão",          1999,  6, 10, "Attaccante",     188);
        p(milan, "Álvaro",      "Morata",        1992, 10, 23, "Attaccante",     186);
        p(milan, "Noah",        "Okafor",        2000,  5, 24, "Attaccante",     181);
        p(milan, "Francesco",   "Camarda",       2008,  3, 10, "Attaccante",     182);
        p(milan, "Luka",        "Jović",         1997, 12, 23, "Attaccante",     181);

        Squadra lazio = club("SS Lazio", 1900, "Rome");
        p(lazio, "Ivan",      "Provedel",      1994,  3, 17, "Portiere",       191);
        p(lazio, "Christos",  "Mandas",        2001, 10, 15, "Portiere",       196);
        p(lazio, "Alessio",   "Romagnoli",     1994,  1, 12, "Difensore",      186);
        p(lazio, "Mario",     "Gila",          2001,  8, 29, "Difensore",      188);
        p(lazio, "Adam",      "Marušić",       1992, 10, 17, "Difensore",      185);
        p(lazio, "Patric",    "Gabarron",      1993,  4, 17, "Difensore",      182);
        p(lazio, "Elseid",    "Hysaj",         1994,  2,  2, "Difensore",      178);
        p(lazio, "Luca",      "Pellegrini",    1999,  3,  7, "Difensore",      180);
        p(lazio, "Nuno",      "Tavares",       2000,  1, 26, "Difensore",      178);
        p(lazio, "Samuel",    "Gigot",         1993,  7,  9, "Difensore",      191);
        p(lazio, "Nicolò",    "Rovella",       2001, 12,  4, "Centrocampista", 180);
        p(lazio, "Matías",    "Vecino",        1991,  8, 24, "Centrocampista", 183);
        p(lazio, "Fisayo",    "Dele-Bashiru",  2001,  9, 17, "Centrocampista", 182);
        p(lazio, "Toma",      "Bašić",         1996, 11, 25, "Centrocampista", 183);
        p(lazio, "Gustav",    "Isaksen",       2001,  4, 19, "Attaccante",     181);
        p(lazio, "Mattia",    "Zaccagni",      1995,  6, 16, "Attaccante",     178);
        p(lazio, "Pedro",     "Eliezer",       1987,  7, 28, "Attaccante",     169);
        p(lazio, "Taty",      "Castellanos",   1998, 10,  3, "Attaccante",     179);
        p(lazio, "Boulaye",   "Dia",           1996, 11, 16, "Attaccante",     181);
        p(lazio, "Loum",      "Tchaouna",      2003,  7, 21, "Attaccante",     181);
        p(lazio, "Gaétan",    "Laborde",       1994,  5,  3, "Attaccante",     183);

        serieA.getSquadre().addAll(List.of(napoli, inter, atalanta, juve, milan, lazio));
        torneoRepository.save(serieA);

        // Serie A fixtures
        fixture(serieA, inter,    napoli,   massa,
                LocalDateTime.of(2025,  2,  2, 20, 45), "San Siro, Milan",
                1, 1, StatoPartita.PLAYED);
        fixture(serieA, atalanta, juve,     massa,
                LocalDateTime.of(2025,  3, 16, 18,  0), "Gewiss Stadium, Bergamo",
                2, 0, StatoPartita.PLAYED);
        fixture(serieA, milan,    lazio,    massa,
                LocalDateTime.of(2025,  8, 24, 20, 45), "San Siro, Milan",
                null, null, StatoPartita.SCHEDULED);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Torneo torneo(String nome, int anno, String desc) {
        return torneoRepository.save(new Torneo(nome, anno, desc));
    }

    private Squadra club(String nome, int anno, String citta) {
        return squadraRepository.save(new Squadra(nome, anno, citta));
    }

    private Arbitro arb(String nome, String cognome, String codice) {
        return arbitroRepository.save(new Arbitro(nome, cognome, codice));
    }

    private void p(Squadra s, String nome, String cognome,
                   int y, int m, int d, String ruolo, int h) {
        giocatoreRepository.save(
                new Giocatore(nome, cognome, LocalDate.of(y, m, d), ruolo, h, s));
    }

    private void fixture(Torneo t, Squadra home, Squadra away, Arbitro a,
                         LocalDateTime dt, String luogo,
                         Integer gh, Integer ga, StatoPartita stato) {
        Partita match = new Partita(dt, luogo, t, home, away, a);
        if (stato == StatoPartita.PLAYED) {
            match.setGoalsHome(gh);
            match.setGoalsAway(ga);
            match.setStato(StatoPartita.PLAYED);
        }
        partitaRepository.save(match);
    }
}
