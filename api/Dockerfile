# ---- Etapa de compilacion ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copiamos primero los POMs para aprovechar la cache de dependencias
COPY pom.xml .
COPY jwt-auth-core/pom.xml jwt-auth-core/
COPY jwt-auth-data/pom.xml jwt-auth-data/
COPY catalog-core/pom.xml catalog-core/
COPY catalog-data/pom.xml catalog-data/
COPY jwt-auth-api/pom.xml jwt-auth-api/
RUN mvn -B -q dependency:go-offline || true

# Copiamos el codigo y empaquetamos (sin tests para acelerar la imagen)
COPY . .
RUN mvn -B -DskipTests clean package

# ---- Etapa de ejecucion ----
FROM eclipse-temurin:21-jre AS run
WORKDIR /app
COPY --from=build /build/jwt-auth-api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
