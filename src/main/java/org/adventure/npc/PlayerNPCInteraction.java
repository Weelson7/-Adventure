package org.adventure.npc;

import org.adventure.structure.Structure;

import java.util.List;
import java.util.Random;

/**
 * Handles interactions between player characters and NPCs.
 * Includes marriage, reproduction, and relationship mechanics.
 */
public class PlayerNPCInteraction {
    
    /**
     * Checks if a player can marry a target NPC.
     * 
     * @param player The player NPC (isPlayer=true)
     * @param target The target NPC to marry
     * @return true if marriage is allowed
     */
    public boolean canMarry(NamedNPC player, NamedNPC target) {
        if (!player.isPlayer()) {
            return false; // Not a player
        }
        
        if (player.isMarried() || target.isMarried()) {
            return false; // One or both already married
        }
        
        if (player.getAge() < 18 || target.getAge() < 18) {
            return false; // Both must be adults
        }
        
        if (player.getGender() == target.getGender()) {
            // Same-gender marriage currently not implemented
            // Can be changed in future versions
            return false;
        }
        
        // TODO: Add relationship/reputation requirements
        // if (getRelationship(player, target) < 75) return false;
        
        return true;
    }
    
    /**
     * Marries a player to a target NPC.
     * 
     * @param player The player NPC
     * @param target The target NPC
     * @throws IllegalArgumentException if marriage not allowed
     */
    public void marry(NamedNPC player, NamedNPC target) {
        if (!canMarry(player, target)) {
            throw new IllegalArgumentException("Cannot marry these NPCs");
        }
        
        player.setSpouseId(target.getId());
        target.setSpouseId(player.getId());
        
        // Reset reproduction cooldowns
        player.setLastReproductionCheck(0L);
        target.setLastReproductionCheck(0L);
        
        // TODO: Generate marriage event
        // TODO: Update relationship scores
    }
    
    /**
     * Attempts reproduction for a player-controlled NPC.
     * Player controls the timing (no automatic reproduction).
     * 
     * @param player The player NPC
     * @param allNpcs List of all NPCs
     * @param structures List of all structures
     * @param currentTick Current game tick
     * @param rng Random number generator
     * @return The newborn child, or null if reproduction failed
     */
    public NamedNPC tryReproduceAsPlayer(
        NamedNPC player,
        List<NamedNPC> allNpcs,
        List<Structure> structures,
        long currentTick,
        Random rng
    ) {
        if (!player.isPlayer()) {
            throw new IllegalArgumentException("Not a player NPC");
        }
        
        if (player.getSpouseId() == null) {
            return null; // Not married
        }
        
        if (player.getFertility() == 0) {
            return null; // Not fertile
        }
        
        // Find spouse
        NamedNPC spouse = allNpcs.stream()
            .filter(n -> n.getId().equals(player.getSpouseId()))
            .findFirst()
            .orElse(null);
        
        if (spouse == null || spouse.getFertility() == 0) {
            return null; // Spouse not found or not fertile
        }
        
        // Check home capacity (max 4 NPCs per house)
        Structure home = structures.stream()
            .filter(s -> s.getId().equals(player.getHomeStructureId()))
            .findFirst()
            .orElse(null);
        
        if (home == null) {
            return null;
        }
        
        long occupants = allNpcs.stream()
            .filter(n -> n.getHomeStructureId().equals(home.getId()))
            .count();
        
        if (occupants >= 4) {
            return null; // House full
        }
        
        // Check cooldown (minimum 5000 ticks between attempts)
        if (currentTick - player.getLastReproductionCheck() < 5000) {
            return null; // Too soon
        }
        
        // Reproduction succeeds based on fertility
        double chance = Math.max(player.getFertility(), spouse.getFertility()) / 100.0;
        
        if (rng.nextDouble() >= chance) {
            player.setLastReproductionCheck(currentTick);
            return null; // Failed conception
        }
        
        // Create child
        Gender childGender = rng.nextBoolean() ? Gender.MALE : Gender.FEMALE;
        
        // Generate deterministic child ID
        String childId = generateChildId(player, spouse, currentTick, rng);
        
        NamedNPC child = new NamedNPC.Builder()
            .id(childId)
            .name(generateChildName(childGender, rng))
            .clanId(player.getClanId())
            .age(0)
            .gender(childGender)
            .job(NPCJob.CHILD)
            .homeStructureId(player.getHomeStructureId())
            .workplaceStructureId(null)
            .spouseId(null)
            .childrenIds(List.of())
            .fertility(0)
            .isPlayer(false) // Children start as NPCs
            .birthTick(currentTick)
            .lastReproductionCheck(0L)
            .build();
        
        // Update parents
        player.addChild(child.getId());
        spouse.addChild(child.getId());
        player.setLastReproductionCheck(currentTick);
        
        // Add to world
        allNpcs.add(child);
        
        // TODO: Create "Birth" event
        // TODO: Child may inherit player traits/stats
        
        return child;
    }
    
    /**
     * Generates a name for a child.
     * 
     * @param gender Child's gender
     * @param rng Random number generator
     * @return Generated name
     */
    private String generateChildName(Gender gender, Random rng) {
        List<String> names = (gender == Gender.MALE)
            ? List.of("Aldric", "Borin", "Cedric", "Daven", "Elric", "Gareth", "Hadrian", "Ivor", "Jorah", "Kael")
            : List.of("Aria", "Brynn", "Celia", "Dessa", "Elara", "Freya", "Gwen", "Helia", "Isolde", "Kira");
        
        return names.get(rng.nextInt(names.size()));
    }
    
    /**
     * Generates a deterministic child ID based on parents and birth tick.
     * 
     * @param parent1 First parent
     * @param parent2 Second parent
     * @param birthTick Tick when child is born
     * @param rng Random number generator
     * @return Deterministic child ID
     */
    private String generateChildId(NamedNPC parent1, NamedNPC parent2, long birthTick, Random rng) {
        long hash = parent1.getId().hashCode();
        hash = 31 * hash + parent2.getId().hashCode();
        hash = 31 * hash + birthTick;
        hash = 31 * hash + rng.nextInt(100000);
        
        return "npc_player_child_" + parent1.getClanId() + "_" + Math.abs(hash);
    }
    
    /**
     * Divorces a player from their spouse.
     * 
     * @param player The player NPC
     * @param allNpcs List of all NPCs
     */
    public void divorce(NamedNPC player, List<NamedNPC> allNpcs) {
        if (!player.isPlayer()) {
            throw new IllegalArgumentException("Not a player NPC");
        }
        
        if (player.getSpouseId() == null) {
            return; // Not married
        }
        
        // Find spouse and divorce
        allNpcs.stream()
            .filter(n -> n.getId().equals(player.getSpouseId()))
            .findFirst()
            .ifPresent(spouse -> {
                spouse.setSpouseId(null);
                player.setSpouseId(null);
                
                // TODO: Handle custody of children
                // TODO: Create "Divorce" event
            });
    }
}
