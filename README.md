# Billing API

Multi-tenant invoicing and subscription billing REST API built with Spring Boot 3 and Java 21.

> Status: in progress. Day 1 skeleton.

## Problem it solves
Small businesses need to invoice customers, run recurring subscriptions, and get paid online
without stitching together spreadsheets and payment dashboards.

## Stack
Java 21, Spring Boot 3, Spring Security (JWT), PostgreSQL, Flyway, Docker, OpenAPI/Swagger.

## Run locally
```bash
docker compose up -d
./gradlew bootRun      # first run: gradle wrapper (see below)
```
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- Ping: http://localhost:8080/api/v1/ping

## Roadmap
- [x] Project skeleton
- [ ] Data model and migrations
- [ ] JWT auth and roles
- [ ] Customers, plans, invoices
- [ ] Recurring billing
- [ ] Stripe test-mode webhook
- [ ] Async notifications
- [ ] Integration tests and CI

## Gradle wrapper
If `gradlew` is missing, run `gradle wrapper` once (needs Gradle installed), or generate the project at start.spring.io and copy the `gradle/` folder and `gradlew` files in.
