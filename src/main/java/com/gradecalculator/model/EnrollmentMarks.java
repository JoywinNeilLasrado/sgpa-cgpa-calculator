package com.gradecalculator.model;

import jakarta.persistence.Embeddable;

/**
 * Embeddable entity encapsulating all grades and marks details for an Enrollment.
 * Helps break down the large Enrollment class to comply with the SOLID Single Responsibility Principle.
 */
@Embeddable
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

    // Getters and Setters
    public Integer getCieMarks() { return cieMarks; }
    public void setCieMarks(Integer cieMarks) { this.cieMarks = cieMarks; }

    public Integer getCieTheoryMarks() { return cieTheoryMarks; }
    public void setCieTheoryMarks(Integer cieTheoryMarks) { this.cieTheoryMarks = cieTheoryMarks; }

    public Integer getCieLabMarks() { return cieLabMarks; }
    public void setCieLabMarks(Integer cieLabMarks) { this.cieLabMarks = cieLabMarks; }

    public Integer getTest1Marks() { return test1Marks; }
    public void setTest1Marks(Integer test1Marks) { this.test1Marks = test1Marks; }

    public Integer getTest2Marks() { return test2Marks; }
    public void setTest2Marks(Integer test2Marks) { this.test2Marks = test2Marks; }

    public Integer getAssignmentMarks() { return assignmentMarks; }
    public void setAssignmentMarks(Integer assignmentMarks) { this.assignmentMarks = assignmentMarks; }

    public Integer getOaaMarks() { return oaaMarks; }
    public void setOaaMarks(Integer oaaMarks) { this.oaaMarks = oaaMarks; }

    public Integer getRegularLabMarks() { return regularLabMarks; }
    public void setRegularLabMarks(Integer regularLabMarks) { this.regularLabMarks = regularLabMarks; }

    public Integer getLabTestMarks() { return labTestMarks; }
    public void setLabTestMarks(Integer labTestMarks) { this.labTestMarks = labTestMarks; }

    public Integer getLabRecordMarks() { return labRecordMarks; }
    public void setLabRecordMarks(Integer labRecordMarks) { this.labRecordMarks = labRecordMarks; }

    public Integer getSeeMarks() { return seeMarks; }
    public void setSeeMarks(Integer seeMarks) { this.seeMarks = seeMarks; }

    public Integer getGraceMarks() { return graceMarks; }
    public void setGraceMarks(Integer graceMarks) { this.graceMarks = graceMarks; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

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
            int t1 = (test1Marks != null ? test1Marks : 0);
            int t2 = (test2Marks != null ? test2Marks : 0);
            int avgTests = (int) Math.round((t1 + t2) * 0.3); // Scaling average tests to 30
            int assign = (assignmentMarks != null ? assignmentMarks : 0);
            int oaa = (oaaMarks != null ? oaaMarks : 0);
            finalCie = avgTests + assign + oaa;
            this.cieMarks = finalCie;
            passesCie = finalCie >= 20;
        } else if (type == CourseType.LABORATORY) {
            int reg = (regularLabMarks != null ? regularLabMarks : 0);
            int test = (labTestMarks != null ? labTestMarks : 0);
            int rec = (labRecordMarks != null ? labRecordMarks : 0);
            finalCie = reg + test + rec;
            this.cieMarks = finalCie;
            passesCie = finalCie >= 25;
        } else if (type == CourseType.INTEGRATED) {
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
        }

        this.totalMarks = finalCie + finalSee;

        boolean passesSee = finalSee >= 18;
        boolean overallPass = passesCie && passesSee && this.totalMarks >= 40;

        if (!overallPass) {
            return LetterGrade.F;
        } else {
            return LetterGrade.fromMarks(this.totalMarks);
        }
    }
}
