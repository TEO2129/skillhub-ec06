# TP1 - Serveur d'Authentification Dangereuse

[![TP1](https://img.shields.io/badge/TP1-Dangereuse-red.svg)](https://github.com/your-repo)

## ⚠️ AVERTISSEMENT

> **Cette implémentation est VOLONTAIREMENT DANGEREUSE et ne doit JAMAIS être utilisée en production.**

## Description

Serveur d'authentification Spring Boot développé dans le cadre du TP1.
L'objectif est de comprendre les risques d'une authentification mal conçue.

## Fonctionnalités TP1

| Fonctionnalité | Endpoint | Description |
|----------------|----------|-------------|
| **Inscription** | `POST /api/auth/register` | Crée un compte avec email + mot de passe (min 4 caractères) |
| **Connexion** | `POST /api/auth/login` | Authentification avec comparaison en clair |
| **Profil** | `GET /api/me` | Récupère les infos utilisateur (protégé par token) |

### Compte de test
- Email : `toto@example.com`
- Mot de passe : `pwd1234`

## 🔴 Risques identifiés (TP1)

1. **Mots de passe stockés en clair** - Vulnerable en cas de fuite de base de données
2. **Pas de politique de mot de passe forte** - Seulement 4 caractères minimum
3. **Pas de protection contre le brute force** - Pas de limitation des tentatives
4. **Token simple non signé** - Facilement prévisible ou falsifiable
5. **Pas de TLS/HTTPS** - Les identifiants circulent en clair sur le réseau

## Installation

```bash
# Aller dans le dossier du projet
cd EC06_MU202613/tp1-auth

# Lancer le serveur avec Maven
cd server
./mvnw spring-boot:run