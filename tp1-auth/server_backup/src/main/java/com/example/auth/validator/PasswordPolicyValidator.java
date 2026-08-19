package com.example.auth.validator;

import com.example.auth.exception.InvalidInputException;

/**
 * Validateur de politique de mot de passe TP2.
 * Exige 12 caractères min, majuscule, minuscule, chiffre et caractère spécial.
 */
public class PasswordPolicyValidator {

    /**
     * Valide le mot de passe selon la politique TP2.
     * @param password le mot de passe à valider
     * @throws InvalidInputException si le mot de passe ne respecte pas la politique
     */
    public static void validate(String password) {
        if (password == null || password.length() < 12) {
            throw new InvalidInputException("Le mot de passe doit contenir au moins 12 caractères.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new InvalidInputException("Le mot de passe doit contenir au moins une majuscule.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new InvalidInputException("Le mot de passe doit contenir au moins une minuscule.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new InvalidInputException("Le mot de passe doit contenir au moins un chiffre.");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new InvalidInputException("Le mot de passe doit contenir au moins un caractère spécial.");
        }
    }
}