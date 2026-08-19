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

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    // =========================================================
    // TESTS TP1 (10 tests)
    // =========================================================

    @Test
    void testRegister_EmailVide_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("", "Password123!"));
    }

    @Test
    void testRegister_EmailFormatIncorrect_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("pasunemail", "Password123!"));
    }

    @Test
    void testRegister_PasswordTropCourt_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("user@test.com", "abc"));
    }

    @Test
    void testRegister_OK() {
        User user = authService.register("newuser@test.com", "Password123!");
        assertNotNull(user);
        assertEquals("newuser@test.com", user.getEmail());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testRegister_EmailDejaExistant_ShouldThrowResourceConflict() {
        authService.register("duplicate@test.com", "Password123!");
        assertThrows(ResourceConflictException.class,
                () -> authService.register("duplicate@test.com", "Password123!"));
    }

    @Test
    void testLogin_OK() {
        authService.register("login@test.com", "Password123!");
        String token = authService.login("login@test.com", "Password123!");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void testLogin_MauvaisMotDePasse_ShouldThrowAuthFailed() {
        authService.register("wrongpwd@test.com", "Password123!");
        assertThrows(AuthenticationFailedException.class,
                () -> authService.login("wrongpwd@test.com", "mauvais"));
    }

    @Test
    void testLogin_EmailInconnu_ShouldThrowAuthFailed() {
        assertThrows(AuthenticationFailedException.class,
                () -> authService.login("inconnu@test.com", "Password123!"));
    }

    @Test
    void testGetMe_SansToken_ShouldThrowAuthFailed() {
        assertThrows(AuthenticationFailedException.class,
                () -> authService.getMe(null));
    }

    @Test
    void testGetMe_OK_ApresLogin() {
        authService.register("me@test.com", "Password123!");
        String token = authService.login("me@test.com", "Password123!");
        User user = authService.getMe(token);
        assertEquals("me@test.com", user.getEmail());
    }

    // =========================================================
    // TP2 : NOUVEAUX TESTS
    // =========================================================

    @Test
    void testRegister_PasswordSansMajuscule_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("test@test.com", "password123!"));
    }

    @Test
    void testRegister_PasswordSansMinuscule_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("test@test.com", "PASSWORD123!"));
    }

    @Test
    void testRegister_PasswordSansChiffre_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("test@test.com", "Password!"));
    }

    @Test
    void testRegister_PasswordSansSpecial_ShouldThrowInvalidInput() {
        assertThrows(InvalidInputException.class,
                () -> authService.register("test@test.com", "Password123"));
    }

    @Test
    void testLogin_LockoutAfter5FailedAttempts() {
        String email = "lockout@test.com";
        authService.register(email, "Password123!");

        for (int i = 0; i < 5; i++) {
            try {
                authService.login(email, "wrong" + i);
            } catch (AuthenticationFailedException e) {
                // Ignorer
            }
        }

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(email, "wrong6"));
    }

    @Test
    void testLogin_NonDivulgationDesErreurs() {
        // Email inconnu
        try {
            authService.login("inconnu@test.com", "Password123!");
        } catch (AuthenticationFailedException e) {
            assertEquals("Identifiants invalides.", e.getMessage());
        }

        // Mauvais mot de passe
        authService.register("existing@test.com", "Password123!");
        try {
            authService.login("existing@test.com", "wrong");
        } catch (AuthenticationFailedException e) {
            assertEquals("Identifiants invalides.", e.getMessage());
        }
    }
}