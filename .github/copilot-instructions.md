# Microservices Platform Guidelines

## Tech Stack Rules
- Language: Java 21 LTS
- Framework: Spring Boot 3.x with Web, JPA/Mongo, Actuator, Kafka
- Code Reduction: Use Lombok annotations (@Data, @Builder, @RequiredArgsConstructor)
- Database Patterns: Spring Data JPA (PostgreSQL) or Spring Data MongoDB
- Messaging: Spring for Apache Kafka using JSON serialization (Jackson)

## Architecture & Code Design Standards
- Follow Clean Architecture (Controller -> Service -> Repository -> Entity)
- Use Java 21 Records for DTOs and Kafka Event Payloads where applicable.
- Do NOT use cross-service DB calls or cross-service JPA Foreign Keys.
- Rest API Responses must follow standard HTTP status codes and RFC 7807 Problem Details for errors.
- Always implement health checks (/actuator/health) and log using SLF4J.

## Testing Standards
- Unit Tests: JUnit 5 + Mockito
- Integration Tests: Spring Boot Test with Testcontainers (PostgreSQL & Kafka)
