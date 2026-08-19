package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================
 * POINT D'ENTRÉE DE L'APPLICATION SPRING BOOT
 * ============================================================
 *
 * Cette classe est le point de départ de l'application.
 * L'annotation @SpringBootApplication active :
 *   - @Configuration : pour les beans Spring
 *   - @EnableAutoConfiguration : pour la configuration automatique
 *   - @ComponentScan : pour scanner les packages
 *
 * En TP1, l'application démarre sans sécurité.
 *
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class AuthServerApplication {

    /**
     * Méthode principale qui lance l'application Spring Boot.
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        // SpringApplication.run() démarre le conteneur Spring
        // et lance le serveur embarqué (Tomcat par défaut)
        SpringApplication.run(AuthServerApplication.class, args);

        // Une fois démarré, l'API est accessible sur http://localhost:8080
        // Console H2 : http://localhost:8080/h2-console
    }
}