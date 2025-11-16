package model;

import java.util.Map;

public class University {
    private final String name;
    private final String country;
    private final String city;
    private final String program;
    private final String degree;
    private final CourseType courseType;
    private final Region region;

    private final double ieltsMin;
    private final boolean greRequired;
    private final boolean onCampus;

    private final double tuitionUsd;
    private final double totalUsd;

    private final int globalRank;
    private final int subjectRank;

    private final boolean hasResearchLab;
    private final String specializationTag;

    public University(Map<String, String> row) {
        this.name = row.getOrDefault("name", "").trim();
        this.country = row.getOrDefault("country", "").trim();
        this.city = row.getOrDefault("city", "").trim();
        this.program = row.getOrDefault("program", "").trim();
        this.degree = row.getOrDefault("degree", "").trim();

        String ct = row.getOrDefault("course_type", "");
        this.courseType = CourseType.fromString(ct);

        String rawRegion = row.getOrDefault("region", "");
        this.region = Region.fromString(rawRegion, this.country);

        this.ieltsMin = parseDouble(row.get("ielts_min"), 0.0);
        this.greRequired = parseBool(row.get("gre_required"));
        this.onCampus = parseBool(row.get("on_campus"));

        this.tuitionUsd = parseDouble(row.get("tuition_usd"), 0.0);
        this.totalUsd = parseDouble(row.get("total_usd"), 0.0);

        this.globalRank = (int) parseDouble(row.get("global_rank"), 0.0);
        this.subjectRank = (int) parseDouble(row.get("subject_rank"), 0.0);

        this.hasResearchLab = parseBool(row.get("has_research_lab"));
        this.specializationTag = row.getOrDefault("specialization", "").trim();
    }

    private static boolean parseBool(String v) {
        if (v == null) return false;
        String s = v.trim().toLowerCase();
        return s.equals("true") || s.equals("yes") || s.equals("1") || s.equals("y");
    }

    private static double parseDouble(String v, double defVal) {
        if (v == null || v.isBlank()) return defVal;
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return defVal;
        }
    }

    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getCity() { return city; }
    public String getProgram() { return program; }
    public String getDegree() { return degree; }
    public CourseType getCourseType() { return courseType; }
    public Region getRegion() { return region; }
    public double getIeltsMin() { return ieltsMin; }
    public boolean isGreRequired() { return greRequired; }
    public boolean isOnCampus() { return onCampus; }
    public double getTuitionUsd() { return tuitionUsd; }
    public double getTotalUsd() { return totalUsd; }
    public int getGlobalRank() { return globalRank; }
    public int getSubjectRank() { return subjectRank; }
    public boolean hasResearchLab() { return hasResearchLab; }
    public String getSpecializationTag() { return specializationTag; }
}
