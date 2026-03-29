# URL Shortener API

A simple URL shortener REST API built with Kotlin and Spring Boot.

## JDK Version

- Tested with OpenJDK 25
- Kotlin jvmTarget is set to JVM_23
- Should work with Java 17+

## Running Locally

```bash
./gradlew bootRun
```

The API starts on `http://localhost:8080`.

To run multiple instances, assign each a unique prefix to guarantee non-overlapping codes:

```bash
# Instance A on port 8080
./gradlew bootRun --args='--url-shortener.instance-prefix=AA --server.port=8080'

# Instance B on port 8081
./gradlew bootRun --args='--url-shortener.instance-prefix=BB --server.port=8081'
```

When no prefix is set (the default), the service runs in standalone mode with 6-character random codes.

### Configuration

| Property | Default | Description |
|---|---|---|
| `url-shortener.instance-prefix` | _(empty)_ | Prefix prepended to generated codes for multi-instance uniqueness |
| `url-shortener.code-length` | `6` | Number of random characters in the generated code |
| `url-shortener.alphabet` | `abcdefghijkm...23456789` | Character set used for random code generation (default excludes ambiguous characters like `l`, `1`, `O`, `0`) |
| `url-shortener.max-retries` | `25` | Max retry attempts when a generated code collides |

## Running Tests

```bash
./gradlew test
```

A JaCoCo code coverage report is generated automatically after tests run. Open [build/reports/jacoco/test/html/index.html](build/reports/jacoco/test/html/index.html) in a browser to view it. Generated code (OpenAPI models/interfaces) and the Spring Boot application class are excluded from coverage metrics.

## API Documentation

Full API documentation is available in [api-docs.html](API-DOCS.html) — open it in a browser. It is generated from [src/main/resources/openapi.yml](src/main/resources/openapi.yml) and regenerated on every build. To regenerate manually:

```bash
./gradlew generateApiDocs
```

## API Endpoints

### Shorten a URL

```
POST /
Content-Type: application/json

{"url": "https://www.originenergy.com.au/electricity-gas/plans.html"}
```

Response (201 Created):
```json
{
  "shortUrl": "http://localhost:8080/a1B2c3",
  "originalUrl": "https://www.originenergy.com.au/electricity-gas/plans.html"
}
```

### Redirect to Original URL

```
GET /{code}
```

Response: 302 redirect to the original URL.

### Get URL Info

```
GET /{code}/info
```

Response (200 OK):
```json
{
  "code": "a1B2c3",
  "originalUrl": "https://www.originenergy.com.au/electricity-gas/plans.html",
  "createdAt": "2026-03-26T10:30:00Z"
}
```

## Design Decisions

- **In-memory store**: Uses a `ConcurrentHashMap` for thread-safe storage without external dependencies.
- **Random code generation**: 6-character random codes from an alphanumeric alphabet (excluding ambiguous characters like `l`, `1`, `O`, `0`). Codes are non-sequential and uniqueness is guaranteed atomically via `putIfAbsent`.
- **Instance-prefixed codes**: When running multiple instances, each can be assigned a unique prefix via `url-shortener.instance-prefix`. The prefix is prepended to the random part, ensuring no two instances can generate the same code — even with an in-memory store per instance. Defaults to empty (standalone mode).
- **Separation of concerns**: Controller handles HTTP, service handles business logic and validation, repository handles storage. Each layer is independently testable.
- **Atomic code uniqueness**: The repository uses `ConcurrentHashMap.putIfAbsent()` to prevent race conditions where two concurrent requests could generate the same code. On collision, the service retries with a new code (configurable via `url-shortener.max-retries`, default 25).
- **URL validation**: Only `http` and `https` schemes are accepted. Other schemes (e.g., `ftp`, `mailto`, `file`) are rejected with a 400 error. This is intentional — the service is designed for shortening web URLs only. Validation is implemented using the Jakarta Bean Validation framework with a custom `@ValidUrl` constraint annotation, keeping validation logic declarative and reusable across layers.
- **No deduplication**: Shortening the same URL multiple times produces a unique code each time. This is intentional — it allows independent tracking of each shortened link (e.g., per campaign or channel) without coupling code generation to URL lookups.
- **Immutable domain model**: `ShortenedUrl` is a Kotlin `data class` — immutable by default.
