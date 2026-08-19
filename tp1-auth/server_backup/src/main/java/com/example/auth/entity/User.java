package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ============================================================
 * ENTITÉ UTILISATEUR - TP1
 * ============================================================
 *
 * Cette classe représente un utilisateur dans la base de données.
 *
 * ⚠️ ATTENTION TP1 : VOLONTAIREMENT DANGEREUX !
 * - Le mot de passe est stocké en CLAIR (non hashé)
 * - Pas de politique de mot de passe forte
 * - Token de session stocké en base (non signé)
 *
 * Cette implémentation NE DOIT JAMAIS être utilisée en production.
 *
 * @see jakarta.persistence.Entity
 * @see jakarta.persistence.Table
 */
@Entity                           // Indique que cette classe est une entité JPA
@Table(name = "users")           // Nom de la table en base de données
public class User {

    // =========================================================
    // CHAMPS DE L'ENTITÉ
    // =========================================================

    /**
     * Identifiant unique auto-généré.
     * Stratégie IDENTITY = auto-incrémenté par la base.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email de l'utilisateur.
     * - unique = true : deux utilisateurs ne peuvent pas avoir le même email
     * - nullable = false : l'email est obligatoire
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * ⚠️ TP1 : Mot de passe en CLAIR !
     * VOLONTAIREMENT DANGEREUX - Ne jamais faire ça en production.
     *
     * Normalement, on stockerait un hash (BCrypt, Argon2, etc.)
     * Mais ici, c'est intentionnel pour montrer les risques.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Date de création du compte.
     * Initialisée automatiquement à la création.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Token de session simple (non signé, non sécurisé).
     * Stocké en base pour vérifier l'authentification.
     * ⚠️ TP1 : Simple, pas de JWT, pas de signature.
     */
    @Column(name = "session_token")
    private String sessionToken;

    // =========================================================
    // CONSTRUCTEURS
    // =========================================================

    /**
     * Constructeur par défaut (requis par JPA).
     */
    public User() {}

    /**
     * Constructeur pour créer un nouvel utilisateur.
     *
     * @param email    l'email de l'utilisateur
     * @param password le mot de passe (stocké en clair)
     */
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();  // Date actuelle
    }

    // =========================================================
    // GETTERS ET SETTERS
    // =========================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    // =========================================================
    // MÉTHODES UTILITAIRES
    // =========================================================

    /**
     * Représentation textuelle de l'utilisateur.
     * Utilisé pour les logs.
     */
    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "'}";
    }
}