package org.adventure.npc;

import org.adventure.structure.Structure;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Manages NPC lifecycle events: aging, marriage, reproduction, and death.
 * Processes tick-by-tick simulation of NPC populations.
 */
public class NPCLifecycleManager {
    private static final int TICKS_PER_YEAR = 10000;
    private static final int REPRODUCTION_COOLDOWN = 5000; // ~6 months
    private static final int MARRIAGE_AGE_MIN = 18;
    private static final int DEATH_AGE_START = 70;
    
    /**
     * Simulates one tick of NPC lifecycle for all NPCs.
     * Handles aging, marriage proposals, reproduction, and death checks.
     * 
     * @param npcs List of all NPCs to simulate
     * @param structures List of all structures (for home capacity checks)
     * @param currentTick The current game tick
     */
    public void simulateTick(
        List<NamedNPC> npcs,
        List<Structure> structures,
        long currentTick
    ) {
        // Process each NPC
        for (NamedNPC npc : npcs) {
            if (npc.isPlayer()) {
                continue; // Players control their own lifecycle
            }
            
            // Update age
            updateAge(npc, currentTick);
            
            // Check for marriage opportunities
            if (shouldCheckMarriage(npc, currentTick)) {
                attemptMarriage(npc, npcs, currentTick);
            }
            
            // Check for reproduction
            if (shouldCheckReproduction(npc, currentTick)) {
                attemptReproduction(npc, npcs, structures, currentTick);
            }
            
            // Check for death
            if (shouldCheckDeath(npc)) {
                handleDeath(npc, npcs, currentTick);
            }
        }
        
        // Remove dead NPCs (marked for removal during handleDeath)
        npcs.removeIf(npc -> npc.getAge() == -1); // Using age=-1 as death marker
    }
    
    /**
     * Updates NPC age based on ticks elapsed since birth.
     * 
     * @param npc The NPC to update
     * @param currentTick Current game tick
     */
    private void updateAge(NamedNPC npc, long currentTick) {
        long ticksSinceBirth = currentTick - npc.getBirthTick();
        int newAge = (int)(ticksSinceBirth / TICKS_PER_YEAR);
        
        if (newAge != npc.getAge()) {
            npc.setAge(newAge);
            
            // Update job for children becoming adults
            if (newAge == 18 && npc.getJob() == NPCJob.CHILD) {
                npc.setJob(NPCJob.UNEMPLOYED); // Assign actual job in clan expansion simulator
            }
            
            // Update fertility as age changes
            npc.setFertility(calculateFertility(npc.getAge(), npc.getGender()));
        }
    }
    
    /**
     * Calculates fertility based on age.
     * 
     * @param age NPC age
     * @param gender NPC gender
     * @return Fertility value (0-100)
     */
    private int calculateFertility(int age, Gender gender) {
        if (age < 18 || age > 45) {
            return 0;
        }
        
        int peak = (age >= 20 && age <= 35) ? 100 : 60;
        int decline = Math.abs(27 - age) * 3;
        
        return Math.max(0, peak - decline);
    }
    
    /**
     * Determines if NPC should check for marriage opportunities this tick.
     * 
     * @param npc The NPC to check
     * @param currentTick Current game tick
     * @return true if should check marriage
     */
    private boolean shouldCheckMarriage(NamedNPC npc, long currentTick) {
        return npc.getAge() >= MARRIAGE_AGE_MIN 
            && npc.getSpouseId() == null
            && currentTick % 5000 == 0; // Check every ~6 months
    }
    
    /**
     * Attempts to marry this NPC with a compatible partner.
     * 
     * @param npc The NPC proposing marriage
     * @param allNpcs List of all NPCs
     * @param currentTick Current game tick
     */
    private void attemptMarriage(NamedNPC npc, List<NamedNPC> allNpcs, long currentTick) {
        // Find compatible partners (same clan, similar age, unmarried, opposite gender)
        List<NamedNPC> candidates = allNpcs.stream()
            .filter(other -> !other.getId().equals(npc.getId()))
            .filter(other -> other.getClanId().equals(npc.getClanId()))
            .filter(other -> other.getAge() >= MARRIAGE_AGE_MIN)
            .filter(other -> other.getSpouseId() == null)
            .filter(other -> other.getGender() != npc.getGender())
            .filter(other -> Math.abs(other.getAge() - npc.getAge()) <= 10) // Similar age
            .collect(Collectors.toList());
        
        if (candidates.isEmpty()) {
            return;
        }
        
        // 10% chance to propose marriage
        Random rng = new Random(currentTick ^ npc.getId().hashCode());
        if (rng.nextDouble() < 0.10) {
            NamedNPC partner = candidates.get(rng.nextInt(candidates.size()));
            
            // Marry them
            npc.setSpouseId(partner.getId());
            partner.setSpouseId(npc.getId());
            
            // Reset reproduction cooldowns
            npc.setLastReproductionCheck(currentTick);
            partner.setLastReproductionCheck(currentTick);
            
            // TODO: Create "Marriage" event for story system
        }
    }
    
    /**
     * Determines if NPC should check for reproduction this tick.
     * 
     * @param npc The NPC to check
     * @param currentTick Current game tick
     * @return true if should attempt reproduction
     */
    private boolean shouldCheckReproduction(NamedNPC npc, long currentTick) {
        return npc.getSpouseId() != null
            && npc.getFertility() > 0
            && (currentTick - npc.getLastReproductionCheck()) >= REPRODUCTION_COOLDOWN;
    }
    
    /**
     * Attempts reproduction for a married NPC.
     * 
     * @param npc The NPC attempting reproduction
     * @param allNpcs List of all NPCs
     * @param structures List of all structures
     * @param currentTick Current game tick
     */
    private void attemptReproduction(
        NamedNPC npc,
        List<NamedNPC> allNpcs,
        List<Structure> structures,
        long currentTick
    ) {
        npc.setLastReproductionCheck(currentTick);
        
        // Find spouse
        NamedNPC spouse = allNpcs.stream()
            .filter(n -> n.getId().equals(npc.getSpouseId()))
            .findFirst()
            .orElse(null);
        
        if (spouse == null) {
            return; // Spouse not found (should not happen)
        }
        
        // Check if home has space (max 4 NPCs per HOUSE)
        Structure home = structures.stream()
            .filter(s -> s.getId().equals(npc.getHomeStructureId()))
            .findFirst()
            .orElse(null);
        
        if (home == null) {
            return;
        }
        
        long occupants = allNpcs.stream()
            .filter(n -> n.getHomeStructureId().equals(home.getId()))
            .count();
        
        if (occupants >= 4) {
            return; // House full
        }
        
        // Base chance: fertility / 100 (e.g., 80% fertility = 80% chance per check)
        // Check happens every 5000 ticks, so ~2 checks per year
        Random rng = new Random(currentTick ^ npc.getId().hashCode());
        double chance = npc.getFertility() / 100.0;
        
        if (rng.nextDouble() < chance) {
            // Create child!
            Gender childGender = rng.nextBoolean() ? Gender.MALE : Gender.FEMALE;
            
            // Generate deterministic child ID
            String childId = generateChildId(npc, spouse, currentTick, rng);
            
            NamedNPC child = new NamedNPC.Builder()
                .id(childId)
                .name(generateChildName(childGender, rng))
                .clanId(npc.getClanId())
                .age(0)
                .gender(childGender)
                .job(NPCJob.CHILD)
                .homeStructureId(npc.getHomeStructureId())
                .workplaceStructureId(null)
                .spouseId(null)
                .childrenIds(List.of())
                .fertility(0)
                .isPlayer(false)
                .birthTick(currentTick)
                .lastReproductionCheck(0L)
                .build();
            
            allNpcs.add(child);
            
            // Update parents
            npc.addChild(child.getId());
            spouse.addChild(child.getId());
            
            // TODO: Create "Birth" event for story system
        }
    }
    
    /**
     * Generates a name for a newborn child.
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
        
        return "npc_child_" + parent1.getClanId() + "_" + Math.abs(hash);
    }
    
    /**
     * Determines if NPC should check for death this tick.
     * 
     * @param npc The NPC to check
     * @return true if should check death
     */
    private boolean shouldCheckDeath(NamedNPC npc) {
        return npc.getAge() >= DEATH_AGE_START;
    }
    
    /**
     * Handles death check for elderly NPCs.
     * Death probability increases with age.
     * 
     * @param npc The NPC to check
     * @param allNpcs List of all NPCs
     * @param currentTick Current game tick
     */
    private void handleDeath(NamedNPC npc, List<NamedNPC> allNpcs, long currentTick) {
        // Death chance increases with age
        // Age 70: 1% per year
        // Age 80: 10% per year
        // Age 90: 50% per year
        // Age 100+: 100% per year
        
        int age = npc.getAge();
        double deathChance;
        
        if (age < 70) {
            deathChance = 0.0;
        } else if (age < 80) {
            deathChance = 0.0001; // 1% per 10k ticks = 1% per year
        } else if (age < 90) {
            deathChance = 0.001; // 10% per year
        } else if (age < 100) {
            deathChance = 0.005; // 50% per year
        } else {
            deathChance = 0.01; // 100% per year
        }
        
        Random rng = new Random(currentTick ^ npc.getId().hashCode());
        
        if (rng.nextDouble() < deathChance) {
            // NPC dies
            
            // Handle spouse (make them widow/widower)
            if (npc.getSpouseId() != null) {
                allNpcs.stream()
                    .filter(n -> n.getId().equals(npc.getSpouseId()))
                    .findFirst()
                    .ifPresent(spouse -> spouse.setSpouseId(null));
            }
            
            // TODO: Handle inheritance (transfer possessions to children/spouse)
            // TODO: Create "Death" event for story system
            
            // Mark for removal (using age=-1 as death marker)
            npc.setAge(-1);
        }
    }
}
