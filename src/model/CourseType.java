package model;

public enum CourseType {
    STEM,
    MBA,
    MIS,
    HEALTH,
    ARTS,
    FINANCE,
    OTHER;

    public static CourseType fromString(String raw) {
        if (raw == null) return OTHER;
        String s = raw.trim().toUpperCase();
        return switch (s) {
            case "STEM" -> STEM;
            case "MBA" -> MBA;
            case "MIS" -> MIS;
            case "HEALTH" -> HEALTH;
            case "ARTS" -> ARTS;
            case "FINANCE" -> FINANCE;
            default -> OTHER;
        };
    }
}
