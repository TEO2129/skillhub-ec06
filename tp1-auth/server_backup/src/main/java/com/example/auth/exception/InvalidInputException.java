package com.example.auth.exception;

/**
 * ============================================================
 * EXCEPTION : DONNÉES INVALIDES
 * ============================================================
 *
 * Cette exception est levée lorsque les données fournies par l'utilisateur
 * sont invalides.
 *
 * Codes HTTP associés : 400 BAD REQUEST
 *
 * Exemples d'utilisation :
 * - Email vide ou invalide
 * - Mot de passe trop court
 * - Données manquantes
 *
 * @see com.example.auth.exception.GlobalExceptionHandler
 */
public class InvalidInputException extends RuntimeException {

    /**
     * Constructeur avec message d'erreur.
     *
     * @param message le message d'erreur à transmettre au client
     */
    public InvalidInputException(String message) {
        super(message);  // Appel au constructeur de RuntimeException
    }
}