# Lending Application

Java lending application developed for the Tezza interview case study. The application demonstrates configurable loan products, customer loan limits, loan creation and disbursement, repayment schedules, repayment allocation, overdue processing, write-off rules, and event-driven notifications.

## Technology Stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway database migrations

- JUnit 5 and Mockito

## Architecture

The application is structured as a modular monolith to showcase adaptability to microservices architecture. Each business capability has separate API, application, domain, and repository responsibilities where appropriate:

```text
com.lending.app
|-- customer           Customer profiles and loan limits
|-- product            Loan products, fees, and product configuration
|-- loan               Loan lifecycle, terms, fees, and overdue sweeps
|-- repayment_schedule Repayment schedules and installments
|-- repayment          Repayments and repayment allocations
|-- notification       Events, rules, templates, preferences, and channels

```