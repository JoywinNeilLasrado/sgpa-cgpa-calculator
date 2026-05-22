package com.gradecalculator.service;

import com.gradecalculator.model.*;
import com.gradecalculator.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Data initializer with multiple students across semesters
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private StudentRepository studentRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private AppUserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private DepartmentRepository departmentRepository;

    // BCrypt hash for "password123"
    private static final String PASS_HASH = "$2a$10$N9qo8uLOknjlSew6UoOqZuJaeNpKR3TmK7JvGqgCWzWldJ5m7w1xGy";

    @Override
    public void run(String... args) throws Exception {
        if (departmentRepository.count() == 0) {
            departmentRepository.save(new Department("Computer Science", "CS"));
            departmentRepository.save(new Department("Information Technology", "IT"));
            departmentRepository.save(new Department("Electronics & Communication", "ECE"));
        }

        if (studentRepository.count() > 0) return;

        AppUser adminUser = createUserIfNotExists("admin", "admin", AppUser.Role.ADMIN);
        AppUser defaultFaculty = createUserIfNotExists("faculty", "faculty", AppUser.Role.FACULTY);
        AppUser profJones = createUserIfNotExists("prof.jones", "password123", AppUser.Role.FACULTY);
        AppUser profSmith = createUserIfNotExists("prof.smith", "password123", AppUser.Role.FACULTY);
        AppUser profDavis = createUserIfNotExists("prof.davis", "password123", AppUser.Role.FACULTY);
        
        // Additional faculty members (generated like students)
        String[] extraFacultyUsernames = {"prof.williams", "prof.brown", "prof.taylor"};
        for (String fu : extraFacultyUsernames) {
            createUserIfNotExists(fu, "password123", AppUser.Role.FACULTY);
        }

        List<Semester> semesters = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            semesters.add(semesterRepository.save(new Semester(i)));
        }

        String[][] codeMatrix = {
            {"CS101", "MA101", "PH101", "EN101", "CS102"},
            {"CS201", "MA201", "PH201", "EN201", "CS202"},
            {"CS301", "MA301", "CS302", "CS303", "MA302"},
            {"CS401", "CS402", "CS403", "MA401", "CS404"},
            {"CS501", "CS502", "CS503", "CS504", "CS505"},
            {"CS601", "CS602", "CS603", "CS604", "CS606"}
        };
        String[] names = {"Programming", "Mathematics", "Physics", "English", "Electronics"};

        List<List<Course>> allCourses = new ArrayList<>();
        for (int s = 0; s < 6; s++) {
            List<Course> semCourses = new ArrayList<>();
            for (int c = 0; c < 5; c++) {
                Course course = new Course(codeMatrix[s][c], names[c] + " " + (s + 1), 4);
                course.setSemester(semesters.get(s));

                // Assign faculty
                String code = codeMatrix[s][c];
                if (s < 3) {
                    if (code.startsWith("CS")) {
                        course.setFaculty(profJones);
                    } else if (code.startsWith("MA") || code.startsWith("PH") || code.startsWith("EN")) {
                        course.setFaculty(profSmith);
                    } else {
                        course.setFaculty(defaultFaculty);
                    }
                } else {
                    course.setFaculty(profDavis);
                }

                semCourses.add(courseRepository.save(course));
            }
            allCourses.add(semCourses);
        }

        String studentPasswordHash = passwordEncoder.encode("password123");

        // 12 students divided into different branches with different grades
        createStudent("CS2024001", "Alice Johnson", "alice", "Computer Science", allCourses, LetterGrade.O, LetterGrade.A_PLUS, studentPasswordHash);
        createStudent("CS2024002", "Bob Smith", "bob", "Computer Science", allCourses, LetterGrade.A_PLUS, LetterGrade.A, studentPasswordHash);
        createStudent("CS2024003", "Charlie Brown", "charlie", "Computer Science", allCourses, LetterGrade.A_PLUS, LetterGrade.A, studentPasswordHash);
        createStudent("CS2024004", "Diana Prince", "diana", "Computer Science", allCourses, LetterGrade.A, LetterGrade.B_PLUS, studentPasswordHash);
        createStudent("CS2024005", "Edward Norton", "edward", "Information Technology", allCourses, LetterGrade.A, LetterGrade.B_PLUS, studentPasswordHash);
        createStudent("CS2024006", "Fiona Apple", "fiona", "Information Technology", allCourses, LetterGrade.B_PLUS, LetterGrade.A, studentPasswordHash);
        createStudent("CS2024007", "George Miller", "george", "Information Technology", allCourses, LetterGrade.B_PLUS, LetterGrade.B, studentPasswordHash);
        createStudent("CS2024008", "Hannah Lee", "hannah", "Information Technology", allCourses, LetterGrade.B, LetterGrade.C, studentPasswordHash);
        createStudent("CS2024009", "Ian Curtis", "ian", "Electronics & Communication", allCourses, LetterGrade.B, LetterGrade.C, studentPasswordHash);
        createStudent("CS2024010", "Julia Roberts", "julia", "Electronics & Communication", allCourses, LetterGrade.C, LetterGrade.B_PLUS, studentPasswordHash);
        createStudent("CS2024011", "Kevin Perry", "kevin", "Electronics & Communication", allCourses, LetterGrade.C, LetterGrade.C, studentPasswordHash);
        createStudent("CS2024012", "Laura Palmer", "laura", "Electronics & Communication", allCourses, LetterGrade.C, LetterGrade.P, studentPasswordHash);
    }

    private void createStudent(String roll, String name, String username, String branch, List<List<Course>> courses, LetterGrade best, LetterGrade avg, String passwordHash) {
        Student student = new Student(name, roll, branch);
        student.setUsername(username);
        student = studentRepository.save(student);
        
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setName(username); // default name same as username
        u.setPassword(passwordHash);
        u.setRole(AppUser.Role.STUDENT);
        try { userRepository.save(u); } catch (Exception e) {}

        // Enroll in first 4 semesters (20 courses)
        LetterGrade[] grades = {best, best, avg, avg, avg};
        for (int sem = 0; sem < 4; sem++) {
            for (int c = 0; c < 5; c++) {
                Enrollment e = new Enrollment(student, courses.get(sem).get(c), grades[c]);
                enrollmentRepository.save(e);
            }
        }
    }

    private AppUser createUserIfNotExists(String username, String pw, AppUser.Role role) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername(username);
            u.setName(username); // use username as default name
            u.setPassword(passwordEncoder.encode(pw));
            u.setRole(role);
            return userRepository.save(u);
        });
    }
}