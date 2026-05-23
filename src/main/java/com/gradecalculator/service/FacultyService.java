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
}
