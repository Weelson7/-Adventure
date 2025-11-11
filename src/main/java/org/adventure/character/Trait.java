package org.adventure.character;

import org.adventure.character.Character.CoreStat;

/**
 * Trait represents a permanent character attribute that modifies stats, skills, and behavior.
 * 
 * <p>Traits are lifelong and cannot be suppressed or hidden (except through special events).
 * They affect stat progression rates, skill XP gain, and provide unique modifiers.
 * 
 * <p>Design: docs/characters_stats_traits_skills.md → Trait System
 */
public class Trait {
    
    private final String id;
    private final String name;
    private final String description;
    private final boolean hereditary;
    
    // Stat modifiers
    private final int softCapThresholdBonus; // Increases soft cap (default 0)
    private final double statProgressionMultiplier; // Multiplies stat gains (default 1.0)
    
    // Skill modifiers
    private final double skillXPMultiplier; // Multiplies skill XP gains (default 1.0)
    
    // Per-stat multipliers (optional, for traits like "Athletic" that only boost STR/DEX)
    private final java.util.Map<CoreStat, Double> perStatMultipliers;
    
    /**
     * Create a new trait with specified modifiers.
     * 
     * @param id Unique trait identifier
     * @param name Display name
     * @param description Description of trait effects
     * @param hereditary True if trait can be inherited by offspring
     * @param softCapThresholdBonus Bonus to soft cap threshold (0 = no change)
     * @param statProgressionMultiplier Multiplier for stat gains (1.0 = no change)
     * @param skillXPMultiplier Multiplier for skill XP (1.0 = no change)
     */
    public Trait(String id, String name, String description, boolean hereditary,
                 int softCapThresholdBonus, double statProgressionMultiplier, double skillXPMultiplier) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.hereditary = hereditary;
        this.softCapThresholdBonus = softCapThresholdBonus;
        this.statProgressionMultiplier = statProgressionMultiplier;
        this.skillXPMultiplier = skillXPMultiplier;
        this.perStatMultipliers = new java.util.HashMap<>();
    }
    
    /**
     * Simple trait constructor with no modifiers (pure flavor trait).
     */
    public Trait(String id, String name, String description, boolean hereditary) {
        this(id, name, description, hereditary, 0, 1.0, 1.0);
    }
    
    // ==================== Pre-defined Traits ====================
    
    /** Fast Learner: +20% stat progression, +30% skill XP */
    public static final Trait FAST_LEARNER = new Trait(
        "fast_learner", "Fast Learner",
        "Learns skills 30% faster and gains stats 20% faster",
        false, 0, 1.2, 1.3
    );
    
    /** Robust: +10 Constitution base, +5 soft cap threshold */
    public static final Trait ROBUST = new Trait(
        "robust", "Robust",
        "Naturally hardy constitution, +10 CON, +5 soft cap",
        true, 5, 1.0, 1.0
    );
    
    /** Agile: +10 Dexterity base, +5 soft cap threshold */
    public static final Trait AGILE = new Trait(
        "agile", "Agile",
        "Naturally quick and nimble, +10 DEX, +5 soft cap",
        true, 5, 1.0, 1.0
    );
    
    /** Clumsy: -10% stat progression, -20% skill XP (negative trait) */
    public static final Trait CLUMSY = new Trait(
        "clumsy", "Clumsy",
        "Struggles with physical coordination, -10% stat gain, -20% skill XP",
        false, 0, 0.9, 0.8
    );
    
    /** Blessed: +10% stat progression, +10 soft cap threshold */
    public static final Trait BLESSED = new Trait(
        "blessed", "Blessed",
        "Touched by divine favor, +10% stat progression, +10 soft cap",
        false, 10, 1.1, 1.0
    );
    
    /** Cursed: -20% stat progression, -10 soft cap threshold (negative trait) */
    public static final Trait CURSED = new Trait(
        "cursed", "Cursed",
        "Plagued by misfortune, -20% stat progression, -10 soft cap",
        false, -10, 0.8, 1.0
    );
    
    /** Night Vision: No modifier, pure ability trait */
    public static final Trait NIGHT_VISION = new Trait(
        "night_vision", "Night Vision",
        "Can see clearly in darkness",
        true, 0, 1.0, 1.0
    );
    
    /** Resilient: +15% stat progression for CON only */
    public static final Trait RESILIENT = new Trait(
        "resilient", "Resilient",
        "Exceptionally resistant to disease and poison, +15% CON progression",
        true, 0, 1.0, 1.0
    );
    
    /** Genius: +20 INT base, +10 soft cap for INT */
    public static final Trait GENIUS = new Trait(
        "genius", "Genius",
        "Exceptional intellect, +20 INT base, +10 INT soft cap",
        false, 10, 1.0, 1.0
    );
    
    /** Charismatic: +15 CHA base, +5 soft cap for CHA */
    public static final Trait CHARISMATIC = new Trait(
        "charismatic", "Charismatic",
        "Natural leader and negotiator, +15 CHA base, +5 CHA soft cap",
        true, 5, 1.0, 1.0
    );
    
    /** Lucky: +10 LUCK base */
    public static final Trait LUCKY = new Trait(
        "lucky", "Lucky",
        "Fortune favors this character, +10 LUCK",
        false, 0, 1.0, 1.0
    );
    
    /** Legendary Potential: +50 soft cap threshold for all stats */
    public static final Trait LEGENDARY_POTENTIAL = new Trait(
        "legendary_potential", "Legendary Potential",
        "Destined for greatness, +50 soft cap threshold",
        false, 50, 1.0, 1.0
    );
    
    // ==================== Per-Stat Multipliers ====================
    
    /**
     * Set a per-stat multiplier (for traits that only affect specific stats).
     * 
     * @param stat Core stat to modify
     * @param multiplier Multiplier for this stat (1.0 = no change)
     */
    public void setStatMultiplier(CoreStat stat, double multiplier) {
        perStatMultipliers.put(stat, multiplier);
    }
    
    /**
     * Get stat progression multiplier for a specific stat.
     * Returns per-stat multiplier if set, otherwise returns base multiplier.
     */
    public double getStatProgressionMultiplier(CoreStat stat) {
        return perStatMultipliers.getOrDefault(stat, statProgressionMultiplier);
    }
    
    // ==================== Getters ====================
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isHereditary() {
        return hereditary;
    }
    
    public int getSoftCapThresholdBonus() {
        return softCapThresholdBonus;
    }
    
    public double getSkillXPMultiplier() {
        return skillXPMultiplier;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Trait)) return false;
        Trait other = (Trait) obj;
        return id.equals(other.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
