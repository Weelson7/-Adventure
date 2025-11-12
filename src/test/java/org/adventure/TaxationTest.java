package org.adventure;

import org.adventure.structure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for the TaxationSystem class.
 * 
 * Tests cover:
 * - Tax calculation and collection
 * - Grace period logic
 * - Seizure threshold enforcement
 * - Payment recording
 * - Multi-structure taxation
 * 
 * Default parameters tested:
 * - Tax rate: 0.05 (5%)
 * - Cadence: 7 days
 * - Grace period: 14 days
 * - Seizure threshold: 21 days
 */
public class TaxationTest {
    
    private static final int TICKS_PER_DAY = 86400;
    private TaxationSystem taxSystem;
    
    @BeforeEach
    public void setUp() {
        taxSystem = new TaxationSystem();
    }
    
    @Test
    public void testDefaultParameters() {
        assertEquals(0.05, taxSystem.getDefaultTaxRate(), 0.001);
        assertEquals(7, taxSystem.getTaxCadenceDays());
        assertEquals(14, taxSystem.getGracePeriodDays());
        assertEquals(21, taxSystem.getSeizureThresholdDays());
    }
    
    @Test
    public void testCustomParameters() {
        TaxationSystem custom = new TaxationSystem(0.10, 14, 7, 30);
        assertEquals(0.10, custom.getDefaultTaxRate(), 0.001);
        assertEquals(14, custom.getTaxCadenceDays());
        assertEquals(7, custom.getGracePeriodDays());
        assertEquals(30, custom.getSeizureThresholdDays());
    }
    
    @Test
    public void testInvalidTaxRate() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TaxationSystem(-0.05, 7, 14, 21);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new TaxationSystem(1.5, 7, 14, 21);
        });
    }
    
    @Test
    public void testInvalidCadence() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TaxationSystem(0.05, 0, 14, 21);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new TaxationSystem(0.05, -7, 14, 21);
        });
    }
    
    @Test
    public void testRegisterStructure() {
        taxSystem.registerStructure("struct-001", 0);
        
        TaxRecord record = taxSystem.getTaxRecord("struct-001");
        assertNotNull(record);
        assertEquals("struct-001", record.getStructureId());
        assertEquals(0, record.getLastTaxTick());
        assertEquals(7 * TICKS_PER_DAY, record.getNextTaxDueTick());
        assertEquals(0.0, record.getTaxOwed());
        assertEquals(0.0, record.getTaxPaid());
        assertTrue(record.isPaid());
    }
    
    @Test
    public void testUnregisterStructure() {
        taxSystem.registerStructure("struct-001", 0);
        assertNotNull(taxSystem.getTaxRecord("struct-001"));
        
        taxSystem.unregisterStructure("struct-001");
        assertNull(taxSystem.getTaxRecord("struct-001"));
    }
    
    @Test
    public void testCalculateTax() {
        double tax = taxSystem.calculateTax(1000.0, null);
        assertEquals(50.0, tax);  // 5% of 1000 = 50
        
        tax = taxSystem.calculateTax(1234.56, null);
        assertEquals(61.0, tax);  // floor(5% of 1234.56) = floor(61.728) = 61
    }
    
    @Test
    public void testCalculateTaxWithCustomRate() {
        double tax = taxSystem.calculateTax(1000.0, 0.10);
        assertEquals(100.0, tax);  // 10% of 1000 = 100
    }
    
    @Test
    public void testProcessTaxCollection() {
        taxSystem.registerStructure("struct-001", 0);
        
        // Process tax collection with 1000 taxable income
        int currentTick = 7 * TICKS_PER_DAY;  // After 7 days
        TaxRecord record = taxSystem.processTaxCollection("struct-001", 1000.0, currentTick);
        
        assertNotNull(record);
        assertEquals(1000.0, record.getTaxableIncome());
        assertEquals(50.0, record.getTaxOwed());  // 5% of 1000
        assertEquals(0.0, record.getTaxPaid());
        assertFalse(record.isPaid());
        assertEquals(14 * TICKS_PER_DAY, record.getNextTaxDueTick());  // Next tax due after 7 more days
    }
    
    @Test
    public void testProcessTaxCollectionNotRegistered() {
        assertThrows(IllegalArgumentException.class, () -> {
            taxSystem.processTaxCollection("struct-999", 1000.0, 0);
        });
    }
    
    @Test
    public void testRecordPayment() {
        taxSystem.registerStructure("struct-001", 0);
        taxSystem.processTaxCollection("struct-001", 1000.0, 0);
        
        TaxRecord record = taxSystem.getTaxRecord("struct-001");
        assertEquals(50.0, record.getTaxOwed());
        assertFalse(record.isPaid());
        
        // Make partial payment
        taxSystem.recordPayment("struct-001", 30.0, 100);
        record = taxSystem.getTaxRecord("struct-001");
        assertEquals(30.0, record.getTaxPaid());
        assertEquals(20.0, record.getOutstandingBalance());
        assertFalse(record.isPaid());
        
        // Complete payment
        taxSystem.recordPayment("struct-001", 20.0, 200);
        record = taxSystem.getTaxRecord("struct-001");
        assertEquals(50.0, record.getTaxPaid());
        assertEquals(0.0, record.getOutstandingBalance());
        assertTrue(record.isPaid());
        assertEquals(0, record.getUnpaidSinceTick());
    }
    
    @Test
    public void testRecordPaymentRejectsNegative() {
        taxSystem.registerStructure("struct-001", 0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            taxSystem.recordPayment("struct-001", -10.0, 0);
        });
    }
    
    @Test
    public void testRecordPaymentNotRegistered() {
        assertThrows(IllegalArgumentException.class, () -> {
            taxSystem.recordPayment("struct-999", 100.0, 0);
        });
    }
    
    @Test
    public void testUpdateEnforcementGracePeriod() {
        taxSystem.registerStructure("struct-001", 0);
        taxSystem.processTaxCollection("struct-001", 1000.0, 0);
        
        // Tax is unpaid, move forward into grace period (7 days after due)
        int gracePeriodTick = 10 * TICKS_PER_DAY;
        List<String> toSeize = taxSystem.updateEnforcement(gracePeriodTick);
        
        assertTrue(toSeize.isEmpty());  // Not yet at seizure threshold
        
        TaxRecord record = taxSystem.getTaxRecord("struct-001");
        assertTrue(record.isInGracePeriod());
        assertFalse(record.isUnderSeizureRisk());
        
        List<String> inGrace = taxSystem.getStructuresInGracePeriod();
        assertEquals(1, inGrace.size());
        assertTrue(inGrace.contains("struct-001"));
    }
    
    @Test
    public void testUpdateEnforcementSeizureRisk() {
        taxSystem.registerStructure("struct-001", 0);
        taxSystem.processTaxCollection("struct-001", 1000.0, 0);
        
        // Tax is due at 7 days (nextTaxDueTick), unpaid starts there
        // Grace period ends at 7 + 14 = 21 days
        // Move past grace period (21 days) into seizure risk zone (but before seizure at 21+21=42)
        int seizureRiskTick = 25 * TICKS_PER_DAY;
        List<String> toSeize = taxSystem.updateEnforcement(seizureRiskTick);
        
        assertTrue(toSeize.isEmpty());  // Not yet at seizure threshold (42 days)
        
        TaxRecord record = taxSystem.getTaxRecord("struct-001");
        assertFalse(record.isInGracePeriod());
        assertTrue(record.isUnderSeizureRisk());
        
        List<String> underRisk = taxSystem.getStructuresUnderSeizureRisk();
        assertEquals(1, underRisk.size());
        assertTrue(underRisk.contains("struct-001"));
    }
    
    @Test
    public void testUpdateEnforcementSeizure() {
        taxSystem.registerStructure("struct-001", 0);
        taxSystem.processTaxCollection("struct-001", 1000.0, 0);
        
        // Move to seizure threshold (14 grace + 21 seizure = 35 days after initial due date)
        // Initial due date was at 7 days (from registration at tick 0)
        // So seizure happens at 7 + 14 + 21 = 42 days
        int seizureTick = 42 * TICKS_PER_DAY;
        List<String> toSeize = taxSystem.updateEnforcement(seizureTick);
        
        assertEquals(1, toSeize.size());
        assertTrue(toSeize.contains("struct-001"));
        
        TaxRecord record = taxSystem.getTaxRecord("struct-001");
        assertTrue(record.isUnderSeizureRisk());
    }
    
    @Test
    public void testUpdateEnforcementPaidStructuresNotSeized() {
        taxSystem.registerStructure("struct-001", 0);
        taxSystem.processTaxCollection("struct-001", 1000.0, 0);
        taxSystem.recordPayment("struct-001", 50.0, 100);  // Pay in full
        
        // Move to seizure threshold
        int seizureTick = 42 * TICKS_PER_DAY;
        List<String> toSeize = taxSystem.updateEnforcement(seizureTick);
        
        assertTrue(toSeize.isEmpty());  // Paid structures not seized
        
        TaxRecord record = taxSystem.getTaxRecord("struct-001");
        assertFalse(record.isInGracePeriod());
        assertFalse(record.isUnderSeizureRisk());
    }
    
    @Test
    public void testMultipleStructuresTaxation() {
        taxSystem.registerStructure("struct-001", 0);
        taxSystem.registerStructure("struct-002", 0);
        taxSystem.registerStructure("struct-003", 0);
        
        // Process taxes for all
        taxSystem.processTaxCollection("struct-001", 1000.0, 0);
        taxSystem.processTaxCollection("struct-002", 2000.0, 0);
        taxSystem.processTaxCollection("struct-003", 500.0, 0);
        
        // Pay one structure in full
        taxSystem.recordPayment("struct-002", 100.0, 100);
        
        // Move to seizure threshold
        int seizureTick = 42 * TICKS_PER_DAY;
        List<String> toSeize = taxSystem.updateEnforcement(seizureTick);
        
        // Two structures should be seized (001 and 003), but not 002 (paid)
        assertEquals(2, toSeize.size());
        assertTrue(toSeize.contains("struct-001"));
        assertFalse(toSeize.contains("struct-002"));
        assertTrue(toSeize.contains("struct-003"));
    }
    
    @Test
    public void testAccumulatedUnpaidTaxes() {
        taxSystem.registerStructure("struct-001", 0);
        
        // First tax period
        taxSystem.processTaxCollection("struct-001", 1000.0, 7 * TICKS_PER_DAY);
        TaxRecord record = taxSystem.getTaxRecord("struct-001");
        assertEquals(50.0, record.getTaxOwed());
        
        // Second tax period without payment (taxes accumulate)
        taxSystem.processTaxCollection("struct-001", 1000.0, 14 * TICKS_PER_DAY);
        record = taxSystem.getTaxRecord("struct-001");
        assertEquals(100.0, record.getTaxOwed());  // 50 + 50
        
        // Third tax period
        taxSystem.processTaxCollection("struct-001", 1000.0, 21 * TICKS_PER_DAY);
        record = taxSystem.getTaxRecord("struct-001");
        assertEquals(150.0, record.getTaxOwed());  // 50 + 50 + 50
    }
    
    @Test
    public void testGetAllTaxRecords() {
        taxSystem.registerStructure("struct-001", 0);
        taxSystem.registerStructure("struct-002", 0);
        taxSystem.registerStructure("struct-003", 0);
        
        var allRecords = taxSystem.getAllTaxRecords();
        assertEquals(3, allRecords.size());
        assertTrue(allRecords.containsKey("struct-001"));
        assertTrue(allRecords.containsKey("struct-002"));
        assertTrue(allRecords.containsKey("struct-003"));
    }
    
    @Test
    public void testGracePeriodExactThreshold() {
        // Test the exact boundary of grace period
        taxSystem.registerStructure("struct-001", 0);
        taxSystem.processTaxCollection("struct-001", 1000.0, 0);
        
        // At exactly 14 days (end of grace period)
        int exactGraceEnd = 7 * TICKS_PER_DAY + 14 * TICKS_PER_DAY;  // 7 days initial + 14 grace
        taxSystem.updateEnforcement(exactGraceEnd);
        
        TaxRecord record = taxSystem.getTaxRecord("struct-001");
        assertFalse(record.isInGracePeriod());
        assertTrue(record.isUnderSeizureRisk());
    }
    
    @Test
    public void testSeizureExactThreshold() {
        // Test the exact boundary of seizure threshold
        taxSystem.registerStructure("struct-001", 0);
        taxSystem.processTaxCollection("struct-001", 1000.0, 0);
        
        // At exactly 35 days after unpaid (7 initial + 14 grace + 21 seizure threshold)
        // But unpaidSinceTick is set when first check happens at nextTaxDueTick
        int firstCheck = 7 * TICKS_PER_DAY;
        taxSystem.updateEnforcement(firstCheck);  // Mark as unpaid
        
        // Now at exactly seizure threshold
        int exactSeizure = firstCheck + 14 * TICKS_PER_DAY + 21 * TICKS_PER_DAY;
        List<String> toSeize = taxSystem.updateEnforcement(exactSeizure);
        
        assertEquals(1, toSeize.size());
        assertTrue(toSeize.contains("struct-001"));
    }
}
