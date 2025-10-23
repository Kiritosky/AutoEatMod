package autoeatmod.food;

public enum FoodCategory {
    SUPER_PREMIUM(5, "Super Premium"),
    PREMIUM(4, "Premium"),
    NORMAL(3, "Normal"),
    LOW_QUALITY(2, "Low Quality"),
    DANGEROUS(1, "Dangerous"),
    UNKNOWN(0, "Unknown");

    private final int priority;
    private final String displayName;

    FoodCategory(int priority, String displayName) {
        this.priority = priority;
        this.displayName = displayName;
    }

    public int getPriority() {
        return priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDangerous() {
        return this == DANGEROUS;
    }

    public boolean isPremium() {
        return this == PREMIUM || this == SUPER_PREMIUM;
    }
}

