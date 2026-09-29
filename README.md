# EcoMarket Backend

RESTful API de **EcoMarket**, el buscador inteligente de comercio local y sostenible.
Proyecto del curso 1ASI0705 Arquitectura de Aplicaciones Web (UPC), Grupo 06.

Conecta a consumidores con comercios locales sostenibles cercanos a su ubicación.
Los comerciantes publican sus productos y un clasificador con IA les asigna eco-etiquetas
("Venta a granel", "Sin envase plástico", "Producto local"...).

## Stack

- Java 21 y Spring Boot 4.1
- Spring Web MVC, Spring Data JPA y Bean Validation
- Spring Security con JWT (jjwt) y contraseñas con BCrypt
- MySQL 8 (H2 en memoria para pruebas y demos)
- springdoc-openapi (Swagger UI)
- OpenStreetMap Nominatim para geocodificar direcciones
- Google Gemini para clasificar productos, con un clasificador por palabras clave como respaldo

## Cómo ejecutarlo

Requisitos: JDK 21 o superior. Maven no hace falta, porque el proyecto trae el wrapper `mvnw`.

### Rápido, sin instalar MySQL (perfil `h2`)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

En Windows usa `mvnw.cmd` en lugar de `./mvnw`. La base de datos vive en memoria y se borra al apagar la aplicación.

### Con MySQL

La base `ecomarket` se crea sola. Solo hay que indicar la contraseña:

```bash
DB_PASSWORD=tu_clave ./mvnw spring-boot:run
```

En PowerShell: `$env:DB_PASSWORD="tu_clave"; .\mvnw.cmd spring-boot:run`.

### Pruebas

```bash
./mvnw test
```

Son pruebas de integración que levantan toda la API sobre H2 y prueban autenticación, roles, validaciones y el CRUD de cada módulo.

## Documentación y pruebas manuales

- **Swagger UI:** http://localhost:8080/swagger-ui.html. Primero haz login, copia el `token` y pégalo en el botón **Authorize**.
- **Postman:** importa [`postman/EcoMarket.postman_collection.json`](postman/EcoMarket.postman_collection.json) y ejecútala en orden con el *Collection Runner*. Los tokens y los ids se guardan solos en las variables de la colección. Si la app corre en otro puerto, cambia la variable `baseUrl`.

Usuario administrador por defecto: `admin@ecomarket.pe` / `Admin12345!`. Se crea al arrancar y se cambia con `ADMIN_EMAIL` y `ADMIN_PASSWORD`.

## Variables de entorno

| Variable | Para qué sirve | Valor por defecto |
|---|---|---|
| `PORT` | Puerto HTTP | `8080` |
| `DB_URL` | URL JDBC de MySQL | `jdbc:mysql://localhost:3306/ecomarket?createDatabaseIfNotExist=true...` |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciales de MySQL | `root` / vacío |
| `JWT_SECRET` | Clave Base64 (mínimo 32 bytes) para firmar tokens | Clave de desarrollo; **cámbiala en producción** |
| `JWT_EXPIRATION_MINUTES` | Duración del token | `60` |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Administrador inicial | `admin@ecomarket.pe` / `Admin12345!` |
| `CORS_ALLOWED_ORIGINS` | Orígenes del frontend, separados por coma | `http://localhost:4200` |
| `GEMINI_API_KEY` | Activa la clasificación con Gemini | vacío (usa palabras clave) |
| `GEMINI_MODEL` | Modelo de Gemini | `gemini-2.5-flash` |
| `GEOCODING_ENABLED` | Activa la geocodificación con Nominatim | `true` |

Nunca subas claves reales al repositorio: el repo es público. Usa variables de entorno o un archivo `.env`, que ya está en `.gitignore`.

## Arquitectura

El código sigue **Domain-Driven Design**, con un paquete por *bounded context*:

```
pe.edu.upc.ecomarket
├── shared     Kernel común: aggregate root auditable, excepciones, manejo global de errores, Swagger
├── iam        Identidad y acceso: usuarios, roles, JWT, seguridad
├── commerce   Comercios: registro, validación por el admin, geolocalización y búsqueda por cercanía
└── catalog    Catálogo: categorías, eco-etiquetas, productos y clasificación con IA
```

Cada contexto tiene cuatro capas:

| Capa | Contenido |
|---|---|
| `domain` | Aggregates, value objects, commands, queries, eventos e interfaces de servicio |
| `application` | Implementación de los servicios (reglas de negocio), puertos de salida y ACL |
| `infrastructure` | Repositorios JPA y adaptadores externos (JWT, BCrypt, Nominatim, Gemini) |
| `interfaces` | Controladores REST, resources (DTOs) y assemblers |

Los contextos no se llaman por dentro. Se comunican por **fachadas ACL** (`IamContextFacade`, `CommerceContextFacade`) y por **eventos de dominio** (`StoreDeletedEvent`: al borrar un comercio, catalog borra sus productos).

### Roles y reglas principales

- **Consumidor** (`ROLE_CONSUMER`): consulta comercios y productos.
- **Comerciante** (`ROLE_SELLER`): registra sus comercios y productos, y solo puede editar lo suyo.
- **Administrador** (`ROLE_ADMIN`): valida comercios y gestiona usuarios, categorías y eco-etiquetas. No se autoregistra.
- Un comercio nuevo queda en `PENDING`. Ni él ni sus productos son públicos hasta que el admin lo apruebe (`APPROVED`).
- Si el comercio no envía latitud y longitud, su dirección se geocodifica automáticamente.

## Endpoints

| Recurso | Método y ruta | Acceso |
|---|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` | Público |
| Auth | `GET /api/auth/me` | Autenticado |
| Usuarios | `GET /api/usuarios`, `DELETE /api/usuarios/{id}` | Admin |
| Usuarios | `GET /api/usuarios/{id}`, `PUT /api/usuarios/{id}` | Admin o el propio usuario |
| Comercios | `GET /api/comercios[?distrito=]`, `GET /api/comercios/{id}` | Público |
| Comercios | `POST /api/comercios`, `GET /api/comercios/mis-comercios` | Comerciante |
| Comercios | `PUT /api/comercios/{id}`, `DELETE /api/comercios/{id}` | Dueño o admin |
| Comercios | `GET /api/comercios/pendientes`, `PATCH /api/comercios/{id}/validacion` | Admin |
| Categorías | `GET /api/categorias[/{id}]` | Público |
| Categorías | `POST`, `PUT /{id}`, `DELETE /{id}` | Admin |
| Eco-etiquetas | `GET /api/eco-etiquetas[/{id}]` | Público |
| Eco-etiquetas | `POST`, `PUT /{id}`, `DELETE /{id}` | Admin |
| Productos | `GET /api/productos[?comercioId&categoriaId&ecoEtiquetaId]`, `GET /api/productos/{id}` | Público |
| Productos | `POST /api/productos`, `GET /api/productos/mis-productos` | Comerciante |
| Productos | `PUT /{id}`, `DELETE /{id}`, `POST /{id}/clasificar` | Dueño del comercio o admin |
| Búsqueda | `GET /api/busqueda/cercanos?lat&lng&radio` | Público |
| Búsqueda | `GET /api/busqueda/productos?nombre&categoria&ecoEtiqueta` | Público |

Todos los errores responden con el mismo formato JSON:

```json
{
  "timestamp": "2026-09-29T20:35:17Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Hay campos inválidos",
  "path": "/api/comercios",
  "errors": { "name": "El nombre es obligatorio" }
}
```

## Flujo de trabajo (GitFlow)

- `main`: versiones entregadas.
- `develop`: integración, y rama por defecto.
- `feature/<nombre>`: una por funcionalidad. Sale de `develop` y vuelve a `develop` con `--no-ff`.
- `release/<versión>` y `hotfix/<nombre>`: según GitFlow.

Commits en inglés con [Conventional Commits](https://www.conventionalcommits.org/) y scope, por ejemplo `feat(catalog): add product search`.
