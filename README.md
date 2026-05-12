# Mums Aqua API — Water Plant ERP Backend

## Tech Stack
- Java 17
- Spring Boot 3.3
- Spring Security (Basic Auth)
- Spring Data JPA
- PostgreSQL
- Lombok

## Setup

### 1. Install PostgreSQL
Download from https://www.postgresql.org/download/ or use Docker:
```bash
docker run -d --name mumsaqua-db -p 5432:5432 -e POSTGRES_DB=mumsaqua -e POSTGRES_PASSWORD=postgres postgres:16
```

### 2. Create Database
```sql
CREATE DATABASE mumsaqua;
```

### 3. Run the Application
```bash
cd backend
./mvnw spring-boot:run
```
Or on Windows:
```cmd
mvnw.cmd spring-boot:run
```

The app starts on `http://localhost:8080`.

### 4. Default Credentials
On first run, the app seeds these users:

| Username | Password | Role |
|----------|----------|------|
| admin | password | ADMIN |
| salesman | password | SALESMAN |
| driver | password | DRIVER |

### 5. Configuration
Edit `src/main/resources/application.yml`:
- `spring.datasource.url` — PostgreSQL connection URL
- `spring.datasource.username` / `password` — DB credentials
- `app.cors.allowed-origins` — Frontend URLs allowed

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/auth/me | Get current user info |
| GET/POST/PUT/DELETE | /api/products | Product CRUD |
| GET/POST/PUT/DELETE | /api/raw-materials | Raw material CRUD |
| POST | /api/raw-materials/stock-entries | Add stock |
| GET/POST/PUT | /api/customers | Customer CRUD |
| GET/POST/DELETE | /api/sales | Sale CRUD |
| GET/POST | /api/payments | Payment CRUD |
| GET/POST/PUT/DELETE | /api/labour | Labour CRUD |
| GET/POST | /api/attendance | Attendance |
| GET/POST/PUT/DELETE | /api/vehicles | Vehicle CRUD |
| GET/POST/PUT/DELETE | /api/drivers | Driver CRUD |
| GET/PUT | /api/fuel-prices | Fuel prices |
| GET | /api/fuel-prices/history | Fuel price history |
| GET/POST | /api/km-entries | KM tracking |
| GET | /api/dashboard/stats | Dashboard stats |
| GET | /api/dashboard/low-stock | Low stock items |
