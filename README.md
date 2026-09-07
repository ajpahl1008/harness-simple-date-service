# simple-date-service

A minimal RESTful web service that returns the current date as JSON in ISO-8601 format.

Spring Boot 4.1 · Java 25 · Gradle · springdoc-openapi (Swagger) · Spring Boot Actuator

## Endpoints

| Method | Path                | Description                                  |
|--------|---------------------|----------------------------------------------|
| GET    | `/api/v1/date`      | Current date as JSON                          |
| GET    | `/api/v1/openapi`   | OpenAPI 3.1 specification (JSON)              |
| GET    | `/api/v1/swagger-ui`| Swagger UI (redirects to `.../index.html`)    |
| GET    | `/actuator/health`  | Health check                                  |
| GET    | `/actuator/info`    | Service info, including the call counter      |
| GET    | `/actuator/metrics` | Micrometer metrics                            |

### `GET /api/v1/date`

```
HTTP/1.1 200
Content-Type: application/json

{"date":"2026-09-06"}
```

`date` is an ISO-8601 calendar date (`yyyy-MM-dd`) derived from the current UTC instant.

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

## Tests and coverage

```bash
./gradlew build
```

`build` runs the test suite and enforces a minimum of 90% line coverage via JaCoCo.
The HTML report is written to `build/reports/jacoco/test/html/index.html`.

Test layers:

- `DateControllerUnitTest` — plain JUnit tests of the date logic against fixed clocks
  (leap day, year boundaries, UTC day rollover, ISO formatting) plus counter increments.
- `DateControllerTest` — `@WebMvcTest` slice covering status, content type, exact JSON body, and
  that a served request bumps the counter.
- `OpenApiDocumentationTest` — asserts the OpenAPI spec is served on its configured
  endpoint and documents `/api/v1/date`.
- `ActuatorEndpointsTest` — asserts `/actuator/health` reports `UP` and that the call counter is
  exposed, and moves, on both `/actuator/info` and `/actuator/metrics`.
- `SimpleDateServiceApplicationTests` — context load.

## Notes on the date source

The controller reads `java.time.Clock`, supplied as a Spring bean (`ClockConfig`), rather than
calling `LocalDate.now()` directly. Production uses `Clock.systemUTC()`; tests substitute a fixed
clock, which is what makes the expected dates exact rather than "whatever today happens to be".
