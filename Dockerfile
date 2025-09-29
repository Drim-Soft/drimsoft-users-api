# Etapa 1: build con Maven y JDK
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: run con JDK ligero
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

ENV SUPABASE_URL="https://snzwdmlrtalwdrgfzejq.supabase.co"
ENV SUPABASE_ANON_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNuendkbWxydGFsd2RyZ2Z6ZWpxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTkxMTgwMDEsImV4cCI6MjA3NDY5NDAwMX0.iYhWpK3hiwkm1wast1uMfuy5qGbPgNnVlN0vsWzNe6I"
ENV SUPABASE_SERVICE_ROLE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNuendkbWxydGFsd2RyZ2Z6ZWpxIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1OTExODAwMSwiZXhwIjoyMDc0Njk0MDAxfQ.OsAJTkFSVjo8xMscXQFDEUhHq6d-_xgSfV8_qTF0zvM"
ENV SUPABASE_JWT_SECRET="fQn8emsM8DDQtrX8BsdNJPz05BNOZ3T2FUgCoFTD09RelwrRnqGHx+GC5APhV43eAwvR6ZcjCOcYfygmkf9YnQ=="

