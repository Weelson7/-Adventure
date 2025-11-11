# Skill.java - Learned Ability System

**Package:** `org.adventure.character`  
**Source:** [Skill.java](../../../../../src/main/java/org/adventure/character/Skill.java)  
**Phase:** MVP Phase 1.3 (Characters & NPCs)

## Overview

`Skill` represents a learned ability that can be improved through practice and XP. Skills are organized into 5 categories (Combat, Crafting, Magic, Social, Survival) with 17 pre-defined skills, each progressing through 5 proficiency tiers from Novice to Master.

Unlike traits (permanent), skills can be forgotten for retraining, creating strategic choices about specialization vs. versatility.

## Design Philosophy

### Why Skills Matter

Skills define character capabilities:
- **Specialization**: Warriors focus on Combat, mages on Magic, merchants on Social
- **Progression**: Visible improvement from Novice (0 XP) to Master (2700+ XP)
- **Prerequisites**: Advanced skills require mastering basics (Dual Wielding requires Sword Fighting)
- **Retraining**: Forget obsolete skills to learn new ones (50% XP penalty)

From `docs/characters_stats_traits_skills.md`:
> "Skills can be forgotten for retraining; more skills means slower skill upgrade rate."

### 5 Skill Categories, 17 Skills

```java
public enum Category {
    COMBAT,    // Physical combat abilities
    CRAFTING,  // Item creation and repair
    MAGIC,     // Spell casting and enchanting
    SOCIAL,    // Diplomacy, trade, leadership
    SURVIVAL   // Wilderness skills
}

// Combat Skills (4)
SWORD_FIGHTING, ARCHERY, SHIELD_DEFENSE, DUAL_WIELDING

// Crafting Skills (4)
SMITHING, ALCHEMY, ENCHANTING, CARPENTRY

// Magic Skills (4)
FIRE_MAGIC, ICE_MAGIC, HEALING_MAGIC, RUNE_CASTING

// Social Skills (3)
PERSUASION, LEADERSHIP, INTIMIDATION

// Survival Skills (3)
FORAGING, TRACKING, CAMPING
```

## Proficiency Tier System

### 5 Tiers of Mastery

```java
public enum ProficiencyTier {
    NOVICE      (0,    100),   // Beginner, just learned
    APPRENTICE  (100,  400),   // Competent, regular use
    JOURNEYMAN  (400,  1101),  // Skilled, professional level
    EXPERT      (1101, 2700),  // Master craftsman/warrior
    MASTER      (2700, MAX)    // Legendary, peak performance
}
```

**XP Thresholds:**
- **0-99 XP**: Novice (just learned)
- **100-399 XP**: Apprentice (1-4 months practice)
- **400-1100 XP**: Journeyman (1-2 years practice)
- **1101-2699 XP**: Expert (5-10 years mastery)
- **2700+ XP**: Master (lifetime dedication)

**Design Rationale:**
- **Exponential Growth**: Each tier requires ~3x previous tier's XP
- **Achievable Mastery**: Journeyman (400 XP) is realistic for dedicated characters
- **Aspirational Peak**: Master tier (2700+ XP) is rare, reserved for legendary NPCs

### XP Progression Formula

```java
public void addXP(int xp) {
    currentXP += xp;
    updateTier();
}

private void updateTier() {
    ProficiencyTier newTier = ProficiencyTier.fromXP(currentXP);
    if (newTier != currentTier) {
        currentTier = newTier;
        // Emit level-up notification
    }
}

public static ProficiencyTier fromXP(int xp) {
    for (ProficiencyTier tier : values()) {
        if (xp >= tier.minXP && xp < tier.maxXP) {
            return tier;
        }
    }
    return MASTER;
}
```

**Usage Example:**
```java
Skill sword = Skill.SWORD_FIGHTING;
sword.addXP(50);   // 50 XP → NOVICE
sword.addXP(60);   // 110 XP → APPRENTICE
sword.addXP(300);  // 410 XP → JOURNEYMAN
```

## Skill Prerequisites

### Learning Requirements

```java
private final List<String> prerequisiteSkillIds;

public boolean canLearn(List<Skill> characterSkills) {
    for (String prereqId : prerequisiteSkillIds) {
        boolean hasPrereq = characterSkills.stream()
            .anyMatch(s -> s.getId().equals(prereqId));
        if (!hasPrereq) {
            return false;
        }
    }
    return true;
}
```

**Pre-Defined Prerequisites:**
```java
static {
    // Dual Wielding requires Sword Fighting
    DUAL_WIELDING.addPrerequisite("sword_fighting");
    
    // Rune Casting requires Fire Magic OR Ice Magic
    RUNE_CASTING.addPrerequisite("fire_magic");
    RUNE_CASTING.addPrerequisite("ice_magic");
}
```

**Why Prerequisites?**
- **Realism**: Can't dual-wield without basic sword training
- **Progression**: Forces gradual skill tree advancement
- **Balance**: Prevents rushing to overpowered skills

## Skill Forgetting System

### Retraining Mechanics

```java
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
```

**Usage Scenario:**
```java
// Character has Sword Fighting (500 XP, Journeyman)
int penalty = sword.forget();  // Returns 250 XP penalty
// Now Sword Fighting reset to 0 XP (Novice)
// Character can learn Archery or another skill
```

**Design Rationale:**
- **50% Penalty**: Discourages frivolous retraining, but allows flexibility
- **Total Reset**: Back to Novice tier, must rebuild from scratch
- **Strategic Choice**: "Do I retrain now or keep this skill?"

### Skill Limits (Future)

From `docs/characters_stats_traits_skills.md`:
> "More skills means slower skill upgrade rate."

**Planned Implementation (Phase 1.4):**
```java
// XP gain penalty based on total skill count
public void addXP(int baseXP, Character character) {
    int skillCount = character.getSkills().size();
    double penalty = 1.0 / (1.0 + (skillCount / 10.0));
    int actualXP = (int) Math.round(baseXP * penalty);
    currentXP += actualXP;
}
```

**Effect:**
- 5 skills: 100% XP gain (no penalty)
- 10 skills: 50% XP gain
- 20 skills: 33% XP gain

**Balance Goal:** Encourage specialization, discourage "jack of all trades"

## Pre-Defined Skills Gallery

### Combat Skills

#### Sword Fighting
```java
SWORD_FIGHTING = new Skill(
    "sword_fighting", "Sword Fighting", Category.COMBAT,
    "Melee combat with swords, increases accuracy and damage"
);
```
- **Prerequisites**: None (basic skill)
- **Effect**: +10% melee accuracy per tier
- **Unlocks**: Dual Wielding

#### Archery
```java
ARCHERY = new Skill(
    "archery", "Archery", Category.COMBAT,
    "Ranged combat with bows, increases accuracy and range"
);
```
- **Prerequisites**: None
- **Effect**: +15% ranged accuracy per tier
- **Synergy**: DEX stat bonus

#### Shield Defense
```java
SHIELD_DEFENSE = new Skill(
    "shield_defense", "Shield Defense", Category.COMBAT,
    "Blocking attacks with shields, increases defense and parry chance"
);
```
- **Prerequisites**: None
- **Effect**: +5% damage reduction per tier
- **Synergy**: CON stat bonus

#### Dual Wielding
```java
DUAL_WIELDING = new Skill(
    "dual_wielding", "Dual Wielding", Category.COMBAT,
    "Fighting with two weapons, increases attack speed and parry chance"
);
```
- **Prerequisites**: Sword Fighting
- **Effect**: +20% attack speed per tier
- **Advanced Skill**: Requires mastery of basics

### Crafting Skills

#### Smithing
```java
SMITHING = new Skill(
    "smithing", "Smithing", Category.CRAFTING,
    "Forging weapons and armor, improves quality and reduces material waste"
);
```
- **Effect**: +10% item quality per tier
- **Integration**: Phase 1.4 (Objects & Crafting)

#### Alchemy
```java
ALCHEMY = new Skill(
    "alchemy", "Alchemy", Category.CRAFTING,
    "Brewing potions and elixirs, increases effect potency and reduces failure chance"
);
```
- **Effect**: +15% potion potency per tier
- **Synergy**: INT stat bonus

#### Enchanting
```java
ENCHANTING = new Skill(
    "enchanting", "Enchanting", Category.CRAFTING,
    "Imbuing items with magic, improves enchantment power and duration"
);
```
- **Effect**: +20% enchantment strength per tier
- **Synergy**: WIS stat bonus

#### Carpentry
```java
CARPENTRY = new Skill(
    "carpentry", "Carpentry", Category.CRAFTING,
    "Building structures and furniture, improves construction speed and quality"
);
```
- **Effect**: +10% build speed per tier
- **Integration**: Phase 1.8 (Structures & Ownership)

### Magic Skills

#### Fire Magic
```java
FIRE_MAGIC = new Skill(
    "fire_magic", "Fire Magic", Category.MAGIC,
    "Casting fire spells, increases damage and reduces mana cost"
);
```
- **Effect**: +10% fire damage per tier
- **Unlocks**: Rune Casting
- **Synergy**: INT stat bonus

#### Ice Magic
```java
ICE_MAGIC = new Skill(
    "ice_magic", "Ice Magic", Category.MAGIC,
    "Casting ice spells, increases damage and adds slow effect"
);
```
- **Effect**: +10% ice damage + slow effect per tier
- **Unlocks**: Rune Casting

#### Healing Magic
```java
HEALING_MAGIC = new Skill(
    "healing_magic", "Healing Magic", Category.MAGIC,
    "Casting healing spells, increases heal amount and reduces mana cost"
);
```
- **Effect**: +15% heal potency per tier
- **Synergy**: WIS stat bonus

#### Rune Casting
```java
RUNE_CASTING = new Skill(
    "rune_casting", "Rune Casting", Category.MAGIC,
    "Advanced magic using runes, powerful but complex spells"
);
```
- **Prerequisites**: Fire Magic OR Ice Magic
- **Effect**: +25% spell power per tier
- **Advanced Skill**: Requires elemental mastery

### Social Skills

#### Persuasion
```java
PERSUASION = new Skill(
    "persuasion", "Persuasion", Category.SOCIAL,
    "Convincing others, improves negotiation and quest outcomes"
);
```
- **Effect**: +10% success chance per tier
- **Synergy**: CHA stat bonus

#### Leadership
```java
LEADERSHIP = new Skill(
    "leadership", "Leadership", Category.SOCIAL,
    "Leading groups, increases follower morale and combat effectiveness"
);
```
- **Effect**: +15% follower effectiveness per tier
- **Integration**: Phase 1.6 (Societies & Clans)

#### Intimidation
```java
INTIMIDATION = new Skill(
    "intimidation", "Intimidation", Category.SOCIAL,
    "Threatening others, improves success chance and can avoid combat"
);
```
- **Effect**: +10% intimidation success per tier
- **Synergy**: STR stat bonus

### Survival Skills

#### Foraging
```java
FORAGING = new Skill(
    "foraging", "Foraging", Category.SURVIVAL,
    "Finding food and herbs, increases yield and quality"
);
```
- **Effect**: +20% resource yield per tier
- **Synergy**: PER stat bonus

#### Tracking
```java
TRACKING = new Skill(
    "tracking", "Tracking", Category.SURVIVAL,
    "Following trails, reveals creature locations and improves hunting"
);
```
- **Effect**: +25% tracking range per tier
- **Synergy**: PER stat bonus

#### Camping
```java
CAMPING = new Skill(
    "camping", "Camping", Category.SURVIVAL,
    "Setting up camp, improves rest benefits and reduces random encounters"
);
```
- **Effect**: +10% rest efficiency per tier
- **Synergy**: CON stat bonus

## Singleton Design Pattern

### The Singleton Problem

**Initial Design:**
```java
public static final Skill SWORD_FIGHTING = new Skill(...);
```

**Issue:** Static singleton skills are shared across all tests!
```java
@Test
void test1() {
    Skill.SWORD_FIGHTING.addXP(50);  // Now has 50 XP
}

@Test
void test2() {
    assertEquals(0, Skill.SWORD_FIGHTING.getCurrentXP());  
    // FAILS! Still has 50 XP from test1
}
```

### The Solution: Reset Method

```java
/**
 * Reset skill to initial state (for testing purposes).
 */
public void reset() {
    currentXP = 0;
    currentTier = ProficiencyTier.NOVICE;
}
```

**Test Setup:**
```java
@BeforeEach
void setUp() {
    // Reset all skills before each test
    Skill.SWORD_FIGHTING.reset();
    Skill.ARCHERY.reset();
    // ... all 17 skills
}
```

**Why Not Clone?**
- Simpler than deep cloning
- Works for MVP testing
- Future: Instance-based skills (not singletons)

## API Reference

### Constructor

```java
public Skill(String id, String name, Category category, String description)
```

### XP Management

```java
public void addXP(int xp)
public int getCurrentXP()
public ProficiencyTier getCurrentTier()
public int getXPToNextTier()
```

### Prerequisites

```java
public void addPrerequisite(String skillId)
public List<String> getPrerequisiteSkillIds()
public boolean canLearn(List<Skill> characterSkills)
```

### Forgetting

```java
public int forget()  // Returns XP penalty
```

### Getters

```java
public String getId()
public String getName()
public Category getCategory()
public String getDescription()
```

## Testing

See [SkillTest.md](../../../../../test/java/org/adventure/SkillTest.md):
- 16 unit tests covering all functionality
- XP progression validation
- Proficiency tier transitions
- Prerequisite checks
- Forgetting mechanics

**All tests passing ✅** after fixing tier ranges and adding reset method.

## Integration with Character System

```java
// Character.java
private final List<Skill> skills;

public void addSkill(Skill skill) {
    if (!skills.contains(skill) && skill.canLearn(skills)) {
        skills.add(skill);
    }
}

public boolean hasSkill(Skill skill) {
    return skills.contains(skill);
}
```

## Future Enhancements

### Skill Synergies
```java
// Bonus when combining skills
if (hasSkill(FIRE_MAGIC) && hasSkill(ICE_MAGIC)) {
    // Unlock "Steam Magic" bonus
}
```

### Skill Trees
```java
// Visual progression paths
SWORD_FIGHTING → DUAL_WIELDING → BLADE_MASTER
```

### Passive Bonuses
```java
// Master-tier bonuses
if (skill.getCurrentTier() == MASTER) {
    statBonus += 5;  // +5 to related stat
}
```

## References

- **Design Docs**: `docs/characters_stats_traits_skills.md` → Skill System
- **Related Classes**: [Character.md](Character.md), [Trait.md](Trait.md)
- **Tests**: [SkillTest.md](../../../../../test/java/org/adventure/SkillTest.md)

---

**Last Updated:** Phase 1.3 Implementation (November 2025)  
**Status:** ✅ Complete - 16 tests passing, 17 skills implemented
