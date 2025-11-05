# Fixed Deposit Management System - Project Report

## 1. Introduction

The Fixed Deposit Management System (Credexa FD) is a comprehensive microservices-based banking application designed to handle all aspects of fixed deposit account lifecycle management. The system implements a modern, scalable architecture using Spring Boot microservices on the backend and React with TypeScript on the frontend, connected through an API Gateway for centralized routing and security.

The project encompasses five core microservices: Customer Service, Product Pricing Service, FD Calculator Service, FD Account Service, and Login/Authentication Service, each handling distinct business domains. The system enables customers to open fixed deposit accounts, calculate returns, manage transactions, handle premature withdrawals, and administer role-based access control. All services communicate through RESTful APIs and are orchestrated via Spring Cloud Gateway, providing a unified entry point for the React-based user interface.

## 2. Work Breakdown Structure - FD Account Service Module

The FD Account Service represents the core business logic module, responsible for complete FD account lifecycle management from creation to maturity. The development was distributed across team members with specialized responsibilities:

**Account Creation & Management (Member A)**
- Implemented dual account creation modes: standard and customized accounts
- Developed account number generation algorithm using timestamp and random components
- Created validation logic for principal amount ranges, term limits, and product constraints
- Built account inquiry endpoints supporting search by account number, customer ID, product code, and branch
- Implemented maturity calculation engine with compound interest formulas
- Developed account summary generation with real-time balance calculations

**Transaction Processing (Member B)**
- Designed and implemented transaction management system supporting multiple transaction types (CREDIT, DEBIT, INTEREST_CREDIT, PENALTY, etc.)
- Built transaction reference generation using UUID-based approach
- Developed transaction reversal mechanism with audit trail maintenance
- Implemented premature withdrawal inquiry and processing with penalty calculations
- Created transaction history tracking with balance after transaction computation
- Built concurrent transaction handling with optimistic locking

**Role Management & Security (Member C)**
- Developed multi-party account role system supporting PRIMARY, SECONDARY, JOINT, and NOMINEE roles
- Implemented ownership percentage validation ensuring total ownership equals 100%
- Built role activation/deactivation workflow with effective date management
- Created role-based access control queries for customer account retrieval
- Implemented role conflict resolution for joint account scenarios
- Developed role audit logging for compliance requirements

**Data Layer & Integration (Member D)**
- Designed JPA entity relationships with bidirectional mappings
- Implemented custom repository queries using JPQL with JOIN FETCH optimization
- Configured Hibernate lazy loading strategies to prevent N+1 query issues
- Built database migration scripts for schema evolution
- Integrated with common-lib for shared DTOs and utilities
- Implemented exception handling with custom error responses

## 3. Project Timeline - FD Account Service

**August 2024**
- Week 1 (Aug 1-7): Requirements gathering, database schema design, entity modeling
- Week 2 (Aug 8-14): Account creation endpoints implementation, basic CRUD operations
- Week 3 (Aug 15-21): Transaction management system development, transaction types implementation
- Week 4 (Aug 22-31): Role management system design and initial implementation

**September 2024**
- Week 1 (Sep 1-9): Account inquiry endpoints, search functionality, pagination support
- Week 2-3 (Sep 10-20): *Mid-semester break - No development*
- Week 4 (Sep 21-30): Premature withdrawal feature, penalty calculation, integration testing

**October 2024**
- Week 1 (Oct 1-7): Repository query optimization, JOIN FETCH implementation, performance tuning
- Week 2 (Oct 8-14): API Gateway integration, CORS configuration, endpoint testing
- Week 3 (Oct 15-21): UI-backend integration, bug fixes, lazy loading resolution
- Week 4 (Oct 22-28): Final testing, documentation, code cleanup, deployment preparation

## 4. Minutes of Team Meetings

**Meeting 1 - August 5, 2024**
Attendees: All team members
Agenda: Project kickoff, requirement analysis, architecture discussion
Decisions: Adopted microservices architecture, selected Spring Boot and React stack, defined service boundaries
Action Items: Create entity diagrams, set up GitHub repository, initialize Spring Boot projects

**Meeting 2 - August 12, 2024**
Attendees: All team members
Agenda: Review account creation implementation, discuss database schema
Decisions: Finalized account number format (YYYYMMDDHHMMSS-RRRR), agreed on dual creation modes
Action Items: Implement validation rules, create unit tests for account creation

**Meeting 3 - August 19, 2024**
Attendees: All team members
Agenda: Transaction system design review, discuss transaction types
Decisions: Implemented UUID-based transaction references, defined transaction lifecycle states
Action Items: Build transaction reversal logic, implement audit logging

**Meeting 4 - August 26, 2024**
Attendees: All team members
Agenda: Role management implementation status, ownership validation
Decisions: Enforced 100% ownership constraint, implemented role effective dates
Action Items: Complete role CRUD operations, add role conflict validation

**Meeting 5 - September 2, 2024**
Attendees: All team members
Agenda: Account inquiry endpoints, search optimization
Decisions: Implemented pagination for large result sets, added multiple search criteria
Action Items: Optimize database queries, add index on frequently searched columns

**Meeting 6 - September 9, 2024**
Attendees: All team members
Agenda: Pre-break status update, premature withdrawal feature planning
Decisions: Defined penalty calculation formula, agreed on inquiry-before-process workflow
Action Items: Implement withdrawal inquiry endpoint, calculate reduced interest rates

**Meeting 7 - September 23, 2024**
Attendees: All team members
Agenda: Post-break sync, integration testing review
Decisions: Identified LazyInitializationException issues, planned JOIN FETCH optimization
Action Items: Refactor repository queries, add explicit fetch strategies

**Meeting 8 - September 30, 2024**
Attendees: All team members
Agenda: Performance issues discussion, query optimization
Decisions: Removed unnecessary isActive filters causing query failures, implemented JOIN FETCH
Action Items: Rebuild service, test all inquiry endpoints, validate optimizations

**Meeting 9 - October 7, 2024**
Attendees: All team members
Agenda: API Gateway integration planning, routing configuration
Decisions: Gateway on port 8080, FD Account Service on 8086, defined route prefixes
Action Items: Configure gateway routes, enable CORS for frontend

**Meeting 10 - October 14, 2024**
Attendees: All team members
Agenda: UI development status, backend endpoint testing
Decisions: Resolved 500 errors on inquiry endpoints, fixed repository query issues
Action Items: Test all 27 endpoints through gateway, document API responses

**Meeting 11 - October 21, 2024**
Attendees: All team members
Agenda: Login service integration, authentication flow
Decisions: Login service on port 8081, context path /api/auth, JWT token implementation
Action Items: Update gateway configuration, implement protected routes in UI

**Meeting 12 - October 28, 2024**
Attendees: All team members
Agenda: Final integration review, bug fixes, deployment readiness
Decisions: Fixed CORS issues, resolved 403 errors, validated complete user flows
Action Items: Final testing, prepare deployment scripts, documentation review

## 5. Project Deliverables

**Backend Microservices**
1. **Customer Service (Port 8083)**: Customer CRUD operations, KYC management, customer profile maintenance
2. **Product Pricing Service (Port 8084)**: FD product catalog, interest rate management, product features configuration
3. **FD Calculator Service (Port 8085)**: Standalone and product-based FD return calculations, compound interest computation, maturity projections
4. **FD Account Service (Port 8086)**: Complete account lifecycle management with 27 endpoints covering account creation (standard/customized), inquiry (by account/customer/product/branch), transaction processing, premature withdrawal, role management
5. **Login/Authentication Service (Port 8081)**: User registration, JWT-based authentication, session management, role-based authorization, audit logging

**API Gateway (Port 8080)**
- Centralized routing for all microservices
- CORS configuration for frontend integration
- Request/response logging filter
- Health monitoring via Spring Boot Actuator

**Frontend Application**
- React 18 with TypeScript for type safety
- Vite for fast development and optimized builds
- Tailwind CSS and shadcn/ui component library for modern UI
- Comprehensive pages: Login/Register, Dashboard, Customers, Products, FD Calculator, FD Accounts (listing, creation, details)
- Protected routes with JWT token management
- Axios-based API integration with automatic token injection
- Responsive design supporting mobile and desktop

**Infrastructure**
- Spring Cloud Gateway for microservices orchestration
- MySQL databases for each service with JPA/Hibernate ORM
- Batch scripts for service lifecycle management (start-all-services.bat, stop-all-services.bat)
- Swagger/OpenAPI documentation for all endpoints

## 6. Challenges Faced

**Technical Challenges**

*LazyInitializationException in Account Queries*
The most critical issue encountered was Hibernate's lazy loading causing exceptions when accessing account roles outside transaction boundaries. Initial queries like `findByRoles_CustomerId` failed with "could not initialize proxy - no Session" errors. Resolution required adding explicit `JOIN FETCH` clauses and removing detached entity access patterns.

*Repository Query Optimization*
Original queries filtering by `isActive=true` were incorrectly excluding valid accounts because the filter was applied at the role level instead of account level. Example: `WHERE r.customerId = :customerId AND r.isActive = true` blocked results when any role was inactive. Solution: Removed the isActive filter and implemented JOIN FETCH for eager loading: `SELECT DISTINCT a FROM FdAccount a JOIN FETCH a.roles r WHERE r.customerId = :customerId`.

*API Gateway Routing Complexity*
The Login Service used context path `/api/auth` on port 8081, requiring complex gateway routing: `/api/login/**` → `http://localhost:8081/api/auth/**`. Initial configurations with `StripPrefix` failed. Final solution used RewritePath: `RewritePath=/api/login/(?<segment>.*), /api/auth/${segment}`.

*CORS Configuration Conflicts*
Frontend running on Vite (port 5173) faced 403 Forbidden errors because backend CORS only allowed ports 3000 and 4200. Multiple configuration points required synchronization: Login Service WebConfig, Gateway CORS config, and individual service configurations.

*Port Conflicts During Development*
Frequent port conflicts (8080, 8083, 8086) when restarting services required manual process killing using `netstat -ano | findstr :PORT` and `taskkill /F /PID`. Created batch files to automate service management.

**Non-Technical Challenges**

*Team Coordination Across Modules*
With four team members working on different components (account creation, transactions, roles, data layer), frequent merge conflicts occurred. Established daily sync meetings and feature branch strategy to minimize conflicts.

*Communication Gaps During Break Period*
The September 10-20 break disrupted development momentum. Post-break integration revealed multiple incompatible API contracts. Resolution required dedicated re-sync sessions and API documentation updates.

*Frontend-Backend Integration Mismatch*
Backend APIs returned wrapped responses (`ApiResponse<T>` structure) but frontend initially expected direct data. Confusion over response structure (`response.data.data` vs `response.data`) delayed integration by several days.

*Dependency Version Conflicts*
Different microservices used varying Spring Boot versions (3.1.x vs 3.2.x), causing common-lib compatibility issues. Standardized all services to Spring Boot 3.2.0 for consistency.

## 7. Lessons Learned

**Architectural Insights**
Early definition of API contracts and DTO structures prevents late-stage integration issues. Implementing Swagger/OpenAPI documentation from day one significantly improved team communication and frontend-backend coordination.

**JPA/Hibernate Best Practices**
Understanding Hibernate's fetch strategies is crucial for microservices. Always use `JOIN FETCH` for required associations to avoid N+1 queries and lazy loading exceptions. Entity graphs provide better control than default lazy/eager settings.

**Microservices Communication**
Centralized API Gateway simplifies client integration but adds complexity in routing configuration. RewritePath and StripPrefix filters must be carefully tested. CORS must be configured at both gateway and service levels for proper cross-origin access.

**Testing Strategy**
Unit tests alone are insufficient for microservices. Integration testing with all services running revealed critical issues (e.g., repository queries, CORS) that unit tests missed. Established practice of testing endpoints through gateway, not just direct service access.

**Version Control Workflow**
Feature branches with pull requests prevented main branch instability. Code reviews caught issues like missing null checks, improper exception handling, and security vulnerabilities before merge.

**Frontend State Management**
localStorage for JWT tokens simplified authentication but required careful cleanup on logout. Axios interceptors for automatic token injection reduced code duplication across API calls.

**Team Collaboration**
Regular communication (daily standups, bi-weekly detailed reviews) prevented knowledge silos. Shared documentation (Confluence/README files) ensured everyone understood system architecture. Breaking work into small, independently testable units enabled parallel development.

## 8. Conclusion

The Fixed Deposit Management System project successfully demonstrated implementation of a production-grade microservices architecture with complete separation of concerns. The FD Account Service, as the core module, handles complex financial calculations, multi-party account management, and comprehensive transaction processing across 27 well-defined endpoints.

Key achievements include seamless integration of five independent microservices through Spring Cloud Gateway, implementation of secure JWT-based authentication, and development of a modern React-based user interface with complete CRUD functionality. The system supports both standard and customized FD accounts, real-time return calculations, premature withdrawal with penalty computation, and role-based access control for joint accounts.

Technical challenges, particularly around Hibernate lazy loading and API Gateway routing, provided valuable learning experiences in production-ready application development. The team's ability to identify root causes (removing isActive filters, implementing JOIN FETCH) and implement robust solutions demonstrates strong problem-solving capabilities.

The project's success can be attributed to clear division of responsibilities, consistent communication, and adherence to software engineering best practices including version control workflows, API documentation, comprehensive testing, and iterative development. The deliverable represents a fully functional banking application ready for deployment, with all services tested and integrated through a unified gateway, providing a foundation for future enhancements such as interest rate revisions, account closure workflows, and advanced reporting features.
