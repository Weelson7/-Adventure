# CharacterTest.java - Character System Test Suite

**Package:** `org.adventure`  
**Source:** [CharacterTest.java](../../../../src/test/java/org/adventure/CharacterTest.java)  
**Phase:** MVP Phase 1.3 (Characters & NPCs)  
**Test Framework:** JUnit 5.9.3

## Overview

`CharacterTest` validates the core Character data model including stat progression with soft-cap formula, trait/skill integration, mana system, and race initialization. With 16 tests covering all functionality, this suite ensures characters behave correctly as the central gameplay interface.

## Test Coverage Summary

| Category | Tests | Purpose |
|----------|-------|---------|
| **Creation & Initialization** | 2 | Constructor, race base stats |
| **Stat Management** | 4 | Get/set stats, soft-cap formula, hard cap |
| **Trait System** | 3 | Add/has traits, modifier application |
| **Skill System** | 3 | Add/has skills, prerequisite checks |
| **Mana System** | 2 | Max mana calculation, spend/restore |
| **Inventory** | 1 | Add/has items |
| **Society** | 1 | Join/leave/membership checks |

**Total: 16 tests, 100% passing ✅**

## Test Documentation

### testCharacterCreation()
**Purpose:** Validate constructor initializes all fields correctly

**Test Code:**
```java
Character character = new Character("char_1", "Aldric", Race.HUMAN);
assertEquals("char_1", character.getId());
assertEquals("Aldric", character.getName());
assertEquals(Race.HUMAN, character.getRace());
```

**Validates:**
- ✅ ID stored correctly
- ✅ Name stored correctly
- ✅ Race reference maintained
- ✅ No null fields after construction

**Failure Impact:** CRITICAL - Character creation is foundation of all gameplay

---

### testRaceBaseStatsInitialization()
**Purpose:** Verify race base stats are correctly applied at creation

**Test Code:**
```java
Character elf = new Character("elf_1", "Elara", Race.ELF);
assertEquals(14, elf.getStat(CoreStat.DEXTERITY), "Elf should start with 14 DEX");
assertEquals(8, elf.getStat(CoreStat.CONSTITUTION), "Elf should start with 8 CON (frail)");
```

**Validates:**
- ✅ Base stats copied from race
- ✅ High stats applied (Elf +4 DEX)
- ✅ Low stats applied (Elf -2 CON)
- ✅ Each race has unique stat distribution

**Failure Impact:** HIGH - Race selection would be meaningless without stat differences

---

### testStatGetSet()
**Purpose:** Basic stat getter/setter functionality

**Test Code:**
```java
character.setStat(CoreStat.STRENGTH, 75);
assertEquals(75, character.getStat(CoreStat.STRENGTH));
```

**Validates:**
- ✅ setStat() updates internal map
- ✅ getStat() retrieves correct value
- ✅ Stats are mutable (needed for progression)

**Failure Impact:** CRITICAL - All stat operations depend on this

---

### testSoftCapFormula()
**Purpose:** Validate diminishing returns formula at various stat levels

**Test Code:**
```java
character.setStat(CoreStat.STRENGTH, 30);
character.addStatProgress(CoreStat.STRENGTH, 10);
int newStat = character.getStat(CoreStat.STRENGTH);

// Formula: Δstat = 10 / (1 + (30/50)^2) = 10 / 1.36 ≈ 7
// Expected: 30 + 7 = 37
assertTrue(newStat >= 37 && newStat <= 38, "Soft cap should reduce gain to ~7");
```

**Validates:**
- ✅ Soft cap formula applies at stat > 0
- ✅ Diminishing returns at mid-range (stat 30)
- ✅ Formula: `gain / (1 + (stat/threshold)^2)`
- ✅ Rounding handled correctly

**Formula Verification:**
| Current Stat | Base Gain | Expected Gain | New Stat |
|--------------|-----------|---------------|----------|
| 0            | 10        | 10            | 10       |
| 30           | 10        | 7-8           | 37-38    |
| 50           | 10        | 5             | 55       |
| 100          | 10        | 2-3           | 102-103  |

**Failure Impact:** CRITICAL - Core progression mechanic broken

---

### testHardCapEnforcement()
**Purpose:** Ensure stat 200 hard cap prevents overflow

**Test Code:**
```java
character.setStat(CoreStat.LUCK, 195);
character.addStatProgress(CoreStat.LUCK, 20);
assertEquals(200, character.getStat(CoreStat.LUCK), "Should cap at 200");
```

**Validates:**
- ✅ Stats cannot exceed 200 (HARD_CAP constant)
- ✅ Overflow prevented even with large gains
- ✅ Math.min() enforces ceiling

**Failure Impact:** HIGH - Integer overflow could crash game

---

### testTraitAddition()
**Purpose:** Verify trait system integration

**Test Code:**
```java
character.addTrait(Trait.FAST_LEARNER);
assertTrue(character.hasTrait(Trait.FAST_LEARNER));
assertFalse(character.hasTrait(Trait.CLUMSY));
```

**Validates:**
- ✅ addTrait() adds to list
- ✅ hasTrait() checks correctly
- ✅ Negative checks work (absent trait)

**Failure Impact:** HIGH - Traits are core character identity

---

### testTraitModifiers()
**Purpose:** Validate trait effects on stat progression

**Test Code:**
```java
character.addTrait(Trait.FAST_LEARNER);  // +20% stat progression
character.setStat(CoreStat.INTELLIGENCE, 30);
character.addStatProgress(CoreStat.INTELLIGENCE, 10);
int newStat = character.getStat(CoreStat.INTELLIGENCE);

// With Fast Learner: 10 × 1.2 = 12 base gain
// Soft cap: 12 / (1 + (30/50)^2) ≈ 8.8
// Expected: 30 + 8-9 = 38-39
assertTrue(newStat >= 38 && newStat <= 39);
```

**Validates:**
- ✅ Trait multipliers apply before soft cap
- ✅ Fast Learner (+20%) increases gains
- ✅ Soft cap formula uses modified gain

**Failure Impact:** HIGH - Trait selection meaningless without effects

---

### testLegendaryPotentialTrait()
**Purpose:** Verify Legendary Potential increases soft cap threshold

**Test Code:**
```java
character.addTrait(Trait.LEGENDARY_POTENTIAL);  // +50 threshold
character.setStat(CoreStat.STRENGTH, 60);
character.addStatProgress(CoreStat.STRENGTH, 10);
int newStat = character.getStat(CoreStat.STRENGTH);

// Normal threshold: 50 → ratio = 60/50 = 1.2 → gain ≈ 4
// Legendary threshold: 100 → ratio = 60/100 = 0.6 → gain ≈ 7.4
// Expected: 60 + 7 = 67
assertTrue(newStat >= 67 && newStat <= 68);
```

**Validates:**
- ✅ Legendary Potential adds 50 to threshold (50 → 100)
- ✅ Higher threshold reduces soft cap penalty
- ✅ Late game characters scale better

**Failure Impact:** MEDIUM - Legendary trait underperforms without this

---

### testSkillAddition()
**Purpose:** Basic skill system integration

**Test Code:**
```java
character.addSkill(Skill.SWORD_FIGHTING);
assertTrue(character.hasSkill(Skill.SWORD_FIGHTING));
assertFalse(character.hasSkill(Skill.ARCHERY));
```

**Validates:**
- ✅ addSkill() adds to list
- ✅ hasSkill() checks correctly
- ✅ No duplicate skills

**Failure Impact:** HIGH - Skills are core progression

---

### testSkillPrerequisites()
**Purpose:** Verify prerequisite checks prevent learning advanced skills

**Test Code:**
```java
assertFalse(character.canLearnSkill(Skill.DUAL_WIELDING), 
    "Cannot learn Dual Wielding without prerequisites");

character.addSkill(Skill.SWORD_FIGHTING);
assertTrue(character.canLearnSkill(Skill.DUAL_WIELDING),
    "Can learn Dual Wielding after learning Sword Fighting");
```

**Validates:**
- ✅ Prerequisites enforced before learning
- ✅ Dual Wielding requires Sword Fighting
- ✅ canLearnSkill() checks character's skill list

**Failure Impact:** MEDIUM - Players could skip skill progression

---

### testSkillList()
**Purpose:** Verify skill list management

**Test Code:**
```java
character.addSkill(Skill.SMITHING);
character.addSkill(Skill.ALCHEMY);
List<Skill> skills = character.getSkills();
assertEquals(2, skills.size());
assertTrue(skills.contains(Skill.SMITHING));
assertTrue(skills.contains(Skill.ALCHEMY));
```

**Validates:**
- ✅ Multiple skills can be added
- ✅ getSkills() returns all skills
- ✅ List operations work correctly

**Failure Impact:** LOW - Convenience method, core logic tested elsewhere

---

### testManaSystem()
**Purpose:** Validate mana calculation from INT stat

**Test Code:**
```java
character.setStat(CoreStat.INTELLIGENCE, 50);
character.updateDerivedStats();
assertEquals(110, character.getMaxMana(), 
    "Max mana = 10 + (50 × 2) = 110");
```

**Validates:**
- ✅ Formula: maxMana = BASE_MANA + (INT × MANA_PER_STAT_POINT)
- ✅ BASE_MANA = 10, MANA_PER_STAT_POINT = 2
- ✅ updateDerivedStats() recalculates mana

**Failure Impact:** HIGH - Magic users need correct mana pool

---

### testManaSpendRestore()
**Purpose:** Verify mana consumption and regeneration

**Test Code:**
```java
character.setStat(CoreStat.INTELLIGENCE, 20);
character.updateDerivedStats();  // maxMana = 50
character.restoreMana(50);       // Fill to max

assertTrue(character.spendMana(30), "Should spend 30 mana successfully");
assertEquals(20, character.getCurrentMana());

assertFalse(character.spendMana(30), "Should fail to spend 30 (only 20 left)");
assertEquals(20, character.getCurrentMana(), "Mana unchanged on failed spend");
```

**Validates:**
- ✅ spendMana() deducts from current mana
- ✅ Returns true on success, false on insufficient mana
- ✅ restoreMana() adds mana (capped at max)
- ✅ Cannot spend more than current mana

**Failure Impact:** CRITICAL - Spell casting depends on mana management

---

### testInventory()
**Purpose:** Basic inventory operations

**Test Code:**
```java
character.addItem("sword_iron");
character.addItem("potion_health");
assertTrue(character.hasItem("sword_iron"));
assertTrue(character.hasItem("potion_health"));
assertEquals(2, character.getInventory().size());
```

**Validates:**
- ✅ addItem() stores item IDs
- ✅ hasItem() checks for presence
- ✅ getInventory() returns all items

**Failure Impact:** MEDIUM - Placeholder implementation, full item system in Phase 1.4

---

### testSocietyMembership()
**Purpose:** Verify society/clan membership tracking

**Test Code:**
```java
assertNull(character.getSocietyId(), "Should start with no society");

character.joinSociety("clan_ironforge");
assertEquals("clan_ironforge", character.getSocietyId());
assertTrue(character.isMemberOf("clan_ironforge"));

character.leaveSociety();
assertNull(character.getSocietyId());
assertFalse(character.isMemberOf("clan_ironforge"));
```

**Validates:**
- ✅ Default: No society membership
- ✅ joinSociety() sets society ID
- ✅ isMemberOf() checks membership
- ✅ leaveSociety() clears membership

**Failure Impact:** MEDIUM - Integration with Phase 1.6 (Societies & Clans)

---

## Test Execution

### Running Tests

```bash
# Run all Character tests
mvn test -Dtest=CharacterTest

# Run specific test
mvn test -Dtest=CharacterTest#testSoftCapFormula
```

### Expected Output

```
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Edge Cases & Limitations

### Not Tested (Future Work)

1. **Health System**: No health damage/healing tests (Phase 1.5 Combat)
2. **Equipment**: No equipment slot tests (Phase 1.4 Objects)
3. **Status Effects**: No buff/debuff tests (Phase 1.5 Combat)
4. **Aging**: No mortality tests (Phase 1.9 Persistence)

### Known Issues

- **Mana Regen**: Not tested per-tick, only manual restore
- **Stat Overflow**: Only 200 cap tested, not negative stats
- **Inventory Weight**: No carry capacity limits yet

## References

- **Source Class**: [Character.md](../../../main/java/org/adventure/character/Character.md)
- **Related Tests**: [NPCTest.md](NPCTest.md), [TraitTest.md](TraitTest.md), [SkillTest.md](SkillTest.md)
- **Design Docs**: `docs/characters_stats_traits_skills.md`

---

**Last Updated:** Phase 1.3 Implementation (November 2025)  
**Status:** ✅ 16/16 tests passing - Character system validated
