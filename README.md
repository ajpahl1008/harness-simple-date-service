# simple-date-service

A minimal RESTful web service that returns the current date and time as JSON in ISO-8601 format.

Spring Boot 4.1 · Java 25 · Gradle · springdoc-openapi (Swagger)

## Endpoints

| Method | Path                | Description                                  |
|--------|---------------------|----------------------------------------------|
| GET    | `/api/v1/date`      | Current date and time as JSON                 |
| GET    | `/api/v1/openapi`   | OpenAPI 3.1 specification (JSON)              |
| GET    | `/api/v1/swagger-ui`| Swagger UI (redirects to `.../index.html`)    |

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
  (leap day, year boundaries, UTC day rollover, millisecond padding and truncation, round-trip).
- `DateResponseSerializationTest` — what Jackson actually emits at the format edges: `.000` on a
  whole second, `.100` rather than `.1`, and zero padding on every field.
- `DateControllerTest` — `@WebMvcTest` slice covering status, content type, and exact JSON body.
- `OpenApiDocumentationTest` — asserts the OpenAPI spec is served on its configured
  endpoint and documents `/api/v1/date`.
- `SimpleDateServiceApplicationTests` — context load.

## Notes on the date source

The controller reads `java.time.Clock`, supplied as a Spring bean (`ClockConfig`), rather than
calling `LocalDate.now()` directly. Production uses `Clock.systemUTC()`; tests substitute a fixed
clock, which is what makes the expected timestamps exact rather than "whatever now happens to be".
