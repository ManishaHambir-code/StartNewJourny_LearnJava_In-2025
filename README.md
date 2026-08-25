# Employee Management REST API

Spring Boot REST API for managing employees, built with a clean layered architecture
(controller → service → repository → entity) on top of Spring Data JPA / Hibernate and MySQL.

The repository also keeps the original Java learning examples under `src/main/java/com/scp/java`
(exception handling, Java 8 concepts, etc.); the API lives in `com.scp.java.employee`.

## Tech stack

| Item | Version |
| --- | --- |
| Java | 8 (`maven.compiler.source/target = 1.8`) |
| Spring Boot | 2.7.18 |
| Spring Data JPA / Hibernate | managed by Spring Boot |
| MySQL | 8.x (driver `com.mysql:mysql-connector-j`) |
| Build | Maven |
| Tests | JUnit 5, Mockito, Spring MockMvc |

## Project layout

```
src/main/java/com/scp/java/employee
├── EmployeeManagementApplication.java   # Spring Boot entry point
├── controller/EmployeeController.java   # REST layer (/api/employees)
├── service/EmployeeService.java         # Service contract
├── service/impl/EmployeeServiceImpl.java# Business logic + transactions
├── repository/EmployeeRepository.java   # Spring Data JPA repository
├── entity/Employee.java                 # JPA entity mapped to `employees`
├── dto/EmployeeDto.java                 # Request/response payload + validation
├── dto/ErrorResponse.java               # Standard error payload
├── mapper/EmployeeMapper.java           # Entity <-> DTO mapping
└── exception/                           # Custom exceptions + @RestControllerAdvice
```

## MySQL configuration

Settings live in `src/main/resources/application.properties` and can be overridden with
environment variables:

| Variable | Default |
| --- | --- |
| `MYSQL_HOST` | `localhost` |
| `MYSQL_PORT` | `3306` |
| `MYSQL_DATABASE` | `employee_db` |
| `MYSQL_USER` | `root` |
| `MYSQL_PASSWORD` | `root` |
| `SERVER_PORT` | `8080` |

The JDBC URL uses `createDatabaseIfNotExist=true` and `spring.jpa.hibernate.ddl-auto=update`,
so the schema is created on first start. To create the database manually:

```sql
CREATE DATABASE employee_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## Build and run

```bash
mvn clean package                # compile + run tests + build the jar
mvn spring-boot:run              # run the API on http://localhost:8080
java -jar target/scientecheasy_ExceptionHandling-0.0.1-SNAPSHOT.jar
mvn test                         # tests only
```

Java 8 is required for the build; e.g. `JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 mvn clean package`.

## API

Base path: `/api/employees`

| Method | Path | Success | Description |
| --- | --- | --- | --- |
| POST | `/api/employees` | 201 + `Location` | Create an employee |
| GET | `/api/employees` | 200 | List all employees |
| GET | `/api/employees/{id}` | 200 | Get one employee |
| PUT | `/api/employees/{id}` | 200 | Update an employee |
| DELETE | `/api/employees/{id}` | 204 | Delete an employee |

### Request / response body

```json
{
  "id": 1,
  "firstName": "Manisha",
  "lastName": "Hambir",
  "email": "manisha@example.com",
  "department": "Engineering",
  "salary": 75000.00,
  "dateOfJoining": "2025-01-10"
}
```

### Validation rules

- `firstName`, `lastName`: required, max 50 characters
- `email`: required, valid email, max 120 characters, unique across employees
- `department`: required, max 60 characters
- `salary`: required, greater than 0
- `dateOfJoining`: required, not in the future (ISO `yyyy-MM-dd`)

### Error handling

All errors are produced by `GlobalExceptionHandler` in a single shape:

```json
{
  "timestamp": "2025-01-10T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for request payload",
  "path": "/api/employees",
  "fieldErrors": { "email": "email must be a valid address" }
}
```

| Status | When |
| --- | --- |
| 400 | Bean validation failure (details in `fieldErrors`), malformed/missing JSON body, unparseable path variable |
| 404 | `EmployeeNotFoundException` — unknown id |
| 405 | HTTP method not supported for the path |
| 409 | `DuplicateEmailException` — email already used |
| 500 | Any other unhandled exception (generic message; stack trace is logged server-side only) |

### curl examples

```bash
curl -X POST http://localhost:8080/api/employees \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Manisha","lastName":"Hambir","email":"manisha@example.com","department":"Engineering","salary":75000.00,"dateOfJoining":"2025-01-10"}'

curl http://localhost:8080/api/employees
curl http://localhost:8080/api/employees/1

curl -X PUT http://localhost:8080/api/employees/1 \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Manisha","lastName":"Hambir","email":"manisha.h@example.com","department":"Platform","salary":82000.00,"dateOfJoining":"2025-01-10"}'

curl -X DELETE http://localhost:8080/api/employees/1
```

## Tests

- `EmployeeServiceImplTest` — service logic with mocked repository (CRUD, not-found, duplicate email)
- `EmployeeControllerTest` — `@WebMvcTest` slice covering status codes, JSON payloads and error responses

No database is needed to run the tests.
