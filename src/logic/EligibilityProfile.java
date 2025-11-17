package logic;

import model.CourseType;
import model.Region;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EligibilityProfile {

    private final double cgpa10;          // 0.0 - 10.0
    private final double ieltsOverall;    // 0.0 - 9.0
    private final boolean hasGre;
    private final int greQuant;           // 130 - 170 if present
    private final int greVerbal;          // 130 - 170 if present
    private final int yearsExperience;    // 0 - 15
    private final int numPapers;          // 0 - 10
    private final double budgetUsd;       // 0 means "no limit"

    private final CourseType courseType;
    private final Set<Region> regions;

    public EligibilityProfile(
            double cgpa10,
            double ieltsOverall,
            boolean hasGre,
            int greQuant,
            int greVerbal,
            int yearsExperience,
            int numPapers,
            double budgetUsd,
            CourseType courseType,
            Set<Region> regions
    ) {
        this.cgpa10 = cgpa10;
        this.ieltsOverall = ieltsOverall;
        this.hasGre = hasGre;
        this.greQuant = greQuant;
        this.greVerbal = greVerbal;
        this.yearsExperience = yearsExperience;
        this.numPapers = numPapers;
        this.budgetUsd = budgetUsd;
        this.courseType = courseType;
        this.regions = new HashSet<>(regions);
    }

    public double getCgpa10() {
        return cgpa10;
    }

    public double getIeltsOverall() {
        return ieltsOverall;
    }

    public boolean hasGre() {
        return hasGre;
    }

    public int getGreQuant() {
        return greQuant;
    }

    public int getGreVerbal() {
        return greVerbal;
    }

    public int getYearsExperience() {
        return yearsExperience;
    }

    public int getNumPapers() {
        return numPapers;
    }

    public double getBudgetUsd() {
        return budgetUsd;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public Set<Region> getRegions() {
        return Collections.unmodifiableSet(regions);
    }

    /**
     * 100-point profile score:
     *  - GPA       : 40 pts
     *  - IELTS     : 25 pts
     *  - GRE       : 20 pts
     *  - Research  : 10 pts
     *  - Experience: 5 pts
     */
    public double computeProfileScore() {
        double score = 0.0;

        // GPA (0-10) → 0-40
        double gpa = clamp(cgpa10, 0.0, 10.0);
        score += (gpa / 10.0) * 40.0;

        // IELTS (0-9) → 0-25
        double ielts = clamp(ieltsOverall, 0.0, 9.0);
        score += (ielts / 9.0) * 25.0;

        // GRE (if present): total 260-340 → 0-20
        if (hasGre) {
            int q = clampInt(greQuant, 130, 170);
            int v = clampInt(greVerbal, 130, 170);
            int total = q + v; // 260 - 340
            double norm = (total - 260.0) / 80.0; // 0 - 1
            norm = clamp(norm, 0.0, 1.0);
            score += norm * 20.0;
        }

        // Research papers: 0-3 → 0-10 (saturate at 3)
        int papers = Math.max(0, Math.min(numPapers, 3));
        score += (papers / 3.0) * 10.0;

        // Experience: 0-5 years → 0-5 (saturate at 5)
        int yrs = Math.max(0, Math.min(yearsExperience, 5));
        score += (yrs / 5.0) * 5.0;

        return score;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static int clampInt(int v, int lo, int hi) {
        return (int) Math.max(lo, Math.min(hi, v));
    }
}
