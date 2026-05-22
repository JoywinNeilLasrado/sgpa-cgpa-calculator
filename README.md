# GradePoint: Academic Grade Calculator

A professional Spring Boot application to calculate **SGPA** (Semester Grade Point Average) and **CGPA** (Cumulative Grade Point Average) following autonomous college grading regulations with role-based access control.

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-green)
![License](https://img.shields.io/badge/License-MIT-orange)

## 🎯 Features

### Core Functionality
- **SGPA Calculator** - Calculate semester-wise GPA
- **CGPA Calculator** - Calculate cumulative GPA across all semesters
- **Dashboard** - View all student results at a glance
- **Analytics** - Visual performance insights with charts
- **Transcript** - Generate & download official PDF transcripts
- **Role-Based Access** - Admin, Faculty, Student roles

### Authentication & Security
- **JWT Authentication** - Secure token-based login
- **Role-Based Access Control (RBAC)** - Different views per role
- **Password Change** - Self-service password management

### Technical
- **RESTful API** - Full backend endpoints
- **10-Point Grading Scale** - Complete grading scale (O to F)
- **Professional UI** - Academic-themed responsive interface
- **Data Management** - CRUD for students, courses, departments, enrollments

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

### Demo Accounts (auto-created)
| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Faculty | faculty | faculty123 |
| Student | student | student123 |

## 📄 Pages

| Page | Route | Description |
|------|-------|-------------|
| Home | `/` | Landing page with features |
| Login | `/login.html` | Authentication |
| Admin | `/admin.html` | Admin dashboard & management |
| Analytics | `/analytics.html` | Performance charts |
| Transcript | `/transcript.html` | PDF transcript generator |
| Faculty Grades | `/faculty-grades.html` | Grade entry by faculty |
| Student Dashboard | `/student-dashboard.html` | Student view |
| Profile | `/profile.html` | User profile |

## 📐 Grading Scale (10-Point)

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

## 🧮 Formulas

### SGPA (Semester Grade Point Average)
```
SGPA = Σ(Credit Points) / Σ(Course Credits)
```

### CGPA (Cumulative Grade Point Average)
```
CGPA = Σ(Credit Points excluding F) / Σ(Credits excluding F)
```

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Register new user
- `POST /api/auth/change-password` - Change password

### Students
- `GET /api/students` - List all students
- `POST /api/students` - Create student
- `GET /api/students/{id}` - Get student
- `PUT /api/students/{id}` - Update student
- `DELETE /api/students/{id}` - Delete student

### Departments
- `GET /api/departments` - List departments
- `POST /api/departments` - Create department
- `GET /api/departments/{id}` - Get department

### Courses
- `GET /api/courses` - List courses
- `POST /api/courses` - Create course
- `GET /api/courses/{id}` - Get course
- `PUT /api/courses/{id}` - Update course

### Enrollments
- `GET /api/enrollments` - List enrollments
- `POST /api/enrollments` - Create enrollment
- `GET /api/enrollments/student/{id}` - Student enrollments

### Grade Calculations
- `GET /api/sgpa/student/{id}/semester/{semId}` - SGPA for semester
- `GET /api/cgpa/student/{id}` - Overall CGPA
- `GET /api/cgpa/student/{id}/semester/{semId}` - CGPA up to semester
- `GET /api/students/{id}/dashboard` - Full dashboard

### Grade Scale
- `GET /api/grades/scale` - Get grading scale
- `GET /api/grades/from-marks/{marks}` - Get grade from marks

### Analytics
- `GET /api/analytics/overview` - Overall analytics
- `GET /api/analytics/department/{id}` - Dept analytics

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.2.0 (Java 21)
- **Database**: H2 in-memory DB
- **Security**: Spring Security + JWT (jjwt 0.12.3)
- **PDF Generation**: OpenPDF 1.3.30
- **Build Tool**: Maven
- **Frontend**: Vanilla HTML/CSS/JS

## 📂 Project Structure

```
src/main/java/com/gradecalculator/
├── SgpaCgpaCalculatorApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/          (15 Controllers)
├── dto/                (30+ DTOs)
├── exception/
│   └── GlobalExceptionHandler.java
├── model/              (7 Models)
├── repository/        (6 Repositories)
├── security/           (4 Security Classes)
├── service/           (12 Services)

src/main/resources/META-INF/resources/
├── admin.html
├── analytics.html
├── faculty-grades.html
├── home.html
├── index.html
├── login.html
├── profile.html
├── student-dashboard.html
├── student.html
├── transcript.html
├── css/theme.css
└── js/
    └── api-v2.js
```

## 🎨 UI Features

- **Responsive Design** - Desktop and mobile friendly
- **Consistent Theme** - Professional crimson/gold palette
- **Navigation Bar** - Easy page switching
- **Smooth Animations** - Modern transitions
- **PDF Export** - Transcript downloadable as PDF

## 📝 License

MIT License - Feel free to use and modify!

---

**Built with ❤️ for academic excellence**