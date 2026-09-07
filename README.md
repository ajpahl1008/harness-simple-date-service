# simple-date-service

A minimal RESTful web service that returns the current date and time as JSON in ISO-8601 format.

Spring Boot 4.1 · Java 25 · Gradle · springdoc-openapi (Swagger) · Spring Boot Actuator

## Endpoints

| Method | Path                | Description                                  |
|--------|---------------------|----------------------------------------------|
| GET    | `/api/v1/date`      | Current date and time as JSON                 |
| GET    | `/api/v1/openapi`   | OpenAPI 3.1 specification (JSON)              |
| GET    | `/api/v1/swagger-ui`| Swagger UI (redirects to `.../index.html`)    |
| GET    | `/actuator/health`  | Health check                                  |
| GET    | `/actuator/info`    | Service info, including the call counter      |
| GET    | `/actuator/metrics` | Micrometer metrics                            |

### `GET /api/v1/date`

```
HTTP/1.1 200
Content-Type: application/json

{"date":"2026-09-07T00:20:19.657"}
```

`date` is an ISO-8601 date-time with millisecond precision (`yyyy-MM-dd'T'HH:mm:ss.SSS`) derived
from the current UTC instant. The format is pinned with an explicit `@JsonFormat` pattern rather
than left to `LocalDateTime.toString()`, which drops trailing zeros and omits the fractional part
altogether on a whole second.

## Observability

Spring Boot Actuator is enabled, with `health`, `info`, and `metrics` exposed over HTTP
(`management.endpoints.web.exposure.include` in `application.yml`). Everything else stays
unexposed.

### Health

```bash
curl http://localhost:8080/actuator/health
```

```json
{"status":"UP","components":{"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
```

Health details are shown in full (`management.endpoint.health.show-details: always`), and the
built-in `liveness` and `readiness` groups are available at `/actuator/health/liveness` and
`/actuator/health/readiness` for Kubernetes probes.

### Request counter

Every call to `GET /api/v1/date` increments a Micrometer counter named
`date.service.requests`. It is readable two ways.

`/actuator/info` — the friendly view:

```bash
curl http://localhost:8080/actuator/info
```

```json
{"dateRequests":{"total":3,"meter":"date.service.requests"}}
```

`/actuator/metrics/date.service.requests` — the raw meter:

```bash
curl http://localhost:8080/actuator/metrics/date.service.requests
```

```json
{
  "name": "date.service.requests",
  "description": "Total number of requests served by the date endpoint",
  "baseUnit": "requests",
  "measurements": [{"statistic": "COUNT", "value": 3.0}]
}
```

The counter lives in memory on each instance: it starts at zero on boot and is not shared between
replicas. For a fleet-wide total, aggregate `date.service.requests` in the metrics backend.

Implementation:

- `DateRequestCounter` (`metrics`) — owns the Micrometer counter and exposes `increment()`/`count()`.
- `DateRequestInfoContributor` (`actuator`) — an `InfoContributor` that adds the `dateRequests`
  block to `/actuator/info`.
- `DateController` — increments the counter on each served request.

## Running

```bash
./gradlew bootRun
```

The service listens on port 8080. Try it:

```bash
curl http://localhost:8080/api/v1/date
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/info
```

## Docker

The image follows the same shape as the other Bespin services: Amazon Corretto on Alpine,
running as a non-root `sdg` user, with the boot jar copied to `/app/app.jar`.

Build the jar first, then the image:

```bash
./gradlew build
./createLocalDockerImage.zsh 0.0.1-SNAPSHOT
```

| Script | Purpose |
|--------|---------|
| `createLocalDockerImage.zsh <version>` | `linux/arm64` build loaded into the local Docker daemon |
| `createDockerHubImage.zsh <version>`   | Multi-arch (`arm64` + `amd64`) build pushed to Docker Hub with provenance and SBOM |
| `runDockerImage.zsh <version>`         | Removes any previous container, then runs the image on port 8080 |

All three take the image version as their only argument and publish to
`bespinengineering/simple-date-service`.

```bash
./runDockerImage.zsh 0.0.1-SNAPSHOT
```

`runDockerImage.zsh` passes a `.env` through with Docker's `--env-file` when one is present.
This service needs no configuration to start, so `.env` is optional.

### Container health

The image declares a `HEALTHCHECK` against `/actuator/health`, so `docker ps` and any orchestrator
reading container health see the service's own health check rather than just "the process is
running":

```bash
docker inspect --format '{{.State.Health.Status}}' simple-date-service
# healthy
```

`curl` is installed in the image for that check; it is the only package added on top of the base.

`build.gradle` disables the plain jar (`tasks.named('jar') { enabled = false }`) so that only the
executable boot jar lands in `build/libs`, which keeps the Dockerfile's
`simple-date-service-*.jar` glob unambiguous.

## Tests and coverage

```bash
./gradlew build
```

`build` runs the test suite and enforces a minimum of 90% line coverage via JaCoCo.
The HTML report is written to `build/reports/jacoco/test/html/index.html`.

Test layers:

- `DateControllerUnitTest` — plain JUnit tests of the date logic against fixed clocks
  (leap day, year boundaries, UTC day rollover, millisecond padding and truncation, round-trip).
- `DateResponseSerializationTest` — what Jackson actually emits at the format edges: `.000` on a
  whole second, `.100` rather than `.1`, and zero padding on every field.
- `DateControllerTest` — `@WebMvcTest` slice covering status, content type, and exact JSON body.
- `OpenApiDocumentationTest` — asserts the OpenAPI spec is served on its configured
  endpoint and documents `/api/v1/date`.
- `ActuatorEndpointsTest` — asserts `/actuator/health` reports `UP` and that the call counter is
  exposed, and moves, on both `/actuator/info` and `/actuator/metrics`.
- `SimpleDateServiceApplicationTests` — context load.

## Notes on the date source

The controller reads `java.time.Clock`, supplied as a Spring bean (`ClockConfig`), rather than
calling `LocalDate.now()` directly. Production uses `Clock.systemUTC()`; tests substitute a fixed
clock, which is what makes the expected timestamps exact rather than "whatever now happens to be".
