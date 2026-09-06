# simple-date-service

A minimal RESTful web service that returns the current date as JSON in ISO-8601 format.

Spring Boot 4.1 · Java 25 · Gradle · springdoc-openapi (Swagger)

## Endpoints

| Method | Path                | Description                                  |
|--------|---------------------|----------------------------------------------|
| GET    | `/api/v1/date`      | Current date as JSON                          |
| GET    | `/api/v1/openapi`   | OpenAPI 3.1 specification (JSON)              |
| GET    | `/api/v1/swagger-ui`| Swagger UI (redirects to `.../index.html`)    |

### `GET /api/v1/date`

```
HTTP/1.1 200
Content-Type: application/json

{"date":"2026-09-06"}
```

`date` is an ISO-8601 calendar date (`yyyy-MM-dd`) derived from the current UTC instant.

## Running

```bash
./gradlew bootRun
```

The service listens on port 8080. Try it:

```bash
curl http://localhost:8080/api/v1/date
```

## Tests and coverage

```bash
./gradlew build
```

`build` runs the test suite and enforces a minimum of 90% line coverage via JaCoCo.
The HTML report is written to `build/reports/jacoco/test/html/index.html`.

Test layers:

- `DateControllerUnitTest` — plain JUnit tests of the date logic against fixed clocks
  (leap day, year boundaries, UTC day rollover, ISO formatting).
- `DateControllerTest` — `@WebMvcTest` slice covering status, content type, and exact JSON body.
- `OpenApiDocumentationTest` — asserts the OpenAPI spec is served on its configured
  endpoint and documents `/api/v1/date`.
- `SimpleDateServiceApplicationTests` — context load.

## Notes on the date source

The controller reads `java.time.Clock`, supplied as a Spring bean (`ClockConfig`), rather than
calling `LocalDate.now()` directly. Production uses `Clock.systemUTC()`; tests substitute a fixed
clock, which is what makes the expected dates exact rather than "whatever today happens to be".
