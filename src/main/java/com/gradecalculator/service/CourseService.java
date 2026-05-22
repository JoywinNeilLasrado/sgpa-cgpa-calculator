package com.gradecalculator.service;

import com.gradecalculator.model.Course;
import com.gradecalculator.model.Semester;
import com.gradecalculator.model.AppUser;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.SemesterRepository;
import com.gradecalculator.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AppUserRepository userRepository;

    public CourseService(
            CourseRepository courseRepository,
            SemesterRepository semesterRepository,
            EnrollmentRepository enrollmentRepository,
            AppUserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.semesterRepository = semesterRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public List<Course> findBySemesterId(Long semesterId) {
        return courseRepository.findBySemesterId(semesterId);
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    public Course create(String courseCode, String courseName, Object creditsValue, Object semesterIdValue, Object facultyIdValue) {
        Long semesterId = parseLong(semesterIdValue, "Semester is required");
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));
        validateCourse(courseCode, courseName);
        Integer credits = parsePositiveInt(creditsValue, "Credits must be greater than zero");
        courseRepository.findByCourseCodeAndSemesterId(courseCode, semesterId)
                .ifPresent(course -> {
                    throw new IllegalArgumentException("A course with this code already exists in the semester");
                });

        AppUser faculty = null;
        if (facultyIdValue != null && !facultyIdValue.toString().trim().isEmpty() && !facultyIdValue.toString().equalsIgnoreCase("none") && !facultyIdValue.toString().equals("0")) {
            Long facultyId = parseLong(facultyIdValue, "Invalid faculty ID");
            faculty = userRepository.findById(facultyId)
                    .orElseThrow(() -> new IllegalArgumentException("Faculty user not found"));
            if (faculty.getRole() != AppUser.Role.FACULTY) {
                throw new IllegalArgumentException("Assigned user must be a faculty member");
            }
        }

        Course course = new Course(courseCode, courseName, credits);
        course.setSemester(semester);
        course.setFaculty(faculty);
        return courseRepository.save(course);
    }

    public Course update(Long id, String courseCode, String courseName, Object creditsValue, Object semesterIdValue, Object facultyIdValue) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Long semesterId = parseLong(semesterIdValue, "Semester is required");
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));
        validateCourse(courseCode, courseName);

        courseRepository.findByCourseCodeAndSemesterId(courseCode, semesterId)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A course with this code already exists in the semester");
                });

        AppUser faculty = null;
        if (facultyIdValue != null && !facultyIdValue.toString().trim().isEmpty() && !facultyIdValue.toString().equalsIgnoreCase("none") && !facultyIdValue.toString().equals("0")) {
            Long facultyId = parseLong(facultyIdValue, "Invalid faculty ID");
            faculty = userRepository.findById(facultyId)
                    .orElseThrow(() -> new IllegalArgumentException("Faculty user not found"));
            if (faculty.getRole() != AppUser.Role.FACULTY) {
                throw new IllegalArgumentException("Assigned user must be a faculty member");
            }
        }

        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setCredits(parsePositiveInt(creditsValue, "Credits must be greater than zero"));
        course.setSemester(semester);
        course.setFaculty(faculty);
        return courseRepository.save(course);
    }

    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        enrollmentRepository.deleteAll(enrollmentRepository.findByCourseId(id));
        courseRepository.delete(course);
    }

    private void validateCourse(String courseCode, String courseName) {
        validateText(courseCode, "Course code is required");
        validateText(courseName, "Course name is required");
    }

    private void validateText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private Long parseLong(Object value, String message) {
        try {
            return Long.parseLong(value.toString());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private Integer parsePositiveInt(Object value, String message) {
        try {
            int parsed = Integer.parseInt(value.toString());
            if (parsed < 1) {
                throw new IllegalArgumentException(message);
            }
            return parsed;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(message);
        }
    }
}
