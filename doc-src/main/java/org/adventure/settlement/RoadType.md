# RoadType

**Package:** `org.adventure.settlement`  
**Type:** Enum  
**Phase:** 1.10.2  
**Status:** ✅ Complete

## Overview

Types of roads that can be built/upgraded. Roads automatically progress from DIRT → STONE → PAVED as traffic increases.

## Values

### DIRT
- **Default Type:** All auto-generated roads start as DIRT
- **Traffic Requirement:** 0-49 (upgrades at 50+)
- **Movement Speed:** 1.0× (base speed)
- **Description:** Basic dirt path created by foot traffic

### STONE
- **Upgrade From:** DIRT at 50+ traffic
- **Traffic Requirement:** 50-79 (upgrades at 80+)
- **Movement Speed:** 1.5× (50% faster than DIRT)
- **Description:** Stone-paved road, more durable

### PAVED
- **Upgrade From:** STONE at 80+ traffic
- **Traffic Requirement:** N/A (maximum tier)
- **Movement Speed:** 2.0× (100% faster than DIRT)
- **Description:** Fully paved road, highest quality

## Upgrade Flow

```
DIRT (traffic: 0-49)
  ↓ (traffic >= 50)
STONE (traffic: 50-79)
  ↓ (traffic >= 80)
PAVED (traffic: 80-100)
  (no further upgrades)
```

## Usage

```java
// Create DIRT road (default)
RoadTile road = new RoadTile.Builder()
    .position(10, 20)
    .type(RoadType.DIRT)
    .build();

// Simulate traffic
road.incrementTraffic(50);

// Try upgrade
if (road.tryUpgrade()) {
    System.out.println("Upgraded to " + road.getType());  // STONE
}

// More traffic
road.incrementTraffic(30);  // Now at 80

if (road.tryUpgrade()) {
    System.out.println("Upgraded to " + road.getType());  // PAVED
}

// No more upgrades
if (!road.tryUpgrade()) {
    System.out.println("Road is at maximum tier");
}
```

## Movement Speed

```java
public double getSpeedMultiplier(RoadType type) {
    switch (type) {
        case DIRT: return 1.0;
        case STONE: return 1.5;
        case PAVED: return 2.0;
    }
}
```

## Related Classes

- `RoadTile` — Uses RoadType for type classification
- `RoadGenerator` — Creates roads (default DIRT)

## References

- **Design Doc:** `BUILD_PHASE1.10.x.md` → Phase 1.10.2
