# SGPA & CGPA Calculator

A Spring Boot application to calculate Semester Grade Point Average (SGPA) and Cumulative Grade Point Average (CGPA) based on the 10-point grading scale.

## Features

- **SGPA Calculator** - Calculate semester-wise GPA
- **CGPA Calculator** - Calculate cumulative GPA across all semesters
- **10-Point Grading Scale** - Full grading scale display
- **REST API** - Backend endpoints for integration
- **Professional UI** - Clean, academic-themed interface

## Technology Stack

- **Backend**: Spring Boot 3.2.0 (Java 21)
- **Database**: H2 in-memory
- **Build Tool**: Maven

## Grading Scale

| Letter Grade | Performance | Marks | Points |
|--------------|-------------|-------|--------|
| O | Outstanding | 90-100 | 10 |
| A+ | Excellent | 80-89 | 9 |
| A | Very Good | 70-79 | 8 |
| B+ | Good | 60-69 | 7 |
| B | Above Average | 55-59 | 6 |
| C | Average | 50-54 | 5 |
| P | Pass | 40-49 | 4 |
| F | Fail | 00-39 | 0 |

## Formulas

### SGPA
```
SGPA = Σ(Course Credits × Grade Points) / Σ(Course Credits)
```

### CGPA
```
CGPA = Σ(Credit Points excluding F grades) / Σ(Credits excluding F grades)
```

## Building & Running

```bash
# Build the project
mvn clean package

# Run the application  
mvn spring-boot:run

# Or run the JAR
java -jar target/sgpa-cgpa-calculator-1.0.0.jar
```

The application runs on **port 8080**.

## API Endpoints

- `GET /api/students` - List students
- `GET /api/semesters` - List semesters
- `GET /api/courses` - List courses
- `GET /api/sgpa/student/{id}/semester/{id}` - Calculate SGPA
- `GET /api/cgpa/student/{id}` - Calculate overall CGPA
- `GET /api/cgpa/student/{id}/semester/{semId}` - Calculate CGPA up to semester

## Web Interface

Access the UI at: `http://localhost:8080/`

## Project Structure

```
src/main/java/com/gradecalculator/
├── SgpaCgpaCalculatorApplication.java
├── controller/
│   ├── GradeController.java
│   └── HomeController.java
├── dto/
│   ├── CgpaResponse.java
│   ├── EnrollmentRequest.java
│   └── SgpaResponse.java
├── model/
│   ├── Course.java
│   ├── Enrollment.java
│   ├── LetterGrade.java
│   ├── Semester.java
│   └── Student.java
├── repository/
│   ├── CourseRepository.java
│   ├── EnrollmentRepository.java
│   ├── SemesterRepository.java
│   └── StudentRepository.java
└── service/
    ├── DataInitializer.java
    └── GradeCalculationService.java
```

## License

MIT