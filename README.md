# Lending Application
This is a Java Lending App that is part of the Tezza Assessment

## Set-up
Prerequisites
- Java 21
- Docker
- Docker Compose

Running the application

1. Start PostgreSQL:

   docker compose up -d

2. Start the application:

   ./gradlew bootRun

Flyway automatically creates the database schema.