package com.gradecalculator.service;

import com.gradecalculator.model.Course;
import com.gradecalculator.model.Semester;
import com.gradecalculator.repository.CourseRepository;
import com.gradecalculator.repository.EnrollmentRepository;
import com.gradecalculator.repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseService(
            CourseRepository courseRepository,
            SemesterRepository semesterRepository,
            EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.semesterRepository = semesterRepository;
        this.enrollmentRepository = enrollmentRepository;
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

    public Course create(String courseCode, String courseName, Object creditsValue, Object semesterIdValue) {
        Long semesterId = parseLong(semesterIdValue, "Semester is required");
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found"));
        validateCourse(courseCode, courseName);
        Integer credits = parsePositiveInt(creditsValue, "Credits must be greater than zero");
        courseRepository.findByCourseCodeAndSemesterId(courseCode, semesterId)
                .ifPresent(course -> {
                    throw new IllegalArgumentException("A course with this code already exists in the semester");
                });

        Course course = new Course(courseCode, courseName, credits);
        course.setSemester(semester);
        return courseRepository.save(course);
    }

    public Course update(Long id, String courseCode, String courseName, Object creditsValue, Object semesterIdValue) {
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

        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setCredits(parsePositiveInt(creditsValue, "Credits must be greater than zero"));
        course.setSemester(semester);
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
