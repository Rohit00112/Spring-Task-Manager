# Task Manager API

A RESTful API for managing tasks, built with Spring Boot.

## Features

- User authentication with JWT
- Task management (CRUD operations)
- Task categorization
- Task status tracking
- Task priorities
- Dashboard with task statistics
- API documentation with Swagger/OpenAPI

## Technologies

- Java 17
- Spring Boot 3.4.5
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT Authentication
- Swagger/OpenAPI for documentation
- JUnit and Mockito for testing

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven
- PostgreSQL

### Database Setup

1. Create a PostgreSQL database named `taskmanager`
2. Update the database configuration in `src/main/resources/application.properties` if needed

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on port 8080.

### API Documentation

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token

### Tasks

- `GET /api/tasks` - Get all tasks for the authenticated user
- `GET /api/tasks/{id}` - Get a specific task
- `POST /api/tasks` - Create a new task
- `PUT /api/tasks/{id}` - Update a task
- `DELETE /api/tasks/{id}` - Delete a task

### Categories

- `GET /api/categories` - Get all categories for the authenticated user
- `GET /api/categories/{id}` - Get a specific category
- `POST /api/categories` - Create a new category
- `PUT /api/categories/{id}` - Update a category
- `DELETE /api/categories/{id}` - Delete a category

### Dashboard

- `GET /api/dashboard/stats` - Get task statistics for the authenticated user

## Testing

```bash
mvn test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
