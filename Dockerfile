# Multi-stage build for Allo Bank Split Bill API
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml ./
RUN mvn dependency:go-offline -B -q

COPY src/ src/
RUN mvn package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY --from=builder /app/target/my-spring-app-allobank-splitbill-1.0.0.jar app.jar
EXPOSE 4110
ENTRYPOINT ["java", "-jar", "app.jar"]
