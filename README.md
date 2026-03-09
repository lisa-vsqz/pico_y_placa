# Pico y Placa — Sistema de Consulta de Restricción Vehicular

[![CI Pipeline](https://github.com/lisa-vsqz/pico_y_placa/actions/workflows/ci.yml/badge.svg)](https://github.com/lisa-vsqz/pico_y_placa/actions/workflows/ci.yml)

Sistema web full-stack para consultar si un vehículo puede circular en Quito, Ecuador, según las reglas de **Pico y Placa**, basándose en el último dígito de la placa, la fecha y la hora.

**Stack tecnológico:** Angular 18 · Spring Boot 3.2 · Java 17 · Docker

---

## Tabla de Contenidos

- [Descripción](#descripción)
- [Reglas de Negocio](#reglas-de-negocio)
- [Arquitectura](#arquitectura)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Decisiones Técnicas](#decisiones-técnicas)
- [Requisitos Previos](#requisitos-previos)
- [Ejecución Local](#ejecución-local)
- [API Overview](#api-overview)
- [Testing](#testing)
- [Testing Adversarial y Casos Borde](#testing-adversarial-y-casos-borde)
- [Seguridad](#seguridad)
- [Despliegue](#despliegue)
- [CI/CD](#cicd)
- [Mejoras Futuras](#mejoras-futuras)

---

## Descripción

El sistema permite a un usuario:

1. Ingresar el **número de placa** de un vehículo (formato ecuatoriano: `ABC-1234`)
2. Seleccionar una **fecha y hora** futura
3. Obtener un resultado indicando si el vehículo **puede o no circular**

La lógica de restricción está completamente **externalizada en archivos de configuración**, permitiendo modificar reglas (días, dígitos, horarios) sin alterar el código fuente.

---

## Reglas de Negocio

| Día              | Último dígito restringido |
| ---------------- | ------------------------- |
| Lunes            | 1, 2                      |
| Martes           | 3, 4                      |
| Miércoles        | 5, 6                      |
| Jueves           | 7, 8                      |
| Viernes          | 9, 0                      |
| Sábado / Domingo | Sin restricción           |

**Horarios de restricción:**

- Mañana: **07:00 – 09:30**
- Tarde: **16:00 – 19:30**

Un vehículo **NO puede circular** si se cumplen **ambas** condiciones:

1. El último dígito de su placa coincide con el día de restricción
2. La hora consultada cae dentro de alguna franja horaria restringida

---

## Arquitectura

```
┌──────────────────┐        HTTP / REST         ┌──────────────────┐
│                  │  POST /api/v1/pico-y-placa │                  │
│   Angular 18     │ ────────────────────────── │  Spring Boot 3.2 │
│   (Frontend)     │ <──────────────────────── │  (Backend API)   │
│                  │       JSON Response        │                  │
└──────────────────┘                            └──────────────────┘
```

| Capa                          | Responsabilidades                                                                            |
| ----------------------------- | -------------------------------------------------------------------------------------------- |
| **Frontend (Angular 18)**     | Interfaz de usuario, validación de formulario, presentación de resultados                    |
| **Backend (Spring Boot 3.2)** | Lógica de negocio, validación de datos, configuración de reglas, documentación API (Swagger) |

- **Comunicación:** REST sobre HTTP con JSON
- **Sin base de datos:** El sistema es una consulta computacional pura sin estado que persistir
- **Sin autenticación:** No requerida por el alcance del problema

---

## Estructura del Proyecto

```
pico_y_placa/
├── README.md
├── .gitignore
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/com/picoyplaca/
│       │   ├── PicoYPlacaApplication.java          # Punto de entrada
│       │   ├── config/
│       │   │   ├── PicoYPlacaProperties.java       # Reglas externalizadas (@ConfigurationProperties)
│       │   │   ├── CorsConfig.java                 # Configuración CORS
│       │   │   ├── OpenApiConfig.java              # Swagger / OpenAPI
│       │   │   └── SecurityHeadersFilter.java      # Cabeceras de seguridad HTTP
│       │   ├── controller/
│       │   │   └── PicoYPlacaController.java       # Endpoint REST
│       │   ├── service/
│       │   │   └── PicoYPlacaService.java          # Lógica de negocio
│       │   ├── domain/dto/
│       │   │   ├── PicoYPlacaRequest.java          # DTO de entrada (record)
│       │   │   ├── PicoYPlacaResponse.java         # DTO de salida (record)
│       │   │   └── ErrorResponse.java              # DTO estándar de error
│       │   └── exception/
│       │       ├── DateInPastException.java        # Excepción de negocio
│       │       └── GlobalExceptionHandler.java     # Manejo centralizado de errores
│       ├── main/resources/
│       │   ├── application.properties              # Configuración + reglas Pico y Placa
│       │   └── application-prod.properties         # Overrides para producción
│       └── test/java/com/picoyplaca/
│           ├── PicoYPlacaApplicationTest.java      # Test de contexto
│           ├── service/PicoYPlacaServiceTest.java  # Tests unitarios del servicio (50+)
│           └── controller/PicoYPlacaControllerTest.java  # Tests de integración
└── frontend/
    ├── package.json
    ├── angular.json
    ├── tsconfig.json
    └── src/
        ├── index.html
        ├── main.ts
        ├── styles.css
        ├── environments/
        │   ├── environment.ts                      # Config desarrollo
        │   └── environment.prod.ts                 # Config producción
        └── app/
            ├── app.component.ts                    # Componente raíz (standalone)
            ├── app.config.ts                       # Configuración Angular
            ├── models/
            │   └── pico-y-placa.model.ts           # Interfaces TypeScript
            ├── services/
            │   └── pico-y-placa.service.ts         # Servicio HTTP
            └── components/
                └── pico-y-placa-form/
                    ├── pico-y-placa-form.component.ts
                    ├── pico-y-placa-form.component.html
                    ├── pico-y-placa-form.component.css
                    └── pico-y-placa-form.component.spec.ts
```

---

## Decisiones Técnicas

| Decisión                                    | Justificación                                                              |
| ------------------------------------------- | -------------------------------------------------------------------------- |
| **Spring Boot 3.2 + Java 17**               | LTS estable, ecosistema maduro para APIs REST                              |
| **Angular 18 Standalone**                   | Enfoque moderno sin NgModules, menor boilerplate                           |
| **Records para DTOs**                       | Inmutabilidad, semántica clara, menos código (Java 17+)                    |
| **Reglas en `application.properties`**      | Principio Open/Closed — modificar reglas sin recompilar                    |
| **POST en vez de GET**                      | La placa es dato sensible; GET la expondría en URL y logs                  |
| **Zona horaria fija (`America/Guayaquil`)** | Consistencia — Pico y Placa aplica en hora local de Quito                  |
| **Sin base de datos**                       | Consulta computacional pura; no hay estado que persistir                   |
| **Validación doble (front + back)**         | Defensa en profundidad; el frontend valida UX, el backend valida seguridad |
| **`@RestControllerAdvice` global**          | Manejo centralizado de errores con respuestas consistentes                 |
| **Logging con placa enmascarada**           | Seguridad: registra `PBX-***4` en vez de `PBX-1234`                        |

---

## Requisitos Previos

| Herramienta | Versión mínima |
| ----------- | -------------- |
| Java (JDK)  | 17             |
| Maven       | 3.8+           |
| Node.js     | 18+            |
| npm         | 9+             |

---

## Ejecución Local

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

El backend inicia en `http://localhost:8080`.

Verificar estado:

```bash
curl http://localhost:8080/actuator/health
# → {"status":"UP"}
```

### Frontend

```bash
cd frontend
npm install
ng serve
```

El frontend inicia en `http://localhost:4200`.

### Documentación Swagger

Con el backend en ejecución:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

---

## API Overview

### `POST /api/v1/pico-y-placa`

Verifica si un vehículo puede circular según las reglas de Pico y Placa.

**Request:**

```json
{
  "plateNumber": "PBX-1234",
  "dateTime": "2026-03-11T08:00:00"
}
```

**Response (200):**

```json
{
  "plateNumber": "PBX-1234",
  "dateTime": "2026-03-11T08:00:00",
  "canDrive": false,
  "message": "El vehículo con placa PBX-1234 NO puede circular el miércoles 11/03/2026 a las 08:00. Restricción activa para dígitos 3, 4 en horario 07:00–09:30."
}
```

**Validaciones del request:**

- `plateNumber`: obligatorio, formato ecuatoriano (`^[A-Za-z]{3}-?\d{3,4}$`)
- `dateTime`: obligatorio, no puede ser anterior a la fecha/hora actual

**Códigos de respuesta:**

| Código | Significado                                         |
| ------ | --------------------------------------------------- |
| `200`  | Consulta procesada exitosamente                     |
| `400`  | Datos inválidos (placa malformada, JSON incorrecto) |
| `422`  | Fecha en el pasado                                  |
| `405`  | Método HTTP no soportado                            |
| `500`  | Error interno (sin exposición de detalles)          |

---

## Testing

### Backend — 69 tests

```bash
cd backend
mvn test
```

**Cobertura de tests:**

| Categoría                    | Tests | Descripción                                              |
| ---------------------------- | ----- | -------------------------------------------------------- |
| Restricción por día y dígito | 11    | Cada día de la semana con dígito correcto e incorrecto   |
| Franjas horarias             | 8     | Bordes exactos: 06:59, 07:00, 09:30, 09:31, 16:00, 19:30 |
| Fines de semana              | 20    | Los 10 dígitos (0–9) × sábado y domingo                  |
| Validación de fechas         | 1     | Fecha pasada → excepción                                 |
| Integración del servicio     | 2     | Flujo completo request → response                        |
| Formato de placa             | 8     | Placas válidas e inválidas con `@CsvSource`              |
| Controller (integración)     | 18    | Validación, errores, métodos HTTP, seguridad             |

### Frontend

```bash
cd frontend
ng test
```

Tests de componentes con Jasmine/Karma: validación de formulario, formato de placa, fecha pasada, reset.

---

## Testing Adversarial y Casos Borde

El sistema incluye tests específicos para validar robustez ante entradas maliciosas o inesperadas:

### Inyección y XSS

- Placas con contenido `<script>alert('xss')</script>` → rechazadas con `400 Bad Request`
- Placas con intento de SQL injection (`'; DROP TABLE--`) → rechazadas por regex estricto

### Formatos inválidos

- Placa vacía, placa con solo letras, placa con caracteres especiales → `400`
- JSON malformado o body vacío → `400`
- Strings excesivamente largos (>1000 caracteres) → rechazados por validación

### Casos borde de horario

- **06:59** → fuera de restricción (puede circular)
- **07:00** → inicio exacto de restricción (no puede circular)
- **09:30** → último minuto de restricción (no puede circular)
- **09:31** → fuera de restricción (puede circular)
- Mismo patrón para la franja de tarde (16:00–19:30)

### Fines de semana

- Los 10 dígitos posibles (0–9) verificados para sábado y domingo → todos pueden circular

### Métodos HTTP no soportados

- `GET`, `PUT`, `DELETE` al endpoint → `405 Method Not Allowed`

El objetivo de estos tests es garantizar que el sistema responde de forma predecible y segura ante cualquier entrada, sin exponer información interna ni generar errores no controlados.

---

## Seguridad

**Protecciones implementadas:**

| Medida                 | Detalle                                                                                         |
| ---------------------- | ----------------------------------------------------------------------------------------------- |
| POST (no GET)          | La placa no se expone en URL ni logs de servidores intermedios                                  |
| Regex estricto         | Solo acepta `^[A-Z]{3}-?\d{3,4}$`; cualquier otra entrada es rechazada                          |
| Logging enmascarado    | Los logs registran `PBX-***4` en vez de la placa completa                                       |
| Sin persistencia       | La placa no se almacena en ningún medio                                                         |
| Cabeceras de seguridad | `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Referrer-Policy`, `Cache-Control: no-store` |
| Errores controlados    | `GlobalExceptionHandler` nunca expone stack traces; errores 500 son genéricos                   |
| Sin cookies/sesión     | API completamente stateless                                                                     |

---

## Despliegue

El proyecto está preparado para desplegarse en **Render.com** con la siguiente arquitectura:

| Componente   | Tipo en Render       | Directorio raíz | Tecnología                                      |
| ------------ | -------------------- | --------------- | ----------------------------------------------- |
| **Backend**  | Web Service (Docker) | `backend/`      | Dockerfile multi-stage con `eclipse-temurin:17` |
| **Frontend** | Static Site          | `frontend/`     | Build Angular (`npm run build:prod`)            |

**Características del Dockerfile:**

- Build multi-stage (JDK para compilar, JRE para ejecutar)
- Ejecución con usuario no-root
- Health check integrado via `/actuator/health`
- Configuración por variables de entorno (`SERVER_PORT`, `SPRING_PROFILES_ACTIVE`, `CORS_ALLOWED_ORIGINS`)

**Variables de entorno requeridas en producción:**

| Variable                 | Descripción                 |
| ------------------------ | --------------------------- |
| `SPRING_PROFILES_ACTIVE` | `prod`                      |
| `CORS_ALLOWED_ORIGINS`   | URL del frontend desplegado |
| `APP_TIMEZONE`           | `America/Guayaquil`         |

---

## CI/CD

El proyecto cuenta con un pipeline de **Integración Continua** implementado con **GitHub Actions** (`.github/workflows/ci.yml`) que se ejecuta automáticamente en cada push a `main` y en pull requests.

| Job                        | Descripción                                                                        | Herramientas            |
| -------------------------- | ---------------------------------------------------------------------------------- | ----------------------- |
| **Backend Build & Test**   | Compila el proyecto Spring Boot y ejecuta los 69 tests unitarios e integración     | Java 17, Maven          |
| **Frontend Build & Test**  | Instala dependencias, genera el bundle de producción y ejecuta tests               | Node.js 20, Angular CLI |
| **Security Scan (CodeQL)** | Análisis estático de seguridad (SAST) sobre código Java y TypeScript               | GitHub CodeQL           |
| **Dependency Scan**        | Escaneo de vulnerabilidades en dependencias con OWASP Dependency Check y npm audit | OWASP, npm              |
| **Docker Build**           | Valida que el Dockerfile del backend construye correctamente la imagen             | Docker                  |

**Paralelismo:** Los jobs de backend, frontend, seguridad y dependencias se ejecutan en paralelo. El build de Docker espera a que el backend compile exitosamente.

**Resultados de seguridad:** Los hallazgos de CodeQL aparecen en la pestaña **Security → Code scanning alerts** del repositorio en GitHub. El reporte OWASP se genera como artefacto descargable del workflow.

---

## Mejoras Futuras

| Mejora                 | Descripción                                            |
| ---------------------- | ------------------------------------------------------ |
| Calendario de feriados | Lista configurable de fechas sin restricción           |
| Soporte multi-ciudad   | Selector de ciudad; las reglas ya son configurables    |
| Rate limiting          | Prevención de abuso en el endpoint público             |
| PWA                    | Uso offline aprovechando soporte nativo de Angular     |
| Tests E2E              | Integración completa frontend ↔ backend con Playwright |
| Métricas               | Monitoreo con Micrometer/Prometheus                    |

---

## Licencia

MIT License
