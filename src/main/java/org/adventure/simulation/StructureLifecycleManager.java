package org.adventure.simulation;

import org.adventure.society.Clan;
import org.adventure.structure.Structure;
import org.adventure.structure.StructureType;
import org.adventure.structure.OwnerType;

import java.util.*;

/**
 * Manages structure lifecycle: natural disasters, attacks, neglect, and conversion to ruins.
 * 
 * <p>Destruction Triggers:
 * <ul>
 *   <li><b>Natural Disasters:</b> Random events (earthquake, fire, flood) with 5% chance per 1000 ticks</li>
 *   <li><b>Rival Attacks:</b> Handled by ClanExpansionSimulator warfare system</li>
 *   <li><b>Neglect:</b> Unpaid taxes cause -5% health decay per week (from Phase 1.5 taxation)</li>
 * </ul>
 * 
 * <p>When structure health reaches 0, it converts to ANCIENT_RUINS (can be rebuilt or looted).
 * 
 * @see docs/BUILD_PHASE1.10.x.md Phase 1.10.3 specification
 */
public class StructureLifecycleManager {
    private static final int DISASTER_CHECK_INTERVAL = 1000;
    private static final double DISASTER_CHANCE = 0.05; // 5% chance per check
    private static final int NEGLECT_CHECK_INTERVAL = 7000; // ~1 week in ticks
    private static final double NEGLECT_DECAY_PERCENT = 0.05; // 5% health loss
    
    public StructureLifecycleManager() {
        // Default constructor
    }
    
    /**
     * Simulates one tick of structure lifecycle for all structures.
     * Handles disasters, neglect, and ruin conversion.
     * 
     * @param structures List of all structures (mutable, destroyed structures converted to ruins)
     * @param clans List of all clans (for neglect/tax checking)
     * @param currentTick Current game tick
     */
    public void simulateTick(
        List<Structure> structures,
        List<Clan> clans,
        long currentTick
    ) {
        List<Structure> toConvert = new ArrayList<>();
        
        for (Structure structure : structures) {
            if (structure.isDestroyed()) {
                toConvert.add(structure);
                continue; // Already destroyed, mark for conversion
            }
            
            // Check for natural disasters
            if (currentTick % DISASTER_CHECK_INTERVAL == 0) {
                checkForDisasters(structure, currentTick);
            }
            
            // Check for neglect (unpaid taxes, abandonment)
            if (currentTick % NEGLECT_CHECK_INTERVAL == 0) {
                checkForNeglect(structure, clans, currentTick);
            }
        }
        
        // Convert destroyed structures to ruins
        for (Structure destroyed : toConvert) {
            structures.remove(destroyed);
            Structure ruin = convertToRuin(destroyed, currentTick);
            structures.add(ruin);
        }
    }
    
    /**
     * Checks for and applies natural disaster damage to a structure.
     * 
     * <p>Disaster types:
     * <ul>
     *   <li><b>Earthquake:</b> 30-50% damage, 10% chance to destroy outright</li>
     *   <li><b>Fire:</b> 40-60% damage, wooden structures (HOUSE) more vulnerable</li>
     *   <li><b>Flood:</b> 20-30% damage, low-elevation structures affected</li>
     * </ul>
     * 
     * @param structure The structure to check
     * @param currentTick Current game tick
     */
    private void checkForDisasters(Structure structure, long currentTick) {
        // Re-seed RNG for determinism based on structure and tick
        Random rng = new Random(currentTick ^ structure.getId().hashCode());
        
        if (rng.nextDouble() >= DISASTER_CHANCE) {
            return; // No disaster
        }
        
        // Determine disaster type
        DisasterType disasterType = DisasterType.values()[rng.nextInt(DisasterType.values().length)];
        
        double damage = 0.0;
        boolean instantDestroy = false;
        
        switch (disasterType) {
            case EARTHQUAKE:
                damage = 30.0 + rng.nextDouble() * 20.0; // 30-50%
                instantDestroy = rng.nextDouble() < 0.10; // 10% chance to destroy
                break;
                
            case FIRE:
                damage = 40.0 + rng.nextDouble() * 20.0; // 40-60%
                // Wooden structures (HOUSE) take extra damage
                if (structure.getType() == StructureType.HOUSE) {
                    damage *= 1.5;
                }
                break;
                
            case FLOOD:
                damage = 20.0 + rng.nextDouble() * 10.0; // 20-30%
                // Low-elevation structures would take more damage
                // (requires elevation data, simplified for now)
                break;
        }
        
        if (instantDestroy) {
            structure.takeDamage(structure.getHealth(), (int) currentTick); // Destroy completely
        } else {
            structure.takeDamage(damage, (int) currentTick);
        }
        
        // TODO: Create disaster event for story system
    }
    
    /**
     * Checks for neglect-based decay (unpaid taxes, abandonment).
     * 
     * <p>Structures decay if owner clan has insufficient treasury or no active members.
     * Decay rate: -5% health per week until structure reaches 0 health.
     * 
     * @param structure The structure to check
     * @param clans List of all clans
     * @param currentTick Current game tick
     */
    private void checkForNeglect(Structure structure, List<Clan> clans, long currentTick) {
        if (structure.getOwnerType() != OwnerType.CLAN) {
            return; // Only clan-owned structures can be neglected
        }
        
        // Find owner clan
        Clan owner = clans.stream()
            .filter(clan -> clan.getId().equals(structure.getOwnerId()))
            .findFirst()
            .orElse(null);
        
        if (owner == null) {
            // Clan no longer exists (destroyed/merged), structure is abandoned
            applyNeglectDecay(structure, currentTick);
            return;
        }
        
        // Check if clan has sufficient treasury for maintenance
        // Simple rule: If treasury < 100 gold, structures start decaying
        if (owner.getTreasury() < 100.0) {
            applyNeglectDecay(structure, currentTick);
        }
        
        // Check if clan has been inactive for too long
        long inactivityTicks = currentTick - owner.getLastActiveTick();
        if (inactivityTicks > 50000) { // ~5 years of inactivity
            applyNeglectDecay(structure, currentTick);
        }
    }
    
    /**
     * Applies neglect decay to a structure.
     * 
     * @param structure The structure to decay
     * @param currentTick Current game tick
     */
    private void applyNeglectDecay(Structure structure, long currentTick) {
        double decayAmount = structure.getMaxHealth() * NEGLECT_DECAY_PERCENT;
        structure.takeDamage(decayAmount, (int) currentTick);
    }
    
    /**
     * Converts a destroyed structure into an ANCIENT_RUINS structure.
     * Ruins preserve original location and some metadata.
     * 
     * @param original The destroyed structure
     * @param currentTick Current game tick
     * @return New ruin structure
     */
    private Structure convertToRuin(Structure original, long currentTick) {
        String ruinId = original.getId() + "_ruin";
        
        return new Structure.Builder()
            .id(ruinId)
            .type(StructureType.ANCIENT_RUINS)
            .ownerId("") // Ruins have no owner
            .ownerType(OwnerType.NONE)
            .locationTileId(original.getLocationTileId())
            .health(0.0)
            .maxHealth(original.getMaxHealth())
            .createdAtTick((int) currentTick)
            .lastUpdatedTick((int) currentTick)
            .entrance(original.getEntrance())
            .build();
    }
    
    /**
     * Enum for disaster types.
     */
    private enum DisasterType {
        EARTHQUAKE,
        FIRE,
        FLOOD
    }
}
