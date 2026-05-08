# VitalsMed — Setup Guide
**Group:** TriThread | **Sprint:** 1 | **v0.1.0 "Life-Link"**

## Group Members
- Velayo, Apriliza C.
- Gacrama, Monix B.
- Tiro, John Marc

---

## Quick Setup (XAMPP)

### 1. Place files in XAMPP
```
C:\xampp\htdocs\vitalsmed\
├── index.html          ← Main frontend (open this in browser)
├── api\
│   └── index.php       ← REST API router
├── config\
│   └── db.php          ← DB connection
└── database\
    └── schema.sql      ← Run this in phpMyAdmin
```

### 2. Start XAMPP
- Open XAMPP Control Panel
- Start **Apache** and **MySQL**

### 3. Create Database
1. Go to `http://localhost/phpmyadmin`
2. Click **Import** → choose `database/schema.sql`
3. Click **Go**

### 4. Open the App
```
http://localhost/vitalsmed/index.html
```

---

## Demo Login
| Username | Password | Role    |
|----------|----------|---------|
| admin    | admin123 | Admin   |
| drvelayo | password | Provider|

---

## API Endpoints

### Auth
| Method | Endpoint              | Description       |
|--------|-----------------------|-------------------|
| POST   | /api/auth/login       | User login        |
| POST   | /api/auth/register    | Register account  |

### Patients
| Method | Endpoint              | Description       |
|--------|-----------------------|-------------------|
| GET    | /api/patients         | List all patients |
| POST   | /api/patients         | Create patient    |
| GET    | /api/patients/{id}    | Get one patient   |
| PUT    | /api/patients/{id}    | Update patient    |
| DELETE | /api/patients/{id}    | Deactivate patient|

### Vitals (Sprint 1 Core)
| Method | Endpoint              | Description              |
|--------|-----------------------|--------------------------|
| POST   | /api/vitals/log       | Log vitals (validated)   |
| GET    | /api/vitals/{pid}     | Get patient vitals       |
| GET    | /api/vitals/alerts    | Get out-of-range alerts  |

### Medications (Sprint 1 Core)
| Method | Endpoint                   | Description          |
|--------|----------------------------|----------------------|
| GET    | /api/patient/medications   | Get all medications  |
| POST   | /api/medications/create    | Create medication    |
| GET    | /api/medications/{id}      | Get one medication   |
| PUT    | /api/medications/{id}      | Update medication    |
| DELETE | /api/medications/{id}      | Delete medication    |

### Adherence
| Method | Endpoint                   | Description          |
|--------|----------------------------|----------------------|
| POST   | /api/adherence/confirm     | Mark dose as taken   |
| GET    | /api/adherence/{patient_id}| Get adherence history|

---

## Vitals Safe Zones
| Metric       | Safe Range     | Alert Triggered If      |
|--------------|----------------|-------------------------|
| Heart Rate   | 60–100 bpm     | < 60 or > 100           |
| Temperature  | 36.1–37.5°C    | < 36.1 or > 37.5        |
| BP Systolic  | 90–140 mmHg    | < 90 or > 140           |
| BP Diastolic | 60–90 mmHg     | < 60 or > 90            |

---

## Adherence Window Logic
```
Scheduled Time: 08:00
Window Open:    08:00
Window Closes:  10:00  (+ 2 hours)

Before 10:00 → status: "Taken"
After  10:00 → status: "Late"  (auto-flagged by system)
```

## GitHub Repository
```
https://github.com/trithread/vitalsmed
```
