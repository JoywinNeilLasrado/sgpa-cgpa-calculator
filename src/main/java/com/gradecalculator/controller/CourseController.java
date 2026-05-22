package com.gradecalculator.controller;

import com.gradecalculator.dto.CourseRequest;
import com.gradecalculator.model.Course;
import com.gradecalculator.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.findAll());
    }

    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<List<Course>> getCoursesBySemester(@PathVariable Long semesterId) {
        return ResponseEntity.ok(courseService.findBySemesterId(semesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        return courseService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.create(
                request.getCourseCode(),
                request.getCourseName(),
                request.getCredits(),
                request.getSemesterId(),
                request.getFacultyId()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.update(
                id,
                request.getCourseCode(),
                request.getCourseName(),
                request.getCredits(),
                request.getSemesterId(),
                request.getFacultyId()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
