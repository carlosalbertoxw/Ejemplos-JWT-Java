# ---- Etapa de compilacion ----
# El perfil "production" compila el frontend de Vaadin (descarga Node automaticamente).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY jwt-auth-client/pom.xml jwt-auth-client/
COPY catalog-client/pom.xml catalog-client/
COPY jwt-auth-web/pom.xml jwt-auth-web/
RUN mvn -B -q dependency:go-offline || true

COPY . .
RUN mvn -B -DskipTests -Pproduction clean package

# ---- Etapa de ejecucion ----
FROM eclipse-temurin:21-jre AS run
WORKDIR /app
COPY --from=build /build/jwt-auth-web/target/*.jar app.jar
# URL de la API dentro de la red de Docker (servicio "api")
ENV API_BASE_URL=http://api:8080
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
