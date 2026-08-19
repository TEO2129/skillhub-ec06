package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.auth.exception.AuthenticationFailedException;
import com.example.auth.exception.InvalidInputException;
import com.example.auth.exception.ResourceConflictException;
import com.example.auth.repository.UserRepository;
import com.example.auth.validator.PasswordPolicyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // TP2 : Anti-brute force
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 2;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String email, String password) {
        // Validation email
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Tentative d'inscription avec email vide");
            throw new InvalidInputException("L'email est obligatoire.");
        }
        if (!isValidEmail(email)) {
            logger.warn("Tentative d'inscription avec email invalide : {}", email);
            throw new InvalidInputException("Format d'email invalide.");
        }

        // TP2 : Politique mot de passe stricte
        PasswordPolicyValidator.validate(password);

        if (userRepository.existsByEmail(email)) {
            logger.warn("Tentative d'inscription avec email déjà existant : {}", email);
            throw new ResourceConflictException("Cet email est déjà utilisé.");
        }

        // TP2 : Hash BCrypt
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, hashedPassword);
        User saved = userRepository.save(user);

        logger.info("Inscription réussie pour : {}", email);
        return saved;
    }

    @Transactional
    public String login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidInputException("L'email est obligatoire.");
        }
        if (password == null || password.isEmpty()) {
            throw new InvalidInputException("Le mot de passe est obligatoire.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Tentative de connexion avec email inconnu : {}", email);
                    return new AuthenticationFailedException("Identifiants invalides.");
                });

        // TP2 : Vérification du verrouillage
        if (user.isLocked()) {
            logger.warn("Tentative de connexion sur compte verrouillé : {}", email);
            throw new AuthenticationFailedException(
                    "Compte temporairement verrouillé. Réessayez dans " + LOCK_DURATION_MINUTES + " minutes."
            );
        }

        // TP2 : Vérification BCrypt
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // TP2 : Incrémenter les tentatives échouées
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                logger.warn("Compte verrouillé après {} échecs pour : {}", MAX_FAILED_ATTEMPTS, email);
            }
            userRepository.save(user);
            logger.warn("Connexion échouée - mot de passe incorrect pour : {}", email);
            throw new AuthenticationFailedException("Identifiants invalides.");
        }

        // TP2 : Réinitialiser les tentatives après succès
        user.setFailedAttempts(0);
        user.setLockUntil(null);
        userRepository.save(user);

        String token = "session-" + System.currentTimeMillis() + "-" + user.getId();
        user.setSessionToken(token);
        userRepository.save(user);

        logger.info("Connexion réussie pour : {}", email);
        return token;
    }

    public User getMe(String token) {
        if (token == null || token.isEmpty()) {
            throw new AuthenticationFailedException("Token manquant.");
        }
        return userRepository.findBySessionToken(token)
                .orElseThrow(() -> new AuthenticationFailedException("Token invalide ou expiré."));
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
}