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

##  Prerequisites

Before you start, make sure you have installed:

- **Java JDK 21+** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **Node.js 22+** - [Download](https://nodejs.org/)
- **PostgreSQL 14+** (if using local database) - [Download](https://www.postgresql.org/download/)
- **SSH Client** (for remote database access) - Built-in on Windows 10+, Mac, Linux

## Configuration for Remote Database

### Step 1: Setup `.env.properties` File

Create a `.env.properties` file in the project root directory with your remote database credentials:

```properties
# Remote Database Credentials (provided by your professor or administrator)
DB_REMOTE_NAME=your_database_name
DB_REMOTE_USERNAME=your_database_username
DB_REMOTE_PASSWORD=your_database_password

# JWT Secret for authentication
JWT_SECRET=your_jwt_secret_key_min_32_characters

# Local database password (for local profile, default is 'postgres')
DB_PASSWORD=postgres
```

**Important Security Notes:**
- ⚠️ This file is in `.gitignore` and must NEVER be committed to Git
- ⚠️ Keep your credentials secure and do not share them
- ⚠️ Never upload this file to any public repository

### Step 2: SSH Tunnel Setup (for Remote Database)

Before starting the application, you must establish an SSH tunnel to access the remote database:

```bash
# Windows (Command Prompt or PowerShell)
ssh -L 9999:localhost:5432 your_ssh_username@remote_server_address

# Example:
ssh -L 9999:localhost:5432 t_medora@194.149.135.130
```

**Important:** Keep this terminal window open while running the application. The tunnel will close if you close the terminal.

**Expected Output:**
```
Enter password: [enter your SSH password]
Access granted. Press Return to begin session.
Local port 9999 forwarding to localhost:5432
```

---

##  Quick Start

### Prerequisites Checklist

Before starting the application, ensure you have:

-  Created `.env.properties` file with your database credentials
-  SSH tunnel is running and connected (see Configuration section above)
-  Terminal window with SSH tunnel remains open

---

### Step 1: Start Backend (Terminal 1)

Navigate to the project root and run:

```bash
cd backend
./mvnw.cmd spring-boot:run
```

**Expected Output:**
```
Started MedoraApplication in X seconds
```

**Port:** `http://localhost:8081`

---

### Step 2: Start Frontend (Terminal 2)

In a new terminal, navigate to frontend directory and run:

```bash
cd frontend
npm install
npm start
```

**Expected Output:**
```
Compiled successfully!
You can now view medora-frontend in the browser.
Local: http://localhost:3001
```

**Port:** `http://localhost:3001`

---

### Step 3: Access the Application

Open your browser and navigate to: **http://localhost:3001**

You should see the Medora login screen.

---

### ⚠️ Important: Keep These Terminals Open

- **Terminal 0:** SSH Tunnel (must remain open)
- **Terminal 1:** Backend (Spring Boot)
- **Terminal 2:** Frontend (React)

Close any of these and the application will stop working.

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

The backend is configured to use a **remote PostgreSQL database** via SSH tunnel.

**Main Configuration File:** `backend/src/main/resources/application.properties`

```properties
# Application Setup
spring.application.name=medora
server.port=8081
spring.profiles.active=remote

# Load environment variables from .env.properties
spring.config.import=optional:file:.env.properties

# JWT Authentication
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Hibernate/JPA Settings
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**Remote Database Configuration:** `backend/src/main/resources/application-remote.properties`

```properties
# Remote PostgreSQL Database (via SSH tunnel on port 9999)
spring.datasource.url=jdbc:postgresql://localhost:9999/${DB_REMOTE_NAME}
spring.datasource.username=${DB_REMOTE_USERNAME}
spring.datasource.password=${DB_REMOTE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
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
