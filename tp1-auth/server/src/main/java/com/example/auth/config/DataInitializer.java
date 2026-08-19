package com.example.auth.config;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // AJOUTÉ

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;  // AJOUTÉ
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("toto@example.com").isEmpty()) {
            // TP2 : Mot de passe hashé avec BCrypt
            String hashedPassword = passwordEncoder.encode("pwd1234");
            User testUser = new User("toto@example.com", hashedPassword);
            userRepository.save(testUser);
            System.out.println(" Compte créé : toto@example.com / pwd1234 (hashé BCrypt)");
        }
    }
}