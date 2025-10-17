# Login Service - Architecture Diagram

## 📐 System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         CLIENT APPLICATIONS                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐ │
│  │   React UI   │  │   Postman    │  │   Mobile App │  │ Other µServices │
│  │  (Port 3000) │  │              │  │              │  │  (8082-8085)   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬──────┘ │
│         │                 │                 │                 │          │
└─────────┼─────────────────┼─────────────────┼─────────────────┼──────────┘
          │                 │                 │                 │
          └─────────────────┼─────────────────┼─────────────────┘
                            │                 │
                            ▼                 ▼
         ┌──────────────────────────────────────────────────────┐
         │          LOGIN SERVICE (Port 8081)                    │
         │         Context Path: /api/auth                       │
         └──────────────────────────────────────────────────────┘
                            │
         ┌──────────────────┴──────────────────┐
         │                                      │
         ▼                                      ▼
┌────────────────────┐              ┌──────────────────────┐
│   Security Layer   │              │    Swagger UI        │
│  ┌──────────────┐  │              │   /swagger-ui.html   │
│  │ JWT Filter   │  │              └──────────────────────┘
│  │   (Bearer)   │  │
│  └──────────────┘  │
│  ┌──────────────┐  │
│  │   BCrypt     │  │
│  │  Encoder     │  │
│  └──────────────┘  │
└────────┬───────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│                     CONTROLLER LAYER                         │
│  ┌────────────────────────────────────────────────────────┐  │
│  │              AuthController                            │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │  │
│  │  │ Register │ │  Login   │ │  Logout  │ │ Validate │  │  │
│  │  │   POST   │ │   POST   │ │   POST   │ │   POST   │  │  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │  │
│  │  ┌──────────┐ ┌──────────┐                            │  │
│  │  │  Bank    │ │  Health  │                            │  │
│  │  │  Config  │ │  Check   │                            │  │
│  │  │   GET    │ │   GET    │                            │  │
│  │  └──────────┘ └──────────┘                            │  │
│  └────────────────────────────────────────────────────────┘  │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                           │
│  ┌─────────────────┐  ┌──────────────────┐  ┌────────────┐  │
│  │   AuthService   │  │  SessionService  │  │ BankConfig │  │
│  │                 │  │                  │  │  Service   │  │
│  │ - register()    │  │ - updateSession()│  │ - getBank  │  │
│  │ - login()       │  │ - isExpired()    │  │   Config() │  │
│  │ - logout()      │  │ - autoLogout()   │  │            │  │
│  │ - validateToken │  │   @Scheduled     │  │            │  │
│  └─────────────────┘  └──────────────────┘  └────────────┘  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │     CustomUserDetailsService (Spring Security)       │   │
│  │              - loadUserByUsername()                  │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│                   REPOSITORY LAYER (JPA)                     │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐ │
│  │     User     │ │     Role     │ │    UserSession       │ │
│  │  Repository  │ │  Repository  │ │    Repository        │ │
│  └──────────────┘ └──────────────┘ └──────────────────────┘ │
│  ┌──────────────┐ ┌──────────────────────────────────────┐  │
│  │   AuditLog   │ │   BankConfiguration Repository       │  │
│  │  Repository  │ │                                      │  │
│  └──────────────┘ └──────────────────────────────────────┘  │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER (MySQL)                    │
│  ┌────────────────────────────────────────────────────────┐  │
│  │                  login_db Database                     │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────────────┐   │  │
│  │  │  users   │ │  roles   │ │    user_roles        │   │  │
│  │  │          │ │          │ │    (join table)      │   │  │
│  │  └──────────┘ └──────────┘ └──────────────────────┘   │  │
│  │  ┌──────────────────┐ ┌────────────┐ ┌─────────────┐  │  │
│  │  │  user_sessions   │ │ audit_logs │ │    bank     │  │  │
│  │  │                  │ │            │ │ configuration│ │  │
│  │  └──────────────────┘ └────────────┘ └─────────────┘  │  │
│  └────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘

         ┌──────────────────────────────────────┐
         │       EVENT PUBLISHING (Kafka)        │
         │  ┌─────────────────────────────────┐  │
         │  │     LoginEventPublisher         │  │
         │  │  - publishLoginEvent()          │  │
         │  └─────────────────────────────────┘  │
         │              │                         │
         │              ▼                         │
         │  ┌─────────────────────────────────┐  │
         │  │   Kafka Topic: login-events     │  │
         │  │  - LOGIN_SUCCESS                │  │
         │  │  - LOGIN_FAILURE                │  │
         │  │  - LOGOUT                       │  │
         │  │  - AUTO_LOGOUT                  │  │
         │  └─────────────────────────────────┘  │
         └──────────────────────────────────────┘
                       │
                       ▼
         ┌──────────────────────────────────────┐
         │  KAFKA CONSUMERS (Other Services)    │
         │  - Customer Service                  │
         │  - Product Service                   │
         │  - FD Account Service                │
         │  - Notification Service (future)     │
         └──────────────────────────────────────┘
```

---

## 🔄 Request Flow - User Login

```
┌─────────┐
│ Client  │
└────┬────┘
     │ POST /api/auth/login
     │ { username, password }
     ▼
┌─────────────────────────┐
│  JWT Authentication     │ ← Bypass for /login endpoint
│       Filter            │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│   AuthController        │
│   login() endpoint      │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│    AuthService          │
│                         │
│ 1. Find user in DB      │ ──→ UserRepository.findByUsername()
│ 2. Check if active      │
│ 3. Check if locked      │
│ 4. Validate password    │ ──→ BCrypt.matches()
│ 5. Reset failed attempts│
│ 6. Update last login    │
│ 7. Generate JWT         │ ──→ JwtUtil.generateToken()
│ 8. Create session       │ ──→ UserSessionRepository.save()
│ 9. Log audit event      │ ──→ AuditLogRepository.save()
│ 10. Publish Kafka event │ ──→ LoginEventPublisher.publish()
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│   Response              │
│   - JWT Token           │
│   - User details        │
│   - Roles               │
│   - Preferences         │
└────────┬────────────────┘
         │
         ▼
┌─────────┐
│ Client  │ ← Stores token for future requests
└─────────┘
```

---

## 🔐 Request Flow - Protected Endpoint (with JWT)

```
┌─────────┐
│ Client  │
└────┬────┘
     │ POST /api/auth/logout
     │ Header: Authorization: Bearer <JWT>
     ▼
┌─────────────────────────────────────┐
│  JWT Authentication Filter          │
│                                     │
│ 1. Extract token from header        │
│ 2. Validate token signature         │ ──→ JwtUtil.validateToken()
│ 3. Extract username & roles         │ ──→ JwtUtil.extractUsername()
│ 4. Load user details                │ ──→ UserDetailsService
│ 5. Set Security Context             │
└────────┬────────────────────────────┘
         │ ✅ Authenticated
         ▼
┌─────────────────────────┐
│   AuthController        │
│   logout() endpoint     │
│   @SecurityRequirement  │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│    AuthService          │
│                         │
│ 1. Find session by token│ ──→ UserSessionRepository
│ 2. Mark session inactive│
│ 3. Set logout time      │
│ 4. Log audit event      │ ──→ AuditLogRepository
│ 5. Publish Kafka event  │ ──→ LoginEventPublisher
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│   Response              │
│   - Success message     │
└────────┬────────────────┘
         │
         ▼
┌─────────┐
│ Client  │ ← Clears token
└─────────┘
```

---

## ⏰ Auto-Logout Scheduled Task

```
     Every 1 minute (Cron Job)
            │
            ▼
┌─────────────────────────────────────┐
│   SessionService                    │
│   @Scheduled(fixedRate = 60000)     │
│                                     │
│ 1. Calculate threshold              │
│    (now - 5 minutes)                │
│                                     │
│ 2. Find expired sessions            │ ──→ UserSessionRepository
│    WHERE last_activity < threshold  │     .findByActiveTrueAnd
│    AND is_active = TRUE             │      LastActivityBefore()
│                                     │
│ 3. For each expired session:        │
│    - Mark inactive                  │
│    - Set logout time                │
│    - Save to DB                     │
│    - Log AUTO_LOGOUT event          │ ──→ AuditLogRepository
│                                     │
└─────────────────────────────────────┘
```

---

## 🗄️ Database Entity Relationships

```
┌────────────────┐
│     users      │
│ ───────────────│
│ id (PK)        │───┐
│ username       │   │
│ password       │   │
│ email          │   │
│ mobile_number  │   │
│ ...            │   │
└────────────────┘   │
                     │
                     │ One-to-Many
                     │
                     ▼
        ┌────────────────────────┐
        │    user_sessions       │
        │ ───────────────────────│
        │ id (PK)                │
        │ user_id (FK)           │
        │ session_token          │
        │ last_activity          │
        │ is_active              │
        │ ...                    │
        └────────────────────────┘

┌────────────────┐       ┌─────────────────┐       ┌────────────────┐
│     users      │       │   user_roles    │       │     roles      │
│ ───────────────│       │ ────────────────│       │ ───────────────│
│ id (PK)        │───────│ user_id (FK)    │       │ id (PK)        │
│ username       │  Many │ role_id (FK)    │  Many │ name           │
│ ...            │  to   │                 │  to   │ description    │
└────────────────┘  Many └─────────────────┘  One  └────────────────┘

┌────────────────────────┐
│     audit_logs         │
│ ───────────────────────│
│ id (PK)                │
│ username               │ ← Not FK (for deleted users)
│ event_type             │
│ success                │
│ message                │
│ event_time             │
│ ...                    │
└────────────────────────┘

┌────────────────────────┐
│  bank_configuration    │
│ ───────────────────────│
│ id (PK)                │
│ bank_name              │
│ logo_url               │
│ default_language       │
│ default_currency       │
│ is_active              │
└────────────────────────┘
```

---

## 🔧 Common Library Integration

```
┌────────────────────────────────────────────────────┐
│              COMMON-LIB Module                      │
│  (Shared across all microservices)                 │
│                                                     │
│  ┌────────────────────────────────────────────┐    │
│  │            UTILITIES                       │    │
│  │  ┌──────────────────────────────────────┐  │    │
│  │  │  JwtUtil                             │  │    │
│  │  │  - generateToken()                   │  │    │
│  │  │  - validateToken()                   │  │    │
│  │  │  - extractUsername()                 │  │    │
│  │  │  - extractRoles()                    │  │    │
│  │  └──────────────────────────────────────┘  │    │
│  │  ┌──────────────────────────────────────┐  │    │
│  │  │  EncryptionUtil                      │  │    │
│  │  │  - encrypt() (AES)                   │  │    │
│  │  │  - decrypt()                         │  │    │
│  │  └──────────────────────────────────────┘  │    │
│  │  ┌──────────────────────────────────────┐  │    │
│  │  │  PIIMaskingUtil                      │  │    │
│  │  │  - maskEmail()                       │  │    │
│  │  │  - maskMobileNumber()                │  │    │
│  │  │  - maskString()                      │  │    │
│  │  └──────────────────────────────────────┘  │    │
│  └────────────────────────────────────────────┘    │
│  ┌────────────────────────────────────────────┐    │
│  │            COMMON DTOs                     │    │
│  │  ┌──────────────────────────────────────┐  │    │
│  │  │  ApiResponse<T>                      │  │    │
│  │  │  - success()                         │  │    │
│  │  │  - error()                           │  │    │
│  │  └──────────────────────────────────────┘  │    │
│  │  ┌──────────────────────────────────────┐  │    │
│  │  │  UserDTO                             │  │    │
│  │  │  (For inter-service communication)   │  │    │
│  │  └──────────────────────────────────────┘  │    │
│  └────────────────────────────────────────────┘    │
└────────────────────────────────────────────────────┘
         │                       │
         │ Used by               │ Used by
         │                       │
         ▼                       ▼
┌──────────────────┐    ┌──────────────────┐
│  Login Service   │    │  Customer Service│
│  (Port 8081)     │    │  (Port 8083)     │
└──────────────────┘    └──────────────────┘
         │                       │
         │ Used by               │ Used by
         │                       │
         ▼                       ▼
┌──────────────────┐    ┌──────────────────┐
│ Product Service  │    │ FD Account Svc   │
│  (Port 8082)     │    │  (Port 8085)     │
└──────────────────┘    └──────────────────┘
```

---

## 📊 Technology Stack Details

```
┌────────────────────────────────────────────────┐
│           SPRING BOOT 3.5.6                    │
├────────────────────────────────────────────────┤
│  Spring Web        │ REST APIs                 │
│  Spring Security   │ JWT + BCrypt              │
│  Spring Data JPA   │ Database Access           │
│  Spring Kafka      │ Event Publishing          │
│  Spring Validation │ Input Validation          │
│  Spring Scheduling │ Auto-logout Cron          │
└────────────────────────────────────────────────┘
           │
           ▼
┌────────────────────────────────────────────────┐
│          DATABASE - MySQL 8.0                  │
│  - Hibernate (ORM)                             │
│  - Auto DDL (Table creation)                   │
│  - Connection Pooling                          │
└────────────────────────────────────────────────┘
           │
           ▼
┌────────────────────────────────────────────────┐
│        MESSAGING - Apache Kafka                │
│  - Topic: login-events                         │
│  - JSON Serialization                          │
│  - Async Event Publishing                      │
└────────────────────────────────────────────────┘
           │
           ▼
┌────────────────────────────────────────────────┐
│      DOCUMENTATION - Swagger/OpenAPI           │
│  - SpringDoc 2.6.0                             │
│  - Interactive UI                              │
│  - JWT Integration                             │
└────────────────────────────────────────────────┘
```

---

## 🎯 Key Design Patterns Used

1. **Layered Architecture**
   - Controller → Service → Repository → Database

2. **Dependency Injection**
   - Spring's `@Autowired` / Constructor Injection

3. **Builder Pattern**
   - Lombok's `@Builder` for entities and DTOs

4. **Repository Pattern**
   - Spring Data JPA repositories

5. **DTO Pattern**
   - Separate Request/Response objects

6. **Singleton Pattern**
   - Spring beans are singletons by default

7. **Strategy Pattern**
   - BCrypt for password encoding

8. **Observer Pattern**
   - Kafka event publishing

9. **Filter Chain Pattern**
   - JWT Authentication Filter

10. **Scheduled Tasks Pattern**
    - Auto-logout scheduler

---

**🎊 This architecture is production-ready, scalable, and follows microservices best practices! 🎊**
