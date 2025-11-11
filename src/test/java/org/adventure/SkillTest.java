package org.adventure;

import org.adventure.character.Skill;
import org.adventure.character.Skill.Category;
import org.adventure.character.Skill.ProficiencyTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Skill class.
 * Validates skill XP progression, proficiency tiers, categories, and prerequisites.
 */
class SkillTest {

    @BeforeEach
    void setUp() {
        // Reset all skills to initial state before each test
        Skill.SWORD_FIGHTING.reset();
        Skill.ARCHERY.reset();
        Skill.SHIELD_DEFENSE.reset();
        Skill.DUAL_WIELDING.reset();
        Skill.SMITHING.reset();
        Skill.ALCHEMY.reset();
        Skill.ENCHANTING.reset();
        Skill.CARPENTRY.reset();
        Skill.FIRE_MAGIC.reset();
        Skill.ICE_MAGIC.reset();
        Skill.HEALING_MAGIC.reset();
        Skill.RUNE_CASTING.reset();
        Skill.PERSUASION.reset();
        Skill.LEADERSHIP.reset();
        Skill.INTIMIDATION.reset();
        Skill.FORAGING.reset();
        Skill.TRACKING.reset();
        Skill.CAMPING.reset();
    }

    @Test
    void testSkillBasicProperties() {
        assertEquals("sword_fighting", Skill.SWORD_FIGHTING.getId(), "ID should match");
        assertEquals("Sword Fighting", Skill.SWORD_FIGHTING.getName(), "Name should match");
        assertEquals(Category.COMBAT, Skill.SWORD_FIGHTING.getCategory(), "Category should be COMBAT");
        assertNotNull(Skill.SWORD_FIGHTING.getDescription(), "Description should not be null");
    }

    @Test
    void testSkillXPProgression() {
        Skill skill = Skill.SWORD_FIGHTING;
        
        assertEquals(0, skill.getCurrentXP(), "Skill should start with 0 XP");
        assertEquals(ProficiencyTier.NOVICE, skill.getCurrentTier(), "Should start at NOVICE");

        skill.addXP(50);

        assertEquals(50, skill.getCurrentXP(), "XP should be 50");
        assertEquals(ProficiencyTier.NOVICE, skill.getCurrentTier(), "Should still be NOVICE");

        skill.addXP(60);

        assertEquals(110, skill.getCurrentXP(), "XP should be 110");
        assertEquals(ProficiencyTier.APPRENTICE, skill.getCurrentTier(), "Should be APPRENTICE at 110 XP");
    }

    @Test
    void testProficiencyTiers() {
        assertEquals(ProficiencyTier.NOVICE, ProficiencyTier.fromXP(0), "0 XP should be NOVICE");
        assertEquals(ProficiencyTier.NOVICE, ProficiencyTier.fromXP(99), "99 XP should be NOVICE");
        assertEquals(ProficiencyTier.APPRENTICE, ProficiencyTier.fromXP(100), "100 XP should be APPRENTICE");
        assertEquals(ProficiencyTier.APPRENTICE, ProficiencyTier.fromXP(399), "399 XP should be APPRENTICE");
        assertEquals(ProficiencyTier.JOURNEYMAN, ProficiencyTier.fromXP(400), "400 XP should be JOURNEYMAN");
        assertEquals(ProficiencyTier.JOURNEYMAN, ProficiencyTier.fromXP(1100), "1100 XP should be JOURNEYMAN");
        assertEquals(ProficiencyTier.EXPERT, ProficiencyTier.fromXP(1101), "1101 XP should be EXPERT");
        assertEquals(ProficiencyTier.EXPERT, ProficiencyTier.fromXP(2699), "2699 XP should be EXPERT");
        assertEquals(ProficiencyTier.MASTER, ProficiencyTier.fromXP(2700), "2700 XP should be MASTER");
    }

    @Test
    void testSkillForget() {
        Skill skill = Skill.ARCHERY;
        
        skill.addXP(500);
        assertEquals(500, skill.getCurrentXP());

        skill.forget();

        assertEquals(0, skill.getCurrentXP(), "Forgetting should reset XP to 0");
        assertEquals(ProficiencyTier.NOVICE, skill.getCurrentTier(), "Should be back to NOVICE");
    }

    @Test
    void testCombatSkills() {
        assertEquals(Category.COMBAT, Skill.SWORD_FIGHTING.getCategory());
        assertEquals(Category.COMBAT, Skill.ARCHERY.getCategory());
        assertEquals(Category.COMBAT, Skill.SHIELD_DEFENSE.getCategory());
        assertEquals(Category.COMBAT, Skill.DUAL_WIELDING.getCategory());
    }

    @Test
    void testCraftingSkills() {
        assertEquals(Category.CRAFTING, Skill.SMITHING.getCategory());
        assertEquals(Category.CRAFTING, Skill.ALCHEMY.getCategory());
        assertEquals(Category.CRAFTING, Skill.ENCHANTING.getCategory());
        assertEquals(Category.CRAFTING, Skill.CARPENTRY.getCategory());
    }

    @Test
    void testMagicSkills() {
        assertEquals(Category.MAGIC, Skill.FIRE_MAGIC.getCategory());
        assertEquals(Category.MAGIC, Skill.ICE_MAGIC.getCategory());
        assertEquals(Category.MAGIC, Skill.HEALING_MAGIC.getCategory());
        assertEquals(Category.MAGIC, Skill.RUNE_CASTING.getCategory());
    }

    @Test
    void testSocialSkills() {
        assertEquals(Category.SOCIAL, Skill.PERSUASION.getCategory());
        assertEquals(Category.SOCIAL, Skill.LEADERSHIP.getCategory());
        assertEquals(Category.SOCIAL, Skill.INTIMIDATION.getCategory());
    }

    @Test
    void testSurvivalSkills() {
        assertEquals(Category.SURVIVAL, Skill.FORAGING.getCategory());
        assertEquals(Category.SURVIVAL, Skill.TRACKING.getCategory());
        assertEquals(Category.SURVIVAL, Skill.CAMPING.getCategory());
    }

    @Test
    void testSkillPrerequisites() {
        // Dual Wielding requires Sword Fighting
        assertFalse(Skill.DUAL_WIELDING.getPrerequisiteSkillIds().isEmpty(), "Dual Wielding should have prerequisites");
        assertTrue(Skill.DUAL_WIELDING.getPrerequisiteSkillIds().contains("sword_fighting"), 
                  "Dual Wielding should require sword_fighting");

        // Rune Casting requires Fire Magic or Ice Magic
        assertFalse(Skill.RUNE_CASTING.getPrerequisiteSkillIds().isEmpty(), "Rune Casting should have prerequisites");
    }

    @Test
    void testSkillWithoutPrerequisites() {
        // Basic skills have no prerequisites
        assertTrue(Skill.SWORD_FIGHTING.getPrerequisiteSkillIds().isEmpty(), "Sword Fighting should have no prerequisites");
        assertTrue(Skill.SMITHING.getPrerequisiteSkillIds().isEmpty(), "Smithing should have no prerequisites");
        assertTrue(Skill.PERSUASION.getPrerequisiteSkillIds().isEmpty(), "Persuasion should have no prerequisites");
    }

    @Test
    void testAllSkillsHaveUniqueIds() {
        Skill[] skills = {
            Skill.SWORD_FIGHTING, Skill.ARCHERY, Skill.SHIELD_DEFENSE, Skill.DUAL_WIELDING,
            Skill.SMITHING, Skill.ALCHEMY, Skill.ENCHANTING, Skill.CARPENTRY,
            Skill.FIRE_MAGIC, Skill.ICE_MAGIC, Skill.HEALING_MAGIC, Skill.RUNE_CASTING,
            Skill.PERSUASION, Skill.LEADERSHIP, Skill.INTIMIDATION,
            Skill.FORAGING, Skill.TRACKING, Skill.CAMPING
        };

        for (int i = 0; i < skills.length; i++) {
            for (int j = i + 1; j < skills.length; j++) {
                assertNotEquals(skills[i].getId(), skills[j].getId(), 
                               "Skills should have unique IDs");
            }
        }
    }

    @Test
    void testSkillDescriptionsNotEmpty() {
        Skill[] skills = {
            Skill.SWORD_FIGHTING, Skill.ARCHERY, Skill.SHIELD_DEFENSE, Skill.DUAL_WIELDING,
            Skill.SMITHING, Skill.ALCHEMY, Skill.ENCHANTING, Skill.CARPENTRY,
            Skill.FIRE_MAGIC, Skill.ICE_MAGIC, Skill.HEALING_MAGIC, Skill.RUNE_CASTING,
            Skill.PERSUASION, Skill.LEADERSHIP, Skill.INTIMIDATION,
            Skill.FORAGING, Skill.TRACKING, Skill.CAMPING
        };

        for (Skill skill : skills) {
            assertNotNull(skill.getDescription(), skill.getName() + " should have a description");
            assertFalse(skill.getDescription().isEmpty(), 
                       skill.getName() + " description should not be empty");
        }
    }

    @Test
    void testCategoryEnumProperties() {
        for (Category category : Category.values()) {
            assertNotNull(category.getDisplayName(), "Category should have display name");
            assertNotNull(category.getDescription(), "Category should have description");
            assertFalse(category.getDisplayName().isEmpty(), "Display name should not be empty");
            assertFalse(category.getDescription().isEmpty(), "Description should not be empty");
        }
    }

    @Test
    void testProficiencyTierEnumProperties() {
        for (ProficiencyTier tier : ProficiencyTier.values()) {
            assertNotNull(tier.getDisplayName(), "Proficiency tier should have display name");
            assertTrue(tier.getMinXP() >= 0, "Min XP should be non-negative");
            assertTrue(tier.getMaxXP() > tier.getMinXP(), "Max XP should be greater than min XP");
        }
    }

    @Test
    void testSkillEquality() {
        // Same skill references should be equal
        assertEquals(Skill.SWORD_FIGHTING, Skill.SWORD_FIGHTING, "Same skill should be equal");
        
        // Different skills should not be equal
        assertNotEquals(Skill.SWORD_FIGHTING, Skill.ARCHERY, "Different skills should not be equal");
    }
}
