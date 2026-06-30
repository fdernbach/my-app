# CLAUDE.md — my-app

Instructions pour Claude Code sur les conventions et attendus de ce projet.

---

## Architecture générale

Projet full-stack **contract-first** :
1. Le contrat API est défini dans `backend/my-app-backend-api/src/main/resources/my-app-backend-api.yaml`
2. Toute modification de l'API commence par le YAML, puis régénération backend (`mvn compile`) et frontend (`npm run generate`)
3. Ne jamais modifier les fichiers générés dans `backend/**/target/` ou `frontend/src/app/api/`

---

## Backend — Java / Spring Boot

### Langage et framework
- **Java 21**, **Spring Boot 3.5.x**, Maven multi-module
- Utiliser les features Java 21 : records, sealed classes, pattern matching, text blocks

### Architecture hexagonale — règles strictes
```
domain/        ← aucune dépendance Spring, aucune dépendance JPA
  model/       ← POJOs purs
  port/in/     ← interfaces use case (entrées)
  port/out/    ← interfaces repository (sorties)
  exception/   ← exceptions métier pures (pas d'ErrorResponseException)
application/   ← implémente port/in, injecte port/out — @Service @Transactional ici uniquement
infrastructure/
  rest/        ← @RestController, implémente l'interface générée UsersApi
  persistence/ ← @Component adapter, entités JPA, MapStruct mappers
  config/      ← @Configuration Spring (@EnableJpaAuditing, Security, etc.)
```

- **Jamais** de dépendance Spring/JPA dans `domain/`
- **Jamais** de `@Transactional` ailleurs que dans `application/`
- **Jamais** d'accès direct au repository JPA depuis le controller

### OpenAPI / génération de code
- Le plugin `openapi-generator-maven-plugin` génère l'interface Spring MVC dans `my-app-backend-api`
- `UserController` implémente l'interface générée (`UsersApi`) — ne pas ajouter d'annotations Spring MVC manuellement sur le controller
- Toute nouvelle route ou modification de signature passe par le YAML en premier

### JPA et persistance
- Entités dans `infrastructure/persistence/entity/`, jamais dans `domain/`
- Utiliser `@Embedded` pour les value objects (`AddressEmbeddable`, `AuditDataEmbeddable`)
- Optimistic locking avec `@Version`
- Utiliser `saveAndFlush()` + `entityManager.refresh()` dans l'adapter pour garantir l'état DB après audit
- Tri explicite obligatoire sur les requêtes paginées (`Sort.by(...)`)

### Audit JPA
- `@EnableJpaAuditing` configuré dans `JpaAuditingConfig` avec `AuditorAware<String>` et `DateTimeProvider` pour `OffsetDateTime`
- Ne jamais setter manuellement `createdAt`, `createdBy`, `updatedAt`, `updatedBy`

### Mapping
- **MapStruct** uniquement pour les mappings entre couches
- Utiliser `@AfterMapping` + `@MappingTarget` pour les cas où MapStruct ne peut pas dériver automatiquement (ex : nested version)

### Gestion des erreurs — RFC 7807
- Toutes les réponses d'erreur suivent **RFC 7807** (`ProblemDetail`)
- `ProblemDetailExceptionHandler` étend `ResponseEntityExceptionHandler`
- Chaque exception métier expose : `type` (URI `urn:problem:*`), `title`, `detail`, `instance`
- `spring.mvc.problemdetails.enabled=true` dans `application.yaml`
- Ne pas retourner de `ResponseEntity<String>` pour les erreurs

### Tests
- `@DataJpaTest` + `@WithMockUser` pour les tests de repository JPA
- `@SpringBootTest(webEnvironment=NONE)` + `@Transactional` pour les tests de service
- `@SpringBootTest(webEnvironment=MOCK)` + `@AutoConfigureMockMvc` + `@Transactional` pour les tests de controller
- Les tests IT (`*IT.java`) sont inclus dans Surefire via `<includes>`
- Tester la structure `ProblemDetail` (type, title, status) sur les cas d'erreur

### Configuration
- Fichiers `application.yaml` / `application-dev.yaml` — jamais `.properties`
- Profil `dev` : H2 in-memory, `create-drop`, console H2, Basic Auth `admin/admin`

---

## Frontend — Angular

### Version et style
- **Angular 18**, composants **standalone** uniquement — pas de `NgModule`
- Nouvelle syntaxe de control flow : `@if`, `@else if`, `@else`, `@for` — pas de `*ngIf` / `*ngFor`
- `@else if` ne supporte pas le alias `as` — utiliser un `@if` imbriqué dans `@else` si nécessaire

### Génération des modèles
- **ng-openapi-gen** génère tous les modèles et fonctions dans `src/app/api/`
- Importer les types depuis `../../api/models` (barrel `models.ts`) — jamais créer de modèles manuellement
- Après toute modification du YAML : `npm run generate`
- Ne jamais modifier les fichiers dans `src/app/api/`

### Composants
- Toujours **standalone** avec `imports: [...]` explicites
- `AsyncPipe` pour les observables dans les templates — pas de `subscribe()` dans `ngOnInit` sauf nécessité absolue
- `DatePipe` pour le formatage des dates
- `RouterLink` pour la navigation déclarative

### Gestion d'état
- **Signals** (`signal()`) pour l'état local mutable (loading, error, dialog state)
- **`BehaviorSubject`** pour les triggers de rechargement (pagination, refresh après mutation)
- Pattern `state$` observable avec `switchMap` + `catchError` + `startWith` pour le chargement asynchrone
- Ne pas utiliser `NgRx` ou autre state manager — les signals + RxJS suffisent

### Formulaires
- **Reactive Forms** (`FormGroup`, `FormControl`, `Validators`) — pas de template-driven forms
- `nonNullable: true` sur tous les `FormControl`
- Valider avec `form.markAllAsTouched()` avant soumission
- Signal `submitting` pour désactiver le bouton pendant la requête

### Routing
- Routes **statiques avant routes paramétrées** (`users/new` avant `users/:id`)
- Routes paramétrées imbriquées : `users/:id/edit` avant `users/:id`
- Utiliser `ActivatedRoute.snapshot.paramMap` pour lire les params dans `ngOnInit`

### HTTP
- `UserService` (handwritten) utilise `HttpClient` directement avec les types générés
- Intercepteur Basic Auth dans `auth/basic-auth.interceptor.ts` (`HttpInterceptorFn`)
- Proxy dev dans `src/proxy.conf.json` : `/api` → `http://localhost:8080` avec `pathRewrite`

### Structure des dossiers
```
src/app/
  api/              ← généré par ng-openapi-gen, ne pas modifier
  shared/           ← composants réutilisables (ex: ConfirmDialogComponent)
  user/             ← feature User
    user.service.ts ← service handwritten utilisant les types générés
    user-list/
    user-create/
    user-view/
    user-modify/
```

---

## Commandes utiles

```bash
# Backend
cd backend && mvn spring-boot:run -pl my-app-backend   # démarrer (profil dev par défaut)
cd backend && mvn test                                  # tous les tests

# Frontend
cd frontend && npm run generate   # régénérer les modèles depuis le YAML
cd frontend && npm start           # démarrer sur http://localhost:4200
```
