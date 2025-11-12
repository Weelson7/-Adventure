package org.adventure;

import org.adventure.world.Plate;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PlateTest {

    @Test
    void testCreateRandomPlateDeterminism() {
        // Same seed → identical plate properties
        Random rng1 = new Random(123456789L);
        Random rng2 = new Random(123456789L);
        
        Plate p1 = Plate.createRandomPlate(0, 512, 512, 999L, rng1);
        Plate p2 = Plate.createRandomPlate(0, 512, 512, 999L, rng2);
        
        assertEquals(p1.getId(), p2.getId(), "IDs should match");
        assertEquals(p1.getCenterX(), p2.getCenterX(), "Center X should match");
        assertEquals(p1.getCenterY(), p2.getCenterY(), "Center Y should match");
        assertEquals(p1.getDriftX(), p2.getDriftX(), 0.0001, "Drift X should match");
        assertEquals(p1.getDriftY(), p2.getDriftY(), 0.0001, "Drift Y should match");
        assertEquals(p1.getType(), p2.getType(), "Plate type should match");
    }

    @Test
    void testDifferentPlatesHaveDifferentProperties() {
        Random rng = new Random(999L);
        
        Plate p1 = Plate.createRandomPlate(0, 512, 512, 123L, rng);
        Plate p2 = Plate.createRandomPlate(1, 512, 512, 123L, rng);
        
        // Different IDs → different centers (with high probability)
        assertNotEquals(p1.getCenterX(), p2.getCenterX(), "Different plates should have different centers");
    }

    @Test
    void testPlatePropertiesInValidRange() {
        Random rng = new Random(777L);
        Plate plate = Plate.createRandomPlate(5, 512, 512, 123456789L, rng);
        
        // Center within world bounds
        assertTrue(plate.getCenterX() >= 0 && plate.getCenterX() < 512, "Center X in bounds");
        assertTrue(plate.getCenterY() >= 0 && plate.getCenterY() < 512, "Center Y in bounds");
        
        // Drift vectors in [-0.5, +0.5] range
        assertTrue(plate.getDriftX() >= -0.5 && plate.getDriftX() <= 0.5, "Drift X in range");
        assertTrue(plate.getDriftY() >= -0.5 && plate.getDriftY() <= 0.5, "Drift Y in range");
        
        // Type is valid enum value
        assertNotNull(plate.getType(), "Plate type should not be null");
    }

    @Test
    void testCollisionDetectionConvergingPlates() {
        // Plates moving toward each other (head-on collision)
        Plate p1 = new Plate(0, 100, 100, +0.3, 0, Plate.PlateType.CONTINENTAL);
        Plate p2 = new Plate(1, 200, 100, -0.3, 0, Plate.PlateType.OCEANIC);
        
        assertTrue(p1.isColliding(p2), "Plates moving toward each other should collide");
        assertTrue(p2.isColliding(p1), "Collision should be symmetric");
    }

    @Test
    void testCollisionDetectionDivergingPlates() {
        // Plates moving away from each other
        Plate p1 = new Plate(0, 100, 100, -0.3, 0, Plate.PlateType.CONTINENTAL);
        Plate p2 = new Plate(1, 200, 100, +0.3, 0, Plate.PlateType.OCEANIC);
        
        assertFalse(p1.isColliding(p2), "Plates moving apart should not collide");
        assertFalse(p2.isColliding(p1), "Non-collision should be symmetric");
    }

    @Test
    void testCollisionDetectionParallelPlates() {
        // Plates moving in parallel (no collision)
        Plate p1 = new Plate(0, 100, 100, 0, +0.4, Plate.PlateType.CONTINENTAL);
        Plate p2 = new Plate(1, 200, 100, 0, +0.4, Plate.PlateType.OCEANIC);
        
        assertFalse(p1.isColliding(p2), "Parallel plates should not collide");
    }

    @Test
    void testCollisionIntensityRange() {
        // Max drift = 0.5, so max relative drift = 1.0
        // Max intensity = 1.0² / 4 = 0.25
        Plate p1 = new Plate(0, 0, 0, +0.5, 0, Plate.PlateType.CONTINENTAL);
        Plate p2 = new Plate(1, 100, 0, -0.5, 0, Plate.PlateType.OCEANIC);
        
        double intensity = p1.collisionIntensity(p2);
        
        assertTrue(intensity >= 0, "Intensity should be non-negative");
        assertTrue(intensity <= 0.25, "Intensity should not exceed 0.25");
    }

    @Test
    void testCollisionIntensityZeroForStaticPlates() {
        // No drift → no collision intensity
        Plate p1 = new Plate(0, 0, 0, 0, 0, Plate.PlateType.CONTINENTAL);
        Plate p2 = new Plate(1, 100, 0, 0, 0, Plate.PlateType.OCEANIC);
        
        double intensity = p1.collisionIntensity(p2);
        
        assertEquals(0.0, intensity, 0.0001, "Static plates should have zero intensity");
    }

    @Test
    void testCollisionIntensitySymmetry() {
        Plate p1 = new Plate(0, 100, 100, +0.3, -0.2, Plate.PlateType.CONTINENTAL);
        Plate p2 = new Plate(1, 200, 150, -0.1, +0.4, Plate.PlateType.OCEANIC);
        
        double intensity1 = p1.collisionIntensity(p2);
        double intensity2 = p2.collisionIntensity(p1);
        
        assertEquals(intensity1, intensity2, 0.0001, "Intensity should be symmetric");
    }

    @Test
    void testTileManagement() {
        Random rng = new Random(123L);
        Plate plate = Plate.createRandomPlate(0, 512, 512, 999L, rng);
        
        // Initially empty tile list
        assertEquals(0, plate.getTiles().size(), "New plate should have no tiles");
        
        // Add tiles
        plate.addTile(10, 20);
        plate.addTile(30, 40);
        plate.addTile(50, 60);
        
        assertEquals(3, plate.getTiles().size(), "Should have 3 tiles");
        
        // Verify coordinates
        assertEquals(10, plate.getTiles().get(0).x, "First tile X");
        assertEquals(20, plate.getTiles().get(0).y, "First tile Y");
        assertEquals(30, plate.getTiles().get(1).x, "Second tile X");
        assertEquals(40, plate.getTiles().get(1).y, "Second tile Y");
    }

    @Test
    void testPlateTypeDistribution() {
        // Over many plates, expect ~70% continental (matching Earth's distribution)
        Random rng = new Random(42L);
        int continentalCount = 0;
        int totalPlates = 100;
        
        for (int i = 0; i < totalPlates; i++) {
            Plate plate = Plate.createRandomPlate(i, 512, 512, 123456789L, rng);
            if (plate.getType() == Plate.PlateType.CONTINENTAL) {
                continentalCount++;
            }
        }
        
        double continentalRatio = continentalCount / (double) totalPlates;
        
        // Expect 70% ± 15% (allowing variance in random distribution)
        assertTrue(continentalRatio >= 0.55 && continentalRatio <= 0.85,
                "Continental ratio should be ~0.7, got " + continentalRatio);
    }

    @Test
    void testGettersReturnCorrectValues() {
        Plate plate = new Plate(42, 256, 128, 0.25, -0.15, Plate.PlateType.OCEANIC);
        
        assertEquals(42, plate.getId(), "ID getter");
        assertEquals(256, plate.getCenterX(), "Center X getter");
        assertEquals(128, plate.getCenterY(), "Center Y getter");
        assertEquals(0.25, plate.getDriftX(), 0.0001, "Drift X getter");
        assertEquals(-0.15, plate.getDriftY(), 0.0001, "Drift Y getter");
        assertEquals(Plate.PlateType.OCEANIC, plate.getType(), "Type getter");
    }
}
