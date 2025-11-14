package org.adventure.world;

/**
 * Represents a biome type with environmental characteristics.
 * Biomes are assigned to tiles based on elevation, temperature, and moisture.
 */
public enum Biome {
    // Water biomes
    OCEAN("Ocean", 0.0, 0.15, -20, 30, 1.0, 0.6),
    DEEP_OCEAN("Deep Ocean", 0.0, 0.1, -20, 15, 1.0, 0.4),
    LAKE("Lake", 0.15, 0.2, -10, 25, 0.8, 0.7),
    
    // Cold biomes
    TUNDRA("Tundra", 0.0, 1.0, -10, 5, 0.3, 0.2),
    TAIGA("Taiga", 0.2, 0.7, 0, 10, 0.5, 0.6),
    ICE("Ice", 0.0, 0.3, -40, -5, 0.1, 0.1),
    GLACIER("Glacier", 0.5, 1.0, -30, 0, 0.3, 0.2),
    
    // Temperate biomes
    GRASSLAND("Grassland", 0.2, 0.7, 5, 22, 0.4, 0.9),
    FOREST("Forest", 0.2, 0.7, 5, 25, 0.7, 0.8),
    SWAMP("Swamp", 0.2, 0.5, 10, 30, 0.9, 0.7),
    
    // Warm/dry biomes
    DESERT("Desert", 0.2, 0.7, 25, 45, 0.1, 0.1),
    SAVANNA("Savanna", 0.2, 0.6, 22, 35, 0.5, 0.7),
    
    // Hot/wet biomes
    JUNGLE("Jungle", 0.2, 0.5, 22, 35, 0.8, 0.9),
    RAINFOREST("Rainforest", 0.2, 0.5, 20, 32, 0.9, 1.0),
    
    // Mountain biomes
    HILLS("Hills", 0.6, 0.8, 0, 20, 0.4, 0.6),
    MOUNTAIN("Mountain", 0.8, 1.0, -10, 10, 0.5, 0.5),
    HIGHLAND("Highland", 0.5, 0.8, -5, 15, 0.5, 0.6),
    
    // Arid/rocky biomes
    BADLANDS("Badlands", 0.3, 0.6, 25, 40, 0.2, 0.3),
    
    // Special biomes
    VOLCANIC("Volcanic", 0.6, 0.9, 25, 50, 0.6, 1.2),
    MAGICAL("Magical", 0.3, 0.7, 5, 25, 0.6, 1.0);

    private final String displayName;
    private final double minElevation;
    private final double maxElevation;
    private final int minTemperature;  // Celsius
    private final int maxTemperature;
    private final double moisturePreference;  // 0.0 = dry, 1.0 = wet
    private final double resourceAbundance;    // 0.0 = scarce, 1.0 = abundant

    Biome(String displayName, double minElevation, double maxElevation,
          int minTemperature, int maxTemperature, double moisturePreference, double resourceAbundance) {
        this.displayName = displayName;
        this.minElevation = minElevation;
        this.maxElevation = maxElevation;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.moisturePreference = moisturePreference;
        this.resourceAbundance = resourceAbundance;
    }

    /**
     * Assign biome based on tile characteristics.
     */
    public static Biome assign(double elevation, double temperature, double moisture) {
        // Water check first
        if (elevation < 0.15) {
            return OCEAN;
        }
        if (elevation < 0.2) {
            return LAKE;
        }

        // Mountain ranges
        if (elevation > 0.8) {
            // Special case: Hot + elevated + wet = volcanic
            if (temperature > 25 && moisture > 0.6) {
                return VOLCANIC;
            }
            return MOUNTAIN;
        }

        if (elevation > 0.6) {
            // Special case: Hot + elevated + wet = volcanic
            if (temperature > 25 && moisture > 0.6) {
                return VOLCANIC;
            }
            return HILLS;
        }

        // Cold biomes
        if (temperature < 0) {
            return TUNDRA;
        }
        if (temperature < 10) {
            return TAIGA;
        }

        // Hot & dry biomes
        if (temperature > 25 && moisture < 0.3) {
            return DESERT;
        }

        // Tropical biomes
        if (temperature > 22) {
            return (moisture > 0.7) ? JUNGLE : SAVANNA;
        }

        // Temperate biomes
        if (moisture > 0.8) {
            return SWAMP;
        }
        if (moisture > 0.6) {
            return FOREST;
        }

        // Default: Grassland
        return GRASSLAND;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMinElevation() {
        return minElevation;
    }

    public double getMaxElevation() {
        return maxElevation;
    }

    public int getMinTemperature() {
        return minTemperature;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public double getMoisturePreference() {
        return moisturePreference;
    }

    public double getResourceAbundance() {
        return resourceAbundance;
    }

    /**
     * Check if biome is water-based.
     */
    public boolean isWater() {
        return this == OCEAN || this == DEEP_OCEAN || this == LAKE;
    }

    /**
     * Check if biome is habitable by default.
     */
    public boolean isHabitable() {
        return !isWater() && this != MOUNTAIN;
    }
}
