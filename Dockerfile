# Stage 1: Build
FROM gradle:8.3.2-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
