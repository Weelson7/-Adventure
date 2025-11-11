package org.adventure;

import org.adventure.region.Region;
import org.adventure.region.ResourceNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionTest {

    @Test
    void testRegionCreation() {
        Region region = new Region(1, 256, 256, 64, 64);

        assertEquals(1, region.getId(), "ID should match");
        assertEquals(256, region.getCenterX(), "Center X should match");
        assertEquals(256, region.getCenterY(), "Center Y should match");
        assertEquals(64, region.getWidth(), "Width should match");
        assertEquals(64, region.getHeight(), "Height should match");
        assertEquals(0, region.getLastProcessedTick(), "Should start at tick 0");
        assertEquals(Region.RegionState.BACKGROUND, region.getState(), "Should start in background state");
        assertEquals(0, region.getNpcCount(), "Should start with 0 NPCs");
    }

    @Test
    void testRegionContainsPoint() {
        Region region = new Region(1, 256, 256, 64, 64);
        // Region spans [224, 288) in both dimensions

        assertTrue(region.contains(256, 256), "Should contain center point");
        assertTrue(region.contains(224, 224), "Should contain bottom-left corner");
        assertTrue(region.contains(287, 287), "Should contain top-right corner (inclusive)");

        assertFalse(region.contains(223, 256), "Should not contain point outside left edge");
        assertFalse(region.contains(288, 256), "Should not contain point outside right edge");
        assertFalse(region.contains(256, 223), "Should not contain point outside bottom edge");
        assertFalse(region.contains(256, 288), "Should not contain point outside top edge");
    }

    @Test
    void testRegionStateTransitions() {
        Region region = new Region(1, 256, 256, 64, 64);

        assertEquals(Region.RegionState.BACKGROUND, region.getState(), "Should start in background");

        region.setState(Region.RegionState.ACTIVE);
        assertEquals(Region.RegionState.ACTIVE, region.getState(), "Should transition to active");

        region.setState(Region.RegionState.BACKGROUND);
        assertEquals(Region.RegionState.BACKGROUND, region.getState(), "Should transition back to background");
    }

    @Test
    void testRegionResourceNodes() {
        Region region = new Region(1, 256, 256, 64, 64);

        assertEquals(0, region.getResourceNodes().size(), "Should start with no resource nodes");

        ResourceNode node1 = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 5.0);
        ResourceNode node2 = new ResourceNode(2, ResourceNode.ResourceType.ORE, 260, 260, 100.0, 0.0);

        region.addResourceNode(node1);
        region.addResourceNode(node2);

        assertEquals(2, region.getResourceNodes().size(), "Should have 2 resource nodes");
        assertTrue(region.getResourceNodes().contains(node1), "Should contain node1");
        assertTrue(region.getResourceNodes().contains(node2), "Should contain node2");
    }

    @Test
    void testRegionResourceRegeneration() {
        Region region = new Region(1, 256, 256, 64, 64);

        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 10.0);
        node.setCurrentQuantity(50.0);
        region.addResourceNode(node);

        // Regenerate for 2 seconds at tick 100
        region.regenerateResources(100, 2.0);

        // Expected: 50 + 10*2*(1-50/100) = 50 + 10 = 60
        assertEquals(60.0, node.getCurrentQuantity(), 0.001, "Resource should regenerate");
        assertEquals(100, region.getLastProcessedTick(), "Last processed tick should update");
    }

    @Test
    void testRegionMultipleResourceRegeneration() {
        Region region = new Region(1, 256, 256, 64, 64);

        ResourceNode wood = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 10.0);
        ResourceNode crops = new ResourceNode(2, ResourceNode.ResourceType.CROPS, 250, 250, 100.0, 8.0);

        wood.setCurrentQuantity(50.0);
        crops.setCurrentQuantity(40.0);

        region.addResourceNode(wood);
        region.addResourceNode(crops);

        region.regenerateResources(50, 1.0);

        // Wood: 50 + 10*1*(1-50/100) = 50 + 5 = 55
        // Crops: 40 + 8*1*(1-40/100) = 40 + 4.8 = 44.8
        assertEquals(55.0, wood.getCurrentQuantity(), 0.001, "Wood should regenerate");
        assertEquals(44.8, crops.getCurrentQuantity(), 0.001, "Crops should regenerate");
    }

    @Test
    void testRegionTickUpdate() {
        Region region = new Region(1, 256, 256, 64, 64);

        assertEquals(0, region.getLastProcessedTick(), "Should start at tick 0");

        region.setLastProcessedTick(100);
        assertEquals(100, region.getLastProcessedTick(), "Should update to tick 100");

        region.setLastProcessedTick(500);
        assertEquals(500, region.getLastProcessedTick(), "Should update to tick 500");
    }

    @Test
    void testRegionNpcCount() {
        Region region = new Region(1, 256, 256, 64, 64);

        assertEquals(0, region.getNpcCount(), "Should start with 0 NPCs");

        region.setNpcCount(50);
        assertEquals(50, region.getNpcCount(), "Should have 50 NPCs");

        region.setNpcCount(200);
        assertEquals(200, region.getNpcCount(), "Should have 200 NPCs");
    }

    @Test
    void testRegionBoundaryContains() {
        Region region = new Region(1, 100, 100, 20, 20);
        // Region spans [90, 110) in both dimensions

        assertTrue(region.contains(90, 90), "Should contain minimum boundary (inclusive)");
        assertTrue(region.contains(109, 109), "Should contain maximum boundary - 1");
        assertFalse(region.contains(110, 110), "Should not contain maximum boundary (exclusive)");
        assertFalse(region.contains(89, 100), "Should not contain below minimum");
    }

    @Test
    void testRegionGetters() {
        Region region = new Region(42, 512, 384, 128, 96);

        assertEquals(42, region.getId(), "ID getter");
        assertEquals(512, region.getCenterX(), "Center X getter");
        assertEquals(384, region.getCenterY(), "Center Y getter");
        assertEquals(128, region.getWidth(), "Width getter");
        assertEquals(96, region.getHeight(), "Height getter");
    }
}
