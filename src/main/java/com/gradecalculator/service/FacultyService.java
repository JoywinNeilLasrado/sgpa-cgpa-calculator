package com.gradecalculator.service;

import com.gradecalculator.model.AppUser;
import com.gradecalculator.model.Course;
import com.gradecalculator.repository.AppUserRepository;
import com.gradecalculator.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacultyService {

    private final AppUserRepository userRepository;
    private final CourseRepository courseRepository;

    public FacultyService(AppUserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<AppUser> findAllFacultyMembers() {
        return userRepository.findByRole(AppUser.Role.FACULTY);
    }

    @Transactional
    public void deleteFacultyMember(Long id) {
        AppUser faculty = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Faculty member not found"));

        // Nullify faculty in assigned courses
        List<Course> courses = courseRepository.findByFacultyId(id);
        for (Course course : courses) {
            course.setFaculty(null);
            courseRepository.save(course);
        }

        userRepository.delete(faculty);
    }

    @Transactional
    public void assignFacultyToCourse(Long courseId, Long facultyId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        AppUser faculty = userRepository.findById(facultyId)
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found"));

        course.setFaculty(faculty);
        courseRepository.save(course);
    }

    @Transactional
    public AppUser updateFacultyMember(Long id, String name, String username, String email, String department) {
        AppUser faculty = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Faculty member not found"));

        if (!faculty.getUsername().equalsIgnoreCase(username) && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Faculty name cannot be empty");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        faculty.setName(name.trim());
        faculty.setUsername(username.trim());
        faculty.setEmail(email != null ? email.trim() : null);
        faculty.setDepartment(department != null ? department.trim() : null);

        return userRepository.save(faculty);
    }
}
