package org.adventure;

import org.adventure.story.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Story class.
 */
public class StoryTest {

    @Test
    public void testStoryBuilderCreatesValidStory() {
        Story story = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.LEGEND)
                .title("The Lost Sword")
                .description("An ancient sword was lost in the mountains.")
                .originTileId(100)
                .originTick(0)
                .build();

        assertNotNull(story);
        assertEquals("story_1", story.getId());
        assertEquals(StoryType.LEGEND, story.getStoryType());
        assertEquals("The Lost Sword", story.getTitle());
        assertEquals(100, story.getOriginTileId());
        assertEquals(StoryStatus.ACTIVE, story.getStatus());
    }

    @Test
    public void testStoryBuilderRequiresId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Story.Builder()
                    .storyType(StoryType.QUEST)
                    .title("Quest")
                    .build();
        });
    }

    @Test
    public void testStoryBuilderRequiresStoryType() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Story.Builder()
                    .id("story_1")
                    .title("Quest")
                    .build();
        });
    }

    @Test
    public void testStoryBuilderRequiresTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Story.Builder()
                    .id("story_1")
                    .storyType(StoryType.QUEST)
                    .build();
        });
    }

    @Test
    public void testStoryBuilderValidatesBaseProbability() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Story.Builder()
                    .id("story_1")
                    .storyType(StoryType.QUEST)
                    .title("Quest")
                    .baseProbability(-0.1)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Story.Builder()
                    .id("story_1")
                    .storyType(StoryType.QUEST)
                    .title("Quest")
                    .baseProbability(1.5)
                    .build();
        });
    }

    @Test
    public void testStoryBuilderValidatesPriority() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Story.Builder()
                    .id("story_1")
                    .storyType(StoryType.QUEST)
                    .title("Quest")
                    .priority(-1)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Story.Builder()
                    .id("story_1")
                    .storyType(StoryType.QUEST)
                    .title("Quest")
                    .priority(11)
                    .build();
        });
    }

    @Test
    public void testStoryStatusTransitions() {
        Story story = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.RUMOR)
                .title("Strange Sounds")
                .build();

        assertEquals(StoryStatus.ACTIVE, story.getStatus());

        story.setStatus(StoryStatus.RESOLVED);
        assertEquals(StoryStatus.RESOLVED, story.getStatus());

        story.setStatus(StoryStatus.ARCHIVED);
        assertEquals(StoryStatus.ARCHIVED, story.getStatus());
    }

    @Test
    public void testStoryHopCountTracking() {
        Story story = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.LEGEND)
                .title("The Legend")
                .hopCount(0)
                .maxHops(6)
                .build();

        assertEquals(0, story.getHopCount());
        assertEquals(6, story.getMaxHops());

        story.setHopCount(3);
        assertEquals(3, story.getHopCount());
    }

    @Test
    public void testStoryAffectedRegions() {
        Story story = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.QUEST)
                .title("The Quest")
                .addAffectedRegion("region_1")
                .addAffectedRegion("region_2")
                .build();

        assertEquals(2, story.getAffectedRegions().size());
        assertTrue(story.getAffectedRegions().contains("region_1"));
        assertTrue(story.getAffectedRegions().contains("region_2"));

        story.addAffectedRegion("region_3");
        assertEquals(3, story.getAffectedRegions().size());
    }

    @Test
    public void testStoryMetadata() {
        Story story = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.MYSTERY)
                .title("The Mystery")
                .metadata("biome", "FOREST")
                .metadata("difficulty", 5)
                .build();

        assertEquals("FOREST", story.getMetadata().get("biome"));
        assertEquals(5, story.getMetadata().get("difficulty"));

        story.setMetadata("discovered", true);
        assertEquals(true, story.getMetadata().get("discovered"));
    }

    @Test
    public void testStoryLastProcessedTick() {
        Story story = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.TRAGEDY)
                .title("The Tragedy")
                .lastProcessedTick(0)
                .build();

        assertEquals(0, story.getLastProcessedTick());

        story.setLastProcessedTick(1000);
        assertEquals(1000, story.getLastProcessedTick());
    }

    @Test
    public void testStoryDefaults() {
        Story story = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.COMEDY)
                .title("The Comedy")
                .build();

        assertEquals(StoryStatus.ACTIVE, story.getStatus());
        assertEquals(0.9, story.getBaseProbability(), 0.001);
        assertEquals(0, story.getHopCount());
        assertEquals(6, story.getMaxHops());
        assertEquals(5, story.getPriority());
        assertEquals(0, story.getLastProcessedTick());
    }

    @Test
    public void testStorySchemaVersion() {
        Story story = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.PROPHECY)
                .title("The Prophecy")
                .build();

        assertEquals(1, story.getSchemaVersion());
        assertEquals("story/Story", story.getType());
    }

    @Test
    public void testStoryEquality() {
        Story story1 = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.LEGEND)
                .title("Legend 1")
                .build();

        Story story2 = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.RUMOR)
                .title("Legend 2")
                .build();

        Story story3 = new Story.Builder()
                .id("story_2")
                .storyType(StoryType.LEGEND)
                .title("Legend 1")
                .build();

        assertEquals(story1, story2); // Same ID
        assertNotEquals(story1, story3); // Different ID
        assertEquals(story1.hashCode(), story2.hashCode());
    }

    @Test
    public void testStoryToString() {
        Story story = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.QUEST)
                .title("The Quest")
                .priority(7)
                .hopCount(2)
                .maxHops(6)
                .build();

        String str = story.toString();
        assertTrue(str.contains("story_1"));
        assertTrue(str.contains("QUEST"));
        assertTrue(str.contains("priority=7"));
    }
}
