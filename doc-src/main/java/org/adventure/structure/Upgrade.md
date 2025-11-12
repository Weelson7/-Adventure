# Upgrade

**Package:** `org.adventure.structure`  
**Type:** Immutable Value Class  
**Schema Version:** 1

---

## Overview

`Upgrade` represents an improvement applied to a structure. Upgrades can increase capacity, provide bonuses, unlock new features, or enhance existing functionality. Once applied, upgrades are permanent (unless structure is destroyed).

---

## Class Structure

```java
public final class Upgrade {
    private final String id;
    private final String name;
    private final String description;
    private final Map<String, Double> costs;
    private final Map<String, Object> effects;
    private final int timeRequiredTicks;
    private final int appliedAtTick;
    private final int schemaVersion;
}
```

---

## Builder Pattern

### Construction
```java
Upgrade upgrade = new Upgrade.Builder()
    .id("upgrade-001")
    .name("Reinforced Walls")
    .description("Increases structure max health by 50")
    .cost("stone", 100.0)
    .cost("iron", 20.0)
    .effect("maxHealthBonus", 50)
    .effect("defenseBonus", 5)
    .timeRequiredTicks(86400)  // 1 day
    .appliedAtTick(1000)
    .build();
```

### Builder Methods
- **`id(String)`**: Unique upgrade identifier (required)
- **`name(String)`**: Display name (required)
- **`description(String)`**: Detailed description (required)
- **`costs(Map<String, Double>)`**: Bulk cost assignment
- **`cost(String, Double)`**: Add individual cost
- **`effects(Map<String, Object>)`**: Bulk effect assignment
- **`effect(String, Object)`**: Add individual effect
- **`timeRequiredTicks(int)`**: Time to complete upgrade (default: 0)
- **`appliedAtTick(int)`**: When upgrade was applied (default: 0)
- **`schemaVersion(int)`**: Schema version (default: 1)

---

## Costs System

Upgrades require resources to apply, stored as `Map<String, Double>`:

### Common Cost Keys
- **stone**: Stone units
- **wood**: Wood units
- **iron**: Iron units
- **gold**: Gold currency
- **mana**: Mana cost (for magical upgrades)

### Example
```java
Upgrade blacksmithForge = new Upgrade.Builder()
    .name("Blacksmith Forge")
    .cost("stone", 50.0)
    .cost("iron", 30.0)
    .cost("wood", 20.0)
    .cost("gold", 500.0)
    .build();
```

---

## Effects System

Upgrades provide various effects, stored as `Map<String, Object>`:

### Common Effect Keys

#### Numeric Bonuses
- **maxHealthBonus**: Increase structure max health
- **defenseBonus**: Percentage defense increase
- **capacityBonus**: Increase storage/room capacity
- **proficiencyBonus**: Crafting proficiency increase
- **xpBonus**: XP gain multiplier
- **manaRegenBonus**: Mana regeneration increase

#### Boolean Flags
- **unlockCrafting**: Enable crafting category
- **enableTeleport**: Add teleport pad
- **enableStorage**: Add storage capacity

#### String Values
- **unlockedFeature**: Name of feature unlocked
- **upgradeType**: Category of upgrade

### Example
```java
Upgrade wizardTowerEnhancement = new Upgrade.Builder()
    .name("Arcane Amplifier")
    .effect("manaRegenBonus", 20)      // +20 mana/hour
    .effect("spellPowerBonus", 10)     // +10% spell power
    .effect("unlockCrafting", true)    // Unlock enchanting
    .effect("unlockedFeature", "Enchanting")
    .build();
```

---

## Time Requirements

Upgrades can take time to complete (construction period):

```java
Upgrade largeUpgrade = new Upgrade.Builder()
    .name("Castle Expansion")
    .timeRequiredTicks(7 * 86400)  // 7 days
    .appliedAtTick(currentTick)
    .build();

// Check if upgrade is complete
int completionTick = upgrade.getAppliedAtTick() + upgrade.getTimeRequiredTicks();
if (currentTick >= completionTick) {
    // Upgrade effects are now active
}
```

---

## Validation Rules

### At Construction
- `id` cannot be null or empty
- `name` cannot be null or empty
- `description` cannot be null or empty
- `timeRequiredTicks` cannot be negative
- `appliedAtTick` cannot be negative

---

## Equality and Hashing

- **Equality**: Based on `id` and `name`
- **Hash Code**: Based on `id` and `name`

---

## Persistence

### JSON Schema (v1)
```json
{
  "id": "upgrade-001",
  "name": "Reinforced Walls",
  "description": "Increases structure max health by 50",
  "costs": {
    "stone": 100.0,
    "iron": 20.0
  },
  "effects": {
    "maxHealthBonus": 50,
    "defenseBonus": 5
  },
  "timeRequiredTicks": 86400,
  "appliedAtTick": 1000,
  "schemaVersion": 1
}
```

---

## Usage Examples

### Apply Upgrade to Structure
```java
Upgrade upgrade = new Upgrade.Builder()
    .id("upgrade-reinforced-walls")
    .name("Reinforced Walls")
    .description("Increases max health by 50")
    .cost("stone", 100.0)
    .effect("maxHealthBonus", 50)
    .timeRequiredTicks(86400)
    .appliedAtTick(currentTick)
    .build();

structure.applyUpgrade(upgrade, currentTick);
```

### Check Upgrade Status
```java
List<Upgrade> upgrades = structure.getUpgrades();

for (Upgrade upgrade : upgrades) {
    int completionTick = upgrade.getAppliedAtTick() + upgrade.getTimeRequiredTicks();
    if (currentTick >= completionTick) {
        System.out.println(upgrade.getName() + " is complete!");
    } else {
        int remaining = completionTick - currentTick;
        System.out.println(upgrade.getName() + " completes in " + remaining + " ticks");
    }
}
```

---

## Upgrade Categories (Examples)

### Defensive Upgrades
```java
new Upgrade.Builder()
    .name("Stone Fortification")
    .cost("stone", 200.0)
    .effect("maxHealthBonus", 100)
    .effect("defenseBonus", 15)
    .build();
```

### Crafting Upgrades
```java
new Upgrade.Builder()
    .name("Master Forge")
    .cost("iron", 50.0)
    .cost("gold", 1000.0)
    .effect("proficiencyBonus", 20)
    .effect("unlockCrafting", true)
    .effect("unlockedFeature", "Legendary Weapons")
    .build();
```

### Magical Upgrades
```java
new Upgrade.Builder()
    .name("Ley Line Connection")
    .cost("mana", 500.0)
    .cost("gold", 2000.0)
    .effect("manaRegenBonus", 50)
    .effect("spellPowerBonus", 25)
    .effect("enableTeleport", true)
    .build();
```

---

## Design Decisions

1. **Why immutable?**: Upgrades don't change after application; replace entire upgrade to modify.

2. **Why maps for costs/effects?**: Flexible schema allows any resource type or effect without code changes.

3. **Why timeRequiredTicks?**: Simulates construction time; prevents instant upgrades.

4. **Why appliedAtTick?**: Tracks when upgrade started for completion calculation.

---

## Future Enhancements

1. **Prerequisites**: Require other upgrades before applying
   ```java
   private final List<String> prerequisiteUpgradeIds;
   ```

2. **Maintenance Costs**: Periodic costs to maintain upgrade
   ```java
   private final Map<String, Double> maintenanceCosts;
   private final int maintenanceCadenceTicks;
   ```

3. **Upgrade Levels**: Sequential levels of same upgrade
   ```java
   private final int level;  // Reinforced Walls I, II, III
   ```

4. **Skill Requirements**: Require certain skills to apply
   ```java
   private final Map<String, Integer> requiredSkills;
   ```

---

## Related Classes

- **Structure**: Contains upgrades
- **Room**: Upgrades can affect room properties
- **CraftingSystem**: Upgrades can unlock crafting categories

---

## Testing

**Test Coverage**: Included in `StructureTest.java`
- `testApplyUpgrade()`: Validates upgrade application
- `testApplyUpgradeRejectsNull()`: Validates null rejection

---

## References

- Design: `docs/structures_ownership.md` → Upgrade System
- Summary: `archive/PHASE_1.5_SUMMARY.md`
