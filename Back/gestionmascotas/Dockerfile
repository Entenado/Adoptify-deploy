# Etapa 1: Construcción del JAR
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución de la aplicación
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/gestionmascotas-0.0.1.jar app_gestionmascotas.jar
EXPOSE 7070
ENTRYPOINT ["java", "-jar", "app_gestionmascotas.jar"]
