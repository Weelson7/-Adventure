# Named NPC System — Phase 1.10.x Addition

**Status:** ✅ SPECIFIED (Implementation Pending)  
**Priority:** CRITICAL FOR LIVING WORLD  
**Integration:** Phase 1.10.1 (Week 1, Days 1-2)

---

## 🎯 Overview

The **Named NPC System** replaces placeholder NPC IDs with actual, living characters that have:
- **Unique names** (from predefined name lists)
- **Ages** (0-95 years, aging 1 year per ~10k ticks)
- **Genders** (MALE, FEMALE, NON_BINARY)
- **Jobs** (BLACKSMITH, FARMER, WARRIOR, MERCHANT, etc.)
- **Homes** (assigned to HOUSE structures, max 4 per house)
- **Marriages** (NPCs can marry each other)
- **Children** (married NPCs can reproduce)
- **Lifecycles** (NPCs age, marry, reproduce, and die)

---

## 🏠 Key Features

### 1. **Named NPCs with Identities**
```java
public class NamedNPC {
    String id;              // UUID
    String name;            // "Aldric", "Aria", etc.
    String clanId;
    int age;                // 0-95 years
    Gender gender;
    NPCJob job;             // BLACKSMITH, FARMER, WARRIOR, etc.
    String homeStructureId; // Where they live
    String workplaceStructureId; // Where they work
    String spouseId;        // null if unmarried
    List<String> childrenIds;
    int fertility;          // 0-100, decreases with age
    boolean isPlayer;       // true for player characters
    long birthTick;
}
```

### 2. **Jobs Tied to Structures**
NPCs have jobs that require specific workplace structures:
- **FARMER** → FARM (produces 50 gold/1000 ticks)
- **BLACKSMITH** → FORGE (produces 100 gold/1000 ticks)
- **MERCHANT** → SHOP (produces 80 gold/1000 ticks)
- **WARRIOR** → BARRACKS (produces 20 gold/1000 ticks)
- **PRIEST** → TEMPLE (produces 70 gold/1000 ticks)
- **WIZARD** → WIZARD_TOWER (produces 90 gold/1000 ticks)

### 3. **Marriage System**
- **Eligibility:** Age 18+, unmarried, same clan (or nearby clans)
- **Proposal:** 10% chance every ~6 months (5000 ticks) if compatible
- **Compatibility:** Similar age (within 10 years), unmarried
- **Effect:** Married NPCs move to same home, can reproduce

### 4. **Reproduction System**
- **Requirements:** Married couple, both fertile (age 18-45), home has space (<4 occupants)
- **Chance:** Fertility-based (peak at age 27, 100% fertility = 100% chance per check)
- **Cooldown:** 5000 ticks (~6 months) between reproduction attempts
- **Outcome:** Child NPC created (age 0, job CHILD)
- **Inheritance:** Children linked to both parents via `childrenIds`

### 5. **Aging & Death**
- **Aging:** 1 year per ~10,000 ticks
- **Lifecycle Stages:**
  - **CHILD** (0-17): No job, lives with parents
  - **ADULT** (18-60): Has job, can marry/reproduce
  - **ELDER** (60+): Often UNEMPLOYED, can still marry
- **Death:** Starts at age 70, increasing probability:
  - Age 70: 1% chance per check
  - Age 75: 5%
  - Age 80: 20%
  - Age 85: 50%
  - Age 90: 90%
  - Age 95+: 100%
- **Inheritance:** Spouse and children inherit possessions

### 6. **Player Integration**
Players are also NamedNPCs (`isPlayer = true`), allowing:
- **Marriage:** Players can marry NPCs (requires relationship > 75)
- **Reproduction:** Married players can have children (player controls timing)
- **Inheritance:** Children inherit player stats/traits
- **Clan Control:** Player-led clans skip automatic AI behavior

---

## 📊 Initial Population Distribution

At worldgen, each clan's population follows:
- **20% Children** (age 0-17, job CHILD)
- **50% Adults** (age 18-60, various jobs)
- **30% Elders** (age 60-80, often UNEMPLOYED)

**Marriage:** 50% of adults are married at worldgen

**Home Assignment:** NPCs distributed across HOUSE structures (max 4 per house)

---

## 🔄 Lifecycle Simulation

### Per Tick (in `NPCLifecycleManager.simulateTick()`):
1. **Age Update:** Increment age if 10k ticks elapsed since birth
2. **Job Update:** Children aging to 18 get assigned adult jobs
3. **Marriage Check:** Unmarried adults (18+) check for compatible partners every 5k ticks (10% proposal chance)
4. **Reproduction Check:** Married couples check fertility every 5k ticks (fertility% chance)
5. **Death Check:** Elderly NPCs (70+) roll for death (probability increases with age)
6. **Inheritance:** On death, spouse becomes unmarried, children remain linked

### Player NPCs Skip:
- Automatic aging (player controls aging)
- Automatic marriage proposals (player initiates)
- Automatic death (player characters don't die automatically)

---

## 🎮 Player-NPC Interactions

### Marriage:
```java
PlayerNPCInteraction.canMarry(player, npc);
// Checks: both 18+, unmarried, relationship > 75
PlayerNPCInteraction.marry(player, npc);
// Effect: Set spouseIds, move to player's home
```

### Reproduction:
```java
NamedNPC child = PlayerNPCInteraction.tryReproduceAsPlayer(player, spouse, structures, currentTick, rng);
// Returns: Child NPC if successful, null if house full or other issue
// Player controls: Timing (not automatic like NPC couples)
```

---

## 📁 New Files (8 classes)

```
src/main/java/org/adventure/npc/
├── NamedNPC.java                   (data model)
├── NPCGenerator.java               (worldgen NPC creation)
├── NPCLifecycleManager.java        (aging, marriage, reproduction, death)
├── PlayerNPCInteraction.java       (player marriage/reproduction)
├── Gender.java                     (enum: MALE, FEMALE, NON_BINARY)
└── NPCJob.java                     (enum: 15+ job types)

src/test/java/org/adventure/npc/
├── NamedNPCTest.java
├── NPCGeneratorTest.java
├── NPCLifecycleManagerTest.java
└── PlayerNPCInteractionTest.java
```

---

## 🧪 Testing Requirements

### Determinism:
```java
@Test
public void testNPCGenerationDeterminism() {
    // Same seed → same NPC names, ages, jobs, marriages
}

@Test
public void testNPCLifecycleDeterminism() {
    // Same seed → same marriages, births, deaths over 10k ticks
}
```

### Lifecycle:
```java
@Test
public void testNPCsAge() {
    // Verify NPCs age 1 year per 10k ticks
}

@Test
public void testMarriageProposals() {
    // Verify compatible NPCs marry with expected probability
}

@Test
public void testReproduction() {
    // Verify married couples produce children based on fertility
}

@Test
public void testDeath() {
    // Verify elderly NPCs die with age-based probability
}
```

### Player Integration:
```java
@Test
public void testPlayerMarriage() {
    // Verify player can marry NPC with sufficient relationship
}

@Test
public void testPlayerReproduction() {
    // Verify player can have children with spouse
}
```

---

## 🎯 Integration Points

### WorldGen (Phase 12):
```java
private void generateNamedNPCs(long seed) {
    Map<String, List<Structure>> clanStructures = buildClanStructureMap();
    this.npcs = ClanGenerator.generateNPCsForClans(clans, clanStructures, seed, 0L);
}
```

### RegionSimulator (Phase 1.10.3):
```java
public void simulateTick(Region region, long currentTick) {
    // FIRST: NPC lifecycle (before clan expansion)
    npcLifecycleManager.simulateTick(region.getNPCs(), region.getStructures(), currentTick);
    
    // THEN: Clan expansion (uses updated NPC population)
    clanExpansionSimulator.simulateTick(...);
}
```

---

## 📈 Expected Outcomes

After 10,000 ticks (~1 year in-game):
- **Marriages:** Additional ~10-20% of unmarried adults marry
- **Births:** ~5-10 children born per 100 adults (fertility-dependent)
- **Deaths:** ~1-2 elders die per 100 NPCs (age-dependent)
- **Population Growth:** Net positive initially (births > deaths)
- **Job Changes:** Children aging to 18 get assigned jobs

After 100,000 ticks (~10 years):
- **Generational Shift:** Original children become adults with jobs
- **New Families:** Second generation marries and reproduces
- **Population Stabilization:** Births ≈ deaths (equilibrium)
- **Clan Growth:** NPCs born into clans increase member count

---

## 🚀 Implementation Priority

**Week 1, Days 1-2:**
1. Create `NamedNPC` class with all fields
2. Create `NPCGenerator` with name lists and population generation
3. Create `Gender` and `NPCJob` enums
4. Write basic tests (creation, determinism)

**Week 1, Day 6:**
1. Create `NPCLifecycleManager` with aging/marriage/reproduction/death
2. Write comprehensive lifecycle tests

**Week 3, Day 4:**
1. Create `PlayerNPCInteraction` for player marriage/reproduction
2. Write player interaction tests

---

## ⚠️ Critical Considerations

1. **Determinism:** All NPC generation and lifecycle events must be deterministic (same seed = same results)
2. **Performance:** NPCLifecycleManager must handle 1000+ NPCs per tick efficiently
3. **Balance:** Fertility rates must produce sustainable population growth (not too fast/slow)
4. **Player Experience:** Player-controlled NPCs must feel different (no forced aging/death)
5. **Story Integration:** Marriages, births, deaths should generate Story events
6. **Home Capacity:** Enforce max 4 NPCs per HOUSE (prevents overcrowding)

---

## 🔗 Related Documentation

- [BUILD_PHASE1.10.x.md](BUILD_PHASE1.10.x.md) — Main implementation guide
- [docs/characters_stats_traits_skills.md](docs/characters_stats_traits_skills.md) — Character system
- [docs/societies_clans_kingdoms.md](docs/societies_clans_kingdoms.md) — Clan structure
- [docs/structures_ownership.md](docs/structures_ownership.md) — Structure types

---

**END OF NAMED_NPC_SYSTEM.md**
