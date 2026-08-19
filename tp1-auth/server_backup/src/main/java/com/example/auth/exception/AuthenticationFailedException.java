package com.example.auth.exception;

/**
 * ============================================================
 * EXCEPTION : AUTHENTIFICATION ÉCHOUÉE
 * ============================================================
 *
 * Cette exception est levée lorsque l'utilisateur ne peut pas
 * s'authentifier correctement.
 *
 * Codes HTTP associés : 401 UNAUTHORIZED
 *
 * Exemples d'utilisation :
 * - Email inconnu
 * - Mot de passe incorrect
 * - Token expiré ou invalide
 *
 * @see com.example.auth.exception.GlobalExceptionHandler
 */
public class AuthenticationFailedException extends RuntimeException {

    /**
     * Constructeur avec message d'erreur.
     *
     * @param message le message d'erreur à transmettre au client
     */
    public AuthenticationFailedException(String message) {
        super(message);  // Appel au constructeur de RuntimeException
    }
}