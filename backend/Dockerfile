# =====================================================================
# Etapa 1 — build con Maven + JDK 17
# =====================================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache de dependencias: copio primero el pom.xml y prefetch del repo local.
COPY pom.xml .
RUN mvn -B -e -ntp dependency:go-offline

# Copio el código y empaqueto el jar (skipping tests; corren en CI aparte).
COPY src ./src
RUN mvn -B -e -ntp clean package -DskipTests

# =====================================================================
# Etapa 2 — runtime mínimo con JRE 17
# =====================================================================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copio el jar resultante (excluyo el *-plain.jar de Spring Boot 3+).
COPY --from=build /workspace/target/*.jar /app/app.jar

# Render inyecta PORT en runtime; localmente cae al default 8080.
ENV PORT=8080
EXPOSE 8080

# Le paso PORT al server de Spring vía variable de entorno (lo lee application.properties).
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar /app/app.jar"]
