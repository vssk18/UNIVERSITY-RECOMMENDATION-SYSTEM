package cli;

import data.UniversityRepository;
import logic.RecommendationEngine;
import logic.RecommendationEngine.EligibilityProfile;
import logic.RecommendationEngine.Result;
import logic.RecommendationEngine.ScoredUniversity;
import logic.RecommendationEngine.Mode;
import model.CourseType;
import model.Region;
import model.University;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class UniversityRecommendationSystem {

    private static final String DATA_PATH = "data/universities_complete.csv";

    public static void main(String[] args) {
        try {
            Path path = Paths.get(DATA_PATH);
            List<University> all = UniversityRepository.load(path);
            if (all.isEmpty()) {
                System.out.println("No universities loaded. Check " + DATA_PATH);
                return;
            }

            Scanner sc = new Scanner(System.in);
            System.out.println("===============================================");
            System.out.println("      UNIVERSITY RECOMMENDATION SYSTEM");
            System.out.println("===============================================");
            System.out.println("Dataset loaded: " + all.size() + " universities\n");

            CourseType courseType = askCourseType(sc);
            Set<Region> regions = askRegions(sc);
            Mode mode = askMode(sc);

            EligibilityProfile profile = askFullProfile(sc, courseType, regions, mode);

            RecommendationEngine engine = new RecommendationEngine();
            Result result = engine.recommend(all, profile);

            System.out.println();
            System.out.println("Generated at: " + result.generatedAt);
            System.out.println("Profile classification: " + result.profileLabel);
            System.out.println();

            if (mode == Mode.PREDICT) {
                printSuggestedSplit(profile, result);
            }

            printBucket("Ambitious (reach schools)", result.ambitious);
            printBucket("Target (balanced fit)", result.target);
            printBucket("Safe (safer, but not guaranteed)", result.safe);

            Path logPath = saveRunLog(profile, result);
            System.out.println();
            System.out.println("Saved detailed run log to: " + logPath.toString());
            System.out.println("You can attach this file in future discussions or reviews.");

            System.out.println();
            System.out.println("=== Notes & Warnings ===");
            for (String msg : result.globalWarnings) {
                System.out.println("- " + msg);
            }

        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    // ---------------- QUESTIONS ----------------

    private static CourseType askCourseType(Scanner sc) {
        System.out.println("Select your primary course type (ONE only):");
        System.out.println("  1) STEM");
        System.out.println("  2) MBA");
        System.out.println("  3) MIS");
        System.out.println("  4) Health");
        System.out.println("  5) Arts");
        System.out.println("  6) Finance");
        int choice = readInt(sc, "Enter choice [1-6]: ", 1, 6);
        switch (choice) {
            case 1: return CourseType.STEM;
            case 2: return CourseType.MBA;
            case 3: return CourseType.MIS;
            case 4: return CourseType.HEALTH;
            case 5: return CourseType.ARTS;
            case 6: return CourseType.FINANCE;
            default: return CourseType.STEM;
        }
    }

    private static Set<Region> askRegions(Scanner sc) {
        System.out.println();
        System.out.println("Select up to 3 regions (comma-separated):");
        System.out.println("  1) USA");
        System.out.println("  2) Europe");
        System.out.println("  3) Asia");
        System.out.println("  4) Middle East");
        System.out.println("  5) Australia");
        System.out.print("Enter choices (e.g., 1,3,5): ");

        String line = sc.nextLine().trim();
        if (line.isEmpty()) {
            return EnumSet.of(Region.USA);
        }

        String[] parts = line.split(",");
        Set<Region> regions = EnumSet.noneOf(Region.class);
        for (String p : parts) {
            try {
                int v = Integer.parseInt(p.trim());
                switch (v) {
                    case 1: regions.add(Region.USA); break;
                    case 2: regions.add(Region.EUROPE); break;
                    case 3: regions.add(Region.ASIA); break;
                    case 4: regions.add(Region.MIDDLE_EAST); break;
                    case 5: regions.add(Region.AUSTRALIA); break;
                    default: break;
                }
            } catch (NumberFormatException ignored) {}
        }
        if (regions.isEmpty()) {
            regions.add(Region.USA);
        }
        if (regions.size() > 3) {
            regions = regions.stream()
                    .limit(3)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(Region.class)));
        }
        return regions;
    }

    private static Mode askMode(Scanner sc) {
        System.out.println();
        System.out.println("Mode:");
        System.out.println("  1) Let the system PREDICT Ambitious / Target / Safe");
        System.out.println("  2) Just LIST all universities that match my filters");
        int choice = readInt(sc, "Enter choice [1-2]: ", 1, 2);
        return (choice == 1) ? Mode.PREDICT : Mode.VIEW_ALL;
    }

    private static EligibilityProfile askFullProfile(Scanner sc,
                                                     CourseType courseType,
                                                     Set<Region> regions,
                                                     Mode mode) {
        System.out.println();
        System.out.print("Your CGPA on 10-point scale: ");
        double cgpa = readDouble(sc, 0.0, 10.0);

        System.out.print("Your IELTS overall: ");
        double ielts = readDouble(sc, 0.0, 9.0);

        System.out.print("Do you have a GRE score? (y/n): ");
        boolean hasGre = sc.nextLine().trim().toLowerCase().startsWith("y");

        int greQ = 0;
        int greV = 0;
        if (hasGre) {
            System.out.print("GRE Quant (130-170): ");
            greQ = readInt(sc, 130, 170);
            System.out.print("GRE Verbal (130-170): ");
            greV = readInt(sc, 130, 170);
        }

        System.out.print("Years of relevant experience (0-15): ");
        int exp = readInt(sc, 0, 15);

        System.out.print("Number of research papers (0-10): ");
        int papers = readInt(sc, 0, 10);

        System.out.print("Budget ceiling in USD (0 = no limit): ");
        double budget = readDouble(sc, 0.0, 500000.0);

        int desiredTotal = 0;
        if (mode == Mode.PREDICT) {
            System.out.print("How many universities are you planning to apply to? ");
            desiredTotal = readInt(sc, 1, 50);
        }

        return new EligibilityProfile(
                courseType,
                regions,
                mode,
                cgpa,
                ielts,
                hasGre,
                greQ,
                greV,
                exp,
                papers,
                budget,
                desiredTotal
        );
    }

    // ---------------- PRINTING ----------------

    private static void printSuggestedSplit(EligibilityProfile p, Result result) {
        int amb = result.ambitious.size();
        int tar = result.target.size();
        int saf = result.safe.size();
        int total = amb + tar + saf;

        System.out.println("=== Suggested split ===");
        System.out.printf("Ambitious: %d%n", amb);
        System.out.printf("Target   : %d%n", tar);
        System.out.printf("Safe     : %d%n", saf);
        if (total < p.desiredTotal) {
            System.out.println();
            System.out.println("Note: fewer universities matched your filters than requested.");
            System.out.println("Try relaxing budget, regions, or IELTS cutoff if you need more options.");
        }
        System.out.println();
    }

    private static void printBucket(String title, List<ScoredUniversity> list) {
        System.out.println();
        System.out.println("=== " + title + " ===");
        if (list.isEmpty()) {
            System.out.println("(none)");
            return;
        }

        System.out.printf("%-3s %-30s %-10s %-10s %-10s %-5s %-7s %-9s %-30s%n",
                "#", "University", "Country", "Region", "CourseType",
                "IELTS", "GRE", "GlobRank", "Reasons");
        System.out.println("---- ------------------------------ ---------- ---------- ---------- ----- ------- --------- ------------------------------");

        int idx = 1;
        for (ScoredUniversity s : list) {
            University u = s.uni;
            String gre = u.isGreRequired() ? "Req" : "Opt";
            String reasons = String.join("|", s.reasons);
            System.out.printf("%-3d %-30s %-10s %-10s %-10s %-5.1f %-7s %-9d %-30s%n",
                    idx,
                    cut(u.getName(), 30),
                    cut(u.getCountry(), 10),
                    u.getRegion(),
                    u.getCourseType(),
                    u.getIeltsMin(),
                    gre,
                    u.getGlobalRank(),
                    cut(reasons, 30)
            );
            idx++;
        }
    }

    private static Path saveRunLog(EligibilityProfile p, Result result) {
        try {
            Files.createDirectories(Paths.get("runs"));
            String ts = LocalDateTime.now().toString().replace(":", "-");
            Path logPath = Paths.get("runs", "run_" + ts + ".txt");

            try (BufferedWriter bw = Files.newBufferedWriter(logPath);
                 PrintWriter out = new PrintWriter(bw)) {

                out.println("UNIVERSITY RECOMMENDATION SYSTEM RUN");
                out.println("Generated at: " + result.generatedAt);
                out.println("Profile label: " + result.profileLabel);
                out.println();
                out.println("CourseType: " + p.courseType);
                out.println("Regions  : " + p.regions);
                out.println("CGPA     : " + p.cgpa10);
                out.println("IELTS    : " + p.ielts);
                out.println("GRE      : " + (p.hasGre ? (p.greQuant + "/" + p.greVerbal) : "None"));
                out.println("Experience years: " + p.yearsExperience);
                out.println("Papers         : " + p.researchPapers);
                out.println("Budget         : " + p.budgetUsd);
                out.println("Mode           : " + p.mode);
                out.println();

                writeBucketToLog(out, "Ambitious", result.ambitious);
                writeBucketToLog(out, "Target", result.target);
                writeBucketToLog(out, "Safe", result.safe);

                out.println();
                out.println("Warnings:");
                for (String msg : result.globalWarnings) {
                    out.println("- " + msg);
                }
            }
            return logPath;
        } catch (IOException e) {
            System.out.println("Could not save run log: " + e.getMessage());
            return Paths.get("runs", "last_run_failed.txt");
        }
    }

    private static void writeBucketToLog(PrintWriter out, String title, List<ScoredUniversity> list) {
        out.println("=== " + title + " ===");
        int idx = 1;
        for (ScoredUniversity s : list) {
            University u = s.uni;
            String reasons = String.join("|", s.reasons);
            out.printf(Locale.US,
                    "%d,%s,%s,%s,%.1f,%s,%d,%s%n",
                    idx,
                    u.getName(),
                    u.getCountry(),
                    u.getCourseType(),
                    u.getIeltsMin(),
                    u.isGreRequired() ? "Req" : "Opt",
                    u.getGlobalRank(),
                    reasons
            );
            idx++;
        }
        out.println();
    }

    // ---------------- HELPERS ----------------

    private static int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Enter a number between " + min + " and " + max + ".");
            }
        }
    }

    private static int readInt(Scanner sc, int min, int max) {
        while (true) {
            String line = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.print("Enter a number between " + min + " and " + max + ": ");
            }
        }
    }

    private static double readDouble(Scanner sc, double min, double max) {
        while (true) {
            String line = sc.nextLine().trim();
            try {
                double v = Double.parseDouble(line);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.print("Enter a number between " + min + " and " + max + ": ");
            }
        }
    }

    private static String cut(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        if (maxLen <= 1) return s.substring(0, 1);
        return s.substring(0, maxLen - 1) + "…";
    }
}
