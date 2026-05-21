# SGPA CGPA Calculator - Spring Boot Project

This is a Spring Boot 3.2.0 application that calculates Semester Grade Point Average (SGPA) and Cumulative Grade Point Average (CGPA) based on the 10-point grading scale.

## Project Structure

```
src/
├── main/
│   ├── java/com/gradecalculator/
│   │   ├── SgpaCgpaCalculatorApplication.java  # Main Spring Boot application
│   │   ├── controller/
│   │   │   └── GradeController.java          # REST API endpoints
│   │   ├── service/
│   │   │   ├── GradeCalculationService.java # SGPA/CGPA calculation logic
│   │   │   └── DataInitializer.java         # Sample data loader
│   │   ├── model/
│   │   │   ├── Course.java                  # Course entity
│   │   │   ├── Enrollment.java              # Enrollment entity
│   │   │   ├── Semester.java                # Semester entity
│   │   │   ├── Student.java                 # Student entity
│   │   │   └── LetterGrade.java              # Grade enum (O, A+, A, B+, B, C, P, F)
│   │   ├── dto/
│   │   │   ├── SgpaResponse.java           # SGPA response DTO
│   │   │   ├── CgpaResponse.java            # CGPA response DTO
│   │   │   └── EnrollmentRequest.java       # Enrollment request DTO
│   │   └── repository/
│   │       ├── StudentRepository.java
│   │       ├── SemesterRepository.java
│   │       ├── CourseRepository.java
│   │       └── EnrollmentRepository.java
│   └── resources/
│       └── application.properties           # Application config
└── test/
    └── java/com/gradecalculator/             # Test files (to be added)
```

## Grading Scale

| Letter Grade | Performance Level | Marks Range | Grade Points |
|--------------|-------------------|-------------|--------------|
| O            | Outstanding       | 90-100     | 10           |
| A+           | Excellent        | 80-89      | 09           |
| A            | Very Good        | 70-79      | 08           |
| B+           | Good            | 60-69      | 07           |
| B            | Above Average   | 55-59      | 06           |
| C            | Average         | 50-54      | 05           |
| P            | Pass             | 40-49      | 04           |
| F            | Fail            | 00-39      | 00           |

## Formulas

### SGPA (Semester Grade Point Average)
```
SGPA = Σ(Course Credits × Grade Points) / Σ(Course Credits)
```

### CGPA (Cumulative Grade Point Average)
```
CGPA = Σ(Credit Points excluding F grades) / Σ(Course Credits excluding F grades)
```

## Running the Application

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/sgpa-cgpa-calculator-1.0.0.jar
```

The application runs on port 8080 by default.

## API Endpoints

### Students
- `GET /api/students` - List all students
- `GET /api/students/{id}` - Get student by ID
- `POST /api/students` - Create new student

### Semesters
- `GET /api/semesters` - List all semesters
- `POST /api/semesters` - Create new semester

### Courses
- `GET /api/courses` - List all courses
- `GET /api/courses/semester/{semesterId}` - Get courses by semester
- `POST /api/courses` - Create new course

### Enrollments
- `GET /api/enrollments/student/{studentId}` - Get student enrollments
- `GET /api/enrollments/student/{studentId}/semester/{semesterId}` - Get enrollments by semester
- `POST /api/enrollments` - Create enrollment

### Grade Calculations
- `GET /api/sgpa/student/{studentId}/semester/{semesterId}` - Calculate SGPA
- `GET /api/cgpa/student/{studentId}/semester/{semesterId}` - Calculate CGPA (up to semester)
- `GET /api/cgpa/student/{studentId}` - Calculate overall CGPA
- `GET /api/grade-scale` - Get grade points scale

## Sample Data

The application initializes with sample data:
- 3 semesters
- 1 student (John Doe, CS2024001)
- 12 courses across 3 semesters
- 12 enrollments with various grades

This allows testing the SGPA/CGPA calculations immediately after startup.