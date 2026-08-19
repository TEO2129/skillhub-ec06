package com.example.auth.validator;

import com.example.auth.exception.InvalidInputException;

/**
 * VALIDATEUR DE POLITIQUE DE MOT DE PASSE - TP2
 * 12 caractères minimum, majuscule, minuscule, chiffre, caractère spécial
 */
public class PasswordPolicyValidator {

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