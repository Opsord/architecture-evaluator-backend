# AGENTS.md

This repository is the **backend** of Architecture Evaluator. It statically analyzes Java / Spring Boot projects and returns maintainability metrics as JSON.

The Maven project is nested: all build, test, and source work happens in `architecture_evaluator_backend/`.

Sibling repos:

- Parent (git submodules + Docker Compose): https://github.com/Opsord/architecture-evaluator
- Frontend: https://github.com/Opsord/architecture-evaluator-frontend

## Setup and commands

Run from `architecture_evaluator_backend/`:

```bash
./mvnw spring-boot:run
./mvnw test
./mvnw -B verify
./mvnw -B verify -Pcoverage
```

Java 17. Spring Boot 3.4.x. Default port **8080**. Dev profile is active (`application.properties`); `application-dev.properties` excludes `DataSourceAutoConfiguration` — there is **no database** at runtime despite `spring-boot-starter-data-jpa` on the classpath.

Do not run Maven from the git root.

## Architecture

Package root: `io.github.opsord.architecture_evaluator_backend`.

| Area | Role |
|------|------|
| `modules.api.orchestrator` | HTTP API, zip/GitHub ingest, analysis pipeline |
| `modules.api.project_scanner` | Find `src`, parse `pom.xml` / Gradle files |
| `modules.api.parser` | JavaParser visitors → class/method/statement models |
| `modules.api.processor` | Metrics (program, complexity, coupling, cohesion) |
| `modules.config.WebConfig` | CORS |

Only controller: `OrchestratorController` at `/api/orchestrator`.

- `POST /analyze?projectPath=` — local path (dev/debug)
- `POST /analyze-upload` — multipart field `project` (zip of a project)
- `POST /analyze-github?repoUrl=` — GitHub HTTPS URL; downloads `{owner}/{repo}/archive/refs/heads/main.zip` only (`main` branch)

Response type: `ProjectAnalysisInstance` (classes grouped by layer: entities, documents, repositories, services, controllers, testClasses, otherClasses, plus pom/gradle DTOs).

Metrics per class: LOC/methods/statements, approx McCabe CC, afferent/efferent coupling + instability, LCOM1–5.

## Conventions

- Prefer existing visitor/service split: visitors extract AST data; services assemble DTOs and metrics.
- Keep JSON payloads small: omit nulls (`NON_NULL`), do **not** put full AST source on statements. For `MethodCallExpr` used by LCOM4, store the **callee method name** only (`StatementInstance`). `CohesionMetricsService.extractCalledNames` must stay null-safe.
- Dependency lookup must stay O(n) (`Set`/`Map`), not nested scans of all classes.
- JavaParser instances are `ThreadLocal` in `SrcScannerService` for parallel parse — do not share a parser across threads.
- Prefer `src/main/java` when present; zip extract should skip non-analysis paths.
- Tests live under `src/test/java/...` mirroring the module. Use JUnit 5 + Mockito. After behavior changes, update the matching `*Test` and run `./mvnw test`.
- Lombok is on the compiler annotation path; do not hand-write getters/setters on `@Data` types.

## Gotchas

- **LCOM4 depends on method names on statements.** Removing `structure`/source without keeping call names NPEs cohesion.
- GitHub ingest **always uses `main`**, not the repo default branch.
- Uploads up to 150MB; analysis can take minutes. HTTP GitHub timeouts are 15s connect / 120s read.
- CORS allows Vite (`5173`), CRA (`3000`), and Docker frontend on port 80.
- SonarCloud runs in `.github/workflows/sonarcloud.yml` (`mvn verify ... sonar -Pcoverage`). Local work does not need Sonar. `SONAR_TOKEN` is a repo secret.
- This repo is a **submodule** at `backend/` in the parent. After merging here, bump the parent submodule pointer on a separate parent PR.

## Boundaries

- Do not add persistence or rewrite the pipeline as async jobs unless asked.
- Do not invent extra REST endpoints in the README or code without implementing them.
- Do not commit secrets, PostgreSQL credentials, or large sample zips.
