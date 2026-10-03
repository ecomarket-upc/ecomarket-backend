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
