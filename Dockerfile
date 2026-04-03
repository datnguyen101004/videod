# Stage 1: build
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# 🔥 Copy pom trước để cache dependency
COPY pom.xml .

# Download dependency (cache layer này)
RUN mvn -B -q -e -DskipTests dependency:go-offline

# Copy source sau
COPY src ./src

# Build jar
RUN mvn -B -q -DskipTests package

# Stage 2: run
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]