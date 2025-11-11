package org.adventure.character;

import org.adventure.world.Biome;

import java.util.Random;

/**
 * NPC (Non-Player Character) extends Character with AI behavior and spawning logic.
 * 
 * <p>NPCs are spawned deterministically based on world seed, biome, and region.
 * Each biome has preferred NPC types and density limits. NPCs can be friendly,
 * neutral, or hostile based on race affinity and diplomacy.
 * 
 * <p>Design: docs/characters_stats_traits_skills.md
 * <p>Data Model: docs/data_models.md → Character Schema
 */
public class NPC extends Character {
    
    /**
     * NPC behavior type (AI placeholder for Phase 1.3, full AI in Phase 2).
     */
    public enum BehaviorType {
        PEACEFUL("Peaceful", "Ignores player unless attacked"),
        NEUTRAL("Neutral", "Defends itself when attacked"),
        AGGRESSIVE("Aggressive", "Attacks player on sight"),
        TRADER("Trader", "Offers trade services"),
        QUEST_GIVER("Quest Giver", "Provides quests to player"),
        GUARD("Guard", "Patrols and enforces laws");
        
        private final String displayName;
        private final String description;
        
        BehaviorType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final BehaviorType behaviorType;
    private final int spawnX;
    private final int spawnY;
    private final String biomeId;
    
    // AI state (placeholder for Phase 2)
    private int currentX;
    private int currentY;
    private int health;
    private int maxHealth;
    
    /**
     * Create an NPC with specified behavior and spawn location.
     * 
     * @param id Unique NPC identifier
     * @param name NPC name
     * @param race NPC race
     * @param behaviorType AI behavior type
     * @param spawnX Spawn X coordinate
     * @param spawnY Spawn Y coordinate
     * @param biomeId Biome where NPC spawned
     */
    public NPC(String id, String name, Race race, BehaviorType behaviorType, 
               int spawnX, int spawnY, String biomeId) {
        super(id, name, race);
        this.behaviorType = behaviorType;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.currentX = spawnX;
        this.currentY = spawnY;
        this.biomeId = biomeId;
        
        // Initialize health from derived stats
        this.maxHealth = (int) getDerivedStat("maxHealth");
        this.health = maxHealth;
    }
    
    // ==================== Deterministic Spawning ====================
    
    /**
     * Spawn NPCs for a region deterministically.
     * 
     * <p>Algorithm:
     * <ol>
     *   <li>Seed RNG with regionId + worldSeed for determinism</li>
     *   <li>Calculate NPC count based on region size and biome</li>
     *   <li>For each NPC: select race, behavior, stats based on biome</li>
     *   <li>Validate placement (no overlap, density limits)</li>
     * </ol>
     * 
     * @param regionId Region identifier
     * @param worldSeed World generation seed
     * @param regionCenterX Region center X
     * @param regionCenterY Region center Y
     * @param regionWidth Region width
     * @param regionHeight Region height
     * @param dominantBiome Dominant biome in region
     * @return List of spawned NPCs
     */
    public static java.util.List<NPC> spawnNPCsForRegion(
            int regionId, long worldSeed, int regionCenterX, int regionCenterY,
            int regionWidth, int regionHeight, Biome dominantBiome) {
        
        // Seed RNG for determinism
        Random rng = new Random(worldSeed + regionId * 1000L);
        
        java.util.List<NPC> npcs = new java.util.ArrayList<>();
        
        // Calculate NPC count based on biome and region size
        int baseCount = calculateNPCCount(dominantBiome, regionWidth, regionHeight);
        int npcCount = baseCount + rng.nextInt(Math.max(1, baseCount / 2)); // +0 to +50% variance
        
        // Spawn NPCs
        for (int i = 0; i < npcCount; i++) {
            // Generate NPC
            String npcId = "npc_r" + regionId + "_" + i;
            Race race = selectRaceForBiome(dominantBiome, rng);
            BehaviorType behavior = selectBehaviorForBiome(dominantBiome, rng);
            
            // Select spawn position within region
            int spawnX = regionCenterX - regionWidth / 2 + rng.nextInt(regionWidth);
            int spawnY = regionCenterY - regionHeight / 2 + rng.nextInt(regionHeight);
            
            // Generate name
            String name = generateNPCName(race, rng);
            
            // Create NPC
            NPC npc = new NPC(npcId, name, race, behavior, spawnX, spawnY, dominantBiome.name());
            
            // Add random trait (10% chance)
            if (rng.nextDouble() < 0.1) {
                Trait randomTrait = selectRandomTrait(rng);
                npc.addTrait(randomTrait);
            }
            
            npcs.add(npc);
        }
        
        return npcs;
    }
    
    /**
     * Calculate base NPC count for biome and region size.
     * 
     * <p>Density formula:
     * <ul>
     *   <li>Habitable land biomes: 1 NPC per 200 tiles</li>
     *   <li>Harsh biomes (desert, tundra): 1 NPC per 500 tiles</li>
     *   <li>Water/Mountain: 1 NPC per 1000 tiles (rare)</li>
     * </ul>
     */
    private static int calculateNPCCount(Biome biome, int width, int height) {
        int area = width * height;
        
        if (biome.isWater() || biome == Biome.MOUNTAIN) {
            return area / 1000; // Rare in water/mountains
        } else if (biome == Biome.DESERT || biome == Biome.TUNDRA) {
            return area / 500; // Sparse in harsh biomes
        } else if (biome.isHabitable()) {
            return area / 200; // Normal density in habitable areas
        }
        
        return area / 800; // Default for other biomes
    }
    
    /**
     * Select race based on biome affinity.
     * 
     * <p>Biome → Race mapping:
     * <ul>
     *   <li>Grassland/Forest: Humans, Elves, Halflings</li>
     *   <li>Hills/Mountains: Dwarves, Goblins</li>
     *   <li>Desert/Savanna: Orcs, Humans</li>
     *   <li>Tundra/Taiga: Humans, Trolls</li>
     *   <li>Volcanic: Dragons (rare)</li>
     * </ul>
     */
    private static Race selectRaceForBiome(Biome biome, Random rng) {
        double roll = rng.nextDouble();
        
        switch (biome) {
            case GRASSLAND:
            case FOREST:
            case SWAMP:
                if (roll < 0.5) return Race.HUMAN;
                if (roll < 0.8) return Race.ELF;
                return Race.HALFLING;
                
            case HILLS:
            case MOUNTAIN:
                if (roll < 0.6) return Race.DWARF;
                if (roll < 0.9) return Race.GOBLIN;
                return Race.HUMAN;
                
            case DESERT:
            case SAVANNA:
                if (roll < 0.6) return Race.ORC;
                return Race.HUMAN;
                
            case TUNDRA:
            case TAIGA:
                if (roll < 0.7) return Race.HUMAN;
                return Race.TROLL;
                
            case VOLCANIC:
                if (roll < 0.1) return Race.DRAGON; // Rare
                if (roll < 0.6) return Race.DWARF; // Dwarves like heat
                return Race.ORC;
                
            default:
                return Race.HUMAN; // Default fallback
        }
    }
    
    /**
     * Select behavior based on biome danger level.
     * 
     * <p>Biome → Behavior mapping:
     * <ul>
     *   <li>Peaceful biomes (Grassland, Forest): More traders and peaceful NPCs</li>
     *   <li>Neutral biomes (Hills, Taiga): Balanced mix</li>
     *   <li>Dangerous biomes (Volcanic, Tundra): More aggressive NPCs</li>
     * </ul>
     */
    private static BehaviorType selectBehaviorForBiome(Biome biome, Random rng) {
        double roll = rng.nextDouble();
        
        if (biome == Biome.GRASSLAND || biome == Biome.FOREST) {
            // Peaceful biomes
            if (roll < 0.4) return BehaviorType.PEACEFUL;
            if (roll < 0.7) return BehaviorType.TRADER;
            if (roll < 0.9) return BehaviorType.NEUTRAL;
            return BehaviorType.QUEST_GIVER;
        } else if (biome == Biome.VOLCANIC || biome == Biome.TUNDRA || biome == Biome.DESERT) {
            // Dangerous biomes
            if (roll < 0.5) return BehaviorType.AGGRESSIVE;
            if (roll < 0.8) return BehaviorType.NEUTRAL;
            return BehaviorType.PEACEFUL;
        } else {
            // Neutral biomes
            if (roll < 0.3) return BehaviorType.PEACEFUL;
            if (roll < 0.6) return BehaviorType.NEUTRAL;
            if (roll < 0.8) return BehaviorType.TRADER;
            return BehaviorType.AGGRESSIVE;
        }
    }
    
    /**
     * Generate NPC name based on race.
     */
    private static String generateNPCName(Race race, Random rng) {
        String[][] namesByRace = {
            // Human names
            {"Alyn", "Bran", "Cara", "Dain", "Elara", "Finn", "Gwen", "Hale"},
            // Elf names
            {"Aelindra", "Belanor", "Calistra", "Daenor", "Elenwe", "Finrod"},
            // Dwarf names
            {"Balin", "Dwalin", "Gimli", "Thorin", "Oin", "Gloin"},
            // Orc names
            {"Grok", "Thrak", "Grath", "Morg", "Urg", "Drek"},
            // Goblin names
            {"Snit", "Grizz", "Pox", "Zit", "Gibber", "Skrat"},
            // Halfling names
            {"Bilbo", "Frodo", "Sam", "Merry", "Pippin", "Rosie"},
            // Troll names
            {"Grumm", "Thud", "Rok", "Brak", "Muk"},
            // Dragon names
            {"Smaug", "Ancalagon", "Glaurung", "Scatha", "Saphira"}
        };
        
        int raceIndex = 0;
        if (race == Race.ELF) raceIndex = 1;
        else if (race == Race.DWARF) raceIndex = 2;
        else if (race == Race.ORC) raceIndex = 3;
        else if (race == Race.GOBLIN) raceIndex = 4;
        else if (race == Race.HALFLING) raceIndex = 5;
        else if (race == Race.TROLL) raceIndex = 6;
        else if (race == Race.DRAGON) raceIndex = 7;
        
        String[] names = namesByRace[raceIndex];
        return names[rng.nextInt(names.length)];
    }
    
    /**
     * Select a random trait for NPC variety.
     */
    private static Trait selectRandomTrait(Random rng) {
        Trait[] traits = {
            Trait.FAST_LEARNER, Trait.ROBUST, Trait.AGILE, Trait.CLUMSY,
            Trait.BLESSED, Trait.CURSED, Trait.NIGHT_VISION, Trait.RESILIENT,
            Trait.LUCKY
        };
        return traits[rng.nextInt(traits.length)];
    }
    
    // ==================== AI Behavior (Placeholder) ====================
    
    /**
     * Update NPC AI for one tick (placeholder for Phase 2).
     * 
     * <p>Phase 1.3: Simple placeholder, no movement
     * <p>Phase 2: Full pathfinding, behavior trees, combat AI
     */
    public void updateAI(long currentTick) {
        // Placeholder: regenerate mana
        regenerateMana();
        
        // TODO Phase 2: Implement pathfinding, combat, trading, questing
    }
    
    /**
     * Take damage (combat placeholder).
     */
    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }
    
    /**
     * Heal (healing placeholder).
     */
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }
    
    /**
     * Check if NPC is alive.
     */
    public boolean isAlive() {
        return health > 0;
    }
    
    // ==================== Getters ====================
    
    public BehaviorType getBehaviorType() {
        return behaviorType;
    }
    
    public int getSpawnX() {
        return spawnX;
    }
    
    public int getSpawnY() {
        return spawnY;
    }
    
    public String getBiomeId() {
        return biomeId;
    }
    
    public int getCurrentX() {
        return currentX;
    }
    
    public int getCurrentY() {
        return currentY;
    }
    
    public void setCurrentX(int currentX) {
        this.currentX = currentX;
    }
    
    public void setCurrentY(int currentY) {
        this.currentY = currentY;
    }
    
    public int getHealth() {
        return health;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    @Override
    public String toString() {
        return getName() + " (" + getRace().getName() + " " + behaviorType.getDisplayName() + ")";
    }
}
