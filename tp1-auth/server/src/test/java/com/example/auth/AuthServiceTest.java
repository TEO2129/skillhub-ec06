package com.example.auth;

import com.example.auth.entity.User;
import com.example.auth.exception.AuthenticationFailedException;
import com.example.auth.exception.InvalidInputException;
import com.example.auth.exception.ResourceConflictException;
import com.example.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================
 * TESTS UNITAIRES DU SERVICE D'AUTHENTIFICATION - TP4
 * ============================================================
 *
 * TP1 : 10 tests de base (inscription, connexion, /api/me)
 * TP2 : Ajout des tests de politique mot de passe, lockout
 * TP3 : Ajout des tests HMAC, nonce, timestamp (non inclus ici)
 * TP4 : Ajout des tests Master Key (AesGcmUtilTest)
 *
 * Le service teste actuellement la version TP4 avec :
 * - BCrypt pour le hash des mots de passe
 * - Politique de mot de passe stricte (12 caractères + maj/min/chiffre/spécial)
 * - Anti-brute force (5 échecs → 2 minutes de blocage)
 * - Master Key AES GCM pour le chiffrement
 * - JWT pour les tokens
 *
 * @see com.example.auth.service.AuthService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    // =========================================================
    // TESTS TP1 (10 tests de base - adaptés TP4)
    // =========================================================

    /**
     * Test 1 : Inscription avec email vide
     * Doit lever InvalidInputException (400 BAD REQUEST)
     */
    @Test
    void testRegister_EmailVide_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("", "Abcdef123!@#"));
    }

    /**
     * Test 2 : Inscription avec email format incorrect
     * Doit lever InvalidInputException (400 BAD REQUEST)
     */
    @Test
    void testRegister_EmailFormatIncorrect_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("pasunemail", "Abcdef123!@#"));
    }

    /**
     * Test 3 : Inscription avec mot de passe trop court (< 12 caractères)
     * Doit lever InvalidInputException (400 BAD REQUEST)
     */
    @Test
    void testRegister_PasswordTropCourt_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("user@test.com", "abc"));
    }

    /**
     * Test 4 : Inscription réussie avec mot de passe valide TP4
     * Vérifie que l'utilisateur est bien créé avec les bonnes informations
     */
    @Test
    void testRegister_OK() {
        User user = authService.register("newuser@test.com", "Abcdef123!@#");
        assertNotNull(user);
        assertEquals("newuser@test.com", user.getEmail());
        assertNotNull(user.getCreatedAt());
    }

    /**
     * Test 5 : Inscription refusée si email déjà existant
     * Doit lever ResourceConflictException (409 CONFLICT)
     */
    @Test
    void testRegister_EmailDejaExistant_ShouldThrowResourceConflict() {
        authService.register("duplicate@test.com", "Abcdef123!@#");
        assertThrows(ResourceConflictException.class,
                () -> authService.register("duplicate@test.com", "Abcdef123!@#"));
    }

    /**
     * Test 6 : Connexion réussie
     * Vérifie qu'un token est retourné après connexion
     */
    @Test
    void testLogin_OK() {
        authService.register("login@test.com", "Abcdef123!@#");
        String token = authService.login("login@test.com", "Abcdef123!@#");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    /**
     * Test 7 : Connexion avec mot de passe incorrect
     * Doit lever AuthenticationFailedException (401 UNAUTHORIZED)
     */
    @Test
    void testLogin_MauvaisMotDePasse_ShouldThrowAuthFailed() {
        authService.register("wrongpwd@test.com", "Abcdef123!@#");
        assertThrows(AuthenticationFailedException.class,
                () -> authService.login("wrongpwd@test.com", "mauvais"));
    }

    /**
     * Test 8 : Connexion avec email inconnu
     * Doit lever AuthenticationFailedException (401 UNAUTHORIZED)
     */
    @Test
    void testLogin_EmailInconnu_ShouldThrowAuthFailed() {
        assertThrows(AuthenticationFailedException.class,
                () -> authService.login("inconnu@test.com", "Abcdef123!@#"));
    }

    /**
     * Test 9 : Accès à /api/me sans token
     * Doit lever AuthenticationFailedException (401 UNAUTHORIZED)
     */
    @Test
    void testGetMe_SansToken_ShouldThrowAuthFailed() {
        assertThrows(AuthenticationFailedException.class,
                () -> authService.getMe(null));
    }

    /**
     * Test 10 : Accès à /api/me après connexion réussie
     * Vérifie que l'utilisateur peut récupérer son profil
     */
    @Test
    void testGetMe_OK_ApresLogin() {
        authService.register("me@test.com", "Abcdef123!@#");
        String token = authService.login("me@test.com", "Abcdef123!@#");
        User user = authService.getMe(token);
        assertEquals("me@test.com", user.getEmail());
    }

    // =========================================================
    // TESTS TP2 - Politique de mot de passe stricte
    // =========================================================

    /**
     * Test 11 : Mot de passe sans majuscule
     * Doit lever InvalidInputException
     */
    @Test
    void testRegister_PasswordSansMajuscule_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("test@test.com", "abcdef123!@#"));
    }

    /**
     * Test 12 : Mot de passe sans minuscule
     * Doit lever InvalidInputException
     */
    @Test
    void testRegister_PasswordSansMinuscule_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("test@test.com", "ABCDEF123!@#"));
    }

    /**
     * Test 13 : Mot de passe sans chiffre
     * Doit lever InvalidInputException
     */
    @Test
    void testRegister_PasswordSansChiffre_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("test@test.com", "Abcdef!@#"));
    }

    /**
     * Test 14 : Mot de passe sans caractère spécial
     * Doit lever InvalidInputException
     */
    @Test
    void testRegister_PasswordSansSpecial_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("test@test.com", "Abcdef123"));
    }

    // =========================================================
    // TESTS TP2 - Anti-brute force (lockout)
    // =========================================================

    /**
     * Test 15 : Lockout après 5 échecs
     * La 6ème tentative doit échouer avec message de verrouillage
     */
    @Test
    void testLogin_LockoutAfter5FailedAttempts() {
        String email = "lockout@test.com";
        authService.register(email, "Abcdef123!@#");

        // 5 tentatives échouées
        for (int i = 0; i < 5; i++) {
            try {
                authService.login(email, "wrong" + i);
            } catch (AuthenticationFailedException e) {
                // Ignorer, c'est normal
            }
        }

        // 6ème tentative → doit être verrouillée
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> authService.login(email, "wrong6")
        );
        assertTrue(exception.getMessage().contains("verrouillé"));
    }

    // =========================================================
    // TESTS TP2 - Non-divulgation des erreurs
    // =========================================================

    /**
     * Test 16 : Même message pour email inconnu et mauvais mot de passe
     * Le message ne doit pas révéler si l'email existe ou non.
     */
    @Test
    void testLogin_NonDivulgationDesErreurs() {
        // Email inconnu
        try {
            authService.login("inconnu@test.com", "Abcdef123!@#");
        } catch (AuthenticationFailedException e) {
            assertEquals("Identifiants invalides.", e.getMessage());
        }

        // Mauvais mot de passe
        authService.register("existing@test.com", "Abcdef123!@#");
        try {
            authService.login("existing@test.com", "wrong");
        } catch (AuthenticationFailedException e) {
            assertEquals("Identifiants invalides.", e.getMessage());
        }
    }
}