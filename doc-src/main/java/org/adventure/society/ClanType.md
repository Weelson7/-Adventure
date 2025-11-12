# ClanType Enum Documentation

**Package:** `org.adventure.society`  
**File:** `src/main/java/org/adventure/society/ClanType.java`  
**Status:** ✅ Complete (Phase 1.6)

---

## Overview

`ClanType` is a simple enumeration defining the types of social organizations in !Adventure. It provides type safety for clan categorization and supports future expansion of organization-specific behaviors.

### Enum Values
- **CLAN** — Basic social group
- **KINGDOM** — Large hierarchical organization
- **GUILD** — Professional or trade-based organization

---

## Design Rationale

### Why an Enum?
- **Type safety:** Prevents invalid organization types
- **Clarity:** Self-documenting code (`ClanType.KINGDOM` vs. `"kingdom"`)
- **Extensibility:** Easy to add new types without breaking existing code
- **Pattern matching:** Java switch expressions work well with enums

### Why These Three Types?
- **CLAN:** Fundamental unit of social organization, small to medium size
- **KINGDOM:** Large-scale political entity, multiple clans united
- **GUILD:** Specialized professional organizations, craft-focused

---

## Usage

### In Clan Construction
```java
Clan clan = new Clan.Builder()
    .id("clan-001")
    .name("Warriors of the North")
    .type(ClanType.CLAN)  // Default type
    .build();

Clan kingdom = new Clan.Builder()
    .id("kingdom-001")
    .name("Northern Empire")
    .type(ClanType.KINGDOM)
    .build();

Clan guild = new Clan.Builder()
    .id("guild-001")
    .name("Blacksmiths Guild")
    .type(ClanType.GUILD)
    .build();
```

### Default Value
The `Clan.Builder` defaults to `ClanType.CLAN` if not specified:
```java
Clan defaultClan = new Clan.Builder()
    .id("clan-002")
    .name("Default Clan")
    .build();
// Type is ClanType.CLAN
```

### Pattern Matching (Future)
```java
String description = switch (clan.getType()) {
    case CLAN -> "A group of warriors and families";
    case KINGDOM -> "A vast empire with many subjects";
    case GUILD -> "A professional organization of craftsmen";
};
```

---

## Semantic Differences

### CLAN
- **Scale:** Small to medium (10-100 members typical)
- **Structure:** Informal, family-based, loyalty-driven
- **Territory:** Limited, often a single settlement or region
- **Governance:** Elder council, chieftain, or democratic
- **Focus:** Survival, mutual aid, defense

**Example:** "The Iron Wolves" — a nomadic warrior clan

---

### KINGDOM
- **Scale:** Large (100-10,000+ members)
- **Structure:** Hierarchical, formal bureaucracy
- **Territory:** Multiple regions, cities, vassals
- **Governance:** Monarchy, empire, feudal system
- **Focus:** Expansion, taxation, military power

**Example:** "The Northern Empire" — a vast kingdom spanning many regions

---

### GUILD
- **Scale:** Variable (5-500 members)
- **Structure:** Professional hierarchy (apprentice → master)
- **Territory:** Distributed, trade routes, guild halls
- **Governance:** Guild master, council of masters
- **Focus:** Craft quality, trade monopoly, training

**Example:** "The Blacksmiths Guild" — craftsmen across multiple cities

---

## Integration with Game Systems

### With Clan Merging
```java
// When merging, the first clan's type is preserved
Clan merged = Clan.merge(kingdom, clan, "new-id", "United Kingdom", tick);
assertEquals(ClanType.KINGDOM, merged.getType()); // Preserves kingdom type
```

### With Diplomacy (Future)
Different types might have different diplomatic behaviors:
- Clans may prefer alliances with other clans (cultural affinity)
- Kingdoms may seek vassalage rather than equal alliances
- Guilds may focus on trade agreements over military pacts

### With Economy (Future)
- Clans: Resource-sharing, communal ownership
- Kingdoms: Taxation, tribute systems, centralized treasury
- Guilds: Trade bonuses, craft specializations

---

## Future Enhancements (Phase 2+)

### Additional Types (Potential)
```java
public enum ClanType {
    CLAN,
    KINGDOM,
    GUILD,
    // Future additions:
    TRIBE,           // Nomadic, small-scale
    EMPIRE,          // Multi-kingdom scale
    CONFEDERATION,   // Loose alliance of clans
    THEOCRACY,       // Religious-based government
    REPUBLIC,        // Democratic city-state
    MERCHANT_LEAGUE, // Trade-focused alliance
    MERCENARY_COMPANY // Military contract organization
}
```

### Type-Specific Behavior
```java
// Example: Type-specific treasury management
double getTaxRate(ClanType type) {
    return switch (type) {
        case CLAN -> 0.02;     // 2% (minimal)
        case KINGDOM -> 0.05;  // 5% (standard)
        case GUILD -> 0.10;    // 10% (guild dues)
    };
}
```

### Type Transitions
```java
// Clan → Kingdom promotion
if (clan.getType() == ClanType.CLAN && clan.getMemberCount() > 100) {
    Clan promoted = new Clan.Builder(clan)
        .type(ClanType.KINGDOM)
        .build();
}
```

---

## Testing

### Coverage
- Tested in `ClanTest.testGuildTypeClan()` — Verifies GUILD type
- Tested in `ClanTest.testKingdomTypeClan()` — Verifies KINGDOM type
- Default CLAN type tested in most other tests

### No Explicit Enum Tests
Simple enums typically don't require dedicated tests beyond usage verification in consuming classes.

---

## Persistence

### JSON Serialization
```json
{
  "id": "clan-001",
  "name": "Warriors",
  "type": "CLAN",
  ...
}
```

Jackson automatically serializes enums as strings. Deserialization works via `@JsonProperty` annotations in `Clan` constructor.

---

## Related Classes
- **Clan** — Primary consumer of `ClanType`
- **ClanTest** — Tests enum usage in clan construction

---

## Specification References
- `docs/societies_clans_kingdoms.md` — Clan and kingdom concepts
- `docs/data_models.md` — Clan schema with type field

---

## Version History
- **v1.0 (Phase 1.6):** Initial enum with three types (CLAN, KINGDOM, GUILD)

---

**Status:** ✅ Complete for MVP Phase 1.6  
**Extensibility:** Ready for additional types in Phase 2+  
**Usage:** Required field in `Clan` class, default CLAN
