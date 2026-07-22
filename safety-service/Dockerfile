# --- Build stage ---
FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

COPY safety-service/mvnw .
COPY safety-service/.mvn .mvn
COPY safety-service/pom.xml .
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY safety-service/src ./src
COPY db/migration ./src/main/resources/db/migration
RUN ./mvnw -B clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:26-jre
WORKDIR /app
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8083
EXPOSE ${PORT}
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
