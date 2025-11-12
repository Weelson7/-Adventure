package org.adventure.society;

import java.util.*;

/**
 * Manages diplomatic interactions and relationship updates between clans.
 * Handles event-driven updates (trade, war, betrayal) and periodic decay.
 * 
 * Design:
 * - Validates relationship metric bounds
 * - Applies decay formulas per specs_summary.md
 * - Supports event-driven updates (trade, betrayal, gifts)
 * - Tracks update history for auditing
 * 
 * @see RelationshipRecord for relationship data
 * @see Clan for clan relationships
 */
public class Diplomacy {
    
    /**
     * Process periodic decay for all relationships.
     * Should be called every 100 ticks per specs_summary.md.
     * 
     * @param clan the clan to process
     * @param currentTick the current game tick
     * @return new Clan with decayed relationships
     */
    public static Clan processPeriodicDecay(Clan clan, long currentTick) {
        Objects.requireNonNull(clan, "Clan cannot be null");
        
        Map<String, RelationshipRecord> updatedRelationships = new HashMap<>();
        
        for (Map.Entry<String, RelationshipRecord> entry : clan.getRelationships().entrySet()) {
            RelationshipRecord relationship = entry.getValue();
            long ticksSinceUpdate = currentTick - relationship.getLastUpdatedTick();
            
            RelationshipRecord decayed = relationship.applyDecay(ticksSinceUpdate, currentTick);
            updatedRelationships.put(entry.getKey(), decayed);
        }
        
        Clan.Builder builder = new Clan.Builder(clan);
        builder.relationships(updatedRelationships);
        builder.lastActiveTick(currentTick);
        
        return builder.build();
    }
    
    /**
     * Apply effects of a successful trade mission.
     * Increases reputation and influence.
     * 
     * @param clan the clan performing the trade
     * @param targetSocietyId the target society
     * @param currentTick the current game tick
     * @return new Clan with updated relationship
     */
    public static Clan applyTradeMission(Clan clan, String targetSocietyId, long currentTick) {
        Objects.requireNonNull(clan, "Clan cannot be null");
        Objects.requireNonNull(targetSocietyId, "Target society ID cannot be null");
        
        RelationshipRecord existing = clan.getRelationships().get(targetSocietyId);
        if (existing == null) {
            // Create new neutral relationship
            existing = new RelationshipRecord(targetSocietyId, 0, 0, 0, 0, currentTick);
        }
        
        // Trade mission: +5 reputation, +2 influence
        RelationshipRecord updated = existing
                .withReputation(existing.getReputation() + 5, currentTick)
                .withInfluence(existing.getInfluence() + 2, currentTick);
        
        return clan.updateRelationship(updated);
    }
    
    /**
     * Apply effects of a betrayal event.
     * Severely damages reputation.
     * 
     * @param clan the clan that was betrayed
     * @param betrayerId the betraying society ID
     * @param currentTick the current game tick
     * @return new Clan with updated relationship
     */
    public static Clan applyBetrayal(Clan clan, String betrayerId, long currentTick) {
        Objects.requireNonNull(clan, "Clan cannot be null");
        Objects.requireNonNull(betrayerId, "Betrayer ID cannot be null");
        
        RelationshipRecord existing = clan.getRelationships().get(betrayerId);
        if (existing == null) {
            existing = new RelationshipRecord(betrayerId, 0, 0, 0, 0, currentTick);
        }
        
        // Betrayal: -30 reputation
        RelationshipRecord updated = existing.withReputation(existing.getReputation() - 30, currentTick);
        
        return clan.updateRelationship(updated);
    }
    
    /**
     * Apply effects of a diplomatic gift.
     * Increases reputation and alignment.
     * 
     * @param clan the clan giving the gift
     * @param targetSocietyId the target society
     * @param currentTick the current game tick
     * @return new Clan with updated relationship
     */
    public static Clan applyDiplomaticGift(Clan clan, String targetSocietyId, long currentTick) {
        Objects.requireNonNull(clan, "Clan cannot be null");
        Objects.requireNonNull(targetSocietyId, "Target society ID cannot be null");
        
        RelationshipRecord existing = clan.getRelationships().get(targetSocietyId);
        if (existing == null) {
            existing = new RelationshipRecord(targetSocietyId, 0, 0, 0, 0, currentTick);
        }
        
        // Diplomatic gift: +3 reputation, +1 alignment
        RelationshipRecord updated = existing
                .withReputation(existing.getReputation() + 3, currentTick)
                .withAlignment(existing.getAlignment() + 1, currentTick);
        
        return clan.updateRelationship(updated);
    }
    
    /**
     * Apply effects of a war declaration.
     * Severely damages reputation and alignment.
     * 
     * @param clan the clan declaring war
     * @param targetSocietyId the target society
     * @param currentTick the current game tick
     * @return new Clan with updated relationship
     */
    public static Clan applyWarDeclaration(Clan clan, String targetSocietyId, long currentTick) {
        Objects.requireNonNull(clan, "Clan cannot be null");
        Objects.requireNonNull(targetSocietyId, "Target society ID cannot be null");
        
        RelationshipRecord existing = clan.getRelationships().get(targetSocietyId);
        if (existing == null) {
            existing = new RelationshipRecord(targetSocietyId, 0, 0, 0, 0, currentTick);
        }
        
        // War declaration: -40 reputation, -20 alignment
        RelationshipRecord updated = existing
                .withReputation(existing.getReputation() - 40, currentTick)
                .withAlignment(existing.getAlignment() - 20, currentTick);
        
        return clan.updateRelationship(updated);
    }
    
    /**
     * Form an alliance between two clans if conditions are met.
     * Requires alliance strength > 30.
     * 
     * @param clan the clan proposing alliance
     * @param targetSocietyId the target society
     * @param currentTick the current game tick
     * @return new Clan with updated relationship if alliance formed
     * @throws IllegalStateException if alliance requirements not met
     */
    public static Clan formAlliance(Clan clan, String targetSocietyId, long currentTick) {
        Objects.requireNonNull(clan, "Clan cannot be null");
        Objects.requireNonNull(targetSocietyId, "Target society ID cannot be null");
        
        RelationshipRecord existing = clan.getRelationships().get(targetSocietyId);
        if (existing == null) {
            existing = new RelationshipRecord(targetSocietyId, 0, 0, 0, 0, currentTick);
        }
        
        if (!existing.canFormAlliance()) {
            throw new IllegalStateException(
                "Cannot form alliance: alliance strength " + existing.getAllianceStrength() + 
                " must be > 30. Current reputation: " + existing.getReputation() + 
                ", alignment: " + existing.getAlignment());
        }
        
        // Forming alliance boosts reputation and alignment further
        RelationshipRecord updated = existing
                .withReputation(existing.getReputation() + 10, currentTick)
                .withAlignment(existing.getAlignment() + 10, currentTick);
        
        return clan.updateRelationship(updated);
    }
    
    /**
     * Check if war is likely between two clans based on relationship.
     * 
     * @param clan the clan to check
     * @param targetSocietyId the potential enemy
     * @return true if war likelihood > 0.5
     */
    public static boolean isWarLikely(Clan clan, String targetSocietyId) {
        Objects.requireNonNull(clan, "Clan cannot be null");
        Objects.requireNonNull(targetSocietyId, "Target society ID cannot be null");
        
        RelationshipRecord relationship = clan.getRelationships().get(targetSocietyId);
        if (relationship == null) {
            return false; // No relationship = no war
        }
        
        return relationship.getWarLikelihood() > 0.5;
    }
    
    /**
     * Get all societies with which this clan has positive relations (reputation > 0).
     * 
     * @param clan the clan to check
     * @return list of society IDs with positive relations
     */
    public static List<String> getPositiveRelations(Clan clan) {
        Objects.requireNonNull(clan, "Clan cannot be null");
        
        List<String> positive = new ArrayList<>();
        for (Map.Entry<String, RelationshipRecord> entry : clan.getRelationships().entrySet()) {
            if (entry.getValue().getReputation() > 0) {
                positive.add(entry.getKey());
            }
        }
        return positive;
    }
    
    /**
     * Get all societies with which this clan has negative relations (reputation < 0).
     * 
     * @param clan the clan to check
     * @return list of society IDs with negative relations
     */
    public static List<String> getNegativeRelations(Clan clan) {
        Objects.requireNonNull(clan, "Clan cannot be null");
        
        List<String> negative = new ArrayList<>();
        for (Map.Entry<String, RelationshipRecord> entry : clan.getRelationships().entrySet()) {
            if (entry.getValue().getReputation() < 0) {
                negative.add(entry.getKey());
            }
        }
        return negative;
    }
}
