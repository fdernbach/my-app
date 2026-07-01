# my-app

Application de démonstration full-stack en cours de développement, entièrement pilotée par **[Claude Code](https://claude.ai/code)** (Anthropic).

> L'ensemble du code source — architecture, choix techniques, implémentation et tests — a été produit par Claude Code à partir de prompts en langage naturel, sans écriture manuelle de code.

---

## Stack technique

| Couche    | Technologie                        |
|-----------|------------------------------------|
| Backend   | Java 21 · Spring Boot 3.5          |
| API       | OpenAPI 3.0 (contract-first)       |
| Frontend  | Angular 18 (standalone components) |
| Base de données       | PostgreSQL 16          |

---

## Backend — Spring Boot

- **Architecture hexagonale** : séparation domaine / ports / adapters
- **Maven multi-module** : `my-app-backend-api` (contrat OpenAPI) + `my-app-backend` (implémentation)
- **OpenAPI Generator** (`openapi-generator-maven-plugin`) : génération de l'interface Spring MVC depuis le YAML
- **Spring Data JPA** avec PostgreSQL 16 : entités, `JpaRepository`, optimistic locking (`@Version`)
- **JPA Auditing** (`@EnableJpaAuditing`) : remplissage automatique de `createdAt`, `createdBy`, `updatedAt`, `updatedBy` via `AuditorAware` et `DateTimeProvider`
- **Pagination** : `Pageable` / `PageRequest` Spring Data, exposée via `GET /users?page=0&size=10`
- **MapStruct** : mapping entre entités JPA, modèles domaine et DTOs OpenAPI
- **Spring Security** : Basic Auth (utilisateur `admin` en profil dev)
- **Spring Boot Actuator** : endpoint `/actuator/health`
- **RFC 7807 Problem Details** (`ProblemDetail`, `ResponseEntityExceptionHandler`) : réponses d'erreur standardisées avec `type`, `title`, `status`, `detail`, `instance` — erreurs de validation enrichies avec un tableau `errors[]` par champ
- **Testcontainers** : conteneur PostgreSQL éphémère démarré automatiquement pour tous les tests (`@Testcontainers` + `@ServiceConnection`) — aucune base de données externe requise pour les tests
- **Tests d'intégration** :
  - `@DataJpaTest` + `@AutoConfigureTestDatabase(replace=NONE)` pour la couche JPA sur PostgreSQL réel
  - `@SpringBootTest(webEnvironment=NONE)` pour la couche service
  - `@SpringBootTest(webEnvironment=MOCK)` + `@AutoConfigureMockMvc` pour la couche REST
  - `@WithMockUser` (Spring Security Test)

---

## Frontend — Angular 18

- **Standalone components** : pas de `NgModule`
- **ng-openapi-gen** : génération automatique des modèles TypeScript et services Angular depuis le YAML OpenAPI
- **Reactive Forms** (`FormGroup`, `FormControl`, `Validators`) pour les formulaires de création et modification
- **Signals** (`signal()`, `computed()`) pour la gestion d'état local des composants
- **RxJS / AsyncPipe** : `Observable`, `BehaviorSubject`, `switchMap`, `catchError`, `startWith` pour le chargement asynchrone et la pagination
- **Angular Router** : navigation déclarative, routes paramétrées (`/users/:id`, `/users/:id/edit`)
- **HttpClient** avec intercepteur Basic Auth (`HttpInterceptorFn`)
- **Proxy de développement** (`proxy.conf.json`) : réécriture de `/api` → `http://localhost:8080`
- **Control flow** `@if` / `@else` / `@for` (nouvelle syntaxe Angular 17+)
- **Composants** : `UserListComponent`, `UserCreateComponent`, `UserViewComponent`, `UserModifyComponent`, `ConfirmDialogComponent`
- **Pagination côté client** : sélecteur de taille de page (1 / 5 / 10 / 20), navigation première / précédente / suivante / dernière

### Éditeur de cours de mathématiques

L'éditeur de cours (`CourseEditorComponent`) est un éditeur de documents structurés dédié à la rédaction de cours de mathématiques. Il est intégré dans les vues de création, modification et consultation des cours.

**Technologies**

| Bibliothèque | Version | Rôle |
|---|---|---|
| Tiptap 3 | `@tiptap/core` 3.27 | Éditeur ProseMirror (nœuds, extensions, InputRules) |
| KaTeX | 0.16 | Rendu LaTeX → HTML (inline et display) |
| MathLive | 0.110 | Champ de saisie interactif pour les équations (double-clic) |

**Hiérarchie de blocs**

```
Cours
└── Chapitre
    └── Section
        └── Sous-section
            ├── Définition
            ├── Théorème
            ├── Démonstration
            ├── Exemple
            ├── Exercice
            └── Solution
```

Blocs atomiques (non-conteneurs) : **Équation**, **Image**, tableaux et listes.

**Saisie des mathématiques**

| Syntaxe | Résultat |
|---|---|
| `$f' = f$` | Maths inline rendues par KaTeX (InputRule dollar simple) |
| `$$\int_0^1 f$$` | Maths inline — syntaxe native Tiptap (double dollar) |
| Bouton « ∑ Équation » | Bloc display — éditeur MathLive (double-clic pour modifier) |
| Barre de raccourcis | Insertion en un clic : ℝ ℕ ℤ ℚ ℂ ∅ ∈ ∉ ⊂ ⊆ ∀ ∃ ⇒ ⟺ α β γ δ ε λ π σ θ ± ∞ |

**Extensions Tiptap personnalisées** (`src/app/course/course-editor/extensions/`)

- `course-blocks.ts` — 9 types de blocs structurels et sémantiques via la factory `makeCourseBlock`
- `equation-node.ts` — nœud atomique avec rendu KaTeX et éditeur MathLive au double-clic
- `image-node.ts` — nœud atomique avec saisie d'URL et légende inline
- `single-dollar-math.ts` — InputRule qui convertit `$...$` en nœud `inlineMath` (complète la règle native `$$...$$`)

**Architecture du composant**

- `ViewEncapsulation.None` : les styles SCSS du composant s'appliquent au DOM généré par ProseMirror
- Insertion contextuelle : un clic sur « Démonstration » insère le bloc *après* le Théorème courant (pas à l'intérieur), en remontant l'arbre ProseMirror via `$from.depth`
- Le document est sérialisé en JSON Tiptap (stocké dans la colonne `document_json` JSONB de PostgreSQL)

---

## Démarrage rapide

### Backend
```bash
# Démarrer PostgreSQL (Docker requis)
docker run -d --name myappdb \
  -e POSTGRES_DB=myappdb -e POSTGRES_USER=myapp -e POSTGRES_PASSWORD=myapp \
  -p 5432:5432 postgres:16

# Si le conteneur existe déjà (arrêté)
docker start myappdb

# Démarrer le backend
cd backend
mvn spring-boot:run -pl my-app-backend
# API disponible sur http://localhost:8080
# Credentials : admin / admin
```

### Frontend
```bash
cd frontend
npm install
npm run generate   # génère les modèles depuis le YAML OpenAPI
npm start
# Application disponible sur http://localhost:4200
```

---

## Structure du projet

```
my-app/
├── backend/
│   ├── pom.xml                          # Parent Maven
│   ├── my-app-backend-api/
│   │   └── src/main/resources/
│   │       └── my-app-backend-api.yaml  # Contrat OpenAPI
│   └── my-app-backend/
│       └── src/main/java/com/myapp/backend/
│           ├── domain/                  # Modèles et ports (hexagone)
│           ├── application/             # Services applicatifs
│           └── infrastructure/          # REST, JPA, config
└── frontend/
    ├── ng-openapi-gen.json              # Config génération TypeScript
    └── src/app/
        ├── api/                         # Code généré (ng-openapi-gen)
        ├── shared/                      # Composants réutilisables
        ├── user/                        # Feature User (CRUD + pagination)
        └── course/                      # Feature Course
            ├── course.service.ts
            ├── course-list/
            ├── course-create/
            ├── course-modify/
            ├── course-view/
            └── course-editor/           # Éditeur de cours (Tiptap + KaTeX + MathLive)
                └── extensions/          # Nœuds et règles Tiptap personnalisés
```
