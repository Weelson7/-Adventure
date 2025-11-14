# NPCJob

**Package:** `org.adventure.npc`  
**Type:** Enum  
**Since:** Phase 1.10.1

---

## Overview

`NPCJob` represents the occupation/role of a Named NPC in the game world. Jobs determine:
- Workplace requirements (structure type needed)
- Economic production (future: goods/services produced)
- Social status and interactions
- Skill development opportunities

---

## Enum Values

### Residential (No Workplace Required)
- `CHILD` — Children (age 0-17), no workplace
- `UNEMPLOYED` — Adults without employment, common for elders

### Agricultural
- `FARMER` — Works at FARM structures, produces food
- `HERDER` — Works with livestock (future: RANCH structures)

### Crafting
- `BLACKSMITH` — Works at FORGE, crafts metal items (weapons, tools)
- `CARPENTER` — Works at WORKSHOP, crafts wood items (furniture, buildings)
- `TAILOR` — Works at WORKSHOP, crafts cloth items (clothing, banners)
- `ALCHEMIST` — Works at WORKSHOP, crafts potions and reagents

### Commercial
- `MERCHANT` — Works at SHOP or MARKET, buys/sells goods
- `INNKEEPER` — Works at INN, provides lodging and food

### Professional
- `HEALER` — Works at TEMPLE or CLINIC, heals injuries and cures diseases
- `SCHOLAR` — Works at LIBRARY or TEMPLE, researches and teaches
- `MAGE` — Works at TOWER, casts spells and enchants items

### Military
- `GUARD` — Works at BARRACKS or WATCHTOWER, defends settlement
- `SOLDIER` — Works at BARRACKS, fights in wars

### Leadership
- `CHIEF` — Leader of clan, works at GUILD_HALL or TEMPLE
- `PRIEST` — Religious leader, works at TEMPLE

---

## Methods

### `boolean requiresWorkplace()`
Returns true if this job requires a specific workplace structure.

```java
NPCJob.FARMER.requiresWorkplace()     // true
NPCJob.UNEMPLOYED.requiresWorkplace() // false
NPCJob.CHILD.requiresWorkplace()      // false
```

### `StructureType getRequiredWorkplace()`
Returns the structure type required for this job, or null if no workplace required.

```java
NPCJob.BLACKSMITH.getRequiredWorkplace() // StructureType.FORGE
NPCJob.MERCHANT.getRequiredWorkplace()   // StructureType.SHOP
NPCJob.UNEMPLOYED.getRequiredWorkplace() // null
```

---

## Job Assignment Algorithm

At worldgen, NPCs are assigned jobs based on available workplace structures:

```java
// 1. Collect available workplace structures by type
Map<StructureType, List<Structure>> workplaces = getWorkplaceStructures();

// 2. Build list of available jobs
List<NPCJob> availableJobs = new ArrayList<>();
for (NPCJob job : NPCJob.values()) {
    if (job == NPCJob.CHILD) continue; // Skip CHILD for adults
    
    if (!job.requiresWorkplace()) {
        availableJobs.add(job); // UNEMPLOYED always available
    } else if (workplaces.containsKey(job.getRequiredWorkplace())) {
        availableJobs.add(job); // Workplace exists
    }
}

// 3. Randomly assign job to NPC
NPCJob assignedJob = availableJobs.get(rng.nextInt(availableJobs.size()));

// 4. Assign workplace if needed
if (assignedJob.requiresWorkplace()) {
    List<Structure> jobStructures = workplaces.get(assignedJob.getRequiredWorkplace());
    npc.setWorkplaceStructureId(jobStructures.get(rng.nextInt(jobStructures.size())).getId());
}
```

---

## Job-Structure Mapping

| Job | Required Structure(s) | Production (Future) |
|-----|----------------------|---------------------|
| CHILD | None | None |
| UNEMPLOYED | None | None |
| FARMER | FARM | Food, crops |
| HERDER | RANCH (future) | Meat, leather |
| BLACKSMITH | FORGE | Metal tools, weapons |
| CARPENTER | WORKSHOP | Wood items, furniture |
| TAILOR | WORKSHOP | Cloth items, clothing |
| ALCHEMIST | WORKSHOP | Potions, reagents |
| MERCHANT | SHOP, MARKET | Trade goods |
| INNKEEPER | INN | Lodging, food |
| HEALER | TEMPLE, CLINIC | Healing services |
| SCHOLAR | LIBRARY, TEMPLE | Knowledge, books |
| MAGE | TOWER | Spells, enchantments |
| GUARD | BARRACKS, WATCHTOWER | Security |
| SOLDIER | BARRACKS | Military power |
| CHIEF | GUILD_HALL, TEMPLE | Leadership |
| PRIEST | TEMPLE | Religious services |

---

## Job Distribution at Worldgen

**Target Distribution:**
- **60% Residential:** CHILD (based on age), UNEMPLOYED (elders)
- **30% Economic:** FARMER, BLACKSMITH, MERCHANT, etc.
- **10% Special:** HEALER, SCHOLAR, MAGE, CHIEF, PRIEST

**Actual Distribution (Dynamic):**
- Based on available workplace structures
- If no FORGE exists, no BLACKSMITH jobs assigned
- UNEMPLOYED is fallback when no workplaces available

---

## Lifecycle & Job Changes

### Childhood → Adulthood
- Age 0-17: NPCJob.CHILD
- Age 18: Assigned adult job based on available structures

### Adulthood → Elderhood
- Age 60-70: May continue working
- Age 70+: Usually retire (NPCJob.UNEMPLOYED)

### Job Changes (Future)
- NPCs can change jobs based on:
  - Skill development
  - New structures built
  - Economic demand
  - Clan needs

---

## Usage Examples

### Assigning Job at Worldgen
```java
// Get available workplaces
Map<StructureType, List<Structure>> workplaces = getClanStructures();

// Assign job
NPCJob job = assignJob(workplaces, rng);

// Create NPC with job
NamedNPC npc = NPCGenerator.generateNPC(
    clanId, gender, age, job, homeId, currentTick, rng);

// Assign workplace if needed
if (job.requiresWorkplace() && workplaces.containsKey(job.getRequiredWorkplace())) {
    List<Structure> jobStructures = workplaces.get(job.getRequiredWorkplace());
    npc.setWorkplaceStructureId(jobStructures.get(rng.nextInt(jobStructures.size())).getId());
}
```

### Checking Job Requirements
```java
if (npc.getJob().requiresWorkplace()) {
    String workplaceId = npc.getWorkplaceStructureId();
    if (workplaceId == null) {
        System.err.println("NPC " + npc.getName() + " has job " + npc.getJob() + 
                           " but no workplace assigned!");
    }
}
```

### Finding NPCs by Job
```java
List<NamedNPC> blacksmiths = allNPCs.stream()
    .filter(npc -> npc.getJob() == NPCJob.BLACKSMITH)
    .collect(Collectors.toList());
```

---

## Future Enhancements

**Phase 2.x:**
- Production mechanics (blacksmiths produce weapons, farmers produce food)
- Skill development (NPCs improve at their jobs over time)
- Job satisfaction (affects productivity and loyalty)
- Job market (NPCs seek better jobs, negotiate wages)

**Phase 3.x:**
- Custom jobs (players define new job types)
- Job specializations (Master Blacksmith, Expert Farmer)
- Apprenticeship system (NPCs learn from masters)

---

## Related Classes

- `NamedNPC` — Uses NPCJob as a field
- `NPCGenerator` — Assigns jobs based on available structures
- `StructureType` — Defines workplace structure types
- `Structure` — Represents physical workplace buildings
