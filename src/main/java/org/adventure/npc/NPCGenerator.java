package org.adventure.npc;

import org.adventure.society.Clan;
import org.adventure.structure.Structure;
import org.adventure.structure.StructureType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates Named NPCs with names, ages, jobs, homes, and relationships.
 * Ensures deterministic generation from seed for reproducible worlds.
 */
public class NPCGenerator {
    private static final List<String> MALE_NAMES = Arrays.asList(
        "Aldric", "Borin", "Cedric", "Daven", "Elric", "Gareth", "Hadrian",
        "Ivor", "Jorah", "Kael", "Lorian", "Magnus", "Nolan", "Orin", "Pyke",
        "Roran", "Soren", "Thorne", "Ulric", "Valen", "Wren", "Xander", "Yorick", "Zane"
    );
    
    private static final List<String> FEMALE_NAMES = Arrays.asList(
        "Aria", "Brynn", "Celia", "Dessa", "Elara", "Freya", "Gwen",
        "Helia", "Isolde", "Kira", "Luna", "Mira", "Nessa", "Ophelia", "Petra",
        "Quinn", "Rhea", "Selene", "Thea", "Una", "Vera", "Willa", "Xena", "Yara", "Zara"
    );
    
    /**
     * Generates a single NPC with specified attributes.
     * Uses deterministic UUID generation based on seed for reproducibility.
     * 
     * @param clanId The clan this NPC belongs to
     * @param gender The gender of the NPC
     * @param age The age of the NPC in years
     * @param job The job/occupation of the NPC
     * @param homeStructureId The structure where this NPC lives
     * @param currentTick The current game tick (for birthTick calculation)
     * @param rng Random number generator (seeded for determinism)
     * @return A new NamedNPC instance
     */
    public static NamedNPC generateNPC(
        String clanId,
        Gender gender,
        int age,
        NPCJob job,
        String homeStructureId,
        long currentTick,
        Random rng
    ) {
        String name = generateName(gender, rng);
        int fertility = calculateFertility(age, gender);
        long birthTick = currentTick - (age * 10000L); // 1 year = 10,000 ticks
        
        // Generate deterministic ID based on clan, name, and birth tick
        String id = generateDeterministicId(clanId, name, birthTick, rng);
        
        return new NamedNPC.Builder()
            .id(id)
            .name(name)
            .clanId(clanId)
            .age(age)
            .gender(gender)
            .job(job)
            .homeStructureId(homeStructureId)
            .workplaceStructureId(null) // Assigned later if job requires workplace
            .spouseId(null) // Assigned during marriage creation
            .childrenIds(new ArrayList<>())
            .fertility(fertility)
            .isPlayer(false)
            .birthTick(birthTick)
            .lastReproductionCheck(0L)
            .build();
    }
    
    /**
     * Generates a deterministic ID for an NPC.
     * Uses a combination of clan, name, birth tick, and RNG state for uniqueness.
     * 
     * @param clanId Clan identifier
     * @param name NPC name
     * @param birthTick Birth tick
     * @param rng Random number generator (for uniqueness)
     * @return Deterministic NPC ID
     */
    private static String generateDeterministicId(String clanId, String name, long birthTick, Random rng) {
        // Create a deterministic but unique ID using hash
        long hash = clanId.hashCode();
        hash = 31 * hash + name.hashCode();
        hash = 31 * hash + birthTick;
        hash = 31 * hash + rng.nextInt(1000000); // Add entropy from seeded RNG
        
        return "npc_" + clanId + "_" + Math.abs(hash);
    }
    
    /**
     * Generates a random name based on gender.
     * 
     * @param gender The gender for name selection
     * @param rng Random number generator
     * @return A randomly selected name
     */
    private static String generateName(Gender gender, Random rng) {
        List<String> nameList = (gender == Gender.MALE) ? MALE_NAMES : FEMALE_NAMES;
        return nameList.get(rng.nextInt(nameList.size()));
    }
    
    /**
     * Calculates fertility based on age and gender.
     * Peak fertility at age 27, declining before and after.
     * 
     * @param age The age of the NPC
     * @param gender The gender (currently not used but reserved for future mechanics)
     * @return Fertility value (0-100)
     */
    private static int calculateFertility(int age, Gender gender) {
        if (age < 18 || age > 45) {
            return 0;
        }
        
        // Peak fertility between 20-35
        int peak = (age >= 20 && age <= 35) ? 100 : 60;
        int decline = Math.abs(27 - age) * 3; // 3 points per year away from optimal age
        
        return Math.max(0, peak - decline);
    }
    
    /**
     * Generates the initial population for a clan at worldgen.
     * Distributes ages (20% children, 50% adults, 30% elders) and assigns homes/jobs.
     * 
     * @param clan The clan to generate population for
     * @param clanStructures The structures owned by this clan
     * @param currentTick The current game tick (typically 0 at worldgen)
     * @param rng Random number generator (seeded for determinism)
     * @return List of generated NPCs
     */
    public static List<NamedNPC> generateInitialClanPopulation(
        Clan clan,
        List<Structure> clanStructures,
        long currentTick,
        Random rng
    ) {
        List<NamedNPC> npcs = new ArrayList<>();
        int targetPopulation = clan.getMembers().size(); // From ClanGenerator
        
        if (targetPopulation == 0) {
            return npcs; // Empty clan
        }
        
        // Distribute ages: 20% children, 50% adults, 30% elders
        int children = Math.max(0, (int)(targetPopulation * 0.20));
        int adults = Math.max(1, (int)(targetPopulation * 0.50)); // At least 1 adult
        int elders = targetPopulation - children - adults;
        
        // Get residential structures (houses) for home assignment
        List<Structure> homes = clanStructures.stream()
            .filter(s -> s.getType() == StructureType.HOUSE)
            .collect(Collectors.toList());
        
        if (homes.isEmpty()) {
            // Fallback: use any structure as home if no houses exist
            homes = new ArrayList<>(clanStructures);
            if (homes.isEmpty()) {
                return npcs; // Cannot generate NPCs without homes
            }
        }
        
        int homeIndex = 0;
        
        // Generate children (age 0-17)
        for (int i = 0; i < children; i++) {
            Gender gender = randomGender(rng);
            int age = rng.nextInt(18); // 0-17
            String homeId = homes.get(homeIndex % homes.size()).getId();
            
            NamedNPC child = generateNPC(clan.getId(), gender, age, NPCJob.CHILD, homeId, currentTick, rng);
            npcs.add(child);
            
            if ((i + 1) % 2 == 0) homeIndex++; // 2 children per home
        }
        
        // Get workplace structures for job assignment
        Map<StructureType, List<Structure>> workplaces = clanStructures.stream()
            .filter(s -> s.getType() != StructureType.HOUSE)
            .collect(Collectors.groupingBy(Structure::getType));
        
        // Generate adults (age 18-60) with jobs
        for (int i = 0; i < adults; i++) {
            Gender gender = randomGender(rng);
            int age = 18 + rng.nextInt(43); // 18-60
            String homeId = homes.get(homeIndex % homes.size()).getId();
            
            NPCJob job = assignJob(workplaces, rng);
            NamedNPC adult = generateNPC(clan.getId(), gender, age, job, homeId, currentTick, rng);
            
            // Assign workplace if job requires it
            if (job.requiresWorkplace() && workplaces.containsKey(job.getRequiredWorkplace())) {
                List<Structure> jobStructures = workplaces.get(job.getRequiredWorkplace());
                if (!jobStructures.isEmpty()) {
                    adult.setWorkplaceStructureId(jobStructures.get(rng.nextInt(jobStructures.size())).getId());
                }
            }
            
            npcs.add(adult);
            
            if ((i + 1) % 2 == 0) homeIndex++; // 2 adults per home (couples)
        }
        
        // Generate elders (age 60-80)
        for (int i = 0; i < elders; i++) {
            Gender gender = randomGender(rng);
            int age = 60 + rng.nextInt(21); // 60-80
            String homeId = homes.get(homeIndex % homes.size()).getId();
            
            NamedNPC elder = generateNPC(clan.getId(), gender, age, NPCJob.UNEMPLOYED, homeId, currentTick, rng);
            npcs.add(elder);
            
            homeIndex++;
        }
        
        // Auto-marry some adults (50% married)
        createInitialMarriages(npcs, rng);
        
        return npcs;
    }
    
    /**
     * Randomly selects a gender.
     * 
     * @param rng Random number generator
     * @return MALE or FEMALE with equal probability
     */
    private static Gender randomGender(Random rng) {
        return rng.nextBoolean() ? Gender.MALE : Gender.FEMALE;
    }
    
    /**
     * Assigns a job to an adult NPC based on available workplace structures.
     * 
     * @param workplaces Map of structure types to available structures
     * @param rng Random number generator
     * @return An assigned NPCJob
     */
    private static NPCJob assignJob(Map<StructureType, List<Structure>> workplaces, Random rng) {
        // Collect all available jobs based on workplaces
        List<NPCJob> availableJobs = new ArrayList<>();
        
        for (NPCJob job : NPCJob.values()) {
            if (job == NPCJob.CHILD) continue; // Skip child job for adults
            
            if (!job.requiresWorkplace()) {
                availableJobs.add(job); // UNEMPLOYED is always available
            } else if (workplaces.containsKey(job.getRequiredWorkplace())) {
                availableJobs.add(job);
            }
        }
        
        if (availableJobs.isEmpty()) {
            return NPCJob.UNEMPLOYED;
        }
        
        return availableJobs.get(rng.nextInt(availableJobs.size()));
    }
    
    /**
     * Creates initial marriages between compatible NPCs.
     * Targets 50% of adults to be married at worldgen.
     * 
     * @param npcs List of all NPCs to process
     * @param rng Random number generator
     */
    private static void createInitialMarriages(List<NamedNPC> npcs, Random rng) {
        // Get unmarried adults (age 18-60, not married, opposite genders)
        List<NamedNPC> unmarriedMales = npcs.stream()
            .filter(npc -> npc.getAge() >= 18 && npc.getAge() <= 60)
            .filter(npc -> npc.getSpouseId() == null)
            .filter(npc -> npc.getGender() == Gender.MALE)
            .collect(Collectors.toList());
        
        List<NamedNPC> unmarriedFemales = npcs.stream()
            .filter(npc -> npc.getAge() >= 18 && npc.getAge() <= 60)
            .filter(npc -> npc.getSpouseId() == null)
            .filter(npc -> npc.getGender() == Gender.FEMALE)
            .collect(Collectors.toList());
        
        // Shuffle for random pairing
        Collections.shuffle(unmarriedMales, rng);
        Collections.shuffle(unmarriedFemales, rng);
        
        // Target: 50% of all adults married
        // Each marriage creates 2 married people, so we need totalAdults * 0.50 / 2 marriages
        // But we're limited by min(males, females) available
        int maxPairs = Math.min(unmarriedMales.size(), unmarriedFemales.size());
        int marriageCount = maxPairs; // Marry all possible pairs (this may give > 50% if genders unbalanced)
        
        for (int i = 0; i < marriageCount; i++) {
            NamedNPC male = unmarriedMales.get(i);
            NamedNPC female = unmarriedFemales.get(i);
            
            // Marry them
            male.setSpouseId(female.getId());
            female.setSpouseId(male.getId());
            
            // Move them to same home (use male's home)
            // Note: This requires updating the female's home, but NamedNPC.homeStructureId is final
            // This is a design decision - homes are assigned at creation and don't change
            // Alternatively, we could assign homes AFTER marriage in a future iteration
        }
    }
}
