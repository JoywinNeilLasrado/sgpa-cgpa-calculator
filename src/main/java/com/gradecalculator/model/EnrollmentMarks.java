package com.gradecalculator.model;

import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Embeddable entity encapsulating all grades and marks details for an Enrollment.
 * Helps break down the large Enrollment class to comply with the SOLID Single Responsibility Principle.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentMarks {

    private Integer cieMarks;
    private Integer cieTheoryMarks;
    private Integer cieLabMarks;
    
    // Detailed Theory Marks
    private Integer test1Marks;
    private Integer test2Marks;
    private Integer assignmentMarks;
    private Integer oaaMarks;
    
    // Detailed Lab Marks
    private Integer regularLabMarks;
    private Integer labTestMarks;
    private Integer labRecordMarks;

    private Integer seeMarks;
    private Integer graceMarks;
    private Integer totalMarks;

    /**
     * Compute and determine the LetterGrade based on the detailed marks and the CourseType.
     * Encapsulates calculation rules in the embedded marks entity.
     *
     * @param type the CourseType (THEORY, LABORATORY, INTEGRATED)
     * @return the calculated LetterGrade
     */
    public LetterGrade calculateGrade(CourseType type) {
        if (type == null) {
            return null;
        }

        int finalCie = 0;
        int finalSee = (seeMarks != null ? seeMarks : 0) + (graceMarks != null ? graceMarks : 0);
        boolean passesCie = false;

        if (type == CourseType.THEORY) {
            boolean hasDetailMarks = (test1Marks != null || test2Marks != null
                    || assignmentMarks != null || oaaMarks != null);
            if (hasDetailMarks) {
                int t1 = (test1Marks != null ? test1Marks : 0);
                int t2 = (test2Marks != null ? test2Marks : 0);
                int avgTests = (int) Math.round((t1 + t2) * 0.3); // Scaling average tests to 30
                int assign = (assignmentMarks != null ? assignmentMarks : 0);
                int oaa = (oaaMarks != null ? oaaMarks : 0);
                finalCie = avgTests + assign + oaa;
                this.cieMarks = finalCie;
            } else {
                // Fall back to pre-set cieMarks
                finalCie = (cieMarks != null ? cieMarks : 0);
            }
            passesCie = finalCie >= 20;
        } else if (type == CourseType.LABORATORY) {
            boolean hasDetailMarks = (regularLabMarks != null || labTestMarks != null || labRecordMarks != null);
            if (hasDetailMarks) {
                int reg = (regularLabMarks != null ? regularLabMarks : 0);
                int test = (labTestMarks != null ? labTestMarks : 0);
                int rec = (labRecordMarks != null ? labRecordMarks : 0);
                finalCie = reg + test + rec;
                this.cieMarks = finalCie;
            } else {
                // Fall back to pre-set cieMarks
                finalCie = (cieMarks != null ? cieMarks : 0);
            }
            passesCie = finalCie >= 25;
        } else if (type == CourseType.INTEGRATED) {
            boolean hasTheoryDetail = (test1Marks != null || test2Marks != null
                    || assignmentMarks != null || oaaMarks != null);
            boolean hasLabDetail = (regularLabMarks != null || labTestMarks != null || labRecordMarks != null);
            if (hasTheoryDetail || hasLabDetail) {
                int t1 = (test1Marks != null ? test1Marks : 0);
                int t2 = (test2Marks != null ? test2Marks : 0);
                int avgTests = (int) Math.round((t1 + t2) * 0.3);
                int assign = (assignmentMarks != null ? assignmentMarks : 0);
                int oaa = (oaaMarks != null ? oaaMarks : 0);
                double theoryTotal = avgTests + assign + oaa;
                int theoryReduced = (int) Math.round(theoryTotal * 0.6); // Scale to 30

                int reg = (regularLabMarks != null ? regularLabMarks : 0);
                int test = (labTestMarks != null ? labTestMarks : 0);
                int rec = (labRecordMarks != null ? labRecordMarks : 0);
                int labTotal = reg + test + rec;
                int labReduced = (int) Math.round(labTotal * 0.4); // Scale to 20

                this.cieTheoryMarks = theoryReduced;
                this.cieLabMarks = labReduced;
                finalCie = theoryReduced + labReduced;
                this.cieMarks = finalCie;
                passesCie = theoryReduced >= 12 && labReduced >= 8;
            } else {
                // Fall back to pre-set cieMarks
                finalCie = (cieMarks != null ? cieMarks : 0);
                int theoryReduced = (cieTheoryMarks != null ? cieTheoryMarks : 0);
                int labReduced = (cieLabMarks != null ? cieLabMarks : 0);
                passesCie = theoryReduced >= 12 && labReduced >= 8;
            }
        }

        // Use pre-set totalMarks if explicitly provided, otherwise compute from CIE + SEE
        if (totalMarks != null && totalMarks > 0 && test1Marks == null && test2Marks == null
                && assignmentMarks == null && oaaMarks == null
                && regularLabMarks == null && labTestMarks == null && labRecordMarks == null) {
            // Tests pre-set totalMarks — respect that
        } else {
            this.totalMarks = finalCie + finalSee;
        }
        int effectiveTotalMarks = (this.totalMarks != null ? this.totalMarks : finalCie + finalSee);

        boolean passesSee = finalSee >= 18;
        boolean overallPass = passesCie && passesSee && effectiveTotalMarks >= 40;

        if (!overallPass) {
            return LetterGrade.F;
        } else {
            // Ensure total marks are within 0-100 range for LetterGrade mapping
            int cappedMarks = Math.min(effectiveTotalMarks, 100);
            return LetterGrade.fromMarks(cappedMarks);
        }
    }
}
