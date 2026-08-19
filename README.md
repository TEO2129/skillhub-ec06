# SkillHub — Plateforme de Formation en Ligne

## Description

SkillHub est une plateforme web collaborative mettant en relation formateurs et apprenants autour de formations en ligne structurées en modules.

---

## Architecture Microservices

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (React)                           │
│                     http://localhost:5173                       │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP
          ┌────────────────┴─────────────────┐
          │                                  │
          ▼                                  ▼
┌─────────────────────┐          ┌───────────────────────┐
│   Laravel API REST  │◄────────►│  Spring Boot SSO      │
│   (Backend PHP)     │  HTTP    │  (Microservice Auth)  │
│   port 8000         │          │  port 8080            │
└────────┬────────────┘          └───────────┬───────────┘
         │                                   │
    ┌────┴────┐                         ┌────┴────┐
    │  MySQL  │                         │   H2    │
    │  :3307  │                         │ (mémoire│
    └─────────┘                         └─────────┘
         │
    ┌────┴────┐
    │ MongoDB │
    │  :27017 │
    └─────────┘
```

### Services

| Service | Technologie | Port | Rôle |
|---|---|---|---|
| Frontend | React + Vite | 5173 | Interface utilisateur |
| Backend | Laravel 12 + PHP 8.4 | 8000 | API REST principale |
| Auth SSO | Spring Boot 3.3 + Java 17 | 8080 | Microservice authentification |
| Base MySQL | MySQL 8.0 | 3307 | Données principales |
| Base MongoDB | MongoDB latest | 27017 | Logs et historisation |
| phpMyAdmin | phpMyAdmin | 8080 | Administration MySQL |

---

## Système d'Authentification SSO

### Fonctionnement

L'authentification est déléguée au microservice Spring Boot SSO selon le flux suivant :

```
1. Client → POST /api/auth/login (Spring Boot :8080)
           body: email + password

2. Spring Boot → vérifie les credentials
              → génère un JWT signé avec la Master Key
              → retourne { token: "eyJhbG..." }

3. Client → GET /api/sso/profil (Laravel :8000)
           header: Authorization: Bearer <token>

4. Laravel (SSOAuthMiddleware) → GET /api/me (Spring Boot :8080)
                                header: X-Session-Token: <token>

5. Spring Boot → valide le token → retourne les infos utilisateur

6. Laravel → autorise la requête si Spring Boot répond 200
```

### Master Key JWT

La clé secrète de signature des tokens JWT est configurée dans `application.properties` :

```properties
jwt.secret=SkillHubMasterKey2026!SecretJWTKey256BitsMinimum
jwt.expiration=86400000
```

> ⚠️ En production, cette clé doit être stockée dans les secrets GitHub Actions et jamais en clair dans le code.

### Middleware Laravel SSO

Le fichier `app/Http/Middleware/SSOAuthMiddleware.php` intercepte les requêtes sur les routes protégées et délègue la validation du token au microservice Spring Boot via l'endpoint `/api/me`.

---

## Règle Métier — Limite d'Inscriptions (Q1)

### Description

Un apprenant ne peut pas s'inscrire à plus de **5 formations simultanément**.

### Endpoint modifié

```
POST /api/formations/{id}/inscription
```

### Comportement

| Situation | Code HTTP | Message |
|---|---|---|
| Moins de 5 inscriptions | 201 | Inscription réussie |
| Exactement 5 inscriptions | 400 | Vous ne pouvez pas vous inscrire à plus de 5 formations simultanément |
| Déjà inscrit | 409 | Vous êtes déjà inscrit à cette formation |
| Non apprenant | 403 | Seul un apprenant peut s'inscrire |

### Fichier modifié

`app/Http/Controllers/InscriptionController.php` — ajout de la constante `LIMITE_INSCRIPTIONS = 5` et vérification avant l'enregistrement.

### Test correspondant

```bash
php artisan test --filter=inscription_store_echoue_si_limite_5_formations_atteinte
```

---

## Installation et Lancement

### Prérequis

- Docker Desktop
- Git
- PHP 8.4
- Composer
- Node.js 20+
- Java 17
- Maven 3.9+

### Lancement complet avec Docker


# Lancer tous les services
cd skillhub_CICD_backend
docker-compose up -d --build

# Vérifier que les containers tournent
docker ps
```

### Lancement manuel

**Backend Laravel :**
```bash
cd skillhub_CICD_backend
composer install
cp .env.example .env
php artisan key:generate
php artisan jwt:secret
php artisan migrate
php artisan serve
```

**Microservice Spring Boot SSO :**
```bash
cd tp1-auth/server
mvn spring-boot:run -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 -Dmaven.compiler.release=17
```

**Frontend React :**
```bash
cd Skillhub-FrontEnd
npm install
npm run dev
```

---

## Variables d'Environnement

### Laravel — `.env.example`

```env
APP_NAME=SkillHub
APP_ENV=local
APP_KEY=
APP_DEBUG=true
APP_URL=http://localhost:8000

DB_CONNECTION=mysql
DB_HOST=mysql
DB_PORT=3306
DB_DATABASE=skillhub2
DB_USERNAME=root
DB_PASSWORD=

MONGODB_HOST=mongodb
MONGODB_PORT=27017
MONGODB_DATABASE=skillhub_logs

JWT_SECRET=

# URL du microservice Spring Boot SSO
SSO_SERVICE_URL=http://localhost:8080
```

### Spring Boot — `application.properties`

```properties
server.port=8080
spring.datasource.url=jdbc:h2:mem:authdb
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true

# Master Key JWT
jwt.secret=SkillHubMasterKey2026!SecretJWTKey256BitsMinimum
jwt.expiration=86400000
```

---

## Outils

### Docker — Conteneurisation

Docker isole chaque service dans un container indépendant. Le `docker-compose.yaml` orchestre tous les services (Laravel, Spring Boot, MySQL, MongoDB, phpMyAdmin) et gère les dépendances entre eux via `depends_on` et `healthcheck`.

### GitHub Actions — CI/CD

Le pipeline `.github/workflows/ci-cd.yml` se déclenche sur chaque push vers `dev` et `main` et exécute dans l'ordre :
1. Checkout du code
2. Installation des dépendances (Composer + Maven)
3. Lint (php-cs-fixer)
4. Tests unitaires Laravel (`php artisan test`)
5. Analyse qualité SonarCloud
6. Build des images Docker taguées avec le SHA Git
7. Push vers la registry (sur merge vers `main` uniquement)

### SonarCloud — Analyse Qualité

SonarCloud analyse automatiquement la qualité du code à chaque push. Les métriques surveillées sont la couverture de tests, les bugs potentiels, les code smells et les vulnérabilités de sécurité.

**Analyse avant la feature `limite-inscriptions` :** coverage estimé ~70%

**Analyse après la feature `limite-inscriptions` :** coverage estimé ~80% grâce aux 2 nouveaux tests ajoutés.

**Plan d'amélioration proposé (sans modification de code) :**
- Ajouter des tests sur les routes non authentifiées pour atteindre 90%+
- Documenter les endpoints avec PHPDoc pour réduire les code smells
- Configurer les règles SonarCloud pour ignorer les fichiers générés (`vendor/`, `storage/`)
- Mettre en place des quality gates bloquants en dessous de 80% de coverage

---

## Branches Git

| Branche | Rôle |
|---|---|
| `main` | Code stable, déploiement production |
| `dev` | Intégration continue |
| `feature/limite-inscriptions` | Règle métier Q1 + SSO Q2 |

---

## Tests Unitaires

```bash
# Tous les tests
cd skillhub_CICD_backend
php artisan test

# Test spécifique limite inscriptions
php artisan test --filter=inscription_store_echoue_si_limite_5_formations_atteinte

# Avec coverage
php artisan test --coverage
```

Les tests couvrent : l'authentification JWT, le CRUD formations, les modules, les inscriptions, la messagerie, les logs MongoDB et la règle métier des 5 inscriptions.


## Q1 

test ajouté au fichier test ModuleEtInscriptionControllerTest a partir de la ligne 564 : 

//limite de 5 inscriptions en meme temps

    #[\PHPUnit\Framework\Attributes\Test]
    public function inscription_store_echoue_si_limite_5_formations_atteinte(): void
    {
        ['user' => $formateur]                    = $this->creerUser('formateur');
        ['user' => $apprenant, 'token' => $token] = $this->creerUser('apprenant');

        // L'apprentit est inscrit à 5 formations
        for ($i = 1; $i <= 5; $i++) {
            $formation = $this->creerFormation($formateur, ['titre' => 'Formation ' . $i]);
            $this->inscrire($apprenant, $formation);
        }

        // Tente une 6ème inscription
        $formation6 = $this->creerFormation($formateur, ['titre' => 'Formation 6']);

        $response = $this->postJson(
            '/api/formations/' . $formation6->id . '/inscription',
            [],
            $this->authHeaders($token)
        );

        $response->assertStatus(400)
            ->assertJsonFragment([
                'message' => 'Vous ne pouvez pas vous inscrire à plus de 5 formations simultanément',
            ]);

        // Vérifie que la 6ème inscription n'a pas été créée
        $this->assertDatabaseMissing('inscriptions', [
            'utilisateur_id' => $apprenant->id,
            'formation_id'   => $formation6->id,
        ]);
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function inscription_store_reussit_si_exactement_4_inscriptions_existantes(): void
    {
        ['user' => $formateur]                    = $this->creerUser('formateur');
        ['user' => $apprenant, 'token' => $token] = $this->creerUser('apprenant');

        // Inscrit l'apprenant à 4 formations
        for ($i = 1; $i <= 4; $i++) {
            $formation = $this->creerFormation($formateur, ['titre' => 'Formation ' . $i]);
            $this->inscrire($apprenant, $formation);
        }

        // La 5ème doit réussir
        $formation5 = $this->creerFormation($formateur, ['titre' => 'Formation 5']);

        $response = $this->postJson(
            '/api/formations/' . $formation5->id . '/inscription',
            [],
            $this->authHeaders($token)
        );

        $response->assertStatus(201);

        $this->assertDatabaseHas('inscriptions', [
            'utilisateur_id' => $apprenant->id,
            'formation_id'   => $formation5->id,
        ]);
    }    }

