package antifraud.repository.entity;

public enum Region {
    EAP("EAP", "East Asia and Pacific"),
    ECA("ECA", "Europe and Central Asia"),
    HIC("HIC", "High-Income countries"),
    LAC("LAC", "Latin America and the Caribbean"),
    MENA("MENA", "The Middle East and North Africa"),
    SA("SA", "South Asia"),
    SSA("SSA", "Sub-Saharan Africa");

    private final String code;
    private final String description;

    Region(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public static Region enumerate(String e) {
        for (Region i : Region.values()) {
            if (i.getCode().equals(e)) {
                return i;
            }
        }
        return null;
    }
}