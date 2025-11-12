package org.adventure;

import org.adventure.society.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Diplomacy class and RelationshipRecord.
 * Covers: relationship metrics, decay, event-driven updates, alliances.
 * 
 * Quality Gates:
 * - Diplomacy metrics: reputation, influence, alignment, race affinity within ranges
 * - Decay formulas: reputation→0, influence decreases, alignment minimal decay
 * - Event impacts: trade, betrayal, gifts, war affect metrics correctly
 */
class DiplomacyTest {

    @Test
    @DisplayName("Create relationship record with valid metrics")
    void testCreateRelationshipRecord() {
        RelationshipRecord rel = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 10.0, 1000);

        assertEquals("clan-002", rel.getTargetSocietyId());
        assertEquals(50.0, rel.getReputation());
        assertEquals(30.0, rel.getInfluence());
        assertEquals(40.0, rel.getAlignment());
        assertEquals(10.0, rel.getRaceAffinity());
        assertEquals(1000, rel.getLastUpdatedTick());
    }

    @Test
    @DisplayName("Reputation clamped to [-100, 100]")
    void testReputationClamped() {
        RelationshipRecord tooHigh = new RelationshipRecord(
                "clan-002", 150.0, 30.0, 40.0, 10.0, 1000);
        assertEquals(100.0, tooHigh.getReputation());

        RelationshipRecord tooLow = new RelationshipRecord(
                "clan-002", -150.0, 30.0, 40.0, 10.0, 1000);
        assertEquals(-100.0, tooLow.getReputation());
    }

    @Test
    @DisplayName("Influence clamped to [0, 100]")
    void testInfluenceClamped() {
        RelationshipRecord tooHigh = new RelationshipRecord(
                "clan-002", 50.0, 150.0, 40.0, 10.0, 1000);
        assertEquals(100.0, tooHigh.getInfluence());

        RelationshipRecord negative = new RelationshipRecord(
                "clan-002", 50.0, -10.0, 40.0, 10.0, 1000);
        assertEquals(0.0, negative.getInfluence());
    }

    @Test
    @DisplayName("Alignment clamped to [-100, 100]")
    void testAlignmentClamped() {
        RelationshipRecord tooHigh = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 150.0, 10.0, 1000);
        assertEquals(100.0, tooHigh.getAlignment());

        RelationshipRecord tooLow = new RelationshipRecord(
                "clan-002", 50.0, 30.0, -150.0, 10.0, 1000);
        assertEquals(-100.0, tooLow.getAlignment());
    }

    @Test
    @DisplayName("Race affinity clamped to [-50, 50]")
    void testRaceAffinityClamped() {
        RelationshipRecord tooHigh = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 100.0, 1000);
        assertEquals(50.0, tooHigh.getRaceAffinity());

        RelationshipRecord tooLow = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, -100.0, 1000);
        assertEquals(-50.0, tooLow.getRaceAffinity());
    }

    @Test
    @DisplayName("Calculate alliance strength")
    void testCalculateAllianceStrength() {
        RelationshipRecord rel = new RelationshipRecord(
                "clan-002", 60.0, 30.0, 40.0, 10.0, 1000);

        double allianceStrength = rel.getAllianceStrength();
        assertEquals(50.0, allianceStrength, 0.001); // (60 + 40) / 2
    }

    @Test
    @DisplayName("Can form alliance when strength > 30")
    void testCanFormAlliance() {
        RelationshipRecord strong = new RelationshipRecord(
                "clan-002", 40.0, 30.0, 30.0, 10.0, 1000);
        assertTrue(strong.canFormAlliance()); // (40 + 30) / 2 = 35 > 30

        RelationshipRecord weak = new RelationshipRecord(
                "clan-002", 20.0, 30.0, 20.0, 10.0, 1000);
        assertFalse(weak.canFormAlliance()); // (20 + 20) / 2 = 20 < 30
    }

    @Test
    @DisplayName("Calculate war likelihood")
    void testCalculateWarLikelihood() {
        RelationshipRecord hostile = new RelationshipRecord(
                "clan-002", -70.0, 30.0, 40.0, 10.0, 1000);
        double warLikelihood = hostile.getWarLikelihood();
        assertEquals(1.0, warLikelihood, 0.001); // (-(-70) - 20) / 50 = 1.0

        RelationshipRecord neutral = new RelationshipRecord(
                "clan-002", 0.0, 30.0, 40.0, 10.0, 1000);
        assertEquals(0.0, neutral.getWarLikelihood(), 0.001); // max(0, (-0 - 20) / 50) = 0

        RelationshipRecord friendly = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 10.0, 1000);
        assertEquals(0.0, friendly.getWarLikelihood(), 0.001);
    }

    @Test
    @DisplayName("Update reputation")
    void testUpdateReputation() {
        RelationshipRecord rel = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 10.0, 1000);

        RelationshipRecord updated = rel.withReputation(60.0, 2000);

        assertEquals(60.0, updated.getReputation());
        assertEquals(30.0, updated.getInfluence()); // Unchanged
        assertEquals(2000, updated.getLastUpdatedTick());
    }

    @Test
    @DisplayName("Reputation decay toward zero (positive)")
    void testReputationDecayPositive() {
        RelationshipRecord rel = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 10.0, 1000);

        // After 100 ticks: decay = -0.01 * 1 = -0.01
        RelationshipRecord decayed = rel.applyDecay(100, 1100);
        assertTrue(decayed.getReputation() < 50.0);
        assertTrue(decayed.getReputation() > 49.0); // Small decay
    }

    @Test
    @DisplayName("Reputation decay toward zero (negative)")
    void testReputationDecayNegative() {
        RelationshipRecord rel = new RelationshipRecord(
                "clan-002", -50.0, 30.0, 40.0, 10.0, 1000);

        // After 100 ticks: decay toward 0
        RelationshipRecord decayed = rel.applyDecay(100, 1100);
        assertTrue(decayed.getReputation() > -50.0);
        assertTrue(decayed.getReputation() < -49.0); // Small decay toward 0
    }

    @Test
    @DisplayName("Influence decay over time")
    void testInfluenceDecay() {
        RelationshipRecord rel = new RelationshipRecord(
                "clan-002", 50.0, 50.0, 40.0, 10.0, 1000);

        // After 100 ticks: influence decay = -0.05 * 1 = -0.05
        RelationshipRecord decayed = rel.applyDecay(100, 1100);
        assertTrue(decayed.getInfluence() < 50.0);
        assertTrue(decayed.getInfluence() >= 49.0);
    }

    @Test
    @DisplayName("Influence never goes below zero")
    void testInfluenceNeverNegative() {
        RelationshipRecord rel = new RelationshipRecord(
                "clan-002", 50.0, 1.0, 40.0, 10.0, 1000);

        // After 10000 ticks: should decay to 0, not negative
        RelationshipRecord decayed = rel.applyDecay(10000, 11000);
        assertEquals(0.0, decayed.getInfluence(), 0.001);
    }

    @Test
    @DisplayName("Alignment minimal decay")
    void testAlignmentMinimalDecay() {
        RelationshipRecord rel = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 50.0, 10.0, 1000);

        // After 100 ticks: alignment decay = -0.001 * 100 = -0.1
        RelationshipRecord decayed = rel.applyDecay(100, 1100);
        assertEquals(49.9, decayed.getAlignment(), 0.001);
    }

    @Test
    @DisplayName("Race affinity does not decay")
    void testRaceAffinityNoDecay() {
        RelationshipRecord rel = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 20.0, 1000);

        RelationshipRecord decayed = rel.applyDecay(1000, 2000);
        assertEquals(20.0, decayed.getRaceAffinity(), 0.001);
    }

    @Test
    @DisplayName("Process periodic decay for clan")
    void testProcessPeriodicDecay() {
        RelationshipRecord rel = new RelationshipRecord(
                "clan-002", 50.0, 50.0, 40.0, 10.0, 1000);

        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .relationships(Map.of("clan-002", rel))
                .lastActiveTick(1000)
                .build();

        Clan updated = Diplomacy.processPeriodicDecay(clan, 1100);

        RelationshipRecord updatedRel = updated.getRelationships().get("clan-002");
        assertTrue(updatedRel.getReputation() < 50.0); // Decayed
        assertTrue(updatedRel.getInfluence() < 50.0); // Decayed
        assertEquals(1100, updated.getLastActiveTick());
    }

    @Test
    @DisplayName("Apply trade mission increases reputation and influence")
    void testApplyTradeMission() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .build();

        Clan updated = Diplomacy.applyTradeMission(clan, "clan-002", 1000);

        RelationshipRecord rel = updated.getRelationships().get("clan-002");
        assertNotNull(rel);
        assertEquals(5.0, rel.getReputation()); // +5 from trade
        assertEquals(2.0, rel.getInfluence()); // +2 from trade
    }

    @Test
    @DisplayName("Apply trade mission to existing relationship")
    void testApplyTradeMissionExisting() {
        RelationshipRecord existing = new RelationshipRecord(
                "clan-002", 10.0, 5.0, 0.0, 0.0, 1000);

        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .relationships(Map.of("clan-002", existing))
                .build();

        Clan updated = Diplomacy.applyTradeMission(clan, "clan-002", 2000);

        RelationshipRecord rel = updated.getRelationships().get("clan-002");
        assertEquals(15.0, rel.getReputation()); // 10 + 5
        assertEquals(7.0, rel.getInfluence()); // 5 + 2
    }

    @Test
    @DisplayName("Apply betrayal decreases reputation")
    void testApplyBetrayal() {
        RelationshipRecord existing = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 10.0, 1000);

        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .relationships(Map.of("clan-002", existing))
                .build();

        Clan updated = Diplomacy.applyBetrayal(clan, "clan-002", 2000);

        RelationshipRecord rel = updated.getRelationships().get("clan-002");
        assertEquals(20.0, rel.getReputation()); // 50 - 30
    }

    @Test
    @DisplayName("Apply diplomatic gift increases reputation and alignment")
    void testApplyDiplomaticGift() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .build();

        Clan updated = Diplomacy.applyDiplomaticGift(clan, "clan-002", 1000);

        RelationshipRecord rel = updated.getRelationships().get("clan-002");
        assertEquals(3.0, rel.getReputation()); // +3 from gift
        assertEquals(1.0, rel.getAlignment()); // +1 from gift
    }

    @Test
    @DisplayName("Apply war declaration damages reputation and alignment")
    void testApplyWarDeclaration() {
        RelationshipRecord existing = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 10.0, 1000);

        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .relationships(Map.of("clan-002", existing))
                .build();

        Clan updated = Diplomacy.applyWarDeclaration(clan, "clan-002", 2000);

        RelationshipRecord rel = updated.getRelationships().get("clan-002");
        assertEquals(10.0, rel.getReputation()); // 50 - 40
        assertEquals(20.0, rel.getAlignment()); // 40 - 20
    }

    @Test
    @DisplayName("Form alliance when requirements met")
    void testFormAlliance() {
        RelationshipRecord strong = new RelationshipRecord(
                "clan-002", 40.0, 30.0, 30.0, 10.0, 1000);

        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .relationships(Map.of("clan-002", strong))
                .build();

        Clan updated = Diplomacy.formAlliance(clan, "clan-002", 2000);

        RelationshipRecord rel = updated.getRelationships().get("clan-002");
        assertEquals(50.0, rel.getReputation()); // 40 + 10 alliance bonus
        assertEquals(40.0, rel.getAlignment()); // 30 + 10 alliance bonus
    }

    @Test
    @DisplayName("Cannot form alliance when requirements not met")
    void testCannotFormAllianceWeak() {
        RelationshipRecord weak = new RelationshipRecord(
                "clan-002", 10.0, 30.0, 10.0, 10.0, 1000);

        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .relationships(Map.of("clan-002", weak))
                .build();

        assertThrows(IllegalStateException.class, () -> {
            Diplomacy.formAlliance(clan, "clan-002", 2000);
        });
    }

    @Test
    @DisplayName("Check if war is likely")
    void testIsWarLikely() {
        RelationshipRecord hostile = new RelationshipRecord(
                "clan-002", -70.0, 30.0, 40.0, 10.0, 1000);

        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .relationships(Map.of("clan-002", hostile))
                .build();

        assertTrue(Diplomacy.isWarLikely(clan, "clan-002"));
    }

    @Test
    @DisplayName("War not likely with neutral relationship")
    void testWarNotLikelyNeutral() {
        RelationshipRecord neutral = new RelationshipRecord(
                "clan-002", 0.0, 30.0, 40.0, 10.0, 1000);

        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .relationships(Map.of("clan-002", neutral))
                .build();

        assertFalse(Diplomacy.isWarLikely(clan, "clan-002"));
    }

    @Test
    @DisplayName("War not likely with no relationship")
    void testWarNotLikelyNoRelationship() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .build();

        assertFalse(Diplomacy.isWarLikely(clan, "clan-002"));
    }

    @Test
    @DisplayName("Get positive relations")
    void testGetPositiveRelations() {
        RelationshipRecord positive1 = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 10.0, 1000);
        RelationshipRecord positive2 = new RelationshipRecord(
                "clan-003", 20.0, 10.0, 15.0, 5.0, 1000);
        RelationshipRecord negative = new RelationshipRecord(
                "clan-004", -30.0, 10.0, -10.0, 0.0, 1000);

        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .relationships(Map.of(
                        "clan-002", positive1,
                        "clan-003", positive2,
                        "clan-004", negative))
                .build();

        List<String> positiveRels = Diplomacy.getPositiveRelations(clan);
        assertEquals(2, positiveRels.size());
        assertTrue(positiveRels.contains("clan-002"));
        assertTrue(positiveRels.contains("clan-003"));
        assertFalse(positiveRels.contains("clan-004"));
    }

    @Test
    @DisplayName("Get negative relations")
    void testGetNegativeRelations() {
        RelationshipRecord positive = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 10.0, 1000);
        RelationshipRecord negative1 = new RelationshipRecord(
                "clan-003", -20.0, 10.0, -15.0, 5.0, 1000);
        RelationshipRecord negative2 = new RelationshipRecord(
                "clan-004", -50.0, 10.0, -30.0, 0.0, 1000);

        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .relationships(Map.of(
                        "clan-002", positive,
                        "clan-003", negative1,
                        "clan-004", negative2))
                .build();

        List<String> negativeRels = Diplomacy.getNegativeRelations(clan);
        assertEquals(2, negativeRels.size());
        assertTrue(negativeRels.contains("clan-003"));
        assertTrue(negativeRels.contains("clan-004"));
        assertFalse(negativeRels.contains("clan-002"));
    }

    @Test
    @DisplayName("Relationship equality based on target society")
    void testRelationshipEquality() {
        RelationshipRecord rel1 = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 10.0, 1000);
        RelationshipRecord rel2 = new RelationshipRecord(
                "clan-002", 60.0, 40.0, 50.0, 20.0, 2000);
        RelationshipRecord rel3 = new RelationshipRecord(
                "clan-003", 50.0, 30.0, 40.0, 10.0, 1000);

        assertEquals(rel1, rel2); // Same target
        assertNotEquals(rel1, rel3); // Different target
    }

    @Test
    @DisplayName("Relationship toString contains key info")
    void testRelationshipToString() {
        RelationshipRecord rel = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 10.0, 1000);

        String str = rel.toString();
        assertTrue(str.contains("clan-002"));
        assertTrue(str.contains("reputation=50.0"));
        assertTrue(str.contains("influence=30.0"));
        assertTrue(str.contains("alignment=40.0"));
        assertTrue(str.contains("allianceStrength=45.0"));
    }
}
