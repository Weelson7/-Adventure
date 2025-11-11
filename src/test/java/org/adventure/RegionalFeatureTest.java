package org.adventure;

import org.adventure.world.Biome;
import org.adventure.world.RegionalFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionalFeatureTest {

    @Test
    void testFeatureGenerationDeterminism() {
        // Same seed → same features
        double[][] elevation = createTestElevation(128, 128);
        Biome[][] biomes = createTestBiomes(elevation, 128, 128);
        long seed = 12345L;
        
        var features1 = RegionalFeature.generateFeatures(elevation, biomes, seed, 128, 128, 1.0);
        var features2 = RegionalFeature.generateFeatures(elevation, biomes, seed, 128, 128, 1.0);
        
        assertEquals(features1.size(), features2.size(), "Feature count should match");
        
        for (int i = 0; i < features1.size(); i++) {
            RegionalFeature f1 = features1.get(i);
            RegionalFeature f2 = features2.get(i);
            
            assertEquals(f1.getX(), f2.getX(), "Feature X should match");
            assertEquals(f1.getY(), f2.getY(), "Feature Y should match");
            assertEquals(f1.getType(), f2.getType(), "Feature type should match");
        }
    }

    @Test
    void testFeatureCountScalesWithWorldSize() {
        // Larger worlds should have more features
        double[][] smallElev = createTestElevation(64, 64);
        double[][] largeElev = createTestElevation(256, 256);
        Biome[][] smallBiomes = createTestBiomes(smallElev, 64, 64);
        Biome[][] largeBiomes = createTestBiomes(largeElev, 256, 256);
        
        var smallFeatures = RegionalFeature.generateFeatures(smallElev, smallBiomes, 777L, 64, 64, 1.0);
        var largeFeatures = RegionalFeature.generateFeatures(largeElev, largeBiomes, 777L, 256, 256, 1.0);
        
        assertTrue(largeFeatures.size() > smallFeatures.size(), 
            "Larger world should have more features");
    }

    @Test
    void testFeatureDensityControl() {
        // Density parameter should affect feature count
        double[][] elevation = createTestElevation(128, 128);
        Biome[][] biomes = createTestBiomes(elevation, 128, 128);
        
        var lowDensity = RegionalFeature.generateFeatures(elevation, biomes, 999L, 128, 128, 0.5);
        var highDensity = RegionalFeature.generateFeatures(elevation, biomes, 999L, 128, 128, 2.0);
        
        assertTrue(highDensity.size() > lowDensity.size(), 
            "Higher density should produce more features");
    }

    @Test
    void testVolcanoPlacement() {
        // Volcanoes should only appear at high elevation on land
        double[][] elevation = createTestElevation(128, 128);
        Biome[][] biomes = createTestBiomes(elevation, 128, 128);
        
        var features = RegionalFeature.generateFeatures(elevation, biomes, 111L, 128, 128, 5.0);
        
        for (RegionalFeature feature : features) {
            if (feature.getType() == RegionalFeature.FeatureType.VOLCANO) {
                double elev = elevation[feature.getX()][feature.getY()];
                Biome biome = biomes[feature.getX()][feature.getY()];
                
                assertTrue(elev >= 0.5, "Volcano should be at high elevation");
                assertFalse(biome.isWater(), "Volcano should not be in water");
            }
        }
    }

    @Test
    void testSubmergedCityPlacement() {
        // Submerged cities should only appear in ocean
        double[][] elevation = createTestElevation(128, 128);
        Biome[][] biomes = createTestBiomes(elevation, 128, 128);
        
        var features = RegionalFeature.generateFeatures(elevation, biomes, 222L, 128, 128, 5.0);
        
        for (RegionalFeature feature : features) {
            if (feature.getType() == RegionalFeature.FeatureType.SUBMERGED_CITY) {
                Biome biome = biomes[feature.getX()][feature.getY()];
                
                assertEquals(Biome.OCEAN, biome, "Submerged city should be in ocean");
            }
        }
    }

    @Test
    void testMagicZonePlacement() {
        // Magic zones can appear in habitable areas
        double[][] elevation = createTestElevation(128, 128);
        Biome[][] biomes = createTestBiomes(elevation, 128, 128);
        
        var features = RegionalFeature.generateFeatures(elevation, biomes, 333L, 128, 128, 5.0);
        
        for (RegionalFeature feature : features) {
            if (feature.getType() == RegionalFeature.FeatureType.MAGIC_ZONE) {
                Biome biome = biomes[feature.getX()][feature.getY()];
                
                assertTrue(biome.isHabitable(), "Magic zone should be in habitable area");
            }
        }
    }

    @Test
    void testFeatureSeparation() {
        // Features should not be too close together
        double[][] elevation = createTestElevation(128, 128);
        Biome[][] biomes = createTestBiomes(elevation, 128, 128);
        
        var features = RegionalFeature.generateFeatures(elevation, biomes, 444L, 128, 128, 2.0);
        
        int minSeparation = 10;
        
        for (int i = 0; i < features.size(); i++) {
            for (int j = i + 1; j < features.size(); j++) {
                RegionalFeature f1 = features.get(i);
                RegionalFeature f2 = features.get(j);
                
                int dx = f1.getX() - f2.getX();
                int dy = f1.getY() - f2.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                assertTrue(dist >= minSeparation, 
                    "Features should be at least " + minSeparation + " tiles apart, got " + dist);
            }
        }
    }

    @Test
    void testFeatureIntensityRange() {
        // Intensity should be in [0, 1] range
        double[][] elevation = createTestElevation(128, 128);
        Biome[][] biomes = createTestBiomes(elevation, 128, 128);
        
        var features = RegionalFeature.generateFeatures(elevation, biomes, 555L, 128, 128, 2.0);
        
        for (RegionalFeature feature : features) {
            double intensity = feature.getIntensity();
            
            assertTrue(intensity >= 0.0 && intensity <= 1.0, 
                "Intensity should be in [0, 1], got " + intensity);
        }
    }

    @Test
    void testAllFeatureTypesCanGenerate() {
        // Verify all feature types can be generated (with large enough sample)
        double[][] elevation = createDiverseElevation(256, 256);
        Biome[][] biomes = createTestBiomes(elevation, 256, 256);
        
        var features = RegionalFeature.generateFeatures(elevation, biomes, 666L, 256, 256, 10.0);
        
        var generatedTypes = new java.util.HashSet<RegionalFeature.FeatureType>();
        for (RegionalFeature feature : features) {
            generatedTypes.add(feature.getType());
        }
        
        // Should have at least 3 different types with high density
        assertTrue(generatedTypes.size() >= 3, 
            "Should generate multiple feature types, got " + generatedTypes.size());
    }

    @Test
    void testFeatureGetters() {
        // Test all getter methods
        double[][] elevation = createTestElevation(64, 64);
        Biome[][] biomes = createTestBiomes(elevation, 64, 64);
        
        var features = RegionalFeature.generateFeatures(elevation, biomes, 777L, 64, 64, 1.0);
        
        if (!features.isEmpty()) {
            RegionalFeature feature = features.get(0);
            
            assertTrue(feature.getId() >= 0, "ID should be non-negative");
            assertNotNull(feature.getType(), "Type should not be null");
            assertTrue(feature.getX() >= 0 && feature.getX() < 64, "X should be in bounds");
            assertTrue(feature.getY() >= 0 && feature.getY() < 64, "Y should be in bounds");
            assertNotNull(feature.getEffectDescription(), "Effect description should not be null");
        }
    }

    @Test
    void testFeatureEffectDescriptions() {
        // All feature types should have effect descriptions
        for (RegionalFeature.FeatureType type : RegionalFeature.FeatureType.values()) {
            RegionalFeature feature = new RegionalFeature(0, type, 0, 0, 0.5);
            String effect = feature.getEffectDescription();
            
            assertNotNull(effect, "Feature type " + type + " should have effect description");
            assertFalse(effect.isEmpty(), "Effect description should not be empty");
        }
    }

    @Test
    void testDifferentSeedsProduceDifferentFeatures() {
        // Different seeds → different features
        double[][] elevation = createTestElevation(128, 128);
        Biome[][] biomes = createTestBiomes(elevation, 128, 128);
        
        var features1 = RegionalFeature.generateFeatures(elevation, biomes, 111L, 128, 128, 1.0);
        var features2 = RegionalFeature.generateFeatures(elevation, biomes, 222L, 128, 128, 1.0);
        
        // At least one feature should differ
        boolean foundDifference = false;
        
        if (features1.size() != features2.size()) {
            foundDifference = true;
        } else {
            for (int i = 0; i < features1.size(); i++) {
                if (features1.get(i).getX() != features2.get(i).getX() ||
                    features1.get(i).getY() != features2.get(i).getY()) {
                    foundDifference = true;
                    break;
                }
            }
        }
        
        assertTrue(foundDifference, "Different seeds should produce different features");
    }

    @Test
    void testFeatureCompatibilityRules() {
        // Test feature type compatibility checking
        // Volcano: high elevation, not water
        assertTrue(RegionalFeature.FeatureType.VOLCANO.isCompatible(0.8, Biome.MOUNTAIN));
        assertFalse(RegionalFeature.FeatureType.VOLCANO.isCompatible(0.1, Biome.OCEAN));
        
        // Submerged city: ocean only
        assertTrue(RegionalFeature.FeatureType.SUBMERGED_CITY.isCompatible(0.1, Biome.OCEAN));
        assertFalse(RegionalFeature.FeatureType.SUBMERGED_CITY.isCompatible(0.5, Biome.GRASSLAND));
        
        // Magic zone: habitable areas
        assertTrue(RegionalFeature.FeatureType.MAGIC_ZONE.isCompatible(0.4, Biome.FOREST));
        assertFalse(RegionalFeature.FeatureType.MAGIC_ZONE.isCompatible(0.1, Biome.OCEAN));
    }

    // Helper: Create test elevation map
    private double[][] createTestElevation(int width, int height) {
        double[][] elevation = new double[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Create gradient: high in center, low at edges
                double dx = (x - width / 2.0) / (width / 2.0);
                double dy = (y - height / 2.0) / (height / 2.0);
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                elevation[x][y] = Math.max(0.0, 0.9 - dist * 0.5);
            }
        }
        
        return elevation;
    }

    // Helper: Create diverse elevation map (more biome variety)
    private double[][] createDiverseElevation(int width, int height) {
        double[][] elevation = new double[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Create varied terrain
                double wave1 = Math.sin(x * 0.05) * 0.3;
                double wave2 = Math.cos(y * 0.05) * 0.3;
                elevation[x][y] = 0.4 + wave1 + wave2;
                
                // Clamp
                elevation[x][y] = Math.max(0.0, Math.min(1.0, elevation[x][y]));
            }
        }
        
        return elevation;
    }

    // Helper: Create biome map from elevation
    private Biome[][] createTestBiomes(double[][] elevation, int width, int height) {
        Biome[][] biomes = new Biome[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Simple biome assignment based on elevation
                double temp = 15.0;  // Temperate
                double moisture = 0.5;  // Medium
                biomes[x][y] = Biome.assign(elevation[x][y], temp, moisture);
            }
        }
        
        return biomes;
    }
}
