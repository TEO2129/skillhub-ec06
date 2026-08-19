package com.example.auth.controller;

import com.example.auth.entity.User;
import com.example.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class AuthController package com.example.auth.controller;

import com.example.auth.entity.User;
import com.example.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.Map;

/**
 * ============================================================
 * CONTROLEUR REST D'AUTHENTIFICATION - TP1
 * ============================================================
 *
 * Ce contrôleur expose les endpoints de l'API REST pour :
 * - L'inscription (/api/auth/register)
 * - La connexion (/api/auth/login)
 * - Le profil utilisateur (/api/me)
 *
 * @RestController = @Controller + @ResponseBody
 *
 * @see org.springframework.web.bind.annotation.RestController
 */
@RestController
@CrossOrigin(origins = "*")  // ✅ Permet au client JavaFX d'appeler l'API
public class AuthController {

    private final AuthService authService;

    /**
     * Constructeur avec injection de dépendance.
     * Spring injecte automatiquement AuthService.
     *
     * @param authService le service d'authentification
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =========================================================
    // ENDPOINT : INSCRIPTION
    // =========================================================

    /**
     * Inscrit un nouvel utilisateur.
     *
     * Méthode HTTP : POST
     * URL : /api/auth/register
     * Paramètres : email, password (en form-data ou x-www-form-urlencoded)
     *
     * Réponse :
     * - 200 OK : { "message": "Inscription réussie." }
     * - 400 BAD REQUEST : email invalide ou mot de passe trop court
     * - 409 CONFLICT : email déjà utilisé
     *
     * @param email    l'email de l'utilisateur
     * @param password le mot de passe
     * @return une réponse avec un message de succès
     */
    @PostMapping("/api/auth/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestParam String email,
            @RequestParam String password) {

        // Appel au service d'authentification
        authService.register(email, password);

        // Retourner une réponse JSON avec un message de succès
        return ResponseEntity.ok(Map.of("message", "Inscription réussie."));
    }

    // =========================================================
    // ENDPOINT : CONNEXION
    // =========================================================

    /**
     * Connecte un utilisateur et retourne un token.
     *
     * Méthode HTTP : POST
     * URL : /api/auth/login
     * Paramètres : email, password (en form-data ou x-www-form-urlencoded)
     *
     * Réponse :
     * - 200 OK : { "message": "Connexion réussie.", "token": "session-xxx" }
     * - 400 BAD REQUEST : email ou mot de passe manquant
     * - 401 UNAUTHORIZED : email inconnu ou mot de passe incorrect
     *
     * @param email    l'email de l'utilisateur
     * @param password le mot de passe
     * @return une réponse avec le token et un message de succès
     */
    @PostMapping("/api/auth/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestParam String email,
            @RequestParam String password) {

        // Appel au service d'authentification
        String token = authService.login(email, password);

        // Retourner le token et un message de succès
        return ResponseEntity.ok(Map.of(
                "message", "Connexion réussie.",
                "token", token
        ));
    }

    // =========================================================
    // ENDPOINT : PROFIL UTILISATEUR (PROTÉGÉ)
    // =========================================================

    /**
     * Récupère les informations de l'utilisateur connecté.
     *
     * Méthode HTTP : GET
     * URL : /api/me
     * Header requis : X-Session-Token: <token>
     *
     * Réponse :
     * - 200 OK : { "id": 1, "email": "toto@example.com", "createdAt": "2026-04-24T11:04:35" }
     * - 401 UNAUTHORIZED : token manquant, invalide ou expiré
     *
     * @param token le token de session (header X-Session-Token)
     * @return les informations de l'utilisateur
     */
    @GetMapping("/api/me")
    public ResponseEntity<Map<String, Object>> getMe(
            @RequestHeader("X-Session-Token") String token) {

        // Appel au service d'authentification
        User user = authService.getMe(token);

        // Retourner les informations de l'utilisateur
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "createdAt", user.getCreatedAt().toString()
        ));
    }
}