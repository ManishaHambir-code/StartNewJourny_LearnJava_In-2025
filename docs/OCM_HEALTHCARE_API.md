# OCM Healthcare Management API

Production-style Spring MVC backend inspired by Optum OCM workflows.

## Technology stack

| Technology | Version / role |
| --- | --- |
| Java | 8 |
| Spring Boot | 2.7.18 |
| REST / validation | Spring MVC, Bean Validation |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Security | Spring Security, JWT HS256, BCrypt |
| Documentation | OpenAPI / Swagger UI |
| Messaging | Kafka-ready domain event publisher |
| Build / deployment | Maven, multi-stage Docker |

## Architecture

Each domain is self-contained and can be extracted as a service:

```text
com.scp.java.ocm
├── common, config, messaging
├── security
├── member
├── provider
├── careplan
└── claim
```

Controllers validate and authorize requests, services enforce business rules and
transactions, mappers translate DTOs, and repositories isolate persistence.

## Run locally

Use Java 8 explicitly:

```bash
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 mvn -B clean verify
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 mvn spring-boot:run
```

The OCM entry point activates the `ocm` profile. The employee entry point is
still available with
`-Dspring-boot.run.main-class=com.scp.java.employee.EmployeeManagementApplication`.

## Run with Docker

```bash
docker run -d --name ocmmysql -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=ocm_db -p 3306:3306 mysql:8.0
docker compose up --no-build
```

Or use the complete compose stack:

```bash
docker compose up
OCM_KAFKA_ENABLED=true docker compose --profile kafka up
```

Kafka is disabled by default. Start the Kafka profile with:

```bash
OCM_KAFKA_ENABLED=true docker compose --profile kafka up
```

The compose configuration provides `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`.

## Authentication

Seeded development users are `admin`/`Admin@12345`,
`care.manager`/`Care@12345`, and `viewer`/`Viewer@12345`. Change these defaults
outside development. Login and use the returned bearer token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin@12345"}' | jq -r .accessToken)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/members
```

## Endpoints

All endpoints use `/api/v1`.

| Method | Path | Roles | Purpose |
| --- | --- | --- | --- |
| POST | `/auth/login` | Public | Issue JWT |
| POST | `/auth/register` | ADMIN | Register a user |
| GET | `/auth/me` | Authenticated | Current user |
| CRUD | `/members` | Read: all; write: ADMIN/CARE_MANAGER; delete: ADMIN | Member management |
| PATCH | `/members/{id}/status` | ADMIN/CARE_MANAGER | Member status transition |
| GET | `/members/{id}/care-plans` | Read: all | Member plans |
| GET | `/members/{id}/claims` | Read: all | Member claims |
| CRUD | `/providers` | Read: all; write: ADMIN/CARE_MANAGER; delete: ADMIN | Provider management |
| CRUD | `/care-plans` | Read: all; write: ADMIN/CARE_MANAGER | Care plans |
| POST | `/care-plans/{id}/activate` | ADMIN/CARE_MANAGER | Activate draft |
| POST | `/care-plans/{id}/complete` | ADMIN/CARE_MANAGER | Complete active plan |
| CRUD | `/claims` | Read: all; write: ADMIN/CARE_MANAGER; delete: ADMIN | Claims |
| POST | `/claims/{id}/adjudicate` | ADMIN/CARE_MANAGER | Approve or deny claim |

List endpoints support `page`, `size`, `sort=field,dir`, and their documented
domain filters. Sizes are 1–100 and default to 20.
Unknown sort fields return `400 Bad Request`.

Member status is changed only through `PATCH /members/{id}/status`. The
`status` property in `MemberRequest` is ignored on both create and update;
new members always start as `ACTIVE`, and updates preserve the current status.

## Validation and business rules

MRNs, NPIs, claim numbers and applicable emails are unique. Dates cannot be in
the future; NPIs are exactly 10 digits; diagnosis codes use an ICD-10-like
pattern. Members must be active for new care plans, active providers must manage
plans, and a member cannot be deleted while referenced. Terminated members are
terminal and cannot have active care plans when terminated. Care plans allow
only DRAFT→ACTIVE→COMPLETED workflow transitions and completed/cancelled plans
cannot be updated. Claims begin as SUBMITTED, can be adjudicated once, and can
be deleted only while submitted. Approved amounts must be positive and no more
than billed; denied claims require a reason and receive allowed amount zero.

## Errors

```json
{
  "timestamp": "2025-01-10T12:00:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Business rule message",
  "path": "/api/v1/claims/1/adjudicate"
}
```

Validation errors add a non-empty `fieldErrors` map. Status codes are:

| Status | Meaning |
| --- | --- |
| 400 | Malformed request, validation failure, or unknown sort field |
| 401 | Missing, invalid or expired JWT, or invalid login credentials |
| 403 | Authenticated user lacks required role |
| 404 | Resource not found |
| 409 | Duplicate resource |
| 422 | Business rule violation |
| 500 | Unexpected server error |

## Kafka-ready events

Member enrollment/status changes, care-plan creation/activation/completion and
claim submission/adjudication publish `DomainEvent` objects. Logging is the
default broker-free publisher. The Kafka publisher is selected only when
`ocm.kafka.enabled=true`, with aggregate-specific topics configured through
`ocm.kafka.topics.*`.

Swagger UI entry point: <http://localhost:8080/swagger-ui.html> (redirects with
`302` to the UI resource, which is reachable with `200`)
OpenAPI JSON: <http://localhost:8080/v3/api-docs>
