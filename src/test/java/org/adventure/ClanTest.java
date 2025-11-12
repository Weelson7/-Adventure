package org.adventure;

import org.adventure.society.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Clan class.
 * Covers: construction, membership, treasury, relationships, merging, destruction.
 * 
 * Quality Gates:
 * - Treasury validation: treasury never goes negative
 * - Membership logic: join/leave updates member lists correctly
 * - Merge logic: combines members and treasuries correctly
 */
class ClanTest {

    @Test
    @DisplayName("Create clan with valid data")
    void testCreateClan() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .type(ClanType.CLAN)
                .foundingTick(1000)
                .lastActiveTick(1000)
                .build();

        assertEquals("clan-001", clan.getId());
        assertEquals("Test Clan", clan.getName());
        assertEquals(ClanType.CLAN, clan.getType());
        assertEquals(0, clan.getMemberCount());
        assertEquals(0.0, clan.getTreasury());
        assertEquals(1000, clan.getFoundingTick());
        assertEquals(1, clan.getSchemaVersion());
    }

    @Test
    @DisplayName("Clan name cannot be null")
    void testClanNameCannotBeNull() {
        assertThrows(NullPointerException.class, () -> {
            new Clan.Builder()
                    .id("clan-001")
                    .name(null)
                    .foundingTick(1000)
                    .build();
        });
    }

    @Test
    @DisplayName("Clan name cannot be empty")
    void testClanNameCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Clan.Builder()
                    .id("clan-001")
                    .name("   ")
                    .foundingTick(1000)
                    .build();
        });
    }

    @Test
    @DisplayName("Treasury cannot be negative")
    void testTreasuryCannotBeNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Clan.Builder()
                    .id("clan-001")
                    .name("Test Clan")
                    .treasury(-100)
                    .build();
        });
    }

    @Test
    @DisplayName("Add member to clan")
    void testAddMember() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .build();

        Clan updated = clan.addMember("char-001");

        assertTrue(updated.hasMember("char-001"));
        assertEquals(1, updated.getMemberCount());
        assertFalse(clan.hasMember("char-001")); // Original unchanged
    }

    @Test
    @DisplayName("Cannot add duplicate member")
    void testCannotAddDuplicateMember() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .members(List.of("char-001"))
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            clan.addMember("char-001");
        });
    }

    @Test
    @DisplayName("Remove member from clan")
    void testRemoveMember() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .members(List.of("char-001", "char-002"))
                .build();

        Clan updated = clan.removeMember("char-001");

        assertFalse(updated.hasMember("char-001"));
        assertTrue(updated.hasMember("char-002"));
        assertEquals(1, updated.getMemberCount());
    }

    @Test
    @DisplayName("Cannot remove non-existent member")
    void testCannotRemoveNonExistentMember() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            clan.removeMember("char-999");
        });
    }

    @Test
    @DisplayName("Deposit funds into treasury")
    void testDepositFunds() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .treasury(100.0)
                .build();

        Clan updated = clan.deposit(50.0);

        assertEquals(150.0, updated.getTreasury());
        assertEquals(100.0, clan.getTreasury()); // Original unchanged
    }

    @Test
    @DisplayName("Cannot deposit negative amount")
    void testCannotDepositNegativeAmount() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            clan.deposit(-50.0);
        });
    }

    @Test
    @DisplayName("Withdraw funds from treasury")
    void testWithdrawFunds() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .treasury(100.0)
                .build();

        Clan updated = clan.withdraw(30.0);

        assertEquals(70.0, updated.getTreasury());
    }

    @Test
    @DisplayName("Cannot withdraw more than treasury balance")
    void testCannotWithdrawMoreThanBalance() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .treasury(100.0)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            clan.withdraw(150.0);
        });
    }

    @Test
    @DisplayName("Treasury never goes negative")
    void testTreasuryNeverGoesNegative() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .treasury(50.0)
                .build();

        // Try to withdraw exact amount
        Clan updated = clan.withdraw(50.0);
        assertEquals(0.0, updated.getTreasury());

        // Try to withdraw from zero balance
        assertThrows(IllegalArgumentException.class, () -> {
            updated.withdraw(1.0);
        });
    }

    @Test
    @DisplayName("Update relationship")
    void testUpdateRelationship() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .build();

        RelationshipRecord relationship = new RelationshipRecord(
                "clan-002", 50.0, 30.0, 40.0, 10.0, 1000);

        Clan updated = clan.updateRelationship(relationship);

        assertTrue(updated.getRelationships().containsKey("clan-002"));
        assertEquals(50.0, updated.getRelationships().get("clan-002").getReputation());
    }

    @Test
    @DisplayName("Merge two clans")
    void testMergeClans() {
        Clan clan1 = new Clan.Builder()
                .id("clan-001")
                .name("Clan A")
                .members(List.of("char-001", "char-002"))
                .treasury(100.0)
                .foundingTick(1000)
                .build();

        Clan clan2 = new Clan.Builder()
                .id("clan-002")
                .name("Clan B")
                .members(List.of("char-003", "char-004"))
                .treasury(200.0)
                .foundingTick(1500)
                .build();

        Clan merged = Clan.merge(clan1, clan2, "clan-merged", "United Clan", 2000);

        assertEquals("clan-merged", merged.getId());
        assertEquals("United Clan", merged.getName());
        assertEquals(4, merged.getMemberCount());
        assertTrue(merged.hasMember("char-001"));
        assertTrue(merged.hasMember("char-002"));
        assertTrue(merged.hasMember("char-003"));
        assertTrue(merged.hasMember("char-004"));
        assertEquals(300.0, merged.getTreasury());
        assertEquals(2000, merged.getFoundingTick());
    }

    @Test
    @DisplayName("Merge clans with duplicate members")
    void testMergeClansWithDuplicateMembers() {
        Clan clan1 = new Clan.Builder()
                .id("clan-001")
                .name("Clan A")
                .members(List.of("char-001", "char-002"))
                .treasury(100.0)
                .build();

        Clan clan2 = new Clan.Builder()
                .id("clan-002")
                .name("Clan B")
                .members(List.of("char-002", "char-003")) // char-002 is in both
                .treasury(200.0)
                .build();

        Clan merged = Clan.merge(clan1, clan2, "clan-merged", "United Clan", 2000);

        assertEquals(3, merged.getMemberCount()); // Should not duplicate char-002
        assertTrue(merged.hasMember("char-001"));
        assertTrue(merged.hasMember("char-002"));
        assertTrue(merged.hasMember("char-003"));
    }

    @Test
    @DisplayName("Merge clans combines relationships")
    void testMergeClansRelationships() {
        RelationshipRecord rel1 = new RelationshipRecord("clan-003", 50.0, 30.0, 40.0, 10.0, 1000);
        RelationshipRecord rel2 = new RelationshipRecord("clan-004", -20.0, 10.0, -10.0, 5.0, 1000);

        Clan clan1 = new Clan.Builder()
                .id("clan-001")
                .name("Clan A")
                .relationships(Map.of("clan-003", rel1))
                .build();

        Clan clan2 = new Clan.Builder()
                .id("clan-002")
                .name("Clan B")
                .relationships(Map.of("clan-004", rel2))
                .build();

        Clan merged = Clan.merge(clan1, clan2, "clan-merged", "United Clan", 2000);

        assertEquals(2, merged.getRelationships().size());
        assertTrue(merged.getRelationships().containsKey("clan-003"));
        assertTrue(merged.getRelationships().containsKey("clan-004"));
    }

    @Test
    @DisplayName("Check if clan is empty")
    void testIsEmpty() {
        Clan emptyClan = new Clan.Builder()
                .id("clan-001")
                .name("Empty Clan")
                .build();

        assertTrue(emptyClan.isEmpty());

        Clan nonEmptyClan = emptyClan.addMember("char-001");
        assertFalse(nonEmptyClan.isEmpty());
    }

    @Test
    @DisplayName("Update last active tick")
    void testUpdateLastActiveTick() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .lastActiveTick(1000)
                .build();

        Clan updated = clan.updateLastActiveTick(2000);

        assertEquals(2000, updated.getLastActiveTick());
        assertEquals(1000, clan.getLastActiveTick()); // Original unchanged
    }

    @Test
    @DisplayName("Clan equality based on id")
    void testClanEquality() {
        Clan clan1 = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .build();

        Clan clan2 = new Clan.Builder()
                .id("clan-001")
                .name("Different Name")
                .build();

        Clan clan3 = new Clan.Builder()
                .id("clan-002")
                .name("Test Clan")
                .build();

        assertEquals(clan1, clan2); // Same ID
        assertNotEquals(clan1, clan3); // Different ID
    }

    @Test
    @DisplayName("Clan toString contains key info")
    void testClanToString() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .type(ClanType.KINGDOM)
                .treasury(1000.0)
                .members(List.of("char-001", "char-002", "char-003"))
                .build();

        String str = clan.toString();
        assertTrue(str.contains("clan-001"));
        assertTrue(str.contains("Test Clan"));
        assertTrue(str.contains("KINGDOM"));
        assertTrue(str.contains("memberCount=3"));
        assertTrue(str.contains("treasury=1000.0"));
    }

    @Test
    @DisplayName("Builder creates copy of clan")
    void testBuilderCopyClan() {
        Clan original = new Clan.Builder()
                .id("clan-001")
                .name("Original Clan")
                .members(List.of("char-001"))
                .treasury(500.0)
                .build();

        Clan copy = new Clan.Builder(original)
                .name("Modified Clan")
                .build();

        assertEquals(original.getId(), copy.getId());
        assertEquals("Modified Clan", copy.getName());
        assertEquals(original.getTreasury(), copy.getTreasury());
        assertEquals(original.getMemberCount(), copy.getMemberCount());
    }

    @Test
    @DisplayName("Multiple treasury operations")
    void testMultipleTreasuryOperations() {
        Clan clan = new Clan.Builder()
                .id("clan-001")
                .name("Test Clan")
                .treasury(100.0)
                .build();

        Clan updated = clan
                .deposit(50.0)
                .withdraw(30.0)
                .deposit(20.0)
                .withdraw(10.0);

        assertEquals(130.0, updated.getTreasury());
    }

    @Test
    @DisplayName("Guild type clan")
    void testGuildTypeClan() {
        Clan guild = new Clan.Builder()
                .id("guild-001")
                .name("Craftsmen Guild")
                .type(ClanType.GUILD)
                .build();

        assertEquals(ClanType.GUILD, guild.getType());
    }

    @Test
    @DisplayName("Kingdom type clan")
    void testKingdomTypeClan() {
        Clan kingdom = new Clan.Builder()
                .id("kingdom-001")
                .name("Northern Kingdom")
                .type(ClanType.KINGDOM)
                .build();

        assertEquals(ClanType.KINGDOM, kingdom.getType());
    }
}
