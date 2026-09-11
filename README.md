# NovaWavex Backend

A production-ready **Java Spring Boot REST API** for the NovaWavex workflow management platform.

The backend provides secure authentication, role-based authorization, user management, workflow management, workflow execution, notifications, password reset, and PostgreSQL database integration.

## 🚀 Live Backend

**Backend API:** https://novawavex-backend.onrender.com

## 🛠️ Technology Stack

* Java 26
* Spring Boot 4.1.0
* Spring Security
* JWT Authentication
* Spring Data JPA
* Hibernate
* PostgreSQL
* Maven
* Jakarta Validation
* REST APIs
* Spring Boot Actuator
* JUnit
* Render

## ✨ Features

### Authentication & Security

* User registration and login
* JWT-based authentication
* Role-based authorization
* Protected REST endpoints
* Password change
* Forgot password
* Password reset using reset tokens
* Account enable/disable functionality
* Admin user management

### User Management

* User profile management
* Update profile name
* Profile image support
* View user details
* Change user roles
* Enable/disable user accounts
* Delete users
* Self-account deletion

### Workflow Management

* Create workflows
* View workflows
* View individual workflows
* Update workflows
* Delete workflows
* Workflow ownership and authorization
* Workflow execution management
* Workflow execution status tracking

### Notifications

* User notifications
* Read/unread notification status
* Unread notification count
* Mark notification as read
* Mark all notifications as read
* Delete notifications
* Notification priorities and types

### Database

The application uses PostgreSQL with JPA/Hibernate.

Main entities include:

* User
* PasswordResetToken
* Workflow
* WorkflowExecution
* WorkflowStep
* WorkflowStepRelation
* Notification

## 🏗️ Backend Architecture

The backend follows a layered Spring Boot architecture:

```text
Client / React Frontend
          │
          ▼
      REST API
          │
          ▼
     Controllers
          │
          ▼
       Services
          │
          ▼
     Repositories
          │
          ▼
   JPA / Hibernate
          │
          ▼
     PostgreSQL
```

Security is handled through:

```text
Request
   │
   ▼
JWT Authentication Filter
   │
   ▼
Spring Security
   │
   ▼
Role / Authorization Checks
   │
   ▼
Controller
```

## 📂 Project Structure

```text
src/
├── main/
│   ├── java/com/novawavex/novawavex/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── notification/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   └── workflow/
│   │
│   └── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-prod.properties
│
└── test/
    └── java/com/novawavex/novawavex/
```

## 🔐 Environment Variables

Sensitive configuration is provided through environment variables rather than hardcoded credentials.

### Database

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

### Email

```text
MAIL_USERNAME
MAIL_PASSWORD
```

### Frontend

```text
FRONTEND_URL
```

The repository does **not** contain production passwords, database credentials, email credentials, or JWT secrets.

## ▶️ Running Locally

### 1. Clone the repository

```bash
git clone https://github.com/hemantkr26/novawavex.git
cd novawavex
```

### 2. Configure environment variables

Set the required environment variables:

```text
DB_USERNAME
DB_PASSWORD
MAIL_USERNAME
MAIL_PASSWORD
```

For production configuration:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
FRONTEND_URL
```

### 3. Start PostgreSQL

Create a PostgreSQL database:

```text
novawavex_db
```

### 4. Run the application

Using Maven:

```bash
mvn spring-boot:run
```

Or on Windows:

```cmd
mvnw.cmd spring-boot:run
```

The backend starts by default on:

```text
http://localhost:8080
```

## 🧪 Testing

The project contains unit, integration, security, repository, controller, and workflow tests.

Run the complete test suite with:

```bash
mvn test
```

Or on Windows:

```cmd
mvnw.cmd test
```

The test suite covers areas including:

* Authentication
* JWT security
* Authorization
* User management
* Profile management
* Password reset
* Workflow management
* Workflow execution
* Notifications
* Repository operations
* Validation
* Exception handling
* CORS
* Actuator health

## 📡 API

The backend exposes REST APIs for:

* Authentication
* Users
* Workflows
* Workflow executions
* Notifications

Detailed API documentation is maintained in the frontend repository.

## 🌐 Deployment

The backend is deployed on **Render** with PostgreSQL integration.

Production backend:

```text
https://novawavex-backend.onrender.com
```

The production configuration uses environment variables for database, CORS, and other sensitive configuration.

## 🔒 Security

NovaWavex uses:

* JWT authentication
* Spring Security
* Role-based access control
* Password hashing
* Protected endpoints
* Password reset tokens
* Account status controls
* Environment-based secret management
* CORS configuration

Sensitive credentials are not committed to the repository.

## 🔗 Related Repository

### Frontend

https://github.com/hemantkr26/novawavex-frontend

The frontend is built with:

* React
* Vite
* Axios
* React Router
* Lucide React

## 👨‍💻 Author

**Hemant Kumar**

GitHub: https://github.com/hemantkr26

Portfolio: https://hemantkrportfolio.netlify.app/

## 📌 Project Status

**Production Ready**

NovaWavex is deployed with a separate React frontend, Spring Boot backend, and PostgreSQL database.
