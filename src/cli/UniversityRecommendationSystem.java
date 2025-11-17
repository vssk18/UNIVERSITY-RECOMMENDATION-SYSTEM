package cli;

import data.UniversityRepository;
import logic.EligibilityProfile;
import logic.RecommendationEngine;
import logic.RecommendationEngine.Mode;
import logic.RecommendationEngine.Result;
import logic.RecommendationEngine.ScoredUniversity;
import logic.RecommendationEngine.ProfileTier;
import model.CourseType;
import model.Region;
import model.University;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UniversityRecommendationSystem {

    private static final Path DATA_PATH = Paths.get("data", "universities_complete.csv");

    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("      UNIVERSITY RECOMMENDATION SYSTEM");
        System.out.println("===============================================");

        List<University> all;
        try {
            UniversityRepository repo = new UniversityRepository(DATA_PATH);
            all = repo.getAll();
        } catch (IOException e) {
            System.err.println("Failed to load dataset from " + DATA_PATH + ": " + e.getMessage());
            return;
        }

        System.out.println("Dataset loaded: " + all.size() + " universities");
        System.out.println();

        Scanner sc = new Scanner(System.in);

        CourseType courseType = askCourseType(sc);
        Set<Region> regions = askRegions(sc);

        Mode mode = askMode(sc);

        double cgpa = askDouble(sc, "Your CGPA on 10-point scale: ", 0.0, 10.0);
        double ielts = askDouble(sc, "Your IELTS overall: ", 0.0, 9.0);

        boolean hasGre = askYesNo(sc, "Do you have a GRE score? (y/n): ");
        int greQ = 0;
        int greV = 0;
        if (hasGre) {
            greQ = (int) askDouble(sc, "GRE Quant (130-170): ", 130, 170);
            greV = (int) askDouble(sc, "GRE Verbal (130-170): ", 130, 170);
        }

        int yearsExp = (int) askDouble(sc, "Years of relevant experience (0-15): ", 0, 15);
        int papers = (int) askDouble(sc, "Number of research papers (0-10): ", 0, 10);
        double budget = askDouble(sc, "Budget ceiling in USD (0 = no limit): ", 0, 500000);

        int desiredCount = 0;
        if (mode == Mode.PREDICT) {
            desiredCount = (int) askDouble(sc, "How many universities are you planning to apply to? ", 1, 100);
        }

        EligibilityProfile profile = new EligibilityProfile(
                cgpa,
                ielts,
                hasGre,
                greQ,
                greV,
                yearsExp,
                papers,
                budget,
                courseType,
                regions
        );

        Result result = RecommendationEngine.recommend(all, profile, mode, desiredCount);

        printSummary(result);
        printBuckets(result);
        printWarnings(result);
    }

    // ---------- Asking inputs ----------

    private static CourseType askCourseType(Scanner sc) {
        while (true) {
            System.out.println("Select your primary course type (ONE only):");
            System.out.println("  1) STEM");
            System.out.println("  2) MBA");
            System.out.println("  3) MIS");
            System.out.println("  4) Health");
            System.out.println("  5) Arts");
            System.out.println("  6) Finance");
            System.out.print("Enter choice [1-6]: ");

            String line = sc.nextLine().trim();
            try {
                int c = Integer.parseInt(line);
                CourseType ct = null;
                switch (c) {
                    case 1 -> ct = CourseType.STEM;
                    case 2 -> ct = CourseType.MBA;
                    case 3 -> ct = CourseType.MIS;
                    case 4 -> ct = CourseType.HEALTH;
                    case 5 -> ct = CourseType.ARTS;
                    case 6 -> ct = CourseType.FINANCE;
                    default -> {
                        System.out.println("Please enter a number between 1 and 6.");
                    }
                }
                if (ct != null) {
                    return ct;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number between 1 and 6.");
            }
        }
    }

    private static Set<Region> askRegions(Scanner sc) {
        while (true) {
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
                System.out.println("Please select at least one region.");
                continue;
            }
            String[] parts = line.split(",");
            if (parts.length > 3) {
                System.out.println("You can select at most 3 regions.");
                continue;
            }

            Set<Region> set = new LinkedHashSet<>();
            boolean ok = true;
            for (String p : parts) {
                p = p.trim();
                if (p.isEmpty()) continue;
                try {
                    int c = Integer.parseInt(p);
                    Region r = switch (c) {
                        case 1 -> Region.USA;
                        case 2 -> Region.EUROPE;
                        case 3 -> Region.ASIA;
                        case 4 -> Region.MIDDLE_EAST;
                        case 5 -> Region.AUSTRALIA;
                        default -> null;
                    };
                    if (r == null) {
                        System.out.println("Invalid region: " + p);
                        ok = false;
                        break;
                    }
                    set.add(r);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid region: " + p);
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;
            if (set.isEmpty()) {
                System.out.println("Please select at least one valid region.");
                continue;
            }
            return set;
        }
    }

    private static Mode askMode(Scanner sc) {
        while (true) {
            System.out.println();
            System.out.println("Mode:");
            System.out.println("  1) Let the system PREDICT Ambitious / Target / Safe");
            System.out.println("  2) Just LIST all universities that match my filters");
            System.out.print("Enter choice [1-2]: ");

            String line = sc.nextLine().trim();
            try {
                int c = Integer.parseInt(line);
                if (c == 1) return Mode.PREDICT;
                if (c == 2) return Mode.VIEW_ALL;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Please enter 1 or 2.");
        }
    }

    private static boolean askYesNo(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim().toLowerCase(Locale.ROOT);
            if (line.startsWith("y")) return true;
            if (line.startsWith("n")) return false;
            System.out.println("Please enter 'y' or 'n'.");
        }
    }

    private static double askDouble(Scanner sc, String prompt, double lo, double hi) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                double v = Double.parseDouble(line);
                if (v < lo || v > hi) {
                    System.out.println("Enter a number between " + lo + " and " + hi + ".");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Enter a number between " + lo + " and " + hi + ".");
            }
        }
    }

    // ---------- Printing ----------

    private static void printSummary(Result r) {
        System.out.println();
        System.out.println("Generated at: " +
                r.generatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.printf(Locale.US, "Profile score: %.1f / 100%n", r.profileScore);
        System.out.println("Profile classification: " + tierLabel(r.tier));
        System.out.println("Total matching (before filters): " + r.totalUniversities);
        if (r.excludedByIelts > 0) {
            System.out.println("IELTS filter: " + r.excludedByIelts +
                    " universities excluded because IELTS < program minimum.");
        }
        System.out.println();
    }

    private static String tierLabel(ProfileTier tier) {
        return switch (tier) {
            case EXCEPTIONAL -> "exceptional";
            case VERY_STRONG -> "very strong";
            case STRONG -> "strong";
            case GOOD -> "good";
            case DEVELOPING -> "developing";
            case BASIC -> "basic";
        };
    }

    private static void printBuckets(Result r) {
        if (r.ambitious.isEmpty() && r.target.isEmpty() && r.safe.isEmpty()) {
            System.out.println("No universities matched your filters after IELTS / budget constraints.");
            return;
        }

        if (r.mode == Mode.PREDICT) {
            int total = r.ambitious.size() + r.target.size() + r.safe.size();
            System.out.println("=== Suggested split ===");
            System.out.printf("Ambitious: %d%n", r.ambitious.size());
            System.out.printf("Target   : %d%n", r.target.size());
            System.out.printf("Safe     : %d%n", r.safe.size());
            System.out.printf("Total    : %d%n", total);
            System.out.println();
        }

        printBucket("Ambitious (reach schools)", r.ambitious);
        printBucket("Target (balanced fit)", r.target);
        printBucket("Safe (safer, but not guaranteed)", r.safe);
    }

    private static void printBucket(String title, List<ScoredUniversity> list) {
        System.out.println("=== " + title + " ===");
        if (list.isEmpty()) {
            System.out.println("(none)");
            System.out.println();
            return;
        }

        System.out.printf("%-3s %-30s %-10s %-10s %-10s %-5s %-6s %-9s %-10s%n",
                "#", "University", "Country", "Region", "CourseType", "IELTS", "GRE", "GlobRank", "Reasons");
        System.out.println("---- ------------------------------ ---------- ---------- ---------- ----- ------ --------- ----------");

        int idx = 1;
        for (ScoredUniversity s : list) {
            University u = s.uni;
            String greLabel = u.isGreRequired() ? "Req" : "Opt";
            System.out.printf("%-3d %-30s %-10s %-10s %-10s %-5.1f %-6s %-9d %-10s%n",
                    idx++,
                    cut(u.getName(), 30),
                    cut(u.getCountry(), 10),
                    cut(u.getRegion().name(), 10),
                    cut(u.getCourseType().name(), 10),
                    u.getMinIelts(),
                    greLabel,
                    u.getGlobalRank(),
                    String.join("|", s.reasons)
            );
        }
        System.out.println();
    }

    private static String cut(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        if (maxLen <= 1) return s.substring(0, maxLen);
        return s.substring(0, maxLen - 1) + "…";
    }

    private static void printWarnings(Result r) {
        System.out.println("=== Notes & Warnings ===");
        System.out.println("- Top-15 global programs are always treated as STRICT Ambitious.");
        System.out.println("- Some 'Safe' universities may still be top-50 or top-100 globally. "
                + "Admissions remain highly competitive.");
        System.out.println("- No bucket guarantees admission. Use these categories as guidance only, "
                + "and double-check each program’s selectivity and requirements.");
        if (r.excludedByIelts > 0) {
            System.out.println("- Consider improving IELTS to unlock more universities.");
        }
    }
}
