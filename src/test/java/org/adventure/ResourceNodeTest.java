package org.adventure;

import org.adventure.region.ResourceNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNodeTest {

    @Test
    void testResourceNodeCreation() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 10, 20, 100.0, 5.0);

        assertEquals(1, node.getId(), "ID should match");
        assertEquals(ResourceNode.ResourceType.WOOD, node.getType(), "Type should match");
        assertEquals(10, node.getX(), "X coordinate should match");
        assertEquals(20, node.getY(), "Y coordinate should match");
        assertEquals(100.0, node.getRMax(), 0.001, "Rmax should match");
        assertEquals(5.0, node.getRegenRate(), 0.001, "Regen rate should match");
        assertEquals(100.0, node.getCurrentQuantity(), 0.001, "Should start at full capacity");
    }

    @Test
    void testResourceRegeneration() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 10.0);
        node.setCurrentQuantity(50.0); // Start at half capacity

        // Regenerate for 1 second: R(t+1) = 50 + 10 * 1 * (1 - 50/100) = 50 + 5 = 55
        node.regenerate(1.0);

        assertEquals(55.0, node.getCurrentQuantity(), 0.001, "Should regenerate 5 units");
    }

    @Test
    void testResourceRegenerationApproachesMax() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 10.0);
        node.setCurrentQuantity(90.0); // Start near max

        // Regenerate for 1 second: R(t+1) = 90 + 10 * 1 * (1 - 90/100) = 90 + 1 = 91
        node.regenerate(1.0);

        assertEquals(91.0, node.getCurrentQuantity(), 0.001, "Should regenerate 1 unit (approaching max)");
    }

    @Test
    void testResourceRegenerationNeverExceedsMax() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 50.0);
        node.setCurrentQuantity(99.0);

        // Large regen rate, but should cap at Rmax
        node.regenerate(10.0);

        assertEquals(100.0, node.getCurrentQuantity(), 0.001, "Should cap at Rmax");
    }

    @Test
    void testResourceRegenerationWhenFull() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 10.0);

        // Already at max
        node.regenerate(5.0);

        assertEquals(100.0, node.getCurrentQuantity(), 0.001, "Should stay at max");
    }

    @Test
    void testResourceHarvesting() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 5.0);

        double harvested = node.harvest(30.0);

        assertEquals(30.0, harvested, 0.001, "Should harvest 30 units");
        assertEquals(70.0, node.getCurrentQuantity(), 0.001, "Should have 70 units remaining");
    }

    @Test
    void testResourceHarvestingInsufficientResources() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 5.0);
        node.setCurrentQuantity(20.0);

        double harvested = node.harvest(50.0); // Try to harvest more than available

        assertEquals(20.0, harvested, 0.001, "Should harvest only 20 units (all available)");
        assertEquals(0.0, node.getCurrentQuantity(), 0.001, "Should be depleted");
    }

    @Test
    void testResourceHarvestAndRegenCycle() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 10.0);

        // Harvest 50 units
        node.harvest(50.0);
        assertEquals(50.0, node.getCurrentQuantity(), 0.001, "Should have 50 after harvest");

        // Regenerate for 2 seconds: R(t+2) = 50 + 10*2*(1-50/100) = 50 + 10 = 60
        node.regenerate(2.0);
        assertEquals(60.0, node.getCurrentQuantity(), 0.001, "Should regenerate to 60");
    }

    @Test
    void testFiniteResourceZeroRegen() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.ORE, 0, 0, 100.0, 0.0);
        node.setCurrentQuantity(50.0);

        // No regeneration for finite resources
        node.regenerate(100.0);

        assertEquals(50.0, node.getCurrentQuantity(), 0.001, "Finite resources should not regenerate");
    }

    @Test
    void testIsDepletedFlag() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 5.0);

        assertFalse(node.isDepleted(), "Should not be depleted initially");

        node.setCurrentQuantity(0.0);
        assertTrue(node.isDepleted(), "Should be depleted when quantity is 0");

        node.setCurrentQuantity(0.1);
        assertFalse(node.isDepleted(), "Should not be depleted when quantity > 0");
    }

    @Test
    void testIsFullFlag() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 5.0);

        assertTrue(node.isFull(), "Should be full initially");

        node.setCurrentQuantity(99.9);
        assertFalse(node.isFull(), "Should not be full when quantity < Rmax");

        node.setCurrentQuantity(100.0);
        assertTrue(node.isFull(), "Should be full when quantity == Rmax");
    }

    @Test
    void testSetQuantityClamps() {
        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 5.0);

        // Try to set above max
        node.setCurrentQuantity(150.0);
        assertEquals(100.0, node.getCurrentQuantity(), 0.001, "Should clamp to Rmax");

        // Try to set below zero
        node.setCurrentQuantity(-50.0);
        assertEquals(0.0, node.getCurrentQuantity(), 0.001, "Should clamp to 0");
    }

    @Test
    void testResourceTypes() {
        // Test all resource types
        ResourceNode wood = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 10.0);
        ResourceNode ore = new ResourceNode(2, ResourceNode.ResourceType.ORE, 0, 0, 100.0, 0.0);
        ResourceNode crops = new ResourceNode(3, ResourceNode.ResourceType.CROPS, 0, 0, 100.0, 8.0);
        ResourceNode stone = new ResourceNode(4, ResourceNode.ResourceType.STONE, 0, 0, 100.0, 0.0);
        ResourceNode herbs = new ResourceNode(5, ResourceNode.ResourceType.HERBS, 0, 0, 100.0, 6.0);

        assertEquals(ResourceNode.ResourceType.WOOD, wood.getType());
        assertEquals(ResourceNode.ResourceType.ORE, ore.getType());
        assertEquals(ResourceNode.ResourceType.CROPS, crops.getType());
        assertEquals(ResourceNode.ResourceType.STONE, stone.getType());
        assertEquals(ResourceNode.ResourceType.HERBS, herbs.getType());
    }

    @Test
    void testRegenFormulaDeterminism() {
        // Same initial conditions → same results
        ResourceNode node1 = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 10.0);
        ResourceNode node2 = new ResourceNode(2, ResourceNode.ResourceType.WOOD, 0, 0, 100.0, 10.0);

        node1.setCurrentQuantity(50.0);
        node2.setCurrentQuantity(50.0);

        node1.regenerate(3.0);
        node2.regenerate(3.0);

        assertEquals(node1.getCurrentQuantity(), node2.getCurrentQuantity(), 0.001,
                "Same conditions should produce same regeneration");
    }
}
