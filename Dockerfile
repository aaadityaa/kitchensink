# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN ./mvnw -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render sets PORT dynamically, your Spring Boot app already uses ${PORT:9092}
ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]
