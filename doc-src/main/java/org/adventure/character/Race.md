# Race.java - Species & Starting Attributes

**Package:** `org.adventure.character`  
**Source:** [Race.java](../../../../../src/main/java/org/adventure/character/Race.java)  
**Phase:** MVP Phase 1.3 (Characters & NPCs)

## Overview

`Race` (or Species) defines base stats, stat affinities, natural traits, and unique abilities for character types. All 8 playable races have distinct strengths and weaknesses, encouraging diverse playstyles.

From `docs/characters_stats_traits_skills.md`:
> "Every sapient race playable; bestiary required. Races/species have base stats, predispositions, affinities, and unique abilities."

## Design Philosophy

### 8 Playable Races

Each race balances **base stats** (starting values) with **stat affinities** (growth rates):

```java
// Base Stat: Starting value for new characters
// Stat Affinity: Multiplier for stat progression (1.0 = normal)

Race elf = new Race("elf", "Elf", "...");
elf.setBaseStat(CoreStat.DEXTERITY, 14);      // Starts with 14 DEX
elf.setStatAffinity(CoreStat.DEXTERITY, 1.2); // +20% DEX growth
```

**Design Rationale:**
- **Early Power**: High base stats give immediate advantage
- **Late Scaling**: Stat affinities reward specialization
- **Balance**: Races excel in some stats, weak in others

## Race Gallery

### Human
**Archetype:** Versatile Jack-of-All-Trades

```java
HUMAN = new Race("human", "Human", 
    "Versatile and adaptable, humans excel at learning new skills quickly");
// Base Stats: Balanced (10 baseline, +2 CHA)
// Affinities: +10% INT progression
```

**Stats:**
- Balanced across all stats (10 baseline)
- +2 CHA (social bonus)
- +10% INT progression (adaptable learners)

**Why Play Human?**
- **Flexibility**: No weak stats, works for any build
- **Fast Learner**: Often starts with Fast Learner trait
- **Social Bonus**: Better at diplomacy, trading, leadership

---

### Elf
**Archetype:** Agile Archer & Mage

```java
ELF = new Race("elf", "Elf",
    "Graceful and perceptive, elves are natural archers and mages");
// Base Stats: +4 DEX, +4 PER, +2 INT, -2 CON
// Affinities: +20% DEX progression
// Traits: Night Vision
```

**Stats:**
- **High:** DEX 14, PER 14, INT 12
- **Low:** CON 8 (frail)
- **Affinity:** +20% DEX progression

**Unique Abilities:**
- **Night Vision**: See clearly in darkness
- **Keen Senses**: +5 to detection and initiative rolls

**Why Play Elf?**
- **Archer Build**: Unmatched ranged accuracy
- **Mage Build**: High INT for spellcasting
- **Exploration**: Night Vision for dungeons, PER for finding secrets
- **Tradeoff:** Low CON means fragile in combat

---

### Dwarf
**Archetype:** Tank & Craftsman

```java
DWARF = new Race("dwarf", "Dwarf",
    "Hardy and industrious, dwarves are master smiths and miners");
// Base Stats: +3 STR, +5 CON, -2 DEX, -1 CHA
// Affinities: +30% CON progression
// Traits: Robust
```

**Stats:**
- **High:** CON 15, STR 13
- **Low:** DEX 8, CHA 9
- **Affinity:** +30% CON progression (tankiest race)

**Unique Abilities:**
- **Mining Bonus**: +20% resource yield from ore and stone
- **Poison Resistance**: +50% resistance to toxins

**Why Play Dwarf?**
- **Tank Build**: Highest health pool in game
- **Smithing**: Natural crafting bonus
- **Survival**: Poison resistance for swamps/dungeons
- **Tradeoff:** Slow (low DEX), poor social skills

---

### Orc
**Archetype:** Brutal Warrior

```java
ORC = new Race("orc", "Orc",
    "Powerful and intimidating, orcs are fearsome warriors");
// Base Stats: +6 STR, +3 CON, -3 INT, -3 CHA
// Affinities: +25% STR progression
```

**Stats:**
- **High:** STR 16, CON 13
- **Low:** INT 7, CHA 7
- **Affinity:** +25% STR progression (strongest race)

**Unique Abilities:**
- **Intimidating**: +10 to intimidation checks
- **Rage**: +25% damage when below 30% health (planned Phase 1.5)

**Why Play Orc?**
- **Melee Build**: Unmatched physical damage
- **Combat Focus**: High STR + Intimidation synergy
- **Berserker Style**: Rage ability for clutch wins
- **Tradeoff:** Poor mage (low INT), poor diplomat (low CHA)

---

### Halfling
**Archetype:** Lucky Rogue

```java
HALFLING = new Race("halfling", "Halfling",
    "Small and lucky, halflings are natural rogues and tricksters");
// Base Stats: +4 DEX, +4 LUCK, -4 STR, -2 CON
// Affinities: +20% LUCK progression
// Traits: Agile
```

**Stats:**
- **High:** DEX 14, LUCK 14
- **Low:** STR 6, CON 8
- **Affinity:** +20% LUCK progression

**Unique Abilities:**
- **Lucky**: Reroll failed critical rolls once per encounter
- **Small Size**: +10% dodge chance (planned Phase 1.5)

**Why Play Halfling?**
- **Rogue Build**: High DEX + LUCK for critical hits
- **Loot Farming**: LUCK increases rare drops
- **Evasion**: High dodge, hard to hit
- **Tradeoff:** Weakest race (low STR/CON)

---

### Gnome
**Archetype:** Arcane Scholar

```java
GNOME = new Race("gnome", "Gnome",
    "Clever and inventive, gnomes excel at magic and tinkering");
// Base Stats: +4 INT, +4 WIS, -3 STR, -3 CON
// Affinities: +25% INT progression
// Traits: Scholar
```

**Stats:**
- **High:** INT 14, WIS 14
- **Low:** STR 7, CON 7
- **Affinity:** +25% INT progression (best mage race)

**Unique Abilities:**
- **Arcane Affinity**: Spells cost 10% less mana
- **Inventor**: +15% crafting speed for enchanted items

**Why Play Gnome?**
- **Mage Build**: Highest INT scaling in game
- **Enchanter**: Natural bonus for magical crafting
- **Wisdom**: High WIS for healing magic
- **Tradeoff:** Weakest in melee (low STR/CON)

---

### Half-Elf
**Archetype:** Balanced Hybrid

```java
HALF_ELF = new Race("half_elf", "Half-Elf",
    "Blending human versatility with elven grace");
// Base Stats: Slightly above baseline (+1-2 to most stats)
// Affinities: Balanced (no exceptional growth)
```

**Stats:**
- **Balanced:** All stats 10-12 (no major weaknesses)
- **Affinities:** 1.1x all stats (generalist)

**Why Play Half-Elf?**
- **Versatility**: Like Human but with slight stat boosts
- **No Weaknesses**: Can play any role competently
- **Roleplay**: Mixed heritage for interesting stories
- **Tradeoff:** No exceptional strengths

---

### Dragonborn
**Archetype:** Draconic Warrior/Paladin

```java
DRAGONBORN = new Race("dragonborn", "Dragonborn",
    "Dragon-blooded humanoids with innate fire magic");
// Base Stats: +4 STR, +3 CHA, +2 CON
// Affinities: +15% STR progression
```

**Stats:**
- **High:** STR 14, CHA 13, CON 12
- **Affinities:** +15% STR, +10% CHA

**Unique Abilities:**
- **Fire Resistance**: 50% reduction to fire damage
- **Dragon Breath**: Once per day, breathe fire for 2× STR damage (Phase 1.5)

**Why Play Dragonborn?**
- **Paladin Build**: High STR + CHA for combat + leadership
- **Fire Immunity**: Tanking fire mages/dragons
- **Breath Weapon**: Unique AOE damage ability
- **Tradeoff:** No major weaknesses, but no exceptional scaling

---

## API Reference

### Constructor

```java
public Race(String id, String name, String description)
```

### Base Stats

```java
public void setBaseStat(CoreStat stat, int value)
public int getBaseStat(CoreStat stat)
```

### Stat Affinities

```java
public void setStatAffinity(CoreStat stat, double multiplier)
public double getStatAffinity(CoreStat stat)
```

### Natural Traits

```java
public void addNaturalTrait(Trait trait)
public List<Trait> getNaturalTraits()
```

### Unique Abilities

```java
public void addUniqueAbility(String ability)
public List<String> getUniqueAbilities()
```

## Integration with Character

```java
// Character.java constructor
public Character(String id, String name, Race race) {
    this.race = race;
    
    // Apply base stats from race
    for (CoreStat stat : CoreStat.values()) {
        this.stats.put(stat, race.getBaseStat(stat));
    }
    
    // Apply natural traits
    for (Trait trait : race.getNaturalTraits()) {
        this.addTrait(trait);
    }
}
```

## Testing

See [CharacterTest.md](../../../../../test/java/org/adventure/CharacterTest.md) for race initialization tests.

**Status:** ✅ Complete - 8 races implemented, tested via Character class

## References

- **Design Docs**: `docs/characters_stats_traits_skills.md` → Bestiary Details
- **Related Classes**: [Character.md](Character.md), [Trait.md](Trait.md)

---

**Last Updated:** Phase 1.3 Implementation (November 2025)  
**Status:** ✅ Complete - 8 playable races
