package com.example.auth.repository;

import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ============================================================
 * REPOSITORY DES UTILISATEURS
 * ============================================================
 *
 * Interface pour accéder à la table "users" en base de données.
 *
 * JpaRepository fournit déjà des méthodes comme :
 *   - save() : sauvegarder un utilisateur
 *   - findAll() : récupérer tous les utilisateurs
 *   - findById() : récupérer par ID
 *   - delete() : supprimer un utilisateur
 *
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository                         // Indique que c'est un bean Spring pour l'accès aux données
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son email.
     *
     * @param email l'email à rechercher
     * @return un Optional contenant l'utilisateur s'il existe, vide sinon
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un email existe déjà en base.
     *
     * @param email l'email à vérifier
     * @return true si l'email existe, false sinon
     */
    boolean existsByEmail(String email);

    /**
     * TP1 : Recherche un utilisateur par son token de session.
     * Utilisé pour la route protégée /api/me.
     *
     * ⚠️ TP1 : Le token est stocké en clair en base (non sécurisé).
     *
     * @param sessionToken le token de session
     * @return l'utilisateur correspondant
     */
    Optional<User> findBySessionToken(String sessionToken);
}