package com.example.auth.exception;

/**
 * ============================================================
 * EXCEPTION : CONFLIT DE RESSOURCE
 * ============================================================
 *
 * Cette exception est levée lorsqu'une ressource est en conflit
 * avec une autre existante.
 *
 * Codes HTTP associés : 409 CONFLICT
 *
 * Exemples d'utilisation :
 * - Email déjà utilisé
 * - Utilisateur déjà inscrit
 *
 * @see com.example.auth.exception.GlobalExceptionHandler
 */
public class ResourceConflictException extends RuntimeException {

    /**
     * Constructeur avec message d'erreur.
     *
     * @param message le message d'erreur à transmettre au client
     */
    public ResourceConflictException(String message) {
        super(message);  // Appel au constructeur de RuntimeException
    }
}