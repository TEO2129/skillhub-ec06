package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ============================================================
 * CONFIGURATION DE SÉCURITÉ SPRING - TP1
 * ============================================================
 *
 *  TP1 : Cette configuration désactive COMPLÈTEMENT Spring Security.
 *
 * Pourquoi ?
 * - Notre logique d'authentification est dans AuthService
 * - On veut que tous les endpoints soient accessibles
 * - Pas de sécurité ajoutée par Spring
 *
 *  TP1 : C'est volontairement dangereux
 *  TP2/TP3/TP4 : On ajoutera BCrypt, JWT, etc.
 *
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure la chaîne de filtres de sécurité.
     *
     * @param http l'objet HttpSecurity pour configurer la sécurité
     * @return le SecurityFilterChain configuré
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Désactive CSRF (Cross-Site Request Forgery)
                // Permet les requêtes POST/PUT/DELETE sans token CSRF
                .csrf(csrf -> csrf.disable())

                // Autorise toutes les requêtes sans authentification
                // TP1 : Aucune protection !
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    // TP1 : PAS de PasswordEncoder
    // Le mot de passe est stocké et comparé en clair
    // Cela sera ajouté en TP2 avec BCryptPasswordEncoder
}