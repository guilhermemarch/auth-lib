# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml first to leverage Docker cache
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

RUN ls -l /app/target/*.jar && \
    test -s /app/target/*.jar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk --no-cache add curl

COPY --from=build /app/target/auth-api-0.0.1-SNAPSHOT.jar app.jar

RUN ls -l app.jar && \
    test -s app.jar

# Expose the application port
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 