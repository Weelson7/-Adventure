package org.adventure.structure;

import java.util.*;

/**
 * Manages ownership transfers for structures.
 * Handles voluntary transfers, sales, succession, conquest, and contested ownership.
 * Maintains transfer history and enforces transfer rules.
 * 
 * Key responsibilities:
 * - Validate transfer requests
 * - Execute ownership changes
 * - Track transfer history
 * - Handle contested ownership disputes
 * - Resolve expired contests
 * 
 * @see Structure
 * @see TransferRecord
 * @see ContestedOwnership
 * @see TransferType
 */
public final class OwnershipTransferSystem {
    
    private final int contestedExpiryTicks;
    private final Map<String, List<ContestedOwnership>> activeContests;
    
    /**
     * Create ownership transfer system with default parameters.
     * Default contested expiry: 7200 ticks (2 hours @ 1 tick/second).
     */
    public OwnershipTransferSystem() {
        this(7200);
    }
    
    /**
     * Create ownership transfer system with custom parameters.
     * 
     * @param contestedExpiryTicks Ticks until contested ownership expires
     */
    public OwnershipTransferSystem(int contestedExpiryTicks) {
        if (contestedExpiryTicks < 0) {
            throw new IllegalArgumentException("contestedExpiryTicks cannot be negative");
        }
        this.contestedExpiryTicks = contestedExpiryTicks;
        this.activeContests = new HashMap<>();
    }
    
    public int getContestedExpiryTicks() {
        return contestedExpiryTicks;
    }
    
    /**
     * Execute a voluntary transfer between two parties.
     * 
     * @param structure Structure to transfer
     * @param toOwnerId New owner ID
     * @param toOwnerType New owner type
     * @param currentTick Current game tick
     * @return Transfer record
     * @throws IllegalStateException if structure has active contested ownership
     */
    public TransferRecord executeVoluntaryTransfer(
            Structure structure,
            String toOwnerId,
            OwnerType toOwnerType,
            int currentTick) {
        
        validateTransfer(structure, toOwnerId, toOwnerType, currentTick);
        
        return executeTransfer(
            structure,
            toOwnerId,
            toOwnerType,
            TransferType.VOLUNTARY,
            currentTick,
            new HashMap<>()
        );
    }
    
    /**
     * Execute a sale transfer with payment.
     * 
     * @param structure Structure to transfer
     * @param toOwnerId New owner ID
     * @param toOwnerType New owner type
     * @param price Sale price
     * @param currentTick Current game tick
     * @return Transfer record
     * @throws IllegalStateException if structure has active contested ownership
     */
    public TransferRecord executeSale(
            Structure structure,
            String toOwnerId,
            OwnerType toOwnerType,
            double price,
            int currentTick) {
        
        validateTransfer(structure, toOwnerId, toOwnerType, currentTick);
        
        if (price < 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("price", price);
        metadata.put("currency", "gold");
        
        return executeTransfer(
            structure,
            toOwnerId,
            toOwnerType,
            TransferType.SALE,
            currentTick,
            metadata
        );
    }
    
    /**
     * Execute succession transfer (inheritance, will, or heir).
     * 
     * @param structure Structure to transfer
     * @param toOwnerId New owner ID (heir)
     * @param toOwnerType New owner type
     * @param successionType Type of succession (INHERITANCE, WILL, or HEIR)
     * @param currentTick Current game tick
     * @return Transfer record
     * @throws IllegalArgumentException if successionType is not a succession type
     */
    public TransferRecord executeSuccession(
            Structure structure,
            String toOwnerId,
            OwnerType toOwnerType,
            TransferType successionType,
            int currentTick) {
        
        if (!successionType.isSuccession()) {
            throw new IllegalArgumentException("Transfer type must be a succession type");
        }
        
        // Succession ignores contested ownership (death overrides disputes)
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("deceasedOwner", structure.getOwnerId());
        metadata.put("deceasedOwnerType", structure.getOwnerType());
        
        return executeTransfer(
            structure,
            toOwnerId,
            toOwnerType,
            successionType,
            currentTick,
            metadata
        );
    }
    
    /**
     * Execute conquest transfer (forced military takeover).
     * 
     * @param structure Structure to transfer
     * @param conquerorId Conqueror ID
     * @param conquerorType Conqueror type
     * @param currentTick Current game tick
     * @return Transfer record
     */
    public TransferRecord executeConquest(
            Structure structure,
            String conquerorId,
            OwnerType conquerorType,
            int currentTick) {
        
        // Conquest clears contested ownership (might makes right)
        clearContestedOwnership(structure.getId());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("conquestVictory", true);
        metadata.put("defeatedOwner", structure.getOwnerId());
        
        return executeTransfer(
            structure,
            conquerorId,
            conquerorType,
            TransferType.CONQUEST,
            currentTick,
            metadata
        );
    }
    
    /**
     * Contest current ownership of a structure.
     * 
     * @param structure Structure being contested
     * @param contestingPartyId Contesting party ID
     * @param contestingPartyType Contesting party type
     * @param claimBasis Reason for contesting ownership
     * @param evidence Evidence supporting the contest
     * @param currentTick Current game tick
     * @return Contested ownership record
     * @throws IllegalStateException if structure already has active contest
     */
    public ContestedOwnership contestOwnership(
            Structure structure,
            String contestingPartyId,
            OwnerType contestingPartyType,
            String claimBasis,
            Map<String, Object> evidence,
            int currentTick) {
        
        if (hasActiveContest(structure.getId(), currentTick)) {
            throw new IllegalStateException(
                "Structure " + structure.getId() + " already has active contested ownership"
            );
        }
        
        ContestedOwnership contest = new ContestedOwnership.Builder()
            .structureId(structure.getId())
            .contestingPartyId(contestingPartyId)
            .contestingPartyType(contestingPartyType)
            .claimBasis(claimBasis)
            .contestedAtTick(currentTick)
            .expiresAtTick(currentTick + contestedExpiryTicks)
            .evidence(evidence != null ? evidence : new HashMap<>())
            .build();
        
        activeContests.computeIfAbsent(structure.getId(), k -> new ArrayList<>()).add(contest);
        
        return contest;
    }
    
    /**
     * Resolve contested ownership in favor of contesting party.
     * Rolls back original transfer and transfers to contesting party.
     * 
     * @param structure Structure being contested
     * @param contest Contest to resolve
     * @param currentTick Current game tick
     * @return Transfer record of resolution
     */
    public TransferRecord resolveContestedOwnershipInFavorOfContestant(
            Structure structure,
            ContestedOwnership contest,
            int currentTick) {
        
        if (contest.isResolved()) {
            throw new IllegalStateException("Contest already resolved");
        }
        
        if (contest.isExpired(currentTick)) {
            throw new IllegalStateException("Contest has expired");
        }
        
        // Mark contest as resolved
        contest.setResolved(true);
        contest.setResolvedAtTick(currentTick);
        contest.setResolutionOutcome("GRANTED");
        
        // Transfer to contesting party
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contestResolution", "GRANTED");
        metadata.put("originalDispute", contest.getClaimBasis());
        
        return executeTransfer(
            structure,
            contest.getContestingPartyId(),
            contest.getContestingPartyType(),
            TransferType.VOLUNTARY, // Use voluntary as default resolved transfer
            currentTick,
            metadata
        );
    }
    
    /**
     * Resolve contested ownership in favor of current owner.
     * Marks contest as resolved and denied.
     * 
     * @param contest Contest to resolve
     * @param currentTick Current game tick
     */
    public void resolveContestedOwnershipInFavorOfOwner(
            ContestedOwnership contest,
            int currentTick) {
        
        if (contest.isResolved()) {
            throw new IllegalStateException("Contest already resolved");
        }
        
        if (contest.isExpired(currentTick)) {
            throw new IllegalStateException("Contest has expired");
        }
        
        contest.setResolved(true);
        contest.setResolvedAtTick(currentTick);
        contest.setResolutionOutcome("DENIED");
    }
    
    /**
     * Process all expired contests and auto-deny them.
     * 
     * @param currentTick Current game tick
     * @return List of expired contest structure IDs
     */
    public List<String> processExpiredContests(int currentTick) {
        List<String> expiredStructureIds = new ArrayList<>();
        
        for (Map.Entry<String, List<ContestedOwnership>> entry : activeContests.entrySet()) {
            String structureId = entry.getKey();
            List<ContestedOwnership> contests = entry.getValue();
            
            for (ContestedOwnership contest : contests) {
                if (contest.isExpired(currentTick)) {
                    contest.setResolved(true);
                    contest.setResolvedAtTick(currentTick);
                    contest.setResolutionOutcome("EXPIRED");
                    expiredStructureIds.add(structureId);
                }
            }
        }
        
        return expiredStructureIds;
    }
    
    /**
     * Check if structure has active contested ownership.
     * 
     * @param structureId Structure ID to check
     * @param currentTick Current game tick
     * @return True if structure has active contest
     */
    public boolean hasActiveContest(String structureId, int currentTick) {
        List<ContestedOwnership> contests = activeContests.get(structureId);
        if (contests == null) {
            return false;
        }
        
        return contests.stream().anyMatch(c -> c.isActive(currentTick));
    }
    
    /**
     * Get active contest for structure (null if none).
     * 
     * @param structureId Structure ID
     * @param currentTick Current game tick
     * @return Active contest or null
     */
    public ContestedOwnership getActiveContest(String structureId, int currentTick) {
        List<ContestedOwnership> contests = activeContests.get(structureId);
        if (contests == null) {
            return null;
        }
        
        return contests.stream()
            .filter(c -> c.isActive(currentTick))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get all contests for structure (active, resolved, expired).
     * 
     * @param structureId Structure ID
     * @return Unmodifiable list of contests
     */
    public List<ContestedOwnership> getAllContests(String structureId) {
        List<ContestedOwnership> contests = activeContests.get(structureId);
        return contests != null ? Collections.unmodifiableList(contests) : Collections.emptyList();
    }
    
    /**
     * Clear all contested ownership for a structure.
     * Used when structure is destroyed or forcibly transferred (conquest, tax seizure).
     * 
     * @param structureId Structure ID
     */
    public void clearContestedOwnership(String structureId) {
        activeContests.remove(structureId);
    }
    
    // Private helper methods
    
    private void validateTransfer(
            Structure structure,
            String toOwnerId,
            OwnerType toOwnerType,
            int currentTick) {
        
        if (toOwnerId == null || toOwnerId.isEmpty()) {
            throw new IllegalArgumentException("toOwnerId cannot be null or empty");
        }
        
        if (toOwnerType == null) {
            throw new IllegalArgumentException("toOwnerType cannot be null");
        }
        
        if (hasActiveContest(structure.getId(), currentTick)) {
            throw new IllegalStateException(
                "Cannot transfer structure " + structure.getId() + " with active contested ownership"
            );
        }
    }
    
    private TransferRecord executeTransfer(
            Structure structure,
            String toOwnerId,
            OwnerType toOwnerType,
            TransferType transferType,
            int currentTick,
            Map<String, Object> metadata) {
        
        // Create transfer record
        TransferRecord record = new TransferRecord.Builder()
            .structureId(structure.getId())
            .fromOwnerId(structure.getOwnerId())
            .fromOwnerType(structure.getOwnerType())
            .toOwnerId(toOwnerId)
            .toOwnerType(toOwnerType)
            .transferType(transferType)
            .transferredAtTick(currentTick)
            .metadata(metadata)
            .build();
        
        // Execute transfer on structure
        structure.transferOwnership(toOwnerId, toOwnerType, currentTick);
        
        return record;
    }
}
