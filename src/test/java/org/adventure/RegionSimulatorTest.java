package org.adventure;

import org.adventure.region.Region;
import org.adventure.region.RegionSimulator;
import org.adventure.region.ResourceNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionSimulatorTest {

    @Test
    void testRegionSimulatorDefaults() {
        RegionSimulator sim = new RegionSimulator();

        assertEquals(1.0, sim.getTickLength(), 0.001, "Default tick length should be 1 second");
        assertEquals(1.0, sim.getActiveTickRateMultiplier(), 0.001, "Default active multiplier should be 1.0");
        assertEquals(1.0 / 60.0, sim.getBackgroundTickRateMultiplier(), 0.001,
                "Default background multiplier should be 1/60");
        assertEquals(0, sim.getCurrentTick(), "Should start at tick 0");
        assertEquals(0, sim.getRegionCount(), "Should start with 0 regions");
    }

    @Test
    void testRegionSimulatorCustomConfiguration() {
        RegionSimulator sim = new RegionSimulator(2.0, 1.5, 0.5);

        assertEquals(2.0, sim.getTickLength(), 0.001, "Custom tick length");
        assertEquals(1.5, sim.getActiveTickRateMultiplier(), 0.001, "Custom active multiplier");
        assertEquals(0.5, sim.getBackgroundTickRateMultiplier(), 0.001, "Custom background multiplier");
    }

    @Test
    void testAddAndGetRegion() {
        RegionSimulator sim = new RegionSimulator();
        Region region = new Region(1, 256, 256, 64, 64);

        sim.addRegion(region);

        assertEquals(1, sim.getRegionCount(), "Should have 1 region");
        assertNotNull(sim.getRegion(1), "Should retrieve region by ID");
        assertEquals(region, sim.getRegion(1), "Retrieved region should match");
    }

    @Test
    void testGetAllRegions() {
        RegionSimulator sim = new RegionSimulator();
        Region region1 = new Region(1, 100, 100, 64, 64);
        Region region2 = new Region(2, 200, 200, 64, 64);
        Region region3 = new Region(3, 300, 300, 64, 64);

        sim.addRegion(region1);
        sim.addRegion(region2);
        sim.addRegion(region3);

        assertEquals(3, sim.getAllRegions().size(), "Should have 3 regions");
        assertTrue(sim.getAllRegions().contains(region1), "Should contain region1");
        assertTrue(sim.getAllRegions().contains(region2), "Should contain region2");
        assertTrue(sim.getAllRegions().contains(region3), "Should contain region3");
    }

    @Test
    void testActivateRegion() {
        RegionSimulator sim = new RegionSimulator();
        Region region = new Region(1, 256, 256, 64, 64);
        sim.addRegion(region);

        assertEquals(Region.RegionState.BACKGROUND, region.getState(), "Should start in background");

        sim.activateRegion(1);

        assertEquals(Region.RegionState.ACTIVE, region.getState(), "Should be active after activation");
    }

    @Test
    void testDeactivateRegion() {
        RegionSimulator sim = new RegionSimulator();
        Region region = new Region(1, 256, 256, 64, 64);
        region.setState(Region.RegionState.ACTIVE);
        sim.addRegion(region);

        sim.deactivateRegion(1);

        assertEquals(Region.RegionState.BACKGROUND, region.getState(), "Should be background after deactivation");
    }

    @Test
    void testTickAdvancesCounter() {
        RegionSimulator sim = new RegionSimulator();

        assertEquals(0, sim.getCurrentTick(), "Should start at tick 0");

        sim.tick();
        assertEquals(1, sim.getCurrentTick(), "Should be at tick 1");

        sim.tick();
        assertEquals(2, sim.getCurrentTick(), "Should be at tick 2");
    }

    @Test
    void testAdvanceTicks() {
        RegionSimulator sim = new RegionSimulator();

        sim.advanceTicks(10);

        assertEquals(10, sim.getCurrentTick(), "Should be at tick 10");

        sim.advanceTicks(5);

        assertEquals(15, sim.getCurrentTick(), "Should be at tick 15");
    }

    @Test
    void testActiveRegionProcessing() {
        RegionSimulator sim = new RegionSimulator();
        Region region = new Region(1, 256, 256, 64, 64);
        region.setState(Region.RegionState.ACTIVE);

        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 10.0);
        node.setCurrentQuantity(50.0);
        region.addResourceNode(node);

        sim.addRegion(region);

        // Process 1 tick (1 second at default multiplier)
        sim.tick();

        // Expected: 50 + 10*1*(1-50/100) = 50 + 5 = 55
        assertEquals(55.0, node.getCurrentQuantity(), 0.001, "Active region should regenerate resources");
        assertEquals(1, region.getLastProcessedTick(), "Last processed tick should be 1");
    }

    @Test
    void testBackgroundRegionProcessing() {
        RegionSimulator sim = new RegionSimulator();
        Region region = new Region(1, 256, 256, 64, 64);
        // Stays in BACKGROUND state

        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 10.0);
        node.setCurrentQuantity(50.0);
        region.addResourceNode(node);

        sim.addRegion(region);

        // Process 60 ticks (background updates every 60 ticks at default 1/60 multiplier)
        sim.advanceTicks(60);

        // At tick 60, background region processes with deltaTime = 60 * 1.0 * (1/60) = 1.0 second
        // Expected: 50 + 10*1*(1-50/100) = 50 + 5 = 55
        assertEquals(55.0, node.getCurrentQuantity(), 0.01, "Background region should regenerate after 60 ticks");
    }

    @Test
    void testResynchronizationOnActivation() {
        RegionSimulator sim = new RegionSimulator();
        Region region = new Region(1, 256, 256, 64, 64);
        // Starts in BACKGROUND state

        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 10.0);
        node.setCurrentQuantity(50.0);
        region.addResourceNode(node);

        sim.addRegion(region);

        // Advance 120 ticks in background
        sim.advanceTicks(120);

        // Activate region at tick 120
        sim.activateRegion(1);

        // Resynchronization should apply: deltaTime = 120 * 1.0 * (1/60) = 2.0 seconds
        // Expected: 50 + 10*2*(1-50/100) = 50 + 10 = 60
        // Note: Due to discrete tick processing, may be slightly less (59.5-60)
        assertTrue(node.getCurrentQuantity() >= 59.5 && node.getCurrentQuantity() <= 60.0,
                "Should resynchronize to ~60 (got " + node.getCurrentQuantity() + ")");
        assertEquals(120, region.getLastProcessedTick(), "Last processed tick should be current tick");
    }

    @Test
    void testActiveRegionCounters() {
        RegionSimulator sim = new RegionSimulator();
        Region region1 = new Region(1, 100, 100, 64, 64);
        Region region2 = new Region(2, 200, 200, 64, 64);
        Region region3 = new Region(3, 300, 300, 64, 64);

        region1.setState(Region.RegionState.ACTIVE);
        region2.setState(Region.RegionState.BACKGROUND);
        region3.setState(Region.RegionState.ACTIVE);

        sim.addRegion(region1);
        sim.addRegion(region2);
        sim.addRegion(region3);

        assertEquals(3, sim.getRegionCount(), "Should have 3 regions total");
        assertEquals(2, sim.getActiveRegionCount(), "Should have 2 active regions");
        assertEquals(1, sim.getBackgroundRegionCount(), "Should have 1 background region");
    }

    @Test
    void testTickDeterminism() {
        // Same initial conditions → same results
        RegionSimulator sim1 = new RegionSimulator();
        RegionSimulator sim2 = new RegionSimulator();

        Region region1 = new Region(1, 256, 256, 64, 64);
        Region region2 = new Region(1, 256, 256, 64, 64);

        region1.setState(Region.RegionState.ACTIVE);
        region2.setState(Region.RegionState.ACTIVE);

        ResourceNode node1 = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 10.0);
        ResourceNode node2 = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 10.0);

        node1.setCurrentQuantity(50.0);
        node2.setCurrentQuantity(50.0);

        region1.addResourceNode(node1);
        region2.addResourceNode(node2);

        sim1.addRegion(region1);
        sim2.addRegion(region2);

        sim1.advanceTicks(10);
        sim2.advanceTicks(10);

        assertEquals(node1.getCurrentQuantity(), node2.getCurrentQuantity(), 0.001,
                "Same conditions should produce same results");
    }

    @Test
    void testRegionUpgradeDowngradeCycle() {
        RegionSimulator sim = new RegionSimulator();
        Region region = new Region(1, 256, 256, 64, 64);

        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 10.0);
        node.setCurrentQuantity(50.0);
        region.addResourceNode(node);

        sim.addRegion(region);

        // Start in background, advance 60 ticks
        sim.advanceTicks(60);
        double quantityAfterBackground = node.getCurrentQuantity();

        // Activate and advance 10 active ticks
        sim.activateRegion(1);
        sim.advanceTicks(10);
        double quantityAfterActive = node.getCurrentQuantity();

        assertTrue(quantityAfterActive > quantityAfterBackground, "Resources should increase");

        // Deactivate and verify state preserved
        sim.deactivateRegion(1);
        assertEquals(Region.RegionState.BACKGROUND, region.getState(), "Should be background");
        assertEquals(70, region.getLastProcessedTick(), "Last processed tick should be updated");
    }

    @Test
    void testMultipleRegionsSimultaneously() {
        RegionSimulator sim = new RegionSimulator();

        Region activeRegion = new Region(1, 100, 100, 64, 64);
        Region backgroundRegion = new Region(2, 200, 200, 64, 64);

        activeRegion.setState(Region.RegionState.ACTIVE);
        backgroundRegion.setState(Region.RegionState.BACKGROUND);

        ResourceNode activeNode = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 90, 90, 100.0, 10.0);
        ResourceNode backgroundNode = new ResourceNode(2, ResourceNode.ResourceType.WOOD, 190, 190, 100.0, 10.0);

        activeNode.setCurrentQuantity(50.0);
        backgroundNode.setCurrentQuantity(50.0);

        activeRegion.addResourceNode(activeNode);
        backgroundRegion.addResourceNode(backgroundNode);

        sim.addRegion(activeRegion);
        sim.addRegion(backgroundRegion);

        sim.advanceTicks(60);

        // Active region processes every tick: 10 ticks → significant regeneration
        assertTrue(activeNode.getCurrentQuantity() > 90.0, "Active region should regenerate significantly");

        // Background region processes once at tick 60: ~1 second effective → modest regeneration
        assertTrue(backgroundNode.getCurrentQuantity() > 50.0 && backgroundNode.getCurrentQuantity() < 60.0,
                "Background region should regenerate modestly");
    }

    @Test
    void testResourceCapsEnforced() {
        RegionSimulator sim = new RegionSimulator();
        Region region = new Region(1, 256, 256, 64, 64);
        region.setState(Region.RegionState.ACTIVE);

        ResourceNode node = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 50.0);
        node.setCurrentQuantity(95.0); // Near max with high regen rate

        region.addResourceNode(node);
        sim.addRegion(region);

        sim.advanceTicks(100); // Many ticks

        assertEquals(100.0, node.getCurrentQuantity(), 0.001, "Resource should cap at Rmax");
        assertFalse(node.getCurrentQuantity() > 100.0, "Should never exceed Rmax");
    }
}
