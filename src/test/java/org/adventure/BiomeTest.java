package org.adventure;

import org.adventure.world.Biome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BiomeTest {

    @Test
    void testOceanAssignment() {
        // Low elevation → ocean
        Biome biome = Biome.assign(0.1, 10, 0.5);
        assertEquals(Biome.OCEAN, biome, "Elevation 0.1 should be OCEAN");
    }

    @Test
    void testLakeAssignment() {
        // Elevation 0.15-0.2 → lake
        Biome biome = Biome.assign(0.18, 15, 0.6);
        assertEquals(Biome.LAKE, biome, "Elevation 0.18 should be LAKE");
    }

    @Test
    void testMountainAssignment() {
        // High elevation → mountain
        Biome biome = Biome.assign(0.9, 15, 0.4);
        assertEquals(Biome.MOUNTAIN, biome, "Elevation 0.9 should be MOUNTAIN");
    }

    @Test
    void testHillsAssignment() {
        // Moderate elevation (0.6-0.8) → hills
        Biome biome = Biome.assign(0.7, 18, 0.5);
        assertEquals(Biome.HILLS, biome, "Elevation 0.7 should be HILLS");
    }

    @Test
    void testVolcanicAssignment() {
        // Elevated + hot + wet → volcanic
        Biome biome = Biome.assign(0.7, 30, 0.7);
        assertEquals(Biome.VOLCANIC, biome, "Elevated hot wet terrain should be VOLCANIC");
    }

    @Test
    void testTundraAssignment() {
        // Cold temperature → tundra
        Biome biome = Biome.assign(0.4, -5, 0.3);
        assertEquals(Biome.TUNDRA, biome, "Temperature -5°C should be TUNDRA");
    }

    @Test
    void testTaigaAssignment() {
        // Cool temperature (0-10°C) → taiga
        Biome biome = Biome.assign(0.35, 5, 0.4);
        assertEquals(Biome.TAIGA, biome, "Temperature 5°C should be TAIGA");
    }

    @Test
    void testDesertAssignment() {
        // Hot + dry → desert
        Biome biome = Biome.assign(0.4, 35, 0.2);
        assertEquals(Biome.DESERT, biome, "Hot and dry should be DESERT");
    }

    @Test
    void testJungleAssignment() {
        // Hot + wet → jungle
        Biome biome = Biome.assign(0.3, 28, 0.9);
        assertEquals(Biome.JUNGLE, biome, "Hot and wet should be JUNGLE");
    }

    @Test
    void testSavannaAssignment() {
        // Hot + moderate moisture → savanna
        Biome biome = Biome.assign(0.35, 27, 0.5);
        assertEquals(Biome.SAVANNA, biome, "Hot with moderate moisture should be SAVANNA");
    }

    @Test
    void testForestAssignment() {
        // Temperate + high moisture (not swamp level) → forest
        Biome biome = Biome.assign(0.4, 15, 0.7);
        assertEquals(Biome.FOREST, biome, "Temperate with high moisture should be FOREST");
    }

    @Test
    void testSwampAssignment() {
        // Temperate + very high moisture → swamp
        Biome biome = Biome.assign(0.3, 18, 0.9);
        assertEquals(Biome.SWAMP, biome, "Temperate with very high moisture should be SWAMP");
    }

    @Test
    void testGrasslandAssignment() {
        // Temperate + moderate moisture (default biome)
        Biome biome = Biome.assign(0.4, 15, 0.5);
        assertEquals(Biome.GRASSLAND, biome, "Temperate moderate conditions should be GRASSLAND");
    }

    @Test
    void testWaterHelper() {
        assertTrue(Biome.OCEAN.isWater(), "OCEAN should be water");
        assertTrue(Biome.LAKE.isWater(), "LAKE should be water");
        assertFalse(Biome.GRASSLAND.isWater(), "GRASSLAND should not be water");
        assertFalse(Biome.MOUNTAIN.isWater(), "MOUNTAIN should not be water");
        assertFalse(Biome.DESERT.isWater(), "DESERT should not be water");
    }

    @Test
    void testHabitabilityHelper() {
        // Water is not habitable
        assertFalse(Biome.OCEAN.isHabitable(), "OCEAN should not be habitable");
        assertFalse(Biome.LAKE.isHabitable(), "LAKE should not be habitable");
        
        // Mountains are not habitable
        assertFalse(Biome.MOUNTAIN.isHabitable(), "MOUNTAIN should not be habitable");
        
        // Land biomes are habitable (even harsh ones)
        assertTrue(Biome.GRASSLAND.isHabitable(), "GRASSLAND should be habitable");
        assertTrue(Biome.FOREST.isHabitable(), "FOREST should be habitable");
        assertTrue(Biome.DESERT.isHabitable(), "DESERT should be habitable (harsh but habitable)");
        assertTrue(Biome.TUNDRA.isHabitable(), "TUNDRA should be habitable (harsh but habitable)");
        assertTrue(Biome.JUNGLE.isHabitable(), "JUNGLE should be habitable");
        assertTrue(Biome.SWAMP.isHabitable(), "SWAMP should be habitable");
        assertTrue(Biome.HILLS.isHabitable(), "HILLS should be habitable");
        assertTrue(Biome.VOLCANIC.isHabitable(), "VOLCANIC should be habitable");
    }

    @Test
    void testBiomeDeterminism() {
        // Same inputs → same biome (multiple calls)
        Biome b1 = Biome.assign(0.5, 20, 0.6);
        Biome b2 = Biome.assign(0.5, 20, 0.6);
        Biome b3 = Biome.assign(0.5, 20, 0.6);
        
        assertEquals(b1, b2, "Biome assignment should be deterministic");
        assertEquals(b2, b3, "Biome assignment should be deterministic");
    }

    @Test
    void testEdgeCaseElevationBoundaries() {
        // Test exact threshold values
        assertEquals(Biome.OCEAN, Biome.assign(0.14, 15, 0.5), "Just below lake threshold");
        assertEquals(Biome.LAKE, Biome.assign(0.15, 15, 0.5), "At lake threshold");
        assertEquals(Biome.LAKE, Biome.assign(0.19, 15, 0.5), "Just below land threshold");
        // At 0.2, elevation check passes, then temperature/moisture determine biome
    }

    @Test
    void testEdgeCaseTemperatureBoundaries() {
        assertEquals(Biome.TUNDRA, Biome.assign(0.5, -1, 0.5), "Just below freezing");
        assertEquals(Biome.TAIGA, Biome.assign(0.5, 0, 0.5), "At freezing point");
        assertEquals(Biome.TAIGA, Biome.assign(0.5, 9, 0.5), "Just below temperate");
    }

    @Test
    void testResourceAbundanceVariation() {
        // Different biomes have different resource abundances
        assertTrue(Biome.JUNGLE.getResourceAbundance() > Biome.DESERT.getResourceAbundance(),
                "Jungle should have more resources than desert");
        assertTrue(Biome.GRASSLAND.getResourceAbundance() > Biome.TUNDRA.getResourceAbundance(),
                "Grassland should have more resources than tundra");
        assertTrue(Biome.FOREST.getResourceAbundance() > Biome.MOUNTAIN.getResourceAbundance(),
                "Forest should have more resources than mountain");
    }

    @Test
    void testAllBiomesHaveValidProperties() {
        // Ensure all biomes have sensible property ranges
        for (Biome biome : Biome.values()) {
            // Elevation range valid
            assertTrue(biome.getMinElevation() >= 0.0, biome + " minElevation >= 0");
            assertTrue(biome.getMaxElevation() <= 1.0, biome + " maxElevation <= 1");
            assertTrue(biome.getMinElevation() <= biome.getMaxElevation(),
                    biome + " min <= max elevation");
            
            // Temperature range valid (allowing extreme values)
            assertTrue(biome.getMinTemperature() >= -50, biome + " minTemp >= -50");
            assertTrue(biome.getMaxTemperature() <= 50, biome + " maxTemp <= 50");
            assertTrue(biome.getMinTemperature() <= biome.getMaxTemperature(),
                    biome + " min <= max temperature");
            
            // Moisture preference valid
            assertTrue(biome.getMoisturePreference() >= 0.0, biome + " moisture >= 0");
            assertTrue(biome.getMoisturePreference() <= 1.0, biome + " moisture <= 1");
            
            // Resource abundance valid
            assertTrue(biome.getResourceAbundance() >= 0.0, biome + " abundance >= 0");
            assertTrue(biome.getResourceAbundance() <= 1.5, biome + " abundance <= 1.5");
        }
    }

    @Test
    void testExtremeColdPrefersTundra() {
        Biome biome = Biome.assign(0.5, -30, 0.3);
        assertEquals(Biome.TUNDRA, biome, "Extreme cold should be TUNDRA");
    }

    @Test
    void testExtremeHeatWithMoistureBecomesJungle() {
        Biome biome = Biome.assign(0.3, 35, 0.85);
        assertEquals(Biome.JUNGLE, biome, "Extreme heat + moisture should be JUNGLE");
    }

    @Test
    void testExtremeHeatWithoutMoistureBecomesDesert() {
        Biome biome = Biome.assign(0.4, 40, 0.15);
        assertEquals(Biome.DESERT, biome, "Extreme heat + dryness should be DESERT");
    }

    @Test
    void testVolcanicRequiresSpecificConditions() {
        // Volcanic needs: elevation > 0.6, temp > 25, moisture > 0.6
        assertEquals(Biome.VOLCANIC, Biome.assign(0.7, 28, 0.65),
                "Should be VOLCANIC with all conditions met");
        
        // Missing one condition → not volcanic
        assertNotEquals(Biome.VOLCANIC, Biome.assign(0.5, 28, 0.65),
                "Low elevation prevents VOLCANIC");
        assertNotEquals(Biome.VOLCANIC, Biome.assign(0.7, 20, 0.65),
                "Low temp prevents VOLCANIC");
        assertNotEquals(Biome.VOLCANIC, Biome.assign(0.7, 28, 0.3),
                "Low moisture prevents VOLCANIC");
    }

    @Test
    void testCoastalBiomeTransitions() {
        // Simulate coastal transition: ocean → beach/grassland
        Biome water = Biome.assign(0.15, 18, 0.5);
        Biome coast = Biome.assign(0.25, 18, 0.5);
        
        assertTrue(water.isWater(), "Should be water");
        assertFalse(coast.isWater(), "Should be land");
    }
}
