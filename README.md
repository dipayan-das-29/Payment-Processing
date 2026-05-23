# Payment Processing Portfolio Setup Guide

This document explains how to set up the payment-processing portfolio project on a new machine, including prerequisites, local infrastructure, application configuration, build steps, runtime sequence, verification steps, and common troubleshooting notes. The project currently depends on PostgreSQL, Redis, and Kafka for the payment flow, while JWT authentication is handled by `auth-service` and enforced at the gateway. [file:673][web:1238][web:1133]

## Project Overview

The project is a multi-module Spring Boot system with `auth-service`, `api-gateway`, `payment-service`, additional downstream services, and shared libraries. The working request path for the core demo is: login through `auth-service`, route through `api-gateway`, then create or query payments through `payment-service`. [file:673]

For local execution, `payment-service` requires PostgreSQL for persistence, Redis for idempotency key storage, and Kafka for publishing payment events. The idempotency implementation stores keys in Redis using the pattern `idem:<key>` with a 10-minute TTL. [file:673][web:1145][web:1132]

## Prerequisites

Install the following software before cloning or running the project. Spring Boot 3.3.2 works with Gradle 8.8, and the project is configured to use the Gradle wrapper rather than a system Gradle installation. [web:917][file:673]

- Java 17 or a compatible JDK configured in `JAVA_HOME`. [web:919]
- Git.
- PostgreSQL running locally on port `5432`.
- Redis running locally on port `6379`.
- Kafka running locally on port `9092`. Kafka must be reachable by `payment-service` or payment creation will fail when publishing events. [web:1228][web:1238]
- A terminal such as PowerShell or Command Prompt on Windows.

## Clone And Open

Clone the repository or copy the project source tree onto the new machine. Then open the root folder of the multi-module project in IntelliJ IDEA or another Java IDE that supports Gradle. [file:673]

Use the project’s Gradle wrapper, not a globally installed Gradle version. The wrapper is configured for Gradle 8.8, which is compatible with the current Spring Boot setup. [web:917][file:673]

## Infrastructure Setup

### PostgreSQL

Create a local PostgreSQL database named `db_payments_processing_portfolio` and ensure the default credentials in `payment-service` match the local instance: username `postgres`, password `postgres`. The current payment-service configuration points to `jdbc:postgresql://localhost:5432/db_payments_processing_portfolio`. [file:673]

Example SQL:

```sql
CREATE DATABASE db_payments_processing_portfolio;
```

### Redis

Start Redis locally on port `6379`. The payment service uses Redis through Spring Data Redis and `StringRedisTemplate` for idempotency key tracking. [web:1133][file:673]

Once the service is running, a successful first-time payment request stores keys like `idem:idem-1002` with value `IN_PROGRESS` and a TTL of 10 minutes. [file:673][web:1132]

### Kafka

Start Kafka locally on port `9092`. The payment flow publishes `PAYMENT_INITIATED`, `PAYMENT_COMPLETED`, and `PAYMENT_FAILED` events using `KafkaTemplate`, and if Kafka is unavailable the producer logs connection warnings such as “Connection to node -1 (localhost/127.0.0.1:9092) could not be established.” [file:673][web:1228]

Kafka must be configured so the broker is reachable from the local machine at `localhost:9092`. If Kafka is containerized, the listener and advertised listener settings must still expose a usable `localhost:9092` endpoint to the host system. [web:1240][web:1243]

## Application Configuration

### auth-service

`auth-service` runs on port `8085`, imports shared JWT beans from `CommonSecurityBeansConfig`, and owns its own servlet security configuration. It exposes `/auth/login` and actuator health when properly configured. [file:673]

### api-gateway

`api-gateway` runs on port `8080`, uses reactive Spring Security, disables HTTP Basic and form login, validates bearer tokens, and forwards trusted headers such as `X-User-Id`, `X-Username`, `X-Roles`, and `X-Correlation-Id` downstream. [file:673]

### payment-service

`payment-service` runs on port `8081`, uses PostgreSQL, Redis, and Kafka, and currently processes a payment as follows: idempotency check in Redis, payment persistence, initiated-event publish to Kafka, bank stub charge, final status update, final event publish, and response generation. The current local bank adapter is a stub that approves payments under `100000` and declines higher amounts. [file:673]

## Build Steps

From the project root, run:

```powershell
.\gradlew.bat clean build
```

The project should be built with the Gradle wrapper. During troubleshooting, several issues were resolved by ensuring module-specific dependencies were declared correctly, avoiding servlet dependency leakage into the reactive gateway, and removing Docker/Testcontainers-based requirements from local test execution. [file:673]

If the build fails, check:

- `auth-service` includes `spring-boot-starter-security`. [file:673]
- `api-gateway` includes actuator if `/actuator/health` is expected. [file:673]
- `payment-service` includes actuator if `/actuator/health` is expected. [file:673]
- payment-service tests are aligned with the current `PaymentService` and `PaymentResponse` APIs. [file:673]
- the root Gradle configuration includes `mavenCentral()` if dependency resolution fails. [file:673]

## Run Sequence

Start the services in this order so dependencies are available before the payment flow is exercised:

1. PostgreSQL
2. Redis
3. Kafka
4. `auth-service`
5. `payment-service`
6. `api-gateway` [file:673][web:1238]

Run each service from the project root:

```powershell
.\gradlew.bat :auth-service:bootRun
```

```powershell
.\gradlew.bat :payment-service:bootRun
```

```powershell
.\gradlew.bat :api-gateway:bootRun
```

## Health Verification

Check that each service is up using actuator health. Spring Boot exposes health at `/actuator/health` when actuator is on the classpath and the endpoint is exposed. [web:1010][web:1011]

```powershell
curl http://localhost:8085/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

Expected result:

```json
{"status":"UP"}
```

## Authentication Flow

Get a JWT from `auth-service`:

```powershell
curl -i -X POST http://localhost:8085/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"demo\",\"password\":\"demo123\"}"
```

Use the returned token as a bearer token for gateway calls. Bearer tokens must be sent in the `Authorization` header in the format `Authorization: Bearer <token>`. [web:755][web:1037]

## Payment Verification Flow

If the database has no seed data, verify the system by creating a payment first, then fetching it back. Payment creation should be sent as JSON with `Content-Type: application/json`; otherwise Spring may reject it with `415 Unsupported Media Type`. [web:1066][web:1065]

### Create Payment

```powershell
curl -i -X POST http://localhost:8080/v1/payments ^
  -H "Authorization: Bearer YOUR_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":\"user-demo-1\",\"merchantId\":\"m-101\",\"orderId\":\"ord-1002\",\"amount\":499.99,\"currency\":\"INR\",\"paymentMethod\":\"CARD\",\"idempotencyKey\":\"idem-1002\",\"correlationId\":\"corr-1002\"}"
```

### Fetch Payments For User

```powershell
curl -i "http://localhost:8080/v1/payments?userId=user-demo-1" ^
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Idempotency Behavior

The payment service currently uses Redis-based idempotency. On first submission, `IdempotencyService` writes `idem:<idempotencyKey>` with value `IN_PROGRESS` and a 10-minute TTL using `setIfAbsent(...)`. If the same key is reused before expiry, the service throws `IllegalStateException: Duplicate request detected for idempotency key: ...`. [file:673][web:1145][web:1132]

This means:

- A new payment attempt requires a fresh idempotency key. [web:1127]
- Reusing the same key is treated as the same request and currently results in duplicate rejection rather than replaying the saved response. [web:1191][file:673]
- The current implementation stores only the marker `IN_PROGRESS`, not the final response payload. [file:673]

To inspect the Redis entry:

```bash
redis-cli GET idem:idem-1002
redis-cli TTL idem:idem-1002
```

## Current Payment-Service Runtime Path

The current local implementation processes a payment in this order:

1. Gateway forwards authenticated request to `payment-service`. [file:673]
2. `IdempotencyService` checks or creates the Redis marker. [file:673]
3. `PaymentService` creates a `Payment`, marks it processing, and saves it to PostgreSQL. [file:673]
4. `KafkaPaymentPublisher.publishInitiated(...)` sends an initiated event to Kafka. [file:673]
5. `StubBankGatewayAdapter` authorizes the payment unless the amount is greater than `100000`. [file:673]
6. The payment is marked `COMPLETED` or `FAILED`, saved again, and the corresponding completion/failure event is published. [file:673]
7. The API returns `PaymentResponse.from(payment)`. [file:673]

## Common Problems

### 415 Unsupported Media Type

Cause: the POST body was sent without `Content-Type: application/json`, so Spring interpreted it as form data. [web:1066]

Fix:

```powershell
-H "Content-Type: application/json"
```

### 500 Internal Server Error On Payment Creation

Cause: unhandled runtime exception inside `payment-service`. In the verified local troubleshooting path, one root cause was duplicate idempotency key reuse, which caused `IllegalStateException`. Another root cause was Kafka being unavailable on `localhost:9092`. [file:673][web:1228]

Fixes:

- use a fresh idempotency key for a fresh payment request, [web:1127]
- ensure Kafka is running and reachable on `localhost:9092`. [web:1238][web:1228]

### Kafka Connection Errors

Symptoms include log lines such as:

```text
Connection to node -1 (localhost/127.0.0.1:9092) could not be established.
Bootstrap broker localhost:9092 disconnected.
```

These indicate that the local Kafka broker is unavailable or misconfigured for the advertised `localhost:9092` endpoint. Kafka producer connection issues commonly occur when the broker is down or listeners/advertised listeners do not match the client’s network path. [web:1228][web:1240][web:1243]

### Actuator Health Returns 404

Cause: the service does not include `spring-boot-starter-actuator` or actuator endpoints are not exposed in configuration. `/actuator/health` exists only when actuator is on the classpath and exposed. [web:1023][web:1011]

Fix:

- add `implementation 'org.springframework.boot:spring-boot-starter-actuator'`, [web:1030]
- expose health/info in `application.yml`. [web:1025]

## Recommended Local Demo Sequence

For a fresh setup demonstration on a new system, use this order:

1. Start PostgreSQL, Redis, and Kafka. [file:673]
2. Start `auth-service`, `payment-service`, and `api-gateway`. [file:673]
3. Verify all three health endpoints. [web:1010]
4. Call `/auth/login` and capture the JWT. [file:673]
5. Create a payment with a brand-new idempotency key. [web:1127]
6. Fetch payments for the user to verify persistence and routing. [file:673]
7. Optionally inspect Redis to observe the idempotency marker. [web:1132]

## Local Configuration

This project uses environment-variable based configuration for all services.

Create a root `.env` file in the project root and keep it out of Git. Commit only `.env.example`.

Example:

```env
GATEWAY_SERVER_PORT=8080
AUTH_SERVER_PORT=8085
PAYMENT_SERVER_PORT=8081
LEDGER_SERVER_PORT=8082
FRAUD_SERVER_PORT=8083
NOTIFICATION_SERVER_PORT=8084
```

## Running on Windows without Docker

A `.env` file is not executed directly by Windows or Spring Boot by default. The easiest local approach is to use a `.bat` launcher that reads `.env`, sets the variables for the current process, and starts the target service. [cite:1621][cite:1627]

Example `run-payment-service.bat`:

```bat
@echo off
setlocal

for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    if not "%%A"=="" (
        if not "%%A:~0,1%"=="#" (
            set "%%A=%%B"
        )
    )
)

call gradlew.bat :payment-service:bootRun

endlocal
```

Run it with:

```bat
run-payment-service.bat
```

You can create similar launchers for the other services:

```bat
run-api-gateway.bat
run-auth-service.bat
run-ledger-service.bat
run-fraud-detection-service.bat
run-notification-service.bat
```

## Alternative: import `.env` directly in Spring Boot

If preferred, Spring Boot can import a root `.env` file using:

```properties
spring.config.import=optional:file:.env[.properties]
```

In that case, the `.env` file must be compatible with Spring property loading. [cite:1568][cite:1626]

## Running with Gradle Wrapper

Use the Gradle wrapper on Windows:

```bat
gradlew.bat :payment-service:bootRun
gradlew.bat :api-gateway:bootRun
gradlew.bat :auth-service:bootRun
```

The Gradle wrapper is the recommended way to run the project because it avoids requiring a separate local Gradle installation. [cite:1627]

## Notes For Improvement

The current implementation is functional for a local portfolio demo, but it is not yet a fully polished production-grade payment flow. A stronger next iteration would:

- store final idempotency results, not just `IN_PROGRESS`, so retries can return the original response, [web:1191][web:1185]
- map duplicate-idempotency cases to a clearer API response such as a replayed response or 409 Conflict instead of generic 500 behavior, [web:1114][web:1191]
- optionally make Kafka publishing configurable for local non-event-driven demos if infrastructure-light startup is desired. [file:673]
