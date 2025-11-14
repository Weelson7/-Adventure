# NamedNPC

**Package:** `org.adventure.npc`  
**Type:** Class  
**Since:** Phase 1.10.1

---

## Overview

`NamedNPC` represents a named NPC for population simulation and clan membership. This class is **distinct** from `org.adventure.character.NPC` (combat AI).

**NamedNPC focuses on:**
- Clan membership and society simulation
- Lifecycle events (birth, marriage, reproduction, death)
- Economic roles (jobs, workplaces, production)
- Family relationships (spouse, children)

**org.adventure.character.NPC focuses on:**
- Combat AI and behavior
- Region-based spawning
- Stats and equipment

A NamedNPC can optionally be linked to a Character instance for stat tracking.

---

## Fields

### Identity
- `String id` — Unique identifier (deterministic hash-based)
- `String name` — NPC's name (from curated lists)
- `String clanId` — Clan this NPC belongs to

### Demographics
- `int age` — Age in years
- `Gender gender` — MALE or FEMALE
- `long birthTick` — Game tick when NPC was born

### Economic
- `NPCJob job` — Current job/occupation
- `String homeStructureId` — Home structure ID (HOUSE)
- `String workplaceStructureId` — Workplace structure ID (FORGE, FARM, etc.)

### Family
- `String spouseId` — Spouse's NPC ID (null if unmarried)
- `List<String> childrenIds` — List of child NPC IDs
- `int fertility` — Fertility value (0-100, peak at age 27)
- `long lastReproductionCheck` — Last tick reproduction was checked

### Flags
- `boolean isPlayer` — True if this NPC represents a player character
- `String characterId` — Optional link to Character instance

---

## Constructor (Builder Pattern)

```java
NamedNPC npc = new NamedNPC.Builder()
    .id("npc_clan_grassland_12345_67890")
    .name("Aldric")
    .clanId("clan_grassland_12345")
    .age(27)
    .gender(Gender.MALE)
    .job(NPCJob.BLACKSMITH)
    .homeStructureId("structure_house_001")
    .workplaceStructureId("structure_forge_001")
    .spouseId(null)
    .childrenIds(new ArrayList<>())
    .fertility(100)
    .isPlayer(false)
    .birthTick(0L)
    .lastReproductionCheck(0L)
    .build();
```

---

## Key Methods

### Getters
```java
String getId()
String getName()
String getClanId()
int getAge()
Gender getGender()
NPCJob getJob()
String getHomeStructureId()
String getWorkplaceStructureId()
String getSpouseId()
List<String> getChildrenIds()
int getFertility()
boolean isPlayer()
long getBirthTick()
long getLastReproductionCheck()
String getCharacterId()
```

### Setters (for mutable fields)
```java
void setAge(int age)
void setJob(NPCJob job)
void setWorkplaceStructureId(String workplaceStructureId)
void setSpouseId(String spouseId)
void addChild(String childId)
void setFertility(int fertility)
void setLastReproductionCheck(long tick)
void setCharacterId(String characterId)
```

### Status Checks
```java
boolean isMarried() // Returns true if spouseId != null
boolean isAdult()   // Returns true if age >= 18
boolean canWork()   // Returns true if age >= 18 && age < 70
```

---

## Lifecycle Stages

### Childhood (Age 0-17)
- **Job:** NPCJob.CHILD
- **Workplace:** None
- **Marriage:** Not allowed
- **Reproduction:** Not allowed

### Adulthood (Age 18-60)
- **Job:** Assigned based on available structures
- **Workplace:** Assigned if job requires it
- **Marriage:** Allowed (compatibility checks apply)
- **Reproduction:** Allowed if married and fertile

### Elderhood (Age 60-80)
- **Job:** NPCJob.UNEMPLOYED (retirement)
- **Workplace:** None
- **Marriage:** Can stay married, rarely marry at this age
- **Reproduction:** Fertility drops to 0

### Death (Age 70+)
- Increasing probability of natural death
- Inheritance: Treasury and possessions pass to children/spouse

---

## Marriage & Reproduction

### Marriage Requirements
- Both NPCs age >= 18
- Both unmarried (spouseId == null)
- Relationship value > 75 (for dynamic marriage)
- Opposite genders (default, configurable)

### Reproduction Requirements
- Both NPCs married (spouseId != null)
- Both fertile (fertility > 0)
- Home has space (< 4 NPCs per HOUSE)
- Random chance based on fertility (higher fertility = higher chance)
- Cooldown: 5,000 ticks between reproduction checks

### Fertility Calculation
```java
if (age < 18 || age > 45) {
    fertility = 0;
} else {
    int peak = (age >= 20 && age <= 35) ? 100 : 60;
    int decline = Math.abs(27 - age) * 3; // 3 points per year from optimal
    fertility = Math.max(0, peak - decline);
}
```

**Fertility Curve:**
- Age 18: 73 (young adult)
- Age 27: 100 (peak)
- Age 35: 76 (still fertile)
- Age 45: 6 (low fertility)
- Age 46+: 0 (infertile)

---

## Determinism

**ID Generation:**
- Hash-based: `npc_<clanId>_<hash(name+birthTick+rngState)>`
- No UUID.randomUUID() usage
- Same seed → same NPCs

**Name Generation:**
- From predefined lists (24 male names, 25 female names)
- Seeded RNG ensures deterministic selection

---

## Usage Examples

### Creating an NPC at Worldgen
```java
NamedNPC npc = NPCGenerator.generateNPC(
    clanId,
    Gender.MALE,
    27, // age
    NPCJob.BLACKSMITH,
    homeStructureId,
    0L, // currentTick
    rng
);
```

### Marrying Two NPCs
```java
NamedNPC male = findCompatibleMale();
NamedNPC female = findCompatibleFemale();

male.setSpouseId(female.getId());
female.setSpouseId(male.getId());
```

### Aging an NPC
```java
long ticksPerYear = 10000L;
if (currentTick - npc.getBirthTick() >= ticksPerYear * (npc.getAge() + 1)) {
    npc.setAge(npc.getAge() + 1);
    // Recalculate fertility
    npc.setFertility(calculateFertility(npc.getAge(), npc.getGender()));
}
```

### Checking Reproduction
```java
if (npc.isMarried() && npc.getFertility() > 0) {
    if (currentTick - npc.getLastReproductionCheck() >= 5000L) {
        int chance = npc.getFertility(); // 0-100
        if (rng.nextInt(100) < chance) {
            // Create child NPC
            NamedNPC child = createChild(npc, spouse);
            npc.addChild(child.getId());
            spouse.addChild(child.getId());
        }
        npc.setLastReproductionCheck(currentTick);
    }
}
```

---

## Integration Points

### With Clan System
- `clanId` links NPC to Clan
- Clan.members contains NPC IDs

### With Structure System
- `homeStructureId` links to HOUSE structure
- `workplaceStructureId` links to workplace structure (FORGE, FARM, etc.)

### With Character System
- `characterId` optionally links to Character instance
- Players can be NamedNPC with isPlayer=true

### With Lifecycle Manager
- NPCLifecycleManager handles aging, marriage, reproduction, death
- Ticks all NPCs every game tick

---

## Related Classes

- `NPCGenerator` — Factory for creating NamedNPC instances
- `NPCLifecycleManager` — Manages NPC lifecycle events
- `PlayerNPCInteraction` — Handles player-NPC marriage
- `Gender` — Enum for NPC gender
- `NPCJob` — Enum for NPC occupations
- `org.adventure.character.NPC` — Combat AI NPC (different purpose)
