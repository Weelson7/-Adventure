package org.adventure;

import org.adventure.structure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OwnershipTransferSystem and related classes.
 * Tests all transfer types, validation, contested ownership, and rollback mechanisms.
 */
public class OwnershipTransferTest {
    
    private OwnershipTransferSystem transferSystem;
    private Structure testStructure;
    
    @BeforeEach
    public void setUp() {
        transferSystem = new OwnershipTransferSystem();
        
        testStructure = new Structure.Builder()
            .id("struct-001")
            .type(StructureType.HOUSE)
            .ownerId("char-001")
            .ownerType(OwnerType.CHARACTER)
            .locationTileId("100:200:0")
            .health(100.0)
            .maxHealth(100.0)
            .build();
    }
    
    // ===== Transfer Type Tests =====
    
    @Test
    public void testVoluntaryTransfer() {
        TransferRecord record = transferSystem.executeVoluntaryTransfer(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            1000
        );
        
        assertEquals("struct-001", record.getStructureId());
        assertEquals("char-001", record.getFromOwnerId());
        assertEquals(OwnerType.CHARACTER, record.getFromOwnerType());
        assertEquals("char-002", record.getToOwnerId());
        assertEquals(OwnerType.CHARACTER, record.getToOwnerType());
        assertEquals(TransferType.VOLUNTARY, record.getTransferType());
        assertEquals(1000, record.getTransferredAtTick());
        
        // Verify structure ownership changed
        assertEquals("char-002", testStructure.getOwnerId());
        assertEquals(OwnerType.CHARACTER, testStructure.getOwnerType());
    }
    
    @Test
    public void testSaleTransfer() {
        TransferRecord record = transferSystem.executeSale(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            5000.0,
            1000
        );
        
        assertEquals(TransferType.SALE, record.getTransferType());
        assertEquals(5000.0, record.getMetadata().get("price"));
        assertEquals("gold", record.getMetadata().get("currency"));
        
        assertEquals("char-002", testStructure.getOwnerId());
    }
    
    @Test
    public void testSuccessionInheritance() {
        TransferRecord record = transferSystem.executeSuccession(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            TransferType.SUCCESSION_INHERITANCE,
            1000
        );
        
        assertEquals(TransferType.SUCCESSION_INHERITANCE, record.getTransferType());
        assertEquals("char-001", record.getMetadata().get("deceasedOwner"));
        assertEquals("char-002", testStructure.getOwnerId());
    }
    
    @Test
    public void testSuccessionWill() {
        TransferRecord record = transferSystem.executeSuccession(
            testStructure,
            "char-003",
            OwnerType.CHARACTER,
            TransferType.SUCCESSION_WILL,
            1000
        );
        
        assertEquals(TransferType.SUCCESSION_WILL, record.getTransferType());
        assertEquals("char-003", testStructure.getOwnerId());
    }
    
    @Test
    public void testSuccessionHeir() {
        // Transfer to clan first
        testStructure.transferOwnership("clan-001", OwnerType.CLAN, 500);
        
        TransferRecord record = transferSystem.executeSuccession(
            testStructure,
            "char-004",
            OwnerType.CHARACTER,
            TransferType.SUCCESSION_HEIR,
            1000
        );
        
        assertEquals(TransferType.SUCCESSION_HEIR, record.getTransferType());
        assertEquals("char-004", testStructure.getOwnerId());
    }
    
    @Test
    public void testConquest() {
        TransferRecord record = transferSystem.executeConquest(
            testStructure,
            "char-attacker",
            OwnerType.CHARACTER,
            1000
        );
        
        assertEquals(TransferType.CONQUEST, record.getTransferType());
        assertTrue((Boolean) record.getMetadata().get("conquestVictory"));
        assertEquals("char-001", record.getMetadata().get("defeatedOwner"));
        assertEquals("char-attacker", testStructure.getOwnerId());
    }
    
    // ===== Transfer Type Helper Tests =====
    
    @Test
    public void testTransferTypeRequiresPayment() {
        assertTrue(TransferType.SALE.requiresPayment());
        assertFalse(TransferType.VOLUNTARY.requiresPayment());
        assertFalse(TransferType.CONQUEST.requiresPayment());
    }
    
    @Test
    public void testTransferTypeRequiresConsent() {
        assertTrue(TransferType.VOLUNTARY.requiresConsent());
        assertTrue(TransferType.SALE.requiresConsent());
        assertFalse(TransferType.CONQUEST.requiresConsent());
        assertFalse(TransferType.TAX_SEIZURE.requiresConsent());
    }
    
    @Test
    public void testTransferTypeIsForced() {
        assertTrue(TransferType.CONQUEST.isForced());
        assertTrue(TransferType.TAX_SEIZURE.isForced());
        assertTrue(TransferType.ABANDONED.isForced());
        assertFalse(TransferType.VOLUNTARY.isForced());
        assertFalse(TransferType.SALE.isForced());
    }
    
    @Test
    public void testTransferTypeIsSuccession() {
        assertTrue(TransferType.SUCCESSION_INHERITANCE.isSuccession());
        assertTrue(TransferType.SUCCESSION_WILL.isSuccession());
        assertTrue(TransferType.SUCCESSION_HEIR.isSuccession());
        assertFalse(TransferType.VOLUNTARY.isSuccession());
        assertFalse(TransferType.CONQUEST.isSuccession());
    }
    
    // ===== Validation Tests =====
    
    @Test
    public void testTransferRequiresValidOwnerId() {
        assertThrows(IllegalArgumentException.class, () -> {
            transferSystem.executeVoluntaryTransfer(
                testStructure,
                "",
                OwnerType.CHARACTER,
                1000
            );
        });
    }
    
    @Test
    public void testTransferRequiresValidOwnerType() {
        assertThrows(IllegalArgumentException.class, () -> {
            transferSystem.executeVoluntaryTransfer(
                testStructure,
                "char-002",
                null,
                1000
            );
        });
    }
    
    @Test
    public void testSaleRequiresNonNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> {
            transferSystem.executeSale(
                testStructure,
                "char-002",
                OwnerType.CHARACTER,
                -100.0,
                1000
            );
        });
    }
    
    @Test
    public void testSuccessionRequiresSuccessionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            transferSystem.executeSuccession(
                testStructure,
                "char-002",
                OwnerType.CHARACTER,
                TransferType.VOLUNTARY, // Not a succession type
                1000
            );
        });
    }
    
    // ===== Contested Ownership Tests =====
    
    @Test
    public void testContestOwnership() {
        Map<String, Object> evidence = new HashMap<>();
        evidence.put("witness", "char-witness-001");
        evidence.put("document", "deed-001");
        
        ContestedOwnership contest = transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "Prior ownership claim with deed",
            evidence,
            1000
        );
        
        assertEquals("struct-001", contest.getStructureId());
        assertEquals("char-002", contest.getContestingPartyId());
        assertEquals(OwnerType.CHARACTER, contest.getContestingPartyType());
        assertEquals("Prior ownership claim with deed", contest.getClaimBasis());
        assertEquals(1000, contest.getContestedAtTick());
        assertEquals(8200, contest.getExpiresAtTick()); // 1000 + 7200
        assertFalse(contest.isResolved());
        assertEquals("char-witness-001", contest.getEvidence().get("witness"));
    }
    
    @Test
    public void testCannotContestAlreadyContestedStructure() {
        transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "First claim",
            new HashMap<>(),
            1000
        );
        
        assertThrows(IllegalStateException.class, () -> {
            transferSystem.contestOwnership(
                testStructure,
                "char-003",
                OwnerType.CHARACTER,
                "Second claim",
                new HashMap<>(),
                1500
            );
        });
    }
    
    @Test
    public void testCannotTransferWithActiveContest() {
        transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "Dispute",
            new HashMap<>(),
            1000
        );
        
        assertThrows(IllegalStateException.class, () -> {
            transferSystem.executeVoluntaryTransfer(
                testStructure,
                "char-003",
                OwnerType.CHARACTER,
                1500
            );
        });
    }
    
    @Test
    public void testResolveContestedOwnershipInFavorOfContestant() {
        ContestedOwnership contest = transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "Valid claim",
            new HashMap<>(),
            1000
        );
        
        // Resolve in favor of contestant
        TransferRecord record = transferSystem.resolveContestedOwnershipInFavorOfContestant(
            testStructure,
            contest,
            2000
        );
        
        assertTrue(contest.isResolved());
        assertEquals(2000, contest.getResolvedAtTick());
        assertEquals("GRANTED", contest.getResolutionOutcome());
        
        // Structure should be transferred to contestant
        assertEquals("char-002", testStructure.getOwnerId());
        assertEquals("char-002", record.getToOwnerId());
        assertEquals("GRANTED", record.getMetadata().get("contestResolution"));
    }
    
    @Test
    public void testResolveContestedOwnershipInFavorOfOwner() {
        ContestedOwnership contest = transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "Invalid claim",
            new HashMap<>(),
            1000
        );
        
        // Resolve in favor of current owner
        transferSystem.resolveContestedOwnershipInFavorOfOwner(contest, 2000);
        
        assertTrue(contest.isResolved());
        assertEquals(2000, contest.getResolvedAtTick());
        assertEquals("DENIED", contest.getResolutionOutcome());
        
        // Structure ownership should not change
        assertEquals("char-001", testStructure.getOwnerId());
    }
    
    @Test
    public void testContestedOwnershipExpiry() {
        ContestedOwnership contest = transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "Claim",
            new HashMap<>(),
            1000
        );
        
        // Before expiry
        assertTrue(contest.isActive(5000));
        assertFalse(contest.isExpired(5000));
        
        // At expiry
        assertFalse(contest.isActive(8200));
        assertTrue(contest.isExpired(8200));
        
        // After expiry
        assertFalse(contest.isActive(10000));
        assertTrue(contest.isExpired(10000));
    }
    
    @Test
    public void testProcessExpiredContests() {
        transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "Claim",
            new HashMap<>(),
            1000
        );
        
        // Process at expiry time
        List<String> expired = transferSystem.processExpiredContests(8200);
        
        assertEquals(1, expired.size());
        assertEquals("struct-001", expired.get(0));
        
        // Contest should be marked as expired
        ContestedOwnership contest = transferSystem.getActiveContest("struct-001", 8200);
        assertNull(contest); // No longer active
        
        List<ContestedOwnership> allContests = transferSystem.getAllContests("struct-001");
        assertEquals(1, allContests.size());
        assertTrue(allContests.get(0).isResolved());
        assertEquals("EXPIRED", allContests.get(0).getResolutionOutcome());
    }
    
    @Test
    public void testCanTransferAfterContestExpired() {
        transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "Claim",
            new HashMap<>(),
            1000
        );
        
        // Process expiry
        transferSystem.processExpiredContests(8200);
        
        // Should be able to transfer now
        transferSystem.executeVoluntaryTransfer(
            testStructure,
            "char-003",
            OwnerType.CHARACTER,
            8500
        );
        
        assertEquals("char-003", testStructure.getOwnerId());
    }
    
    @Test
    public void testConquestClearsContestedOwnership() {
        transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "Claim",
            new HashMap<>(),
            1000
        );
        
        // Conquest should clear contested ownership
        transferSystem.executeConquest(
            testStructure,
            "char-attacker",
            OwnerType.CHARACTER,
            2000
        );
        
        assertFalse(transferSystem.hasActiveContest("struct-001", 2000));
        assertEquals("char-attacker", testStructure.getOwnerId());
    }
    
    @Test
    public void testSuccessionIgnoresContestedOwnership() {
        transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "Claim",
            new HashMap<>(),
            1000
        );
        
        // Succession should work despite contest (death overrides disputes)
        transferSystem.executeSuccession(
            testStructure,
            "char-heir",
            OwnerType.CHARACTER,
            TransferType.SUCCESSION_INHERITANCE,
            2000
        );
        
        assertEquals("char-heir", testStructure.getOwnerId());
    }
    
    @Test
    public void testCannotResolveAlreadyResolvedContest() {
        ContestedOwnership contest = transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "Claim",
            new HashMap<>(),
            1000
        );
        
        // Resolve once
        transferSystem.resolveContestedOwnershipInFavorOfOwner(contest, 2000);
        
        // Cannot resolve again
        assertThrows(IllegalStateException.class, () -> {
            transferSystem.resolveContestedOwnershipInFavorOfOwner(contest, 3000);
        });
    }
    
    @Test
    public void testCannotResolveExpiredContest() {
        ContestedOwnership contest = transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "Claim",
            new HashMap<>(),
            1000
        );
        
        // Move past expiry
        transferSystem.processExpiredContests(8200);
        
        // Cannot manually resolve expired contest
        assertThrows(IllegalStateException.class, () -> {
            transferSystem.resolveContestedOwnershipInFavorOfOwner(contest, 9000);
        });
    }
    
    @Test
    public void testGetAllContests() {
        transferSystem.contestOwnership(
            testStructure,
            "char-002",
            OwnerType.CHARACTER,
            "First claim",
            new HashMap<>(),
            1000
        );
        
        // Resolve it
        ContestedOwnership contest1 = transferSystem.getActiveContest("struct-001", 1000);
        transferSystem.resolveContestedOwnershipInFavorOfOwner(contest1, 2000);
        
        // Add another
        transferSystem.contestOwnership(
            testStructure,
            "char-003",
            OwnerType.CHARACTER,
            "Second claim",
            new HashMap<>(),
            3000
        );
        
        List<ContestedOwnership> all = transferSystem.getAllContests("struct-001");
        assertEquals(2, all.size());
    }
    
    // ===== TransferRecord Tests =====
    
    @Test
    public void testTransferRecordBuilder() {
        TransferRecord record = new TransferRecord.Builder()
            .structureId("struct-001")
            .fromOwnerId("char-001")
            .fromOwnerType(OwnerType.CHARACTER)
            .toOwnerId("char-002")
            .toOwnerType(OwnerType.CHARACTER)
            .transferType(TransferType.VOLUNTARY)
            .transferredAtTick(1000)
            .addMetadata("reason", "gift")
            .build();
        
        assertEquals("struct-001", record.getStructureId());
        assertEquals("char-002", record.getToOwnerId());
        assertEquals("gift", record.getMetadata().get("reason"));
    }
    
    @Test
    public void testTransferRecordRequiresStructureId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TransferRecord.Builder()
                .toOwnerId("char-002")
                .toOwnerType(OwnerType.CHARACTER)
                .transferType(TransferType.VOLUNTARY)
                .build();
        });
    }
    
    @Test
    public void testTransferRecordEquality() {
        TransferRecord r1 = new TransferRecord.Builder()
            .structureId("struct-001")
            .toOwnerId("char-002")
            .toOwnerType(OwnerType.CHARACTER)
            .transferType(TransferType.VOLUNTARY)
            .transferredAtTick(1000)
            .build();
        
        TransferRecord r2 = new TransferRecord.Builder()
            .structureId("struct-001")
            .toOwnerId("char-002")
            .toOwnerType(OwnerType.CHARACTER)
            .transferType(TransferType.VOLUNTARY)
            .transferredAtTick(1000)
            .build();
        
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
    
    // ===== ContestedOwnership Tests =====
    
    @Test
    public void testContestedOwnershipBuilder() {
        ContestedOwnership contest = new ContestedOwnership.Builder()
            .structureId("struct-001")
            .contestingPartyId("char-002")
            .contestingPartyType(OwnerType.CHARACTER)
            .claimBasis("Valid deed")
            .contestedAtTick(1000)
            .expiresAtTick(8200)
            .addEvidence("deed", "deed-001")
            .build();
        
        assertEquals("struct-001", contest.getStructureId());
        assertEquals("char-002", contest.getContestingPartyId());
        assertEquals("Valid deed", contest.getClaimBasis());
        assertEquals("deed-001", contest.getEvidence().get("deed"));
    }
    
    @Test
    public void testContestedOwnershipRequiresFields() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ContestedOwnership.Builder()
                .contestingPartyId("char-002")
                .contestingPartyType(OwnerType.CHARACTER)
                .claimBasis("Claim")
                .contestedAtTick(1000)
                .expiresAtTick(8200)
                .build();
        });
    }
    
    @Test
    public void testContestedOwnershipEquality() {
        ContestedOwnership c1 = new ContestedOwnership.Builder()
            .structureId("struct-001")
            .contestingPartyId("char-002")
            .contestingPartyType(OwnerType.CHARACTER)
            .claimBasis("Claim")
            .contestedAtTick(1000)
            .expiresAtTick(8200)
            .build();
        
        ContestedOwnership c2 = new ContestedOwnership.Builder()
            .structureId("struct-001")
            .contestingPartyId("char-002")
            .contestingPartyType(OwnerType.CHARACTER)
            .claimBasis("Claim")
            .contestedAtTick(1000)
            .expiresAtTick(8200)
            .build();
        
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
