# TP Auth - Serveur d'Authentification Spring Boot

## Description
Serveur d'authentification développé progressivement sur 4 TPs, passant d'une implémentation volontairement dangereuse à une solution industrielle.

---

## 📦 Stack technique

| Composant | Version |
|-----------|---------|
| Java | 17 |
| Spring Boot | 3.3.4 |
| Spring Security | (BCrypt) |
| JWT (JJWT) | 0.11.5 |
| H2 Database | (dev) |
| MySQL | 8.0 (prod) |
| Docker | 20.10+ |
| Maven | 3.8+ |

---

## 🔄 Évolution des TPs

| TP | Nom | Description | Sécurité |
|----|-----|-------------|----------|
| **TP1** | Authentification Dangereuse | Mots de passe en clair, validation min 4 caractères | 🔴 Très faible |
| **TP2** | Authentification Fragile | BCrypt, politique stricte, anti-brute force | 🟠 Faible |
| **TP3** | Authentification Forte | HMAC, nonce, timestamp, anti-rejeu, JWT | 🟢 Bonne |
| **TP4** | Authentification Industrielle | Master Key AES GCM, Docker, CI/CD | 🟢✅ Industrielle |

---

## ⚠️ TP1 - Authentification Dangereuse

> **⚠️ Cette implémentation est VOLONTAIREMENT DANGEREUSE et ne doit JAMAIS être utilisée en production.**

### Fonctionnalités TP1

| Fonctionnalité | Endpoint | Description |
|----------------|----------|-------------|
| **Inscription** | `POST /api/auth/register` | Crée un compte avec email + mot de passe (min 4 caractères) |
| **Connexion** | `POST /api/auth/login` | Authentification avec comparaison en clair |
| **Profil** | `GET /api/me` | Récupère les infos utilisateur (protégé par token) |

### Compte de test
- Email : `toto@example.com`
- Mot de passe : `pwd1234`

### 🔴 Risques identifiés (TP1)
1. Mots de passe stockés en clair
2. Pas de politique de mot de passe forte (4 caractères seulement)
3. Pas de protection contre le brute force
4. Token simple non signé
5. Pas de TLS/HTTPS

### Tags TP1
| Tag | Description |
|-----|-------------|
| `v1.0-init` | Structure initiale |
| `v1-tp1` | TP1 finalisé |

---

## 🟠 TP2 - Authentification Fragile

### ✅ Améliorations TP2

| Fonctionnalité | Description |
|----------------|-------------|
| **BCrypt** | Hashage des mots de passe (plus de stockage en clair) |
| **Politique stricte** | 12 caractères + majuscule + minuscule + chiffre + caractère spécial |
| **Anti-brute force** | 5 échecs → 2 minutes de blocage |
| **Non-divulgation** | Même message pour email inconnu et mauvais mot de passe |
| **Tests** | 16 tests unitaires |
| **SonarCloud** | Configuration SonarCloud |

### ⚠️ Reste fragile
> TP2 améliore le stockage mais ne protège pas encore contre le rejeu.
> Le mot de passe circule encore en clair dans la requête.

### Tags TP2
| Tag | Description |
|-----|-------------|
| `v2.1-db-migration` | Migration base (failedAttempts, lockUntil) |
| `v2.2-password-policy` | Politique mot de passe stricte |
| `v2.3-hashing` | BCrypt implémenté |
| `v2.4-lockout` | Anti-brute force |
| `v2.6-sonarcloud` | SonarCloud configuré |
| `v2-tp2` | TP2 finalisé |

---

## 🟢 TP3 - Authentification Forte

### ✅ Améliorations TP3

| Fonctionnalité | Description |
|----------------|-------------|
| **HMAC** | Signature HMAC-SHA256 (le mot de passe ne circule plus) |
| **Nonce** | Anti-rejeu (table `auth_nonce`) |
| **Timestamp** | Fenêtre de ±60 secondes |
| **JWT** | Token valide 15 minutes |
| **Temps constant** | Comparaison en temps constant (anti-timing attack) |
| **Couverture** | 80% de couverture de tests |

### Nouveaux endpoints TP3
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/login-hmac` | Login avec HMAC + nonce + timestamp |

### Tags TP3
| Tag | Description |
|-----|-------------|
| `v3.0-start` | Début TP3 |
| `v3.1-db-nonce` | Table `auth_nonce` |
| `v3.2-hmac-client` | Service HMAC |
| `v3.3-hmac-server` | Login HMAC |
| `v3.4-anti-replay` | Anti-rejeu |
| `v3.5-token` | Token SSO JWT |
| `v3.6-tests-80` | Couverture 80% |
| `v3-tp3` | TP3 finalisé |

---

## 🟢✅ TP4 - Authentification Industrielle

### ✅ Améliorations TP4

| Fonctionnalité | Description |
|----------------|-------------|
| **Master Key AES GCM** | Chiffrement des mots de passe au repos |
| **APP_MASTER_KEY** | Variable d'environnement obligatoire |
| **Docker** | Dockerfile + docker-compose.yml |
| **CI/CD** | GitHub Actions (push sur main/dev) |
| **SonarCloud** | Quality Gate obligatoire |
| **Sécurité** | Aucune clé en clair dans le code |

### Variables d'environnement TP4
| Variable | Description | Requis |
|----------|-------------|--------|
| `APP_MASTER_KEY` | Clé maître AES GCM (256 bits min) | ✅ OUI |
| `JWT_SECRET` | Clé pour signer les JWT | ⚠️ Optionnel |
| `JWT_EXPIRATION` | Durée de validité JWT (ms) | ⚠️ Optionnel |

### Docker (TP4)
```bash
docker-compose up --build