package com.football.tournaments; // Carpeta raiz del proyecto

import org.springframework.boot.SpringApplication; //Importamos la clase SpringApplication para ejecutar la aplicación
import org.springframework.boot.autoconfigure.SpringBootApplication; //Importamos la anotación SpringBootApplication para que cada vez que aparezca el @ haga automaticamente:
                                                                     /*
                                                                    "1. Escanea todo el proyecto buscando @Controller, @Service, @Repository..."
                                                                    "2. Configura automáticamente la base de datos, el servidor, la seguridad..."
                                                                    "3. Activa el punto de arranque" 
                                                                    */
@SpringBootApplication //Señalamos el punto de arraque de la aplicación y le damos permiso
public class FootballTournamentsApplication {   //Es la clase principal de nuestro proyecto
    public static void main(String[] args) {    //Programa principal
        SpringApplication.run(FootballTournamentsApplication.class, args);  //Arranca el spring boot
    }
}
