package org.adventure.character;

import org.adventure.character.Character.CoreStat;

import java.util.EnumMap;
import java.util.Map;

/**
 * Race/Species defines base stats, affinities, and unique abilities for character types.
 * 
 * <p>All sapient races are playable. Base stats determine starting values and growth affinities.
 * Races can have unique abilities, predispositions, and cultural traits.
 * 
 * <p>Design: docs/characters_stats_traits_skills.md → Bestiary Details
 */
public class Race {
    
    private final String id;
    private final String name;
    private final String description;
    
    // Base stats (starting values for new characters)
    private final Map<CoreStat, Integer> baseStats;
    
    // Stat affinities (multipliers for stat progression, 1.0 = normal)
    private final Map<CoreStat, Double> statAffinities;
    
    // Natural traits (all members of this race have these)
    private final java.util.List<Trait> naturalTraits;
    
    // Unique abilities (descriptive for now, mechanics in Phase 2)
    private final java.util.List<String> uniqueAbilities;
    
    /**
     * Create a new race with specified base stats.
     * 
     * @param id Unique race identifier
     * @param name Display name
     * @param description Race description and lore
     */
    public Race(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseStats = new EnumMap<>(CoreStat.class);
        this.statAffinities = new EnumMap<>(CoreStat.class);
        this.naturalTraits = new java.util.ArrayList<>();
        this.uniqueAbilities = new java.util.ArrayList<>();
        
        // Initialize all base stats to 10 (human baseline)
        for (CoreStat stat : CoreStat.values()) {
            baseStats.put(stat, 10);
            statAffinities.put(stat, 1.0);
        }
    }
    
    // ==================== Pre-defined Races ====================
    
    /** Human: Balanced stats, fast learner */
    public static final Race HUMAN = createHuman();
    
    private static Race createHuman() {
        Race human = new Race("human", "Human", 
            "Versatile and adaptable, humans excel at learning new skills quickly");
        // Balanced stats (all 10 = baseline)
        human.setBaseStat(CoreStat.CHARISMA, 12); // +2 CHA (social bonus)
        human.setStatAffinity(CoreStat.INTELLIGENCE, 1.1); // +10% INT progression
        return human;
    }
    
    /** Elf: Agile, perceptive, long-lived */
    public static final Race ELF = createElf();
    
    private static Race createElf() {
        Race elf = new Race("elf", "Elf",
            "Graceful and perceptive, elves are natural archers and mages");
        elf.setBaseStat(CoreStat.DEXTERITY, 14); // +4 DEX
        elf.setBaseStat(CoreStat.PERCEPTION, 14); // +4 PER
        elf.setBaseStat(CoreStat.INTELLIGENCE, 12); // +2 INT
        elf.setBaseStat(CoreStat.CONSTITUTION, 8); // -2 CON (frail)
        elf.setStatAffinity(CoreStat.DEXTERITY, 1.2); // +20% DEX progression
        elf.addNaturalTrait(Trait.NIGHT_VISION);
        elf.addUniqueAbility("Keen Senses: +5 to detection and initiative rolls");
        return elf;
    }
    
    /** Dwarf: Robust, strong, skilled craftsmen */
    public static final Race DWARF = createDwarf();
    
    private static Race createDwarf() {
        Race dwarf = new Race("dwarf", "Dwarf",
            "Hardy and industrious, dwarves are master smiths and miners");
        dwarf.setBaseStat(CoreStat.STRENGTH, 13); // +3 STR
        dwarf.setBaseStat(CoreStat.CONSTITUTION, 15); // +5 CON
        dwarf.setBaseStat(CoreStat.DEXTERITY, 8); // -2 DEX (less agile)
        dwarf.setBaseStat(CoreStat.CHARISMA, 9); // -1 CHA (gruff)
        dwarf.setStatAffinity(CoreStat.CONSTITUTION, 1.3); // +30% CON progression
        dwarf.addNaturalTrait(Trait.ROBUST);
        dwarf.addUniqueAbility("Mining Bonus: +20% resource yield from ore and stone");
        dwarf.addUniqueAbility("Poison Resistance: +50% resistance to toxins");
        return dwarf;
    }
    
    /** Orc: Powerful, intimidating, warrior culture */
    public static final Race ORC = createOrc();
    
    private static Race createOrc() {
        Race orc = new Race("orc", "Orc",
            "Fierce warriors with unmatched physical prowess");
        orc.setBaseStat(CoreStat.STRENGTH, 16); // +6 STR
        orc.setBaseStat(CoreStat.CONSTITUTION, 14); // +4 CON
        orc.setBaseStat(CoreStat.INTELLIGENCE, 8); // -2 INT
        orc.setBaseStat(CoreStat.CHARISMA, 7); // -3 CHA (intimidating)
        orc.setStatAffinity(CoreStat.STRENGTH, 1.25); // +25% STR progression
        orc.addUniqueAbility("Intimidating Presence: +20% success on intimidation checks");
        orc.addUniqueAbility("Rage: +50% melee damage when health below 30%");
        return orc;
    }
    
    /** Goblin: Cunning, nimble, opportunistic */
    public static final Race GOBLIN = createGoblin();
    
    private static Race createGoblin() {
        Race goblin = new Race("goblin", "Goblin",
            "Small and clever, goblins survive through cunning and stealth");
        goblin.setBaseStat(CoreStat.DEXTERITY, 14); // +4 DEX
        goblin.setBaseStat(CoreStat.LUCK, 13); // +3 LUCK
        goblin.setBaseStat(CoreStat.STRENGTH, 7); // -3 STR (small)
        goblin.setBaseStat(CoreStat.CONSTITUTION, 8); // -2 CON (frail)
        goblin.setStatAffinity(CoreStat.LUCK, 1.3); // +30% LUCK progression
        goblin.addUniqueAbility("Small Target: +10% dodge chance");
        goblin.addUniqueAbility("Scavenger: +20% loot quality from corpses");
        return goblin;
    }
    
    /** Halfling: Lucky, charismatic, stealthy */
    public static final Race HALFLING = createHalfling();
    
    private static Race createHalfling() {
        Race halfling = new Race("halfling", "Halfling",
            "Cheerful and lucky, halflings are natural traders and rogues");
        halfling.setBaseStat(CoreStat.CHARISMA, 14); // +4 CHA
        halfling.setBaseStat(CoreStat.LUCK, 15); // +5 LUCK
        halfling.setBaseStat(CoreStat.DEXTERITY, 12); // +2 DEX
        halfling.setBaseStat(CoreStat.STRENGTH, 7); // -3 STR (small)
        halfling.setStatAffinity(CoreStat.LUCK, 1.4); // +40% LUCK progression
        halfling.addNaturalTrait(Trait.LUCKY);
        halfling.addUniqueAbility("Lucky Escape: 5% chance to avoid death when health reaches 0");
        return halfling;
    }
    
    /** Troll: Regenerative, powerful, low intelligence */
    public static final Race TROLL = createTroll();
    
    private static Race createTroll() {
        Race troll = new Race("troll", "Troll",
            "Massive regenerative creatures with fearsome strength");
        troll.setBaseStat(CoreStat.STRENGTH, 18); // +8 STR
        troll.setBaseStat(CoreStat.CONSTITUTION, 17); // +7 CON
        troll.setBaseStat(CoreStat.INTELLIGENCE, 6); // -4 INT
        troll.setBaseStat(CoreStat.CHARISMA, 5); // -5 CHA (ugly)
        troll.setStatAffinity(CoreStat.CONSTITUTION, 1.5); // +50% CON progression
        troll.addUniqueAbility("Regeneration: Heal 5% max HP per tick (except fire damage)");
        troll.addUniqueAbility("Fire Vulnerability: Take 50% more damage from fire");
        return troll;
    }
    
    /** Dragon (playable form, humanoid): Magical, powerful, rare */
    public static final Race DRAGON = createDragon();
    
    private static Race createDragon() {
        Race dragon = new Race("dragon", "Dragon",
            "Ancient and powerful, dragons possess immense magical potential");
        dragon.setBaseStat(CoreStat.STRENGTH, 15); // +5 STR
        dragon.setBaseStat(CoreStat.INTELLIGENCE, 17); // +7 INT
        dragon.setBaseStat(CoreStat.WISDOM, 15); // +5 WIS
        dragon.setBaseStat(CoreStat.CONSTITUTION, 14); // +4 CON
        dragon.setStatAffinity(CoreStat.INTELLIGENCE, 1.3); // +30% INT progression
        dragon.addNaturalTrait(Trait.LEGENDARY_POTENTIAL);
        dragon.addUniqueAbility("Dragon Breath: Elemental breath weapon (fire/ice/lightning)");
        dragon.addUniqueAbility("Flight: Can fly (humanoid form: limited duration)");
        dragon.addUniqueAbility("Ancient Knowledge: Start with bonus magic skills");
        return dragon;
    }
    
    // ==================== Stat Configuration ====================
    
    /**
     * Set base stat value for this race.
     */
    public void setBaseStat(CoreStat stat, int value) {
        baseStats.put(stat, value);
    }
    
    /**
     * Set stat affinity (progression multiplier) for this race.
     * 
     * @param stat Core stat
     * @param affinity Multiplier (1.0 = normal, >1.0 = faster progression)
     */
    public void setStatAffinity(CoreStat stat, double affinity) {
        statAffinities.put(stat, affinity);
    }
    
    /**
     * Add a natural trait that all members of this race have.
     */
    public void addNaturalTrait(Trait trait) {
        if (!naturalTraits.contains(trait)) {
            naturalTraits.add(trait);
        }
    }
    
    /**
     * Add a unique ability (descriptive for now).
     */
    public void addUniqueAbility(String ability) {
        uniqueAbilities.add(ability);
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
    
    public int getBaseStat(CoreStat stat) {
        return baseStats.get(stat);
    }
    
    public Map<CoreStat, Integer> getBaseStats() {
        return new EnumMap<>(baseStats); // Defensive copy
    }
    
    public double getStatAffinity(CoreStat stat) {
        return statAffinities.get(stat);
    }
    
    public java.util.List<Trait> getNaturalTraits() {
        return new java.util.ArrayList<>(naturalTraits); // Defensive copy
    }
    
    public java.util.List<String> getUniqueAbilities() {
        return new java.util.ArrayList<>(uniqueAbilities); // Defensive copy
    }
    
    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
