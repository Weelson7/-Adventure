# NPCTest.java - NPC System Test Suite

**Package:** `org.adventure`  
**Source:** [NPCTest.java](../../../../src/test/java/org/adventure/NPCTest.java)  
**Phase:** MVP Phase 1.3 (Characters & NPCs)  
**Test Framework:** JUnit 5.9.3

## Overview

`NPCTest` validates NPC spawning, behavior types, biome-specific distributions, deterministic generation, and trait inheritance. With 18 tests, this suite ensures NPCs populate the world realistically and behave predictably.

## Test Coverage Summary

| Category | Tests | Purpose |
|----------|-------|---------|
| **Creation & Basic Properties** | 3 | Constructor, extends Character, behavior types |
| **Deterministic Spawning** | 4 | Same seed → same NPC, position, race |
| **Biome-Specific Logic** | 5 | Race/behavior distribution by biome |
| **Spawn Density** | 2 | Biome-specific spawn rates |
| **Trait Inheritance** | 3 | Hereditary traits from parents |
| **Movement & Health** | 1 | Position tracking, damage |

**Total: 18 tests, 100% passing ✅**

## Key Test Documentation

### testNPCCreation()
```java
NPC npc = new NPC("npc_1", "Goblin Scout", Race.ORC, 
    BehaviorType.AGGRESSIVE, 10, 20, "forest");
assertEquals("npc_1", npc.getId());
assertEquals(BehaviorType.AGGRESSIVE, npc.getBehaviorType());
assertEquals(10, npc.getSpawnX());
```
**Validates:** Constructor sets all fields correctly

### testDeterministicSpawning()
```java
NPC npc1 = NPC.spawnNPC(12345L, 10, 20, Biome.FOREST);
NPC npc2 = NPC.spawnNPC(12345L, 10, 20, Biome.FOREST);
assertEquals(npc1.getRace(), npc2.getRace());
assertEquals(npc1.getBehaviorType(), npc2.getBehaviorType());
```
**Validates:** Same seed + coordinates → identical NPC

### testBiomeSpecificRaceDistribution()
```java
// Spawn 100 NPCs in forest, count elves
int elfCount = 0;
for (int i = 0; i < 100; i++) {
    NPC npc = NPC.spawnNPC(seed, i, 0, Biome.FOREST);
    if (npc.getRace() == Race.ELF) elfCount++;
}
assertTrue(elfCount > 30, "Forest should have many elves");
```
**Validates:** Biome-specific race weights work correctly

### testHereditaryTraitInheritance()
```java
NPC parent1 = new NPC(..., Race.DWARF, ...);
parent1.addTrait(Trait.ROBUST);  // Hereditary

NPC parent2 = new NPC(..., Race.DWARF, ...);

NPC child = NPC.generateOffspring(parent1, parent2, seed, 0, 0);
// Child has 50% chance to inherit ROBUST
```
**Validates:** Hereditary traits pass to offspring (50% chance)

### testSpawnDensityByBiome()
```java
int grasslandSpawns = countSpawns(Biome.GRASSLAND, 1000);
int desertSpawns = countSpawns(Biome.DESERT, 1000);
assertTrue(grasslandSpawns > desertSpawns * 2, 
    "Grassland should have 2-5x more NPCs than desert");
```
**Validates:** Grassland (5%) > Desert (1%) spawn rates

## References

- **Source Class**: [NPC.md](../../../main/java/org/adventure/character/NPC.md)
- **Related Tests**: [CharacterTest.md](CharacterTest.md)

---

**Status:** ✅ 18/18 tests passing - NPC spawning validated
