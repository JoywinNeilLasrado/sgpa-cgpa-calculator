# 1. OBJECTIVE
Add a complete user authentication and authorization system with role-based access control (Admin, Faculty, Student), PDF transcript generation, faculty grade management panel, student-only views, charting data for GPA trends, class rankings, and course-wise analytics to the SGPA/CGPA Calculator application.

# 2. CONTEXT SUMMARY
- **Current System**: Spring Boot 3.2.0 REST API with H2 database for SGPA/CGPA calculations
- **Existing Components**: Student, Course, Enrollment, Semester, LetterGrade entities; GradeCalculationService; DashboardService
- **New Requirements**:
  - JWT-based authentication with BCrypt password hashing
  - Role-based authorization (Admin, Faculty, Student)
  - Faculty grade entry/update panel
  - Student-only view (view own results)
  - PDF transcript generation (A4 format)
  - Charts data for SGPA/CGPA trends
  - Class ranking/topper list
  - Course-wise analytics
- **Dependencies Needed**: Spring Security, jjwt (JWT library), OpenPDF/iText (PDF generation), Lombok

# 3. APPROACH OVERVIEW
Implement a phased approach:
1. Add security infrastructure (Spring Security + JWT) with User entity and authentication endpoints
2. Add role-based authorization at controller level
3. Create faculty grade management endpoints and DTOs
4. Add PDF transcript generation with OpenPDF
5. Add analytics endpoints for charts, rankings, and course-wise stats
6. Keep API responses decoupled (pure JSON) for frontend consumption

# 4. IMPLEMENTATION STEPS

## Step 1: Add Dependencies
- **Goal**: Add required Maven dependencies
- **Method**: Update pom.xml with Spring Security, JWT, PDF libraries, Lombok
- **Reference**: pom.xml

## Step 2: Modularize Package Structure
- **Goal**: Organize code into clear, modular packages
- **Method**: Restructure into layered architecture:
  ```
  com.gradecalculator/
  ├── config/          # Configuration classes (Security, App)
  ├── controller/     # REST controllers
  ├── dto/            # Data Transfer Objects
  ├── model/         # JPA Entities
  ├── repository/    # Data JPA repositories
  ├── service/       # Business logic services
  ├── security/     # JWT, Auth filters, Security config
  ├── dto/request/   # Request DTOs
  ├── dto/response/  # Response DTOs
  ├── exception/    # Custom exceptions
  └── util/          # Utility classes
  ```
- **Reference**: Move existing files to appropriate packages

## Step 3: Create User Entity and Authentication Models
- **Goal**: Create User entity with roles and auth-related DTOs
- **Method**: Create User.java entity with username (StudentId), password (BCrypt-hashed), role enum (ADMIN, FACULTY, STUDENT). Create LoginRequest, LoginResponse, ChangePasswordRequest DTOs
- **Reference**: src/main/java/com/gradecalculator/model/User.java, dto/request/AuthRequests.java, dto/response/AuthResponses.java

## Step 4: Implement JWT Security Configuration (in security package)
- **Goal**: Configure Spring Security with JWT authentication filter
- **Method**: Create JwtTokenProvider (generate/validate JWT), JwtAuthenticationFilter, SecurityConfig (configure endpoints accessible by role)
- **Reference**: src/main/java/com/gradecalculator/security/JwtTokenProvider.java, JwtAuthenticationFilter.java, config/SecurityConfig.java

## Step 5: Create User Repository and Service
- **Goal**: Provide user CRUD operations and authentication service
- **Method**: Create UserRepository, UserService (register, login, change password, assign roles)
- **Reference**: src/main/java/com/gradecalculator/repository/UserRepository.java, service/UserService.java

## Step 6: Add Authentication Controller (in controller package)
- **Goal**: Create login, register, change password endpoints
- **Method**: Create AuthController with POST /api/auth/login, POST /api/auth/register (admin only), PUT /api/auth/change-password
- **Reference**: src/main/java/com/gradecalculator/controller/AuthController.java

## Step 7: Update Student Entity with User Reference
- **Goal**: Link Student entity to User for authentication
- **Method**: Add User reference to Student entity, update constructors
- **Reference**: src/main/java/com/gradecalculator/model/Student.java

## Step 8: Create Faculty Grade Management Panel (service + controller)
- **Goal**: Allow faculty to enter/update student grades
- **Method**: Create GradeManagementService and FacultyGradeController with endpoints to get enrollments by course/semester and update grades
- **Reference**: src/main/java/com/gradecalculator/service/GradeManagementService.java, controller/FacultyGradeController.java

## Step 9: Create Student-Only View Endpoints
- **Goal**: Restrict student access to only their own data
- **Method**: Update StudentController/DashboardController endpoints to use @PreAuthorize("hasRole('STUDENT')") and verify student ID matches logged-in user
- **Reference**: Updated controller files with role annotations

## Step 10: Implement PDF Transcript Generation (service)
- **Goal**: Generate professional A4 PDF transcripts
- **Method**: Create TranscriptPdfService using OpenPDF with: A4 size, university header, semester breakdown, signature lines, watermark
- **Reference**: src/main/java/com/gradecalculator/service/TranscriptPdfService.java

## Step 11: Add Analytics Endpoints (service + controller)
- **Goal**: Provide data for frontend charts and analytics
- **Method**: Create AnalyticsService with:
  - SGPA/CGPA trend data (semester-wise array for charts)
  - Class ranking endpoint (sorted by CGPA descending)
  - Topper list (top N students by CGPA)
  - Course-wise analytics (average grade, grade distribution per course)
- **Reference**: src/main/java/com/gradecalculator/service/AnalyticsService.java, controller/AnalyticsController.java

## Step 12: Add Data Initializer for Demo Users
- **Goal**: Initialize demo users for testing
- **Method**: Update DataInitializer to create demo Admin, Faculty, and Student users with BCrypt-encoded passwords
- **Reference**: src/main/java/com/gradecalculator/service/DataInitializer.java

# 5. TESTING AND VALIDATION
- **Authentication**: Test login with invalid credentials returns 401, valid credentials returns JWT token
- **Authorization**: Test students accessing other student data returns 403 Forbidden
- **Faculty Panel**: Test faculty can update grades, students cannot
- **PDF Generation**: Verify PDF downloads with correct content, A4 format, watermark visible
- **Rankings**: Verify ranking list sorted correctly by CGPA descending
- **Course Analytics**: Verify average calculations match expected values for known courses
- **Chart Data**: Verify array format suitable for Chart.js consumption (e.g., {semester: "Sem 1", sgpa: 8.5})

API Test Examples:
```
POST /api/auth/login -> returns JWT token
GET /api/students/me (as student) -> returns own student data only
GET /api/transcript/pdf/1 -> downloads PDF file
GET /api/analytics/rankings -> returns sorted student list by CGPA
GET /api/analytics/course/CS101 -> returns grade distribution for course
GET /api/analytics/sgpa-trends/1 -> returns SGPA array for chart
```
