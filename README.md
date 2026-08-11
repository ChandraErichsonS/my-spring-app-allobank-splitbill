# Split Bill API

Backend challenge solution for Allo Bank Engineering.

## Overview

This project provides a Spring Boot REST API for managing shared expenses and calculating optimized settlements between participants. Monetary calculations use `BigDecimal` throughout. The settlement engine calculates each participant's net balance, adds the personalized service charge, and minimizes the number of required transfers using a greedy creditor/debtor matching strategy.

## Tech Stack

- Java 21
- Spring Boot 3.5.4
- Maven
- Spring Web
- Spring Data JPA
- H2 database
- JUnit 5 / Mockito
- Docker multi-stage build

## GitHub

GitHub username: `ChandraErichsonS`

Personalized service charge calculation:

- Lowercase username: `chandraerichsons`
- Unicode/ASCII sum: `1695`
- `1695 % 10 = 5`

Therefore:

`service_charge_pct = 5%`

The application computes this value at runtime from `app.github-username`; it is not hardcoded.

## Build and Run

### Local

Requirements: Java 21 and Maven.

```bash
mvn clean test
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:4110
```

### Docker

```bash
docker build -t my-spring-app-allobank-splitbill .
docker run -p 4110:4110 my-spring-app-allobank-splitbill
```

No manual application startup step is required.

## API

### 1. Create a group

```bash
curl -X POST http://localhost:4110/api/groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bali Trip",
    "participants": [
      {"name": "Chandra"},
      {"name": "Andi"},
      {"name": "Budi"}
    ]
  }'
```

Example response:

```json
{
  "id": 1,
  "name": "Bali Trip",
  "participants": [
    {"id": 1, "name": "Chandra"},
    {"id": 2, "name": "Andi"},
    {"id": 3, "name": "Budi"}
  ]
}
```

### 2. Add an expense

The expense is split equally among the participants in `forParticipants`.

```bash
curl -X POST http://localhost:4110/api/groups/1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 300000,
    "paidBy": 1,
    "forParticipants": [1, 2, 3]
  }'
```

### 3. Get settlement

```bash
curl http://localhost:4110/api/groups/1/settlement
```

Example:

```json
{
  "groupId": 1,
  "groupName": "Bali Trip",
  "totalExpense": 300000.00,
  "serviceChargePct": 6,
  "serviceChargeAmount": 18000.00,
  "totalWithServiceCharge": 318000.00,
  "settlements": [
    {
      "from": "Andi",
      "to": "Chandra",
      "amount": 106000.00
    },
    {
      "from": "Budi",
      "to": "Chandra",
      "amount": 106000.00
    }
  ],
  "balances": [
    {"participant": "Chandra", "amount": 212000.00},
    {"participant": "Andi", "amount": -106000.00},
    {"participant": "Budi", "amount": -106000.00}
  ]
}
```

## Design Decisions

The hardest design decision was how to turn individual expense shares into a small number of final transfers while keeping monetary calculations exact. I chose a net-balance model followed by greedy creditor/debtor matching because it is simple, deterministic, and reduces unnecessary transactions without making the domain model overly complex. The trade-off is that the greedy algorithm optimizes the common settlement case but does not attempt an exhaustive search for the mathematically minimum number of transfers in every possible balance configuration. I also chose H2 for the self-contained challenge submission so the application can start without requiring a separate database service.

## Settlement Rules

1. Each expense is divided equally among its beneficiaries.
2. `BigDecimal` is used for all monetary values.
3. Division uses explicit rounding and any fractional-cent remainder is assigned deterministically to the first beneficiary.
4. The personalized service charge is calculated from the configured GitHub username.
5. The service charge is allocated equally across group participants, with any rounding remainder assigned to the first participant.
6. Positive balances are creditors; negative balances are debtors.
7. Creditor/debtor matching generates the final settlement transfers.

## Testing

The unit test covers:

- Total expense calculation
- Personalized service charge calculation
- Balance calculation
- Settlement generation
- Multiple debtor-to-creditor transfers

Run:

```bash
mvn test
```
