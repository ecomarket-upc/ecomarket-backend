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
