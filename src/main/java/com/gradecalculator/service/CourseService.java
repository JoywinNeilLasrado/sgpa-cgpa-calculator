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
import com.gradecalculator.util.ValidationUtil;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing academic courses, credit definitions, semester allocations, and faculty assignments.
 */
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

    /**
     * Retrieves all course registry records.
     *
     * @return a list of all course entities
     */
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    /**
     * Retrieves all courses assigned to a specific academic semester.
     *
     * @param semesterId the database identifier of the semester
     * @return a list of courses allocated to the matching semester
     */
    public List<Course> findBySemesterId(Long semesterId) {
        ValidationUtil.requireNonNull(semesterId, "Semester ID cannot be null");
        return courseRepository.findBySemesterId(semesterId);
    }

    /**
     * Finds a single course by its unique primary key identifier.
     *
     * @param id the database identifier of the course
     * @return an Optional containing the Course if found
     */
    public Optional<Course> findById(Long id) {
        ValidationUtil.requireNonNull(id, "Course ID cannot be null");
        // Optional.empty() will be returned by repository if not found
        return courseRepository.findById(id);
    }

    /**
     * Creates a new course record with default course type (THEORY).
     *
     * @param courseCode      the unique alphanumeric course code
     * @param courseName      the descriptive course name
     * @param creditsValue    the number of credits (must resolve to a positive integer)
     * @param semesterIdValue the target semester ID
     * @param facultyIdValue  the faculty member ID (optional, can be empty or null)
     * @return the saved Course entity
     */
    public Course create(String courseCode, String courseName, Object creditsValue, Object semesterIdValue, Object facultyIdValue) {
        return create(courseCode, courseName, creditsValue, semesterIdValue, facultyIdValue, com.gradecalculator.model.CourseType.THEORY);
    }

    /**
     * Creates a new course record with complete parameters.
     *
     * @param courseCode      the unique alphanumeric course code
     * @param courseName      the descriptive course name
     * @param creditsValue    the number of credits (must resolve to a positive integer)
     * @param semesterIdValue the target semester ID
     * @param facultyIdValue  the faculty member ID (optional, can be empty or null)
     * @param courseType      the type of course (THEORY/LAB)
     * @return the saved Course entity
     */
    public Course create(String courseCode, String courseName, Object creditsValue, Object semesterIdValue, Object facultyIdValue, com.gradecalculator.model.CourseType courseType) {
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
        if (courseType != null) {
            course.setCourseType(courseType);
        }
        course.setSemester(semester);
        course.setFaculty(faculty);
        return courseRepository.save(course);
    }

    /**
     * Updates an existing course record with default course type (THEORY).
     *
     * @param id              the database identifier of the course to update
     * @param courseCode      the alphanumeric course code
     * @param courseName      the course name
     * @param creditsValue    the credit value
     * @param semesterIdValue the semester ID
     * @param facultyIdValue  the faculty ID
     * @return the updated Course entity
     */
    public Course update(Long id, String courseCode, String courseName, Object creditsValue, Object semesterIdValue, Object facultyIdValue) {
        return update(id, courseCode, courseName, creditsValue, semesterIdValue, facultyIdValue, com.gradecalculator.model.CourseType.THEORY);
    }

    /**
     * Updates an existing course record with full configuration parameters.
     *
     * @param id              the database identifier of the course to update
     * @param courseCode      the alphanumeric course code
     * @param courseName      the course name
     * @param creditsValue    the credit value
     * @param semesterIdValue the semester ID
     * @param facultyIdValue  the faculty ID
     * @param courseType      the course type (THEORY/LAB)
     * @return the updated Course entity
     */
    public Course update(Long id, String courseCode, String courseName, Object creditsValue, Object semesterIdValue, Object facultyIdValue, com.gradecalculator.model.CourseType courseType) {
        if (id == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
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
        if (courseType != null) {
            course.setCourseType(courseType);
        }
        course.setSemester(semester);
        course.setFaculty(faculty);
        return courseRepository.save(course);
    }

    /**
     * Deletes a course registry record and all dependent student course enrollments.
     *
     * @param id the database identifier of the course to delete
     */
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
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
        ValidationUtil.requireNonBlank(value, message);
    }

    private Long parseLong(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        try {
            return Long.parseLong(value.toString());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private Integer parsePositiveInt(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
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

    /**
     * Retrieves all course registries assigned to a specific faculty user profile.
     *
     * @param facultyId the primary key identifier of the faculty user
     * @return a list of courses matching the faculty assignment
     */
    public List<Course> findByFacultyId(Long facultyId) {
        if (facultyId == null) {
            return List.of();
        }
        return courseRepository.findByFacultyId(facultyId);
    }
}

