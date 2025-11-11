package org.adventure;

import org.adventure.character.Character;
import org.adventure.character.Character.CoreStat;
import org.adventure.character.Race;
import org.adventure.character.Skill;
import org.adventure.character.Trait;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Character class.
 * Validates stat progression, soft-cap enforcement, derived stats, traits, skills, and mana system.
 */
class CharacterTest {

    @Test
    void testCharacterCreation() {
        Character character = new Character("char-001", "Alyn", Race.HUMAN);

        assertEquals("char-001", character.getId(), "ID should match");
        assertEquals("Alyn", character.getName(), "Name should match");
        assertEquals(Race.HUMAN, character.getRace(), "Race should match");
        
        // Verify base stats from human race
        assertEquals(10, character.getStat(CoreStat.STRENGTH), "STR should be 10 (human baseline)");
        assertEquals(12, character.getStat(CoreStat.CHARISMA), "CHA should be 12 (human bonus)");
        
        // Verify initial mana
        int expectedMaxMana = Character.BASE_MANA + character.getStat(CoreStat.INTELLIGENCE) * Character.MANA_PER_STAT_POINT;
        assertEquals(expectedMaxMana, character.getMaxMana(), "Max mana should be calculated from INT");
        assertEquals(expectedMaxMana, character.getCurrentMana(), "Current mana should start at max");
    }

    @Test
    void testStatProgression() {
        Character character = new Character("char-001", "Bran", Race.HUMAN);
        int initialStr = character.getStat(CoreStat.STRENGTH);

        // Add 10 stat points
        double actualGain = character.addStatProgress(CoreStat.STRENGTH, 10);

        assertTrue(actualGain > 0, "Should gain some stat points");
        assertTrue(character.getStat(CoreStat.STRENGTH) > initialStr, "STR should increase");
    }

    @Test
    void testStatSoftCap() {
        Character character = new Character("char-001", "Cara", Race.HUMAN);

        // Set STR to near soft cap (50)
        character.setStat(CoreStat.STRENGTH, 45);

        // Add 10 points
        double gain1 = character.addStatProgress(CoreStat.STRENGTH, 10);

        // Set STR to above soft cap (60)
        character.setStat(CoreStat.STRENGTH, 60);

        // Add 10 points again
        double gain2 = character.addStatProgress(CoreStat.STRENGTH, 10);

        assertTrue(gain1 > gain2, "Gains should diminish above soft cap");
    }

    @Test
    void testStatHardCap() {
        Character character = new Character("char-001", "Dain", Race.HUMAN);

        // Set STR to hard cap
        character.setStat(CoreStat.STRENGTH, Character.HARD_CAP);

        // Try to add more
        double gain = character.addStatProgress(CoreStat.STRENGTH, 100);

        assertEquals(0.0, gain, 0.001, "No gain at hard cap");
        assertEquals(Character.HARD_CAP, character.getStat(CoreStat.STRENGTH), "Should stay at hard cap");
    }

    @Test
    void testDerivedStatCalculation() {
        Character character = new Character("char-001", "Elara", Race.HUMAN);

        // Set specific stats
        character.setStat(CoreStat.INTELLIGENCE, 20);
        character.setStat(CoreStat.CONSTITUTION, 15);
        character.setStat(CoreStat.STRENGTH, 18);

        // Verify derived stats
        assertEquals(50.0, character.getDerivedStat("maxMana"), 0.001, "Max mana should be 10 + 20*2 = 50");
        assertEquals(3.0, character.getDerivedStat("manaRegen"), 0.001, "Mana regen should be 1 + floor(20/10) = 3");
        assertEquals(125.0, character.getDerivedStat("maxHealth"), 0.001, "Max health should be 50 + 15*5 = 125");
        assertEquals(9.0, character.getDerivedStat("meleeDamageBonus"), 0.001, "Melee damage should be 18/2 = 9");
    }

    @Test
    void testTraitEffects() {
        Character character = new Character("char-001", "Finn", Race.HUMAN);

        // Set initial STR
        character.setStat(CoreStat.STRENGTH, 30);

        // Add Fast Learner trait (+20% stat progression)
        character.addTrait(Trait.FAST_LEARNER);

        double gain = character.addStatProgress(CoreStat.STRENGTH, 10);

        // Expected: 10 / (1 + (30/50)^2) * 1.2 ≈ 7.35 * 1.2 ≈ 8.82
        assertTrue(gain > 8.5 && gain < 9.5, "Fast Learner should boost stat gain by 20%");
    }

    @Test
    void testTraitAddition() {
        Character character = new Character("char-001", "Gwen", Race.HUMAN);

        assertFalse(character.hasTrait(Trait.ROBUST), "Should not have Robust trait initially");

        character.addTrait(Trait.ROBUST);

        assertTrue(character.hasTrait(Trait.ROBUST), "Should have Robust trait after adding");
        assertTrue(character.hasTrait("robust"), "Should find trait by ID");
    }

    @Test
    void testSkillAcquisition() {
        Character character = new Character("char-001", "Hale", Race.HUMAN);

        assertFalse(character.hasSkill("sword_fighting"), "Should not have Sword Fighting initially");

        character.addSkill(Skill.SWORD_FIGHTING);

        assertTrue(character.hasSkill("sword_fighting"), "Should have Sword Fighting after adding");
        assertNotNull(character.getSkill("sword_fighting"), "Should retrieve skill by ID");
    }

    @Test
    void testSkillXPProgression() {
        Character character = new Character("char-001", "Iris", Race.HUMAN);

        Skill skill = Skill.SWORD_FIGHTING;
        character.addSkill(skill);

        assertEquals(0, skill.getCurrentXP(), "Skill should start with 0 XP");

        character.addSkillXP("sword_fighting", 50);

        assertEquals(50, skill.getCurrentXP(), "Skill should have 50 XP");
    }

    @Test
    void testSkillXPTraitModifier() {
        Character character = new Character("char-001", "Jace", Race.HUMAN);

        character.addTrait(Trait.FAST_LEARNER); // +30% skill XP
        character.addSkill(Skill.SMITHING);

        character.addSkillXP("smithing", 100);

        Skill skill = character.getSkill("smithing");
        assertTrue(skill.getCurrentXP() >= 130, "Fast Learner should boost skill XP by 30%");
    }

    @Test
    void testManaSpending() {
        Character character = new Character("char-001", "Kara", Race.HUMAN);

        int initialMana = character.getCurrentMana();

        assertTrue(character.spendMana(10), "Should spend 10 mana");
        assertEquals(initialMana - 10, character.getCurrentMana(), "Mana should decrease by 10");

        assertFalse(character.spendMana(9999), "Should not spend more mana than available");
    }

    @Test
    void testManaRegeneration() {
        Character character = new Character("char-001", "Leon", Race.HUMAN);

        // Set INT to 20 for higher regen (manaRegen = 1 + floor(20/10) = 3)
        character.setStat(CoreStat.INTELLIGENCE, 20);
        character.setCurrentMana(20);

        character.regenerateMana();

        assertEquals(23, character.getCurrentMana(), "Mana should regenerate by 3");
    }

    @Test
    void testManaRegenerationCap() {
        Character character = new Character("char-001", "Mara", Race.HUMAN);

        int maxMana = character.getMaxMana();
        character.setCurrentMana(maxMana - 1);

        character.regenerateMana();

        assertEquals(maxMana, character.getCurrentMana(), "Mana should not exceed max");
    }

    @Test
    void testInventoryManagement() {
        Character character = new Character("char-001", "Nyx", Race.HUMAN);

        assertFalse(character.hasItem("sword-001"), "Should not have sword initially");

        character.addItem("sword-001");

        assertTrue(character.hasItem("sword-001"), "Should have sword after adding");
        assertTrue(character.getInventoryItemIds().contains("sword-001"), "Inventory should contain sword");

        character.removeItem("sword-001");

        assertFalse(character.hasItem("sword-001"), "Should not have sword after removing");
    }

    @Test
    void testStatClamping() {
        Character character = new Character("char-001", "Orin", Race.HUMAN);

        // Try to set above hard cap
        character.setStat(CoreStat.STRENGTH, 250);
        assertEquals(Character.HARD_CAP, character.getStat(CoreStat.STRENGTH), "Should clamp to hard cap");

        // Try to set negative
        character.setStat(CoreStat.STRENGTH, -10);
        assertEquals(0, character.getStat(CoreStat.STRENGTH), "Should clamp to 0");
    }

    @Test
    void testStatDeterminism() {
        // Same race and initial stats → same progression
        Character char1 = new Character("char-001", "Pax", Race.HUMAN);
        Character char2 = new Character("char-002", "Quin", Race.HUMAN);

        char1.setStat(CoreStat.STRENGTH, 30);
        char2.setStat(CoreStat.STRENGTH, 30);

        double gain1 = char1.addStatProgress(CoreStat.STRENGTH, 10);
        double gain2 = char2.addStatProgress(CoreStat.STRENGTH, 10);

        assertEquals(gain1, gain2, 0.001, "Same conditions should produce same stat gains");
    }
}
