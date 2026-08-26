# Architecture Evaluator — Backend

REST service that **statically analyzes Java / Spring Boot projects** and returns maintainability metrics (complexity, coupling, cohesion, program size) as JSON.

This repository is a git submodule of [architecture-evaluator](https://github.com/Opsord/architecture-evaluator). The UI lives in [architecture-evaluator-frontend](https://github.com/Opsord/architecture-evaluator-frontend).

## Features

- Parse Java sources with JavaParser and classify types by Spring layer (`@RestController`, `@Service`, `@Repository`, `@Entity`, tests, other).
- Compute per-class metrics: LOC, methods, statements, approximate McCabe CC, Ca/Ce/instability, LCOM1–5.
- Read Maven (`pom.xml`) and Gradle build metadata when present.
- Ingest a project from a **local path**, a **uploaded zip**, or a **public GitHub repo** (`main` branch zip).

## Stack

- Java 17
- Spring Boot 3.4.x (Web; no database in the default `dev` profile)
- JavaParser, Lombok, Apache Commons Compress
- Maven Wrapper (`./mvnw`)
- JUnit 5 + Mockito; optional JaCoCo profile `coverage`
- SonarCloud on `main` / PRs (CI)

## Layout

```
architecture_evaluator_backend/   # Maven project — run all commands here
  src/main/java/.../modules/api/
    orchestrator/                 # HTTP API + zip/GitHub ingest
    project_scanner/              # src + pom/gradle discovery
    parser/                       # AST visitors
    processor/                    # metrics
  src/main/resources/application*.properties
  Dockerfile
.github/workflows/sonarcloud.yml
```

## Prerequisites

- JDK 17+
- Maven 3.9+ or the included wrapper

## Run locally

```sh
git clone https://github.com/Opsord/architecture-evaluator-backend.git
cd architecture-evaluator-backend/architecture_evaluator_backend
./mvnw spring-boot:run
```

The API listens on **http://localhost:8080**.

With Docker (from this Maven directory):

```sh
docker build -t architecture-evaluator-backend .
docker run --rm -p 8080:8080 architecture-evaluator-backend
```

For frontend + backend together, use Docker Compose in the [parent repository](https://github.com/Opsord/architecture-evaluator).

## API

Base path: `/api/orchestrator`. Responses are `ProjectAnalysisInstance` JSON (gzip when the client allows it). Null fields are omitted.

| Method | Path | Input | Notes |
|--------|------|--------|--------|
| `POST` | `/analyze` | query `projectPath` | Analyze a directory on the server |
| `POST` | `/analyze-upload` | multipart file field `project` | Zip of a Java project (max 150MB) |
| `POST` | `/analyze-github` | query `repoUrl` | Public GitHub HTTPS URL; downloads `.../archive/refs/heads/main.zip` |

Example:

```sh
curl -X POST 'http://localhost:8080/api/orchestrator/analyze-github?repoUrl=https://github.com/OWNER/REPO'
```

There is no metrics history store and no `GET /api/metrics` or `/api/reports`.

## Tests

```sh
cd architecture_evaluator_backend
./mvnw test
```

Coverage (used by CI):

```sh
./mvnw -B verify -Pcoverage
```

## License

MIT. See [LICENSE](LICENSE).
