# Sistema de Gestión de Clientes y Pedidos

Este proyecto es una aplicación de microservicios full-stack diseñada para gestionar clientes y sus pedidos. Está compuesto por un backend robusto en **Java/Spring Boot**, un frontend en **Angular** , y una base de datos **Microsoft SQL Server**. Todo el ecosistema está contenerizado para facilitar su despliegue y desarrollo.

## 🚀 Tecnologías Utilizadas

### Backend (Microservicios)
- **Java 17**
- **Spring Boot 3.x**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Boot Validation
- **Seguridad**: JSON Web Tokens (JWT)
- **Documentación API**: Springdoc OpenAPI (Swagger)
- **Herramientas**: Maven, Lombok

### Base de Datos
- **Microsoft SQL Server 2022** (vía Docker)

### Infraestructura y Despliegue
- **Docker** y **Docker Compose**

### Pruebas
- **JUnit 5** y **Mockito** para pruebas unitarias.
- **Spring Boot Test** para pruebas de integración.

---

## 🏗 Arquitectura del Sistema

El backend está dividido en dos microservicios independientes:

1. **`auth-service` (Puerto 8081)**:
   - Responsable de la gestión de usuarios, registro, autenticación y emisión de tokens JWT.
   - Cuenta con su propia base de datos (`auth_db`).

2. **`customer-order` (Puerto 8082)**:
   - Gestiona el CRUD de Clientes (Customers) y Pedidos (Orders).
   - Proporciona estadísticas y actividad reciente para el Dashboard.
   - Protegido por autenticación JWT verificando los tokens emitidos por `auth-service`.
   - Cuenta con su propia base de datos (`customer_order_db`).

Ambos servicios se comunican con la misma instancia de SQL Server pero en bases de datos separadas, manteniendo el patrón de "Database per Service" de microservicios.

---

## 🔐 Seguridad y Credenciales

**Importante:** No hay credenciales (contraseñas de base de datos ni secretos JWT) expuestas en el código fuente. Se ha implementado el uso de variables de entorno para asegurar la protección de datos sensibles.

- Se ha configurado un archivo `.env` que Docker Compose lee para inyectar estas variables.
- Existe un archivo `.env.example` en la raíz del proyecto para que sirva de guía sobre las variables necesarias.
- El archivo `.env` está incluido en el `.gitignore` para prevenir subidas accidentales al repositorio.

---

## 🚦 Rutas y Endpoints (API)

La documentación interactiva de la API está disponible a través de Swagger UI cuando los servicios están en ejecución:
- **Auth Service**: `http://localhost:8081/swagger-ui.html`
- **Customer & Order Service**: `http://localhost:8082/swagger-ui.html`

### Endpoints Principales

#### `auth-service`
| Método | Endpoint | Descripción | Autenticación |
|---|---|---|---|
| POST | `/api/auth/register` | Registra un nuevo usuario | Pública |
| POST | `/api/auth/login` | Autentica y devuelve un JWT | Pública |
| GET | `/api/auth/validate` | Valida un token JWT existente | Requiere JWT |

#### `customer-order`
Todas las rutas de este servicio requieren un token JWT válido en la cabecera: `Authorization: Bearer <token>`

**Clientes (Customers)**
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/customers` | Obtiene lista de clientes |
| GET | `/api/customers/{id}` | Obtiene un cliente específico |
| POST | `/api/customers` | Crea un nuevo cliente |
| PUT | `/api/customers/{id}` | Actualiza un cliente |
| DELETE| `/api/customers/{id}` | Elimina un cliente |

**Pedidos (Orders)**
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/orders` | Obtiene lista de pedidos |
| GET | `/api/orders/{id}` | Obtiene un pedido específico |
| POST | `/api/orders` | Crea un nuevo pedido |
| PUT | `/api/orders/{id}` | Actualiza un pedido |
| DELETE| `/api/orders/{id}` | Elimina un pedido |

**Dashboard**
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/dashboard/stats` | Obtiene estadísticas generales (totales) |
| GET | `/api/dashboard/activity` | Obtiene actividad reciente de pedidos |

---

## 🧪 Pruebas (Testing)

El proyecto incluye una suite de pruebas para garantizar la calidad y robustez del código:

- **Pruebas Unitarias (`*Test.java`)**: Se prueban los controladores y servicios de forma aislada utilizando *Mockito* para simular dependencias (por ejemplo, validando la lógica de autenticación en `AuthServiceTest` y la respuesta de los controladores en `AuthControllerTest`).
- **Pruebas de Contexto (`CustomerOrderApplicationTests.java`)**: Valida que el contexto de Spring Boot se cargue correctamente, asegurando que las configuraciones, beans y conexiones base de datos estén bien orquestadas.

Para ejecutar las pruebas manualmente, navega a cualquiera de los microservicios y ejecuta:
```bash
./mvnw test
```

---

## ⚙️ Cómo Ejecutar el Proyecto

### Pre-requisitos
- Tener instalado [Docker](https://docs.docker.com/get-docker/) y Docker Compose.
- (Opcional) Java 17 y Maven si deseas ejecutarlo de forma nativa sin Docker.

### Pasos de Ejecución con Docker

1. Clona el repositorio:
   ```bash
   git clone <url-del-repo>
   cd Prueba-tecnica-torres
   ```

2. Configura las variables de entorno:
   Crea un archivo `.env` en la raíz del proyecto basándote en el archivo de ejemplo:
   ```bash
   cp .env.example .env
   ```
   *Asegúrate de colocar valores seguros en el archivo `.env`.*

3. Levanta los contenedores con Docker Compose:
   ```bash
   docker-compose up --build -d
   ```

   Este comando hará lo siguiente:
   - Levantará un contenedor de **SQL Server**.
   - Ejecutará un contenedor efímero (`db-init`) para inicializar las bases de datos (`auth_db` y `customer_order_db`).
   - Compilará y desplegará los microservicios `auth-service` y `customer-order`.
   - Compilará y desplegará el frontend (si la carpeta adyacente está configurada en Docker Compose).

4. Acceso a los servicios:
   - Backend Auth: `http://localhost:8081`
   - Backend Customer-Order: `http://localhost:8082`
   - Frontend: `http://localhost:4200`

### Detener los Servicios
Para detener y eliminar los contenedores generados:
```bash
docker-compose down
```
*(Para mantener persistencia de datos, los volúmenes están configurados en `sqlserver_data`)*
