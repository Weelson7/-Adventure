# PlayerNPCInteraction

**Package:** `org.adventure.npc`  
**Type:** Class (Service)  
**Since:** Phase 1.10.1

---

## Overview

`PlayerNPCInteraction` enables players to interact with Named NPCs for marriage and reproduction. This class provides:
- Player-NPC marriage validation and execution
- Player-NPC reproduction mechanics
- Relationship checks (future: reputation system)
- Child inheritance of player traits

Players follow the same lifecycle rules as NPCs but have manual control over timing.

---

## Key Methods

### `canMarry()`
Checks if a player can marry a target NPC.

**Signature:**
```java
public boolean canMarry(NamedNPC player, NamedNPC target)
```

**Requirements:**
- `player.isPlayer()` must be true
- Both player and target age >= 18
- Both unmarried (spouseId == null)
- Relationship > 75 (future: reputation system)

**Returns:** `true` if marriage is allowed

**Example:**
```java
NamedNPC player = getPlayerNPC("player_123");
NamedNPC npc = getNPCById("npc_clan_123_456");

if (interaction.canMarry(player, npc)) {
    interaction.marry(player, npc);
    System.out.println("Marriage successful!");
} else {
    System.out.println("Cannot marry: requirements not met");
}
```

---

### `marry()`
Executes marriage between player and NPC.

**Signature:**
```java
public void marry(NamedNPC player, NamedNPC target)
```

**Effects:**
1. Set `player.spouseId = target.id`
2. Set `target.spouseId = player.id`
3. Move target to player's home (or prompt player to choose)
4. Generate "Marriage" event (future)
5. Update relationship to 100 (future)

**Example:**
```java
// Before
player.getSpouseId() == null;
npc.getSpouseId() == null;

// Marry
interaction.marry(player, npc);

// After
player.getSpouseId().equals(npc.getId());
npc.getSpouseId().equals(player.getId());
npc.getHomeStructureId().equals(player.getHomeStructureId());
```

---

### `canReproduce()`
Checks if player can have a child with spouse.

**Signature:**
```java
public boolean canReproduce(
    NamedNPC player,
    NamedNPC spouse,
    List<Structure> structures
)
```

**Requirements:**
- `player.spouseId == spouse.id` (must be married)
- Player or spouse fertility > 0
- Home exists and has space (< 4 occupants)
- Cooldown elapsed (5,000 ticks since last child)

**Returns:** `true` if reproduction is possible

---

### `reproduce()`
Creates a child for player and spouse.

**Signature:**
```java
public NamedNPC reproduce(
    NamedNPC player,
    NamedNPC spouse,
    List<Structure> structures,
    long currentTick,
    Random rng
)
```

**Parameters:**
- `player` — Player NPC
- `spouse` — Spouse NPC (player or NPC)
- `structures` — World structures (for home validation)
- `currentTick` — Current game tick
- `rng` — Random number generator (for gender, inherited traits)

**Returns:** New child `NamedNPC`, or `null` if reproduction failed

**Algorithm:**
```
1. Validate requirements (married, home space, cooldown)
2. If invalid → return null
3. Generate child:
   - Random gender (50/50)
   - Age = 0 (newborn)
   - Job = NPCJob.CHILD
   - Home = player's home
   - Clan = player's clan
   - Birth tick = currentTick
4. Inherit traits from player (future):
   - Stats (STR, AGI, INT, WIS, CHA) — average of parents ±10%
   - Traits — 50% chance to inherit each parent trait
5. Add child to parent's childrenIds
6. Add child to spouse's childrenIds
7. Generate "Birth" event (future)
8. Update lastReproductionCheck for both parents
9. Return child NPC
```

**Example:**
```java
Random rng = new Random();
NamedNPC child = interaction.reproduce(
    player, spouse, structures, currentTick, rng);

if (child != null) {
    System.out.println("Child born: " + child.getName());
    System.out.println("Gender: " + child.getGender());
    player.getChildrenIds().contains(child.getId()); // true
    spouse.getChildrenIds().contains(child.getId()); // true
} else {
    System.out.println("Reproduction failed: house full or cooldown");
}
```

---

### `divorce()`
Ends marriage between player and spouse (future feature).

**Signature:**
```java
public void divorce(NamedNPC player, NamedNPC spouse)
```

**Effects:**
1. Set `player.spouseId = null`
2. Set `spouse.spouseId = null`
3. Children remain linked to both parents
4. Relationship penalty: -50 (future)
5. Generate "Divorce" event (future)

---

## Player vs NPC Mechanics

### Marriage
| Aspect | Player | NPC |
|--------|--------|-----|
| Timing | Manual (player chooses when) | Automatic (10% chance per 5k ticks) |
| Partner Selection | Player chooses target | Algorithm selects compatible NPC |
| Requirements | Age 18+, relationship > 75 | Age 18+, same clan, age diff < 10 |
| Home Assignment | Player chooses (prompt) | Auto-move to partner's home |

### Reproduction
| Aspect | Player | NPC |
|--------|--------|-----|
| Timing | Manual (player initiates) | Automatic (fertility-based chance) |
| Cooldown | 5,000 ticks (same as NPC) | 5,000 ticks |
| Fertility | Player fertility (age-based) | Both parents' fertility |
| Inheritance | Child inherits player traits | Child has random traits |

---

## Future Features (Post-MVP)

### Reputation System
```java
public boolean canMarry(NamedNPC player, NamedNPC target) {
    // Check relationship
    int relationship = reputationSystem.getRelationship(player, target);
    if (relationship < 75) {
        return false; // Not friendly enough
    }
    
    // Check clan approval
    int clanReputation = reputationSystem.getClanReputation(
        player.getClanId(), target.getClanId());
    if (clanReputation < 0) {
        return false; // Clans are enemies
    }
    
    return true;
}
```

### Trait Inheritance
```java
private void inheritTraits(NamedNPC child, NamedNPC parent1, NamedNPC parent2) {
    // Inherit stats (average ±10%)
    int strAvg = (parent1.getStr() + parent2.getStr()) / 2;
    child.setStr(strAvg + rng.nextInt(21) - 10); // ±10
    
    // Inherit traits (50% chance each)
    for (Trait trait : parent1.getTraits()) {
        if (rng.nextDouble() < 0.5) {
            child.addTrait(trait);
        }
    }
    
    for (Trait trait : parent2.getTraits()) {
        if (rng.nextDouble() < 0.5) {
            child.addTrait(trait);
        }
    }
}
```

### Multiple Spouses (Polygamy)
```java
// Replace spouseId with spouseIds (List<String>)
public boolean canMarry(NamedNPC player, NamedNPC target) {
    if (player.getSpouseIds().size() >= MAX_SPOUSES) {
        return false; // Already married to max partners
    }
    return true;
}
```

---

## Usage Examples

### Player Marriage Flow
```java
// 1. Player meets NPC
NamedNPC player = getPlayerNPC(playerId);
NamedNPC npc = findNPCByName("Aria");

// 2. Build relationship (future)
// reputationSystem.increaseRelationship(player, npc, 10); // gift, quest, etc.

// 3. Check if can marry
if (interaction.canMarry(player, npc)) {
    // 4. Prompt player
    System.out.println("Propose to " + npc.getName() + "? (y/n)");
    String response = scanner.nextLine();
    
    if (response.equals("y")) {
        // 5. Execute marriage
        interaction.marry(player, npc);
        System.out.println("You married " + npc.getName() + "!");
    }
} else {
    System.out.println("Cannot marry: not friendly enough");
}
```

### Player Reproduction Flow
```java
// 1. Player requests child
NamedNPC player = getPlayerNPC(playerId);
NamedNPC spouse = getNPCById(player.getSpouseId());

if (spouse == null) {
    System.out.println("You are not married");
    return;
}

// 2. Check if can reproduce
if (!interaction.canReproduce(player, spouse, structures)) {
    System.out.println("Cannot have child: house full or cooldown active");
    return;
}

// 3. Attempt reproduction
Random rng = new Random();
NamedNPC child = interaction.reproduce(
    player, spouse, structures, currentTick, rng);

if (child != null) {
    System.out.println("Child born: " + child.getName());
    System.out.println("Gender: " + child.getGender());
    
    // Add to world
    world.addNPC(child);
} else {
    System.out.println("Reproduction failed");
}
```

### NPC Spouse Behavior
```java
// NPCs married to players still follow lifecycle
NPCLifecycleManager manager = new NPCLifecycleManager();

for (long tick = 0; tick < 100000; tick++) {
    manager.tick(tick, allNPCs, structures);
    
    // Check if player's spouse aged, died, etc.
    NamedNPC spouse = getNPCById(player.getSpouseId());
    if (spouse == null) {
        System.out.println("Your spouse has died!");
        player.setSpouseId(null);
    }
}
```

---

## Testing

### Unit Tests
```java
@Test
public void testCanMarry_Valid() {
    NamedNPC player = createPlayerNPC(age=27);
    NamedNPC npc = createNPC(age=25);
    
    assertTrue(interaction.canMarry(player, npc));
}

@Test
public void testCanMarry_TooYoung() {
    NamedNPC player = createPlayerNPC(age=17);
    NamedNPC npc = createNPC(age=25);
    
    assertFalse(interaction.canMarry(player, npc));
}

@Test
public void testMarry() {
    NamedNPC player = createPlayerNPC(age=27);
    NamedNPC npc = createNPC(age=25);
    
    interaction.marry(player, npc);
    
    assertEquals(npc.getId(), player.getSpouseId());
    assertEquals(player.getId(), npc.getSpouseId());
    assertEquals(player.getHomeStructureId(), npc.getHomeStructureId());
}

@Test
public void testReproduce_Success() {
    NamedNPC player = createMarriedPlayerNPC(age=27, fertility=100);
    NamedNPC spouse = getNPCById(player.getSpouseId());
    
    NamedNPC child = interaction.reproduce(
        player, spouse, structures, 0L, new Random());
    
    assertNotNull(child);
    assertEquals(0, child.getAge());
    assertEquals(NPCJob.CHILD, child.getJob());
    assertTrue(player.getChildrenIds().contains(child.getId()));
}

@Test
public void testReproduce_HouseFull() {
    // Create house with 4 occupants
    Structure house = createHouse();
    addNPCsToHouse(house, 4);
    
    NamedNPC player = createMarriedPlayerNPC(homeId=house.getId());
    NamedNPC spouse = getNPCById(player.getSpouseId());
    
    NamedNPC child = interaction.reproduce(
        player, spouse, structures, 0L, new Random());
    
    assertNull(child); // House full
}
```

---

## Related Classes

- `NamedNPC` — Entity class for NPCs and players
- `NPCGenerator` — Factory for creating NPCs
- `NPCLifecycleManager` — Manages NPC lifecycle (also affects player spouses)
- `Gender` — Enum for gender
- `NPCJob` — Enum for jobs
- `ReputationSystem` — (Future) Relationship tracking
