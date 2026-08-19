package com.example.auth;

import com.example.auth.entity.User;
import com.example.auth.exception.AuthenticationFailedException;
import com.example.auth.exception.InvalidInputException;
import com.example.auth.exception.ResourceConflictException;
import com.example.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================
 * TESTS UNITAIRES DU SERVICE D'AUTHENTIFICATION - TP1
 * ============================================================
 *
 * Ces tests vérifient le bon fonctionnement du service AuthService.
 *
 * @SpringBootTest : Charge le contexte Spring complet
 * @Transactional : Annule les modifications en base après chaque test
 *
 * Nombre de tests requis TP1 : minimum 8
 * Ici : 10 tests (8 obligatoires + 2 supplémentaires pour /api/me)
 *
 * @see org.springframework.boot.test.context.SpringBootTest
 */
@SpringBootTest
@Transactional
class AuthServiceTest {

    // Injection automatique du service à tester
    @Autowired
    private AuthService authService;

    // =========================================================
    // TESTS TP1 - 8 tests obligatoires
    // =========================================================

    /**
     * Test 1 : Inscription avec email vide
     * Doit lever InvalidInputException (400 BAD REQUEST)
     */
    @Test
    void testRegister_EmailVide_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("", "abcd"));
    }

    /**
     * Test 2 : Inscription avec email format incorrect
     * Doit lever InvalidInputException (400 BAD REQUEST)
     */
    @Test
    void testRegister_EmailFormatIncorrect_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("pasunemail", "abcd"));
    }

    /**
     * Test 3 : Inscription avec mot de passe trop court (< 4 caractères)
     * Doit lever InvalidInputException (400 BAD REQUEST)
     */
    @Test
    void testRegister_PasswordTropCourt_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("user@test.com", "abc"));
    }

    /**
     * Test 4 : Inscription réussie
     * Vérifie que l'utilisateur est bien créé avec les bonnes informations
     */
    @Test
    void testRegister_OK() {
        // Créer un utilisateur
        User user = authService.register("newuser@test.com", "abcd");

        // Vérifier que l'utilisateur n'est pas null
        assertNotNull(user);

        // Vérifier que l'email est correct
        assertEquals("newuser@test.com", user.getEmail());

        // Vérifier que la date de création est définie
        assertNotNull(user.getCreatedAt());
    }

    /**
     * Test 5 : Inscription refusée si email déjà existant
     * Doit lever ResourceConflictException (409 CONFLICT)
     */
    @Test
    void testRegister_EmailDejaExistant_ShouldThrowResourceConflict() {
        // Première inscription
        authService.register("duplicate@test.com", "abcd");

        // Deuxième inscription avec le même email
        assertThrows(ResourceConflictException.class,
                () -> authService.register("duplicate@test.com", "abcd"));
    }

    /**
     * Test 6 : Connexion réussie
     * Vérifie qu'un token est retourné après connexion
     */
    @Test
    void testLogin_OK() {
        // Inscrire un utilisateur
        authService.register("login@test.com", "abcd");

        // Se connecter
        String token = authService.login("login@test.com", "abcd");

        // Vérifier que le token n'est pas null ni vide
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    /**
     * Test 7 : Connexion avec mot de passe incorrect
     * Doit lever AuthenticationFailedException (401 UNAUTHORIZED)
     */
    @Test
    void testLogin_MauvaisMotDePasse_ShouldThrowAuthFailed() {
        // Inscrire un utilisateur
        authService.register("wrongpwd@test.com", "abcd");

        // Essayer de se connecter avec un mauvais mot de passe
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
                () -> authService.login("inconnu@test.com", "abcd"));
    }

    // =========================================================
    // TESTS SUPPLÉMENTAIRES TP1 (route /api/me)
    // =========================================================

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
        // Inscrire un utilisateur
        authService.register("me@test.com", "abcd");

        // Se connecter et récupérer le token
        String token = authService.login("me@test.com", "abcd");

        // Récupérer le profil avec le token
        User user = authService.getMe(token);

        // Vérifier que l'email est correct
        assertEquals("me@test.com", user.getEmail());
    }
}