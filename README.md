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
| Base de données (dev) | H2 in-memory          |

---

## Backend — Spring Boot

- **Architecture hexagonale** : séparation domaine / ports / adapters
- **Maven multi-module** : `my-app-backend-api` (contrat OpenAPI) + `my-app-backend` (implémentation)
- **OpenAPI Generator** (`openapi-generator-maven-plugin`) : génération de l'interface Spring MVC depuis le YAML
- **Spring Data JPA** avec H2 (profil `dev`) : entités, `JpaRepository`, optimistic locking (`@Version`)
- **JPA Auditing** (`@EnableJpaAuditing`) : remplissage automatique de `createdAt`, `createdBy`, `updatedAt`, `updatedBy` via `AuditorAware` et `DateTimeProvider`
- **Pagination** : `Pageable` / `PageRequest` Spring Data, exposée via `GET /users?page=0&size=10`
- **MapStruct** : mapping entre entités JPA, modèles domaine et DTOs OpenAPI
- **Spring Security** : Basic Auth (utilisateur `admin` en profil dev)
- **Spring Boot Actuator** : endpoint `/actuator/health`
- **Tests d'intégration** :
  - `@DataJpaTest` pour la couche JPA
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

---

## Démarrage rapide

### Backend
```bash
cd backend
mvn spring-boot:run -pl my-app-backend -Pdev
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
        └── user/                        # Feature User (CRUD + pagination)
```
