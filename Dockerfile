# ✅ Spring Boot용 Dockerfile 예시

# 1️⃣ 빌드 스테이지
FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /workspace
COPY . .
RUN gradle clean build -x test

# 2️⃣ 런타임 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
