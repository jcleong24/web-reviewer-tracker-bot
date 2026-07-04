# Backend — web-reviewer-bot

Java Spring Boot REST API (Java 21, Maven) exposing `POST /api/v1/analyze`.

## Prerequisites

- JDK 21+ and Maven
- A reachable PostgreSQL instance

## Required environment variables

| Variable | Purpose | Default |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/webreviewer` |
| `SPRING_DATASOURCE_USERNAME` | DB user | — (required) |
| `SPRING_DATASOURCE_PASSWORD` | DB password | — (required) |
| `ANTHROPIC_API_KEY` | Claude API key (read by the Anthropic client) | — (required for the real analyze step) |
| `ANTHROPIC_MODEL` | Model override | `claude-opus-4-8` |

Credentials are **never** hardcoded — set them in your environment (see `.gitignore`).
`schema.sql` is applied on startup (`spring.sql.init.mode=always`) and is idempotent.

## Run

```bash
mvn clean install      # compile, run tests, package
mvn spring-boot:run    # start on http://localhost:8080
```

## Pipeline (`Controller -> Service -> ...`)

1. **Rate limit** — `RateLimitInterceptor` (Bucket4j) guards `/api/v1/analyze`, returning 429 over quota.
2. **Fetch** — `RestClientPageFetchService` fetches raw HTML server-side with connect/read timeouts.
3. **Extract** — `JsoupContentExtractor` strips `script`/`style`/`nav`/`footer`, keeps text, truncates.
4. **Analyze** — `AnalysisServiceImpl` calls Claude. **Currently a stub** — see the `TODO` there.
5. Errors map to clean HTTP responses in `GlobalExceptionHandler`.

## Not yet wired in

- **Anthropic Java SDK** — add the `com.anthropic:anthropic-java` dependency (pin the current
  version from the SDK repo) and implement the real analyze call in `AnalysisServiceImpl`.
- **Persistence** — `schema.sql` scaffolds `analysis` / `analysis_dimension` tables for the
  deferred history feature; no JPA entities/repositories are wired up yet (MVP does not persist).
