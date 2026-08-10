# Running in Apache NetBeans

## Requirements
- Apache NetBeans 20+ recommended
- JDK 21
- Maven 3.9+ (or NetBeans bundled Maven)
- No external database is required

## Open
1. Extract the ZIP.
2. NetBeans: File -> Open Project.
3. Select `my-spring-app-allobank-splitbill`.
4. NetBeans detects `pom.xml` as a Maven project.
5. Select JDK 21 under Tools -> Java Platforms.

## Test
Right-click project -> Test

Or:
```bash
mvn clean test
```

## Run
Right-click project -> Run

Or:
```bash
mvn spring-boot:run
```

API:
`http://localhost:4110`

## Test API

Create group:
```bash
curl -X POST http://localhost:4110/api/groups -H "Content-Type: application/json" -d "{\"name\":\"Bali Trip\",\"participants\":[{\"name\":\"Chandra\"},{\"name\":\"Andi\"},{\"name\":\"Budi\"}]}"
```

Add expense using IDs returned by the previous response:
```bash
curl -X POST http://localhost:4110/api/groups/1/expenses -H "Content-Type: application/json" -d "{\"amount\":300000,\"paidBy\":1,\"forParticipants\":[1,2,3]}"
```

Get settlement:
```bash
curl http://localhost:4110/api/groups/1/settlement
```

## Personalization
`app.github-username=ChandraErichsonS`

`chandraerichsons` ASCII/Unicode sum = `1695`

`1695 % 10 = 5`

Service charge = **5%** and is calculated at runtime.
