# 💊 VitalsMed API

> **Patient Vitals & Medication Management System**  
> TRITHREAD — System Project | 1st Sprint | `v0.1.0 — The Life-Link Patch`

---

## 👥 Group Members

| Name | Role |
|------|------|
| Velayo, Apriliza C. | Developer |
| Gacrama, Monix B. | Developer |
| Tiro, John Marc | Developer |

---

## 📋 About

VitalsMed is a Spring Boot REST API that connects a patient's biological identity with their medical regimen. It handles real-time biometric ingestion, medication schedule management, adherence tracking, and secure provider-patient data access.

---

## 🏗️ Project Structure

```
vitalmed/
├── src/main/java/com/trithread/vitalmed/
│   ├── VitalsMedApplication.java       ← Entry point
│   ├── controller/
│   │   ├── AuthController.java         ← POST /auth/register, /auth/login
│   │   ├── MedicationController.java   ← CRUD /patient/medications
│   │   └── VitalsController.java       ← POST /vitals/log, GET /vitals/{id}
│   ├── service/
│   │   ├── AuthService.java            ← BCrypt + JWT logic
│   │   ├── MedicationService.java      ← Adherence window logic
│   │   └── VitalsService.java          ← Safe zone threshold logic
│   ├── model/
│   │   ├── Patient.java
│   │   ├── Medication.java
│   │   └── VitalsLog.java
│   ├── repository/
│   │   └── Repositories.java
│   ├── dto/
│   │   └── Dtos.java                   ← AuthRequest, RegisterRequest, AuthResponse
│   └── security/
│       └── JwtUtil.java                ← JWT generation & validation
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+

### Run the App

```bash
# Clone the repository
git clone https://github.com/TRITHREAD/vitalmed.git
cd vitalmed

# Build and run
mvn spring-boot:run
```

The server starts at `http://localhost:8080`  
H2 Console available at `http://localhost:8080/h2-console`

---

## 🔐 Auth Endpoints

### Register
```http
POST /auth/register
Content-Type: application/json

{
  "fullName": "Juan dela Cruz",
  "email": "juan@email.com",
  "password": "securepass123",
  "role": "PATIENT"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "patientId": 1,
  "fullName": "Juan dela Cruz",
  "role": "PATIENT"
}
```

---

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "juan@email.com",
  "password": "securepass123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "patientId": 1,
  "fullName": "Juan dela Cruz",
  "role": "PATIENT"
}
```

> Use the returned `token` as a Bearer token for all authenticated requests.

---

## 💊 Medication Endpoints (CRUD)

### Create Medication
```http
POST /patient/medications
Authorization: Bearer <token>
Content-Type: application/json

{
  "patient": { "id": 1 },
  "medicationName": "Metformin",
  "dosage": "500mg",
  "dailyFrequency": 2
}
```

> System auto-calculates `nextDoseTime` based on `dailyFrequency` (24h ÷ frequency).

---

### Get All Medications
```http
GET /patient/medications?patientId=1
Authorization: Bearer <token>
```

---

### Update Medication
```http
PUT /patient/medications/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "medicationName": "Metformin",
  "dosage": "1000mg",
  "dailyFrequency": 3
}
```

---

### Delete Medication
```http
DELETE /patient/medications/{id}
Authorization: Bearer <token>
```

---

### Mark Dose as Taken
```http
PATCH /patient/medications/{id}/taken
Authorization: Bearer <token>
```

> If confirmed within the **2-hour adherence window** → `TAKEN`  
> If confirmed after the window → `LATE`

---

## 🫀 Vitals Endpoints

### Log Vitals
```http
POST /vitals/log
Authorization: Bearer <token>
Content-Type: application/json

{
  "patient": { "id": 1 },
  "heartRate": 85,
  "bodyTemperature": 36.8
}
```

**Response:**
```json
{
  "id": 1,
  "heartRate": 85.0,
  "bodyTemperature": 36.8,
  "alertStatus": "NORMAL",
  "loggedAt": "2025-01-15T10:30:00"
}
```

---

### Get Vitals History
```http
GET /vitals/{patientId}
Authorization: Bearer <token>
```

---

## 🌡️ Safe Zone Thresholds

| Vital | Normal (Safe Zone) | Alert Trigger | Status |
|-------|-------------------|---------------|--------|
| Heart Rate | 60–100 bpm | < 60 or > 100 | WARNING |
| Heart Rate | < 50 or > 150 | extreme range | CRITICAL |
| Body Temperature | 36.1–37.5°C | < 36.1 or > 37.5 | WARNING |
| Body Temperature | < 35.5 or > 40.0°C | extreme range | CRITICAL |

> Values outside human-possible range (HR: 40–200, Temp: 35–42°C) are **rejected** entirely.

---

## ⏱️ Adherence Window Logic

```
Medication Scheduled → nextDoseTime set
         │
         ├─ Dose confirmed within 2 hours → Status: TAKEN ✅
         │
         └─ No confirmation after 2 hours → Status: LATE ⚠️
```

---

## 🔒 Security

- Passwords hashed with **BCrypt** before storage
- **JWT tokens** (HS256, 24h expiry) issued on login/register
- **Row-Level Security (RLS)** — Medical Providers can only access data of their assigned patients

---

## 📦 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Auth | Spring Security + JWT (jjwt 0.11.5) |
| ORM | Spring Data JPA / Hibernate |
| Database | H2 (dev) / MySQL (prod) |
| Build | Maven |

---

## 📝 Patch Notes

### v0.1.0 — "The Life-Link Patch" *(1st Sprint)*

1. **Patient Vital Tracking** — Dynamic ingestion for heart rate and body temperature with human-range validation
2. **Daily Medication Scheduler** — CRUD with auto-calculated dose intervals
3. **Missed Dose Flagging** — 2-hour adherence window; `PENDING → LATE` if unconfirmed
4. **Security & Privacy** — BCrypt passwords, JWT auth, Row-Level Security

---

*VitalsMed · TRITHREAD · 1st Sprint · v0.1.0*
