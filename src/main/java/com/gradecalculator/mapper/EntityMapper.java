package com.gradecalculator.mapper;

import com.gradecalculator.model.Student;
import com.gradecalculator.model.Course;
import com.gradecalculator.model.Semester;
import com.gradecalculator.model.Enrollment;
import com.gradecalculator.dto.request.StudentRequest;
import com.gradecalculator.dto.response.StudentResponse;
import com.gradecalculator.dto.response.CourseResponse;
import com.gradecalculator.dto.response.SemesterResponse;
import com.gradecalculator.dto.response.EnrollmentResponse;
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
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
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
     * Converts StudentRequest DTO to Student entity (for creation).
     */
    Student toStudentEntity(StudentRequest request);

    /**
     * Updates an existing Student entity from StudentRequest.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
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
    @Mapping(target = "facultyName", source = "faculty.name")
    CourseResponse toCourseResponse(Course course);

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
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", source = "student.name")
    @Mapping(target = "studentRollNumber", source = "student.studentId")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseName", source = "course.courseName")
    @Mapping(target = "courseCode", source = "course.courseCode")
    @Mapping(target = "credits", source = "course.credits")
    @Mapping(target = "courseType", source = "course.courseType")
    @Mapping(target = "semesterId", source = "course.semester.id")
    @Mapping(target = "semesterNumber", source = "course.semester.semesterNumber")
    @Mapping(target = "grade", expression = "java(enrollment.getGrade() != null ? enrollment.getGrade().getGrade() : null)")
    @Mapping(target = "gradePoints", expression = "java(enrollment.getGrade() != null ? enrollment.getGrade().getGradePoints() : 0)")
    @Mapping(target = "creditPoints", expression = "java(enrollment.getCreditPoints())")
    @Mapping(target = "cieMarks", source = "marks.cieMarks")
    @Mapping(target = "cieTheoryMarks", source = "marks.cieTheoryMarks")
    @Mapping(target = "cieLabMarks", source = "marks.cieLabMarks")
    @Mapping(target = "test1Marks", source = "marks.test1Marks")
    @Mapping(target = "test2Marks", source = "marks.test2Marks")
    @Mapping(target = "assignmentMarks", source = "marks.assignmentMarks")
    @Mapping(target = "oaaMarks", source = "marks.oaaMarks")
    @Mapping(target = "regularLabMarks", source = "marks.regularLabMarks")
    @Mapping(target = "labTestMarks", source = "marks.labTestMarks")
    @Mapping(target = "labRecordMarks", source = "marks.labRecordMarks")
    @Mapping(target = "seeMarks", source = "marks.seeMarks")
    @Mapping(target = "graceMarks", source = "marks.graceMarks")
    @Mapping(target = "totalMarks", source = "marks.totalMarks")
    EnrollmentResponse toEnrollmentResponse(Enrollment enrollment);
}