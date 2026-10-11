# Smart Agriculture — Backend REST API

> **Rain Risk Prediction & Crop Protection Decision-Support System**  
> Robust, scalable Java Spring Boot 3.3.4 enterprise backend providing numerical weather ingestion, agronomic rule analysis, Spring Security authentication with BCrypt, role-based access control, and user-scoped data isolation.

---

## 🌾 Overview

The **Smart Agriculture Backend** acts as the central atmospheric analysis and decision-support engine. It ingests numerical hourly forecasts from Open-Meteo, applies agronomic rule thresholds, computes risk classifications (`LOW`, `MEDIUM`, `HIGH`), recommends crop-specific safeguards, and securely persists user accounts and evaluation audits in MySQL.

---

## 🔐 Security Architecture

- **Spring Security 6 & BCrypt**: All passwords are encrypted with `BCryptPasswordEncoder` (10 rounds). Plaintext passwords and demo credentials are never stored.
- **Session Management with HttpOnly Cookies**: Implements secure, server-managed `JSESSIONID` sessions over REST with `SameSite=Lax` and CORS credentials support.
- **Strict Role-Based Access Control (RBAC)**:
  - Public registration (`/api/auth/signup`) assigns **`ROLE_USER`** only. It is architecturally impossible for a public caller to register as `ROLE_ADMIN`.
  - Admin APIs (`/api/admin/**`) strictly require **`ROLE_ADMIN`**. Standard users receive HTTP `403 Forbidden`.
  - Farmer APIs (`/api/risk/**`, `/api/assessments/**`, `/api/dashboard/**`) require authentication.
- **Farmer Privacy & Tenant Isolation**: Every assessment record is stamped with the authenticated user's `userId`. Farmers can only access their own private evaluations.
- **Safe DTO Serialization**: Password hashes and sensitive internal fields are never returned in any API response.
- **One-Time Administrator Bootstrap**: Secure mechanism to provision the first real administrator account without hardcoded credentials. Permanently locked once an admin exists.

---

## 👑 Secure Initial Administrator Provisioning

No default or demo administrator credentials exist in the codebase. To create your first administrator:

### Option 1: Via REST Endpoint / Web UI (One-Time Setup)
Send a POST request to `/api/auth/setup-admin` (or use the toggle on the `/admin-login` page):
```bash
curl -X POST http://localhost:8080/api/auth/setup-admin \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "System Administrator",
    "email": "admin@yourdomain.org",
    "password": "YourSecurePassword123!"
  }'
```
*The backend creates the administrator with `ROLE_ADMIN` and **permanently locks** this endpoint against further calls (returns HTTP 403 / 409 if an admin already exists).*

### Option 2: Via Environment Variables (Server Boot)
Set the following environment variables before starting the backend:
```bash
export ADMIN_INIT_EMAIL="admin@yourdomain.org"
export ADMIN_INIT_PASSWORD="YourSecurePassword123!"
export ADMIN_INIT_NAME="Primary Administrator"
```
On boot, `AdminBootstrapRunner` checks if any administrator exists. If zero exist, it provisions the account securely and logs a confirmation.

---

## 🛠️ Technology Stack

- **Framework**: Java 17+, Spring Boot 3.3.4
- **Security**: Spring Security 6, BCrypt Password Encoder
- **Persistence**: Spring Data JPA, Hibernate ORM 6.5
- **Database**: MySQL 8.x (production), H2 (local development)
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Size`, `@Email`)
- **Monitoring**: Spring Boot Actuator (`/actuator/health`, `/actuator/info`)
- **Weather Provider**: Open-Meteo REST API (numerical models, free, no API key required)
- **Build Tool**: Apache Maven 3.8+

---

## 📡 REST API Reference

### 1. Authentication Endpoints
| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/auth/signup` | Public user registration (creates `ROLE_USER`) | Public |
| `POST` | `/api/auth/login` | Verifies credentials & sets session cookie | Public |
| `POST` | `/api/auth/logout` | Invalidates session & clears cookie | Authenticated |
| `GET` | `/api/auth/me` | Returns current user profile | Authenticated |
| `POST` | `/api/auth/setup-admin` | One-time bootstrap for first `ROLE_ADMIN` | One-Time Only |

### 2. Weather & Risk Assessment Endpoints
| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/health` | Service health status check | Public |
| `GET` | `/api/weather/geocode?query={location}` | Open-Meteo location autocomplete | Public |
| `POST` | `/api/risk/analyze` | Evaluates weather risk & generates crop safeguards | Authenticated |
| `GET` | `/api/assessments` | Retrieves user's private assessments | Authenticated |
| `GET` | `/api/dashboard/summary` | Retrieves user's personal dashboard statistics | Authenticated |

### 3. Administrator Endpoints
| Method | Endpoint | Description | Access |
|---|---|---|---|
| `GET` | `/api/admin/dashboard` | Aggregated platform metrics (users, assessments, risk breakdown) | `ROLE_ADMIN` |
| `GET` | `/api/admin/users` | Directory of all registered farmer accounts | `ROLE_ADMIN` |
| `GET` | `/api/admin/assessments` | System-wide assessment logs across all users | `ROLE_ADMIN` |

---

## 🚀 Local Setup & Running

### Prerequisites
- **JDK**: Java Development Kit 17 or higher
- **Maven**: 3.8+ (or use bundled Maven)
- **MySQL** (optional for dev): MySQL 8.0+

### Development Mode (Zero-Config Persistent H2)
By default, the backend runs with the `dev` profile using a persistent local H2 database file. No MySQL installation is required for development:
```bash
mvn clean spring-boot:run
```
*Service starts on `http://localhost:8080`.*  
*Health Check: `http://localhost:8080/api/health`.*

### Production Mode (MySQL 8.0)
1. Ensure MySQL is running and create the database schema:
```sql
CREATE DATABASE smart_agriculture CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Set environment variables (or copy `.env.example`):
```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/smart_agriculture?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="your_mysql_password"
export CORS_ALLOWED_ORIGINS="http://localhost:5173,https://your-frontend.vercel.app"
```

3. Launch application:
```bash
mvn clean spring-boot:run
```

---

## 🧪 Testing

The repository contains an automated suite of 25 unit and integration tests covering:
- All 12 security requirements (role enforcement, BCrypt hashing, duplicate email rejection, admin lockout, session invalidation, privacy isolation)
- Atmospheric risk heuristic calculations
- Crop protection safeguard logic across 6 crops
- Application context and health endpoints

To run the full test suite:
```bash
mvn test
```

To package the production executable JAR:
```bash
mvn clean package -DskipTests
```
*Executable JAR generated at `target/smart-agriculture-backend-1.0.0.jar`.*

---

## 🐳 Docker Deployment

Build and run using the included production `Dockerfile`:
```bash
# Build Docker image
docker build -t smart-agriculture-backend .

# Run container connected to MySQL
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/smart_agriculture?createDatabaseIfNotExist=true&useSSL=false" \
  -e SPRING_DATASOURCE_USERNAME="root" \
  -e SPRING_DATASOURCE_PASSWORD="your_password" \
  smart-agriculture-backend
```

---

## 📂 Project Structure

```
smart-agriculture-backend/
├── src/
│   ├── main/
│   │   ├── java/com/smartagriculture/
│   │   │   ├── config/              # SecurityConfig, WebConfig, GlobalExceptionHandler
│   │   │   ├── controller/          # AuthController, AdminController, RiskController, WeatherController...
│   │   │   ├── dto/                 # Request/Response data transfer objects
│   │   │   ├── entity/              # User, Assessment JPA entities
│   │   │   ├── repository/          # UserRepository, AssessmentRepository
│   │   │   ├── service/             # AuthService, CustomUserDetailsService, RainRiskService, WeatherService...
│   │   │   └── SmartAgricultureApplication.java
│   │   └── resources/
│   │       ├── application.properties       # Base configuration
│   │       ├── application-dev.properties   # Development (H2) settings
│   │       ├── application-prod.properties  # Production (MySQL) settings
│   │       └── schema.sql                   # DDL initialization scripts
│   └── test/
│       ├── java/com/smartagriculture/
│       │   ├── SecurityAuthenticationTest.java     # 12 security integration test cases
│       │   ├── RainRiskServiceTest.java            # Weather heuristic tests
│       │   ├── CropRecommendationServiceTest.java  # Crop safeguard tests
│       │   └── SmartAgricultureApplicationTests.java
│       └── resources/
│           └── application.properties       # Isolated in-memory test database
├── .env.example            # Environment variables template
├── .gitignore              # Git ignore rules (excludes target/, data/, .env)
├── Dockerfile              # Production container configuration
└── pom.xml                 # Maven build & dependencies descriptor
```

---

## 📄 License
This project is licensed under the MIT License.
