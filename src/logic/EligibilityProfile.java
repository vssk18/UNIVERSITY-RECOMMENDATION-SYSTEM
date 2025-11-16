package logic;

import model.CourseType;
import model.Region;

import java.util.Set;

public class EligibilityProfile {
    public final double cgpa10;
    public final double gpa4;
    public final double ielts;
    public final boolean hasGre;
    public final int greQuant;
    public final int greVerbal;
    public final int yearsExperience;
    public final int researchPapers;
    public final double budgetUsd;
    public final CourseType courseType;
    public final Set<Region> regions;

    public EligibilityProfile(double cgpa10,
                              double ielts,
                              boolean hasGre,
                              int greQuant,
                              int greVerbal,
                              int yearsExperience,
                              int researchPapers,
                              double budgetUsd,
                              CourseType courseType,
                              Set<Region> regions) {
        this.cgpa10 = cgpa10;
        this.gpa4 = cgpa10 / 10.0 * 4.0;
        this.ielts = ielts;
        this.hasGre = hasGre;
        this.greQuant = greQuant;
        this.greVerbal = greVerbal;
        this.yearsExperience = yearsExperience;
        this.researchPapers = researchPapers;
        this.budgetUsd = budgetUsd;
        this.courseType = courseType;
        this.regions = regions;
    }

    public double strengthScore() {
        double s = 0.0;
        s += (gpa4 - 2.5) * 1.8;   // GPA contribution
        s += (ielts - 6.5) * 1.2;  // IELTS contribution
        if (hasGre) {
            double greNorm = (greQuant - 150) * 0.08 + (greVerbal - 145) * 0.04;
            s += greNorm;
        }
        s += Math.min(3, yearsExperience) * 0.7;
        s += Math.min(3, researchPapers) * 0.9;
        return s;
    }
}
