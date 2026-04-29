# Build stage
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render provides a PORT environment variable
ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT java -Dserver.port=${PORT:-8080} -jar app.jar
