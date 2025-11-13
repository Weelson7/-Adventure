package org.adventure.character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Character data model representing both player characters and NPCs.
 * 
 * <p>Characters have core stats with soft-cap progression, traits that modify
 * behavior, skills that can be learned and improved, and inventory for items.
 * 
 * <p><b>Core Stats:</b> STR, DEX, INT, WIS, CON, CHA, PER, LUCK
 * <p><b>Soft-Cap Formula:</b> newStat = currentStat + baseGain / (1 + (currentStat / softCapThreshold)^2)
 * <p><b>Hard Cap:</b> 200 (absolute maximum to prevent overflow)
 * 
 * <p>Design: docs/characters_stats_traits_skills.md
 * <p>Data Model: docs/data_models.md → Character Schema
 */
public class Character {
    
    // Core stats enumeration
    public enum CoreStat {
        STRENGTH("STR", "Physical power, melee damage"),
        DEXTERITY("DEX", "Agility, ranged accuracy"),
        INTELLIGENCE("INT", "Magic power, spell casting"),
        WISDOM("WIS", "Perception, magic resistance"),
        CONSTITUTION("CON", "Health, stamina, poison resistance"),
        CHARISMA("CHA", "Social influence, leadership"),
        PERCEPTION("PER", "Awareness, detection, initiative"),
        LUCK("LUCK", "Critical hits, rare drops, event outcomes");
        
        private final String abbreviation;
        private final String description;
        
        CoreStat(String abbreviation, String description) {
            this.abbreviation = abbreviation;
            this.description = description;
        }
        
        public String getAbbreviation() {
            return abbreviation;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Constants for stat progression
    public static final int DEFAULT_SOFT_CAP_THRESHOLD = 50;
    public static final int LEGENDARY_SOFT_CAP_THRESHOLD = 100;
    public static final int HARD_CAP = 200;
    public static final int BASE_MANA = 10;
    public static final int MANA_PER_STAT_POINT = 2;
    
    // Identity and basic properties
    private final String id;
    private String name;
    private Race race;
    
    // Core stats (stored as map for flexibility)
    private final Map<CoreStat, Integer> stats;
    
    // Derived stats (computed from core stats + traits)
    private final Map<String, Double> derivedStats;
    
    // Mana system (for magic users)
    private int currentMana;
    private int maxMana;
    
    // Traits and skills
    private final List<Trait> traits;
    private final List<Skill> skills;
    
    // Inventory (item IDs for now, full Item integration in Phase 1.4)
    private final List<String> inventoryItemIds;
    
    // Optional: society/clan membership
    private String societyId;
    
    // Simulation tracking
    private long lastUpdatedTick;
    
    // Schema versioning for persistence
    private int schemaVersion = 1;
    
    /**
     * Create a new character with base stats from race.
     * 
     * @param id Unique character identifier
     * @param name Character name
     * @param race Race/species (determines base stats)
     */
    public Character(String id, String name, Race race) {
        this.id = id;
        this.name = name;
        this.race = race;
        
        // Initialize stats from race base stats
        this.stats = new HashMap<>();
        for (CoreStat stat : CoreStat.values()) {
            this.stats.put(stat, race.getBaseStat(stat));
        }
        
        // Initialize derived stats
        this.derivedStats = new HashMap<>();
        updateDerivedStats();
        
        // Initialize mana
        this.maxMana = calculateMaxMana();
        this.currentMana = maxMana;
        
        // Initialize collections
        this.traits = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.inventoryItemIds = new ArrayList<>();
        
        this.lastUpdatedTick = 0;
    }
    
    // ==================== Stat Progression ====================
    
    /**
     * Apply stat progression with soft-cap formula.
     * 
     * <p>Formula: newStat = currentStat + baseGain / (1 + (currentStat / softCapThreshold)^2)
     * 
     * @param stat Core stat to increase
     * @param baseGain Raw gain amount (from XP, training, etc.)
     * @return Actual stat increase (after soft-cap)
     */
    public double addStatProgress(CoreStat stat, double baseGain) {
        int currentValue = stats.get(stat);
        
        // Check hard cap
        if (currentValue >= HARD_CAP) {
            return 0.0; // No more progression at hard cap
        }
        
        // Apply soft-cap formula
        int softCapThreshold = getSoftCapThreshold();
        double ratio = (double) currentValue / softCapThreshold;
        double actualGain = baseGain / (1.0 + ratio * ratio);
        
        // Apply trait modifiers (e.g., Fast Learner)
        actualGain *= getStatProgressionMultiplier(stat);
        
        // Update stat (round down, track fractional progress separately if needed)
        int newValue = Math.min(HARD_CAP, currentValue + (int) Math.round(actualGain));
        stats.put(stat, newValue);
        
        // Update derived stats that depend on this stat
        updateDerivedStats();
        
        return actualGain;
    }
    
    /**
     * Get soft-cap threshold for this character.
     * Base: 50, Legendary: 100 (can be modified by traits).
     */
    private int getSoftCapThreshold() {
        // Base threshold
        int threshold = DEFAULT_SOFT_CAP_THRESHOLD;
        
        // Traits can modify (e.g., "Legendary Potential" increases to 100)
        for (Trait trait : traits) {
            threshold += trait.getSoftCapThresholdBonus();
        }
        
        return Math.max(threshold, DEFAULT_SOFT_CAP_THRESHOLD);
    }
    
    /**
     * Get stat progression multiplier from traits.
     * Base: 1.0, can be modified by traits like Fast Learner (1.2x).
     */
    private double getStatProgressionMultiplier(CoreStat stat) {
        double multiplier = 1.0;
        
        for (Trait trait : traits) {
            multiplier *= trait.getStatProgressionMultiplier(stat);
        }
        
        return multiplier;
    }
    
    // ==================== Derived Stats ====================
    
    /**
     * Update all derived stats based on core stats and traits.
     */
    private void updateDerivedStats() {
        // Max Mana = baseMana + (Intelligence * manaPerStat)
        int intelligence = stats.get(CoreStat.INTELLIGENCE);
        maxMana = calculateMaxMana();
        derivedStats.put("maxMana", (double) maxMana);
        
        // Mana Regen = 1 + floor(Intelligence / 10)
        double manaRegen = 1 + Math.floor(intelligence / 10.0);
        derivedStats.put("manaRegen", manaRegen);
        
        // Max Health = 50 + (Constitution * 5)
        int constitution = stats.get(CoreStat.CONSTITUTION);
        double maxHealth = 50 + constitution * 5;
        derivedStats.put("maxHealth", maxHealth);
        
        // Melee Damage Bonus = Strength / 2
        int strength = stats.get(CoreStat.STRENGTH);
        double meleeDamageBonus = strength / 2.0;
        derivedStats.put("meleeDamageBonus", meleeDamageBonus);
        
        // Ranged Accuracy = Dexterity + Perception / 2
        int dexterity = stats.get(CoreStat.DEXTERITY);
        int perception = stats.get(CoreStat.PERCEPTION);
        double rangedAccuracy = dexterity + perception / 2.0;
        derivedStats.put("rangedAccuracy", rangedAccuracy);
        
        // Initiative = Dexterity + Perception
        double initiative = dexterity + perception;
        derivedStats.put("initiative", initiative);
        
        // Social Influence = Charisma * 2
        int charisma = stats.get(CoreStat.CHARISMA);
        double socialInfluence = charisma * 2;
        derivedStats.put("socialInfluence", socialInfluence);
        
        // Critical Chance = Luck / 100 (as percentage)
        int luck = stats.get(CoreStat.LUCK);
        double critChance = luck / 100.0;
        derivedStats.put("critChance", critChance);
    }
    
    /**
     * Calculate max mana from Intelligence.
     */
    private int calculateMaxMana() {
        int intelligence = stats.get(CoreStat.INTELLIGENCE);
        return BASE_MANA + intelligence * MANA_PER_STAT_POINT;
    }
    
    // ==================== Trait Management ====================
    
    /**
     * Add a trait to this character.
     * Traits are permanent and cannot be removed (except through special events).
     */
    public void addTrait(Trait trait) {
        if (!traits.contains(trait)) {
            traits.add(trait);
            updateDerivedStats(); // Traits affect derived stats
        }
    }
    
    /**
     * Check if character has a specific trait.
     */
    public boolean hasTrait(Trait trait) {
        return traits.contains(trait);
    }
    
    /**
     * Check if character has a trait by ID.
     */
    public boolean hasTrait(String traitId) {
        return traits.stream().anyMatch(t -> t.getId().equals(traitId));
    }
    
    // ==================== Skill Management ====================
    
    /**
     * Add a skill to this character.
     */
    public void addSkill(Skill skill) {
        if (!skills.contains(skill)) {
            skills.add(skill);
        }
    }
    
    /**
     * Get skill by ID.
     */
    public Skill getSkill(String skillId) {
        return skills.stream()
                .filter(s -> s.getId().equals(skillId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Check if character has a specific skill.
     */
    public boolean hasSkill(String skillId) {
        return getSkill(skillId) != null;
    }
    
    /**
     * Add skill XP and handle level-ups.
     */
    public void addSkillXP(String skillId, int xp) {
        Skill skill = getSkill(skillId);
        if (skill != null) {
            // Apply trait modifiers to XP gain
            double xpMultiplier = getSkillXPMultiplier();
            int actualXP = (int) Math.round(xp * xpMultiplier);
            
            skill.addXP(actualXP);
        }
    }
    
    /**
     * Get skill XP multiplier from traits.
     * Base: 1.0, can be modified by traits like Fast Learner.
     */
    private double getSkillXPMultiplier() {
        double multiplier = 1.0;
        
        for (Trait trait : traits) {
            multiplier *= trait.getSkillXPMultiplier();
        }
        
        return multiplier;
    }
    
    // ==================== Mana Management ====================
    
    /**
     * Spend mana for spell casting.
     * 
     * @param amount Mana to spend
     * @return true if mana was available and spent, false otherwise
     */
    public boolean spendMana(int amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Regenerate mana (called each tick).
     */
    public void regenerateMana() {
        double regenAmount = derivedStats.getOrDefault("manaRegen", 1.0);
        currentMana = Math.min(maxMana, currentMana + (int) regenAmount);
    }
    
    /**
     * Restore mana (from consumables, rest, etc.).
     */
    public void restoreMana(int amount) {
        currentMana = Math.min(maxMana, currentMana + amount);
    }
    
    // ==================== Inventory Management ====================
    
    /**
     * Add item to inventory (by ID for now, full Item integration in Phase 1.4).
     */
    public void addItem(String itemId) {
        inventoryItemIds.add(itemId);
    }
    
    /**
     * Remove item from inventory.
     */
    public boolean removeItem(String itemId) {
        return inventoryItemIds.remove(itemId);
    }
    
    /**
     * Check if character has item in inventory.
     */
    public boolean hasItem(String itemId) {
        return inventoryItemIds.contains(itemId);
    }
    
    // ==================== Getters and Setters ====================
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Race getRace() {
        return race;
    }
    
    public int getStat(CoreStat stat) {
        return stats.get(stat);
    }
    
    public void setStat(CoreStat stat, int value) {
        int clampedValue = Math.max(0, Math.min(HARD_CAP, value));
        stats.put(stat, clampedValue);
        updateDerivedStats();
    }
    
    public Map<CoreStat, Integer> getStats() {
        return new HashMap<>(stats); // Defensive copy
    }
    
    public double getDerivedStat(String statName) {
        return derivedStats.getOrDefault(statName, 0.0);
    }
    
    public Map<String, Double> getDerivedStats() {
        return new HashMap<>(derivedStats); // Defensive copy
    }
    
    public int getCurrentMana() {
        return currentMana;
    }
    
    public int getMaxMana() {
        return maxMana;
    }
    
    public void setCurrentMana(int currentMana) {
        this.currentMana = Math.max(0, Math.min(maxMana, currentMana));
    }
    
    public List<Trait> getTraits() {
        return new ArrayList<>(traits); // Defensive copy
    }
    
    public List<Skill> getSkills() {
        return new ArrayList<>(skills); // Defensive copy
    }
    
    public List<String> getInventoryItemIds() {
        return new ArrayList<>(inventoryItemIds); // Defensive copy
    }
    
    public String getSocietyId() {
        return societyId;
    }
    
    public void setSocietyId(String societyId) {
        this.societyId = societyId;
    }
    
    public long getLastUpdatedTick() {
        return lastUpdatedTick;
    }
    
    public void setLastUpdatedTick(long lastUpdatedTick) {
        this.lastUpdatedTick = lastUpdatedTick;
    }
    
    public int getSchemaVersion() {
        return schemaVersion;
    }
}
