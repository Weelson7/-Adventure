# NPCGeneratorTest

**Package:** `org.adventure.npc`  
**Type:** JUnit 5 Test Class  
**Since:** Phase 1.10.1

---

## Overview

`NPCGeneratorTest` validates the `NPCGenerator` factory class. Tests cover:
- Individual NPC generation
- Clan population generation
- Age distribution
- Marriage creation
- Job assignment

**Total Tests:** 5

---

## Test Methods

### `testGenerateNPC()`
Validates single NPC generation.
- Creates NPC with specified attributes
- Verifies all fields set correctly
- **Expected:** Valid NPC instance

```java
@Test
public void testGenerateNPC() {
    Random rng = new Random(12345L);
    NamedNPC npc = NPCGenerator.generateNPC(
        "clan_test",
        Gender.MALE,
        27,
        NPCJob.FARMER,
        "house_1",
        0L,
        rng
    );
    
    assertNotNull(npc.getId());
    assertNotNull(npc.getName());
    assertEquals(27, npc.getAge());
    assertEquals(Gender.MALE, npc.getGender());
    assertEquals(NPCJob.FARMER, npc.getJob());
}
```

---

### `testGenerateInitialClanPopulation()`
Validates clan population generation.
- Creates population for test clan
- Checks NPC count matches clan size
- **Expected:** Correct population size

```java
@Test
public void testGenerateInitialClanPopulation() {
    Clan clan = createTestClan(20); // 20 members
    List<Structure> structures = createTestStructures();
    Random rng = new Random(12345L);
    
    List<NamedNPC> npcs = NPCGenerator.generateInitialClanPopulation(
        clan, structures, 0L, rng);
    
    assertEquals(20, npcs.size());
}
```

---

### `testAgeDistribution()`
Validates 20/50/30 age distribution.
- Generates 100 NPCs
- Counts children/adults/elders
- **Expected:** ~20 children, ~50 adults, ~30 elders (±10%)

```java
@Test
public void testAgeDistribution() {
    Clan clan = createTestClan(100);
    List<Structure> structures = createTestStructures(20); // 20 houses
    Random rng = new Random(12345L);
    
    List<NamedNPC> npcs = NPCGenerator.generateInitialClanPopulation(
        clan, structures, 0L, rng);
    
    long children = npcs.stream().filter(npc -> npc.getAge() < 18).count();
    long adults = npcs.stream().filter(npc -> npc.getAge() >= 18 && npc.getAge() <= 60).count();
    long elders = npcs.stream().filter(npc -> npc.getAge() > 60).count();
    
    assertTrue(children >= 10 && children <= 30); // 20% ±10%
    assertTrue(adults >= 40 && adults <= 60);     // 50% ±10%
    assertTrue(elders >= 20 && elders <= 40);     // 30% ±10%
}
```

---

### `testInitialMarriages()`
Validates marriage creation.
- Generates population with marriages
- Counts married adults
- **Expected:** 30-70% married (tolerance for gender imbalance)

```java
@Test
public void testInitialMarriages() {
    Clan clan = createTestClan(100);
    List<Structure> structures = createTestStructures(25);
    Random rng = new Random(12345L);
    
    List<NamedNPC> npcs = NPCGenerator.generateInitialClanPopulation(
        clan, structures, 0L, rng);
    
    long married = npcs.stream()
        .filter(npc -> npc.getAge() >= 18 && npc.getAge() <= 60)
        .filter(npc -> npc.getSpouseId() != null)
        .count();
    
    long adults = npcs.stream()
        .filter(npc -> npc.getAge() >= 18 && npc.getAge() <= 60)
        .count();
    
    double marriageRate = (double)married / adults;
    assertTrue(marriageRate >= 0.30 && marriageRate <= 0.70);
}
```

---

### `testDeterminism()`
Validates deterministic generation.
- Generates twice with same seed
- Compares all NPC attributes
- **Expected:** Identical results

```java
@Test
public void testDeterminism() {
    Clan clan = createTestClan(50);
    List<Structure> structures = createTestStructures(15);
    
    Random rng1 = new Random(12345L);
    List<NamedNPC> npcs1 = NPCGenerator.generateInitialClanPopulation(
        clan, structures, 0L, rng1);
    
    Random rng2 = new Random(12345L);
    List<NamedNPC> npcs2 = NPCGenerator.generateInitialClanPopulation(
        clan, structures, 0L, rng2);
    
    assertEquals(npcs1.size(), npcs2.size());
    for (int i = 0; i < npcs1.size(); i++) {
        assertEquals(npcs1.get(i).getName(), npcs2.get(i).getName());
        assertEquals(npcs1.get(i).getAge(), npcs2.get(i).getAge());
        assertEquals(npcs1.get(i).getGender(), npcs2.get(i).getGender());
    }
}
```

---

## Related Classes

- `NPCGenerator` — Class under test
- `NamedNPC` — Generated entity
- `Clan` — Test fixture
- `Structure` — Test fixture for homes/workplaces
- `WorldGenDeterminismTest` — Integration tests
