# GradePoint: Academic Grade Calculator

A professional Spring Boot application to calculate **SGPA** (Semester Grade Point Average) and **CGPA** (Cumulative Grade Point Average) following autonomous college grading regulations with a modern web interface.

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-green)
![License](https://img.shields.io/badge/License-MIT-orange)

## ✨ Features

### Core Functionality
- **SGPA Calculator** - Calculate semester-wise GPA
- **CGPA Calculator** - Calculate cumulative GPA across all semesters
- **Dashboard** - View all student results at a glance
- **Analytics** - Visual performance insights with charts
- **Transcript** - Printable official academic transcript

### Technical
- **REST API** - Full backend endpoints for integration
- **10-Point Grading Scale** - Complete grading scale (O to F)
- **Professional UI** - Academic-themed responsive interface
- **Data Management** - CRUD for students, courses, enrollments

## 🚀 Quick Start

```bash
# Clone and build
git clone https://github.com/JoywinNeilLasrado/sgpa-cgpa-calculator.git
cd sgpa-cgpa-calculator

# Build
mvn clean package

# Run
java -jar target/sgpa-cgpa-calculator-1.0.0.jar
```

Access at: **http://localhost:8080**

## 🌐 Pages

| Page | Route | Description |
|------|-------|-------------|
| Calculator | `/` | Main SGPA/CGPA calculator & data management |
| Analytics | `/analytics` | Performance charts & statistics |
| Transcript | `/transcript` | Printable academic transcript |

## 📊 Grading Scale (10-Point)

| Letter Grade | Performance | Marks Range | Grade Points |
|--------------|-------------|--------------|---------------|
| O | Outstanding | 90-100 | 10 |
| A+ | Excellent | 80-89 | 9 |
| A | Very Good | 70-79 | 8 |
| B+ | Good | 60-69 | 7 |
| B | Above Average | 55-59 | 6 |
| C | Average | 50-54 | 5 |
| P | Pass | 40-49 | 4 |
| F | Fail | 00-39 | 0 |

## 📐 Formulas

### SGPA (Semester Grade Point Average)
```
SGPA = Σ(Course Credits × Grade Points) / Σ(Course Credits)
```

### CGPA (Cumulative Grade Point Average)
```
CGPA = Σ(Credit Points excluding F grades) / Σ(Credits excluding F grades)
```

## 🔌 API Endpoints

### Students
- `GET /api/students` - List all students
- `POST /api/students` - Create student
- `GET /api/students/{id}` - Get student by ID
- `DELETE /api/students/{id}` - Delete student

### Semesters
- `GET /api/semesters` - List semesters
- `POST /api/semesters` - Create semester

### Courses
- `GET /api/courses` - List courses
- `POST /api/courses` - Create course

### Enrollments
- `GET /api/enrollments` - List enrollments
- `POST /api/enrollments` - Create enrollment

### Grade Calculations
- `GET /api/sgpa/student/{id}/semester/{semId}` - SGPA for a semester
- `GET /api/cgpa/student/{id}` - Overall CGPA
- `GET /api/cgpa/student/{id}/semester/{semId}` - CGPA up to semester
- `GET /api/students/{id}/dashboard` - Full dashboard with all stats

### Grade Scale
- `GET /api/grades/scale` - Get grading scale
- `GET /api/grades/from-marks/{marks}` - Get grade from marks

## 🛠 Technology Stack

- **Backend**: Spring Boot 3.2.0 (Java 21)
- **Database**: H2 in-memory
- **Build Tool**: Maven
- **Frontend**: Vanilla HTML/CSS/JS

## 📁 Project Structure

```
src/main/java/com/gradecalculator/
├── SgpaCgpaCalculatorApplication.java
├── controller/
│   ├── CourseController.java
│   ├── DashboardController.java
│   ├── EnrollmentController.java
│   ├── GradeController.java
│   ├── HomeController.java
│   ├── SemesterController.java
│   └── StudentController.java
├── dto/
│   ├── CourseRequest.java
│   ├── CourseResultResponse.java
│   ├── DashboardResponse.java
│   ├── SemesterResultResponse.java
│   └── ...
├── exception/
│   └── GlobalExceptionHandler.java
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
│   ├── CourseService.java
│   ├── DashboardService.java
│   ├── DataInitializer.java
│   ├── EnrollmentService.java
│   ├── GradeCalculationService.java
│   ├── SemesterService.java
│   └── StudentService.java
```

## 🎨 UI Features

- **Responsive Design** - Works on desktop and mobile
- **Consistent Theme** - Professional crimson/gold palette
- **Navigation Bar** - Easy switching between pages
- **Animations** - Smooth transitions
- **Print Support** - Transcript printable as PDF

## 📝 License

MIT License - Feel free to use and modify!

---

**Built with ❤️ for academic excellence**