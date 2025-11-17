package model;

import java.util.Locale;
import java.util.Map;

public class University {

    private final Map<String, String> row;

    public University(Map<String, String> row) {
        this.row = row;
    }

    // ---------- Helpers ----------

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String getLike(String... patterns) {
        if (row == null) return "";
        // try direct keys first
        for (String p : patterns) {
            if (row.containsKey(p)) {
                return row.get(p);
            }
        }
        // fuzzy match
        for (String key : row.keySet()) {
            String nk = norm(key);
            for (String p : patterns) {
                String np = norm(p);
                if (!np.isEmpty() && nk.contains(np)) {
                    return row.get(key);
                }
            }
        }
        return "";
    }

    private int parseInt(String s, int fallback) {
        try {
            if (s == null || s.isBlank()) return fallback;
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private double parseDouble(String s, double fallback) {
        try {
            if (s == null || s.isBlank()) return fallback;
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private boolean parseBool(String s, boolean fallback) {
        if (s == null) return fallback;
        String v = s.trim().toLowerCase(Locale.ROOT);
        if (v.isEmpty()) return fallback;
        if (v.equals("y") || v.equals("yes") || v.equals("true") || v.equals("1") || v.equals("req") || v.equals("required")) {
            return true;
        }
        if (v.equals("n") || v.equals("no") || v.equals("false") || v.equals("0") || v.equals("opt") || v.equals("optional")) {
            return false;
        }
        return fallback;
    }

    // ---------- Public getters used by the engine & CLI ----------

    public String getName() {
        String v = getLike("Name", "University", "Institution");
        return v.isBlank() ? "Unknown University" : v;
    }

    public String getCountry() {
        String v = getLike("Country");
        return v.isBlank() ? "Unknown" : v;
    }

    public Region getRegion() {
        // Prefer explicit region column if present
        String regionCell = getLike("Region");
        if (!regionCell.isBlank()) {
            String r = regionCell.trim().toUpperCase(Locale.ROOT);
            if (r.contains("USA") || r.contains("UNITEDSTATES")) return Region.USA;
            if (r.contains("EUROPE")) return Region.EUROPE;
            if (r.contains("ASIA")) return Region.ASIA;
            if (r.contains("MIDDLEEAST") || r.contains("MIDDLE EAST")) return Region.MIDDLE_EAST;
            if (r.contains("AUSTRALIA")) return Region.AUSTRALIA;
        }

        // Fallback: infer from country
        String c = getCountry().toLowerCase(Locale.ROOT);
        if (c.contains("united states") || c.equals("usa")) return Region.USA;

        if (c.contains("uk") || c.contains("united kingdom") || c.contains("england") ||
            c.contains("germany") || c.contains("france") || c.contains("italy") ||
            c.contains("switzerland") || c.contains("spain") || c.contains("netherlands") ||
            c.contains("sweden") || c.contains("denmark") || c.contains("belgium") ||
            c.contains("finland") || c.contains("norway") || c.contains("ireland") ||
            c.contains("austria") || c.contains("poland") || c.contains("czech")) {
            return Region.EUROPE;
        }

        if (c.contains("uae") || c.contains("united arab emirates") ||
            c.contains("saudi") || c.contains("qatar") || c.contains("oman") ||
            c.contains("kuwait") || c.contains("bahrain")) {
            return Region.MIDDLE_EAST;
        }

        if (c.contains("australia") || c.contains("new zealand")) {
            return Region.AUSTRALIA;
        }

        // default: Asia (since many of your entries are in Asia otherwise)
        return Region.ASIA;
    }

    public CourseType getCourseType() {
        String v = getLike("CourseType", "Course Type", "Discipline", "Program Type");
        String n = norm(v);
        if (n.contains("mba") || n.contains("business") || n.contains("management")) {
            return CourseType.MBA;
        }
        if (n.contains("mis") || n.contains("infosys") || n.contains("information systems")) {
            return CourseType.MIS;
        }
        if (n.contains("health") || n.contains("medical") || n.contains("medicine") ||
            n.contains("bio") || n.contains("pharma")) {
            return CourseType.HEALTH;
        }
        if (n.contains("art") || n.contains("humanities") || n.contains("social")) {
            return CourseType.ARTS;
        }
        if (n.contains("finance") || n.contains("accounting") || n.contains("econ")) {
            return CourseType.FINANCE;
        }
        // Default: STEM (CS/engineering/etc.)
        return CourseType.STEM;
    }

    /** Minimum IELTS required for the program. */
    public double getMinIelts() {
        String v = getLike("IELTS", "IELTS_Min", "Min IELTS");
        return parseDouble(v, 6.5);
    }

    /** Whether GRE is required (vs optional). */
    public boolean isGreRequired() {
        String v = getLike("GRE_Required", "GRE", "GRE Requirement");
        // Many cells probably store "Req"/"Opt"/"Yes"/"No"
        return parseBool(v, false);
    }

    /** Approximate global or course rank (lower = better). */
    public int getGlobalRank() {
        String v = getLike("Global_Rank", "Global Rank", "QS_Rank", "Rank", "CourseRank");
        return parseInt(v, 9999);
    }

    /** Estimated total cost in USD (tuition + living, approximate). */
    public double getEstimatedTotalUsd() {
        String v = getLike("Total_USD", "TotalUSD", "Cost_USD", "Total Cost", "Estimated Total (USD)");
        return parseDouble(v, 0.0);
    }

    /** Whether this program clearly has a research lab / strong research profile. */
    public boolean hasResearchLab() {
        String v = getLike("Has_Research_Lab", "Research Lab", "Research", "Lab");
        if (v != null && !v.isBlank()) {
            return parseBool(v, true);
        }
        // Heuristic: top 200 assumed to have decent research infra
        return getGlobalRank() > 0 && getGlobalRank() <= 200;
    }

    /** Whether the program is primarily on-campus (vs fully online). */
    public boolean isOnCampus() {
        String v = getLike("Mode", "Delivery", "OnCampus", "On Campus");
        if (!v.isBlank()) {
            String n = norm(v);
            if (n.contains("online")) return false;
            if (n.contains("campus") || n.contains("oncampus")) return true;
        }
        return true; // default assumption: on-campus
    }

    // Raw access if needed for debugging
    public Map<String, String> getRawRow() {
        return row;
    }
}
