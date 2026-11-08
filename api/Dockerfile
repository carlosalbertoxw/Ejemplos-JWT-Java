# ---- Etapa de compilacion ----
FROM maven:3.10-eclipse-temurin-25 AS build
WORKDIR /build

# Copiamos primero los POMs para aprovechar la cache de dependencias
COPY pom.xml .
COPY jwt-auth-core/pom.xml jwt-auth-core/
COPY jwt-auth-data/pom.xml jwt-auth-data/
COPY catalog-core/pom.xml catalog-core/
COPY catalog-data/pom.xml catalog-data/
COPY jwt-auth-api/pom.xml jwt-auth-api/
RUN mvn -B -q dependency:go-offline

# Copiamos el codigo y empaquetamos (las pruebas corren en CI)
COPY . .
RUN mvn -B -DskipTests clean package

# ---- Etapa de ejecucion ----
FROM eclipse-temurin:25-jre AS run
WORKDIR /app
# Usuario sin privilegios: si alguien explotara la app, no tendria root en el contenedor.
RUN useradd --system --uid 10001 app
COPY --from=build /build/jwt-auth-api/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
