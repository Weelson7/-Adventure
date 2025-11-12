package org.adventure.structure;

/**
 * Enumeration of ownership transfer types for structures.
 * Each type represents a different mechanism by which ownership can change hands.
 * 
 * @see OwnershipTransferSystem
 * @see TransferRecord
 */
public enum TransferType {
    /**
     * Voluntary transfer between two parties by mutual agreement.
     * Requires consent from current owner.
     */
    VOLUNTARY("Voluntary transfer by mutual agreement"),
    
    /**
     * Transfer via sale transaction.
     * Requires payment and consent from both parties.
     */
    SALE("Sale transaction with payment"),
    
    /**
     * Succession via inheritance (family bloodline).
     * Automatic transfer on owner death to closest living relative.
     */
    SUCCESSION_INHERITANCE("Inheritance through family bloodline"),
    
    /**
     * Succession via will (explicit designation).
     * Transfer according to owner's written will.
     */
    SUCCESSION_WILL("Succession according to owner's will"),
    
    /**
     * Succession via clan heir designation.
     * For clan-owned structures, transfer to designated heir.
     */
    SUCCESSION_HEIR("Succession to designated clan heir"),
    
    /**
     * Transfer via conquest (military takeover).
     * Forced transfer through combat or siege.
     */
    CONQUEST("Conquest through military force"),
    
    /**
     * Transfer via tax seizure by government.
     * Automatic transfer when taxes unpaid beyond threshold.
     */
    TAX_SEIZURE("Government seizure for unpaid taxes"),
    
    /**
     * Transfer from abandoned structure.
     * Structure reverts to unowned (NONE) or government control.
     */
    ABANDONED("Abandoned structure claimed or returned");
    
    private final String description;
    
    TransferType(String description) {
        this.description = description;
    }
    
    /**
     * @return Human-readable description of this transfer type
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return True if this transfer type requires payment
     */
    public boolean requiresPayment() {
        return this == SALE;
    }
    
    /**
     * @return True if this transfer type requires consent from current owner
     */
    public boolean requiresConsent() {
        return this == VOLUNTARY || this == SALE;
    }
    
    /**
     * @return True if this transfer type is forced (no consent required)
     */
    public boolean isForced() {
        return this == CONQUEST || this == TAX_SEIZURE || this == ABANDONED;
    }
    
    /**
     * @return True if this transfer type is a succession event
     */
    public boolean isSuccession() {
        return this == SUCCESSION_INHERITANCE || this == SUCCESSION_WILL || this == SUCCESSION_HEIR;
    }
}
