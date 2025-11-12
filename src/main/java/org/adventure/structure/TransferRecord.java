package org.adventure.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable record of an ownership transfer.
 * Tracks who, what, when, and how ownership changed hands.
 * Stored in Structure's transfer history for audit trail.
 * 
 * @see Structure
 * @see OwnershipTransferSystem
 * @see TransferType
 */
public final class TransferRecord {
    private final String structureId;
    private final String fromOwnerId;
    private final OwnerType fromOwnerType;
    private final String toOwnerId;
    private final OwnerType toOwnerType;
    private final TransferType transferType;
    private final int transferredAtTick;
    private final Map<String, Object> metadata;
    private final int schemaVersion;
    
    @JsonCreator
    public TransferRecord(
            @JsonProperty("structureId") String structureId,
            @JsonProperty("fromOwnerId") String fromOwnerId,
            @JsonProperty("fromOwnerType") OwnerType fromOwnerType,
            @JsonProperty("toOwnerId") String toOwnerId,
            @JsonProperty("toOwnerType") OwnerType toOwnerType,
            @JsonProperty("transferType") TransferType transferType,
            @JsonProperty("transferredAtTick") int transferredAtTick,
            @JsonProperty("metadata") Map<String, Object> metadata,
            @JsonProperty("schemaVersion") int schemaVersion) {
        
        if (structureId == null || structureId.isEmpty()) {
            throw new IllegalArgumentException("structureId cannot be null or empty");
        }
        if (toOwnerId == null || toOwnerId.isEmpty()) {
            throw new IllegalArgumentException("toOwnerId cannot be null or empty");
        }
        if (toOwnerType == null) {
            throw new IllegalArgumentException("toOwnerType cannot be null");
        }
        if (transferType == null) {
            throw new IllegalArgumentException("transferType cannot be null");
        }
        if (transferredAtTick < 0) {
            throw new IllegalArgumentException("transferredAtTick cannot be negative");
        }
        
        this.structureId = structureId;
        this.fromOwnerId = fromOwnerId;
        this.fromOwnerType = fromOwnerType;
        this.toOwnerId = toOwnerId;
        this.toOwnerType = toOwnerType;
        this.transferType = transferType;
        this.transferredAtTick = transferredAtTick;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.schemaVersion = schemaVersion;
    }
    
    // Getters
    public String getStructureId() { return structureId; }
    public String getFromOwnerId() { return fromOwnerId; }
    public OwnerType getFromOwnerType() { return fromOwnerType; }
    public String getToOwnerId() { return toOwnerId; }
    public OwnerType getToOwnerType() { return toOwnerType; }
    public TransferType getTransferType() { return transferType; }
    public int getTransferredAtTick() { return transferredAtTick; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public int getSchemaVersion() { return schemaVersion; }
    
    /**
     * Builder for TransferRecord.
     */
    public static class Builder {
        private String structureId;
        private String fromOwnerId = "";
        private OwnerType fromOwnerType = OwnerType.NONE;
        private String toOwnerId;
        private OwnerType toOwnerType;
        private TransferType transferType;
        private int transferredAtTick;
        private Map<String, Object> metadata = new HashMap<>();
        private int schemaVersion = 1;
        
        public Builder structureId(String structureId) {
            this.structureId = structureId;
            return this;
        }
        
        public Builder fromOwnerId(String fromOwnerId) {
            this.fromOwnerId = fromOwnerId;
            return this;
        }
        
        public Builder fromOwnerType(OwnerType fromOwnerType) {
            this.fromOwnerType = fromOwnerType;
            return this;
        }
        
        public Builder toOwnerId(String toOwnerId) {
            this.toOwnerId = toOwnerId;
            return this;
        }
        
        public Builder toOwnerType(OwnerType toOwnerType) {
            this.toOwnerType = toOwnerType;
            return this;
        }
        
        public Builder transferType(TransferType transferType) {
            this.transferType = transferType;
            return this;
        }
        
        public Builder transferredAtTick(int transferredAtTick) {
            this.transferredAtTick = transferredAtTick;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = new HashMap<>(metadata);
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder schemaVersion(int schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }
        
        public TransferRecord build() {
            return new TransferRecord(
                structureId,
                fromOwnerId,
                fromOwnerType,
                toOwnerId,
                toOwnerType,
                transferType,
                transferredAtTick,
                metadata,
                schemaVersion
            );
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferRecord that = (TransferRecord) o;
        return transferredAtTick == that.transferredAtTick &&
               Objects.equals(structureId, that.structureId) &&
               Objects.equals(fromOwnerId, that.fromOwnerId) &&
               fromOwnerType == that.fromOwnerType &&
               Objects.equals(toOwnerId, that.toOwnerId) &&
               toOwnerType == that.toOwnerType &&
               transferType == that.transferType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(structureId, transferredAtTick, transferType);
    }
    
    @Override
    public String toString() {
        return String.format("TransferRecord{structure=%s, from=%s(%s), to=%s(%s), type=%s, tick=%d}",
            structureId, fromOwnerId, fromOwnerType, toOwnerId, toOwnerType, transferType, transferredAtTick);
    }
}
