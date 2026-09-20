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

#### Set or replace a customer loan limit

`PUT /api/v1/customers/{customerId}/loan-limit`

```json
{
   "limitAmount": 50000.00,
   "currency": "KES",
   "reason": "Updated after credit review"
}
```

#### View a customer loan limit

`GET /api/v1/customers/{customerId}/loan-limit`

The response includes the configured limit, available amount, currency, reason, effective date, and status.

### Loan Products

#### Create a loan product

`POST /api/v1/products`

```json
{
   "code": "SALARY-3M",
   "name": "Salary Loan 3 Months",
   "description": "Three month salary loan",
   "tenureValue": 3,
   "tenureUnit": "MONTHS",
   "structure": "INSTALLMENT",
   "billingMode": "INDIVIDUAL",
   "billingDay": null,
   "gracePeriodDays": 3,
   "installmentCount": 3,
   "writeOffAfterDays": 90,
   "fees": [
      {
         "feeType": "SERVICE",
         "calculationType": "PERCENTAGE",
         "value": 2.5,
         "applicationTiming": "ORIGINAL",
         "triggerDays": null
      },
      {
         "feeType": "LATE",
         "calculationType": "FIXED",
         "value": 200.00,
         "applicationTiming": "AFTER_DUE_DATE",
         "triggerDays": 5
      }
   ]
}
```

Important configuration rules:

- `tenureUnit` is `DAYS` or `MONTHS`.
- `structure` is `LUMP_SUM` or `INSTALLMENT`.
- Installment products require `installmentCount`; lump-sum products must omit it.
- `billingMode` is `INDIVIDUAL` or `CONSOLIDATED`.
- Consolidated billing requires `billingDay` from 1 to 28.
- Individual billing must omit `billingDay`.
- `writeOffAfterDays` is optional. When configured, it must be positive.
- Late fees must use `AFTER_DUE_DATE` and must specify `triggerDays`.

Supported fee types include service, daily, and late fees. Calculation types support fixed and percentage values.

#### View a product

`GET /api/v1/products/{productId}`

#### List products

`GET /api/v1/products`

#### Update a product

`PATCH /api/v1/products/{productId}`

#### Deactivate a product

`POST /api/v1/products/{productId}/deactivate`

Product terms are copied into loan terms when a loan is disbursed, so later product changes do not alter existing loans.

### Loans

#### Create a loan

`POST /api/v1/loans`

```json
{
   "customerId": "00000000-0000-0000-0000-000000000001",
   "productId": "00000000-0000-0000-0000-000000000002",
   "principal": 10000.00
}
```

The customer and product must be active, and the principal must fit within the customer available loan limit. New loans start in `CREATED` status.

#### View a loan

`GET /api/v1/loans/{loanId}`

#### Disburse a loan

`POST /api/v1/loans/{loanId}/disburse`

Disbursement snapshots product terms, calculates maturity, creates the repayment schedule, applies original fees, and transitions the loan to `OPEN`.

#### Cancel a loan

`POST /api/v1/loans/{loanId}/cancel`

Cancellation is allowed only while the loan is `CREATED` and transitions it to `CANCELLED`.

Loan statuses are `CREATED`, `OPEN`, `OVERDUE`, `CLOSED`, `CANCELLED`, and `WRITTEN_OFF`.

### Repayments

#### Create a repayment

`POST /api/v1/loans/{loanId}/repayments`

```json
{
   "amount": 4000.00,
   "reference": "PAY-0001",
   "channel": "MOBILE_MONEY"
}
```

Repayments:

- Reject duplicate references.
- Allocate fees before principal.
- Support partial payments.
- Reject overpayments.
- Close the loan when all outstanding fees and principal are paid.
- Serialize concurrent repayments for the same loan with a pessimistic database lock.

## Scheduled Processing

### Overdue and write-off sweep

The overdue scheduler runs according to `loan.overdue-sweep.cron` and:

1. Finds open or overdue loans with outstanding installments.
2. Marks installments overdue after the configured grace period.
3. Applies eligible late fees once per installment and trigger day.
4. Publishes an overdue notification event when a loan first becomes overdue.
5. Writes off an overdue loan when its configured write-off age is reached and it still has an outstanding balance.

Write-off is disabled when `writeOffAfterDays` is null. A fully repaid loan cannot be written off.

### Payment-due reminders

The payment reminder scheduler runs according to `loan.payment-due-reminder.cron` and publishes reminder events for installments due within the configured number of days.

## Notifications

Notifications are event-driven. Rules, templates, customer preferences, and channels are modeled in the notification module. Supported channel abstractions are email, SMS, and push.

For this assessment, I implemented concrete senders log delivery messages rather than integrating external providers to contain the scope within the requirements of the assessment. A `Strategy + Factory` pattern was used for channel selection and demonstrate adaptability.

Notification migrations seed initial configuration and a payment-due template. Provider delivery, durable delivery history, retries, and idempotency are intentionally outside the current scope.

## Database Migrations

Flyway migrations are in `src/main/resources/db/migration` and create the database autonomously. They cover:

- Customers and initial schema
- Loan products and product fees
- Loans and loan terms
- Repayment schedules and installments
- Repayments and repayment allocations
- Loan fee constraints
- Notification rules and templates
- Customer notification preferences
- Customer loan limits
- Write-off policy fields

## Design and Reliability Notes

- UUID identifiers support future distributed ownership without requiring shared numeric sequences.
- Product terms are snapshotted into loan terms at disbursement.
- Customer loan-limit validation is serialized per customer with a pessimistic write lock.
- Repayment processing is serialized per loan with a pessimistic write lock.
- Database uniqueness and foreign-key constraints provide a final integrity boundary.
- Domain state transitions reject invalid lifecycle operations.