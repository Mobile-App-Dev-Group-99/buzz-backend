# BuzzApp — Complete System Data Flow

> How data moves through every layer of the system: database, backend services, and clients.

---

## Table of Contents

1. [System Architecture Overview](#1-system-architecture-overview)
2. [Database Schema (All Tables)](#2-database-schema-all-tables)
3. [Service Responsibilities](#3-service-responsibilities)
4. [Authentication & JWT Flow](#4-authentication--jwt-flow)
5. [User Onboarding Flow](#5-user-onboarding-flow)
6. [Student Registration Flow](#6-student-registration-flow)
7. [Parent Registration & Linking Flow](#7-parent-registration--linking-flow)
8. [Teacher Class Assignment Flow](#8-teacher-class-assignment-flow)
9. [Biometric Registration Flow](#9-biometric-registration-flow)
10. [Attendance Scan Flow](#10-attendance-scan-flow)
11. [Manual Attendance Flow](#11-manual-attendance-flow)
12. [Exeat (Early Exit) Flow](#12-exeat-early-exit-flow)
13. [Notifications Flow](#13-notifications-flow)
14. [Password Reset Flow](#14-password-reset-flow)
15. [Web Portal Data Flow](#15-web-portal-data-flow)
16. [Cross-Service Data Dependencies](#16-cross-service-data-dependencies)
17. [Multi-Tenancy Enforcement](#17-multi-tenancy-enforcement)

---

## 1. System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENTS                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────────────┐ │
│  │ Mobile App   │  │ Web Portal   │  │ Web Portal (Teacher/Parent)│ │
│  │ (React Native│  │ (React/Vite) │  │ (React/Vite)               │ │
│  │  Expo 54)    │  │ Tailwind CSS │  │                            │ │
│  └──────┬───────┘  └──────┬───────┘  └────────────┬───────────────┘ │
└─────────┼──────────────────┼───────────────────────┼─────────────────┘
          │                  │                       │
          │  HTTP + Bearer JWT                      │
          ▼                  ▼                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT MICROSERVICES                         │
│                                                                     │
│  ┌─────────────────────┐  ┌──────────────────┐  ┌───────────────┐  │
│  │   AUTH SERVICE       │  │ ATTENDANCE SVC   │  │ SAFETY SVC   │  │
│  │   :8081              │  │ :8082            │  │ :8083        │  │
│  │                      │  │                  │  │              │  │
│  │ • Schools CRUD       │  │ • Students CRUD  │  │ • Exeats     │  │
│  │ • Users CRUD         │  │ • Parents CRUD   │  │ • Notifs     │  │
│  │ • Login/Register     │  │ • Attendance     │  │              │  │
│  │ • JWT generation     │  │ • Biometrics     │  │              │  │
│  │ • Password reset     │  │ • Teacher classes│  │              │  │
│  │ • Flyway migrations  │  │                  │  │              │  │
│  └──────────┬───────────┘  └────────┬─────────┘  └──────┬──────┘  │
└─────────────┼───────────────────────┼────────────────────┼──────────┘
              │                       │                    │
              ▼                       ▼                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      POSTgreSQL 18                                   │
│                    Database: buzzdb_lgn1                              │
│                                                                     │
│  ┌──────────┐ ┌────────┐ ┌─────────┐ ┌──────────┐ ┌───────────┐   │
│  │ schools  │ │ users  │ │students │ │ parents  │ │ students_ │   │
│  │          │ │        │ │         │ │          │ │  parents  │   │
│  └──────────┘ └────────┘ └─────────┘ └──────────┘ └───────────┘   │
│  ┌──────────────────┐ ┌───────────────────┐ ┌──────────────────┐   │
│  │attendance_events │ │  biometric_       │ │  exeats          │   │
│  │                  │ │  templates        │ │                  │   │
│  └──────────────────┘ └───────────────────┘ └──────────────────┘   │
│  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐   │
│  │ notifications    │ │academic_results  │ │ teacher_classes  │   │
│  └──────────────────┘ └──────────────────┘ └──────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

**Key architectural decisions:**
- All 3 services share ONE PostgreSQL database (shared-nothing at DB level, shared-tables at SQL level)
- Only **auth-service** runs Flyway migrations (V1–V10)
- attendance-service and safety-service use `ddl-auto=update` + Flyway disabled
- Each service validates JWT tokens locally using the same `JWT_SECRET` — no inter-service auth calls
- Multi-tenancy is enforced by filtering every query on `schoolId` from the JWT

---

## 2. Database Schema (All Tables)

### V1 — `schools`
```sql
CREATE TABLE schools (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    location    VARCHAR(200),
    level       VARCHAR(10)   NOT NULL CHECK (level IN ('JHS', 'SHS', 'BOTH')),
    created_at  TIMESTAMP     DEFAULT NOW()
);
```
**Owned by:** auth-service  
**Root table** — every other table ultimately references `schools(id)`.

---

### V2 — `users`
```sql
CREATE TABLE users (
    id          BIGSERIAL    PRIMARY KEY,
    school_id   BIGINT       NOT NULL REFERENCES schools(id),
    username    VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,       -- BCrypt hash
    role        VARCHAR(30)  NOT NULL,        -- ADMIN, TEACHER, STUDENT, PARENT
    created_at  TIMESTAMP    DEFAULT NOW()
);
```
**Owned by:** auth-service  
**Roles:** `ADMIN`, `TEACHER`, `STUDENT`, `PARENT`  
**Note:** Students and parents also have user accounts here for login purposes.

---

### V3 — `students`, `parents`, `students_parents`
```sql
CREATE TABLE students (
    id              BIGSERIAL    PRIMARY KEY,
    school_id       BIGINT       NOT NULL REFERENCES schools(id),
    user_id         BIGINT       REFERENCES users(id),      -- links to login account
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    class_name      VARCHAR(100),                            -- added by V8
    date_of_birth   DATE,
    gender          VARCHAR(10),
    student_type    VARCHAR(20),                             -- DAY or BOARDING
    photo_url       VARCHAR(500),
    created_at      TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE parents (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       REFERENCES users(id),          -- links to login account
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    phone       VARCHAR(20),
    email       VARCHAR(255),                               -- added by V8
    created_at  TIMESTAMP    DEFAULT NOW()
);

-- Junction table: many-to-many (a student can have multiple parents)
CREATE TABLE students_parents (
    student_id  BIGINT NOT NULL REFERENCES students(id),
    parent_id   BIGINT NOT NULL REFERENCES parents(id),
    PRIMARY KEY (student_id, parent_id)
);
```
**Owned by:** attendance-service  
**Key relationship:** `students_parents` links parents to their children. Parents query this to find their child IDs, then query attendance/safety by those IDs.

---

### V4 — `biometric_templates`
```sql
CREATE TABLE biometric_templates (
    id          BIGSERIAL PRIMARY KEY,
    student_id  BIGINT    NOT NULL REFERENCES students(id),
    template    TEXT      NOT NULL,    -- base64 fingerprint template
    created_at  TIMESTAMP DEFAULT NOW()
);
```
**Owned by:** attendance-service  
**Flow:** Admin registers student → calls biometric API → stores fingerprint template here. At gate scan, the template is matched against this table.

---

### V5 — `attendance_events`
```sql
CREATE TABLE attendance_events (
    id          BIGSERIAL   PRIMARY KEY,
    student_id  BIGINT      NOT NULL REFERENCES students(id),
    school_id   BIGINT      NOT NULL REFERENCES schools(id),
    scan_type   VARCHAR(20) NOT NULL CHECK (scan_type IN ('ARRIVAL', 'DEPARTURE')),
    scanned_at  TIMESTAMP   NOT NULL,
    is_late     BOOLEAN     DEFAULT FALSE,
    gate        VARCHAR(50),         -- which gate (added by V8)
    status      VARCHAR(20),         -- ARRIVED, LATE, DEPARTED, ABSENT (added by V8)
    created_at  TIMESTAMP   DEFAULT NOW()
);
```
**Owned by:** attendance-service  
**Created by:** biometric gate scan OR teacher manual attendance  
**Read by:** admin dashboard (live feed, summary, class bars, weekly chart), parent home screen (child history), student dashboard (personal history)

---

### V6 — `exeats`, `notifications`
```sql
CREATE TABLE exeats (
    id              BIGSERIAL    PRIMARY KEY,
    student_id      BIGINT       NOT NULL REFERENCES students(id),
    school_id       BIGINT       REFERENCES schools(id),     -- added by V9
    approved_by     BIGINT       REFERENCES users(id),
    reason          VARCHAR(255),
    expected_return TIMESTAMP,
    actual_return   TIMESTAMP,
    status          VARCHAR(20)  DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'APPROVED', 'DENIED', 'RETURNED')),
    created_at      TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE notifications (
    id          BIGSERIAL    PRIMARY KEY,
    parent_id   BIGINT       NOT NULL REFERENCES parents(id),
    school_id   BIGINT       REFERENCES schools(id),         -- added by V9
    message     VARCHAR(255) NOT NULL,
    type        VARCHAR(50),
    sent_at     TIMESTAMP    DEFAULT NOW(),
    is_read     BOOLEAN      DEFAULT FALSE
);
```
**Owned by:** safety-service  
**Exeat lifecycle:** PENDING → APPROVED/DENIED → RETURNED  
**Notifications:** Created when an exeat is approved/denied/returned. FCM push not yet wired.

---

### V7 — `academic_results`
```sql
CREATE TABLE academic_results (
    id              BIGSERIAL    PRIMARY KEY,
    student_id      BIGINT       NOT NULL REFERENCES students(id),
    submitted_by    BIGINT       NOT NULL REFERENCES users(id),
    subject         VARCHAR(100) NOT NULL,
    score           NUMERIC(5,2),
    grade           VARCHAR(5),
    term            VARCHAR(20),
    year            INT,
    teacher_remark  TEXT,
    created_at      TIMESTAMP    DEFAULT NOW()
);
```
**Owned by:** (not yet wired to any service — exists in schema only)  
**Future:** Teachers will submit grades here. Parents/students will read from here.

---

### V10 — `teacher_classes`
```sql
CREATE TABLE teacher_classes (
    id               BIGSERIAL PRIMARY KEY,
    teacher_user_id  BIGINT    NOT NULL REFERENCES users(id),
    class_name       VARCHAR(20) NOT NULL,    -- e.g., "SHS 2B"
    school_id        BIGINT    NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(teacher_user_id, school_id),       -- one class per teacher per school
    UNIQUE(class_name, school_id)             -- one teacher per class per school
);
```
**Owned by:** attendance-service  
**Flow:** Admin assigns a teacher to a class → creates record here. Teacher then can mark attendance for that class.

---

## 3. Service Responsibilities

### Auth Service (`:8081`)
| Responsibility | Tables Used |
|---|---|
| School onboarding (create school + admin user) | `schools`, `users` |
| User login (email/password → JWT) | `users` |
| User registration (admin creates any role) | `users` |
| Password reset (forgot-password + admin reset) | `users` |
| Token validation endpoint (`/validate`) | reads JWT only |
| User info endpoint (`/me`) | `users` |
| **Runs all Flyway migrations (V1–V10)** | all |

### Attendance Service (`:8082`)
| Responsibility | Tables Used |
|---|---|
| Student CRUD (create, list, list by class) | `students`, `users` |
| Parent CRUD (create, list) | `parents`, `users` |
| Student-Parent linking | `students_parents` |
| Teacher class assignment | `teacher_classes`, `users` |
| Biometric template registration | `biometric_templates`, `students` |
| Attendance scan recording (ARRIVAL/DEPARTURE) | `attendance_events`, `students` |
| Manual attendance marking (teacher) | `attendance_events`, `teacher_classes` |
| Attendance summaries (today, weekly, per-class) | `attendance_events` |
| Student history + term summary | `attendance_events` |
| Student/Parent identity endpoints | `students`, `parents`, `students_parents` |
| Exeat listing (teacher/student views) | `exeats` (reads only, creates via safety-service) |

### Safety Service (`:8083`)
| Responsibility | Tables Used |
|---|---|
| Exeat CRUD (create, approve, deny, return) | `exeats` |
| Notification CRUD (send, list by parent) | `notifications` |

---

## 4. Authentication & JWT Flow

```
┌─────────┐         ┌─────────────┐         ┌──────────┐
│ Client  │──POST──▶│ auth-service │──query──▶│  users   │
│         │◀──JWT───│  /login      │◀─────────│          │
└─────────┘         └─────────────┘         └──────────┘
```

### Login Flow
1. Client sends `POST /api/auth/login` with `{ email, password }`
2. Auth-service looks up user by email in `users` table
3. BCrypt-verifies the password
4. `JwtService.generateToken()` creates a JWT containing:
   - `sub` (subject) = email
   - `role` = ADMIN / TEACHER / STUDENT / PARENT
   - `schoolId` = the school this user belongs to
   - `exp` = expiration (from `jwt.expiration` property)
5. Returns `LoginResponse { email, token, role, schoolId }`
6. Client stores `token`, `role`, `email`, `user_id`, `user_school_id`, `user_username` in SecureStore (mobile) or localStorage (web)

### Subsequent Requests
1. Client attaches `Authorization: Bearer <token>` header to every request
2. Any of the 3 services can independently validate the token:
   - `JwtAuthFilter` (Spring Security filter) intercepts the request
   - Calls `JwtService.isValid(token)` — parses the JWT, checks expiry
   - Extracts `email`, `role`, `schoolId` from claims
   - Creates `UsernamePasswordAuthenticationToken` with:
     - **principal** = `schoolId` (Long) ← this is what controllers read
     - **credentials** = email
     - **authorities** = `[ROLE_ADMIN]`, `[ROLE_TEACHER]`, etc.
3. Controllers access `authentication.getPrincipal()` to get the `schoolId` for multi-tenant filtering

### JWT Claims Structure
```json
{
  "sub": "admin@school.com",
  "role": "ADMIN",
  "schoolId": 1,
  "iat": 1700000000,
  "exp": 1700086400
}
```

---

## 5. User Onboarding Flow

This is the very first action in the system — creating a school and its admin.

```
┌─────────┐    POST /api/auth/onboard-school    ┌─────────────┐
│ Mobile  │─────────────────────────────────────▶│ auth-service │
│ or Web  │◀─────────────────────────────────────│              │
└─────────┘    { token, role, email, schoolId }  └──────┬──────┘
                                                        │
                                          ┌─────────────┼──────────────┐
                                          ▼             ▼              │
                                     schools         users            │
                                   (INSERT)        (INSERT)           │
                                                              ┌───────┘
```

### Step-by-step
1. User opens app → sees LoginScreen → taps "Register School"
2. `OnboardSchoolScreen` collects:
   - School name, location, level (JHS/SHS/BOTH)
   - Admin username, email, password
3. `POST /api/auth/onboard-school` with `OnboardSchoolRequest`
4. **auth-service** does:
   - Creates `schools` row → gets auto-generated `id`
   - Creates `users` row with `role=ADMIN`, `school_id=school.id`, BCrypt-hashed password
   - Generates JWT with admin's email, role, and schoolId
5. Returns `OnboardSchoolResponse { token, role, email, schoolId }`
6. Client stores all auth data in SecureStore and navigates to AdminDashboard

**Database effect:**
```sql
INSERT INTO schools (name, location, level) VALUES ('Accra Academy', 'Kaneshie', 'SHS');
-- returns id = 1

INSERT INTO users (school_id, username, email, password, role)
VALUES (1, 'admin1', 'admin@accra.edu', '$2a$...', 'ADMIN');
```

---

## 6. Student Registration Flow

```
┌─────────┐  POST /api/admin/student  ┌──────────────────┐    ┌─────────┐
│ Admin   │──────────────────────────▶│ attendance-service │───▶│ students│
│ (mobile │◀──────────────────────────│  /api/admin       │    │ users   │
│  or web)│    { studentId, userId }  │                   │    └─────────┘
└─────────┘                           └──────────────────┘
```

### Step-by-step
1. Admin taps "Register Student" in AdminActionsScreen / AdminRegisterStudentScreen
2. Fills in: first name, last name, class name, DOB, gender, student type (DAY/BOARDING)
3. `POST /api/admin/student` with `CreateStudentRequest`
4. **attendance-service** does:
   - Creates `users` row with `role=STUDENT`, `school_id=admin's schoolId`, generates temp password
   - Creates `students` row with all profile fields + `user_id` pointing to the user account
5. Returns `StudentResponse` with the new student ID

**Database effect:**
```sql
INSERT INTO users (school_id, username, email, password, role)
VALUES (1, 'kwame_mensah', 'kwame@accra.edu', '$2a$...', 'STUDENT');

INSERT INTO students (school_id, user_id, first_name, last_name, class_name, gender, student_type)
VALUES (1, 5, 'Kwame', 'Mensah', 'SHS 2B', 'MALE', 'BOARDING');
```

---

## 7. Parent Registration & Linking Flow

```
┌─────────┐  POST /api/admin/parent   ┌──────────────────┐    ┌────────┐
│ Admin   │──────────────────────────▶│ attendance-service │───▶│parents │
│         │                           │                   │    │ users  │
│         │  POST /api/admin/link     │                   │    │student_│
│         │──────────────────────────▶│                   │───▶│parents │
└─────────┘                           └──────────────────┘    └────────┘
```

### Step-by-step
1. Admin taps "Register Parent"
2. Fills in: first name, last name, phone, email, then **multi-selects** children from existing students
3. `POST /api/admin/parent` with `CreateParentRequest`
4. **attendance-service** does:
   - Creates `users` row with `role=PARENT`, temp password
   - Creates `parents` row linked to that user
5. `POST /api/admin/link` with `{ parentId, studentIds: [1, 2, 3] }`
6. **attendance-service** does:
   - For each studentId: `INSERT INTO students_parents (student_id, parent_id) VALUES (?, ?)`
7. Returns `ParentResponse`

**Database effect:**
```sql
INSERT INTO users (school_id, username, email, password, role)
VALUES (1, 'ama_osei', 'ama@gmail.com', '$2a$...', 'PARENT');

INSERT INTO parents (user_id, first_name, last_name, phone, email)
VALUES (7, 'Ama', 'Osei', '0241234567', 'ama@gmail.com');

INSERT INTO students_parents (student_id, parent_id) VALUES (1, 3);
INSERT INTO students_parents (student_id, parent_id) VALUES (2, 3);
```

---

## 8. Teacher Class Assignment Flow

```
┌─────────┐  POST /api/admin/teacher-class   ┌────────────────────┐
│ Admin   │─────────────────────────────────▶│ attendance-service  │
│         │                                  │ /api/admin          │
│         │                                  └────────┬───────────┘
│         │                                           ▼
│         │                                    teacher_classes
└─────────┘
```

### Step-by-step
1. Admin taps "Assign Class" → AdminAssignTeacherScreen
2. Selects a teacher from dropdown, selects a class name (e.g., "SHS 2B")
3. `POST /api/admin/teacher-class` with `AssignTeacherClassRequest { teacherId, className }`
4. **attendance-service** does:
   - Verifies teacher exists with role=TEACHER in the same school
   - Checks no other teacher is already assigned to that class (UNIQUE constraint)
   - Inserts `teacher_classes` row
5. Teacher can now mark attendance for that class

**Database effect:**
```sql
INSERT INTO teacher_classes (teacher_user_id, class_name, school_id)
VALUES (3, 'SHS 2B', 1);
```

---

## 9. Biometric Registration Flow

```
┌─────────┐  POST /api/biometric/register   ┌────────────────────┐
│ Admin   │────────────────────────────────▶│ attendance-service  │
│ or Gate │                                 │                     │
│         │                                 └────────┬───────────┘
│         │                                          ▼
│         │                                   biometric_templates
└─────────┘
```

### Step-by-step
1. Admin registers a student → gets student ID
2. Calls `POST /api/biometric/register` with `BiometricRegisterRequest { studentId, template }`
3. `template` is a base64-encoded fingerprint template captured by the device
4. **attendance-service** does:
   - Verifies student exists and belongs to the same school
   - Inserts into `biometric_templates`
5. Student can now scan at the gate

**Database effect:**
```sql
INSERT INTO biometric_templates (student_id, template)
VALUES (1, 'base64-encoded-fingerprint-data...');
```

---

## 10. Attendance Scan Flow

This is the core real-time flow — the biometric gate scan.

```
┌─────────┐  POST /api/attendance/scan   ┌─────────────────────┐    ┌──────────────────┐
│  Gate   │─────────────────────────────▶│ attendance-service   │───▶│biometric_templates│
│  (phone)│                              │                     │    └────────┬─────────┘
│         │                              │  match template?    │             │
│         │                              │                     │◀────────────┘
│         │                              │                     │
│         │                              │  insert             │    ┌──────────────────┐
│         │                              │─────────────────────┼───▶│attendance_events  │
│         │◀─────────────────────────────│                     │    └──────────────────┘
└─────────┘   ScanResponse {status}      └─────────────────────┘
```

### Step-by-step
1. Student places fingerprint on gate scanner
2. Device captures fingerprint → encodes as base64 template
3. `POST /api/attendance/scan` with `ScanRequest { studentId, template, gate }`
4. **attendance-service** does:
   - Looks up `biometric_templates` for `student_id = ?`
   - Compares the submitted template against stored templates (exact match on base64 string)
   - If no match → returns error
   - If match:
     - Determines scan type: ARRIVAL (first scan today) or DEPARTURE (second scan today)
     - Checks current time vs `attendance.arrival.cutoff` (default 08:00)
     - If ARRIVAL and time > cutoff → `isLate = true`, `status = LATE`
     - If ARRIVAL and time <= cutoff → `isLate = false`, `status = ARRIVED`
     - If DEPARTURE → `status = DEPARTED`
     - Inserts `attendance_events` row
5. Returns `ScanResponse { studentName, status, scannedAt }`

**Database effect:**
```sql
INSERT INTO attendance_events (student_id, school_id, scan_type, scanned_at, is_late, gate, status)
VALUES (1, 1, 'ARRIVAL', '2025-01-15 07:55:00', false, 'Main Gate', 'ARRIVED');

-- Later that day:
INSERT INTO attendance_events (student_id, school_id, scan_type, scanned_at, is_late, gate, status)
VALUES (1, 1, 'DEPARTURE', '2025-01-15 15:30:00', false, 'Main Gate', 'DEPARTED');
```

---

## 11. Manual Attendance Flow

Teachers mark attendance when students forget their biometrics.

```
┌─────────┐  POST /api/attendance/manual   ┌──────────────────┐
│ Teacher │───────────────────────────────▶│ attendance-service │
│ (phone) │                                │                   │
│         │                                │ verify teacher    │
│         │                                │ owns this class   │
│         │                                │                   │
│         │                                │ insert            │
│         │◀───────────────────────────────│ attendance_events │
└─────────┘                                └──────────────────┘
```

### Step-by-step
1. Teacher opens TeacherHomeScreen → sees list of students in their assigned class
2. Taps "Mark Present" / "Mark Late" / "Mark Absent" next to a student
3. `POST /api/attendance/manual` with `ManualAttendanceRequest { studentId, className, status }`
4. **attendance-service** does:
   - Looks up `teacher_classes` for this teacher's user ID + className
   - If no match → rejects (teacher doesn't own this class)
   - Inserts `attendance_events` row with the specified status
5. Returns success

**Database effect:**
```sql
INSERT INTO attendance_events (student_id, school_id, scan_type, scanned_at, is_late, gate, status)
VALUES (2, 1, 'MANUAL', '2025-01-15 08:10:00', false, null, 'PRESENT');
```

---

## 12. Exeat (Early Exit) Flow

An exeat is an approved early exit from campus.

```
                                    ┌─────────────────────┐
                             ┌─────▶│ safety-service      │
                             │      │ POST /api/exeat     │
                             │      │ /create             │
┌─────────┐                  │      └────────┬────────────┘
│ Teacher │── create exeat ──┘               ▼
│ (phone) │                            ┌────────┐
│         │                            │ exeats │
│         │── approve/deny ──────┐     └────────┘
│         │◀─────────────────────┤
│         │                      │     ┌─────────────────────┐
│ Admin   │── approve/deny ──────┘     │ safety-service      │
│ (web)   │                            │ PUT /api/exeat      │
│         │                            │ /{id}/approve       │
│         │                            └─────────────────────┘
└─────────┘
```

### Create Exeat
1. Teacher opens TeacherExeatScreen → fills in student, reason, expected return time
2. `POST /api/exeat/create` with `CreateExeatRequest { studentId, reason, expectedReturn }`
3. **safety-service** does:
   - Creates `exeats` row with `status=PENDING`, `schoolId` from JWT
4. Returns `ExeatResponse`

### Approve/Deny Exeat
1. Admin opens web portal → AdminExeats → sees pending exeats
2. `PUT /api/exeat/{id}/approve` with `ApproveExeatRequest { approvedBy: adminUserId }`
3. **safety-service** does:
   - Fetches exeat, verifies it belongs to the same school (multi-tenancy check)
   - If status != PENDING → rejects with 409 CONFLICT
   - Sets `status=APPROVED`, `approvedBy=adminUserId`
4. Returns updated `ExeatResponse`
5. **TODO:** Parent notification not yet wired (service can't resolve `parentId` from `studentId` without access to `students_parents`)

### Record Return
1. Student returns to campus → admin/teacher records return
2. `PUT /api/exeat/{id}/return` with `ReturnExeatRequest { actualReturn: timestamp }`
3. **safety-service** does:
   - Verifies status == APPROVED (can't return a denied exeat)
   - Sets `status=RETURNED`, `actualReturn` timestamp

**Database effect:**
```sql
-- Create
INSERT INTO exeats (student_id, school_id, reason, expected_return, status, created_at)
VALUES (1, 1, 'Medical appointment', '2025-01-15 14:00:00', 'PENDING', NOW());

-- Approve
UPDATE exeats SET status = 'APPROVED', approved_by = 1 WHERE id = 1;

-- Return
UPDATE exeats SET status = 'RETURNED', actual_return = NOW() WHERE id = 1;
```

---

## 13. Notifications Flow

```
┌─────────────────────┐    POST /api/notification/send    ┌──────────────────┐
│ safety-service      │──────────────────────────────────▶│                  │
│ (internal call)     │                                   │   notifications  │
│                     │                                   └──────────────────┘
│ ExeatService calls  │    GET /api/notification/parent   ┌──────────────────┐
│ notify() on approve │◀──────────────────────────────────│ Mobile / Web     │
└─────────────────────┘                                   └──────────────────┘
```

### How notifications are created
1. When an exeat is approved/denied/returned, `ExeatService` calls `NotificationService.notify(parentId, message, schoolId)`
2. **Currently:** This is TODO-wired — the code has `// TODO: parentId isn't resolvable from safety-service yet`
3. The `NotificationService` would insert into `notifications` table

### How notifications are read
1. Parent opens Alerts tab → NotificationsScreen
2. `GET /api/notification/parent/{parentId}` 
3. **safety-service** does:
   - Queries `notifications` where `parent_id = ?` AND `school_id = ?`
   - Returns `List<NotificationResponse>` ordered by `sent_at DESC`

**Database effect:**
```sql
INSERT INTO notifications (parent_id, school_id, message, sent_at, is_read)
VALUES (3, 1, 'Exeat approved for Kwame Mensah', NOW(), false);
```

---

## 14. Password Reset Flow

### Forgot Password (self-service)
```
┌─────────┐  POST /api/auth/forgot-password  ┌─────────────┐    ┌────────┐
│ User    │─────────────────────────────────▶│ auth-service │───▶│ users  │
│         │◀─────────────────────────────────│              │    └────────┘
└─────────┘   { message: "sent to email" }   └──────────────┘
```

1. User enters email on ForgotPasswordScreen
2. `POST /api/auth/forgot-password` with `ForgotPasswordRequest { email }`
3. **auth-service** does:
   - Looks up user by email
   - Generates temp password (`"BuzzApp" + random 4-digit`)
   - BCrypt-encodes and saves to `users.password`
   - Returns generic message: "A temporary password has been sent to your email"
   - **Note:** The temp password is NEVER returned to the client (security fix)

### Admin Reset Password
```
┌─────────┐  POST /api/auth/admin/reset-password  ┌─────────────┐
│ Admin   │───────────────────────────────────────▶│ auth-service │
│         │◀───────────────────────────────────────│              │
└─────────┘   { message: "Password reset done" }   └──────────────┘
```

1. Admin opens AdminProfileScreen → enters target email + new password
2. `POST /api/auth/admin/reset-password` with `ResetPasswordRequest { email, newPassword }`
3. **auth-service** does:
   - Verifies the calling user is an ADMIN (role check)
   - Verifies target user has the same `schoolId` (multi-tenancy check)
   - BCrypt-encodes new password and saves

---

## 15. Web Portal Data Flow

The web portal (React/Vite) follows the same API patterns as mobile but with different UI.

```
┌──────────────────────────────────────────────────────────────────────┐
│  Web Portal (Vite + React + Tailwind CSS)                            │
│                                                                      │
│  src/services/api.ts  →  3 axios instances (auth/attendance/safety) │
│  src/context/AuthContext.tsx → login state, JWT in localStorage     │
│  src/App.tsx → React Router (/login, /admin, /teacher, /parent)    │
│                                                                      │
│  Pages:                                                              │
│  • LoginPage         → POST /api/auth/login + GET /api/auth/me     │
│  • AdminDashboard    → GET /api/attendance/summary/today,           │
│                        GET /api/attendance/live,                     │
│                        GET /api/attendance/classes/today,            │
│                        GET /api/attendance/weekly                    │
│  • AdminStudents     → GET /api/admin/students                      │
│  • AdminAttendance   → GET /api/attendance/live                     │
│  • AdminExeats       → GET /api/exeat/school, PUT approve/deny     │
│  • AdminStaff        → GET /api/admin/teachers                      │
│  • TeacherDashboard  → GET /api/teacher/me/class,                   │
│                        POST /api/attendance/manual                  │
│  • ParentDashboard   → GET /api/parent/me/children,                 │
│                        GET /api/attendance/student/{id}             │
└──────────────────────────────────────────────────────────────────────┘
```

### Web Login Flow
1. User enters email + password on `LoginPage.tsx`
2. `POST /api/auth/login` → gets `{ token, email, role, schoolId }`
3. Immediately calls `GET /api/auth/me` with the token → gets `{ id, username, email, role, schoolId }`
4. Stores everything in `AuthContext` → persisted to `localStorage`
5. Redirects to role-appropriate dashboard (`/admin`, `/teacher`, `/parent`)
6. 401 interceptor on all API clients → auto-logout + redirect to `/login`

### API Base URLs (Web Portal)
```
Auth Service:      https://auth-service-bpwr.onrender.com
Attendance Service: https://attendance-service-40dn.onrender.com
Safety Service:    https://safety-service-djmq.onrender.com
```

---

## 16. Cross-Service Data Dependencies

Since all 3 services share one database, there are implicit cross-service dependencies:

| Data | Written By | Read By | Dependency Type |
|---|---|---|---|
| `schools` | auth-service | attendance, safety | attendance/safety filter by `schoolId` from JWT |
| `users` | auth-service | attendance (admin lists teachers/parents) | attendance queries `users` directly |
| `students` | attendance-service | safety (create exeat by `studentId`) | safety only stores the ID, no JOIN |
| `parents` | attendance-service | safety (notification by `parentId`) | safety only stores the ID, no JOIN |
| `students_parents` | attendance-service | **nobody yet** | safety-service CANNOT resolve parentId from studentId |
| `teacher_classes` | attendance-service | attendance (validate teacher owns class) | intra-service |
| `biometric_templates` | attendance-service | attendance (gate scan match) | intra-service |
| `attendance_events` | attendance-service | attendance (dashboards, reports) | intra-service |
| `exeats` | safety-service | attendance (teacher/student views) | attendance reads safety's table |
| `notifications` | safety-service | safety (parent reads) | intra-service |
| `academic_results` | **nobody yet** | nobody yet | schema exists, no service wired |

### Critical Gap: `students_parents`
- **safety-service** needs to notify a parent when their child's exeat is approved
- To do this, it needs to look up `parent_id` from `students_parents` given a `student_id`
- **Current state:** This lookup does NOT exist in safety-service — the TODO comments in `ExeatService.approveExeat()` and `recordReturn()` document this gap
- **Impact:** No parent notifications are sent for exeat events

---

## 17. Multi-Tenancy Enforcement

Every query is scoped to a school. The enforcement pattern is consistent:

### How `schoolId` flows through the system
```
1. JWT token contains { email, role, schoolId }
         │
2. JwtAuthFilter extracts schoolId from JWT
         │
3. Sets Authentication.principal = schoolId
         │
4. Controller reads: Long schoolId = (Long) auth.getPrincipal()
         │
5. Service/Repository filters: WHERE school_id = ?
```

### Examples
- `AttendanceService.getTodaySummary(schoolId)` → `attendance_events WHERE school_id = ?`
- `AttendanceService.getClassesToday(schoolId)` → `students WHERE school_id = ?`
- `ExeatService.getOwnedExeat(exeatId, schoolId)` → `exeats WHERE id = ? AND school_id = ?`
- `AdminController.listParents(schoolId)` → `users WHERE role = 'PARENT' AND school_id = ?` → get parent IDs → `parents WHERE id IN (...)`

### Security guarantee
- An admin from school A cannot see students from school B
- A teacher from school A cannot mark attendance for school B's classes
- Cross-school access returns 404 (not 403) to avoid leaking resource existence

---

## Summary: End-to-End Data Lifecycle

```
Onboard School → Register Users → Register Students → Link Parents → Assign Teachers
                                                                        │
                                                                Register Biometrics
                                                                        │
                                                                  Gate Scan (Daily)
                                                                        │
                                                              ┌─────────┼─────────┐
                                                              ▼         ▼         ▼
                                                        Attendance  Exeats   Notifications
                                                        Events      (Safety) (Safety)
                                                              │         │
                                                              ▼         ▼
                                                        Admin Dashboard (Web + Mobile)
                                                        Parent Dashboard (Mobile + Web)
                                                        Teacher Dashboard (Mobile + Web)
```
