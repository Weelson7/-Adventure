package org.adventure.structure;

import java.util.*;

/**
 * Manages taxation for structures in the game world.
 * Implements tax calculation, collection, grace periods, and seizure logic.
 * 
 * Default Parameters (from docs/specs_summary.md):
 * - Tax rate: 0.05 (5%)
 * - Tax cadence: weekly (7 in-game days)
 * - Grace period: 14 in-game days
 * - Seizure threshold: 21 in-game days after grace period
 * 
 * Formula: taxCollected = floor(taxableIncome * taxRate)
 * 
 * Consequences of unpaid taxes (in order):
 * 1. Warning (at due date)
 * 2. Grace period begins (14 days)
 * 3. Fines applied (during grace period)
 * 4. Asset lien (restricted actions)
 * 5. Seizure (transfer to government) after 21 days past grace
 * 
 * @see TaxRecord
 * @see Structure
 */
public final class TaxationSystem {
    // Conversion: 1 in-game day = 86400 ticks (assuming 1 tick = 1 second)
    private static final int TICKS_PER_DAY = 86400;
    
    // Default tax parameters (configurable per world preset)
    private final double defaultTaxRate;
    private final int taxCadenceDays;
    private final int gracePeriodDays;
    private final int seizureThresholdDays;
    
    // Tax records per structure
    private final Map<String, TaxRecord> taxRecords;
    
    /**
     * Create a taxation system with default parameters.
     */
    public TaxationSystem() {
        this(0.05, 7, 14, 21);
    }
    
    /**
     * Create a taxation system with custom parameters.
     * 
     * @param defaultTaxRate Default tax rate (e.g., 0.05 for 5%)
     * @param taxCadenceDays How often taxes are due (in game days)
     * @param gracePeriodDays Grace period before penalties (in game days)
     * @param seizureThresholdDays Days after grace before seizure (in game days)
     */
    public TaxationSystem(double defaultTaxRate, int taxCadenceDays, 
                         int gracePeriodDays, int seizureThresholdDays) {
        if (defaultTaxRate < 0 || defaultTaxRate > 1) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1");
        }
        if (taxCadenceDays <= 0) {
            throw new IllegalArgumentException("Tax cadence must be positive");
        }
        if (gracePeriodDays < 0) {
            throw new IllegalArgumentException("Grace period cannot be negative");
        }
        if (seizureThresholdDays < 0) {
            throw new IllegalArgumentException("Seizure threshold cannot be negative");
        }
        
        this.defaultTaxRate = defaultTaxRate;
        this.taxCadenceDays = taxCadenceDays;
        this.gracePeriodDays = gracePeriodDays;
        this.seizureThresholdDays = seizureThresholdDays;
        this.taxRecords = new HashMap<>();
    }
    
    /**
     * Register a structure for taxation.
     * Creates initial tax record with first tax due at nextTaxTick.
     * 
     * @param structureId The structure ID to register
     * @param currentTick Current game tick
     */
    public void registerStructure(String structureId, int currentTick) {
        if (structureId == null || structureId.isEmpty()) {
            throw new IllegalArgumentException("Structure ID cannot be null or empty");
        }
        
        int nextTaxDueTick = currentTick + (taxCadenceDays * TICKS_PER_DAY);
        
        TaxRecord record = new TaxRecord.Builder()
                .structureId(structureId)
                .lastTaxTick(currentTick)
                .nextTaxDueTick(nextTaxDueTick)
                .build();
        
        taxRecords.put(structureId, record);
    }
    
    /**
     * Unregister a structure from taxation (e.g., when destroyed).
     * 
     * @param structureId The structure ID to unregister
     */
    public void unregisterStructure(String structureId) {
        taxRecords.remove(structureId);
    }
    
    /**
     * Calculate tax owed for a structure based on taxable income.
     * 
     * @param taxableIncome The taxable income for the period
     * @param taxRate The applicable tax rate (use defaultTaxRate if null)
     * @return The amount of tax owed (floored to integer)
     */
    public double calculateTax(double taxableIncome, Double taxRate) {
        double rate = taxRate != null ? taxRate : defaultTaxRate;
        return Math.floor(taxableIncome * rate);
    }
    
    /**
     * Process tax collection for a structure.
     * Updates tax record with new tax owed and tracks payment status.
     * 
     * @param structureId The structure ID
     * @param taxableIncome Income generated since last tax period
     * @param currentTick Current game tick
     * @return The updated tax record
     */
    public TaxRecord processTaxCollection(String structureId, double taxableIncome, int currentTick) {
        TaxRecord record = taxRecords.get(structureId);
        if (record == null) {
            throw new IllegalArgumentException("Structure not registered for taxation: " + structureId);
        }
        
        // Calculate tax owed for this period
        double taxOwed = calculateTax(taxableIncome, null);
        
        // Update record
        record.setTaxableIncome(taxableIncome);
        record.setTaxOwed(record.getTaxOwed() + taxOwed); // Accumulate if unpaid
        
        // Set next tax due date
        int nextTaxDueTick = currentTick + (taxCadenceDays * TICKS_PER_DAY);
        record.setNextTaxDueTick(nextTaxDueTick);
        
        // Mark as unpaid if there's an outstanding balance and not already marked
        if (!record.isPaid() && record.getUnpaidSinceTick() == 0) {
            record.setUnpaidSinceTick(nextTaxDueTick); // Unpaid starts at next tax due tick
        }
        
        return record;
    }
    
    /**
     * Record a tax payment for a structure.
     * 
     * @param structureId The structure ID
     * @param amount Amount paid
     * @param currentTick Current game tick
     * @return The updated tax record
     */
    public TaxRecord recordPayment(String structureId, double amount, int currentTick) {
        if (amount < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative");
        }
        
        TaxRecord record = taxRecords.get(structureId);
        if (record == null) {
            throw new IllegalArgumentException("Structure not registered for taxation: " + structureId);
        }
        
        record.setTaxPaid(record.getTaxPaid() + amount);
        
        // Clear unpaid status if fully paid
        if (record.isPaid()) {
            record.setUnpaidSinceTick(0);
            record.setInGracePeriod(false);
            record.setUnderSeizureRisk(false);
        }
        
        return record;
    }
    
    /**
     * Update tax enforcement status for all structures.
     * Applies grace period logic and seizure risk flagging.
     * 
     * @param currentTick Current game tick
     * @return List of structure IDs that should be seized
     */
    public List<String> updateEnforcement(int currentTick) {
        List<String> structuresToSeize = new ArrayList<>();
        
        for (TaxRecord record : taxRecords.values()) {
            // Skip if taxes are paid
            if (record.isPaid()) {
                continue;
            }
            
            // Skip if not yet unpaid (unpaidSinceTick is set in processTaxCollection)
            if (record.getUnpaidSinceTick() == 0 || currentTick < record.getUnpaidSinceTick()) {
                continue;
            }
            
            int ticksSinceUnpaid = currentTick - record.getUnpaidSinceTick();
            int gracePeriodTicks = gracePeriodDays * TICKS_PER_DAY;
            int seizureThresholdTicks = (gracePeriodDays + seizureThresholdDays) * TICKS_PER_DAY;
            
            // Update grace period status
            if (ticksSinceUnpaid >= 0 && ticksSinceUnpaid < gracePeriodTicks) {
                if (!record.isInGracePeriod()) {
                    record.setInGracePeriod(true);
                    record.setGracePeriodStartTick(currentTick);
                }
            } else if (ticksSinceUnpaid >= gracePeriodTicks) {
                record.setInGracePeriod(false);
                record.setUnderSeizureRisk(true);
            }
            
            // Check for seizure
            if (ticksSinceUnpaid >= seizureThresholdTicks) {
                structuresToSeize.add(record.getStructureId());
            }
        }
        
        return structuresToSeize;
    }
    
    /**
     * Get tax record for a structure.
     * 
     * @param structureId The structure ID
     * @return The tax record, or null if not registered
     */
    public TaxRecord getTaxRecord(String structureId) {
        return taxRecords.get(structureId);
    }
    
    /**
     * Get all tax records.
     * 
     * @return Unmodifiable map of all tax records
     */
    public Map<String, TaxRecord> getAllTaxRecords() {
        return Collections.unmodifiableMap(taxRecords);
    }
    
    /**
     * Get all structures in grace period.
     * 
     * @return List of structure IDs in grace period
     */
    public List<String> getStructuresInGracePeriod() {
        List<String> result = new ArrayList<>();
        for (TaxRecord record : taxRecords.values()) {
            if (record.isInGracePeriod()) {
                result.add(record.getStructureId());
            }
        }
        return result;
    }
    
    /**
     * Get all structures under seizure risk.
     * 
     * @return List of structure IDs under seizure risk
     */
    public List<String> getStructuresUnderSeizureRisk() {
        List<String> result = new ArrayList<>();
        for (TaxRecord record : taxRecords.values()) {
            if (record.isUnderSeizureRisk()) {
                result.add(record.getStructureId());
            }
        }
        return result;
    }
    
    // Getters for configuration
    public double getDefaultTaxRate() {
        return defaultTaxRate;
    }
    
    public int getTaxCadenceDays() {
        return taxCadenceDays;
    }
    
    public int getGracePeriodDays() {
        return gracePeriodDays;
    }
    
    public int getSeizureThresholdDays() {
        return seizureThresholdDays;
    }
}
