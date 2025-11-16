package logic;

import model.CourseType;
import model.Region;
import model.University;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationEngine {

    public enum Mode {
        PREDICT,
        VIEW_ALL
    }

    // ---------------- PROFILE ----------------

    public static class EligibilityProfile {
        public final CourseType courseType;
        public final Set<Region> regions;
        public final Mode mode;

        public final double cgpa10;
        public final double ielts;
        public final boolean hasGre;
        public final int greQuant;
        public final int greVerbal;
        public final int yearsExperience;
        public final int researchPapers;
        public final double budgetUsd;      // currently only logged, not enforced
        public final int desiredTotal;      // only used in PREDICT mode

        public EligibilityProfile(
                CourseType courseType,
                Set<Region> regions,
                Mode mode,
                double cgpa10,
                double ielts,
                boolean hasGre,
                int greQuant,
                int greVerbal,
                int yearsExperience,
                int researchPapers,
                double budgetUsd,
                int desiredTotal
        ) {
            this.courseType = courseType;
            this.regions = regions;
            this.mode = mode;
            this.cgpa10 = cgpa10;
            this.ielts = ielts;
            this.hasGre = hasGre;
            this.greQuant = greQuant;
            this.greVerbal = greVerbal;
            this.yearsExperience = yearsExperience;
            this.researchPapers = researchPapers;
            this.budgetUsd = budgetUsd;
            this.desiredTotal = desiredTotal;
        }
    }

    // ---------------- RESULT ----------------

    public static class ScoredUniversity {
        public final University uni;
        public final double score;
        public final List<String> reasons;

        public ScoredUniversity(University uni, double score, List<String> reasons) {
            this.uni = uni;
            this.score = score;
            this.reasons = reasons;
        }
    }

    public static class Result {
        public final List<ScoredUniversity> ambitious;
        public final List<ScoredUniversity> target;
        public final List<ScoredUniversity> safe;
        public final List<String> globalWarnings;
        public final String profileLabel;
        public final LocalDateTime generatedAt;

        public Result(List<ScoredUniversity> ambitious,
                      List<ScoredUniversity> target,
                      List<ScoredUniversity> safe,
                      List<String> globalWarnings,
                      String profileLabel,
                      LocalDateTime generatedAt) {
            this.ambitious = ambitious;
            this.target = target;
            this.safe = safe;
            this.globalWarnings = globalWarnings;
            this.profileLabel = profileLabel;
            this.generatedAt = generatedAt;
        }
    }

    // ---------------- MAIN ENTRY ----------------

    public Result recommend(List<University> all, EligibilityProfile p) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Filter by course type, region, IELTS, on-campus
        List<University> filtered = all.stream()
                .filter(u -> u.getCourseType() == p.courseType)
                .filter(u -> p.regions.contains(u.getRegion()))
                .filter(u -> p.ielts >= u.getIeltsMin())
                .filter(University::isOnCampus)
                .collect(Collectors.toList());

        // 2. Score each university relative to profile
        double profileStrength = computeProfileStrength(p);
        String profileLabel = labelProfileStrength(profileStrength);

        List<ScoredUniversity> scored = new ArrayList<>();
        for (University u : filtered) {
            List<String> reasons = new ArrayList<>();

            int globalRank = u.getGlobalRank(); // assume 0 if unknown
            if (globalRank > 0) {
                reasons.add("global rank " + globalRank);
            }

            if (u.isGreRequired()) {
                reasons.add("GRE required");
            } else {
                reasons.add("GRE optional");
            }

            if (u.hasResearchLab()) {
                reasons.add("active research lab");
            }

            double uniDifficulty = estimateDifficultyFromRank(globalRank);
            double gap = profileStrength - uniDifficulty;
            double finalScore = gap; // higher = safer

            scored.add(new ScoredUniversity(u, finalScore, reasons));
        }

        // 3. Sort by difficulty (hardest first) using score, then global rank
        scored.sort(Comparator
                .comparingDouble((ScoredUniversity s) -> s.score)
                .thenComparingInt(s -> s.uni.getGlobalRank()));

        // 4. Bucket into Ambitious / Target / Safe with HARD constraints
        List<ScoredUniversity> amb = new ArrayList<>();
        List<ScoredUniversity> tar = new ArrayList<>();
        List<ScoredUniversity> saf = new ArrayList<>();

        for (ScoredUniversity s : scored) {
            University u = s.uni;
            int r = u.getGlobalRank();

            // Hard rule 1: Top-10 globally are ALWAYS Ambitious.
            if (r > 0 && r <= 10) {
                amb.add(s);
                continue;
            }

            // Hard rule 2: Top-30 globally are NEVER Safe.
            if (r > 0 && r <= 30) {
                if (s.score >= 0.5) {
                    tar.add(s);
                } else {
                    amb.add(s);
                }
                continue;
            }

            // For all others, use score thresholds:
            // score < 0      → Ambitious
            // 0 ≤ score < 2  → Target
            // score ≥ 2      → Safe (safer, not guaranteed)
            if (s.score < 0.0) {
                amb.add(s);
            } else if (s.score < 2.0) {
                tar.add(s);
            } else {
                saf.add(s);
            }
        }

        // 5. If LIST_ONLY, keep them all but still bucket by risk.
        // If PREDICT, we still show all, but the user’s desired count is in the profile.

        // 6. Build warnings
        List<String> warnings = new ArrayList<>();

        if (amb.stream().anyMatch(s -> s.uni.getGlobalRank() > 0 && s.uni.getGlobalRank() <= 10)) {
            warnings.add("Top-10 global programs are treated as STRICT Ambitious. Even strong profiles should treat them as high-risk.");
        }

        boolean anyTop30Safe = saf.stream()
                .anyMatch(s -> s.uni.getGlobalRank() > 0 && s.uni.getGlobalRank() <= 30);
        if (anyTop30Safe) {
            warnings.add("A program with global rank ≤ 30 slipped into Safe. Treat it as Target or Ambitious in real life.");
        }

        boolean anyTop50Safe = saf.stream()
                .anyMatch(s -> s.uni.getGlobalRank() > 0 && s.uni.getGlobalRank() <= 50);
        if (anyTop50Safe) {
            warnings.add("Some 'Safe' universities are still top-50 globally. Admissions remain highly competitive.");
        }

        if (p.budgetUsd > 0) {
            warnings.add("Budget-based filtering is currently not enforced in code. Confirm tuition and cost of living manually.");
        }

        warnings.add("No bucket guarantees admission. Use these categories as guidance only, and double-check each program’s selectivity.");

        return new Result(amb, tar, saf, warnings, profileLabel, now);
    }

    // ---------------- INTERNAL HELPERS ----------------

    private double computeProfileStrength(EligibilityProfile p) {
        double s = 0.0;

        // CGPA weight
        if (p.cgpa10 >= 9.0)      s += 3.0;
        else if (p.cgpa10 >= 8.5) s += 2.5;
        else if (p.cgpa10 >= 8.0) s += 2.0;
        else if (p.cgpa10 >= 7.5) s += 1.5;
        else if (p.cgpa10 >= 7.0) s += 1.0;
        else                      s += 0.5;

        // IELTS
        if (p.ielts >= 8.0)      s += 1.0;
        else if (p.ielts >= 7.5) s += 0.8;
        else if (p.ielts >= 7.0) s += 0.6;
        else if (p.ielts >= 6.5) s += 0.4;

        // GRE
        if (p.hasGre) {
            double qBoost = (p.greQuant - 155) / 2.0;   // 160 → +2.5
            double vBoost = (p.greVerbal - 150) / 4.0;  // 160 → +2.5
            s += Math.max(0, qBoost) + Math.max(0, vBoost);
        }

        // Experience
        s += Math.min(3.0, p.yearsExperience * 0.3); // up to +3

        // Research
        s += Math.min(3.0, p.researchPapers * 0.5); // up to +3

        return s;
    }

    private String labelProfileStrength(double s) {
        if (s >= 8.0) return "very strong";
        if (s >= 6.0) return "strong";
        if (s >= 4.0) return "solid";
        if (s >= 2.5) return "developing";
        return "early stage";
    }

    private double estimateDifficultyFromRank(int globalRank) {
        if (globalRank <= 0) return 4.0; // unknown → moderate difficulty
        if (globalRank <= 10) return 9.0;
        if (globalRank <= 30) return 7.5;
        if (globalRank <= 60) return 6.0;
        if (globalRank <= 100) return 5.0;
        if (globalRank <= 200) return 4.5;
        return 3.5;
    }
}
