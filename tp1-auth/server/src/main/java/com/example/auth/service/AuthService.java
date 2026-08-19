package com.example.auth.service;

import com.example.auth.crypto.AesGcmUtil;
import com.example.auth.dto.LoginHmacRequest;
import com.example.auth.entity.Nonce;
import com.example.auth.entity.User;
import com.example.auth.exception.AuthenticationFailedException;
import com.example.auth.exception.InvalidInputException;
import com.example.auth.exception.ResourceConflictException;
import com.example.auth.repository.NonceRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtUtil;
import com.example.auth.validator.PasswordPolicyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * ============================================================
 * SERVICE PRINCIPAL D'AUTHENTIFICATION - TP4
 * ============================================================
 *
 * TP1 : Authentification dangereuse (mots de passe en clair)
 * TP2 : BCrypt + politique stricte + anti-brute force
 * TP3 : HMAC + nonce + timestamp + JWT
 * TP4 : Master Key AES GCM + chiffrement des mots de passe
 *
 * @see org.springframework.stereotype.Service
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // TP2 : Anti-brute force
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 2;

    private final UserRepository userRepository;
    private final NonceRepository nonceRepository;
    private final PasswordEncoder passwordEncoder;
    private final MasterKeyConfig masterKeyConfig;
    private final JwtUtil jwtUtil;

    /**
     * Constructeur TP4 avec toutes les dépendances.
     */
    public AuthService(UserRepository userRepository,
                       NonceRepository nonceRepository,
                       PasswordEncoder passwordEncoder,
                       MasterKeyConfig masterKeyConfig,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.nonceRepository = nonceRepository;
        this.passwordEncoder = passwordEncoder;
        this.masterKeyConfig = masterKeyConfig;
        this.jwtUtil = jwtUtil;
    }

    // =========================================================
    // TP4 : INSCRIPTION (BCrypt + Master Key AES GCM)
    // =========================================================

    @Transactional
    public User register(String email, String password) {

        // =========================================================
        // 1. Validation de l'email
        // =========================================================

        if (email == null || email.trim().isEmpty()) {
            logger.warn("Tentative d'inscription avec email vide");
            throw new InvalidInputException("L'email est obligatoire.");
        }

        if (!isValidEmail(email)) {
            logger.warn("Tentative d'inscription avec email invalide : {}", email);
            throw new InvalidInputException("Format d'email invalide.");
        }

        // =========================================================
        // 2. Validation du mot de passe (politique stricte TP2)
        // =========================================================

        PasswordPolicyValidator.validate(password);

        // =========================================================
        // 3. Vérifier que l'email est unique
        // =========================================================

        if (userRepository.existsByEmail(email)) {
            logger.warn("Tentative d'inscription avec email déjà existant : {}", email);
            throw new ResourceConflictException("Cet email est déjà utilisé.");
        }

        // =========================================================
        // 4. TP4 : Chiffrer le mot de passe avec Master Key AES GCM
        // =========================================================

        String encryptedPassword = encryptPassword(password);

        // =========================================================
        // 5. Hash BCrypt pour compatibilité TP2/TP3
        // =========================================================

        String hashedPassword = passwordEncoder.encode(password);

        // =========================================================
        // 6. Création de l'utilisateur
        // =========================================================

        User user = new User(email, hashedPassword);
        user.setPasswordEncrypted(encryptedPassword);
        user.setFailedAttempts(0);
        user.setLockUntil(null);

        User saved = userRepository.save(user);
        logger.info("Inscription réussie pour : {} (chiffré avec Master Key)", email);
        return saved;
    }

    // =========================================================
    // TP2 : LOGIN (BCrypt + anti-brute force)
    // =========================================================

    @Transactional
    public String login(String email, String password) {

        // =========================================================
        // 1. Validation des champs obligatoires
        // =========================================================

        if (email == null || email.trim().isEmpty()) {
            throw new InvalidInputException("L'email est obligatoire.");
        }
        if (password == null || password.isEmpty()) {
            throw new InvalidInputException("Le mot de passe est obligatoire.");
        }

        // =========================================================
        // 2. Recherche de l'utilisateur
        // =========================================================

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Tentative de connexion avec email inconnu : {}", email);
                    return new AuthenticationFailedException("Identifiants invalides.");
                });

        // =========================================================
        // 3. Vérification du verrouillage (anti-brute force)
        // =========================================================

        if (user.isLocked()) {
            logger.warn("Tentative de connexion sur compte verrouillé : {}", email);
            throw new AuthenticationFailedException(
                    "Compte temporairement verrouillé. Réessayez dans " + LOCK_DURATION_MINUTES + " minutes."
            );
        }

        // =========================================================
        // 4. Vérification du mot de passe avec BCrypt
        // =========================================================

        if (!passwordEncoder.matches(password, user.getPassword())) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                logger.warn("Compte verrouillé après {} échecs pour : {}", MAX_FAILED_ATTEMPTS, email);
            }
            userRepository.save(user);
            logger.warn("Connexion échouée - mot de passe incorrect pour : {}", email);
            throw new AuthenticationFailedException("Identifiants invalides.");
        }

        // =========================================================
        // 5. Connexion réussie - Réinitialiser les tentatives
        // =========================================================

        user.setFailedAttempts(0);
        user.setLockUntil(null);
        userRepository.save(user);

        // =========================================================
        // 6. Génération du token (TP3 : JWT)
        // =========================================================

        String token = jwtUtil.generateToken(email);
        user.setSessionToken(token);
        userRepository.save(user);

        logger.info("Connexion réussie pour : {}", email);
        return token;
    }

    // =========================================================
    // TP3 : LOGIN HMAC (Master Key TP4)
    // =========================================================

    @Transactional
    public String loginHmac(LoginHmacRequest request) {

        String email = request.getEmail();
        String nonce = request.getNonce();
        long timestamp = request.getTimestamp();
        String hmac = request.getHmac();

        // =========================================================
        // 1. Vérifier email existe
        // =========================================================

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Tentative de connexion HMAC avec email inconnu : {}", email);
                    return new AuthenticationFailedException("Identifiants invalides.");
                });

        // =========================================================
        // 2. Vérifier le verrouillage (anti-brute force)
        // =========================================================

        if (user.isLocked()) {
            logger.warn("Tentative de connexion HMAC sur compte verrouillé : {}", email);
            throw new AuthenticationFailedException(
                    "Compte temporairement verrouillé. Réessayez dans " + LOCK_DURATION_MINUTES + " minutes."
            );
        }

        // =========================================================
        // 3. Vérifier timestamp (±60 secondes)
        // =========================================================

        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > 60000) {
            logger.warn("Timestamp invalide pour : {}, écart : {} ms", email, Math.abs(now - timestamp));
            throw new AuthenticationFailedException("Timestamp invalide.");
        }

        // =========================================================
        // 4. Vérifier anti-rejeu (nonce pas déjà utilisé)
        // =========================================================

        if (nonceRepository.findByNonce(nonce).isPresent()) {
            logger.warn("Nonce déjà utilisé pour : {}", email);
            throw new AuthenticationFailedException("Nonce déjà utilisé.");
        }

        // =========================================================
        // 5. TP4 : Récupérer et déchiffrer le mot de passe
        // =========================================================

        String passwordPlain = decryptPassword(user.getPasswordEncrypted());

        // =========================================================
        // 6. Recalculer HMAC
        // =========================================================

        String message = email + ":" + nonce + ":" + timestamp;
        String expectedHmac = HmacService.calculateHmac(message, passwordPlain);

        // =========================================================
        // 7. Comparer en temps constant
        // =========================================================

        if (!HmacService.constantTimeEquals(hmac, expectedHmac)) {
            logger.warn("HMAC invalide pour : {}", email);
            // Incrémenter les tentatives échouées
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                logger.warn("Compte verrouillé après {} échecs HMAC pour : {}", MAX_FAILED_ATTEMPTS, email);
            }
            userRepository.save(user);
            throw new AuthenticationFailedException("Signature invalide.");
        }

        // =========================================================
        // 8. Réinitialiser les tentatives
        // =========================================================

        user.setFailedAttempts(0);
        user.setLockUntil(null);
        userRepository.save(user);

        // =========================================================
        // 9. Marquer nonce comme consommé
        // =========================================================

        Nonce nonceEntity = new Nonce(user.getId(), nonce, LocalDateTime.now().plusMinutes(2));
        nonceRepository.save(nonceEntity);

        // =========================================================
        // 10. Générer JWT
        // =========================================================

        String token = jwtUtil.generateToken(email);
        logger.info("Connexion HMAC réussie pour : {}", email);
        return token;
    }

    // =========================================================
    // ROUTE PROTÉGÉE /api/me
    // =========================================================

    public User getMe(String token) {

        if (token == null || token.isEmpty()) {
            throw new AuthenticationFailedException("Token manquant.");
        }

        // TP3 : Validation JWT
        if (!jwtUtil.validateToken(token)) {
            throw new AuthenticationFailedException("Token invalide ou expiré.");
        }

        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailedException("Utilisateur non trouvé."));
    }

    // =========================================================
    // TP4 : MÉTHODES DE CHIFFREMENT AVEC MASTER KEY
    // =========================================================

    /**
     * Chiffre un mot de passe avec AES GCM.
     *
     * @param plainPassword le mot de passe en clair
     * @return le mot de passe chiffré (format: "v1:Base64(iv):Base64(ciphertext)")
     */
    private String encryptPassword(String plainPassword) {
        return AesGcmUtil.encrypt(plainPassword, masterKeyConfig.getMasterKey());
    }

    /**
     * Déchiffre un mot de passe avec AES GCM.
     *
     * @param encryptedPassword le mot de passe chiffré
     * @return le mot de passe en clair
     */
    private String decryptPassword(String encryptedPassword) {
        return AesGcmUtil.decrypt(encryptedPassword, masterKeyConfig.getMasterKey());
    }

    // =========================================================
    // MÉTHODES UTILITAIRES
    // =========================================================

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
}