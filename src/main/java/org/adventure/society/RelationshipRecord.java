package org.adventure.society;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Tracks diplomatic relationships between societies/clans.
 * Metrics are bounded and decay over time if not maintained.
 * 
 * Metrics:
 * - Reputation: -100 (hostile) to +100 (trusted ally)
 * - Influence: 0 (no leverage) to 100 (dominant)
 * - Alignment: -100 (opposing values) to +100 (shared values)
 * - Race Affinity: -50 (racial tension) to +50 (racial kinship)
 * 
 * Design:
 * - Immutable after creation (modifications return new instances)
 * - Validates all metrics within defined bounds
 * - Tracks last update tick for decay calculations
 * 
 * @see Clan for usage in clan relationships
 */
public class RelationshipRecord {
    private final String targetSocietyId;
    private final double reputation;    // -100 to +100
    private final double influence;     // 0 to 100
    private final double alignment;     // -100 to +100
    private final double raceAffinity;  // -50 to +50
    private final long lastUpdatedTick;

    @JsonCreator
    public RelationshipRecord(
            @JsonProperty("targetSocietyId") String targetSocietyId,
            @JsonProperty("reputation") double reputation,
            @JsonProperty("influence") double influence,
            @JsonProperty("alignment") double alignment,
            @JsonProperty("raceAffinity") double raceAffinity,
            @JsonProperty("lastUpdatedTick") long lastUpdatedTick) {
        this.targetSocietyId = Objects.requireNonNull(targetSocietyId, "Target society ID cannot be null");
        this.reputation = clamp(reputation, -100, 100);
        this.influence = clamp(influence, 0, 100);
        this.alignment = clamp(alignment, -100, 100);
        this.raceAffinity = clamp(raceAffinity, -50, 50);
        this.lastUpdatedTick = lastUpdatedTick;
    }

    // Getters
    public String getTargetSocietyId() { return targetSocietyId; }
    public double getReputation() { return reputation; }
    public double getInfluence() { return influence; }
    public double getAlignment() { return alignment; }
    public double getRaceAffinity() { return raceAffinity; }
    public long getLastUpdatedTick() { return lastUpdatedTick; }

    /**
     * Calculate alliance strength based on reputation and alignment.
     * Must be > 30 to form alliances.
     * @return alliance strength value
     */
    public double getAllianceStrength() {
        return (reputation + alignment) / 2.0;
    }

    /**
     * Calculate war likelihood based on negative reputation.
     * Higher negative reputation increases war probability.
     * @return war likelihood [0.0, 1.0]
     */
    public double getWarLikelihood() {
        return Math.max(0, (-reputation - 20) / 50.0);
    }

    /**
     * Check if alliance is possible (alliance strength > 30).
     * @return true if alliance can be formed
     */
    public boolean canFormAlliance() {
        return getAllianceStrength() > 30;
    }

    /**
     * Update reputation value.
     * @param newReputation new reputation value
     * @param currentTick current game tick
     * @return new RelationshipRecord with updated reputation
     */
    public RelationshipRecord withReputation(double newReputation, long currentTick) {
        return new RelationshipRecord(targetSocietyId, newReputation, influence, 
                                     alignment, raceAffinity, currentTick);
    }

    /**
     * Update influence value.
     * @param newInfluence new influence value
     * @param currentTick current game tick
     * @return new RelationshipRecord with updated influence
     */
    public RelationshipRecord withInfluence(double newInfluence, long currentTick) {
        return new RelationshipRecord(targetSocietyId, reputation, newInfluence, 
                                     alignment, raceAffinity, currentTick);
    }

    /**
     * Update alignment value.
     * @param newAlignment new alignment value
     * @param currentTick current game tick
     * @return new RelationshipRecord with updated alignment
     */
    public RelationshipRecord withAlignment(double newAlignment, long currentTick) {
        return new RelationshipRecord(targetSocietyId, reputation, influence, 
                                     newAlignment, raceAffinity, currentTick);
    }

    /**
     * Update race affinity value.
     * @param newRaceAffinity new race affinity value
     * @param currentTick current game tick
     * @return new RelationshipRecord with updated race affinity
     */
    public RelationshipRecord withRaceAffinity(double newRaceAffinity, long currentTick) {
        return new RelationshipRecord(targetSocietyId, reputation, influence, 
                                     alignment, newRaceAffinity, currentTick);
    }

    /**
     * Apply time-based decay to metrics.
     * - Reputation decays toward 0
     * - Influence decays if not maintained
     * - Alignment has minimal decay
     * 
     * @param ticksSinceUpdate ticks since last update
     * @param currentTick current game tick
     * @return new RelationshipRecord with decayed values
     */
    public RelationshipRecord applyDecay(long ticksSinceUpdate, long currentTick) {
        // Reputation decay: toward neutral (0) at 0.01 per 100 ticks
        double decayedReputation = reputation;
        if (reputation != 0) {
            double reputationDecay = -Math.signum(reputation) * 0.01 * (ticksSinceUpdate / 100.0);
            decayedReputation = reputation + reputationDecay;
            // Stop at zero
            if (Math.signum(reputation) != Math.signum(decayedReputation)) {
                decayedReputation = 0;
            }
        }

        // Influence decay: -0.05 per 100 ticks
        double decayedInfluence = Math.max(0, influence - 0.05 * (ticksSinceUpdate / 100.0));

        // Alignment decay: minimal -0.001 per tick
        double decayedAlignment = alignment - 0.001 * ticksSinceUpdate;
        decayedAlignment = Math.max(-100, Math.min(100, decayedAlignment));

        // Race affinity is static (no decay)
        return new RelationshipRecord(targetSocietyId, decayedReputation, decayedInfluence,
                                     decayedAlignment, raceAffinity, currentTick);
    }

    /**
     * Clamp a value between min and max.
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipRecord that = (RelationshipRecord) o;
        return targetSocietyId.equals(that.targetSocietyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetSocietyId);
    }

    @Override
    public String toString() {
        return "RelationshipRecord{" +
                "target='" + targetSocietyId + '\'' +
                ", reputation=" + reputation +
                ", influence=" + influence +
                ", alignment=" + alignment +
                ", raceAffinity=" + raceAffinity +
                ", allianceStrength=" + getAllianceStrength() +
                '}';
    }
}
