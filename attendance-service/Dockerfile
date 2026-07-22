# --- Build stage ---
FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

COPY attendance-service/mvnw .
COPY attendance-service/.mvn .mvn
COPY attendance-service/pom.xml .
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY attendance-service/src ./src
COPY db/migration ./src/main/resources/db/migration
RUN ./mvnw -B clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:26-jre
WORKDIR /app
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8082
EXPOSE ${PORT}
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
