# WorldGenDeterminismTest

**Package:** `org.adventure`  
**Type:** JUnit 5 Test Class  
**Since:** Phase 1.10.1

---

## Overview

`WorldGenDeterminismTest` validates that all Phase 1.10.1 worldgen components produce identical results from the same seed. This test class ensures:
- Geography determinism
- Clan/settlement determinism
- NPC determinism (names, ages, jobs, marriages)
- Prophecy/quest determinism
- Story determinism

**Total Tests:** 13  
**Coverage:** All Phase 1.10.1 quality gates

---

## Test Methods

### Geography & Clans

#### `testWorldgenDeterminism_Geography()`
Validates that two worlds generated with the same seed have identical geography.
- Checks tile elevation, biomes, rivers, features
- Uses checksum comparison
- **Expected:** Identical checksums

#### `testWorldgenDeterminism_Clans()`
Validates that clans are generated deterministically.
- Checks clan count, IDs, positions, types, treasury
- **NEW:** Checks member counts
- **Expected:** Identical clan lists

#### `testWorldgenDeterminism_Settlements()`
Validates that settlements and structures are identical.
- Checks settlement count, positions, structure IDs
- Verifies structure types and locations
- **Expected:** Identical settlement lists

---

### NPC System

#### `testWorldgenDeterminism_NPCs()`
Validates that NPCs are generated deterministically.
- Checks NPC count, names, ages, genders, jobs
- Verifies home and workplace assignments
- **Expected:** Identical NPC lists

#### `testNPCAgeDistribution()`
Validates NPC age distribution matches spec.
- Counts children (age 0-17), adults (18-60), elders (60-80)
- **Expected:** 20/50/30 split (±10% tolerance)
- **Example:** 100 NPCs → 20±10 children, 50±10 adults, 30±10 elders

#### `testNPCMarriageDistribution()`
Validates marriage rate is ~50% of adults.
- Counts married adults (age 18-60 inclusive)
- **Expected:** 30-70% married (tolerance for gender imbalance)
- **Fixed:** Age filter changed from `<60` to `<=60` to match generation logic

#### `testNPCHomeAssignment()`
Validates all NPCs have valid homes.
- Checks all NPCs have non-null homeStructureId
- Verifies homes are valid HOUSE structures
- **Expected:** 100% NPCs have homes

---

### Stories, Prophecies, Quests

#### `testWorldgenDeterminism_Stories()`
Validates stories are generated deterministically.
- Checks story count, IDs, types, locations
- **Expected:** Identical story lists

#### `testWorldgenDeterminism_Prophecies()`
Validates prophecies are deterministic.
- Checks prophecy count, IDs, types, linked features
- **Expected:** Identical prophecy lists

#### `testWorldgenDeterminism_Quests()`
Validates quests are deterministic.
- Checks quest count, IDs, types, linked features
- **Expected:** Identical quest lists

---

### Scaling & Integration

#### `testClanScalingWithWorldSize()`
Validates clan count scales with world size.
- Tests small (128×128), medium (256×256), large (512×512) worlds
- **Expected:** Minimum 3 clans for all sizes

#### `testOneSettlementPerClan()`
Validates 1:1 clan-to-settlement ratio.
- Checks settlement count == clan count
- **Expected:** Equal counts

#### `testNPCJobWorkplaceIntegrity()`
Validates NPCs with jobs have valid workplaces.
- For jobs requiring workplace: verify workplace exists
- UNEMPLOYED/CHILD: no workplace requirement
- **Expected:** 100% valid job assignments

---

## Quality Gate Mapping

| Quality Gate | Test Method |
|--------------|-------------|
| Same seed → same geography | testWorldgenDeterminism_Geography() |
| Same seed → same clans | testWorldgenDeterminism_Clans() |
| Same seed → same settlements | testWorldgenDeterminism_Settlements() |
| Same seed → same NPCs | testWorldgenDeterminism_NPCs() |
| Same seed → same stories | testWorldgenDeterminism_Stories() |
| Same seed → same prophecies | testWorldgenDeterminism_Prophecies() |
| Same seed → same quests | testWorldgenDeterminism_Quests() |
| Age distribution 20/50/30 | testNPCAgeDistribution() |
| Marriage rate 30-70% | testNPCMarriageDistribution() |
| All NPCs have homes | testNPCHomeAssignment() |
| Jobs have workplaces | testNPCJobWorkplaceIntegrity() |
| Minimum 3 clans | testClanScalingWithWorldSize() |
| 1 settlement per clan | testOneSettlementPerClan() |

---

## Test Results

**Status:** ✅ ALL PASSING (547 total tests)

**Last Run:**
- 13 determinism tests: PASS
- 10 NPC unit tests: PASS
- 524 existing tests: PASS
- **Total:** 547/547 (100% success rate)

**Build Time:** ~4.6 seconds  
**Worldgen Time:** ~665ms per 256×256 world

---

## Example Test

```java
@Test
public void testWorldgenDeterminism_NPCs() {
    long seed = 12345L;
    
    WorldGen gen1 = new WorldGen(256, 256);
    gen1.generate(seed);
    
    WorldGen gen2 = new WorldGen(256, 256);
    gen2.generate(seed);
    
    List<NamedNPC> npcs1 = gen1.getNPCs();
    List<NamedNPC> npcs2 = gen2.getNPCs();
    
    // Same count
    assertEquals(npcs1.size(), npcs2.size());
    
    // Same NPCs (names, ages, jobs)
    for (int i = 0; i < npcs1.size(); i++) {
        assertEquals(npcs1.get(i).getName(), npcs2.get(i).getName());
        assertEquals(npcs1.get(i).getAge(), npcs2.get(i).getAge());
        assertEquals(npcs1.get(i).getGender(), npcs2.get(i).getGender());
        assertEquals(npcs1.get(i).getJob(), npcs2.get(i).getJob());
    }
}
```

---

## Related Classes

- `WorldGen` — System under test
- `NamedNPC` — NPC entity
- `Clan` — Clan entity
- `Settlement` — Settlement entity
- `Prophecy` — Prophecy entity
- `Quest` — Quest entity
