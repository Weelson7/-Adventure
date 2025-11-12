package org.adventure.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Represents a tax record for a structure.
 * Tracks tax payments, unpaid amounts, and enforcement status.
 * 
 * Default tax rate: 0.05 (5%)
 * Default cadence: weekly (every 7 in-game days)
 * Grace period: 14 in-game days
 * Seizure threshold: 21 in-game days after grace period
 * 
 * @see TaxationSystem
 * @see Structure
 */
public final class TaxRecord {
    private final String structureId;
    private final int lastTaxTick;
    private int nextTaxDueTick;
    private double taxableIncome;
    private double taxOwed;
    private double taxPaid;
    private int unpaidSinceTick;
    private boolean inGracePeriod;
    private boolean underSeizureRisk;
    private int gracePeriodStartTick;
    private final int schemaVersion;
    
    @JsonCreator
    public TaxRecord(
            @JsonProperty("structureId") String structureId,
            @JsonProperty("lastTaxTick") int lastTaxTick,
            @JsonProperty("nextTaxDueTick") int nextTaxDueTick,
            @JsonProperty("taxableIncome") double taxableIncome,
            @JsonProperty("taxOwed") double taxOwed,
            @JsonProperty("taxPaid") double taxPaid,
            @JsonProperty("unpaidSinceTick") int unpaidSinceTick,
            @JsonProperty("inGracePeriod") boolean inGracePeriod,
            @JsonProperty("underSeizureRisk") boolean underSeizureRisk,
            @JsonProperty("gracePeriodStartTick") int gracePeriodStartTick,
            @JsonProperty("schemaVersion") int schemaVersion) {
        if (structureId == null || structureId.isEmpty()) {
            throw new IllegalArgumentException("Structure ID cannot be null or empty");
        }
        if (taxableIncome < 0) {
            throw new IllegalArgumentException("Taxable income cannot be negative");
        }
        if (taxOwed < 0) {
            throw new IllegalArgumentException("Tax owed cannot be negative");
        }
        if (taxPaid < 0) {
            throw new IllegalArgumentException("Tax paid cannot be negative");
        }
        
        this.structureId = structureId;
        this.lastTaxTick = lastTaxTick;
        this.nextTaxDueTick = nextTaxDueTick;
        this.taxableIncome = taxableIncome;
        this.taxOwed = taxOwed;
        this.taxPaid = taxPaid;
        this.unpaidSinceTick = unpaidSinceTick;
        this.inGracePeriod = inGracePeriod;
        this.underSeizureRisk = underSeizureRisk;
        this.gracePeriodStartTick = gracePeriodStartTick;
        this.schemaVersion = schemaVersion;
    }
    
    /**
     * Builder for creating TaxRecord instances.
     */
    public static class Builder {
        private String structureId;
        private int lastTaxTick = 0;
        private int nextTaxDueTick = 0;
        private double taxableIncome = 0.0;
        private double taxOwed = 0.0;
        private double taxPaid = 0.0;
        private int unpaidSinceTick = 0;
        private boolean inGracePeriod = false;
        private boolean underSeizureRisk = false;
        private int gracePeriodStartTick = 0;
        private int schemaVersion = 1;
        
        public Builder structureId(String structureId) {
            this.structureId = structureId;
            return this;
        }
        
        public Builder lastTaxTick(int lastTaxTick) {
            this.lastTaxTick = lastTaxTick;
            return this;
        }
        
        public Builder nextTaxDueTick(int nextTaxDueTick) {
            this.nextTaxDueTick = nextTaxDueTick;
            return this;
        }
        
        public Builder taxableIncome(double taxableIncome) {
            this.taxableIncome = taxableIncome;
            return this;
        }
        
        public Builder taxOwed(double taxOwed) {
            this.taxOwed = taxOwed;
            return this;
        }
        
        public Builder taxPaid(double taxPaid) {
            this.taxPaid = taxPaid;
            return this;
        }
        
        public Builder unpaidSinceTick(int unpaidSinceTick) {
            this.unpaidSinceTick = unpaidSinceTick;
            return this;
        }
        
        public Builder inGracePeriod(boolean inGracePeriod) {
            this.inGracePeriod = inGracePeriod;
            return this;
        }
        
        public Builder underSeizureRisk(boolean underSeizureRisk) {
            this.underSeizureRisk = underSeizureRisk;
            return this;
        }
        
        public Builder gracePeriodStartTick(int gracePeriodStartTick) {
            this.gracePeriodStartTick = gracePeriodStartTick;
            return this;
        }
        
        public Builder schemaVersion(int schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }
        
        public TaxRecord build() {
            return new TaxRecord(structureId, lastTaxTick, nextTaxDueTick,
                               taxableIncome, taxOwed, taxPaid, unpaidSinceTick,
                               inGracePeriod, underSeizureRisk, gracePeriodStartTick,
                               schemaVersion);
        }
    }
    
    // Getters
    public String getStructureId() {
        return structureId;
    }
    
    public int getLastTaxTick() {
        return lastTaxTick;
    }
    
    public int getNextTaxDueTick() {
        return nextTaxDueTick;
    }
    
    public double getTaxableIncome() {
        return taxableIncome;
    }
    
    public double getTaxOwed() {
        return taxOwed;
    }
    
    public double getTaxPaid() {
        return taxPaid;
    }
    
    public int getUnpaidSinceTick() {
        return unpaidSinceTick;
    }
    
    public boolean isInGracePeriod() {
        return inGracePeriod;
    }
    
    public boolean isUnderSeizureRisk() {
        return underSeizureRisk;
    }
    
    public int getGracePeriodStartTick() {
        return gracePeriodStartTick;
    }
    
    public int getSchemaVersion() {
        return schemaVersion;
    }
    
    /**
     * Check if taxes are fully paid.
     */
    public boolean isPaid() {
        return taxPaid >= taxOwed;
    }
    
    /**
     * Get the outstanding tax balance.
     */
    public double getOutstandingBalance() {
        return Math.max(0, taxOwed - taxPaid);
    }
    
    // Setters for mutable fields (package-private, only TaxationSystem should modify)
    void setNextTaxDueTick(int nextTaxDueTick) {
        this.nextTaxDueTick = nextTaxDueTick;
    }
    
    void setTaxableIncome(double taxableIncome) {
        this.taxableIncome = Math.max(0, taxableIncome);
    }
    
    void setTaxOwed(double taxOwed) {
        this.taxOwed = Math.max(0, taxOwed);
    }
    
    void setTaxPaid(double taxPaid) {
        this.taxPaid = Math.max(0, taxPaid);
    }
    
    void setUnpaidSinceTick(int unpaidSinceTick) {
        this.unpaidSinceTick = unpaidSinceTick;
    }
    
    void setInGracePeriod(boolean inGracePeriod) {
        this.inGracePeriod = inGracePeriod;
    }
    
    void setUnderSeizureRisk(boolean underSeizureRisk) {
        this.underSeizureRisk = underSeizureRisk;
    }
    
    void setGracePeriodStartTick(int gracePeriodStartTick) {
        this.gracePeriodStartTick = gracePeriodStartTick;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TaxRecord)) return false;
        TaxRecord other = (TaxRecord) obj;
        return Objects.equals(structureId, other.structureId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(structureId);
    }
    
    @Override
    public String toString() {
        return "TaxRecord{structureId='" + structureId + 
               "', taxOwed=" + taxOwed +
               ", taxPaid=" + taxPaid +
               ", outstanding=" + getOutstandingBalance() +
               ", inGracePeriod=" + inGracePeriod +
               ", underSeizureRisk=" + underSeizureRisk + "}";
    }
}
