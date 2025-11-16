package model;

public enum Region {
    USA,
    EUROPE,
    ASIA,
    MIDDLE_EAST,
    AUSTRALIA,
    OTHER;

    public static Region fromString(String rawRegion, String country) {
        if (rawRegion != null && !rawRegion.isBlank()) {
            String r = rawRegion.trim().toUpperCase();
            return switch (r) {
                case "USA" -> USA;
                case "EUROPE" -> EUROPE;
                case "ASIA" -> ASIA;
                case "MIDDLE_EAST", "MIDDLE-EAST", "MIDDLE EAST" -> MIDDLE_EAST;
                case "AUSTRALIA" -> AUSTRALIA;
                default -> OTHER;
            };
        }
        if (country == null) return OTHER;
        String c = country.trim().toUpperCase();
        if (c.equals("UNITED STATES") || c.equals("USA")) return USA;
        if (c.equals("AUSTRALIA")) return AUSTRALIA;
        if (c.equals("UNITED ARAB EMIRATES") || c.equals("SAUDI ARABIA")
                || c.equals("QATAR") || c.equals("OMAN") || c.equals("KUWAIT")) {
            return MIDDLE_EAST;
        }
        if (c.equals("INDIA") || c.equals("CHINA") || c.equals("JAPAN")
                || c.equals("SINGAPORE") || c.equals("SOUTH KOREA")
                || c.equals("HONG KONG") || c.equals("TAIWAN")) {
            return ASIA;
        }
        if (c.equals("UNITED KINGDOM") || c.equals("GERMANY")
                || c.equals("FRANCE") || c.equals("SWITZERLAND")
                || c.equals("NETHERLANDS") || c.equals("SWEDEN")
                || c.equals("FINLAND") || c.equals("ITALY") || c.equals("SPAIN")) {
            return EUROPE;
        }
        return OTHER;
    }
}
