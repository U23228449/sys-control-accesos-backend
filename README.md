# EstacionaAPI — Sistema de Control de Acceso Vehicular

Backend RESTful para el control de ingreso y salida de vehículos en el campus universitario. Construido con **Java 21**, **Spring Boot 3.5**, **Spring Security (JWT)** y **PostgreSQL 15**.

> **Estado del proyecto**: ✅ **100% Completado** — 16 Historias de Usuario implementadas · **161 tests en verde** (79 unitarios + 82 integración)

---

## Tabla de Contenidos

1. [Requisitos Previos](#1-requisitos-previos)
2. [Clonar el Repositorio](#2-clonar-el-repositorio)
3. [Configurar PostgreSQL](#3-configurar-postgresql)
4. [Crear el archivo `.env`](#4-crear-el-archivo-env)
5. [Ejecutar el Proyecto](#5-ejecutar-el-proyecto)
6. [Verificar que Funciona](#6-verificar-que-funciona)
7. [Ejecutar los Tests](#7-ejecutar-los-tests)
8. [Guía Rápida de la API con Postman](#8-guía-rápida-de-la-api-con-postman)
9. [Roles y Permisos](#9-roles-y-permisos)
10. [Estructura del Proyecto](#10-estructura-del-proyecto)
11. [Solución de Problemas Comunes](#11-solución-de-problemas-comunes)

---

## 1. Requisitos Previos

Antes de empezar, asegúrate de tener instalado lo siguiente:

| Herramienta | Versión mínima | Cómo verificar |
|---|---|---|
| **JDK (Java)** | 21 | `java -version` |
| **Maven** | 3.9+ | `mvn -v` *(o usa el wrapper `./mvnw`)* |
| **PostgreSQL** | 15 | `psql --version` |
| **Git** | cualquiera | `git --version` |
| **Docker** | cualquiera *(solo para tests)* | `docker --version` |
| **Postman** | cualquiera | — |

> **Nota sobre Docker**: Solo es necesario para ejecutar los **tests de integración** (Testcontainers levanta PostgreSQL automáticamente en un contenedor). Para simplemente correr la aplicación, Docker **no es obligatorio**.

---

## 2. Clonar el Repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd <nombre-de-la-carpeta>
```

---

## 3. Configurar PostgreSQL

La aplicación apunta a PostgreSQL en `localhost:5433` (puerto **5433**, no el 5432 por defecto). Esto se puede cambiar en el `.env` si tu instalación usa otro puerto.

### Paso 3.1 — Crear la base de datos

Abre **pgAdmin** o una terminal `psql` y ejecuta:

```sql
CREATE DATABASE estaciona_db;
```

> El nombre debe ser exactamente `estaciona_db`. La URL de conexión está configurada como `jdbc:postgresql://localhost:5433/estaciona_db`.

### Paso 3.2 — Crear el usuario (opcional pero recomendado)

Si quieres usar un usuario dedicado en lugar del superusuario `postgres`:

```sql
CREATE USER estaciona_user WITH PASSWORD 'tu_contraseña_segura';
GRANT ALL PRIVILEGES ON DATABASE estaciona_db TO estaciona_user;
```

Luego usa `estaciona_user` y su contraseña en el `.env`.

### Paso 3.3 — ¿El esquema y los datos iniciales?

**No hace falta ejecutar nada manualmente.** Al iniciar la aplicación por primera vez, **Flyway** aplica automáticamente todas las migraciones en orden:

| Migración | Descripción |
|---|---|
| `V1__init_schema.sql` | Crea todas las tablas (`usuarios`, `vehiculos`, `accesos_vehiculares`, `zonas_estacionamiento`, etc.) |
| `V2__seed_data.sql` | Inserta los roles, el campus y el usuario administrador por defecto |
| `V3__auditoria_triggers.sql` | Crea triggers PL/pgSQL append-only para auditoría automática |
| `V4__reportes_indices_vista.sql` | Crea índices compuestos y la vista `vw_reporte_accesos_vehiculares` |
| `V5__configuraciones.sql` | Crea la tabla `configuraciones` e inserta los parámetros del sistema |
| `V6__reporte_zonas_vista.sql` | Crea la vista `vw_reporte_zonas_disponibilidad` para el reporte de zonas |

> **Importante**: Si PostgreSQL no está corriendo cuando inicies la app, verás el error `Connection refused`. Asegúrate de que el servicio de PostgreSQL esté activo antes de ejecutar `./mvnw spring-boot:run`.

---

## 4. Crear el archivo `.env`

> El archivo `.env` **no está en el repositorio** (está en `.gitignore`) porque contiene credenciales. Cada miembro del equipo debe crearlo manualmente.

En la **raíz del proyecto** (junto a `pom.xml`), crea un archivo llamado `.env` con el siguiente contenido:

```env
# Credenciales de la base de datos PostgreSQL
DB_USER=postgres
DB_PASSWORD=tu_contraseña_de_postgres

# Clave secreta para firmar los tokens JWT
# DEBE tener al menos 32 caracteres para el algoritmo HS256
JWT_SECRET=YLvZ+nDSckT754d4MV5Bzi5D0q+qO7kOhMFxGb+d9Yw=
```
¿Cómo generar un `JWT_SECRET` seguro?
Para que tu clave sea criptográficamente segura (se recomiendan al menos 32 bytes codificados en Base64 para HS256), puedes generar una rápidamente ejecutando uno de estos comandos en tu terminal:

**Opción A: Usando Git Bash (yo la usé)**
```bash
openssl rand -base64 64
```
Opción B: Usando PowerShell 
```powershell
$bytes = New-Object Byte[] 64; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes); [Convert]::ToBase64String($bytes)
```

### Variables de entorno explicadas

| Variable | Descripción                                                                                                                          | Valor de ejemplo |
|---|--------------------------------------------------------------------------------------------------------------------------------------|---|
| `DB_USER` | usuario de PostgreSQL                                                                                                                | `postgres` |
| `DB_PASSWORD` | contraseña del usuario de PostgreSQL                                                                                                 | `admin` |
| `JWT_SECRET` | clave para firmar/verificar los tokens JWT. **No cambiar entre reinicios de la app** o los tokens existentes dejarán de ser válidos. | *(la del ejemplo)* |

### ¿Cómo se usa el `.env`?

Spring Boot lee las variables del `.env` automáticamente en la mayoría de IDEs (IntelliJ con el plugin EnvFile). Si usas la **línea de comandos**, debes exportarlas primero:

**En Windows (PowerShell):**
```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim())
    }
}
```

**En Linux / macOS:**
```bash
export $(grep -v '^#' .env | xargs)
```

> **Alternativa más sencilla**: Copia las variables del `.env` directamente en la configuración de tu IDE. En IntelliJ: `Run > Edit Configurations > Environment variables`.

---

## 5. Ejecutar el Proyecto

Dado que el entorno de desarrollo oficial para este proyecto es **IntelliJ IDEA**, la ejecución se realiza directamente desde la interfaz del editor.

### Paso 5.1 — Cargar las variables de entorno
Para que IntelliJ reconozca las credenciales de tu base de datos y la clave JWT, debes indicarle dónde están esas variables:
1. Ve al menú superior y selecciona **`Run` > `Edit Configurations...`**
2. Selecciona la configuración de tu aplicación Spring Boot (`EstacionaApiApplication`).
3. Busca el campo **Environment variables** y pega ahí las variables de tu `.env` separadas por punto y coma.
   *(Ejemplo: `DB_USER=postgres;DB_PASSWORD=tu_contraseña;JWT_SECRET=tu_clave_secreta`)*.
   > **Tip:** Si tienes instalado el plugin **EnvFile** en IntelliJ, simplemente ve a la pestaña "EnvFile", habilítalo y selecciona el archivo `.env` de la raíz del proyecto.

### Paso 5.2 — Arrancar la aplicación
1. En el panel izquierdo del proyecto, navega hasta: `src/main/java/com/estaciona/api/`
2. Abre la clase principal **`EstacionaApiApplication.java`**.
3. Haz clic en el botón de Play verde que aparece en el margen izquierdo junto al método `main()` y selecciona **Run 'EstacionaApiApplication'**.

La API arrancará en: **`http://localhost:8080`**

Verás en la consola inferior de IntelliJ algo como:
```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot :: (v3.5.x)

...
Tomcat started on port 8080 (http) with context path '/'
Started EstacionaApiApplication in X.XXX seconds
```

### Documentación interactiva (Swagger UI)

Una vez corriendo, abre en el navegador:
```
http://localhost:8080/swagger-ui.html
```

Aquí puedes explorar y probar todos los endpoints sin necesidad de Postman.

---

## 6. Verificar que Funciona

Prueba el endpoint de salud (no requiere autenticación):

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{ "status": "UP" }
```

---

## 7. Ejecutar los Tests

> Los tests de integración requieren **Docker corriendo** (Testcontainers levanta PostgreSQL automáticamente en un contenedor temporal).

**Ejecutar toda la suite (161 tests):**
```powershell
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

**Ejecutar solo los tests unitarios (rápidos, sin Docker):**
```powershell
.\mvnw.cmd test "-Dtest=!*IT" -DfailIfNoTests=false
```

**Ejecutar solo los tests de integración:**
```powershell
.\mvnw.cmd test "-Dtest=*IT" -DfailIfNoTests=false
```

**Resultado esperado:**
```
[Unitarios]   Tests run: 79,  Failures: 0, Errors: 0, Skipped: 0
[Integración] Tests run: 82,  Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 8. Guía Rápida de la API con Postman

### Paso 1 — Obtener el Token JWT (Login)

Todos los endpoints (excepto `/login`) requieren un token JWT en el header.

```
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "identificador": "admin@unicampus.edu.pe",
  "password": "Admin123!"
}
```

**Respuesta:**
```json
{
  "status": "success",
  "message": "Sesión iniciada correctamente.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tipoToken": "Bearer",
    "expiraEn": 3600,
    "usuario": {
      "id": "c1f75b7b-23eb-4680-9993-3ea79b183669",
      "nombreCompleto": "Administrador General",
      "correo": "admin@unicampus.edu.pe",
      "rol": "ADMINISTRADOR"
    }
  }
}
```

Copia el `token`. En cada request siguiente, agrega el header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

> En Postman: pestaña **Authorization** → tipo **Bearer Token** → pega el token.

---

### Endpoints Principales

#### Registrar Usuario *(rol: ADMINISTRADOR)*
```
POST /api/v1/usuarios
Authorization: Bearer <token>

{
  "nombreCompleto": "María García",
  "correo": "maria@unicampus.edu.pe",
  "documento": "USR001",
  "password": "Seguro123!",
  "rolId": 4,
  "tipoUsuario": "estudiante"
}
```

#### Registrar Vehículo *(cualquier usuario autenticado)*
```
POST /api/v1/vehiculos
Authorization: Bearer <token>

{
  "tipo": "auto",
  "placa": "ABC123",
  "marcaModelo": "Toyota Corolla 2022",
  "color": "Blanco"
}
```
> `tipo` acepta: `"auto"` o `"moto"`. La placa se normaliza a MAYÚSCULAS automáticamente.

#### Consultar Mis Vehículos *(cualquier usuario autenticado)*
```
GET /api/v1/vehiculos/me
Authorization: Bearer <token>
```

#### Buscar Vehículo por Placa *(rol: SEGURIDAD)*
```
GET /api/v1/vehiculos/buscar/ABC123
Authorization: Bearer <token_seguridad>
```

#### Registrar Ingreso *(rol: SEGURIDAD)*
```
POST /api/v1/accesos-vehiculares
Authorization: Bearer <token_seguridad>

{
  "placa": "ABC123",
  "zonaId": 1
}
```

#### Registrar Salida *(rol: SEGURIDAD)*
```
PATCH /api/v1/accesos-vehiculares/{id}/salida
Authorization: Bearer <token_seguridad>
```
> Reemplaza `{id}` con el UUID devuelto al registrar el ingreso.

#### Actualizar Mi Perfil *(rol: USUARIO)*
```
PUT /api/v1/usuarios/me
Authorization: Bearer <token_usuario>

{
  "nombreCompleto": "Ana Torres",
  "correo": "ana@unicampus.edu.pe",
  "documento": "USR002",
  "passwordActual": "Seguro123!",
  "passwordNuevo": "NuevoPass456!"
}
```

#### Cambiar Rol de un Usuario *(rol: ADMINISTRADOR)*
```
PUT /api/v1/usuarios/{id}/rol
Authorization: Bearer <token_admin>

{ "rolId": 3 }
```

#### Cambiar Estado de un Usuario *(rol: ADMINISTRADOR)*
```
PATCH /api/v1/usuarios/{id}/estado
Authorization: Bearer <token_admin>

{ "enabled": false }
```

#### Consultar Usuarios (paginado con filtros) *(rol: ADMINISTRADOR)*
```
GET /api/v1/usuarios?rol=USUARIO&enabled=true&page=0&size=10
Authorization: Bearer <token_admin>
```

#### Eliminar Usuario (soft-delete) *(rol: ADMINISTRADOR)*
```
DELETE /api/v1/usuarios/{id}
Authorization: Bearer <token_admin>
```

#### Actualizar Zona *(rol: ADMINISTRADOR)*
```
PUT /api/v1/zonas-estacionamiento/{id}
Authorization: Bearer <token_admin>

{
  "nombre": "Zona B Ampliada",
  "ubicacion": "Pabellón B",
  "tipo": "autos",
  "aforoMaximo": 50
}
```

#### Cambiar Estado de Zona *(rol: ADMINISTRADOR)*
```
PATCH /api/v1/zonas-estacionamiento/{id}/estado
Authorization: Bearer <token_admin>

{ "estado": "cerrada" }
```

#### Consultar Zonas (paginado con filtros) *(rol: ADMINISTRADOR)*
```
GET /api/v1/zonas-estacionamiento?campus=1&estado=activa&page=0&size=10
Authorization: Bearer <token_admin>
```

#### Actualizar Vehículo *(propietario autenticado)*
```
PUT /api/v1/vehiculos/{id}
Authorization: Bearer <token_usuario>

{
  "tipo": "auto",
  "marcaModelo": "Toyota Yaris 2023",
  "color": "Rojo"
}
```

#### Eliminar Vehículo (soft-delete) *(propietario autenticado)*
```
DELETE /api/v1/vehiculos/{id}
Authorization: Bearer <token_usuario>
```

#### Ver Configuraciones del Sistema *(rol: ADMINISTRADOR)*
```
GET /api/v1/configuraciones
Authorization: Bearer <token_admin>
```

#### Actualizar Parámetro del Sistema *(rol: ADMINISTRADOR)*
```
PATCH /api/v1/configuraciones/{clave}
Authorization: Bearer <token_admin>

{ "valor": "90" }
```

#### Ver Reporte de Accesos *(rol: COORDINADOR_SEGURIDAD)*
```
GET /api/v1/reportes/accesos?page=0&size=10
Authorization: Bearer <token_coordinador>
```

#### Exportar Reporte de Accesos a Excel *(rol: COORDINADOR_SEGURIDAD)*
```
GET /api/v1/reportes/accesos/exportar
Authorization: Bearer <token_coordinador>
```
> En Postman: **Send → Save Response → Save to a file** y guarda con extensión `.xlsx`.

#### Ver Reporte de Disponibilidad de Zonas *(rol: ADMINISTRADOR)*
```
GET /api/v1/reportes/zonas?campusId=1
Authorization: Bearer <token_admin>
```

#### Exportar Reporte de Zonas a Excel *(rol: ADMINISTRADOR)*
```
GET /api/v1/reportes/zonas/exportar
Authorization: Bearer <token_admin>
```

---

### Códigos de Respuesta Comunes

| Código | Significado |
|---|---|
| `200 OK` | Operación exitosa |
| `201 Created` | Recurso creado correctamente |
| `400 Bad Request` | Datos de entrada inválidos (validación) |
| `401 Unauthorized` | Token no enviado, inválido o expirado |
| `403 Forbidden` | Tu rol no tiene permiso para este endpoint |
| `404 Not Found` | Recurso no encontrado (placa, ID, etc.) |
| `409 Conflict` | Recurso duplicado (placa ya existe, correo ya existe) |
| `422 Unprocessable Entity` | Regla de negocio violada (zona sin aforo, acceso ya en curso) |

---

## 9. Roles y Permisos

| Rol | `rolId` | Permisos |
|---|---|---|
| `ADMINISTRADOR` | `1` | Registrar/actualizar/eliminar usuarios, gestionar zonas, ver configuraciones del sistema, reporte de zonas |
| `COORDINADOR_SEGURIDAD` | `2` | Generar y exportar reportes de accesos vehiculares |
| `SEGURIDAD` | `3` | Registrar ingresos/salidas, buscar vehículos por placa |
| `USUARIO` | `4` | Registrar/actualizar/eliminar sus propios vehículos, editar su propio perfil |

**Usuario administrador creado por el seed inicial:**
- Correo: `admin@unicampus.edu.pe`
- Contraseña: `Admin123!`

---

## 10. Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/estaciona/api/
│   │   ├── config/          # Seguridad, OpenAPI, JPA Auditing
│   │   ├── security/        # Filtro JWT, token provider, RBAC utils
│   │   ├── common/          # Excepciones globales, DTO ApiResponse
│   │   └── modules/
│   │       ├── auth/                # Login y generación de JWT
│   │       ├── usuarios/            # Registro, actualización, eliminación
│   │       │   ├── update/          # Strategies de actualización (Strategy + Factory)
│   │       │   ├── eliminacion/     # Strategies de eliminación soft-delete
│   │       │   ├── spec/            # Specifications para filtros dinámicos
│   │       │   └── dto/             # DTOs de request y response
│   │       ├── roles/               # Entidad Rol (seed en BD)
│   │       ├── vehiculos/           # Registro, actualización, eliminación
│   │       │   └── eliminacion/     # Strategies de eliminación
│   │       ├── campus/              # Campus universitarios
│   │       ├── zonas/               # Zonas de estacionamiento
│   │       │   ├── update/          # Strategies de actualización (Strategy + Factory)
│   │       │   └── spec/            # Specifications para filtros dinámicos
│   │       ├── accesos/             # Control de ingreso/salida vehicular
│   │       │   ├── validation/      # 3 strategies de validación de acceso
│   │       │   └── spec/            # Specifications para historial filtrable
│   │       ├── auditoria/           # Log de auditoría automático (triggers)
│   │       ├── configuraciones/     # Parámetros del sistema con Strategy + Observer
│   │       │   ├── strategy/        # 4 strategies de validación por tipo
│   │       │   └── event/           # ConfiguracionCambiadaEvent (Observer)
│   │       └── reportes/            # Reportes paginados y exportación Excel/PDF
│   │           ├── excel/           # Builder de Excel (Builder Pattern)
│   │           └── strategy/        # ExcelExportacionStrategy / ZonaExcelStrategy
│   └── resources/
│       ├── application.yml          # Configuración de Spring Boot
│       └── db/migration/            # Scripts Flyway (V1 a V6)
└── test/
    └── java/com/estaciona/api/
        ├── support/                 # AbstractIntegrationTest (Testcontainers)
        └── modules/                 # Tests unitarios (*Test) e integración (*IT)
            ├── usuarios/            # UsuarioServiceTest + UsuarioControllerIT
            ├── vehiculos/           # VehiculoServiceTest + VehiculoControllerIT
            ├── zonas/               # ZonaServiceTest + ZonaControllerIT
            ├── accesos/             # AccesoVehicularServiceTest + AccesoVehicularControllerIT
            ├── configuraciones/     # ConfiguracionServiceTest + ConfiguracionControllerIT
            ├── reportes/            # ReporteAccesoServiceTest + ReporteControllerIT
            ├── auditoria/           # AuditoriaServiceTest + AuditoriaControllerIT
            └── auth/                # AuthServiceTest + AuthControllerIT
```

---

## 11. Solución de Problemas Comunes

### `Connection refused` al iniciar

**Causa**: PostgreSQL no está corriendo o el puerto es incorrecto.

**Solución**:
1. Verifica que PostgreSQL esté activo: en Windows abre el panel de servicios (`services.msc`) y busca el servicio de PostgreSQL.
2. Confirma que el puerto en `application.yml` coincide con el de tu instalación. La config usa `localhost:5433`. Si tu instalación usa el puerto estándar `5432`, cambia la URL:
   ```yaml
   # En src/main/resources/application.yml
   url: jdbc:postgresql://localhost:5432/estaciona_db
   ```

### `Password authentication failed for user "postgres"`

**Causa**: La contraseña en el `.env` no coincide con la de PostgreSQL.

**Solución**: Edita el `.env` y pon la contraseña correcta de tu instalación local de PostgreSQL en `DB_PASSWORD`.

### `FlywayException: Found non-empty schema`

**Causa**: La base de datos ya tiene tablas pero sin el historial de Flyway.

**Solución**: Borra la base de datos y recréala vacía:
```sql
DROP DATABASE estaciona_db;
CREATE DATABASE estaciona_db;
```
Luego reinicia la aplicación y Flyway aplicará las migraciones desde cero.

### `JWT_SECRET` no se carga / `IllegalArgumentException: secret key`

**Causa**: Las variables del `.env` no están siendo leídas por la JVM.

**Solución**: Configúralas manualmente en tu IDE (IntelliJ: `Run > Edit Configurations > Environment variables`) o expórtalas en la terminal antes de ejecutar `./mvnw spring-boot:run`.

### Tests fallan con `Could not find a valid Docker environment`

**Causa**: Docker no está corriendo. Es requerido por Testcontainers para los tests de integración (`*IT`).

**Solución**: Inicia Docker Desktop y vuelve a ejecutar los tests. Si no tienes Docker, puedes ejecutar solo los tests unitarios (no requieren Docker):
```powershell
.\mvnw.cmd test "-Dtest=*Test"
```

### `401 Unauthorized` en todos los endpoints

**Causa**: No estás enviando el token JWT o ha expirado (duración: 60 minutos).

**Solución**: Haz login de nuevo con `POST /api/v1/auth/login` para obtener un token fresco y úsalo en el header `Authorization: Bearer <nuevo_token>`.

---

## Stack Tecnológico Completo

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5 |
| Seguridad | Spring Security + JJWT 0.12.6 (HS256) |
| Persistencia | Spring Data JPA + Hibernate 6 + JpaSpecificationExecutor |
| Base de datos | PostgreSQL 15 |
| Migraciones | Flyway (6 scripts: V1 → V6) |
| Hash de contraseñas | BCrypt (strength 12) |
| Reportes Excel | Apache POI 5.3.0 (SXSSFWorkbook) |
| Documentación API | SpringDoc OpenAPI / Swagger UI |
| Tests unitarios | JUnit 5 + Mockito · **79 tests** |
| Tests integración | JUnit 5 + Spring Boot Test + Testcontainers · **82 tests** |
| Patrones de diseño | Strategy, Factory Method, Builder, Observer, Repository, Template Method |
| Build | Maven (con wrapper `./mvnw`) |

---

## Historias de Usuario Implementadas

| HU | Descripción | Fases |
|---|---|---|
| HU-001 | Iniciar Sesión (JWT) | 1-3 |
| HU-002 | Registrar Usuario | 1-3 |
| HU-003 | Consultar Usuarios (filtros + paginación) | 5 |
| HU-004 | Actualizar Usuario (perfil propio, rol, estado) | 4 |
| HU-005 | Eliminar Usuario (soft-delete) | 6 |
| HU-006 | Registrar Vehículo | 1-3 |
| HU-007 | Buscar Vehículo por Placa | 1-3 |
| HU-008 | Actualizar Vehículo | 6 |
| HU-009 | Eliminar Vehículo (soft-delete) | 6 |
| HU-010 | Gestionar Zonas (crear, listar) | 1-3 |
| HU-011 | Consultar Zonas (filtros + paginación) | 5 |
| HU-012 | Actualizar Zona (datos + estado) | 4 |
| HU-013 | Registrar Ingreso Vehicular | 1-3 |
| HU-014 | Registrar Salida Vehicular | 1-3 |
| HU-015 | Consultar Historial de Accesos (filtros + paginación) | 5 |
| HU-016 | Auditoría automática (triggers append-only) | 1-3 |
| HU-017 | Configuraciones del Sistema (Observer) | 6 |
| HU-018 | Reporte de Accesos (Excel + PDF) | 1-3 |
| HU-019 | Reporte de Zonas (Excel) | 4 |
