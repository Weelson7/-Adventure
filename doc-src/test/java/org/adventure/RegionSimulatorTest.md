# RegionSimulatorTest.java - Region Simulation Engine Test Suite

**Package:** `org.adventure`
**Source:** [RegionSimulatorTest.java](../../../../src/test/java/org/adventure/RegionSimulatorTest.java)
**Phase:** 1.2 (Region Simulation)
**Test Framework:** JUnit 5.9.3

## Overview

`RegionSimulatorTest` validates the tick-driven simulation engine for regions, including configuration, region management, activation/deactivation, tick advancement, resource regeneration, state resynchronization, counters, determinism, and resource caps. These tests ensure the simulation engine meets all design requirements for deterministic, scalable, and robust region processing.

## Test Coverage Summary

| Category                   | Tests | Purpose |
|----------------------------|-------|---------|
| Defaults/Configuration     | 2     | Default and custom config |
| Region Management          | 2     | Add/get regions, get all |
| Activation/Deactivation    | 2     | State transitions |
| Tick Advancement           | 2     | Tick counter, advanceTicks |
| Active/Background Updates  | 2     | Resource regeneration in both states |
| Resynchronization Logic    | 1     | State sync on activation |
| Region Counters            | 1     | Active/background counts |
| Determinism                | 1     | Same inputs → same outputs |
| Upgrade/Downgrade Cycle    | 1     | State transitions, tick preservation |
| Multi-Region Processing    | 1     | Simultaneous region updates |
| Resource Caps              | 1     | Capped resource enforcement |

**Total: 16 tests, 100% passing**

## Individual Test Descriptions

### 1-2. Defaults/Configuration
- **Purpose:** Validate default and custom engine configuration

### 3-4. Region Management
- **Purpose:** Validate region addition, retrieval, and listing

### 5-6. Activation/Deactivation
- **Purpose:** Validate region state transitions

### 7-8. Tick Advancement
- **Purpose:** Validate tick counter and advanceTicks logic

### 9-10. Active/Background Updates
- **Purpose:** Validate resource regeneration in both simulation states

### 11. Resynchronization Logic
- **Purpose:** Validate deterministic state sync on activation

### 12. Region Counters
- **Purpose:** Validate active/background region counting

### 13. Determinism
- **Purpose:** Validate tick determinism (same inputs → same outputs)

### 14. Upgrade/Downgrade Cycle
- **Purpose:** Validate state transitions and tick preservation

### 15. Multi-Region Processing
- **Purpose:** Validate simultaneous updates for multiple regions

### 16. Resource Caps
- **Purpose:** Validate capped resource enforcement

## Quality Gates & Design Alignment
- **Determinism:** Tick-driven updates, same initial state → same results
- **Resource Caps:** Regeneration never exceeds Rmax
- **Performance:** Active/background multipliers, scalable tick engine
- **State Preservation:** Last processed tick, resynchronization logic

## Test Execution

```powershell
# Run all RegionSimulatorTest tests
maven\mvn\bin\mvn.cmd test -Dtest=RegionSimulatorTest
```

**Expected Output:**
```
[INFO] Running org.adventure.RegionSimulatorTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: <0.1 s
[INFO] BUILD SUCCESS
```

## References
- [RegionSimulator.java](../../main/java/org/adventure/region/RegionSimulator.md) - Implementation docs
- [docs/game_parameters_setup.md](../../../../docs/game_parameters_setup.md) - Simulation parameters
- [docs/design_decisions.md](../../../../docs/design_decisions.md) - Engine design

---
**Status:** ✅ All 16 tests passing
**Coverage:** 100% of RegionSimulator.java public API
**Last Updated:** 2025-11-11
