package org.adventure;

import org.adventure.structure.EntranceSide;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EntranceSide enum.
 * Validates offset calculations and entrance coordinate generation.
 */
public class EntranceSideTest {
    
    @Test
    public void testNorthOffset() {
        int[] offset = EntranceSide.NORTH.getOffset();
        assertArrayEquals(new int[]{0, -1}, offset);
    }
    
    @Test
    public void testEastOffset() {
        int[] offset = EntranceSide.EAST.getOffset();
        assertArrayEquals(new int[]{1, 0}, offset);
    }
    
    @Test
    public void testSouthOffset() {
        int[] offset = EntranceSide.SOUTH.getOffset();
        assertArrayEquals(new int[]{0, 1}, offset);
    }
    
    @Test
    public void testWestOffset() {
        int[] offset = EntranceSide.WEST.getOffset();
        assertArrayEquals(new int[]{-1, 0}, offset);
    }
    
    @Test
    public void testGetEntranceCoordsNorth() {
        int[] coords = EntranceSide.NORTH.getEntranceCoords(10, 20);
        assertArrayEquals(new int[]{10, 19}, coords); // North means -Y
    }
    
    @Test
    public void testGetEntranceCoordsEast() {
        int[] coords = EntranceSide.EAST.getEntranceCoords(10, 20);
        assertArrayEquals(new int[]{11, 20}, coords); // East means +X
    }
    
    @Test
    public void testGetEntranceCoordsSouth() {
        int[] coords = EntranceSide.SOUTH.getEntranceCoords(10, 20);
        assertArrayEquals(new int[]{10, 21}, coords); // South means +Y
    }
    
    @Test
    public void testGetEntranceCoordsWest() {
        int[] coords = EntranceSide.WEST.getEntranceCoords(10, 20);
        assertArrayEquals(new int[]{9, 20}, coords); // West means -X
    }
    
    @Test
    public void testGetEntranceCoordsAtOrigin() {
        int[] coords = EntranceSide.SOUTH.getEntranceCoords(0, 0);
        assertArrayEquals(new int[]{0, 1}, coords);
    }
    
    @Test
    public void testAllDirectionsDistinct() {
        int[][] offsets = {
                EntranceSide.NORTH.getOffset(),
                EntranceSide.EAST.getOffset(),
                EntranceSide.SOUTH.getOffset(),
                EntranceSide.WEST.getOffset()
        };
        
        // Verify all offsets are different
        for (int i = 0; i < offsets.length; i++) {
            for (int j = i + 1; j < offsets.length; j++) {
                assertFalse(
                        offsets[i][0] == offsets[j][0] && offsets[i][1] == offsets[j][1],
                        "EntranceSide offsets should be distinct"
                );
            }
        }
    }
}
