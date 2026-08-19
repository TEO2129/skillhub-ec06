package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.auth.exception.AuthenticationFailedException;
import com.example.auth.exception.InvalidInputException;
import com.example.auth.exception.ResourceConflictException;
import com.example.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * ============================================================
 * SERVICE PRINCIPAL D'AUTHENTIFICATION - TP1
 * ============================================================
 *
 * ⚠️ ATTENTION : Cette implémentation est VOLONTAIREMENT DANGEREUSE
 * et ne doit JAMAIS être utilisée en production.
 *
 * Risques identifiés en TP1 :
 * 1. ✅ Mots de passe stockés en clair en base
 * 2. ✅ Pas de politique de mot de passe forte (seulement 4 caractères)
 * 3. ✅ Pas de protection contre les attaques par force brute
 * 4. ✅ Token de session simple (non signé, non sécurisé)
 * 5. ✅ Absence de TLS/HTTPS configuré
 *
 * @see org.springframework.stereotype.Service
 */
@Service
public class AuthService {

    // Logger pour tracer les événements (exigé par le sujet TP1)
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;

    /**
     * Constructeur avec injection de dépendance.
     * Spring injecte automatiquement le repository.
     *
     * @param userRepository le repository des utilisateurs
     */
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================================================
    // TP1 : INSCRIPTION (mot de passe en clair)
    // =========================================================

    /**
     * Inscrit un nouvel utilisateur.
     *
     * ⚠️ TP1 : Validation minimale :
     * - Email obligatoire et format valide
     * - Mot de passe : minimum 4 caractères (UNIQUEMENT)
     * - Email unique
     * - Mot de passe stocké en CLAIR
     *
     * @param email    l'email de l'utilisateur
     * @param password le mot de passe (stocké en clair)
     * @return l'utilisateur créé
     * @throws InvalidInputException        si l'email ou le mot de passe est invalide
     * @throws ResourceConflictException    si l'email existe déjà
     */
    public User register(String email, String password) {

        // =========================================================
        // 1. Validation de l'email
        // =========================================================

        // Vérifier que l'email n'est pas vide
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Tentative d'inscription avec email vide");  // Log exigé par le sujet
            throw new InvalidInputException("L'email est obligatoire.");
        }

        // Vérifier le format de l'email (ex: test@example.com)
        if (!isValidEmail(email)) {
            logger.warn("Tentative d'inscription avec email invalide : {}", email);
            throw new InvalidInputException("Format d'email invalide.");
        }

        // =========================================================
        // 2. Validation du mot de passe (TP1 : min 4 caractères)
        // =========================================================

        // ⚠️ TP1 : Seulement 4 caractères minimum (VOLONTAIREMENT FAIBLE)
        if (password == null || password.length() < 4) {
            logger.warn("Tentative d'inscription avec mot de passe trop court pour : {}", email);
            throw new InvalidInputException("Le mot de passe doit contenir au moins 4 caractères.");
        }
        // ❌ TP1 : Pas de vérification de majuscules, chiffres, caractères spéciaux
        // ❌ TP1 : Pas de vérification de longueur > 4

        // =========================================================
        // 3. Vérifier que l'email est unique
        // =========================================================

        if (userRepository.existsByEmail(email)) {
            logger.warn("Tentative d'inscription avec email déjà existant : {}", email);
            throw new ResourceConflictException("Cet email est déjà utilisé.");
        }

        // =========================================================
        // 4. Création de l'utilisateur (stockage en CLAIR)
        // =========================================================

        // ⚠️ TP1 : Mot de passe en CLAIR (VOLONTAIREMENT DANGEREUX)
        User user = new User(email, password);
        User saved = userRepository.save(user);

        // Log d'inscription réussie (exigé par le sujet)
        logger.info("Inscription réussie pour : {}", email);
        return saved;
    }

    // =========================================================
    // TP1 : LOGIN (comparaison en clair)
    // =========================================================

    /**
     * Connecte un utilisateur et retourne un token de session.
     *
     * ⚠️ TP1 :
     * - Comparaison du mot de passe en CLAIR
     * - Pas de BCrypt
     * - Pas de verrouillage après échecs
     * - Token simple (non signé)
     *
     * @param email    l'email de l'utilisateur
     * @param password le mot de passe (comparé en clair)
     * @return un token de session simple
     * @throws InvalidInputException          si l'email ou le mot de passe est vide
     * @throws AuthenticationFailedException  si l'email est inconnu ou le mot de passe incorrect
     */
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
        // 2. Recherche de l'utilisateur par email
        // =========================================================

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Tentative de connexion avec email inconnu : {}", email);
                    // ⚠️ TP2 : Même message pour email inconnu et mauvais mot de passe
                    // (non-divulgation des erreurs)
                    return new AuthenticationFailedException("Identifiants invalides.");
                });

        // =========================================================
        // 3. Vérification du mot de passe (COMPARAISON EN CLAIR)
        // =========================================================

        // ⚠️ TP1 : Comparaison en CLAIR (VOLONTAIREMENT DANGEREUX)
        // Normalement on utilise BCrypt.matches() pour comparer les hash
        if (!user.getPassword().equals(password)) {
            logger.warn("Connexion échouée - mot de passe incorrect pour : {}", email);
            // ⚠️ TP1 : Pas de compteur de tentatives
            // ⚠️ TP1 : Pas de verrouillage après 5 échecs
            throw new AuthenticationFailedException("Identifiants invalides.");
        }

        // =========================================================
        // 4. Génération du token de session (TP1 : SIMPLE)
        // =========================================================

        // ⚠️ TP1 : Token SIMPLE (non signé, non sécurisé)
        // Format : session-[timestamp]-[userId]
        String token = "session-" + System.currentTimeMillis() + "-" + user.getId();

        // Stocker le token en base (pour la route /api/me)
        user.setSessionToken(token);
        userRepository.save(user);

        // Log de connexion réussie
        logger.info("Connexion réussie pour : {}", email);
        return token;
    }

    // =========================================================
    // TP1 : ROUTE PROTÉGÉE /api/me (token simple)
    // =========================================================

    /**
     * Récupère les informations de l'utilisateur authentifié.
     *
     * ⚠️ TP1 :
     * - Vérification par token stocké en base
     * - Pas de JWT, pas de signature
     * - Token non sécurisé
     *
     * @param token le token de session (header X-Session-Token)
     * @return l'utilisateur correspondant
     * @throws AuthenticationFailedException si le token est manquant, invalide ou expiré
     */
    public User getMe(String token) {

        // Vérifier que le token est présent
        if (token == null || token.isEmpty()) {
            throw new AuthenticationFailedException("Token manquant.");
        }

        // ⚠️ TP1 : Recherche par token stocké en base (non sécurisé)
        // Normalement on valide la signature du JWT
        return userRepository.findBySessionToken(token)
                .orElseThrow(() -> new AuthenticationFailedException("Token invalide ou expiré."));
    }

    // =========================================================
    // MÉTHODES UTILITAIRES
    // =========================================================

    /**
     * Valide le format d'un email.
     *
     * @param email l'email à valider
     * @return true si le format est valide, false sinon
     */
    private boolean isValidEmail(String email) {
        // Regex simple pour valider le format email
        // Exemple : test@example.com
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    public String loginHmac(LoginHmacRequest request) {
        String email = request.getEmail();
        String nonce = request.getNonce();
        long timestamp = request.getTimestamp();
        String hmac = request.getHmac();

        // 1. Vérifier email existe
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailedException("Identifiants invalides."));

        // 2. Vérifier timestamp (±60 secondes)
        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > 60000) {
            throw new AuthenticationFailedException("Timestamp invalide.");
        }

        // 3. Vérifier anti-rejeu
        if (nonceRepository.findByNonce(nonce).isPresent()) {
            throw new AuthenticationFailedException("Nonce déjà utilisé.");
        }

        // 4. Récupérer mot de passe (chiffré réversible)
        String passwordPlain = decryptPassword(user.getPassword());

        // 5. Recalculer HMAC
        String message = email + ":" + nonce + ":" + timestamp;
        String expectedHmac = HmacService.calculateHmac(message, passwordPlain);

        // 6. Comparer en temps constant
        if (!HmacService.constantTimeEquals(hmac, expectedHmac)) {
            throw new AuthenticationFailedException("Signature invalide.");
        }

        // 7. Marquer nonce comme consommé
        Nonce nonceEntity = new Nonce(user.getId(), nonce, LocalDateTime.now().plusMinutes(2));
        nonceRepository.save(nonceEntity);

        // 8. Générer JWT
        String token = jwtUtil.generateToken(email);
        logger.info("Connexion HMAC réussie pour : {}", email);
        return token;
    }
}