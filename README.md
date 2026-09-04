# Medora - Medical Records Management System

Medora is a comprehensive healthcare management platform designed to streamline patient medical records, appointments, laboratory tests, prescriptions, and billing operations.

##  Features

- **Patient Management** - Create and manage patient profiles with EMBG (personal identification number)
- **Medical Records** - Access comprehensive patient medical histories including diagnoses, symptoms, allergies, and prescriptions
- **Appointments** - Schedule and manage doctor-patient appointments
- **Lab Tests** - Request and track laboratory test results
- **Prescriptions** - Manage medication prescriptions for patients
- **Doctor Management** - Manage doctor profiles and specializations
- **Billing** - Generate and track billing records for medical services
- **User Authentication** - Secure JWT-based authentication with role-based access control (Admin, Doctor, Patient, Lab Technician, Billing Admin)

##  Tech Stack

### Backend
- **Framework:** Spring Boot (Java)
- **Database:** PostgreSQL
- **ORM:** Hibernate/JPA
- **Security:** Spring Security + JWT
- **Build Tool:** Maven

### Frontend
- **Library:** React 18+
- **Routing:** React Router
- **HTTP Client:** Axios
- **Styling:** TailwindCSS
- **Build Tool:** npm/Create React App

## 📋 Prerequisites

Before you start, make sure you have installed:

- **Java JDK 21+** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **Node.js 22+** - [Download](https://nodejs.org/)
- **PostgreSQL 14+** - [Download](https://www.postgresql.org/download/)

##  Quick Start

### 1. Setup Database

Create a PostgreSQL database for the application:

```bash
createdb medora
```

**Note:** The database schema and tables are automatically created by Flyway migrations and Hibernate when the backend starts.

### 2. Start Backend

Navigate to the backend directory and run:

```bash
cd backend
./mvnw.cmd spring-boot:run
```

The backend will start on `http://localhost:8080`

**Wait for the message:** `Started Application in X seconds`

### 3. Start Frontend (in a new terminal)

Navigate to the frontend directory and run:

```bash
cd frontend
npm install
npm start
```

The frontend will open automatically on `http://localhost:3000`

##  Default Credentials & User Roles

### Admin User
- **Username:** `admin`
- **Password:** `admin123`
- **Role:** ADMIN
- **Permissions:** Full system access, user management, system configuration

### Doctor Users
- **Username:** Doctor's email address (from the database)
- **Password:** `doctor123`
- **Role:** DOCTOR
- **Permissions:** View/manage patient medical records, create prescriptions, manage appointments, request lab tests

**Example:**
```
Username: ivan.stojanov@medora.com
Password: doctor123
```

### Patient Users
- **Username:** Patient's EMBG (personal identification number)
- **Password:** `password123`
- **Role:** PATIENT
- **Permissions:** View own medical records, view appointments, view prescriptions

**Example:**
```
Username: 1505993123477
Password: password123
```

### Lab Technician Users
- **Username:** Lab technician's email address (from the database)
- **Password:** `lab123`
- **Role:** LAB_TECHNICIAN
- **Permissions:** Create and update lab test results, view assigned lab tests

**Example:**
```
Username: lab_marina
Password: lab123
```

### Billing Admin Users
- **Username:** Billing admin's email address (from the database)
- **Password:** `adminmedora123`
- **Role:** BILLING_ADMIN
- **Permissions:** View billing records, generate billing reports, manage billing operations

**Example:**
```
Username: admin_ilija
Password: adminmedora123
```


##  Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/medora
spring.datasource.username=medora_app
spring.datasource.password=postgres

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Hibernate
spring.jpa.hibernate.ddl-auto=update
```


##  Role-Based Features

### ADMIN
- ✅ Manage all users (create, update, delete)
- ✅ Create and manage patients
- ✅ Create and manage doctors
- ✅ View all medical records
- ✅ System configuration
- ✅ View all appointments
- ✅ Access backfill operations

### DOCTOR
- ✅ View patient list
- ✅ View patient medical records (diagnoses, symptoms, allergies)
- ✅ Create prescriptions
- ✅ Request laboratory tests
- ✅ Manage appointments
- ✅ Create medical reports
- ❌ Cannot create new patients (admin only)
- ❌ Cannot access billing information

### PATIENT
- ✅ View own medical records
- ✅ View own appointments
- ✅ View own prescriptions
- ✅ View own lab test results
- ❌ Cannot view other patients' data
- ❌ Cannot modify medical records

### LAB_TECHNICIAN
- ✅ View assigned lab tests
- ✅ Create and update lab test results
- ✅ View patient information for assigned tests
- ❌ Cannot create new lab tests (doctor only)
- ❌ Cannot access medical records
- ❌ Cannot access billing

### BILLING_ADMIN
- ✅ View all billing records
- ✅ Generate billing reports
- ✅ Track revenue and payments
- ✅ View patient information for billing purposes
- ❌ Cannot access medical records
- ❌ Cannot manage appointments
- ❌ Cannot create prescriptions

---

##  Security Features

- JWT-based authentication
- Role-based access control (RBAC)
- Password hashing with BCrypt
- CORS configuration for frontend-backend communication
- SQL injection protection via parameterized queries
- Request validation and error handling

##  Database Migrations

Database migrations are located in `backend/src/main/resources/db/migration/` and are automatically applied by Flyway on startup.
