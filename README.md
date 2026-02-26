# API Rate Limiter using Redis and Spring Boot

This project implements a production-ready API rate limiting system using **Java, Spring Boot, and Redis**. It includes a full CRUD API for managing users, where all endpoints are protected by a configurable rate limiter built with Redis to prevent abuse.

## 🌟 Features

*   **API Rate Limiting:** Limits the number of requests a client (identified by IP address) can make within a specified time window using Redis.
*   **User Management (CRUD):** Fully functional RESTful API to Create, Read, Update, and Delete users.
*   **Relational Database:** Stores user data persistently in a **MySQL** database.
*   **Caching & State:** Uses **Redis** as a fast, in-memory store for tracking request counts.
*   **Custom Interceptor:** Implements a Spring `HandlerInterceptor` to enforce rate limits globally across API endpoints.
*   **Global Exception Handling:** Provides consistent, structured error responses for validation failures, rate limit exceedances (`429 Too Many Requests`), and generic server errors.
*   **Docker Ready:** Includes a `docker-compose.yml` for easy setup of the Redis service (and potentially the entire app).

## 🛠️ Tech Stack

*   **Java 17**
*   **Spring Boot 3.2.x** (Web, Data JPA, Data Redis, Validation)
*   **MySQL** (Relational Database)
*   **Redis** (In-memory Data Store & Rate Limiter)
*   **Docker & Docker Compose**
*   **Maven** (Dependency Management)

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/ratelimiter/
│   │   ├── config/              # Configuration classes (e.g., Redis, Interceptors)
│   │   ├── controller/          # REST API Endpoints (e.g., UserController)
│   │   ├── dto/                 # Data Transfer Objects (Requests & Responses)
│   │   ├── exception/           # Custom Exceptions & Global Exception Handler
│   │   ├── interceptor/         # Spring Interceptors (RateLimitInterceptor)
│   │   ├── model/               # JPA Entities (User)
│   │   ├── repository/          # Spring Data JPA Repositories
│   │   ├── service/             # Business Logic & Rate Limiting implementation
│   │   └── RateLimiterApplication.java # Spring Boot Main Class
│   └── resources/
│       ├── application.properties # Application Configuration (Port, DB, Redis, Limits)
│       └── schema.sql           # Database Table creation script
└── test/                        # Unit and Integration Tests
```

## 🚀 Getting Started

### Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Maven 3.6+
*   MySQL Server (Running on localhost:3306)
*   Redis Server (Running on localhost:6379, or via Docker)
*   Docker Desktop (Optional, but recommended for running Redis easily)

### 1. Database Setup (MySQL)

Ensure MySQL is running. Create a schema (database) named `user`.
The application is configured to connect with:
*   **URL:** `jdbc:mysql://localhost:3306/user`
*   **Username:** `root`
*   **Password:** `root`

*(You can change these in `src/main/resources/application.properties`)*

The `schema.sql` file will automatically create the `users` table if it doesn't exist when the application starts.

### 2. Redis Setup (using Docker Compose)

The easiest way to start Redis is using the provided `docker-compose.yml` file.

Open a terminal in the project root and run:
```bash
docker-compose up -d redis
```

### 3. Application Configuration Summary (`application.properties`)

```properties
# Rate Limiting config (Defaults to 5 requests per 60 seconds)
rate-limit.max-requests=5
rate-limit.window-seconds=60

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/user?createDatabaseIfNotExist=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=root

# Hibernate
spring.jpa.hibernate.ddl-auto=update
```

### 4. Running the Application

You can use the embedded Maven wrapper to run the application:

**On Windows:**
```cmd
.\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
*(Or if you have maven installed globally, just `mvn spring-boot:run`)*

The API will start on `http://localhost:8080`.

## 🧪 Testing the APIs & Rate Limiter

### User CRUD Endpoints

All base endpoints are under `/users`.

*   **Create User:** `POST /users` (Requires `name` and `email` in JSON body)
*   **Get All Users:** `GET /users`
*   **Get User by ID:** `GET /users/{id}`
*   **Update User:** `PUT /users/{id}`
*   **Delete User:** `DELETE /users/{id}`

**Example POST Request:**
```bash
curl -X POST -H "Content-Type: application/json" -d "{\"name\": \"Alice\", \"email\": \"alice@example.com\"}" http://localhost:8080/users
```

### Testing the Rate Limiter

By default, the app allows **5 requests per minute per IP**.

To test this, you can send 6 rapid requests. The first 5 will return a `200 OK` (or `201 Created`), and the 6th will be blocked returning a `429 Too Many Requests`.

**Quick Bash/PowerShell Test:**
```powershell
# PowerShell
for ($i=1; $i -le 10; $i++) { curl.exe -s -o NUL -w "%{http_code}\n" http://localhost:8080/users }
```
```bash
# Bash
for i in {1..10}; do curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/users; done
```

You should see output similar to this:
```
200
200
200
200
200
429
429
...
```
