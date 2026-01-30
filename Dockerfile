FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY build/libs/voice-bot-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]