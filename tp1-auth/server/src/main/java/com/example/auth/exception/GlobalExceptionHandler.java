package com.example.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ============================================================
 * GESTIONNAIRE GLOBAL DES EXCEPTIONS
 * ============================================================
 *
 * Cette classe intercepte toutes les exceptions levées par l'application
 * et les transforme en réponses JSON cohérentes.
 *
 * @RestControllerAdvice = combine @ControllerAdvice et @ResponseBody
 *
 * Format de réponse JSON pour toutes les erreurs :
 * {
 *   "timestamp": "2026-04-24T11:04:35.739",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "L'email est obligatoire.",
 *   "path": "/api/auth/register"
 * }
 *
 * Ce format est exigé par le sujet TP1.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Construit le corps de la réponse d'erreur.
     *
     * @param status  le code HTTP
     * @param message le message d'erreur
     * @param path    le chemin de la requête
     * @return une Map contenant les champs formatés
     */
    private Map<String, Object> buildBody(HttpStatus status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());  // Date/heure de l'erreur
        body.put("status", status.value());                      // Code HTTP (400, 401, 409)
        body.put("error", status.getReasonPhrase());             // Nom de l'erreur
        body.put("message", message);                           // Message explicite
        body.put("path", path);                                 // Chemin de la requête
        return body;
    }

    /**
     * Gère les erreurs 400 - Données invalides.
     *
     * @param ex  l'exception levée
     * @param req la requête HTTP
     * @return une réponse HTTP 400 avec un corps JSON
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidInput(
            InvalidInputException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI()));
    }

    /**
     * Gère les erreurs 401 - Authentification échouée.
     *
     * @param ex  l'exception levée
     * @param req la requête HTTP
     * @return une réponse HTTP 401 avec un corps JSON
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthFailed(
            AuthenticationFailedException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(buildBody(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI()));
    }

    /**
     * Gère les erreurs 409 - Conflit de ressource (email déjà utilisé).
     *
     * @param ex  l'exception levée
     * @param req la requête HTTP
     * @return une réponse HTTP 409 avec un corps JSON
     */
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(
            ResourceConflictException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI()));
    }
}