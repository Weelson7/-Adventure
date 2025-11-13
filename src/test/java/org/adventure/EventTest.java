package org.adventure;

import org.adventure.story.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Event class.
 */
public class EventTest {

    @Test
    public void testEventBuilderCreatesValidEvent() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.REGIONAL)
                .name("Bandit Raid")
                .description("Bandits are raiding the village.")
                .originTileId(200)
                .originTick(100)
                .build();

        assertNotNull(event);
        assertEquals("event_1", event.getId());
        assertEquals(EventCategory.REGIONAL, event.getCategory());
        assertEquals("Bandit Raid", event.getName());
        assertEquals(200, event.getOriginTileId());
        assertEquals(EventStatus.PENDING, event.getStatus());
    }

    @Test
    public void testEventBuilderRequiresId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Event.Builder()
                    .category(EventCategory.WORLD)
                    .name("Event")
                    .build();
        });
    }

    @Test
    public void testEventBuilderRequiresCategory() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Event.Builder()
                    .id("event_1")
                    .name("Event")
                    .build();
        });
    }

    @Test
    public void testEventBuilderRequiresName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Event.Builder()
                    .id("event_1")
                    .category(EventCategory.PERSONAL)
                    .build();
        });
    }

    @Test
    public void testEventBuilderValidatesBaseProbability() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Event.Builder()
                    .id("event_1")
                    .category(EventCategory.RANDOM)
                    .name("Event")
                    .baseProbability(-0.1)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Event.Builder()
                    .id("event_1")
                    .category(EventCategory.RANDOM)
                    .name("Event")
                    .baseProbability(1.5)
                    .build();
        });
    }

    @Test
    public void testEventBuilderValidatesPriority() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Event.Builder()
                    .id("event_1")
                    .category(EventCategory.TRIGGERED)
                    .name("Event")
                    .priority(-1)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Event.Builder()
                    .id("event_1")
                    .category(EventCategory.TRIGGERED)
                    .name("Event")
                    .priority(11)
                    .build();
        });
    }

    @Test
    public void testEventStatusTransitions() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.REGIONAL)
                .name("Festival")
                .build();

        assertEquals(EventStatus.PENDING, event.getStatus());

        event.setStatus(EventStatus.ACTIVE);
        assertEquals(EventStatus.ACTIVE, event.getStatus());

        event.setStatus(EventStatus.PROPAGATING);
        assertEquals(EventStatus.PROPAGATING, event.getStatus());

        event.setStatus(EventStatus.COMPLETED);
        assertEquals(EventStatus.COMPLETED, event.getStatus());
    }

    @Test
    public void testEventIsTriggered() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.TRIGGERED)
                .name("Boss Defeated")
                .status(EventStatus.PENDING)
                .build();

        assertFalse(event.isTriggered());

        event.setStatus(EventStatus.ACTIVE);
        assertTrue(event.isTriggered());

        event.setStatus(EventStatus.PROPAGATING);
        assertTrue(event.isTriggered());

        event.setStatus(EventStatus.COMPLETED);
        assertFalse(event.isTriggered());
    }

    @Test
    public void testEventTriggerConditions() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.TRIGGERED)
                .name("Dragon Slain")
                .triggerCondition("killedBy", "player_1")
                .triggerCondition("location", "mountain")
                .build();

        assertEquals(2, event.getTriggerConditions().size());
        assertEquals("player_1", event.getTriggerConditions().get("killedBy"));
        assertEquals("mountain", event.getTriggerConditions().get("location"));
    }

    @Test
    public void testEventEffects() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.WORLD)
                .name("Solar Eclipse")
                .effect("lighting", 0.3)
                .effect("spawnRate", 1.5)
                .build();

        assertEquals(2, event.getEffects().size());
        assertEquals(0.3, event.getEffects().get("lighting"));
        assertEquals(1.5, event.getEffects().get("spawnRate"));

        event.setEffect("duration", 3600);
        assertEquals(3600, event.getEffects().get("duration"));
    }

    @Test
    public void testEventAffectedRegions() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.REGIONAL)
                .name("Plague")
                .addAffectedRegion("region_1")
                .addAffectedRegion("region_2")
                .build();

        assertEquals(2, event.getAffectedRegions().size());
        assertTrue(event.getAffectedRegions().contains("region_1"));
        assertTrue(event.getAffectedRegions().contains("region_2"));

        event.addAffectedRegion("region_3");
        assertEquals(3, event.getAffectedRegions().size());
    }

    @Test
    public void testEventLinkedStory() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.PERSONAL)
                .name("Quest Completed")
                .linkedStoryId("story_123")
                .build();

        assertEquals("story_123", event.getLinkedStoryId());
    }

    @Test
    public void testEventMetadata() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.RANDOM)
                .name("Meteor Shower")
                .metadata("intensity", 0.8)
                .metadata("duration", 600)
                .build();

        assertEquals(0.8, event.getMetadata().get("intensity"));
        assertEquals(600, event.getMetadata().get("duration"));

        event.setMetadata("witnessed", true);
        assertEquals(true, event.getMetadata().get("witnessed"));
    }

    @Test
    public void testEventDefaults() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.WORLD)
                .name("Global Event")
                .build();

        assertEquals(EventStatus.PENDING, event.getStatus());
        assertEquals(0.9, event.getBaseProbability(), 0.001);
        assertEquals(0, event.getHopCount());
        assertEquals(6, event.getMaxHops());
        assertEquals(5, event.getPriority());
        assertEquals(0, event.getLastProcessedTick());
    }

    @Test
    public void testEventSchemaVersion() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.REGIONAL)
                .name("Regional Event")
                .build();

        assertEquals(1, event.getSchemaVersion());
        assertEquals("story/Event", event.getType());
    }

    @Test
    public void testEventEquality() {
        Event event1 = new Event.Builder()
                .id("event_1")
                .category(EventCategory.WORLD)
                .name("Event 1")
                .build();

        Event event2 = new Event.Builder()
                .id("event_1")
                .category(EventCategory.REGIONAL)
                .name("Event 2")
                .build();

        Event event3 = new Event.Builder()
                .id("event_2")
                .category(EventCategory.WORLD)
                .name("Event 1")
                .build();

        assertEquals(event1, event2); // Same ID
        assertNotEquals(event1, event3); // Different ID
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    public void testEventToString() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.TRIGGERED)
                .name("Dragon Defeated")
                .priority(9)
                .hopCount(1)
                .maxHops(5)
                .build();

        String str = event.toString();
        assertTrue(str.contains("event_1"));
        assertTrue(str.contains("TRIGGERED"));
        assertTrue(str.contains("priority=9"));
    }

    @Test
    public void testEventHopCountTracking() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.REGIONAL)
                .name("Event")
                .hopCount(0)
                .maxHops(6)
                .build();

        assertEquals(0, event.getHopCount());
        assertEquals(6, event.getMaxHops());

        event.setHopCount(4);
        assertEquals(4, event.getHopCount());
    }

    @Test
    public void testEventLastProcessedTick() {
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.WORLD)
                .name("Event")
                .lastProcessedTick(0)
                .build();

        assertEquals(0, event.getLastProcessedTick());

        event.setLastProcessedTick(5000);
        assertEquals(5000, event.getLastProcessedTick());
    }
}
