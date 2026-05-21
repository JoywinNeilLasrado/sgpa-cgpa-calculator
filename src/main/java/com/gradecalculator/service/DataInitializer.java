package com.gradecalculator.service;

import com.gradecalculator.model.Course;
import com.gradecalculator.model.LetterGrade;
import com.gradecalculator.model.Semester;
import com.gradecalculator.model.Student;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Data initializer to populate the database with sample data for demonstration.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create semesters
        Semester sem1 = new Semester(1);
        Semester sem2 = new Semester(2);
        Semester sem3 = new Semester(3);
        
        sem1 = semesterRepository.save(sem1);
        sem2 = semesterRepository.save(sem2);
        sem3 = semesterRepository.save(sem3);

        // Create a sample student
        Student student = new Student("John Doe", "CS2024001");
        student = studentRepository.save(student);

        // Create courses for Semester 1
        Course cs101 = new Course("CS101", "Introduction to Programming", 4);
        cs101.setSemester(sem1);
        Course ma101 = new Course("MA101", "Mathematics I", 4);
        ma101.setSemester(sem1);
        Course ph101 = new Course("PH101", "Physics I", 3);
        ph101.setSemester(sem1);
        Course en101 = new Course("EN101", "English I", 2);
        en101.setSemester(sem1);

        // Create courses for Semester 2
        Course cs201 = new Course("CS201", "Data Structures", 4);
        cs201.setSemester(sem2);
        Course ma201 = new Course("MA201", "Mathematics II", 4);
        ma201.setSemester(sem2);
        Course ph201 = new Course("PH201", "Physics II", 3);
        ph201.setSemester(sem2);
        Course en201 = new Course("EN201", "Technical Communication", 2);
        en201.setSemester(sem2);

        // Create courses for Semester 3
        Course cs301 = new Course("CS301", "Algorithms", 4);
        cs301.setSemester(sem3);
        Course cs302 = new Course("CS302", "Database Systems", 3);
        cs302.setSemester(sem3);
        Course cs303 = new Course("CS303", "Operating Systems", 3);
        cs303.setSemester(sem3);
        Course ma301 = new Course("MA301", "Discrete Mathematics", 3);
        ma301.setSemester(sem3);

        courseRepository.saveAll(Arrays.asList(cs101, ma101, ph101, en101, cs201, ma201, ph201, en201, cs301, cs302, cs303, ma301));

        // Create enrollments (student's course registrations with grades)
        // Semester 1 grades - good performance
        Enrollment e1 = new Enrollment(student, cs101, LetterGrade.A);
        Enrollment e2 = new Enrollment(student, ma101, LetterGrade.B_PLUS);
        Enrollment e3 = new Enrollment(student, ph101, LetterGrade.A_PLUS);
        Enrollment e4 = new Enrollment(student, en101, LetterGrade.B);

        // Semester 2 grades - mixed performance
        Enrollment e5 = new Enrollment(student, cs201, LetterGrade.B);
        Enrollment e6 = new Enrollment(student, ma201, LetterGrade.C);
        Enrollment e7 = new Enrollment(student, ph201, LetterGrade.B_PLUS);
        Enrollment e8 = new Enrollment(student, en201, LetterGrade.A);

        // Semester 3 grades
        Enrollment e9 = new Enrollment(student, cs301, LetterGrade.A_PLUS);
        Enrollment e10 = new Enrollment(student, cs302, LetterGrade.O);
        Enrollment e11 = new Enrollment(student, cs303, LetterGrade.A);
        Enrollment e12 = new Enrollment(student, ma301, LetterGrade.B_PLUS);

        enrollmentRepository.saveAll(Arrays.asList(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12));

        // Create demo users for authentication (passwords are "password123")
        try {
            com.gradecalculator.model.User admin = new com.gradecalculator.model.User();
            admin.setUsername("admin");
            admin.setPassword("$2a$10$N9qo8uLOknjlSew6UoOqZuJaeNpKR3TmK7JvGqgCWzWldJ5m7w1xGy"); // password123
            admin.setRole(com.gradecalculator.model.User.Role.ADMIN);
            userRepository.save(admin);

            com.gradecalculator.model.User faculty = new com.gradecalculator.model.User();
            faculty.setUsername("faculty");
            faculty.setPassword("$2a$10$N9qo8uLOknjlSew6UoOqZuJaeNpKR3TmK7JvGqgCWzWldJ5m7w1xGy");
            faculty.setRole(com.gradecalculator.model.User.Role.FACULTY);
            userRepository.save(faculty);

            com.gradecalculator.model.User studentUser = new com.gradecalculator.model.User();
            studentUser.setUsername("CS2024001");
            studentUser.setPassword("$2a$10$N9qo8uLOknjlSew6UoOqZuJaeNpKR3TmK7JvGqgCWzWldJ5m7w1xGy");
            studentUser.setRole(com.gradecalculator.model.User.Role.STUDENT);
            userRepository.save(studentUser);
        } catch (Exception e) {
            // Users可能已存在，跳过
        }
    }
}