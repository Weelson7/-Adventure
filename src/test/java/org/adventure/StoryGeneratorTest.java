package org.adventure;

import org.adventure.story.*;
import org.adventure.world.Biome;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StoryGenerator class with emphasis on determinism.
 */
public class StoryGeneratorTest {

    @Test
    public void testGenerateStoriesDeterministic() {
        long seed = 12345L;
        int width = 100;
        int height = 100;
        Biome[][] biomes = createTestBiomes(width, height);

        StoryGenerator gen1 = new StoryGenerator(seed, width, height);
        List<Story> stories1 = gen1.generateStories(biomes);

        StoryGenerator gen2 = new StoryGenerator(seed, width, height);
        List<Story> stories2 = gen2.generateStories(biomes);

        // Same seed should produce identical stories
        assertEquals(stories1.size(), stories2.size());
        
        for (int i = 0; i < stories1.size(); i++) {
            Story s1 = stories1.get(i);
            Story s2 = stories2.get(i);
            
            assertEquals(s1.getId(), s2.getId());
            assertEquals(s1.getStoryType(), s2.getStoryType());
            assertEquals(s1.getTitle(), s2.getTitle());
            assertEquals(s1.getOriginTileId(), s2.getOriginTileId());
            assertEquals(s1.getBaseProbability(), s2.getBaseProbability(), 0.0001);
            assertEquals(s1.getMaxHops(), s2.getMaxHops());
            assertEquals(s1.getPriority(), s2.getPriority());
        }
    }

    @Test
    public void testGenerateStoriesDifferentSeeds() {
        int width = 100;
        int height = 100;
        Biome[][] biomes = createTestBiomes(width, height);

        StoryGenerator gen1 = new StoryGenerator(12345L, width, height);
        List<Story> stories1 = gen1.generateStories(biomes);

        StoryGenerator gen2 = new StoryGenerator(67890L, width, height);
        List<Story> stories2 = gen2.generateStories(biomes);

        // Different seeds should produce different story placements
        boolean anyDifferent = false;
        for (int i = 0; i < Math.min(stories1.size(), stories2.size()); i++) {
            if (stories1.get(i).getOriginTileId() != stories2.get(i).getOriginTileId()) {
                anyDifferent = true;
                break;
            }
        }
        assertTrue(anyDifferent, "Different seeds should produce different story placements");
    }

    @Test
    public void testGenerateStoriesScalesWithWorldSize() {
        long seed = 12345L;

        // Small world
        int smallWidth = 50;
        int smallHeight = 50;
        Biome[][] smallBiomes = createTestBiomes(smallWidth, smallHeight);
        StoryGenerator smallGen = new StoryGenerator(seed, smallWidth, smallHeight);
        List<Story> smallStories = smallGen.generateStories(smallBiomes);

        // Large world
        int largeWidth = 200;
        int largeHeight = 200;
        Biome[][] largeBiomes = createTestBiomes(largeWidth, largeHeight);
        StoryGenerator largeGen = new StoryGenerator(seed, largeWidth, largeHeight);
        List<Story> largeStories = largeGen.generateStories(largeBiomes);

        // Large world should have more stories
        assertTrue(largeStories.size() > smallStories.size(),
                "Larger world should have more stories");
    }

    @Test
    public void testGenerateStoriesMinimumOneStory() {
        long seed = 12345L;
        int width = 10;
        int height = 10;
        Biome[][] biomes = createTestBiomes(width, height);

        StoryGenerator gen = new StoryGenerator(seed, width, height);
        List<Story> stories = gen.generateStories(biomes);

        // Even tiny worlds should have at least 1 story
        assertTrue(stories.size() >= 1, "Should have at least 1 story");
    }

    @Test
    public void testGenerateStoriesNoDuplicateTileIds() {
        long seed = 12345L;
        int width = 100;
        int height = 100;
        Biome[][] biomes = createTestBiomes(width, height);

        StoryGenerator gen = new StoryGenerator(seed, width, height);
        List<Story> stories = gen.generateStories(biomes);

        Set<Integer> tileIds = stories.stream()
                .map(Story::getOriginTileId)
                .collect(Collectors.toSet());

        // No two stories should spawn at the same tile
        assertEquals(stories.size(), tileIds.size(), "Stories should have unique tile IDs");
    }

    @Test
    public void testGenerateStoriesBiomeAffinity() {
        long seed = 12345L;
        int width = 100;
        int height = 100;
        Biome[][] biomes = createBiomeGrid(width, height, Biome.MOUNTAIN);

        StoryGenerator gen = new StoryGenerator(seed, width, height);
        List<Story> stories = gen.generateStories(biomes);

        // Mountain biomes should spawn LEGEND or PROPHECY stories
        for (Story story : stories) {
            StoryType type = story.getStoryType();
            assertTrue(type == StoryType.LEGEND || type == StoryType.PROPHECY,
                    "Mountain biome should spawn LEGEND or PROPHECY stories, got: " + type);
        }
    }

    @Test
    public void testGenerateStoriesOriginTickZero() {
        long seed = 12345L;
        int width = 100;
        int height = 100;
        Biome[][] biomes = createTestBiomes(width, height);

        StoryGenerator gen = new StoryGenerator(seed, width, height);
        List<Story> stories = gen.generateStories(biomes);

        // All worldgen stories should have originTick = 0
        for (Story story : stories) {
            assertEquals(0, story.getOriginTick(), "Worldgen stories should have originTick = 0");
        }
    }

    @Test
    public void testGenerateStoriesActiveStatus() {
        long seed = 12345L;
        int width = 100;
        int height = 100;
        Biome[][] biomes = createTestBiomes(width, height);

        StoryGenerator gen = new StoryGenerator(seed, width, height);
        List<Story> stories = gen.generateStories(biomes);

        // All newly generated stories should be ACTIVE
        for (Story story : stories) {
            assertEquals(StoryStatus.ACTIVE, story.getStatus());
        }
    }

    @Test
    public void testGenerateStoriesHasMetadata() {
        long seed = 12345L;
        int width = 100;
        int height = 100;
        Biome[][] biomes = createTestBiomes(width, height);

        StoryGenerator gen = new StoryGenerator(seed, width, height);
        List<Story> stories = gen.generateStories(biomes);

        // Stories should have metadata (biome, x, y)
        for (Story story : stories) {
            assertTrue(story.getMetadata().containsKey("biome"));
            assertTrue(story.getMetadata().containsKey("x"));
            assertTrue(story.getMetadata().containsKey("y"));
        }
    }

    @Test
    public void testGenerateStoriesValidBaseProbability() {
        long seed = 12345L;
        int width = 100;
        int height = 100;
        Biome[][] biomes = createTestBiomes(width, height);

        StoryGenerator gen = new StoryGenerator(seed, width, height);
        List<Story> stories = gen.generateStories(biomes);

        // All stories should have valid base probability [0.0, 1.0]
        for (Story story : stories) {
            assertTrue(story.getBaseProbability() >= 0.0 && story.getBaseProbability() <= 1.0);
        }
    }

    @Test
    public void testGenerateStoriesValidPriority() {
        long seed = 12345L;
        int width = 100;
        int height = 100;
        Biome[][] biomes = createTestBiomes(width, height);

        StoryGenerator gen = new StoryGenerator(seed, width, height);
        List<Story> stories = gen.generateStories(biomes);

        // All stories should have valid priority [0, 10]
        for (Story story : stories) {
            assertTrue(story.getPriority() >= 0 && story.getPriority() <= 10);
        }
    }

    @Test
    public void testGenerateStoriesValidMaxHops() {
        long seed = 12345L;
        int width = 100;
        int height = 100;
        Biome[][] biomes = createTestBiomes(width, height);

        StoryGenerator gen = new StoryGenerator(seed, width, height);
        List<Story> stories = gen.generateStories(biomes);

        // All stories should have positive maxHops
        for (Story story : stories) {
            assertTrue(story.getMaxHops() > 0);
        }
    }

    @Test
    public void testGenerateStoriesInvalidBiomesThrows() {
        long seed = 12345L;
        int width = 100;
        int height = 100;

        StoryGenerator gen = new StoryGenerator(seed, width, height);

        assertThrows(IllegalArgumentException.class, () -> {
            gen.generateStories(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            gen.generateStories(new Biome[0][0]);
        });
    }

    @Test
    public void testGenerateStoriesLegendHasHighPriority() {
        long seed = 12345L;
        int width = 100;
        int height = 100;
        Biome[][] biomes = createBiomeGrid(width, height, Biome.MOUNTAIN);

        StoryGenerator gen = new StoryGenerator(seed, width, height);
        List<Story> stories = gen.generateStories(biomes);

        // Legends should have high priority (8-9)
        for (Story story : stories) {
            if (story.getStoryType() == StoryType.LEGEND) {
                assertTrue(story.getPriority() >= 8, "Legends should have priority >= 8");
            }
        }
    }

    @Test
    public void testGenerateStoriesComedyHasLowPriority() {
        long seed = 12345L;
        int width = 100;
        int height = 100;
        Biome[][] biomes = createBiomeGrid(width, height, Biome.GRASSLAND);

        StoryGenerator gen = new StoryGenerator(seed, width, height);
        List<Story> stories = gen.generateStories(biomes);

        // Comedies should have low priority (2-3)
        for (Story story : stories) {
            if (story.getStoryType() == StoryType.COMEDY) {
                assertTrue(story.getPriority() <= 3, "Comedies should have priority <= 3");
            }
        }
    }

    // Helper methods

    private Biome[][] createTestBiomes(int width, int height) {
        Biome[][] biomes = new Biome[height][width];
        Biome[] biomeTypes = Biome.values();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Create a varied biome distribution
                int index = (x + y * width) % biomeTypes.length;
                biomes[y][x] = biomeTypes[index];
            }
        }
        
        return biomes;
    }

    private Biome[][] createBiomeGrid(int width, int height, Biome biome) {
        Biome[][] biomes = new Biome[height][width];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                biomes[y][x] = biome;
            }
        }
        
        return biomes;
    }
}
