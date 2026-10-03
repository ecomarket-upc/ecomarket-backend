# Guía del backend de EcoMarket (paso a paso)

Esta guía explica, fase por fase, **qué archivo se creó, en qué carpeta, para qué sirve y cómo probarlo**.
Cada parte se relaciona con una User Story (US) o Technical Story (TS) del Product Backlog del informe.

Estructura del proyecto (estilo PC1 y laboratorios):

```text
src/main/java/pe/edu/upc/ecomarket
├── models        Entidades JPA (tablas)
├── dto           Objetos que entran y salen por la API (JSON)
├── repository    Interfaces JpaRepository (consultas a la base de datos)
├── services      Lógica de negocio
├── controller    Endpoints REST
├── exceptions    Errores propios y manejador global (respuestas 400, 403, 404, 409)
├── security      JWT y filtro de autenticación
└── config        Configuración de Spring Security, Swagger y datos iniciales
```

> En esta PC los puertos 8080 y 8081 están ocupados por otros programas. Para correr la API aquí se usa
> `PORT=18080`. En otra computadora basta con el puerto por defecto (8080).

---

## Fase 1 – Base del proyecto (TS08)

| Archivo | Carpeta | Para qué sirve |
|---|---|---|
| `pom.xml` | raíz | Dependencias Maven: Spring Web, Spring Data JPA, Validation, PostgreSQL, Lombok (y luego Security, JWT y Swagger). |
| `EcomarketBackendApplication.java` | `pe/edu/upc/ecomarket` | Clase principal que arranca Spring Boot. |
| `application.properties` | `src/main/resources` | Conexión a PostgreSQL. La contraseña se puede cambiar con la variable de entorno `DB_PASSWORD`. |

### Pasos

1. En **pgAdmin 4**: clic derecho en *Databases* → *Create* → *Database…* → nombre `ecomarket_db` → *Save*.
2. Revisar en `application.properties` el usuario y la contraseña de PostgreSQL (`postgres` / `postgres` en esta PC).
3. Ejecutar `mvnw.cmd spring-boot:run`.
4. Comprobar en la consola:
   - `HikariPool-1 - Start completed.` → Spring Boot se conectó a PostgreSQL.
   - `Database driver: PostgreSQL JDBC Driver`.
   - `Started EcomarketBackendApplication`.

**Resultado obtenido (2026-10-03):** conexión correcta con PostgreSQL 18.

---

## Fase 2 – Modelo, relaciones y datos iniciales (TS08)

### Entidades (`models/`)

| Archivo | Tabla | Relaciones JPA | Historias |
|---|---|---|---|
| `Rol.java` | `roles` | — | US01, US02, TS01 |
| `Usuario.java` | `usuarios` | `@ManyToOne` Rol | US01–US03, US29, US38 |
| `EstadoComercio.java` | (enum) | — | PENDING / APPROVED / REJECTED (TS02) |
| `Comercio.java` | `comercios` | `@ManyToOne` Usuario (propietario) | US19, US21, US30, TS02, TS10 |
| `Categoria.java` | `categorias` | — | US07, TS05 |
| `EcoEtiqueta.java` | `eco_etiquetas` | — | US14, US28, TS05, TS06 |
| `Producto.java` | `productos` + `producto_eco_etiqueta` | `@ManyToOne` Comercio y Categoria, `@ManyToMany` EcoEtiqueta | US10, US11, US13, US20, TS04 |
| `Favorito.java` | `favoritos` | `@ManyToOne` Usuario y Comercio | US09, TS11 |
| `Preferencia.java` | `preferencias` | `@ManyToOne` Usuario y EcoEtiqueta | US04, TS11 |
| `Busqueda.java` | `busquedas` | `@ManyToOne` Usuario (opcional) | US17, TS13 |
| `Visita.java` | `visitas` | `@ManyToOne` Comercio, Producto y Usuario | US22, US23, US31, TS13 |
| `Suscripcion.java` | `suscripciones` | `@ManyToOne` Comercio | US24, TS14 |
| `Promocion.java` | `promociones` | `@ManyToOne` Comercio | US26, TS14 |

```text
Rol ← Usuario ← Comercio ← Producto → Categoria
                    ↑          ↓
                Favorito   EcoEtiqueta ← Preferencia → Usuario
                Visita, Suscripcion, Promocion
```

### Repositorios (`repository/`)

Una interfaz por entidad que extiende `JpaRepository<Entidad, Long>`. Spring Data genera el código
de `save`, `findById`, `findAll`, `deleteById`, etc.

### Datos iniciales (`config/DatosIniciales.java`)

Clase `CommandLineRunner` que se ejecuta al arrancar y crea, solo si no existen:
- 3 roles: CONSUMIDOR, COMERCIANTE, ADMINISTRADOR.
- 6 categorías (Alimentos a granel, Frutas y verduras, Cuidado personal…).
- 7 Eco-Etiquetas oficiales con sus palabras clave (Venta a granel, Sin envase plástico, Producto local…).
- El usuario administrador se agrega en la Fase 4, cuando ya existe BCrypt para cifrar su contraseña.

### Cómo comprobarlo en pgAdmin 4

1. Ejecutar la aplicación.
2. En pgAdmin: `ecomarket_db` → *Schemas* → *public* → *Tables* → clic derecho → *Refresh*. Deben aparecer 13 tablas.
3. Clic derecho en `eco_etiquetas` → *View/Edit Data* → *All Rows*: 7 registros. En `categorias`: 6. En `roles`: 3.
4. Prueba de integridad (TS08, escenario 2). En *Query Tool* ejecutar:
   ```sql
   insert into productos (nombre, precio, stock, comercio_id, categoria_id, activo, fecha_registro)
   values ('Prueba', 1, 1, 999, 1, true, now());
   ```
   Resultado esperado: `ERROR: ... viola la llave foránea ... La llave (comercio_id)=(999) no está presente en la tabla «comercios».`

**Resultado obtenido (2026-10-03):** 13 tablas, 16 llaves foráneas, 3 roles, 6 categorías y 7 Eco-Etiquetas; el insert de prueba fue rechazado por la llave foránea.

---

## Fase 3 – CRUD básico: Categorías y Eco-Etiquetas (TS05, US28)

| Archivo | Carpeta | Para qué sirve |
|---|---|---|
| `CategoriaDTO.java`, `EcoEtiquetaDTO.java` | `dto` | JSON de entrada y salida, con validaciones (`@NotBlank`, `@Size`). |
| `MensajeDTO.java`, `ErrorDTO.java` | `dto` | Respuesta con un mensaje y formato único de errores. |
| `CategoriaService.java` | `services` | Listar, obtener, crear, actualizar y eliminar. No permite nombres repetidos (409) ni borrar categorías con productos (409). |
| `EcoEtiquetaService.java` | `services` | Igual que categorías. Al eliminar una Eco-Etiqueta asignada a productos la **desactiva** (eliminación lógica, TS05). |
| `CategoriaController.java` | `controller` | `/api/categorias` |
| `EcoEtiquetaController.java` | `controller` | `/api/eco-etiquetas` |
| `RecursoNoEncontradoException`, `ReglaNegocioException`, `AccesoDenegadoException`, `ConflictoException` | `exceptions` | Errores propios: 404, 400, 403 y 409. |
| `ManejadorExcepciones.java` | `exceptions` | `@RestControllerAdvice` que convierte cada error en JSON con su código HTTP. |

> En esta fase todavía no hay seguridad. Desde la Fase 4, crear, editar y eliminar requieren el token del administrador.

### Pruebas en Postman

Header en todas las peticiones con cuerpo: `Content-Type: application/json`.

| # | Método y URL | Body | Respuesta esperada |
|---|---|---|---|
| 1 | `GET /api/categorias` | — | **200** con las 6 categorías iniciales |
| 2 | `POST /api/categorias` | `{"nombre": "Mascotas", "descripcion": "Productos para mascotas"}` | **201** con el `id` creado |
| 3 | `POST /api/categorias` (mismo nombre) | igual que el 2 | **409** "Ya existe una categoría llamada Mascotas" |
| 4 | `POST /api/categorias` | `{"nombre": ""}` | **400** con `errores.nombre` = "El nombre es obligatorio" |
| 5 | `GET /api/categorias/999` | — | **404** "Categoría con id 999 no existe" |
| 6 | `GET /api/eco-etiquetas` | — | **200** con las 7 Eco-Etiquetas activas |
| 7 | `POST /api/eco-etiquetas` | `{"nombre": "Comercio justo", "descripcion": "Pago justo", "criterio": "Compra directa al productor", "palabrasClave": "comercio justo, compra directa"}` | **201** |
| 8 | `DELETE /api/eco-etiquetas/{id}` (sin productos) | — | **204** (se borra) |
| 9 | `DELETE /api/eco-etiquetas/{id}` (con productos) | — | **200** `{"mensaje": "La Eco-Etiqueta está asignada a productos: se desactivó…"}` (se prueba en la Fase 7) |
| 10 | `DELETE /api/categorias/{id}` | — | **204** |

**Resultado obtenido (2026-10-03):** las pruebas 1 a 8 y la 10 respondieron con el código esperado.

---

## Fase 4 – Seguridad: BCrypt, registro, login, JWT y roles (TS01, US01, US02, US03, US29, US38)

| Archivo | Carpeta | Para qué sirve |
|---|---|---|
| `pom.xml` | raíz | Se agregan `spring-boot-starter-security` y `jjwt` (api, impl y jackson). |
| `application.properties` | `resources` | `jwt.secreto`, `jwt.expiracion-minutos`, correo y contraseña del administrador inicial, origen CORS. |
| `RegistroDTO`, `LoginDTO`, `AuthRespuestaDTO`, `UsuarioDTO`, `PerfilDTO` | `dto` | JSON de registro, login, respuesta con token y perfil. El rol del registro solo puede ser CONSUMIDOR o COMERCIANTE. |
| `JwtUtil.java` | `security` | Genera el token (correo, id y rol) y lo valida. |
| `JwtFiltro.java` | `security` | Lee `Authorization: Bearer <token>` en cada petición y autentica al usuario. |
| `UsuarioDetallesService.java` | `security` | Carga el usuario por correo para Spring Security (rol `ROLE_<nombre>`; cuenta desactivada = no entra). |
| `UsuarioActual.java` | `security` | Devuelve el usuario dueño del token (para saber quién crea un comercio, sus favoritos, etc.). |
| `ErroresSeguridad.java` | `security` | Respuestas JSON 401 (sin token) y 403 (rol sin permiso). |
| `SecurityConfig.java` | `config` | `BCryptPasswordEncoder`, rutas públicas, API sin sesión (stateless), CORS y filtro JWT. |
| `AuthService.java`, `UsuarioService.java` | `services` | Registro (contraseña con BCrypt), login, perfil y desactivación de cuentas. |
| `AuthController.java` | `controller` | `POST /api/auth/register`, `POST /api/auth/login`. |
| `UsuarioController.java` | `controller` | `GET /api/usuarios` (admin), `GET` y `PUT /api/usuarios/perfil`, `PATCH /api/usuarios/{id}/desactivar` (admin). |
| `DatosIniciales.java` | `config` | Ahora también crea el administrador `admin@ecomarket.pe` / `Admin12345`. |

Control por rol: `@PreAuthorize("hasRole('ADMINISTRADOR')")` en los métodos de crear, editar y eliminar
categorías y Eco-Etiquetas, y en la gestión de usuarios.

> **Cierre de sesión (US03):** como la API no guarda sesiones, cerrar sesión consiste en que el frontend
> borre el token. Después de expirar (120 minutos) el token deja de servir.

### Pruebas en Postman

1. **Registro de consumidor** – `POST /api/auth/register`
   ```json
   {"nombres": "Lucía", "apellidos": "Quispe", "correo": "lucia@correo.com", "contrasena": "Clave2026", "rol": "CONSUMIDOR"}
   ```
   **201** con `token`, `tipo: "Bearer"` y `usuario` (sin contraseña).
2. Mismo registro otra vez → **409** "El correo lucia@correo.com ya está en uso".
3. Registro inválido `{"nombres": "", "apellidos": "X", "correo": "malo", "contrasena": "corta", "rol": "ADMINISTRADOR"}` → **400** con `errores` por campo.
4. **Registro de comerciante** – igual que el 1 con `"rol": "COMERCIANTE"` y otro correo → **201**.
5. **Login** – `POST /api/auth/login` `{"correo": "admin@ecomarket.pe", "contrasena": "Admin12345"}` → **200** con token.
   En Postman: pestaña *Authorization* → *Bearer Token* → pegar el token.
6. Login con contraseña incorrecta → **401** "Correo o contraseña incorrectos".
7. `GET /api/usuarios/perfil` sin token → **401**.
8. `GET /api/usuarios` con token de consumidor → **403** (TS01, escenario 3).
9. `GET /api/usuarios` con token de administrador → **200** con la lista.
10. `POST /api/categorias` con token de consumidor → **403**; con token de administrador → **201**.
11. `PUT /api/usuarios/perfil` con `{"nombres": "Lucía", "apellidos": "Quispe Rojas", "contrasena": "corta"}` → **400** (US38, escenario 2).
12. En pgAdmin, la tabla `usuarios` muestra la contraseña como `$2a$10$...` (hash BCrypt), nunca en texto plano.

**Resultado obtenido (2026-10-03):** todas las pruebas respondieron con el código esperado.
