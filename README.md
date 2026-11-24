# Ejemplos JWT en Java

Ejemplo de autenticación **JWT** en Java con una arquitectura en capas, dividido en un
**backend** (API REST) y una **aplicación web** que lo consume.

El sistema usa **doble token**: un *access token* de vida corta (validado por firma) y un
*refresh token* revocable que **rota** en cada uso (permite detectar reuso/robo). Incluye
roles, hashing de contraseñas con PBKDF2 y límite de intentos de login.

La API implementa el **contrato común** de los ejemplos JWT (el mismo que las versiones en
PHP y .NET: campos `access_token`/`refresh_token`/`expires_in`, roles `Admin`/`User`,
`/api/auth/me` y productos con `id`, `name`, `description` y `price`), así que cualquiera
de los clientes (web PHP, Android, Blazor) puede usarla.

## Arquitectura

| Capa | Módulo | Tecnología |
|------|--------|------------|
| Núcleo de autenticación (JWT) | `api/jwt-auth-core` | jjwt (modelos de auth, PBKDF2, TokenService, AuthService, RateLimiter) |
| Persistencia de autenticación | `api/jwt-auth-data` | Stores en memoria (usuarios, refresh tokens, intentos de login) |
| Catálogo | `api/catalog` | Modelo `Product` + contrato `ProductStore` (paquete `core`) y store en memoria (paquete `data`) |
| API REST | `api/jwt-auth-api` | Spring Boot 4 + Spring Security (compone auth + catálogo) |
| Cliente HTTP de auth | `web/jwt-auth-client` | Spring `RestClient` (login/refresh/logout + `TokenStore`) |
| Cliente HTTP del catálogo | `web/catalog-client` | Spring `RestClient` (`/api/products`), desacoplado de auth |
| App web (UI) | `web/jwt-auth-web` | Vaadin 25 |
| Tests | `jwt-auth-core`, `jwt-auth-data`, `jwt-auth-api`, `jwt-auth-client` | JUnit 6 (MockMvc en la API, MockRestServiceServer en el cliente) |
| Docs API | `api/jwt-auth-api` | springdoc-openapi (`/swagger-ui.html`) |

> Todo lo referente a **JWT/autenticación** vive en `jwt-auth-*` y está separado del **catálogo de productos**
> (`catalog` en el backend, `catalog-client` en la app web). El catálogo no depende de la autenticación.

> **Vaadin** permite escribir la interfaz en Java puro, con el estado viviendo en el
> servidor y actualización de la UI en tiempo real.

## Estructura del repositorio

Son **dos proyectos independientes**, cada uno con su propio build y su `Dockerfile`:

```
.
├── api/                     # BACKEND — se publica como imagen jwt-ejemplo-api
│   ├── jwt-auth-core/       # auth: modelos, PBKDF2, TokenService, AuthService, RateLimiter
│   ├── jwt-auth-data/       # auth: stores en memoria (usuarios, refresh tokens, intentos)
│   ├── catalog/             # catálogo: modelo Product + ProductStore y store en memoria
│   ├── jwt-auth-api/        # API REST (Spring Boot, puerto 8080)
│   └── Dockerfile
├── web/                     # CLIENTE WEB — se publica como imagen jwt-ejemplo-web
│   ├── jwt-auth-client/     # cliente HTTP de auth (login/refresh/logout + TokenStore)
│   ├── catalog-client/      # cliente HTTP del catálogo (/api/products)
│   ├── jwt-auth-web/        # UI Vaadin (puerto 8081)
│   └── Dockerfile
└── docker-compose.yml       # levanta api + web juntos
```

La app web habla con la API **de servidor a servidor** (el navegador solo habla con Vaadin).

## Requisitos

- JDK 25 (LTS)
- Maven 3.9+
- Docker (opcional, para contenerizar)

## Ejecutar en local (sin Docker)

Abre **dos terminales**.

**1) API (backend):**
```bash
cd api
mvn install -DskipTests          # instala los módulos hermanos (core, data, catálogo)
mvn spring-boot:run -pl jwt-auth-api
# API en http://localhost:8080  ·  Swagger en http://localhost:8080/swagger-ui.html
```

**2) App web (Vaadin):**
```bash
cd web
mvn install -DskipTests          # instala los clientes HTTP (auth y catálogo)
mvn spring-boot:run -pl jwt-auth-web
# Web en http://localhost:8081
```

Luego abre <http://localhost:8081>. Usuarios de demostración:

| Usuario | Contraseña | Rol |
|---------|-----------|-------|
| `admin` | `Admin123!` | Admin |
| `user`  | `User123!`  | User |

## Ejecutar con Docker

Con un solo comando se compilan y levantan ambos contenedores:

```bash
docker compose up --build
```

- Web: <http://localhost:8081>
- API: <http://localhost:8080>

Detener: `docker compose down`.

### Construir las imágenes por separado

```bash
docker build -t jwt-ejemplo-api ./api
docker build -t jwt-ejemplo-web ./web

docker run -p 8080:8080 -e JWT_SECRET="$(openssl rand -hex 32)" jwt-ejemplo-api
docker run -p 8081:8081 -e API_BASE_URL=http://host.docker.internal:8080 jwt-ejemplo-web
```

## Endpoints de la API

| Método | Ruta | Protegido | Descripción |
|--------|------|-----------|-------------|
| POST | `/api/auth/login` | No | Devuelve access + refresh token (429 tras varios fallos) |
| POST | `/api/auth/refresh` | No | Rota el par de tokens |
| POST | `/api/auth/logout` | No | Revoca el refresh token enviado (cierra solo esa sesión) |
| GET  | `/api/auth/me` | Sí | Claims del access token |
| GET  | `/api/products/public` | No | Catálogo público (todos los productos, sin precio) |
| GET  | `/api/products` | Sí | Catálogo completo con precio |
| POST | `/api/products` | Sí (Admin) | Crea un producto nuevo |
| POST | `/api/auth/register` | No | Registra un usuario con rol User (extensión de este ejemplo; 409 si ya existe) |
| GET  | `/health` | No | Liveness (lo usa el healthcheck de Docker) |

El **logout es público**: el refresh token del cuerpo es la credencial, así que el cliente no
necesita un access token vigente (ni renovarlo) para cerrar sesión. Cada refresh token se
canjea de forma **atómica**: si dos peticiones lo usan a la vez, solo una obtiene un par nuevo
y la otra cuenta como reuso (se revocan todas las sesiones del usuario).

En **Swagger UI** (`/swagger-ui.html`) usa el botón **Authorize** y pega tu access token
(el que devuelve `/api/auth/login`) para probar los endpoints protegidos.
Con el perfil `prod` Swagger UI y `/v3/api-docs` quedan desactivados.

### Configuración (variables de entorno)

- **API** (`api/jwt-auth-api`): `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE`, `JWT_ACCESS_MINUTES`, `JWT_REFRESH_DAYS`,
  `RATE_LIMIT_MAX_ATTEMPTS` (fallos por cuenta, 5), `RATE_LIMIT_IP_MAX_ATTEMPTS` (fallos por IP, 20) y
  `RATE_LIMIT_WINDOW` (ventana, `15m`). El `issuer` y el `audience` se validan al verificar cada access token.
- **Web** (`web/jwt-auth-web`): `API_BASE_URL` (por defecto `http://localhost:8080`; en Docker Compose apunta a `http://api:8080`).

> **Ninguna clave se versiona.** Si `JWT_SECRET` no está definida, en desarrollo la API genera
> una clave aleatoria al arrancar (los tokens dejan de valer al reiniciar). **En producción**
> activa el perfil `prod` (`SPRING_PROFILES_ACTIVE=prod`) y define `JWT_SECRET` con un secreto
> propio (≥ 32 bytes): con ese perfil la API **no arranca** sin él (ver `CoreBeansConfig`).

> La app web **refresca el access token automáticamente**: ante un `401` reintenta una vez usando
> el *refresh token* (ver `TokenRefreshInterceptor`), de forma transparente para el usuario. Al
> iniciar sesión regenera el ID de la sesión de Vaadin (evita la fijación de sesión), y los tokens
> viven solo en esa sesión del servidor: nunca llegan al navegador.

## Pruebas

```bash
cd api && mvn verify    # núcleo, stores, rate limiting y flujo HTTP completo (MockMvc)
cd web && mvn verify    # cliente HTTP: 401 → refresh → reintento y logout (MockRestServiceServer)
```

`mvn verify` también falla ante advertencias del compilador (`-Xlint:all`) y ante código sin
formatear: el formato es el de [palantir-java-format](https://github.com/palantir/palantir-java-format)
(4 espacios, 120 columnas), aplicado con Spotless. Para corregirlo:

```bash
cd api && mvn spotless:apply
cd web && mvn spotless:apply
```

En CI (GitHub Actions) corren además CodeQL y una prueba de humo contra el stack de Docker
([`scripts/smoke.sh`](scripts/smoke.sh)).

> Los nombres de usuario no distinguen mayúsculas (como en el ejemplo .NET): `Admin` y `admin`
> son la misma cuenta, así que no se puede registrar una cuenta que se confunda con otra.

## Puertos de los ejemplos JWT

Los tres stacks de Docker Compose usan puertos distintos, así que se pueden levantar a la vez
(por ejemplo, para probar la app Android contra cada API):

| Ejemplo | API | Aplicación web |
|---------|-----|----------------|
| PHP | `8000` | `8001` |
| Java | `8080` | `8081` |
| .NET | `5274` | `5070` |

## Licencia

[MIT](LICENSE).
