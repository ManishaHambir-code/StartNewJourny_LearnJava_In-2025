# Student Service

Owns the **Student** aggregate: registration, profile management and (from Phase 6) authentication.
Port **8081**, registers with Eureka as `STUDENT-SERVICE`, reachable through the gateway at
`http://localhost:8080/api/students/**`.

## Architecture

```
HTTP request
   |
   v
StudentController            @RestController  – validates (@Valid), maps HTTP <-> DTO, no business logic
   |
   v
StudentService (interface)   – contract, lets us swap/mocks in tests
StudentServiceImpl           @Service @Transactional – business rules (unique email, not-found), logging
   |                 \
   v                  v
StudentRepository    StudentMapper            – JpaRepository<Student, Long>  /  Entity <-> DTO
   |
   v
Student (@Entity)  ->  MySQL table `students`

GlobalExceptionHandler       @RestControllerAdvice – translates exceptions to ErrorResponse + HTTP status
```

```
com.slp.studentservice
├── StudentServiceApplication
├── config/OpenApiConfig                 # Swagger metadata
├── controller/StudentController         # /api/students
├── service/StudentService               # interface
├── service/impl/StudentServiceImpl      # implementation
├── repository/StudentRepository         # Spring Data JPA
├── entity/Student, StudentStatus        # JPA entity + enum
├── dto/StudentRequest                   # inbound payload (validated, contains password)
├── dto/StudentResponse                  # outbound payload (never contains password)
├── dto/PageResponse<T>                  # pagination wrapper
├── dto/ErrorResponse                    # error payload
├── mapper/StudentMapper                 # request -> entity, entity -> response
└── exception/                           # StudentNotFoundException, EmailAlreadyExistsException, GlobalExceptionHandler
```

Design rules applied: constructor injection only, JPA entity never leaves the service layer,
`@Transactional(readOnly = true)` at class level with `@Transactional` on mutating methods,
SLF4J logging with ids/emails only (passwords are excluded from `toString()` and never logged).

## API

Base path `/api/students` (Swagger UI: http://localhost:8081/swagger-ui.html, spec: `/v3/api-docs`).

| Method | Path | Success | Errors |
| --- | --- | --- | --- |
| `POST` | `/api/students` | `201 Created` + `Location` header | `400` validation, `409` duplicate email |
| `GET` | `/api/students?page=0&size=10&sort=firstName,asc` | `200 OK` (`PageResponse`) | `400` unknown sort property |
| `GET` | `/api/students/{id}` | `200 OK` | `404` not found |
| `PUT` | `/api/students/{id}` | `200 OK` | `400`, `404`, `409` (email used by another student) |
| `DELETE` | `/api/students/{id}` | `204 No Content` | `404` not found |

### Sample request (POST / PUT)

```json
{
  "firstName": "Manisha",
  "lastName": "Hambir",
  "email": "manisha@example.com",
  "phone": "+919876543210",
  "password": "Secret@123",
  "status": "ACTIVE"
}
```

### Sample response (201 / 200)

```json
{
  "id": 1,
  "firstName": "Manisha",
  "lastName": "Hambir",
  "email": "manisha@example.com",
  "phone": "+919876543210",
  "status": "ACTIVE",
  "createdAt": "2026-09-16T06:14:48.847836",
  "updatedAt": "2026-09-16T06:14:48.847836"
}
```

### Sample paginated response

```json
{
  "content": [ { "id": 2, "firstName": "Amit", "...": "..." }, { "id": 1, "firstName": "Manisha", "...": "..." } ],
  "page": 0,
  "size": 10,
  "totalElements": 2,
  "totalPages": 1,
  "last": true
}
```

### Sample error responses

`404`
```json
{ "timestamp": "2026-09-16T06:14:49.002", "status": 404, "error": "Not Found",
  "message": "Student not found with id: 999", "path": "/api/students/999" }
```

`409`
```json
{ "timestamp": "2026-09-16T06:14:48.912", "status": 409, "error": "Conflict",
  "message": "Email already exists: manisha@example.com", "path": "/api/students" }
```

`400`
```json
{ "timestamp": "2026-09-16T06:14:48.934", "status": 400, "error": "Bad Request",
  "message": "Validation failed", "path": "/api/students",
  "validationErrors": { "firstName": "First name is required", "email": "Email must be valid",
                        "password": "Password must be between 8 and 72 characters", "status": "Status is required" } }
```

## Database

Table `students` in database `slp_student_db` (created automatically, `ddl-auto: update` for dev).

| Column | Type | Constraints |
| --- | --- | --- |
| `id` | BIGINT | PK, auto-increment (`GenerationType.IDENTITY`) |
| `first_name` | VARCHAR(50) | NOT NULL |
| `last_name` | VARCHAR(50) | NOT NULL |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE (`idx_students_email`) |
| `phone` | VARCHAR(15) | nullable |
| `password` | VARCHAR(255) | NOT NULL (plain in Phase 2 – BCrypt hashing arrives with Spring Security in Phase 6) |
| `status` | VARCHAR(20) | NOT NULL, enum stored as string (`ACTIVE`, `INACTIVE`, `SUSPENDED`) |
| `created_at` | DATETIME(6) | NOT NULL, set in `@PrePersist`, not updatable |
| `updated_at` | DATETIME(6) | set in `@PrePersist` / `@PreUpdate` |

Configuration (`application.yml`) – all env-overridable:

```yaml
spring.datasource.url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:slp_student_db}?createDatabaseIfNotExist=true...
spring.datasource.username: ${MYSQL_USER:root}
spring.datasource.password: ${MYSQL_PASSWORD:root}
```

Tests use an in-memory H2 database (`src/test/resources/application.yml`) so no MySQL is needed for `mvn test`.

## CRUD flow (create)

1. `StudentController.createStudent` receives JSON -> `StudentRequest`; `@Valid` triggers Bean Validation.
   Failures raise `MethodArgumentNotValidException` -> `400` with field messages.
2. `StudentServiceImpl.createStudent` (write transaction):
   `existsByEmail` -> throw `EmailAlreadyExistsException` (`409`) if taken;
   `StudentMapper.toEntity` -> `studentRepository.save` -> `StudentMapper.toResponse`.
3. Controller returns `201 Created` with `Location: /api/students/{id}` and the `StudentResponse`.

Update follows the same shape, plus `existsByEmailAndIdNot(email, id)` so a student may keep their own
email but not take another student's. Delete and get use `findById(...).orElseThrow(StudentNotFoundException::new)`.

Pagination: the controller accepts Spring Data's `Pageable` (`page`, `size`, `sort=field,dir`;
default `size=10, sort=id,asc`). The repository executes `SELECT ... LIMIT/OFFSET` plus a `COUNT`
query; results are wrapped in `PageResponse` so the JSON contract does not depend on Spring's `Page`
serialization.

## Validation

| Field | Rules |
| --- | --- |
| `firstName`, `lastName` | `@NotBlank`, `@Size(min=2, max=50)` |
| `email` | `@NotBlank`, `@Email`, `@Size(max=100)`; uniqueness enforced in service + DB index |
| `phone` | optional, `@Pattern` 10–15 digits with optional `+` |
| `password` | `@NotBlank`, `@Size(min=8, max=72)`; write-only in Swagger, never returned |
| `status` | `@NotNull`, enum |

## Exception handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) maps:

| Exception | Status |
| --- | --- |
| `StudentNotFoundException` | 404 |
| `EmailAlreadyExistsException` | 409 |
| `MethodArgumentNotValidException` | 400 + `validationErrors` map |
| `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`, `PropertyReferenceException` | 400 (malformed JSON / non-numeric id / unknown sort field) |
| any other `Exception` | 500 with a generic message (details only in server log) |

Every error uses the same `ErrorResponse` shape (`timestamp`, `status`, `error`, `message`, `path`, optional `validationErrors`).

## Testing approach

| Test class | Type | What it covers |
| --- | --- | --- |
| `StudentServiceImplTest` | Unit – JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock StudentRepository`, `@Spy StudentMapper`, `@InjectMocks`) | create success, duplicate email, repository failure (`thenThrow`), get by id, not found, get all (page mapping, empty page), update, update not found, update duplicate email, delete, delete not found. Uses `when/thenReturn/thenThrow`, `verify`, `never()`, `verifyNoMoreInteractions`. |
| `StudentControllerTest` | Slice – `@WebMvcTest` + `@MockBean StudentService` | HTTP contract: 201 + Location, 400 validation/malformed JSON/non-numeric id, 404, 409, 204, 500 masking; pagination params captured with `ArgumentCaptor<Pageable>`. |
| `StudentApiIntegrationTest` | Integration – `@SpringBootTest` + `MockMvc` + H2 | full CRUD lifecycle, duplicate email, pagination & sorting across pages, invalid sort property, validation messages. |
| `StudentServiceApplicationTests` | Smoke | context loads. |

Run: `mvn test` (31 tests). Coverage report (JaCoCo): `target/site/jacoco/index.html` —
`StudentServiceImpl` is at **100 % line / 100 % branch** coverage (target was 80 %).

## Manual verification checklist (done for Phase 2)

- `mvn clean package` – compiles, 31 tests green.
- MySQL 8 in Docker; `actuator/health` shows `db: UP (MySQL)`; rows visible in `slp_student_db.students`.
- All CRUD calls return the documented status codes (201/200/204/400/404/409).
- Swagger UI `http://localhost:8081/swagger-ui.html` returns 200 and lists both paths.
- Eureka lists `STUDENT-SERVICE` as `UP`; `GET http://localhost:8080/api/students/1` works through the gateway.
- Application log contains no password values.

## Interview notes — Phase 2

**Why DTOs instead of exposing the entity?** Decouples API contract from persistence model, hides
sensitive fields (password), avoids lazy-loading / serialization surprises, and lets the entity evolve
without breaking clients.

**Why check `existsByEmail` in the service when there is a unique index?** Fail fast with a clear 409
instead of a `DataIntegrityViolationException`; the DB constraint remains the source of truth under
concurrent requests.

**Why `@Transactional(readOnly = true)` on the class?** Hibernate sets `FlushMode.MANUAL` and skips
dirty checking for reads (cheaper), and read replicas can be routed. Mutating methods override with
plain `@Transactional`.

**Why `saveAndFlush` in update?** With `IDENTITY` ids `save()` on a new entity issues the INSERT
immediately, but an update of a managed entity is only written at flush/commit. Flushing explicitly runs
`@PreUpdate` so the response carries the fresh `updatedAt`.

**`@WebMvcTest` vs `@SpringBootTest`?** `@WebMvcTest` loads only the MVC slice (controller, advice,
Jackson, validation) with a mocked service – fast, good for HTTP contract tests. `@SpringBootTest`
loads the full context – slower, used for true integration tests against H2.

**`@Mock` vs `@Spy`?** A mock has no behaviour until stubbed; a spy wraps a real object (here the mapper)
so real mapping runs while interactions can still be verified.
