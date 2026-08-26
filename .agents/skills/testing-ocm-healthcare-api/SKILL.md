---
name: testing-ocm-healthcare-api
description: How to run and end-to-end test the OCM Healthcare API (Spring Boot 2.7 / Java 8 REST API, no frontend) — starting MySQL + the app, obtaining JWTs for each seeded role, and driving the members/providers/care-plans/claims CRUD and lifecycle flows over HTTP.
---

# Testing the OCM Healthcare API

Spring Boot 2.7.18 / Java 8 REST API under `com.scp.java.ocm`, profile `ocm`, no frontend.
All endpoints live under `http://localhost:8080/api/v1`. Docs: `docs/OCM_HEALTHCARE_API.md`.

## Bring up the environment

MySQL 8 in Docker (the app expects db `ocm_db` for the `ocm` profile):

```bash
docker run -d --name ocmmysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=ocm_db -p 3306:3306 mysql:8.0
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 mvn spring-boot:run
```

Note the repo blueprint's `startup` snippet creates a container named `testmysql` with
database `employee_db`; for OCM work create `ocm_db` instead (or override via
`MYSQL_HOST/MYSQL_PORT/MYSQL_DATABASE/MYSQL_USER/MYSQL_PASSWORD`).
Check for an already-running instance before starting a new one:
`docker ps | grep mysql` and `curl -s -o /dev/null -w '%{http_code}' localhost:8080/v3/api-docs`.

## Authentication

Seeded users (BCrypt, created on startup): `admin/Admin@12345` (ADMIN),
`care.manager/Care@12345` (CARE_MANAGER), `viewer/Viewer@12345` (VIEWER).

```bash
B=http://localhost:8080/api/v1
tok(){ curl -s -X POST $B/auth/login -H 'Content-Type: application/json' \
  -d "{\"username\":\"$1\",\"password\":\"$2\"}" \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["accessToken"])'; }
AT=$(tok admin 'Admin@12345'); CT=$(tok care.manager 'Care@12345'); VT=$(tok viewer 'Viewer@12345')
curl -s $B/members -H "Authorization: Bearer $AT"
```

## Swagger UI

`http://localhost:8080/swagger-ui.html` (302 → `/swagger-ui/index.html`) lists all controllers.
`OpenApiConfig` registers a global `bearerAuth` HTTP/JWT scheme, so authenticated "Try it out"
calls work: execute `POST /api/v1/auth/login`, copy `accessToken`, then paste it into
**Authorize** (token only, no `Bearer ` prefix). For an exhaustive matrix curl is still faster;
a maximized terminal works well for recorded visual evidence, since there is no frontend.

## Gotchas that waste time when writing test payloads

- Invalid enum values return `400 {"message":"Invalid value for request field"}` with a
  `fieldErrors` entry naming the bad value and listing the allowed constants.
  Valid `ProviderSpecialty`: PRIMARY_CARE, CARDIOLOGY, ONCOLOGY, BEHAVIORAL_HEALTH,
  ENDOCRINOLOGY, NEPHROLOGY, OTHER (there is **no** CARE_MANAGEMENT).
- `POST /auth/register` requires `username`, `password`, `fullName`, `role`. Bean validation runs
  **before** authorization, so an incomplete payload from a non-ADMIN returns 400, not 403 —
  send a *valid* payload when asserting the 403.
- `status` in member create/update payloads is ignored; new members are always ACTIVE.
  Claims are always created SUBMITTED and client `allowedAmount`/`denialReason` are ignored.
- Money fields come back as JSON numbers (`600.00` may parse to `600.0`): compare numerically,
  not as strings, or scripted assertions produce false failures.
- List endpoints take `page`, `size` (1..100), `sort=field,dir`. Unknown sort field or out-of-range
  size → `400` with `fieldErrors.sort` / `fieldErrors.size`.
- Error contract: 400 validation (+`fieldErrors`), 401 auth, 403 authorization, 404 not found,
  409 duplicate, **422** business-rule violations (all lifecycle/referential-integrity rules).
- Use a timestamp suffix for every unique field (`mrn`, `email`, `planId`, `npi`, `claimNumber`)
  **and** for filter values such as `lastName`: hardcoded filter values make
  `?lastName=X → totalElements==1` assertions fail on the second run against the same DB.
- `npi` must match `\d{10}` exactly.

## Business rules worth asserting

Care plans: created DRAFT; `activate` only from DRAFT, `complete` only from ACTIVE (else 422);
`complete` fills `endDate` if null; COMPLETED/CANCELLED plans cannot be updated (422);
a second DRAFT/ACTIVE plan for the same member+program → 409; `endDate < startDate` → 422;
require an ACTIVE member and an active care-manager provider.
Claims: adjudicate once only (422 afterwards); APPROVED needs `0 < allowedAmount <= billedAmount`;
DENIED needs a non-blank `denialReason` and zeroes `allowedAmount`; delete only while SUBMITTED.
Members/providers referenced by care plans or claims cannot be deleted (422); a member with an
ACTIVE plan cannot be TERMINATED (422); TERMINATED members cannot change status again (422).
Roles: VIEWER read-only (403 on writes), CARE_MANAGER can write/run lifecycle but not DELETE (403),
ADMIN full access.

## Devin Secrets Needed

None — all credentials are locally seeded test data.
