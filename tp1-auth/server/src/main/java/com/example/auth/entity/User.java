package com.example.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ============================================================
 * ENTITÉ UTILISATEUR - TP4
 * ============================================================
 *
 * ✅ TP1 : Mot de passe en CLAIR (volontairement dangereux)
 * ✅ TP2 : Ajout des champs failedAttempts et lockUntil (anti-brute force)
 * ✅ TP3 : Conservation des champs pour compatibilité
 * ✅ TP4 : Ajout des champs passwordEncrypted et encryptedIv (Master Key AES GCM)
 *
 * Cette implémentation évolue progressivement vers une solution industrielle.
 *
 * @see jakarta.persistence.Entity
 * @see jakarta.persistence.Table
 */
@Entity
@Table(name = "users")
public class User {

    // =========================================================
    // CHAMPS DE L'ENTITÉ
    // =========================================================

    /**
     * Identifiant unique auto-généré.
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
     * TP1 : Mot de passe en CLAIR (VOLONTAIREMENT DANGEREUX)
     * TP2 : Mot de passe HASHÉ avec BCrypt
     * TP3 : Mot de passe HASHÉ avec BCrypt (pour compatibilité)
     * TP4 : Ce champ est conservé pour compatibilité mais on utilise passwordEncrypted
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Date de création du compte.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Token de session (TP1: simple, TP3: JWT).
     */
    @Column(name = "session_token")
    private String sessionToken;

    // =========================================================
    // ✅ TP2 : CHAMPS ANTI-BRUTE FORCE
    // =========================================================

    /**
     * Nombre de tentatives de connexion échouées consécutives.
     */
    @Column(name = "failed_attempts")
    private int failedAttempts = 0;

    /**
     * Date jusqu'à laquelle le compte est verrouillé.
     */
    @Column(name = "lock_until")
    private LocalDateTime lockUntil;

    // =========================================================
    // ✅ TP4 : CHAMPS POUR MASTER KEY AES GCM
    // =========================================================

    /**
     * Mot de passe chiffré avec AES GCM (format: "v1:Base64(iv):Base64(ciphertext)")
     */
    @Column(name = "password_encrypted", columnDefinition = "TEXT")
    private String passwordEncrypted;

    /**
     * IV utilisé pour le chiffrement (stocké séparément)
     * Format: Base64(iv)
     */
    @Column(name = "encrypted_iv", length = 255)
    private String encryptedIv;

    // =========================================================
    // CONSTRUCTEURS
    // =========================================================

    public User() {}

    /**
     * Constructeur TP1/TP2.
     *
     * @param email    l'email de l'utilisateur
     * @param password le mot de passe (TP1: clair, TP2: hash BCrypt)
     */
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.failedAttempts = 0;
        this.lockUntil = null;
    }

    /**
     * Constructeur TP4 avec chiffrement.
     *
     * @param email              l'email de l'utilisateur
     * @param password           le mot de passe (hashé BCrypt)
     * @param passwordEncrypted  le mot de passe chiffré avec AES GCM
     * @param encryptedIv        l'IV utilisé pour le chiffrement
     */
    public User(String email, String password, String passwordEncrypted, String encryptedIv) {
        this.email = email;
        this.password = password;
        this.passwordEncrypted = passwordEncrypted;
        this.encryptedIv = encryptedIv;
        this.createdAt = LocalDateTime.now();
        this.failedAttempts = 0;
        this.lockUntil = null;
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

    // ✅ TP2 : Getters/Setters anti-brute force
    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public LocalDateTime getLockUntil() { return lockUntil; }
    public void setLockUntil(LocalDateTime lockUntil) { this.lockUntil = lockUntil; }

    // ✅ TP4 : Getters/Setters pour la Master Key
    public String getPasswordEncrypted() { return passwordEncrypted; }
    public void setPasswordEncrypted(String passwordEncrypted) { this.passwordEncrypted = passwordEncrypted; }

    public String getEncryptedIv() { return encryptedIv; }
    public void setEncryptedIv(String encryptedIv) { this.encryptedIv = encryptedIv; }

    // =========================================================
    // MÉTHODES UTILITAIRES
    // =========================================================

    /**
     * Vérifie si le compte est actuellement verrouillé (TP2).
     *
     * @return true si le compte est verrouillé, false sinon
     */
    public boolean isLocked() {
        return lockUntil != null && LocalDateTime.now().isBefore(lockUntil);
    }

    /**
     * Vérifie si le mot de passe est chiffré avec Master Key (TP4).
     *
     * @return true si le mot de passe est chiffré, false sinon
     */
    public boolean isEncrypted() {
        return passwordEncrypted != null && !passwordEncrypted.isEmpty();
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', locked=" + isLocked() + "}";
    }
}