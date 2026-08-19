package com.example.auth.config;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================
 * INITIALISEUR DES DONNÉES DE TEST - TP1
 * ============================================================
 *
 * Cette classe crée un compte de test au démarrage de l'application.
 *
 * CommandLineRunner : exécuté après le démarrage complet de Spring.
 *
 * Le compte de test est exigé par le sujet TP1 :
 *   - Email : toto@example.com
 *   - Mot de passe : pwd1234
 *
 * ⚠️ TP1 : Le mot de passe est stocké en CLAIR !
 *
 * @see org.springframework.boot.CommandLineRunner
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    /**
     * Constructeur avec injection du repository.
     *
     * @param userRepository le repository des utilisateurs
     */
    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Méthode exécutée après le démarrage de l'application.
     *
     * @param args les arguments de la ligne de commande
     */
    @Override
    public void run(String... args) {
        // Vérifier si le compte de test existe déjà
        // Si non, on le crée
        if (userRepository.findByEmail("toto@example.com").isEmpty()) {

            // ⚠️ TP1 : Mot de passe en CLAIR (VOLONTAIREMENT DANGEREUX)
            // Normalement on hacherait avec BCrypt, mais c'est le but du TP1
            User testUser = new User("toto@example.com", "pwd1234");

            // Sauvegarder en base
            userRepository.save(testUser);

            // Message dans la console pour confirmer la création
            System.out.println("✅ Compte de test créé : toto@example.com / pwd1234");
            System.out.println("⚠️  TP1 : Mot de passe stocké en CLAIR - NE PAS UTILISER EN PRODUCTION");
        }
    }
}