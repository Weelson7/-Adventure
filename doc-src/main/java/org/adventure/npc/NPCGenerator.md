# NPCGenerator

**Package:** `org.adventure.npc`  
**Type:** Class (Factory)  
**Since:** Phase 1.10.1

---

## Overview

`NPCGenerator` is a factory class for creating `NamedNPC` instances with deterministic generation from seeds. It handles:
- Generating individual NPCs with names, ages, jobs, homes
- Creating initial clan populations at worldgen
- Age distribution (20% children, 50% adults, 30% elders)
- Initial marriage creation (~50% of adults)
- Job assignment based on available workplace structures

All generation is **deterministic** from seed for reproducible worlds.

---

## Name Lists

### Male Names (24 total)
```java
"Aldric", "Borin", "Cedric", "Daven", "Elric", "Gareth", "Hadrian",
"Ivor", "Jorah", "Kael", "Lorian", "Magnus", "Nolan", "Orin", "Pyke",
"Roran", "Soren", "Thorne", "Ulric", "Valen", "Wren", "Xander", "Yorick", "Zane"
```

### Female Names (25 total)
```java
"Aria", "Brynn", "Celia", "Dessa", "Elara", "Freya", "Gwen",
"Helia", "Isolde", "Kira", "Luna", "Mira", "Nessa", "Ophelia", "Petra",
"Quinn", "Rhea", "Selene", "Thea", "Una", "Vera", "Willa", "Xena", "Yara", "Zara"
```

---

## Key Methods

### `generateNPC()`
Generates a single NPC with specified attributes.

**Signature:**
```java
public static NamedNPC generateNPC(
    String clanId,
    Gender gender,
    int age,
    NPCJob job,
    String homeStructureId,
    long currentTick,
    Random rng
)
```

**Parameters:**
- `clanId` — Clan this NPC belongs to
- `gender` — MALE or FEMALE
- `age` — Age in years
- `job` — Occupation (FARMER, BLACKSMITH, etc.)
- `homeStructureId` — Home structure ID (usually HOUSE)
- `currentTick` — Current game tick (for birthTick calculation)
- `rng` — Seeded random number generator

**Returns:** New `NamedNPC` instance

**Algorithm:**
1. Generate random name from appropriate gender list
2. Calculate fertility based on age (peak at 27)
3. Generate deterministic ID from hash of clan+name+birthTick+rng
4. Calculate birthTick = currentTick - (age × 10,000)
5. Build and return NamedNPC instance

---

### `generateInitialClanPopulation()`
Generates complete population for a clan at worldgen.

**Signature:**
```java
public static List<NamedNPC> generateInitialClanPopulation(
    Clan clan,
    List<Structure> clanStructures,
    long currentTick,
    Random rng
)
```

**Parameters:**
- `clan` — Clan to generate population for
- `clanStructures` — Structures owned by this clan
- `currentTick` — Current game tick (typically 0 at worldgen)
- `rng` — Seeded RNG for determinism

**Returns:** List of generated NPCs

**Algorithm:**
```
1. Get target population from clan.getMembers().size()
2. Calculate age distribution:
   - children = 20% (age 0-17)
   - adults = 50% (age 18-60)
   - elders = 30% (age 60-80)
3. Get residential structures (HOUSE type)
4. Generate children:
   - Random gender
   - Random age 0-17
   - Job = NPCJob.CHILD
   - Assign homes (2 children per house)
5. Generate adults:
   - Random gender
   - Random age 18-60
   - Assign job based on available workplaces
   - Assign workplace if job requires it
   - Assign homes (2 adults per house, for couples)
6. Generate elders:
   - Random gender
   - Random age 60-80
   - Job = NPCJob.UNEMPLOYED
   - Assign homes (1 elder per house)
7. Create initial marriages (~50% of adults)
8. Return complete NPC list
```

---

### `calculateFertility()`
Calculates fertility value based on age and gender.

**Signature:**
```java
private static int calculateFertility(int age, Gender gender)
```

**Returns:** Fertility value (0-100)

**Formula:**
```java
if (age < 18 || age > 45) {
    return 0; // Infertile outside reproductive age range
}

int peak = (age >= 20 && age <= 35) ? 100 : 60;
int decline = Math.abs(27 - age) * 3; // 3 points per year from optimal age

return Math.max(0, peak - decline);
```

**Fertility Curve:**
| Age | Fertility | Notes |
|-----|-----------|-------|
| <18 | 0 | Too young |
| 18 | 73 | Young adult |
| 20-35 | 85-100 | Peak fertility |
| 27 | 100 | Optimal age |
| 40 | 61 | Declining |
| 45 | 6 | Very low |
| >45 | 0 | Infertile |

---

### `generateDeterministicId()`
Creates deterministic ID for an NPC.

**Signature:**
```java
private static String generateDeterministicId(
    String clanId, 
    String name, 
    long birthTick, 
    Random rng
)
```

**Algorithm:**
```java
long hash = clanId.hashCode();
hash = 31 * hash + name.hashCode();
hash = 31 * hash + birthTick;
hash = 31 * hash + rng.nextInt(1000000); // Entropy from seeded RNG

return "npc_" + clanId + "_" + Math.abs(hash);
```

**Example:** `npc_clan_grassland_12345_87654321`

---

### `createInitialMarriages()`
Creates marriages between compatible NPCs at worldgen.

**Signature:**
```java
private static void createInitialMarriages(List<NamedNPC> npcs, Random rng)
```

**Algorithm:**
```
1. Filter unmarried adults (age 18-60, spouseId == null)
2. Separate by gender (males and females)
3. Shuffle both lists with seeded RNG
4. Marry all possible pairs (min(males, females) pairs)
5. Set spouseId for both NPCs in each pair
```

**Marriage Rate:**
- Target: ~50% of adults married
- Actual: Depends on gender balance
- If 50 males + 50 females → 50 pairs → 100 married (100%)
- If 30 males + 70 females → 30 pairs → 60 married (60%)
- Tolerance: 30-70% to account for gender imbalance

---

### `assignJob()`
Assigns job to an adult NPC based on available workplace structures.

**Signature:**
```java
private static NPCJob assignJob(
    Map<StructureType, List<Structure>> workplaces,
    Random rng
)
```

**Algorithm:**
```
1. Collect available jobs:
   - UNEMPLOYED always available (no workplace needed)
   - For each NPCJob:
     - If job requires workplace AND workplace exists → add to available
2. If no jobs available → return UNEMPLOYED
3. Randomly select from available jobs with seeded RNG
4. Return selected job
```

**Example:**
```java
// Workplaces: {FORGE: [forge1], FARM: [farm1, farm2]}
// Available jobs: [UNEMPLOYED, BLACKSMITH, FARMER]
// Random selection → FARMER assigned
```

---

## Determinism Guarantees

### ID Generation
- **Hash-based:** `npc_<clanId>_<hash>`
- **No UUID:** Never uses `UUID.randomUUID()`
- **Seeded RNG:** All randomness from seeded Random instance

### Name Selection
- **Predefined lists:** Same names used every generation
- **Seeded RNG:** Same seed → same name sequence

### Age Distribution
- **Formula-based:** Exact 20/50/30 split
- **Seeded RNG:** Same seed → same ages

### Job Assignment
- **Structure-dependent:** Based on available workplaces
- **Seeded RNG:** Same seed + same structures → same jobs

### Marriage Creation
- **Algorithm-based:** All possible pairs married
- **Seeded RNG:** Same seed → same marriages

---

## Usage Examples

### Generate Single NPC
```java
Random rng = new Random(worldSeed);
NamedNPC npc = NPCGenerator.generateNPC(
    "clan_grassland_12345",
    Gender.MALE,
    27, // age
    NPCJob.BLACKSMITH,
    "structure_house_001",
    0L, // worldgen tick
    rng
);
```

### Generate Clan Population at Worldgen
```java
// Setup
Clan clan = clans.get(0);
List<Structure> clanStructures = getClanStructures(clan.getId());
Random rng = new Random(worldSeed ^ 0x12345);

// Generate population
List<NamedNPC> npcs = NPCGenerator.generateInitialClanPopulation(
    clan,
    clanStructures,
    0L, // currentTick = 0 at worldgen
    rng
);

// Result: ~20 NPCs if clan size was 20
// - 4 children (age 0-17)
// - 10 adults (age 18-60, with jobs)
// - 6 elders (age 60-80, unemployed)
// - ~5 married couples
```

### Determinism Verification
```java
// Generate twice with same seed
Random rng1 = new Random(12345L);
List<NamedNPC> npcs1 = NPCGenerator.generateInitialClanPopulation(
    clan, structures, 0L, rng1);

Random rng2 = new Random(12345L);
List<NamedNPC> npcs2 = NPCGenerator.generateInitialClanPopulation(
    clan, structures, 0L, rng2);

// Verify identical
assertEquals(npcs1.size(), npcs2.size());
for (int i = 0; i < npcs1.size(); i++) {
    assertEquals(npcs1.get(i).getName(), npcs2.get(i).getName());
    assertEquals(npcs1.get(i).getAge(), npcs2.get(i).getAge());
    assertEquals(npcs1.get(i).getGender(), npcs2.get(i).getGender());
}
```

---

## Integration Points

### With WorldGen
```java
// In WorldGen.generate(seed):
private void generateNamedNPCs(long seed) {
    Map<String, List<Structure>> clanStructures = buildClanStructureMap();
    
    List<NamedNPC> allNpcs = new ArrayList<>();
    Random rng = new Random(seed ^ 0xNPC);
    
    for (Clan clan : clans) {
        List<Structure> structures = clanStructures.get(clan.getId());
        List<NamedNPC> clanNpcs = NPCGenerator.generateInitialClanPopulation(
            clan, structures, 0L, rng);
        allNpcs.addAll(clanNpcs);
    }
    
    this.npcs = allNpcs;
}
```

### With ClanGenerator
```java
// Generate NPCs after clans and settlements
public static List<NamedNPC> generateNPCsForClans(
    List<Clan> clans,
    Map<String, List<Structure>> clanStructures,
    long worldSeed,
    long currentTick
) {
    List<NamedNPC> allNpcs = new ArrayList<>();
    Random rng = new Random(worldSeed ^ 0xNPC);
    
    for (Clan clan : clans) {
        allNpcs.addAll(NPCGenerator.generateInitialClanPopulation(
            clan, clanStructures.get(clan.getId()), currentTick, rng));
    }
    
    return allNpcs;
}
```

---

## Testing

### Unit Tests
```java
@Test
public void testGenerateNPC() {
    Random rng = new Random(123L);
    NamedNPC npc = NPCGenerator.generateNPC(
        "clan_test", Gender.MALE, 27, NPCJob.FARMER, 
        "house_1", 0L, rng);
    
    assertNotNull(npc.getId());
    assertNotNull(npc.getName());
    assertEquals(27, npc.getAge());
    assertEquals(Gender.MALE, npc.getGender());
    assertEquals(NPCJob.FARMER, npc.getJob());
}

@Test
public void testAgeDistribution() {
    // Generate 100 NPCs
    // Verify ~20% children, ~50% adults, ~30% elders
}

@Test
public void testMarriageCreation() {
    // Generate population
    // Verify ~50% of adults are married (30-70% tolerance)
}

@Test
public void testDeterminism() {
    // Generate twice with same seed
    // Verify identical results
}
```

---

## Related Classes

- `NamedNPC` — Entity class for named NPCs
- `Gender` — Enum for NPC gender
- `NPCJob` — Enum for NPC occupations
- `NPCLifecycleManager` — Manages NPC lifecycle events
- `ClanGenerator` — Uses NPCGenerator to populate clans
- `WorldGen` — Calls NPCGenerator during phase 12
