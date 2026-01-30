# ---- STAGE 1: Build the app ----
FROM gradle:8.13-jdk21 AS builder

WORKDIR /app

# Copy project files
COPY . .

# Build the Spring Boot fat jar
RUN gradle clean bootJar --no-daemon

# ---- STAGE 2: Run the app ----
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built jar from the builder
COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
