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

---

## Fase 5 – Comercios y geolocalización (TS02, TS03, TS10, US05, US07, US08, US19, US21, US30)

| Archivo | Carpeta | Para qué sirve |
|---|---|---|
| `ComercioDTO.java` | `dto` | Datos para registrar o editar un comercio. Latitud y longitud son opcionales. |
| `ComercioRespuestaDTO.java` | `dto` | Detalle del comercio que devuelve la API (incluye `estado`). |
| `ValidacionComercioDTO.java` | `dto` | `{"estado": "APPROVED"}` o `{"estado": "REJECTED", "motivo": "..."}`. |
| `ComercioCercanoDTO.java`, `CoordenadasDTO.java` | `dto` | Comercio con su distancia en km; latitud y longitud de la geocodificación. |
| `ComercioRepository.java` | `repository` | Consultas por estado, distrito, dueño y por un cuadrado de latitud y longitud. |
| `GeocodificacionService.java` | `services` | Llama a OpenStreetMap Nominatim (gratis, sin API key) para convertir la dirección en coordenadas. Tiempo máximo: 5 s. |
| `ComercioService.java` | `services` | Reglas: el comercio nace `PENDING`; solo el dueño o el admin lo editan o eliminan (403); un comercio no aprobado responde 404 al público; rechazar exige motivo; al corregir un rechazado vuelve a `PENDING`; búsqueda por cercanía con la fórmula de Haversine (radio máximo 50 km). |
| `ComercioController.java` | `controller` | `/api/comercios` |
| `BusquedaController.java` | `controller` | `GET /api/busqueda/cercanos?lat&lng&radio` |
| `SecurityConfig.java` | `config` | Hace públicas las consultas GET de comercios y búsqueda (salvo `mis-comercios`, `pendientes` y estadísticas). |

> El mapa en sí lo dibuja el frontend; el backend entrega latitud, longitud y distancia.

### Pruebas en Postman

Primero registrar un comerciante (`"rol": "COMERCIANTE"`), hacer login y usar su token.

| # | Método y URL | Token | Body | Respuesta esperada |
|---|---|---|---|---|
| 1 | `POST /api/comercios` | comerciante | `{"nombre": "Granel Verde", "descripcion": "Tienda a granel", "telefono": "987654321", "horario": "Lun-Sáb 9:00-19:00", "direccion": "Avenida José Larco 345", "distrito": "Miraflores", "ciudad": "Lima"}` | **201**, `estado: "PENDING"` y latitud/longitud calculadas |
| 2 | `POST /api/comercios` | consumidor | el mismo | **403** |
| 3 | `POST /api/comercios` | comerciante | dirección inexistente y sin coordenadas | **400** "No se pudo ubicar la dirección. Indica la latitud y la longitud…" (TS10, escenario 2) |
| 4 | `GET /api/comercios/{id}` | sin token | — | **404** (aún no aprobado, US08) |
| 5 | `GET /api/comercios/{id}` | el dueño | — | **200** |
| 6 | `PUT /api/comercios/{id}` | otro comerciante | datos con `latitud` y `longitud` | **403** (TS02, escenario 2) |
| 7 | `GET /api/comercios/pendientes` | admin | — | **200** |
| 8 | `PATCH /api/comercios/{id}/validacion` | admin | `{"estado": "REJECTED"}` | **400** "Debes indicar el motivo del rechazo" |
| 9 | `PATCH /api/comercios/{id}/validacion` | admin | `{"estado": "APPROVED"}` | **200**, `estado: "APPROVED"` (TS02, escenario 3) |
| 10 | `GET /api/comercios` | sin token | — | **200**, ya aparece el comercio |
| 11 | `GET /api/busqueda/cercanos?lat=-12.1020&lng=-77.0420&radio=5` | sin token | — | **200** con el comercio y `distanciaKm` ≈ 2.5 |
| 12 | `GET /api/busqueda/cercanos?lat=-12.1020&lng=-77.0420&radio=1` | sin token | — | **200** con `[]` (fuera del radio, US07) |
| 13 | `GET /api/busqueda/cercanos?lat=-12.1&lng=-77.0&radio=60` | sin token | — | **400** "El radio debe ser mayor que 0 y como máximo 50 km" (TS03) |

**Resultado obtenido (2026-10-03):** las 13 pruebas respondieron con el código esperado.

---

## Fase 6 – Productos, categorías, búsqueda y filtros (TS04, US06, US07, US10, US11, US12, US15, US16, US20, US29)

| Archivo | Carpeta | Para qué sirve |
|---|---|---|
| `ProductoDTO.java` | `dto` | Datos del producto. `ecoEtiquetaIds` es opcional: si se envía, las Eco-Etiquetas quedan como "manual" (US16). |
| `ProductoRespuestaDTO.java` | `dto` | Detalle con comercio, categoría y Eco-Etiquetas con descripción y criterio (US11, US14). Incluye `avisoEcoEtiquetas` si no tiene ninguna. |
| `ProductoComparadoDTO.java` | `dto` | Producto y distancia (omitida si no se envía la ubicación, US12). |
| `ProductoRepository.java` | `repository` | Catálogo por comercio, productos del comerciante y la consulta `buscar` (nombre, categoría y Eco-Etiqueta, sin distinguir mayúsculas). |
| `ProductoService.java` | `services` | Solo el dueño publica, edita o elimina (403 para otro comerciante). Eliminar es lógico (`activo = false`) para conservar las visitas. Un producto eliminado o de un comercio no aprobado responde 404. |
| `ProductoController.java` | `controller` | `/api/productos`, `/api/productos/mis-productos`, `/api/productos/comparar` |
| `ComercioController.java` | `controller` | Nuevo `GET /api/comercios/{id}/productos` (catálogo, US10). |
| `BusquedaController.java` | `controller` | Nuevos `GET /api/busqueda/productos` y `GET /api/busqueda/comercios?texto=` |

### Pruebas en Postman

Token del comerciante dueño del comercio aprobado en la Fase 5.

| # | Método y URL | Token | Body | Respuesta esperada |
|---|---|---|---|---|
| 1 | `POST /api/productos` | comerciante | `{"comercioId": 1, "categoriaId": 1, "nombre": "Quinua blanca", "descripcion": "Quinua orgánica de Puno vendida a granel", "precio": 12.50, "stock": 40}` | **201** |
| 2 | `POST /api/productos` | comerciante | `{..., "ecoEtiquetaIds": [4]}` | **201** con `motorClasificacion: "manual"` |
| 3 | `POST /api/productos` | comerciante | `"precio": -1, "stock": -3` | **400** con `errores.precio` y `errores.stock` (US20) |
| 4 | `POST /api/productos` | otro comerciante | producto en el comercio 1 | **403** (US20, escenario 3) |
| 5 | `POST /api/productos` | comerciante | `"categoriaId": 999` | **404** |
| 6 | `GET /api/productos/{id}` | sin token | — | **200** con Eco-Etiquetas y su significado (US11, US14) |
| 7 | `GET /api/comercios/1/productos` | sin token | — | **200** catálogo (US10) |
| 8 | `GET /api/busqueda/productos?nombre=QUINUA` | sin token | — | **200** (no distingue mayúsculas, US06) |
| 9 | `GET /api/busqueda/productos?categoria=Alimentos` | sin token | — | **200** solo de esa categoría (US07) |
| 10 | `GET /api/busqueda/productos?ecoEtiqueta=Artesanal` | sin token | — | **200** solo con esa Eco-Etiqueta (US15) |
| 11 | `GET /api/busqueda/productos?nombre=zzz` | sin token | — | **200** con `[]` (sin resultados) |
| 12 | `GET /api/busqueda/comercios?texto=verde` | sin token | — | **200** con "Granel Verde" (US06, escenario 3) |
| 13 | `GET /api/productos/comparar?ids=2,3&lat=-12.1020&lng=-77.0420` | sin token | — | **200** con precio, comercio, Eco-Etiquetas y `distanciaKm` |
| 14 | `GET /api/productos/comparar?ids=2,3` | sin token | — | **200** sin `distanciaKm` (US12, escenario 3) |
| 15 | `GET /api/productos/comparar?ids=2` | sin token | — | **400** "Debes elegir entre 2 y 4 productos…" |
| 16 | `PUT /api/productos/{id}` | otro comerciante | — | **403** |
| 17 | `DELETE /api/productos/{id}` | dueño o admin | — | **204**; luego `GET` del producto → **404** |

**Resultado obtenido (2026-10-03):** las pruebas 1 a 16 respondieron con el código esperado (la 17 se cubre en la Fase 10).

---

## Fase 7 – Eco-Etiquetas con IA: Gemini y respaldo por palabras clave (TS06, US13, US16, US28)

| Archivo | Carpeta | Para qué sirve |
|---|---|---|
| `application.properties` | `resources` | `gemini.api-key=${GEMINI_API_KEY:}` (la clave **solo** va en la variable de entorno), modelo `gemini-2.5-flash` y tiempo de espera (10 s). |
| `ClasificadorService.java` | `services` | Envía nombre, descripción y Eco-Etiquetas activas a Gemini y lee los ids que sugiere. Si no hay clave, Gemini falla o supera el tiempo de espera, usa las **palabras clave** de cada Eco-Etiqueta (ignora tildes, mayúsculas, género y plural). |
| `ClasificacionDTO.java` | `dto` | Respuesta de `/clasificar`: `producto`, `motor` (`gemini` o `keywords`) y `mensaje`. |
| `ProductoService.java` | `services` | Al publicar sin elegir Eco-Etiquetas, clasifica automáticamente (TS06, escenario 1). Nuevo método `clasificar`. |
| `ProductoController.java` | `controller` | Nuevo `POST /api/productos/{id}/clasificar` (solo el dueño). |

```text
Producto → Spring Boot → Gemini → Eco-Etiquetas sugeridas → Producto
                           ↓ (sin clave, error o tiempo de espera)
                     Palabras clave del administrador
```

### Configurar la API key de Gemini (sin subirla a GitHub)

1. Crear la clave en https://aistudio.google.com/app/apikey
2. En Windows (PowerShell), una sola vez: `setx GEMINI_API_KEY "tu-clave"` y reabrir VS Code.
   O en VS Code, en `.vscode/launch.json`: `"env": { "GEMINI_API_KEY": "tu-clave" }` (la carpeta `.vscode` no se sube).
3. Al reiniciar la API, `/clasificar` responderá `"motor": "gemini"`.

### Pruebas en Postman

| # | Método y URL | Token | Respuesta esperada |
|---|---|---|---|
| 1 | `POST /api/productos` con `"descripcion": "Quinua orgánica de productores de Puno, vendida a granel por kilo"` (sin `ecoEtiquetaIds`) | comerciante | **201** con Eco-Etiquetas "Orgánico", "Producto local", "Venta a granel" y `motorClasificacion` |
| 2 | `POST /api/productos` con `"descripcion": "Paquete de fideos tallarín"` | comerciante | **201** sin Eco-Etiquetas y `avisoEcoEtiquetas` (US13, escenario 2) |
| 3 | `POST /api/productos/{id}/clasificar` | dueño | **200** `{"producto": {...}, "motor": "keywords", "mensaje": "Se asignaron 2 Eco-Etiquetas"}` |
| 4 | `POST /api/productos/{id}/clasificar` | otro comerciante | **403** |
| 5 | `POST /api/productos/{id}/clasificar` | sin token | **401** |
| 6 | Con `GEMINI_API_KEY` inválida o sin internet, repetir la 3 | dueño | **200** con `"motor": "keywords"` y en consola `Gemini no respondió, se usan las palabras clave` (TS06, escenario 3) |
| 7 | `DELETE /api/eco-etiquetas/{id}` de una Eco-Etiqueta asignada | admin | **200** "se desactivó y se conserva en esos productos" (TS05, escenario 3) |
| 8 | `GET /api/eco-etiquetas` | sin token | ya no aparece la desactivada; `?incluirInactivas=true` sí la muestra |
| 9 | `PUT /api/productos/{id}` con `"ecoEtiquetaIds": [1, 3]` | dueño | **200** con `motorClasificacion: "manual"` (US16) |

**Resultado obtenido (2026-10-03):** pruebas 1 a 8 correctas. Sin clave el motor fue `keywords`; con una clave falsa
Gemini respondió 400 y la API usó las palabras clave. La prueba con `"motor": "gemini"` necesita una API key real.
