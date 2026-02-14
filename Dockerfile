# ---- STAGE 1: Build the app ----
FROM gradle:8.13-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

# ---- STAGE 2: Run the app ----
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Install python + pip + ffmpeg
RUN apt-get update \
 && apt-get install -y --no-install-recommends python3 python3-pip ffmpeg \
 && rm -rf /var/lib/apt/lists/*

# Install python deps
COPY requirements.txt /app/requirements.txt
RUN pip3 install --no-cache-dir -r /app/requirements.txt

# Copy jar + python
COPY --from=builder /app/build/libs/*.jar /app/app.jar
COPY transcribe.py /app/transcribe.py

RUN mkdir -p /app/work

ENV PYTHON_CMD=python3
ENV TRANSCRIBE_SCRIPT=/app/transcribe.py

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
