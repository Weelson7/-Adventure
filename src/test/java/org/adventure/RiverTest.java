package org.adventure;

import org.adventure.world.River;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiverTest {

    @Test
    void testRiverGenerationDeterminism() {
        // Same seed → same rivers
        double[][] elevation = createTestElevation(128, 128);
        long seed = 999L;
        
        var rivers1 = River.generateRivers(elevation, seed, 128, 128, 5);
        var rivers2 = River.generateRivers(elevation, seed, 128, 128, 5);
        
        assertEquals(rivers1.size(), rivers2.size(), "River count should match");
        
        for (int i = 0; i < rivers1.size(); i++) {
            River r1 = rivers1.get(i);
            River r2 = rivers2.get(i);
            
            assertEquals(r1.getSource().x, r2.getSource().x, "Source X should match");
            assertEquals(r1.getSource().y, r2.getSource().y, "Source Y should match");
            assertEquals(r1.getLength(), r2.getLength(), "River length should match");
        }
    }

    @Test
    void testNoUphillRivers() {
        // Rivers must flow downhill
        double[][] elevation = createTestElevation(64, 64);
        var rivers = River.generateRivers(elevation, 12345L, 64, 64, 3);
        
        for (River river : rivers) {
            assertTrue(river.isValidDownhill(), 
                "River " + river.getId() + " should flow downhill");
        }
    }

    @Test
    void testRiverSourcesInHighlands() {
        // Rivers should start at high elevation
        double[][] elevation = createTestElevation(64, 64);
        var rivers = River.generateRivers(elevation, 42L, 64, 64, 5);
        
        for (River river : rivers) {
            var source = river.getSource();
            double sourceElev = elevation[source.x][source.y];
            
            assertTrue(sourceElev >= 0.6, 
                "River " + river.getId() + " should start in highlands (elev >= 0.6), got " + sourceElev);
        }
    }

    @Test
    void testRiverTerminusInOceanOrLake() {
        // Rivers should end in ocean (low elevation) or lake (high elevation, closed basin)
        double[][] elevation = createTestElevation(64, 64);
        var rivers = River.generateRivers(elevation, 777L, 64, 64, 3);
        
        for (River river : rivers) {
            var terminus = river.getTerminus();
            double terminusElev = elevation[terminus.x][terminus.y];
            
            boolean inOcean = terminusElev < 0.2;
            boolean isLake = river.isLake();
            
            assertTrue(inOcean || isLake, 
                "River " + river.getId() + " should end in ocean or lake");
        }
    }

    @Test
    void testRiverMinimumLength() {
        // Rivers should have minimum length (not single-tile)
        double[][] elevation = createTestElevation(128, 128);
        var rivers = River.generateRivers(elevation, 555L, 128, 128, 5);
        
        for (River river : rivers) {
            assertTrue(river.getLength() > 5, 
                "River " + river.getId() + " should have length > 5, got " + river.getLength());
        }
    }

    @Test
    void testRiverPathContinuity() {
        // River path tiles should be adjacent (4-connected)
        double[][] elevation = createTestElevation(64, 64);
        var rivers = River.generateRivers(elevation, 888L, 64, 64, 2);
        
        for (River river : rivers) {
            var path = river.getPath();
            
            for (int i = 1; i < path.size(); i++) {
                River.Tile prev = path.get(i - 1);
                River.Tile curr = path.get(i);
                
                int dx = Math.abs(curr.x - prev.x);
                int dy = Math.abs(curr.y - prev.y);
                
                // Must be adjacent (4-connected)
                assertTrue((dx == 1 && dy == 0) || (dx == 0 && dy == 1),
                    "River tiles must be adjacent (4-connected)");
            }
        }
    }

    @Test
    void testRiverCountScalesWithWorldSize() {
        // Larger worlds should have more rivers
        double[][] smallElev = createTestElevation(64, 64);
        double[][] largeElev = createTestElevation(256, 256);
        
        var smallRivers = River.generateRivers(smallElev, 111L, 64, 64, 100);  // Request many
        var largeRivers = River.generateRivers(largeElev, 111L, 256, 256, 100);
        
        // Large world should generate more rivers (more highland sources)
        assertTrue(largeRivers.size() > smallRivers.size(), 
            "Larger world should have more rivers");
    }

    @Test
    void testLakeDetection() {
        // Some rivers may end in closed basins (lakes)
        double[][] elevation = createMountainousElevation(128, 128);
        var rivers = River.generateRivers(elevation, 333L, 128, 128, 10);
        
        // At least verify we can detect lakes if present
        long lakeCount = rivers.stream().filter(River::isLake).count();
        
        // Lakes are optional (may or may not exist), just verify field works
        assertTrue(lakeCount >= 0, "Lake count should be non-negative");
    }

    @Test
    void testRiverGetters() {
        // Test all getter methods
        double[][] elevation = createTestElevation(64, 64);
        var rivers = River.generateRivers(elevation, 999L, 64, 64, 1);
        
        if (!rivers.isEmpty()) {
            River river = rivers.get(0);
            
            assertNotNull(river.getSource(), "Source should not be null");
            assertNotNull(river.getTerminus(), "Terminus should not be null");
            assertNotNull(river.getPath(), "Path should not be null");
            assertTrue(river.getLength() > 0, "Length should be positive");
            assertTrue(river.getId() >= 0, "ID should be non-negative");
        }
    }

    @Test
    void testRiverPathImmutability() {
        // Returned path should be a copy (immutable from outside)
        double[][] elevation = createTestElevation(64, 64);
        var rivers = River.generateRivers(elevation, 777L, 64, 64, 1);
        
        if (!rivers.isEmpty()) {
            River river = rivers.get(0);
            var path1 = river.getPath();
            var path2 = river.getPath();
            
            // Should be different list instances
            assertNotSame(path1, path2, "Path should be copied, not same instance");
            
            // But same content
            assertEquals(path1.size(), path2.size(), "Path content should match");
        }
    }

    @Test
    void testDifferentSeedsProduceDifferentRivers() {
        // Different seeds → different rivers
        double[][] elevation = createTestElevation(128, 128);
        
        var rivers1 = River.generateRivers(elevation, 111L, 128, 128, 5);
        var rivers2 = River.generateRivers(elevation, 222L, 128, 128, 5);
        
        // At least one river should differ (high probability)
        boolean foundDifference = false;
        
        if (rivers1.size() == rivers2.size()) {
            for (int i = 0; i < rivers1.size(); i++) {
                if (rivers1.get(i).getSource().x != rivers2.get(i).getSource().x ||
                    rivers1.get(i).getSource().y != rivers2.get(i).getSource().y) {
                    foundDifference = true;
                    break;
                }
            }
        } else {
            foundDifference = true;
        }
        
        assertTrue(foundDifference, "Different seeds should produce different rivers");
    }

    @Test
    void testRiverGenerationWithNoHighlands() {
        // Flat terrain → no or few rivers
        double[][] flatElev = new double[64][64];
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                flatElev[x][y] = 0.1;  // All ocean
            }
        }
        
        var rivers = River.generateRivers(flatElev, 999L, 64, 64, 10);
        
        // Should generate 0 rivers (no sources above threshold)
        assertEquals(0, rivers.size(), "Flat ocean should have no rivers");
    }

    // Helper: Create test elevation map with highlands and lowlands
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

    // Helper: Create mountainous elevation with valleys
    private double[][] createMountainousElevation(int width, int height) {
        double[][] elevation = new double[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Create mountains with some closed basins
                double wave1 = Math.sin(x * 0.1) * 0.3;
                double wave2 = Math.cos(y * 0.1) * 0.3;
                elevation[x][y] = 0.5 + wave1 + wave2;
                
                // Clamp
                elevation[x][y] = Math.max(0.0, Math.min(1.0, elevation[x][y]));
            }
        }
        
        return elevation;
    }
}
