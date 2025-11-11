package org.adventure.world;

import java.util.*;

/**
 * RegionalFeature represents special landmarks like volcanoes, magic zones, and submerged cities.
 * Features are placed using weighted random distribution with logic checks.
 */
public class RegionalFeature {
    
    public enum FeatureType {
        VOLCANO(0.02, 0.7, 1.0),           // Rare, high elevation, any temperature
        MAGIC_ZONE(0.03, 0.2, 0.8),        // Uncommon, varied elevation
        SUBMERGED_CITY(0.01, 0.0, 0.15),   // Very rare, underwater only
        ANCIENT_RUINS(0.04, 0.3, 0.9),     // Uncommon, land-based
        CRYSTAL_CAVE(0.02, 0.5, 0.85);     // Rare, mountainous
        
        private final double rarity;        // Probability weight (lower = rarer)
        private final double minElevation;  // Minimum elevation requirement
        private final double maxElevation;  // Maximum elevation requirement
        
        FeatureType(double rarity, double minElevation, double maxElevation) {
            this.rarity = rarity;
            this.minElevation = minElevation;
            this.maxElevation = maxElevation;
        }
        
        public double getRarity() {
            return rarity;
        }
        
        public double getMinElevation() {
            return minElevation;
        }
        
        public double getMaxElevation() {
            return maxElevation;
        }
        
        /**
         * Check if this feature type is compatible with given conditions.
         */
        public boolean isCompatible(double elevation, Biome biome) {
            // Elevation check
            if (elevation < minElevation || elevation > maxElevation) {
                return false;
            }
            
            // Special rules for specific features
            switch (this) {
                case VOLCANO:
                    // Volcanoes only on land, prefer mountains
                    return !biome.isWater() && elevation > 0.5;
                    
                case SUBMERGED_CITY:
                    // Only in ocean
                    return biome == Biome.OCEAN;
                    
                case MAGIC_ZONE:
                    // Can be anywhere habitable
                    return biome.isHabitable();
                    
                case ANCIENT_RUINS:
                    // Land-based, habitable areas
                    return !biome.isWater() && biome.isHabitable();
                    
                case CRYSTAL_CAVE:
                    // Mountains or high elevation
                    return biome == Biome.MOUNTAIN || biome == Biome.HILLS || elevation > 0.6;
                    
                default:
                    return true;
            }
        }
    }
    
    private final int id;
    private final FeatureType type;
    private final int x;
    private final int y;
    private final double intensity;  // Feature strength/magnitude (0.0 to 1.0)
    
    public RegionalFeature(int id, FeatureType type, int x, int y, double intensity) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.intensity = Math.max(0.0, Math.min(1.0, intensity));
    }
    
    /**
     * Generate regional features for a world.
     * 
     * @param elevation Elevation map
     * @param biomes Biome map
     * @param seed Random seed for determinism
     * @param width World width
     * @param height World height
     * @param density Feature density multiplier (1.0 = standard)
     * @return List of generated features
     */
    public static List<RegionalFeature> generateFeatures(double[][] elevation, Biome[][] biomes,
                                                          long seed, int width, int height, double density) {
        List<RegionalFeature> features = new ArrayList<>();
        Random rng = new Random(seed);
        
        // Calculate base number of features (1 per ~5000 tiles)
        int baseFeatureCount = Math.max(3, (width * height) / 5000);
        int targetFeatureCount = (int) (baseFeatureCount * density);
        
        // Track occupied tiles to prevent overlapping
        Set<TileCoord> occupiedTiles = new HashSet<>();
        int minSeparation = 10;  // Minimum tiles between features
        
        // Build weighted list of feature types
        List<FeatureType> weightedTypes = buildWeightedTypeList();
        
        int attempts = 0;
        int maxAttempts = targetFeatureCount * 10;  // Allow retries
        
        while (features.size() < targetFeatureCount && attempts < maxAttempts) {
            attempts++;
            
            // Random location
            int x = rng.nextInt(width);
            int y = rng.nextInt(height);
            
            // Check if too close to existing feature
            if (isTooClose(x, y, occupiedTiles, minSeparation)) {
                continue;
            }
            
            // Random feature type (weighted)
            FeatureType type = weightedTypes.get(rng.nextInt(weightedTypes.size()));
            
            // Check compatibility
            if (!type.isCompatible(elevation[x][y], biomes[x][y])) {
                continue;
            }
            
            // Generate intensity (slightly skewed toward higher values)
            double intensity = 0.3 + rng.nextDouble() * 0.7;
            
            // Create feature
            RegionalFeature feature = new RegionalFeature(features.size(), type, x, y, intensity);
            features.add(feature);
            occupiedTiles.add(new TileCoord(x, y));
        }
        
        return features;
    }
    
    /**
     * Build weighted list of feature types based on rarity.
     */
    private static List<FeatureType> buildWeightedTypeList() {
        List<FeatureType> weighted = new ArrayList<>();
        
        for (FeatureType type : FeatureType.values()) {
            // Add type multiple times based on rarity (higher rarity = more copies)
            int weight = (int) (type.getRarity() * 100);
            for (int i = 0; i < weight; i++) {
                weighted.add(type);
            }
        }
        
        return weighted;
    }
    
    /**
     * Check if location is too close to existing features.
     */
    private static boolean isTooClose(int x, int y, Set<TileCoord> occupied, int minSeparation) {
        for (TileCoord coord : occupied) {
            int dx = x - coord.x;
            int dy = y - coord.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            if (dist < minSeparation) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Simple coordinate class.
     */
    private static class TileCoord {
        final int x;
        final int y;
        
        TileCoord(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TileCoord coord = (TileCoord) o;
            return x == coord.x && y == coord.y;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
    
    /**
     * Get gameplay effect description for this feature.
     */
    public String getEffectDescription() {
        switch (type) {
            case VOLCANO:
                return "Increased fire damage, obsidian resources, risk of eruption";
            case MAGIC_ZONE:
                return "Enhanced magical abilities, rare spell components, mana regeneration";
            case SUBMERGED_CITY:
                return "Ancient artifacts, treasure, underwater exploration required";
            case ANCIENT_RUINS:
                return "Historical lore, rare items, possible guardian enemies";
            case CRYSTAL_CAVE:
                return "Crystal resources, light magic boost, gem mining";
            default:
                return "Unknown effect";
        }
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public FeatureType getType() {
        return type;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public double getIntensity() {
        return intensity;
    }
}
