package org.adventure.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a contested ownership dispute for a structure.
 * Disputes expire after a configured period (default 7200 ticks).
 * Contains evidence, witnesses, and resolution mechanism.
 * 
 * @see Structure
 * @see OwnershipTransferSystem
 */
public final class ContestedOwnership {
    private final String structureId;
    private final String contestingPartyId;
    private final OwnerType contestingPartyType;
    private final String claimBasis;
    private final int contestedAtTick;
    private final int expiresAtTick;
    private final Map<String, Object> evidence;
    private final int schemaVersion;
    
    // Mutable resolution state (package-private setters for OwnershipTransferSystem)
    private boolean resolved;
    private int resolvedAtTick;
    private String resolutionOutcome;
    
    @JsonCreator
    public ContestedOwnership(
            @JsonProperty("structureId") String structureId,
            @JsonProperty("contestingPartyId") String contestingPartyId,
            @JsonProperty("contestingPartyType") OwnerType contestingPartyType,
            @JsonProperty("claimBasis") String claimBasis,
            @JsonProperty("contestedAtTick") int contestedAtTick,
            @JsonProperty("expiresAtTick") int expiresAtTick,
            @JsonProperty("evidence") Map<String, Object> evidence,
            @JsonProperty("resolved") boolean resolved,
            @JsonProperty("resolvedAtTick") int resolvedAtTick,
            @JsonProperty("resolutionOutcome") String resolutionOutcome,
            @JsonProperty("schemaVersion") int schemaVersion) {
        
        if (structureId == null || structureId.isEmpty()) {
            throw new IllegalArgumentException("structureId cannot be null or empty");
        }
        if (contestingPartyId == null || contestingPartyId.isEmpty()) {
            throw new IllegalArgumentException("contestingPartyId cannot be null or empty");
        }
        if (contestingPartyType == null) {
            throw new IllegalArgumentException("contestingPartyType cannot be null");
        }
        if (claimBasis == null || claimBasis.isEmpty()) {
            throw new IllegalArgumentException("claimBasis cannot be null or empty");
        }
        if (contestedAtTick < 0) {
            throw new IllegalArgumentException("contestedAtTick cannot be negative");
        }
        if (expiresAtTick < contestedAtTick) {
            throw new IllegalArgumentException("expiresAtTick must be >= contestedAtTick");
        }
        
        this.structureId = structureId;
        this.contestingPartyId = contestingPartyId;
        this.contestingPartyType = contestingPartyType;
        this.claimBasis = claimBasis;
        this.contestedAtTick = contestedAtTick;
        this.expiresAtTick = expiresAtTick;
        this.evidence = evidence != null ? new HashMap<>(evidence) : new HashMap<>();
        this.resolved = resolved;
        this.resolvedAtTick = resolvedAtTick;
        this.resolutionOutcome = resolutionOutcome;
        this.schemaVersion = schemaVersion;
    }
    
    // Getters
    public String getStructureId() { return structureId; }
    public String getContestingPartyId() { return contestingPartyId; }
    public OwnerType getContestingPartyType() { return contestingPartyType; }
    public String getClaimBasis() { return claimBasis; }
    public int getContestedAtTick() { return contestedAtTick; }
    public int getExpiresAtTick() { return expiresAtTick; }
    public Map<String, Object> getEvidence() { return new HashMap<>(evidence); }
    public boolean isResolved() { return resolved; }
    public int getResolvedAtTick() { return resolvedAtTick; }
    public String getResolutionOutcome() { return resolutionOutcome; }
    public int getSchemaVersion() { return schemaVersion; }
    
    /**
     * @param currentTick Current game tick
     * @return True if this contest has expired
     */
    public boolean isExpired(int currentTick) {
        return currentTick >= expiresAtTick && !resolved;
    }
    
    /**
     * @param currentTick Current game tick
     * @return True if this contest is still active (not expired, not resolved)
     */
    public boolean isActive(int currentTick) {
        return !resolved && currentTick < expiresAtTick;
    }
    
    // Package-private setters for OwnershipTransferSystem
    void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
    
    void setResolvedAtTick(int resolvedAtTick) {
        this.resolvedAtTick = resolvedAtTick;
    }
    
    void setResolutionOutcome(String resolutionOutcome) {
        this.resolutionOutcome = resolutionOutcome;
    }
    
    /**
     * Builder for ContestedOwnership.
     */
    public static class Builder {
        private String structureId;
        private String contestingPartyId;
        private OwnerType contestingPartyType;
        private String claimBasis;
        private int contestedAtTick;
        private int expiresAtTick;
        private Map<String, Object> evidence = new HashMap<>();
        private boolean resolved = false;
        private int resolvedAtTick = 0;
        private String resolutionOutcome = "";
        private int schemaVersion = 1;
        
        public Builder structureId(String structureId) {
            this.structureId = structureId;
            return this;
        }
        
        public Builder contestingPartyId(String contestingPartyId) {
            this.contestingPartyId = contestingPartyId;
            return this;
        }
        
        public Builder contestingPartyType(OwnerType contestingPartyType) {
            this.contestingPartyType = contestingPartyType;
            return this;
        }
        
        public Builder claimBasis(String claimBasis) {
            this.claimBasis = claimBasis;
            return this;
        }
        
        public Builder contestedAtTick(int contestedAtTick) {
            this.contestedAtTick = contestedAtTick;
            return this;
        }
        
        public Builder expiresAtTick(int expiresAtTick) {
            this.expiresAtTick = expiresAtTick;
            return this;
        }
        
        public Builder evidence(Map<String, Object> evidence) {
            this.evidence = new HashMap<>(evidence);
            return this;
        }
        
        public Builder addEvidence(String key, Object value) {
            this.evidence.put(key, value);
            return this;
        }
        
        public Builder resolved(boolean resolved) {
            this.resolved = resolved;
            return this;
        }
        
        public Builder resolvedAtTick(int resolvedAtTick) {
            this.resolvedAtTick = resolvedAtTick;
            return this;
        }
        
        public Builder resolutionOutcome(String resolutionOutcome) {
            this.resolutionOutcome = resolutionOutcome;
            return this;
        }
        
        public Builder schemaVersion(int schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }
        
        public ContestedOwnership build() {
            return new ContestedOwnership(
                structureId,
                contestingPartyId,
                contestingPartyType,
                claimBasis,
                contestedAtTick,
                expiresAtTick,
                evidence,
                resolved,
                resolvedAtTick,
                resolutionOutcome,
                schemaVersion
            );
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContestedOwnership that = (ContestedOwnership) o;
        return contestedAtTick == that.contestedAtTick &&
               Objects.equals(structureId, that.structureId) &&
               Objects.equals(contestingPartyId, that.contestingPartyId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(structureId, contestingPartyId, contestedAtTick);
    }
    
    @Override
    public String toString() {
        return String.format("ContestedOwnership{structure=%s, contesting=%s(%s), basis=%s, expires=%d, resolved=%s}",
            structureId, contestingPartyId, contestingPartyType, claimBasis, expiresAtTick, resolved);
    }
}
