# Student Learning Platform

A production-style, microservices-based learning platform built as a hands-on Java Backend
interview-preparation project.

Students can register, log in, manage their profile, browse courses, enroll, track progress
and complete courses. Admins manage students, courses and enrollments.

> Built **phase by phase**. Each phase compiles, its tests pass, and this README is updated
> before the next phase begins. See [Phase status](#phase-status).

---

## Architecture

```
                         Client
                           |
                      API Gateway  (:8080, Spring Cloud Gateway)
                           |
             +-------------+-------------+
             |                           |
     Student Service (:8081)     Course Service (:8082)
             |                           |
             +-------------+-------------+
                           |  OpenFeign (sync)
                  Enrollment Service (:8083)
                           |
                         Kafka   (topic: student-enrollment-events)
                           |
                 Notification Service (:8084)

      Service Registry (:8761, Eureka) - every service registers here
```

| Service | Port | Responsibility |
| --- | --- | --- |
| `service-registry` | 8761 | Eureka server – service discovery |
| `api-gateway` | 8080 | Single entry point; routes `/api/**` to services via Eureka (`lb://`) |
| `student-service` | 8081 | Student CRUD, auth (JWT) – MySQL `slp_student_db` |
| `course-service` | 8082 | Course CRUD, Redis caching – MySQL `slp_course_db` |
| `enrollment-service` | 8083 | Enrollments; calls student/course via OpenFeign; Kafka producer – MySQL `slp_enrollment_db` |
| `notification-service` | 8084 | Kafka consumer; simulates notifications |

## Technology stack

| Area | Choice |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot **3.3.5**, Spring Cloud **2023.0.3** |
| Web | Spring Web (MVC) for services, Spring Cloud Gateway (WebFlux) for the gateway |
| Persistence | Spring Data JPA / Hibernate, MySQL 8 (H2 in tests) |
| Discovery | Netflix Eureka |
| Inter-service | OpenFeign (sync), Apache Kafka (async) |
| Resilience | Resilience4j |
| Cache | Redis |
| Security | Spring Security + JWT (jjwt 0.12) |
| Docs | springdoc-openapi 2.6 (Swagger UI) |
| Ops | Spring Boot Actuator, SLF4J |
| Tests | JUnit 5, Mockito, Spring Boot Test |
| Build / Run | Maven multi-module, Lombok, Docker & Docker Compose |

## Project layout

```
student-learning-platform/
├── pom.xml                    # parent: Spring Boot BOM, Spring Cloud BOM, shared deps, Java 17
├── service-registry/
├── api-gateway/
├── student-service/
├── course-service/
├── enrollment-service/
└── notification-service/
    └── src/main/java/com/slp/<service>/<Service>Application.java
    └── src/main/resources/application.yml
    └── src/test/java/...                    # context-load smoke test
    └── src/test/resources/application.yml   # H2 + Eureka disabled for tests
```

Package root: `com.slp.<servicename>` (e.g. `com.slp.studentservice`).

## Build & run

Prerequisites: JDK 17, Maven 3.6+.

```bash
cd student-learning-platform

# compile everything
mvn clean compile

# run all tests
mvn test

# build executable jars
mvn clean package
```

Run services (each in its own terminal, in this order):

```bash
java -jar service-registry/target/service-registry-1.0.0-SNAPSHOT.jar
java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar
java -jar student-service/target/student-service-1.0.0-SNAPSHOT.jar        # needs MySQL
java -jar course-service/target/course-service-1.0.0-SNAPSHOT.jar          # needs MySQL
java -jar enrollment-service/target/enrollment-service-1.0.0-SNAPSHOT.jar  # needs MySQL + Kafka
java -jar notification-service/target/notification-service-1.0.0-SNAPSHOT.jar  # needs Kafka
```

Quick local MySQL:

```bash
docker run -d --name slp-mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:8.0
```

Each service creates its own database (`createDatabaseIfNotExist=true`). Docker Compose for the full
stack arrives in Phase 13.

### Configuration (no hard-coded credentials)

All infrastructure settings are environment-overridable with sensible local defaults:

| Variable | Default | Used by |
| --- | --- | --- |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | all clients |
| `MYSQL_HOST` / `MYSQL_PORT` | `localhost` / `3306` | student, course, enrollment |
| `MYSQL_DATABASE` | `slp_<service>_db` | student, course, enrollment |
| `MYSQL_USER` / `MYSQL_PASSWORD` | `root` / `root` | student, course, enrollment |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | enrollment, notification |

### Useful URLs

| URL | What |
| --- | --- |
| http://localhost:8761 | Eureka dashboard |
| http://localhost:8080/actuator/health | Gateway health |
| http://localhost:808x/actuator/health | Service health |
| http://localhost:808x/swagger-ui.html | Swagger UI (business services) |

Gateway routes:

| Path | Target |
| --- | --- |
| `/api/students/**`, `/api/auth/**` | `lb://student-service` |
| `/api/courses/**` | `lb://course-service` |
| `/api/enrollments/**` | `lb://enrollment-service` |

---

## Phase status

| # | Phase | Status |
| --- | --- | --- |
| 1 | Project setup (multi-module Maven, Spring Boot 3, Spring Cloud, Eureka, Gateway) | Done |
| 2 | Student service CRUD | Pending |
| 3 | Course service CRUD | Pending |
| 4 | Enrollment service (OpenFeign) | Pending |
| 5 | Kafka events + notification consumer | Pending |
| 6 | Spring Security + JWT | Pending |
| 7 | Resilience4j | Pending |
| 8 | Redis caching | Pending |
| 9 | JPA/Hibernate deep-dive | Pending |
| 10 | Testing (JUnit 5 / Mockito / integration) | Pending |
| 11 | Swagger / OpenAPI | Pending |
| 12 | Actuator + logging | Pending |
| 13 | Docker / Docker Compose | Pending |
| 14 | AWS deployment architecture | Pending |
| 15 | System design docs + interview material | Pending |

---

## Phase 1 — What was implemented

1. **Maven multi-module project** – a parent `pom.xml` that
   * inherits `spring-boot-starter-parent:3.3.5` (dependency + plugin management),
   * imports the `spring-cloud-dependencies:2023.0.3` BOM (compatible with Boot 3.3.x),
   * pins Java 17, springdoc 2.6.0 and jjwt 0.12.6,
   * declares dependencies every service shares: Actuator, Lombok, Spring Boot Test.
2. **Six services** – each with its own `pom.xml`, `@SpringBootApplication` class,
   `application.yml` and a `contextLoads` smoke test:
   * `service-registry` – `@EnableEurekaServer`, does not register itself.
   * `api-gateway` – Spring Cloud Gateway with Eureka discovery-locator plus explicit `lb://` routes.
   * `student-service`, `course-service` – Web, Validation, JPA, MySQL, Eureka client, OpenAPI.
   * `enrollment-service` – as above plus OpenFeign (`@EnableFeignClients`) and Spring Kafka.
   * `notification-service` – Web, Eureka client, Spring Kafka.
3. **Test isolation** – `src/test/resources/application.yml` switches JPA services to in-memory H2
   and disables the Eureka client, so `mvn test` needs no MySQL/Kafka/Eureka running.
4. **Verified**: `mvn clean compile` and `mvn test` pass for all modules; `service-registry` and
   `api-gateway` were started from their jars, both report `{"status":"UP"}`, and the gateway
   appears as `API-GATEWAY` in the Eureka registry.

### Interview notes — Phase 1

**Why a Maven parent/BOM?** Version alignment. `spring-boot-starter-parent` + the Spring Cloud BOM
guarantee that every starter, Jackson, Hibernate, Netty etc. are mutually compatible, so child
modules declare dependencies *without versions*.

**Why does the gateway use WebFlux while services use Spring MVC?** Spring Cloud Gateway is built on
Project Reactor / Netty to proxy thousands of concurrent connections with few threads. It *cannot*
coexist with `spring-boot-starter-web` (servlet) on the same classpath.

**Why Eureka?** Service instances scale up/down and change IPs. Clients look up a logical name
(`lb://student-service`) instead of a hard-coded host; the Eureka client caches the registry locally
and load-balances across instances.

**Why Spring Boot 3 / Java 17?** Boot 3 requires Java 17 and moves to Jakarta EE 9+ (`jakarta.*`
packages), Spring Framework 6, native observability (Micrometer) and GraalVM native-image support.

**What does `@SpringBootApplication` do?** It combines `@Configuration`, `@EnableAutoConfiguration`
and `@ComponentScan`. Auto-configuration inspects the classpath and conditionally registers beans
(`@ConditionalOnClass`, `@ConditionalOnMissingBean`), which is why adding a starter is often enough.
