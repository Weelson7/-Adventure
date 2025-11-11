package org.adventure.character;

/**
 * Skill represents a learned ability that can be improved through practice and XP.
 * 
 * <p>Skills are organized into categories (Combat, Crafting, Magic, Social, Survival).
 * Each skill has proficiency tiers from Novice to Master, requiring progressively more XP.
 * 
 * <p>Skills can be forgotten for retraining, with a penalty. More skills means slower
 * individual skill upgrade rate (XP spread across more skills).
 * 
 * <p>Design: docs/characters_stats_traits_skills.md → Skill System
 */
public class Skill {
    
    /**
     * Skill categories for organization and synergy.
     */
    public enum Category {
        COMBAT("Combat", "Physical combat skills"),
        CRAFTING("Crafting", "Item creation and repair"),
        MAGIC("Magic", "Spell casting and enchanting"),
        SOCIAL("Social", "Diplomacy, trade, leadership"),
        SURVIVAL("Survival", "Foraging, tracking, camping");
        
        private final String displayName;
        private final String description;
        
        Category(String displayName, String description) {
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
    
    /**
     * Proficiency tiers for skill mastery.
     */
    public enum ProficiencyTier {
        NOVICE(0, 100, "Novice"),
        APPRENTICE(100, 400, "Apprentice"),
        JOURNEYMAN(400, 1101, "Journeyman"),
        EXPERT(1101, 2700, "Expert"),
        MASTER(2700, Integer.MAX_VALUE, "Master");
        
        private final int minXP;
        private final int maxXP;
        private final String displayName;
        
        ProficiencyTier(int minXP, int maxXP, String displayName) {
            this.minXP = minXP;
            this.maxXP = maxXP;
            this.displayName = displayName;
        }
        
        public int getMinXP() {
            return minXP;
        }
        
        public int getMaxXP() {
            return maxXP;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * Get proficiency tier for given XP amount.
         */
        public static ProficiencyTier fromXP(int xp) {
            for (ProficiencyTier tier : values()) {
                if (xp >= tier.minXP && xp < tier.maxXP) {
                    return tier;
                }
            }
            return MASTER; // Default to master if XP exceeds all thresholds
        }
    }
    
    private final String id;
    private final String name;
    private final Category category;
    private final String description;
    
    private int currentXP;
    private ProficiencyTier currentTier;
    
    // Optional: prerequisite skills
    private final java.util.List<String> prerequisiteSkillIds;
    
    /**
     * Create a new skill.
     * 
     * @param id Unique skill identifier
     * @param name Display name
     * @param category Skill category
     * @param description Skill description
     */
    public Skill(String id, String name, Category category, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.currentXP = 0;
        this.currentTier = ProficiencyTier.NOVICE;
        this.prerequisiteSkillIds = new java.util.ArrayList<>();
    }
    
    // ==================== Pre-defined Skills ====================
    
    // Combat Skills
    public static final Skill SWORD_FIGHTING = new Skill(
        "sword_fighting", "Sword Fighting", Category.COMBAT,
        "Melee combat with swords, increases accuracy and damage"
    );
    
    public static final Skill ARCHERY = new Skill(
        "archery", "Archery", Category.COMBAT,
        "Ranged combat with bows, increases accuracy and critical chance"
    );
    
    public static final Skill SHIELD_DEFENSE = new Skill(
        "shield_defense", "Shield Defense", Category.COMBAT,
        "Defensive combat with shields, increases block chance and damage mitigation"
    );
    
    public static final Skill DUAL_WIELDING = new Skill(
        "dual_wielding", "Dual Wielding", Category.COMBAT,
        "Fighting with two weapons, increases attack speed and parry chance"
    );
    
    // Crafting Skills
    public static final Skill SMITHING = new Skill(
        "smithing", "Smithing", Category.CRAFTING,
        "Forging weapons and armor, improves quality and reduces material waste"
    );
    
    public static final Skill ALCHEMY = new Skill(
        "alchemy", "Alchemy", Category.CRAFTING,
        "Brewing potions and elixirs, increases effect potency and reduces failure chance"
    );
    
    public static final Skill ENCHANTING = new Skill(
        "enchanting", "Enchanting", Category.CRAFTING,
        "Imbuing items with magic, increases enchantment strength"
    );
    
    public static final Skill CARPENTRY = new Skill(
        "carpentry", "Carpentry", Category.CRAFTING,
        "Working with wood, improves structure quality and crafting speed"
    );
    
    // Magic Skills
    public static final Skill FIRE_MAGIC = new Skill(
        "fire_magic", "Fire Magic", Category.MAGIC,
        "Casting fire spells, increases damage and reduces mana cost"
    );
    
    public static final Skill ICE_MAGIC = new Skill(
        "ice_magic", "Ice Magic", Category.MAGIC,
        "Casting ice spells, increases slow effect and reduces mana cost"
    );
    
    public static final Skill HEALING_MAGIC = new Skill(
        "healing_magic", "Healing Magic", Category.MAGIC,
        "Casting healing spells, increases healing amount and reduces mana cost"
    );
    
    public static final Skill RUNE_CASTING = new Skill(
        "rune_casting", "Rune Casting", Category.MAGIC,
        "Advanced spell casting with runes, unlocks complex spells"
    );
    
    // Social Skills
    public static final Skill PERSUASION = new Skill(
        "persuasion", "Persuasion", Category.SOCIAL,
        "Convincing others, improves trade prices and quest outcomes"
    );
    
    public static final Skill LEADERSHIP = new Skill(
        "leadership", "Leadership", Category.SOCIAL,
        "Leading groups, increases follower morale and combat effectiveness"
    );
    
    public static final Skill INTIMIDATION = new Skill(
        "intimidation", "Intimidation", Category.SOCIAL,
        "Threatening others, improves success chance and can avoid combat"
    );
    
    // Survival Skills
    public static final Skill FORAGING = new Skill(
        "foraging", "Foraging", Category.SURVIVAL,
        "Finding food and herbs, increases yield and quality"
    );
    
    public static final Skill TRACKING = new Skill(
        "tracking", "Tracking", Category.SURVIVAL,
        "Following trails, reveals creature locations and improves hunting"
    );
    
    public static final Skill CAMPING = new Skill(
        "camping", "Camping", Category.SURVIVAL,
        "Setting up camp, improves rest benefits and reduces random encounters"
    );
    
    // Static initializer for skill prerequisites
    static {
        // Dual Wielding requires Sword Fighting
        DUAL_WIELDING.addPrerequisite("sword_fighting");
        
        // Rune Casting requires Fire Magic or Ice Magic (we'll add both)
        RUNE_CASTING.addPrerequisite("fire_magic");
        RUNE_CASTING.addPrerequisite("ice_magic");
    }
    
    // ==================== XP and Progression ====================
    
    /**
     * Add XP to this skill and update proficiency tier.
     * 
     * @param xp XP amount to add
     */
    public void addXP(int xp) {
        currentXP += xp;
        updateTier();
    }
    
    /**
     * Update proficiency tier based on current XP.
     */
    private void updateTier() {
        ProficiencyTier newTier = ProficiencyTier.fromXP(currentXP);
        if (newTier != currentTier) {
            currentTier = newTier;
            // Could emit event here for level-up notification
        }
    }
    
    /**
     * Get XP required to reach next tier.
     * 
     * @return XP needed, or 0 if at max tier
     */
    public int getXPToNextTier() {
        if (currentTier == ProficiencyTier.MASTER) {
            return 0; // Already at max
        }
        
        // Find next tier
        ProficiencyTier[] tiers = ProficiencyTier.values();
        for (int i = 0; i < tiers.length - 1; i++) {
            if (tiers[i] == currentTier) {
                return tiers[i + 1].getMinXP() - currentXP;
            }
        }
        
        return 0;
    }
    
    /**
     * Forget this skill (for retraining).
     * Returns XP penalty amount.
     * 
     * @return XP penalty (50% of current XP)
     */
    public int forget() {
        int penalty = currentXP / 2;
        currentXP = 0;
        currentTier = ProficiencyTier.NOVICE;
        return penalty;
    }
    
    /**
     * Reset skill to initial state (for testing purposes).
     */
    public void reset() {
        currentXP = 0;
        currentTier = ProficiencyTier.NOVICE;
    }
    
    // ==================== Prerequisites ====================
    
    /**
     * Add prerequisite skill ID.
     */
    public void addPrerequisite(String skillId) {
        if (!prerequisiteSkillIds.contains(skillId)) {
            prerequisiteSkillIds.add(skillId);
        }
    }
    
    /**
     * Check if character meets prerequisites (requires character context).
     */
    public boolean meetsPrerequisites(java.util.List<Skill> characterSkills) {
        if (prerequisiteSkillIds.isEmpty()) {
            return true; // No prerequisites
        }
        
        for (String prereqId : prerequisiteSkillIds) {
            boolean hasPrereq = characterSkills.stream()
                .anyMatch(s -> s.getId().equals(prereqId));
            if (!hasPrereq) {
                return false;
            }
        }
        
        return true;
    }
    
    // ==================== Getters ====================
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getCurrentXP() {
        return currentXP;
    }
    
    public ProficiencyTier getCurrentTier() {
        return currentTier;
    }
    
    public java.util.List<String> getPrerequisiteSkillIds() {
        return new java.util.ArrayList<>(prerequisiteSkillIds); // Defensive copy
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Skill)) return false;
        Skill other = (Skill) obj;
        return id.equals(other.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return name + " (" + currentTier.getDisplayName() + ", " + currentXP + " XP)";
    }
}
