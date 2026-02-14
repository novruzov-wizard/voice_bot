# ---- STAGE 1: Build the app ----
FROM gradle:8.13-jdk21 AS builder

WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon


# ---- STAGE 2: Run the app ----
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Install python + ffmpeg (and clean apt cache)
RUN apt-get update \
 && apt-get install -y --no-install-recommends python3 ffmpeg \
 && rm -rf /var/lib/apt/lists/*

# Copy the built jar
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# ✅ Copy your transcribe.py into the container
# Put transcribe.py in your repo root or adjust the path below
COPY transcribe.py /app/transcribe.py

# Optional: make sure work dir exists (your Java writes to "work/")
RUN mkdir -p /app/work

# Optional env vars (matches the Java code I sent earlier)
ENV PYTHON_CMD=python3
ENV TRANSCRIBE_SCRIPT=/app/transcribe.py

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
