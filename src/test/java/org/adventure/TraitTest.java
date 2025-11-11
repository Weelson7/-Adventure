package org.adventure;

import org.adventure.character.Trait;
import org.adventure.character.Character.CoreStat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Trait class.
 * Validates trait modifiers, stat effects, and hereditary properties.
 */
class TraitTest {

    @Test
    void testTraitBasicProperties() {
        assertEquals("fast_learner", Trait.FAST_LEARNER.getId(), "ID should match");
        assertEquals("Fast Learner", Trait.FAST_LEARNER.getName(), "Name should match");
        assertNotNull(Trait.FAST_LEARNER.getDescription(), "Description should not be null");
    }

    @Test
    void testFastLearnerModifiers() {
        // Fast Learner: +20% stat progression, +30% skill XP
        assertEquals(1.2, Trait.FAST_LEARNER.getStatProgressionMultiplier(CoreStat.STRENGTH), 0.001, 
                    "Should have 20% stat progression boost");
        assertEquals(1.3, Trait.FAST_LEARNER.getSkillXPMultiplier(), 0.001, 
                    "Should have 30% skill XP boost");
    }

    @Test
    void testRobustModifiers() {
        // Robust: +5 soft cap threshold
        assertEquals(5, Trait.ROBUST.getSoftCapThresholdBonus(), "Robust should give +5 soft cap threshold");
        assertTrue(Trait.ROBUST.isHereditary(), "Robust should be hereditary");
    }

    @Test
    void testAgileModifiers() {
        // Agile: +5 soft cap threshold
        assertEquals(5, Trait.AGILE.getSoftCapThresholdBonus(), "Agile should give +5 soft cap threshold");
        assertTrue(Trait.AGILE.isHereditary(), "Agile should be hereditary");
    }

    @Test
    void testBlessedModifiers() {
        // Blessed: +10% stat progression, +10 soft cap threshold
        assertEquals(10, Trait.BLESSED.getSoftCapThresholdBonus(), "Blessed should give +10 soft cap threshold");
        assertEquals(1.1, Trait.BLESSED.getStatProgressionMultiplier(CoreStat.STRENGTH), 0.001, 
                    "Blessed should give +10% stat progression");
    }

    @Test
    void testCursedModifiers() {
        // Cursed: -20% stat progression, -10 soft cap threshold (negative trait)
        assertEquals(-10, Trait.CURSED.getSoftCapThresholdBonus(), "Cursed should give -10 soft cap threshold");
        assertEquals(0.8, Trait.CURSED.getStatProgressionMultiplier(CoreStat.STRENGTH), 0.001, 
                    "Cursed should give -20% stat progression");
    }

    @Test
    void testLegendaryPotentialModifiers() {
        // Legendary Potential: +50 soft cap threshold for all stats
        assertEquals(50, Trait.LEGENDARY_POTENTIAL.getSoftCapThresholdBonus(), 
                    "Legendary Potential should give +50 soft cap threshold");
        assertEquals(1.0, Trait.LEGENDARY_POTENTIAL.getStatProgressionMultiplier(CoreStat.STRENGTH), 0.001, 
                    "Legendary Potential should have normal stat progression");
        assertEquals(1.0, Trait.LEGENDARY_POTENTIAL.getSkillXPMultiplier(), 0.001, 
                    "Legendary Potential should have normal skill XP");
    }

    @Test
    void testNightVisionProperties() {
        // Night Vision: pure ability trait (hereditary)
        assertNotNull(Trait.NIGHT_VISION.getDescription(), "Night Vision should have description");
        assertTrue(Trait.NIGHT_VISION.isHereditary(), "Night Vision should be hereditary");
    }

    @Test
    void testResilientProperties() {
        // Resilient: pure flavor trait
        assertNotNull(Trait.RESILIENT.getDescription(), "Resilient should have description");
    }

    @Test
    void testGeniusProperties() {
        // Genius: pure flavor trait
        assertNotNull(Trait.GENIUS.getDescription(), "Genius should have description");
    }

    @Test
    void testCharismaticProperties() {
        // Charismatic: pure flavor trait
        assertNotNull(Trait.CHARISMATIC.getDescription(), "Charismatic should have description");
    }

    @Test
    void testLuckyProperties() {
        // Lucky: pure flavor trait
        assertNotNull(Trait.LUCKY.getDescription(), "Lucky should have description");
    }

    @Test
    void testHereditaryProperty() {
        // Robust, Agile, Night Vision, Resilient, and Charismatic are hereditary
        assertTrue(Trait.ROBUST.isHereditary(), "Robust should be hereditary");
        assertTrue(Trait.AGILE.isHereditary(), "Agile should be hereditary");
        assertTrue(Trait.NIGHT_VISION.isHereditary(), "Night Vision should be hereditary");
        assertTrue(Trait.RESILIENT.isHereditary(), "Resilient should be hereditary");
        assertTrue(Trait.CHARISMATIC.isHereditary(), "Charismatic should be hereditary");
        
        // Others are not hereditary
        assertFalse(Trait.FAST_LEARNER.isHereditary(), "Fast Learner should not be hereditary");
        assertFalse(Trait.BLESSED.isHereditary(), "Blessed should not be hereditary");
        assertFalse(Trait.LEGENDARY_POTENTIAL.isHereditary(), "Legendary Potential should not be hereditary");
    }

    @Test
    void testNegativeTraitClumsy() {
        // Clumsy: -10% stat progression, -20% skill XP (negative trait)
        assertEquals(0.9, Trait.CLUMSY.getStatProgressionMultiplier(CoreStat.STRENGTH), 0.001, 
                    "Clumsy should give -10% stat progression");
        assertEquals(0.8, Trait.CLUMSY.getSkillXPMultiplier(), 0.001, 
                    "Clumsy should give -20% skill XP");
    }

    @Test
    void testTraitEquality() {
        // Same trait references should be equal
        assertEquals(Trait.FAST_LEARNER, Trait.FAST_LEARNER, "Same trait should be equal");
        
        // Different traits should not be equal
        assertNotEquals(Trait.FAST_LEARNER, Trait.ROBUST, "Different traits should not be equal");
    }

    @Test
    void testAllTraitsHaveUniqueIds() {
        Trait[] traits = {
            Trait.FAST_LEARNER, Trait.ROBUST, Trait.AGILE, Trait.CLUMSY,
            Trait.BLESSED, Trait.CURSED, Trait.NIGHT_VISION, Trait.RESILIENT,
            Trait.GENIUS, Trait.CHARISMATIC, Trait.LUCKY, Trait.LEGENDARY_POTENTIAL
        };

        for (int i = 0; i < traits.length; i++) {
            for (int j = i + 1; j < traits.length; j++) {
                assertNotEquals(traits[i].getId(), traits[j].getId(), 
                               "Traits should have unique IDs");
            }
        }
    }

    @Test
    void testTraitDescriptionsNotEmpty() {
        Trait[] traits = {
            Trait.FAST_LEARNER, Trait.ROBUST, Trait.AGILE, Trait.CLUMSY,
            Trait.BLESSED, Trait.CURSED, Trait.NIGHT_VISION, Trait.RESILIENT,
            Trait.GENIUS, Trait.CHARISMATIC, Trait.LUCKY, Trait.LEGENDARY_POTENTIAL
        };

        for (Trait trait : traits) {
            assertNotNull(trait.getDescription(), trait.getName() + " should have a description");
            assertFalse(trait.getDescription().isEmpty(), 
                       trait.getName() + " description should not be empty");
        }
    }
}

