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

The application uses one PostgreSQL database. Flyway owns schema creation and Hibernate runs with `ddl-auto=validate`, so application startup validates the schema without mutating it.

## Prerequisites

- Java 21
- Docker Desktop

## Run Locally

Once the project is cloned start PostgreSQL from the repository root:

```bash
docker compose up -d
```

Start and run the application using VS Code (recommended) or on the default Spring Boot port:

```bash
# macOS/Linux
./gradlew bootRun

# Windows PowerShell
.\gradlew.bat bootRun
```

The API is available at `http://localhost:8080`.

Flyway runs automatically during application startup. PostgreSQL is exposed on host port `5433` with these development-only defaults:

```text
Database: lending_app
Username: lending_user
Password: lending_password
JDBC URL: jdbc:postgresql://localhost:5433/lending_app
```

Stop the database with:

```bash
docker compose down
```

To remove the local database volume and recreate the schema from scratch:

```bash
docker compose down -v
docker compose up -d
```
## Configuration

The following environment variables override the local defaults:

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5433/lending_app` | PostgreSQL JDBC URL |
| `DB_USER` | `lending_user` | Database username |
| `DB_PASSWORD` | `lending_password` | Database password |
| `loan.overdue-sweep.cron` | `0 */5 * * * *` | Overdue and write-off sweep schedule |
| `loan.payment-due-reminder.days-before` | `1` | Days before due date for reminders |
| `loan.payment-due-reminder.cron` | `0 0 8 * * *` | Payment reminder schedule |

## API Conventions

All endpoints are versioned under `/api/v1` and use JSON request/response bodies.

Successful responses:

- `201 Created`: customer, product, loan, and repayment creation.
- `200 OK`: reads, updates, loan-limit changes, product deactivation, disbursement, and cancellation.

Error responses:

- `400 Bad Request`: validation failure, malformed JSON, invalid parameter, or invalid input.
- `404 Not Found`: requested resource does not exist.
- `409 Conflict`: business-rule conflict, duplicate data, or database constraint conflict.

Error JSON uses this contract:

```json
{
   "code": "VALIDATION_FAILED",
   "message": "Request validation failed",
   "fieldErrors": {
      "principal": "must be greater than 0"
   }
}
```

`fieldErrors` is an empty object when the error is not associated with a specific request field.

## API Reference

### Customers

#### Create a customer

`POST /api/v1/customers`

```json
{
   "firstName": "Maimuna",
   "lastName": "Maksuudi",
   "email": "maimuna@example.com",
   "phoneNumber": "254717000002",
   "segment": "RETAIL"
}
```

`segment` values include `RETAIL`, `SALARIED` and `BUSINESS`.

#### View a customer

`GET /api/v1/customers/{customerId}`

#### List customers

`GET /api/v1/customers`

#### Update customer details

`PATCH /api/v1/customers/{customerId}`

All fields are optional, but supplied values are validated:

```json
{
   "firstName": "Maimuna",
   "phoneNumber": "254700000001",
   "segment": "BUSINESS"
}
```
