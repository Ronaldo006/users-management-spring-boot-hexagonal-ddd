# Users Management — Spring Boot, Arquitectura Hexagonal y DDD

Aplicación de gestión de usuarios construida con Java 17 y Spring Boot. La API REST es el punto de entrada activo. El código de la antigua CLI se conserva como adaptador inactivo y no posee un contenedor de dependencias independiente.

Spring es el único *composition root*: `Main` inicia el contexto y las dependencias se resuelven mediante configuración y component scanning de Spring.

## Verificación

```bash
./mvnw clean test
./mvnw clean package
```

En Windows se puede utilizar `mvnw.cmd`.

## Ejecución (taller de despliegue)

La base de datos se elige con la variable `DB_ENGINE` (`postgresql` por defecto, o `mysql`).
Todas las claves de `application.properties` se pueden sobrescribir con variables de entorno
(`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SSLMODE`, `SMTP_USERNAME`,
`SMTP_PASSWORD`, `JWT_SECRET`, `PORT`). La tabla `users` se crea sola al arrancar.

### Local con Maven
```bash
mvn clean install -U
# PostgreSQL (crear antes la BD crud_usuarios)
mvnw spring-boot:run
# MySQL
set DB_ENGINE=mysql& set DB_PORT=3306& set DB_USERNAME=root& mvnw spring-boot:run
```

### Docker
```bash
copy .env.example .env      # poner correo y clave de aplicación de Gmail
docker compose up --build                             # API + PostgreSQL
docker compose -f docker-compose.mysql.yml up --build # API + MySQL
```
Swagger: http://localhost:8080/swagger-ui/index.html

### Render
Web Service de tipo **Docker** apuntando a este repo, con las variables `DB_*` de la base
PostgreSQL (Render Postgres o Supabase con `DB_SSLMODE=require`), `SMTP_USERNAME`,
`SMTP_PASSWORD` y `JWT_SECRET`. También se puede usar el `render.yaml` (Blueprint).
