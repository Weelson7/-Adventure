package org.adventure;

import org.adventure.story.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SaturationManager class.
 */
public class SaturationManagerTest {

    @Test
    public void testRegisterStoryIncrementsCount() {
        SaturationManager satMgr = new SaturationManager();
        
        assertEquals(0, satMgr.getStoryCount(1));
        
        satMgr.registerStory(1, StoryType.LEGEND);
        assertEquals(1, satMgr.getStoryCount(1));
        
        satMgr.registerStory(1, StoryType.QUEST);
        assertEquals(2, satMgr.getStoryCount(1));
    }

    @Test
    public void testRegisterEventIncrementsCount() {
        SaturationManager satMgr = new SaturationManager();
        
        assertEquals(0, satMgr.getEventCount(1));
        
        satMgr.registerEvent(1, EventCategory.REGIONAL);
        assertEquals(1, satMgr.getEventCount(1));
        
        satMgr.registerEvent(1, EventCategory.WORLD);
        assertEquals(2, satMgr.getEventCount(1));
    }

    @Test
    public void testUnregisterStoryDecrementsCount() {
        SaturationManager satMgr = new SaturationManager();
        
        satMgr.registerStory(1, StoryType.LEGEND);
        satMgr.registerStory(1, StoryType.QUEST);
        assertEquals(2, satMgr.getStoryCount(1));
        
        satMgr.unregisterStory(1, StoryType.LEGEND);
        assertEquals(1, satMgr.getStoryCount(1));
        
        satMgr.unregisterStory(1, StoryType.QUEST);
        assertEquals(0, satMgr.getStoryCount(1));
    }

    @Test
    public void testUnregisterEventDecrementsCount() {
        SaturationManager satMgr = new SaturationManager();
        
        satMgr.registerEvent(1, EventCategory.REGIONAL);
        satMgr.registerEvent(1, EventCategory.WORLD);
        assertEquals(2, satMgr.getEventCount(1));
        
        satMgr.unregisterEvent(1, EventCategory.REGIONAL);
        assertEquals(1, satMgr.getEventCount(1));
        
        satMgr.unregisterEvent(1, EventCategory.WORLD);
        assertEquals(0, satMgr.getEventCount(1));
    }

    @Test
    public void testSaturationFactorAtZero() {
        SaturationManager satMgr = new SaturationManager(50, 20);
        
        // No events registered, saturation factor should be 1.0 (100%)
        assertEquals(1.0, satMgr.getSaturationFactor(1, EventCategory.REGIONAL), 0.001);
        assertEquals(1.0, satMgr.getStorySaturationFactor(1, StoryType.LEGEND), 0.001);
    }

    @Test
    public void testSaturationFactorAtHalfCap() {
        SaturationManager satMgr = new SaturationManager(50, 20);
        
        // Register 10 events (half of max 20)
        for (int i = 0; i < 10; i++) {
            satMgr.registerEvent(1, EventCategory.REGIONAL);
        }
        
        // Saturation factor = 1 - (10/20) = 0.5
        assertEquals(0.5, satMgr.getSaturationFactor(1, EventCategory.REGIONAL), 0.001);
    }

    @Test
    public void testSaturationFactorAtFullCap() {
        SaturationManager satMgr = new SaturationManager(50, 20);
        
        // Register 20 events (max)
        for (int i = 0; i < 20; i++) {
            satMgr.registerEvent(1, EventCategory.REGIONAL);
        }
        
        // Saturation factor = 1 - (20/20) = 0.0
        assertEquals(0.0, satMgr.getSaturationFactor(1, EventCategory.REGIONAL), 0.001);
    }

    @Test
    public void testSaturationFactorOverCap() {
        SaturationManager satMgr = new SaturationManager(50, 20);
        
        // Register 25 events (over max)
        for (int i = 0; i < 25; i++) {
            satMgr.registerEvent(1, EventCategory.REGIONAL);
        }
        
        // Saturation factor = max(0, 1 - (25/20)) = 0.0
        assertEquals(0.0, satMgr.getSaturationFactor(1, EventCategory.REGIONAL), 0.001);
    }

    @Test
    public void testIsStoryCapReachedAtSoftCap() {
        SaturationManager satMgr = new SaturationManager(50, 20);
        
        // Register 40 stories (80% of 50 = soft cap)
        for (int i = 0; i < 40; i++) {
            satMgr.registerStory(1, StoryType.LEGEND);
        }
        
        assertTrue(satMgr.isStoryCapReached(1));
    }

    @Test
    public void testIsEventCapReachedAtSoftCap() {
        SaturationManager satMgr = new SaturationManager(50, 20);
        
        // Register 16 events (80% of 20 = soft cap)
        for (int i = 0; i < 16; i++) {
            satMgr.registerEvent(1, EventCategory.REGIONAL);
        }
        
        assertTrue(satMgr.isEventCapReached(1));
    }

    @Test
    public void testIsCapReachedBelowSoftCap() {
        SaturationManager satMgr = new SaturationManager(50, 20);
        
        // Register 10 events (50% of 20, below 80% soft cap)
        for (int i = 0; i < 10; i++) {
            satMgr.registerEvent(1, EventCategory.REGIONAL);
        }
        
        assertFalse(satMgr.isEventCapReached(1));
    }

    @Test
    public void testGetStoryTypeCount() {
        SaturationManager satMgr = new SaturationManager();
        
        satMgr.registerStory(1, StoryType.LEGEND);
        satMgr.registerStory(1, StoryType.LEGEND);
        satMgr.registerStory(1, StoryType.QUEST);
        
        assertEquals(2, satMgr.getStoryTypeCount(1, StoryType.LEGEND));
        assertEquals(1, satMgr.getStoryTypeCount(1, StoryType.QUEST));
        assertEquals(0, satMgr.getStoryTypeCount(1, StoryType.RUMOR));
    }

    @Test
    public void testGetEventCategoryCount() {
        SaturationManager satMgr = new SaturationManager();
        
        satMgr.registerEvent(1, EventCategory.REGIONAL);
        satMgr.registerEvent(1, EventCategory.REGIONAL);
        satMgr.registerEvent(1, EventCategory.WORLD);
        
        assertEquals(2, satMgr.getEventCategoryCount(1, EventCategory.REGIONAL));
        assertEquals(1, satMgr.getEventCategoryCount(1, EventCategory.WORLD));
        assertEquals(0, satMgr.getEventCategoryCount(1, EventCategory.PERSONAL));
    }

    @Test
    public void testMultipleRegions() {
        SaturationManager satMgr = new SaturationManager();
        
        satMgr.registerStory(1, StoryType.LEGEND);
        satMgr.registerStory(2, StoryType.QUEST);
        satMgr.registerEvent(1, EventCategory.REGIONAL);
        satMgr.registerEvent(2, EventCategory.WORLD);
        
        assertEquals(1, satMgr.getStoryCount(1));
        assertEquals(1, satMgr.getStoryCount(2));
        assertEquals(1, satMgr.getEventCount(1));
        assertEquals(1, satMgr.getEventCount(2));
    }

    @Test
    public void testReset() {
        SaturationManager satMgr = new SaturationManager();
        
        satMgr.registerStory(1, StoryType.LEGEND);
        satMgr.registerEvent(1, EventCategory.REGIONAL);
        
        assertEquals(1, satMgr.getStoryCount(1));
        assertEquals(1, satMgr.getEventCount(1));
        
        satMgr.reset();
        
        assertEquals(0, satMgr.getStoryCount(1));
        assertEquals(0, satMgr.getEventCount(1));
    }

    @Test
    public void testUnregisterNonExistentStory() {
        SaturationManager satMgr = new SaturationManager();
        
        // Should not throw or go negative
        satMgr.unregisterStory(1, StoryType.LEGEND);
        assertEquals(0, satMgr.getStoryCount(1));
    }

    @Test
    public void testUnregisterNonExistentEvent() {
        SaturationManager satMgr = new SaturationManager();
        
        // Should not throw or go negative
        satMgr.unregisterEvent(1, EventCategory.REGIONAL);
        assertEquals(0, satMgr.getEventCount(1));
    }

    @Test
    public void testCustomCaps() {
        SaturationManager satMgr = new SaturationManager(100, 40);
        
        // Register 50 stories (50% of 100)
        for (int i = 0; i < 50; i++) {
            satMgr.registerStory(1, StoryType.LEGEND);
        }
        
        // Saturation factor = 1 - (50/100) = 0.5
        assertEquals(0.5, satMgr.getStorySaturationFactor(1, StoryType.LEGEND), 0.001);
    }

    @Test
    public void testDefaultCaps() {
        SaturationManager satMgr = new SaturationManager();
        
        // Default: maxStoriesPerRegion = 50, maxEventsPerRegion = 20
        
        // Register 25 stories (50% of 50)
        for (int i = 0; i < 25; i++) {
            satMgr.registerStory(1, StoryType.LEGEND);
        }
        
        assertEquals(0.5, satMgr.getStorySaturationFactor(1, StoryType.LEGEND), 0.001);
    }
}
