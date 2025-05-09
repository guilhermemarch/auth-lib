# Authentication API

A Spring Boot application that provides JWT authentication with user registration and login.

## Features

- User registration with password validation
- User login with JWT authentication
- JWT token-based authorization
- Password validation using Passay
- PostgreSQL database for user storage
- Docker setup for easy deployment

## Prerequisites
[README.md](README.md)
- Java 17
- Docker and Docker Compose
- Maven (optional if using Docker)

## Running with Docker Compose

1. Clone the repository
2. Navigate to the project directory
3. Start the containers:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database (accessible on port 5432)
- pgAdmin for database management (accessible on port 5050)

To include the application in the Docker setup, uncomment the `auth-api` service in `docker-compose.yml`.

## Database Access

You can access pgAdmin at http://localhost:5050 with:
- Email: admin@admin.com
- Password: admin

To connect to the PostgreSQL server from pgAdmin:
- Host: postgres
- Port: 5432
- Database: auth_db
- Username: postgres
- Password: admin

## API Endpoints

### Registration

```
POST /api/auth/register
```

Request body:
```json
{
  "username": "testuser",
  "password": "Password123!",
  "email": "test@example.com"
}
```

### Login

```
POST /api/auth/login
```

Request body:
```json
{
  "username": "testuser",
  "password": "Password123!"
}
```

## Environment Configuration

The application uses environment variables defined in the `.env` file:

- Database settings (PostgreSQL)
- JWT configuration
- Server port

## Running Locally (without Docker)

1. Start a PostgreSQL instance
2. Update `src/main/resources/application.properties` if needed
3. Run:

```bash
./mvnw spring-boot:run
```

## Building the Application

```bash
./mvnw clean package
```

## Security Configuration

The security configuration is defined in `WebSecurityConfig.java`, which:
- Secures API endpoints with JWT authentication
- Configures CORS and CSRF settings
- Sets up authentication providers 