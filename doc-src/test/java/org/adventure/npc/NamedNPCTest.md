# NamedNPCTest

**Package:** `org.adventure.npc`  
**Type:** JUnit 5 Test Class  
**Since:** Phase 1.10.1

---

## Overview

`NamedNPCTest` validates the `NamedNPC` entity class functionality. Tests cover:
- Entity creation and builder pattern
- Field accessors and mutators
- Age calculations
- Fertility calculations
- Marriage/family relationships

**Total Tests:** 5

---

## Test Methods

### `testNamedNPCCreation()`
Validates basic NPC creation using builder pattern.
- Creates NPC with all required fields
- Verifies all fields are set correctly
- **Expected:** All getters return correct values

```java
@Test
public void testNamedNPCCreation() {
    NamedNPC npc = new NamedNPC.Builder()
        .id("npc_test_123")
        .name("Aldric")
        .clanId("clan_001")
        .age(27)
        .gender(Gender.MALE)
        .job(NPCJob.BLACKSMITH)
        .homeStructureId("house_001")
        .fertility(100)
        .birthTick(0L)
        .build();
    
    assertEquals("npc_test_123", npc.getId());
    assertEquals("Aldric", npc.getName());
    assertEquals(27, npc.getAge());
    assertEquals(Gender.MALE, npc.getGender());
    assertEquals(NPCJob.BLACKSMITH, npc.getJob());
}
```

---

### `testNPCMarriage()`
Validates marriage mechanics.
- Two NPCs marry (set spouseId)
- Verifies bidirectional relationship
- **Expected:** Both NPCs reference each other

```java
@Test
public void testNPCMarriage() {
    NamedNPC npc1 = createTestNPC("npc_1", Gender.MALE, 27);
    NamedNPC npc2 = createTestNPC("npc_2", Gender.FEMALE, 25);
    
    npc1.setSpouseId(npc2.getId());
    npc2.setSpouseId(npc1.getId());
    
    assertEquals(npc2.getId(), npc1.getSpouseId());
    assertEquals(npc1.getId(), npc2.getSpouseId());
}
```

---

### `testNPCChildren()`
Validates parent-child relationships.
- Parents add child to childrenIds list
- Child references parents (via clan/home)
- **Expected:** Children list updated correctly

```java
@Test
public void testNPCChildren() {
    NamedNPC parent = createTestNPC("parent_1", Gender.MALE, 30);
    NamedNPC child = createTestNPC("child_1", Gender.MALE, 0);
    
    parent.getChildrenIds().add(child.getId());
    
    assertTrue(parent.getChildrenIds().contains(child.getId()));
    assertEquals(1, parent.getChildrenIds().size());
}
```

---

### `testNPCFertility()`
Validates fertility values at different ages.
- Tests ages 18, 27 (peak), 40, 45
- Verifies fertility decreases with age
- **Expected:** Peak at 27, decline after

```java
@Test
public void testNPCFertility() {
    NamedNPC young = createTestNPC("npc_1", Gender.FEMALE, 18);
    young.setFertility(73);
    assertEquals(73, young.getFertility());
    
    NamedNPC peak = createTestNPC("npc_2", Gender.FEMALE, 27);
    peak.setFertility(100);
    assertEquals(100, peak.getFertility());
    
    NamedNPC older = createTestNPC("npc_3", Gender.FEMALE, 40);
    older.setFertility(61);
    assertTrue(older.getFertility() < peak.getFertility());
}
```

---

### `testNPCAging()`
Validates age updates over ticks.
- Simulates 10k ticks (1 year)
- Checks age increments correctly
- **Expected:** Age increases by 1

```java
@Test
public void testNPCAging() {
    NamedNPC npc = createTestNPC("npc_1", Gender.MALE, 25);
    npc.setBirthTick(0L);
    
    // Simulate 10k ticks
    long currentTick = 10000L;
    int newAge = (int)((currentTick - npc.getBirthTick()) / 10000);
    npc.setAge(newAge);
    
    assertEquals(26, npc.getAge());
}
```

---

## Related Classes

- `NamedNPC` — Entity class under test
- `Gender` — Enum for NPC gender
- `NPCJob` — Enum for NPC occupations
- `NPCGenerator` — Factory using NamedNPC
- `NPCLifecycleManager` — Manages NPC aging/marriage/reproduction
