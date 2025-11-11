# ResourceNode Class Documentation

**Package:** `org.adventure.region`  
**File:** `src/main/java/org/adventure/region/ResourceNode.java`  
**Purpose:** Represents harvestable resources with capped regeneration mechanics

---

## Overview

The `ResourceNode` class models individual resource nodes in the game world (trees, ore veins, crop fields). Resources regenerate over time using a capped exponential formula that prevents runaway accumulation while maintaining realism.

### Key Concepts

- **Capped Regeneration:** Resources approach maximum capacity asymptotically
- **Resource Types:** Five categories (wood, ore, crops, stone, herbs) with different regen rates
- **Harvesting:** Players extract resources, reducing current quantity
- **Finite vs Renewable:** Some resources have zero/low regen (ore, stone), others regenerate quickly (wood, crops)

---

## Class Structure

### Enums

#### ResourceType

```java
public enum ResourceType {
    WOOD,        // Renewable, fast regeneration
    ORE,         // Finite, very slow/zero regeneration
    CROPS,       // Renewable, moderate regeneration
    STONE,       // Finite, zero regeneration
    HERBS        // Renewable, moderate regeneration
}
```

**Resource Characteristics:**

| Type | Regeneration | Typical regenRate | Use Case |
|------|--------------|-------------------|----------|
| WOOD | Fast | 5-10 | Renewable timber resource |
| ORE | None/Slow | 0-0.1 | Limited mineral deposits |
| CROPS | Moderate | 6-8 | Agricultural production |
| STONE | None | 0 | Quarry stone (finite) |
| HERBS | Moderate | 4-6 | Medicinal/crafting plants |

---

## Fields

```java
private final int id;
private final ResourceType type;
private final int x;
private final int y;
private final double rMax;        // Maximum resource quantity
private final double regenRate;   // Regeneration rate per second
private double currentQuantity;
```

- **id:** Unique identifier for this resource node
- **type:** Resource category (WOOD, ORE, etc.)
- **x, y:** World coordinates of this node
- **rMax:** Maximum capacity (resources cannot exceed this)
- **regenRate:** Regeneration speed in units/second
- **currentQuantity:** Current available resources (0 to rMax)

---

## Constructor

```java
public ResourceNode(int id, ResourceType type, int x, int y, double rMax, double regenRate)
```

**Parameters:**
- `id` — Unique node identifier
- `type` — Resource category
- `x, y` — World coordinates
- `rMax` — Maximum capacity
- `regenRate` — Regeneration rate per second

**Initial State:**
- `currentQuantity = rMax` (starts at full capacity)

**Example:**
```java
// Create a wood resource node with max capacity 100, regen rate 5/sec
ResourceNode wood = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 5.0);

// Create a finite ore vein with no regeneration
ResourceNode ore = new ResourceNode(2, ResourceNode.ResourceType.ORE, 260, 260, 100.0, 0.0);
```

---

## Public Methods

### Regeneration

#### regenerate

```java
public void regenerate(double deltaTime)
```

Regenerate resources using the capped exponential formula from `docs/economy_resources.md`.

**Formula:**
```
R(t+Δt) = R(t) + regenRate * Δt * (1 - R(t)/Rmax)
```

**Parameters:**
- `deltaTime` — Time elapsed in seconds

**Behavior:**
- If `currentQuantity >= rMax`, no regeneration (already at max)
- Otherwise, add `regenRate * deltaTime * (1 - currentQuantity / rMax)`
- Result is clamped to `[0, rMax]`

**Example:**
```java
ResourceNode node = new ResourceNode(1, ResourceType.WOOD, 0, 0, 100.0, 10.0);
node.setCurrentQuantity(50.0); // Start at half capacity

// Regenerate for 1 second
node.regenerate(1.0);
// Result: 50 + 10*1*(1-50/100) = 50 + 5 = 55

// Regenerate for another 1 second
node.regenerate(1.0);
// Result: 55 + 10*1*(1-55/100) = 55 + 4.5 = 59.5
```

**Mathematical Properties:**
- **Asymptotic approach:** As `R → Rmax`, regeneration rate → 0
- **Linear at low quantities:** When `R ≈ 0`, regen ≈ `regenRate * Δt`
- **Stability:** Cannot overshoot `Rmax`

**Edge Cases:**
- `regenRate = 0`: Finite resource (no regeneration)
- `currentQuantity = 0`: Full regeneration rate applied
- `currentQuantity = rMax`: No regeneration

---

### Harvesting

#### harvest

```java
public double harvest(double amount)
```

Extract resources from this node.

**Parameters:**
- `amount` — Desired harvest amount

**Returns:** Actual amount harvested (may be less if insufficient resources)

**Behavior:**
- `harvested = min(amount, currentQuantity)`
- `currentQuantity -= harvested`
- Returns `harvested`

**Example:**
```java
ResourceNode node = new ResourceNode(1, ResourceType.WOOD, 0, 0, 100.0, 5.0);
// Node has 100 units

double got = node.harvest(30.0);
// got = 30.0, currentQuantity = 70.0

got = node.harvest(100.0); // Try to harvest more than available
// got = 70.0, currentQuantity = 0.0 (depleted)
```

**Use Case:**
```java
// Player harvests wood
double neededWood = 50.0;
double harvested = woodNode.harvest(neededWood);
if (harvested < neededWood) {
    System.out.println("Not enough wood! Only got " + harvested);
}
player.addInventory(ResourceType.WOOD, harvested);
```

---

### Status Checks

#### isDepleted

```java
public boolean isDepleted()
```

Check if resource node is empty.

**Returns:** `true` if `currentQuantity <= 0`

**Example:**
```java
if (node.isDepleted()) {
    System.out.println("Resource depleted! Waiting for regeneration...");
}
```

#### isFull

```java
public boolean isFull()
```

Check if resource node is at maximum capacity.

**Returns:** `true` if `currentQuantity >= rMax`

**Example:**
```java
if (node.isFull()) {
    System.out.println("Resource at max capacity, no regeneration needed");
}
```

---

### Property Access

#### setCurrentQuantity

```java
public void setCurrentQuantity(double quantity)
```

Set current quantity with clamping to valid range `[0, rMax]`.

**Parameters:**
- `quantity` — Desired quantity

**Behavior:**
- `currentQuantity = max(0, min(rMax, quantity))`

**Example:**
```java
node.setCurrentQuantity(50.0);   // Set to 50
node.setCurrentQuantity(150.0);  // Clamped to rMax (100)
node.setCurrentQuantity(-10.0);  // Clamped to 0
```

---

### Getters

```java
public int getId()
public ResourceType getType()
public int getX()
public int getY()
public double getRMax()
public double getRegenRate()
public double getCurrentQuantity()
```

All properties have standard getters for read access.

---

## Usage Patterns

### Basic Resource Lifecycle

```java
// 1. Create resource node
ResourceNode tree = new ResourceNode(1, ResourceType.WOOD, 100, 100, 100.0, 5.0);

// 2. Player harvests
double wood = tree.harvest(40.0);
System.out.println("Harvested: " + wood); // 40.0
System.out.println("Remaining: " + tree.getCurrentQuantity()); // 60.0

// 3. Time passes, resource regenerates
tree.regenerate(2.0); // 2 seconds
// Result: 60 + 5*2*(1-60/100) = 60 + 4 = 64.0
System.out.println("After regen: " + tree.getCurrentQuantity()); // 64.0
```

### Finite Resource Pattern

```java
// Ore vein with zero regeneration
ResourceNode ore = new ResourceNode(1, ResourceType.ORE, 200, 200, 100.0, 0.0);

// Harvest depletes permanently
ore.harvest(50.0);
ore.regenerate(100.0); // No effect (regenRate = 0)

if (ore.isDepleted()) {
    System.out.println("Ore vein exhausted");
}
```

### Harvest-Regen Simulation

```java
ResourceNode crops = new ResourceNode(1, ResourceType.CROPS, 50, 50, 100.0, 8.0);

// Simulate 10 harvest cycles
for (int day = 0; day < 10; day++) {
    // Daily harvest
    double harvested = crops.harvest(20.0);
    System.out.println("Day " + day + ": harvested " + harvested);
    
    // Overnight regeneration (8 hours = 28800 seconds)
    crops.regenerate(28800.0);
    System.out.println("Day " + day + ": regrew to " + crops.getCurrentQuantity());
}
```

---

## Mathematical Analysis

### Regeneration Formula Derivation

From `docs/economy_resources.md`, the regeneration formula is:

```
R(t+Δt) = R(t) + regenRate * Δt * (1 - R(t)/Rmax)
```

**Intuition:**
- When `R = 0`: regeneration = `regenRate * Δt` (maximum rate)
- When `R = Rmax/2`: regeneration = `regenRate * Δt * 0.5` (half rate)
- When `R = Rmax`: regeneration = 0 (no growth)

**Continuous Form (differential equation):**
```
dR/dt = regenRate * (1 - R/Rmax)
```

**Solution:**
```
R(t) = Rmax * (1 - e^(-regenRate * t / Rmax))
```

**Properties:**
- Exponential approach to `Rmax`
- Half-life: `t_half = (Rmax / regenRate) * ln(2)`
- Never exceeds `Rmax`

### Example: Wood Regeneration Timeline

```java
ResourceNode tree = new ResourceNode(1, ResourceType.WOOD, 0, 0, 100.0, 10.0);
tree.setCurrentQuantity(0.0); // Fully depleted
```

| Time (s) | Quantity | Regen Rate |
|----------|----------|------------|
| 0 | 0.0 | 10.0 |
| 1 | 10.0 | 9.0 |
| 2 | 19.0 | 8.1 |
| 5 | 40.95 | 5.91 |
| 10 | 65.13 | 3.49 |
| 20 | 86.47 | 1.35 |
| 50 | 99.33 | 0.07 |

**Observation:** 90% capacity reached in ~23 seconds; full capacity asymptotically

---

## Design Rationale

### Why Capped Regeneration?

**Problem:** Linear regeneration (`R += regenRate * Δt`) leads to infinite resources  
**Solution:** Multiply by `(1 - R/Rmax)` factor to cap at `Rmax`

**Alternative Considered:** Step function (regen until max, then stop)  
**Rejected:** Unrealistic (trees don't grow instantly to full size)

### Why Different Resource Types?

**Game Balance:** 
- **Renewable resources** (wood, crops) support continuous gameplay
- **Finite resources** (ore, stone) create strategic scarcity
- **Variable regen rates** create resource hierarchy

**Realism:**
- Trees regrow, ore deposits don't
- Different ecosystems have different resource abundance

---

## Integration with Region

ResourceNodes are managed by `Region` objects:

```java
Region region = new Region(1, 256, 256, 64, 64);

// Add resource nodes
ResourceNode wood = new ResourceNode(1, ResourceType.WOOD, 240, 240, 100.0, 5.0);
region.addResourceNode(wood);

// Region handles batch regeneration
region.regenerateResources(currentTick, deltaTime);
```

See `Region.md` for full integration details.

---

## Quality Gates & Testing

### Determinism

✅ **Same inputs → same outputs**

```java
ResourceNode n1 = new ResourceNode(1, ResourceType.WOOD, 0, 0, 100.0, 10.0);
ResourceNode n2 = new ResourceNode(1, ResourceType.WOOD, 0, 0, 100.0, 10.0);

n1.setCurrentQuantity(50.0);
n2.setCurrentQuantity(50.0);

n1.regenerate(3.0);
n2.regenerate(3.0);

assert n1.getCurrentQuantity() == n2.getCurrentQuantity();
```

### Resource Caps Enforced

✅ **Resources never exceed Rmax**

```java
ResourceNode node = new ResourceNode(1, ResourceType.WOOD, 0, 0, 100.0, 50.0);
node.regenerate(1000.0); // Massive regeneration

assert node.getCurrentQuantity() <= 100.0;
assert node.getCurrentQuantity() == 100.0; // Should cap exactly at Rmax
```

### Harvest Correctness

✅ **Harvest never exceeds available**

```java
ResourceNode node = new ResourceNode(1, ResourceType.WOOD, 0, 0, 100.0, 5.0);
node.setCurrentQuantity(20.0);

double harvested = node.harvest(50.0);

assert harvested == 20.0; // Only got what was available
assert node.getCurrentQuantity() == 0.0; // Fully depleted
```

---

## Performance Considerations

### Memory Footprint

- **Per node:** ~100 bytes (id, type, coords, rMax, regenRate, currentQuantity)
- **1000 nodes:** ~100 KB

### Computational Cost

- **regenerate():** O(1) — single arithmetic calculation
- **harvest():** O(1) — min() and subtraction
- **Typical scenario:** 10,000 nodes regenerating per tick = ~0.1ms on modern hardware

---

## Future Enhancements (Post-MVP)

### Planned Features

- **Quality tiers:** Resources have quality levels (poor, normal, exceptional)
- **Environmental effects:** Regen rate modified by weather, biome, season
- **Depletion mechanics:** Overhar vesting reduces long-term `rMax`
- **Resource veins:** Clustered nodes with shared depletion

### Potential Optimizations

- **Batch regeneration:** SIMD operations for large node arrays
- **Lazy evaluation:** Only regenerate when accessed (for background regions)
- **Hierarchical updates:** Update clusters instead of individual nodes

---

## Related Documentation

- **Region.md** — Region-level resource management
- **RegionSimulator.md** — Tick-driven regeneration orchestration
- **economy_resources.md** — Economic design and regeneration formula
- **specs_summary.md** — Canonical defaults and formulas

---

## Version History

- **v0.1.0 (Nov 2025):** Initial implementation for Phase 1.2
  - Five resource types (WOOD, ORE, CROPS, STONE, HERBS)
  - Capped regeneration formula
  - Harvest mechanics
  - Depletion/full status checks
