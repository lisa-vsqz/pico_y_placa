# ===================================================================
# Dockerfile - Backend Spring Boot (Multi-stage build)
# ===================================================================
# Etapa 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Seguridad: no ejecutar como root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/*.jar app.jar

# Configuración por variable de entorno
ENV SERVER_PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV APP_TIMEZONE=America/Guayaquil

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
