package com.gradecalculator.mapper;

import com.gradecalculator.model.Student;
import com.gradecalculator.model.Course;
import com.gradecalculator.model.Semester;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.dto.request.StudentRequest;
import com.gradecalculator.dto.response.StudentResponse;
import com.gradecalculator.dto.response.CourseResponse;
import com.gradecalculator.dto.response.SemesterResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for automatic conversion between entities and DTOs.
 * <p>
 * Provides type-safe, compile-time checked mapping between:
 * <ul>
 *   <li>Student ↔ StudentRequest/StudentResponse</li>
 *   <li>Course ↔ CourseResponse</li>
 *   <li>Semester ↔ SemesterResponse</li>
 *   <li>Enrollment → EnrollmentResponse</li>
 * </ul>
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN,
    unmappedSourcePolicy = ReportingPolicy.WARN
)
public interface EntityMapper {

    // ═══════════════════════════════════════════════════════════════════════════════
    // STUDENT MAPPINGS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Converts Student entity to StudentResponse DTO.
     */
    StudentResponse toStudentResponse(Student student);

    /**
     * Converts Student entity to StudentResponse with optional semester info.
     */
    @Mapping(target = "semesterId", source = "semester.id")
    @Mapping(target = "semesterName", source = "semester.name")
    StudentResponse toStudentWithSemesterResponse(Student student);

    /**
     * Converts StudentRequest DTO to Student entity (for creation).
     */
    Student toStudentEntity(StudentRequest request);

    /**
     * Updates an existing Student entity from StudentRequest.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateStudentFromRequest(StudentRequest request, @MappingTarget Student student);

    /**
     * Converts a list of Student entities to StudentResponse DTOs.
     */
    List<StudentResponse> toStudentResponses(List<Student> students);

    // ═══════════════════════════════════════════════════════════════════════════════
    // COURSE MAPPINGS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Converts Course entity to CourseResponse DTO.
     */
    CourseResponse toCourseResponse(Course course);

    /**
     * Converts Course entity with faculty name.
     */
    @Mapping(target = "facultyName", source = "faculty.name")
    CourseResponse toCourseWithFacultyResponse(Course course);

    /**
     * Converts a list of Course entities to CourseResponse DTOs.
     */
    List<CourseResponse> toCourseResponses(List<Course> courses);

    // ═══════════════════════════════════════════════════════════════════════════════
    // SEMESTER MAPPINGS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Converts Semester entity to SemesterResponse DTO.
     */
    SemesterResponse toSemesterResponse(Semester semester);

    /**
     * Converts a list of Semester entities to SemesterResponse DTOs.
     */
    List<SemesterResponse> toSemesterResponses(List<Semester> semesters);

    // ═══════════════════════════════════════════════════════════════════════════════
    // ENROLLMENT MAPPINGS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Converts Enrollment entity to a basic response (without nested marks).
     */
    @Mapping(target = "studentName", source = "student.name")
    @Mapping(target = "studentRollNumber", source = "student.rollNumber")
    @Mapping(target = "courseName", source = "course.name")
    @Mapping(target = "courseCode", source = "course.courseCode")
    @Mapping(target = "credits", source = "course.credits")
    Enrollment toEnrollmentEntity(
            Long studentId,
            Long courseId,
            java.time.LocalDateTime enrolledAt
    );
}