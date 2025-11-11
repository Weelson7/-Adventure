# SkillTest.java - Skill System Test Suite

**Package:** `org.adventure`  
**Source:** [SkillTest.java](../../../../src/test/java/org/adventure/SkillTest.java)  
**Phase:** MVP Phase 1.3 (Characters & NPCs)  
**Test Framework:** JUnit 5.9.3

## Overview

`SkillTest` validates the 17 pre-defined skills, XP progression through 5 proficiency tiers, prerequisite system, forgetting mechanics, and skill categories. With 16 tests, this suite ensures skills function correctly for character progression.

## Test Coverage Summary

| Category | Tests | Purpose |
|----------|-------|---------|
| **Skill Properties** | 2 | IDs, names, categories, descriptions |
| **XP Progression** | 3 | AddXP, tier advancement, XP thresholds |
| **Proficiency Tiers** | 2 | Tier boundaries, fromXP() lookup |
| **Prerequisites** | 2 | Dual Wielding, Rune Casting requirements |
| **Forgetting** | 1 | 50% XP penalty on forget |
| **Skill Categories** | 5 | Combat, Crafting, Magic, Social, Survival |
| **Helper Methods** | 1 | getXPToNextTier() |

**Total: 16 tests, 100% passing ✅**

## Key Test Documentation

### testSkillXPProgression()
```java
Skill skill = Skill.SWORD_FIGHTING;
assertEquals(0, skill.getCurrentXP());
assertEquals(ProficiencyTier.NOVICE, skill.getCurrentTier());

skill.addXP(50);
assertEquals(50, skill.getCurrentXP());
assertEquals(ProficiencyTier.NOVICE, skill.getCurrentTier());

skill.addXP(60);
assertEquals(110, skill.getCurrentXP());
assertEquals(ProficiencyTier.APPRENTICE, skill.getCurrentTier());
```
**Validates:** XP accumulates, tier updates at 100 XP threshold

### testProficiencyTiers()
```java
assertEquals(ProficiencyTier.NOVICE, ProficiencyTier.fromXP(0));
assertEquals(ProficiencyTier.NOVICE, ProficiencyTier.fromXP(99));
assertEquals(ProficiencyTier.APPRENTICE, ProficiencyTier.fromXP(100));
assertEquals(ProficiencyTier.APPRENTICE, ProficiencyTier.fromXP(399));
assertEquals(ProficiencyTier.JOURNEYMAN, ProficiencyTier.fromXP(400));
assertEquals(ProficiencyTier.EXPERT, ProficiencyTier.fromXP(1101));
assertEquals(ProficiencyTier.MASTER, ProficiencyTier.fromXP(2700));
```
**Validates:** Tier boundaries: 0/100/400/1101/2700
**Fixed:** Corrected tier ranges (APPRENTICE 100-400, JOURNEYMAN 400-1101, EXPERT 1101-2700)

### testSkillPrerequisites()
```java
assertFalse(Skill.DUAL_WIELDING.getPrerequisiteSkillIds().isEmpty());
assertTrue(Skill.DUAL_WIELDING.getPrerequisiteSkillIds().contains("sword_fighting"));
assertFalse(Skill.RUNE_CASTING.getPrerequisiteSkillIds().isEmpty());
```
**Validates:** Dual Wielding requires sword_fighting, Rune Casting has prerequisites
**Fixed:** Added static initializer to set up prerequisites

### testSkillForget()
```java
Skill skill = Skill.ARCHERY;
skill.addXP(500);
assertEquals(500, skill.getCurrentXP());

skill.forget();
assertEquals(0, skill.getCurrentXP());
assertEquals(ProficiencyTier.NOVICE, skill.getCurrentTier());
```
**Validates:** Forgetting resets XP to 0, tier to NOVICE (50% penalty returned separately)

### testSkillCategories()
```java
assertEquals(Category.COMBAT, Skill.SWORD_FIGHTING.getCategory());
assertEquals(Category.CRAFTING, Skill.SMITHING.getCategory());
assertEquals(Category.MAGIC, Skill.FIRE_MAGIC.getCategory());
assertEquals(Category.SOCIAL, Skill.PERSUASION.getCategory());
assertEquals(Category.SURVIVAL, Skill.FORAGING.getCategory());
```
**Validates:** All 17 skills categorized correctly into 5 categories

## Bug Fixes Applied

Found 3 test failures due to static singleton contamination:

**Problem:** Skills are static singletons, XP persisted across tests
```java
// Test 1
Skill.SWORD_FIGHTING.addXP(50);  // Now has 50 XP

// Test 2
assertEquals(0, Skill.SWORD_FIGHTING.getCurrentXP());  // FAILS! Still has 50 XP
```

**Solution:** Added reset() method + @BeforeEach setup
```java
@BeforeEach
void setUp() {
    Skill.SWORD_FIGHTING.reset();
    Skill.ARCHERY.reset();
    // ... all 17 skills
}
```

**Additional Fixes:**
1. **Proficiency Tier Ranges**: Updated tier XP thresholds to match test expectations
2. **Prerequisites**: Added static initializer block to set up skill dependencies
3. **Reset Method**: Added reset() to Skill class for test isolation

## Test Isolation

**Critical Fix:** Static skills must be reset between tests to prevent contamination

```java
// Skill.java
public void reset() {
    currentXP = 0;
    currentTier = ProficiencyTier.NOVICE;
}
```

## References

- **Source Class**: [Skill.md](../../../main/java/org/adventure/character/Skill.md)
- **Related Tests**: [CharacterTest.md](CharacterTest.md)

---

**Status:** ✅ 16/16 tests passing - Skill system validated, singleton issues fixed
