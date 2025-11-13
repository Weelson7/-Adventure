package org.adventure.story;

import org.adventure.world.Biome;

import java.util.*;

/**
 * StoryGenerator creates stories at worldgen with deterministic placement.
 * Stories are seeded based on world seed, biome types, and geographic distribution.
 */
public class StoryGenerator {
    private static final int DEFAULT_STORIES_PER_10K_TILES = 5;
    private static final Map<Biome, List<StoryType>> BIOME_STORY_AFFINITY = new HashMap<>();

    static {
        // Define which story types are more likely to spawn in which biomes
        BIOME_STORY_AFFINITY.put(Biome.OCEAN, Arrays.asList(StoryType.LEGEND, StoryType.MYSTERY));
        BIOME_STORY_AFFINITY.put(Biome.LAKE, Arrays.asList(StoryType.MYSTERY, StoryType.RUMOR));
        BIOME_STORY_AFFINITY.put(Biome.DESERT, Arrays.asList(StoryType.MYSTERY, StoryType.TRAGEDY));
        BIOME_STORY_AFFINITY.put(Biome.SAVANNA, Arrays.asList(StoryType.QUEST, StoryType.RUMOR));
        BIOME_STORY_AFFINITY.put(Biome.GRASSLAND, Arrays.asList(StoryType.COMEDY, StoryType.QUEST));
        BIOME_STORY_AFFINITY.put(Biome.FOREST, Arrays.asList(StoryType.QUEST, StoryType.MYSTERY));
        BIOME_STORY_AFFINITY.put(Biome.JUNGLE, Arrays.asList(StoryType.LEGEND, StoryType.PROPHECY));
        BIOME_STORY_AFFINITY.put(Biome.TUNDRA, Arrays.asList(StoryType.TRAGEDY, StoryType.LEGEND));
        BIOME_STORY_AFFINITY.put(Biome.TAIGA, Arrays.asList(StoryType.MYSTERY, StoryType.TRAGEDY));
        BIOME_STORY_AFFINITY.put(Biome.MOUNTAIN, Arrays.asList(StoryType.LEGEND, StoryType.PROPHECY));
        BIOME_STORY_AFFINITY.put(Biome.HILLS, Arrays.asList(StoryType.QUEST, StoryType.LEGEND));
        BIOME_STORY_AFFINITY.put(Biome.SWAMP, Arrays.asList(StoryType.TRAGEDY, StoryType.MYSTERY));
        BIOME_STORY_AFFINITY.put(Biome.VOLCANIC, Arrays.asList(StoryType.LEGEND, StoryType.PROPHECY));
        BIOME_STORY_AFFINITY.put(Biome.MAGICAL, Arrays.asList(StoryType.PROPHECY, StoryType.LEGEND));
    }

    private final long worldSeed;
    private final int worldWidth;
    private final int worldHeight;
    private final Random rng;

    /**
     * Create a new StoryGenerator with deterministic seeding.
     *
     * @param worldSeed World generation seed
     * @param worldWidth Width of the world in tiles
     * @param worldHeight Height of the world in tiles
     */
    public StoryGenerator(long worldSeed, int worldWidth, int worldHeight) {
        this.worldSeed = worldSeed;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.rng = new Random(worldSeed ^ 0xDEADBEEF); // XOR with constant for story-specific seed
    }

    /**
     * Generate stories for the world at worldgen time.
     *
     * @param biomes 2D array of biomes from worldgen
     * @return List of generated stories
     */
    public List<Story> generateStories(Biome[][] biomes) {
        if (biomes == null || biomes.length == 0 || biomes[0].length == 0) {
            throw new IllegalArgumentException("Biomes array cannot be null or empty");
        }

        int totalTiles = worldWidth * worldHeight;
        int targetStoryCount = (totalTiles / 10000) * DEFAULT_STORIES_PER_10K_TILES;
        // Ensure at least 1 story for small worlds
        targetStoryCount = Math.max(1, targetStoryCount);

        List<Story> stories = new ArrayList<>();
        Set<Integer> usedTileIds = new HashSet<>();

        for (int i = 0; i < targetStoryCount; i++) {
            Story story = generateSingleStory(biomes, usedTileIds, i);
            if (story != null) {
                stories.add(story);
            }
        }

        return stories;
    }

    /**
     * Generate a single story with deterministic placement.
     *
     * @param biomes 2D biome array
     * @param usedTileIds Set of tile IDs already used for stories (to avoid clustering)
     * @param storyIndex Index of the story being generated (for deterministic ID)
     * @return Generated story or null if placement failed
     */
    private Story generateSingleStory(Biome[][] biomes, Set<Integer> usedTileIds, int storyIndex) {
        // Try up to 10 times to find a valid location
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = rng.nextInt(worldWidth);
            int y = rng.nextInt(worldHeight);
            int tileId = y * worldWidth + x;

            // Skip if already used
            if (usedTileIds.contains(tileId)) {
                continue;
            }

            Biome biome = biomes[y][x];
            
            // Skip water biomes for most stories (except legends and mysteries)
            if (biome.isWater()) {
                double waterChance = rng.nextDouble();
                if (waterChance > 0.3) { // 30% chance for water stories
                    continue;
                }
            }

            // Select story type based on biome affinity
            StoryType storyType = selectStoryType(biome);

            // Create the story
            String storyId = String.format("story_%d_%d", worldSeed, storyIndex);
            Story story = new Story.Builder()
                    .id(storyId)
                    .storyType(storyType)
                    .title(generateTitle(storyType, biome, storyIndex))
                    .description(generateDescription(storyType, biome))
                    .originTileId(tileId)
                    .originTick(0) // Stories are created at worldgen (tick 0)
                    .baseProbability(calculateBaseProbability(storyType))
                    .maxHops(calculateMaxHops(storyType))
                    .priority(calculatePriority(storyType))
                    .metadata("biome", biome.name())
                    .metadata("x", x)
                    .metadata("y", y)
                    .build();

            usedTileIds.add(tileId);
            return story;
        }

        // Failed to find a valid location after 10 attempts
        return null;
    }

    /**
     * Select a story type based on biome affinity.
     */
    private StoryType selectStoryType(Biome biome) {
        List<StoryType> affinityList = BIOME_STORY_AFFINITY.getOrDefault(
                biome, 
                Arrays.asList(StoryType.RUMOR, StoryType.QUEST) // Default fallback
        );
        
        int index = rng.nextInt(affinityList.size());
        return affinityList.get(index);
    }

    /**
     * Generate a title for a story based on type and biome.
     */
    private String generateTitle(StoryType storyType, Biome biome, int index) {
        String biomeName = biome.name().toLowerCase().replace('_', ' ');
        
        switch (storyType) {
            case LEGEND:
                return String.format("The Legend of the %s Guardian", capitalize(biomeName));
            case RUMOR:
                return String.format("Whispers from the %s", capitalize(biomeName));
            case QUEST:
                return String.format("Quest in the %s", capitalize(biomeName));
            case PROPHECY:
                return String.format("The %s Prophecy", capitalize(biomeName));
            case TRAGEDY:
                return String.format("Tragedy of the %s", capitalize(biomeName));
            case COMEDY:
                return String.format("Tales of the %s", capitalize(biomeName));
            case MYSTERY:
                return String.format("Mystery of the %s", capitalize(biomeName));
            default:
                return String.format("Story %d", index);
        }
    }

    /**
     * Generate a description for a story.
     */
    private String generateDescription(StoryType storyType, Biome biome) {
        String biomeName = biome.name().toLowerCase().replace('_', ' ');
        
        switch (storyType) {
            case LEGEND:
                return String.format("An ancient legend speaks of a powerful guardian that once protected the %s.", biomeName);
            case RUMOR:
                return String.format("Strange rumors have been circulating about events in the %s.", biomeName);
            case QUEST:
                return String.format("A quest awaits those brave enough to venture into the %s.", biomeName);
            case PROPHECY:
                return String.format("A prophecy foretells great changes coming to the %s.", biomeName);
            case TRAGEDY:
                return String.format("A tragic event has scarred the %s forever.", biomeName);
            case COMEDY:
                return String.format("Amusing tales are told by travelers passing through the %s.", biomeName);
            case MYSTERY:
                return String.format("An unsolved mystery lurks in the depths of the %s.", biomeName);
            default:
                return "A story unfolds...";
        }
    }

    /**
     * Calculate base probability based on story type.
     */
    private double calculateBaseProbability(StoryType storyType) {
        switch (storyType) {
            case LEGEND:
                return 0.95; // Legends spread widely
            case PROPHECY:
                return 0.90;
            case QUEST:
                return 0.85;
            case MYSTERY:
                return 0.80;
            case RUMOR:
                return 0.75; // Rumors spread fast but with less certainty
            case TRAGEDY:
                return 0.70;
            case COMEDY:
                return 0.65;
            default:
                return 0.90; // Default from specs
        }
    }

    /**
     * Calculate max hops based on story type.
     */
    private int calculateMaxHops(StoryType storyType) {
        switch (storyType) {
            case LEGEND:
                return 8; // Legends travel far
            case PROPHECY:
                return 7;
            case RUMOR:
                return 6; // Default from specs
            case QUEST:
                return 5;
            case MYSTERY:
                return 5;
            case TRAGEDY:
                return 4;
            case COMEDY:
                return 3; // Local amusement
            default:
                return 6; // Default from specs
        }
    }

    /**
     * Calculate priority based on story type.
     */
    private int calculatePriority(StoryType storyType) {
        switch (storyType) {
            case LEGEND:
                return 9; // High priority
            case PROPHECY:
                return 8;
            case QUEST:
                return 6;
            case MYSTERY:
                return 6;
            case TRAGEDY:
                return 5;
            case RUMOR:
                return 4;
            case COMEDY:
                return 2; // Low priority
            default:
                return 5; // Medium default
        }
    }

    /**
     * Capitalize first letter of a string.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
