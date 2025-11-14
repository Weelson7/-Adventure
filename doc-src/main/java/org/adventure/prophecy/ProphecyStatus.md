# ProphecyStatus

**Package:** `org.adventure.prophecy`  
**Type:** Enum  
**Since:** Phase 1.10.1

---

## Overview

`ProphecyStatus` tracks the current state of a prophecy throughout its lifecycle.

---

## Values

### HIDDEN
- Prophecy exists but is unknown to the world
- Not displayed to players or NPCs
- Waiting for `revealTick` to be reached
- **Example:** Secret doom prophecy waiting to be discovered

### REVEALED
- Prophecy is now known to the world
- Displayed to players, mentioned in stories
- Waiting for `triggerTick` or trigger condition
- **Example:** "The volcano will erupt in 5 years" (known but not yet happening)

### IN_PROGRESS
- Prophecy is actively unfolding
- Effects are happening in real-time
- Can still be influenced by player actions
- **Example:** Volcano is rumbling, evacuation underway

### FULFILLED
- Prophecy came true as predicted
- Fulfillment effects executed
- Becomes part of world history
- **Example:** Volcano erupted, northern clans destroyed

### FAILED
- Prophecy was prevented or expired
- Failure effects executed (if any)
- Becomes a "what could have been" story
- **Example:** Player evacuated all clans, volcano erupted harmlessly

---

## State Transitions

```
HIDDEN
  ↓ revealTick reached
REVEALED
  ↓ triggerTick reached OR trigger condition met
IN_PROGRESS
  ↓ fulfillment condition met
FULFILLED
  
REVEALED
  ↓ time limit expired OR player prevented
FAILED
```

---

## Usage

```java
Prophecy prophecy = getProphecyById("doom_123");

switch (prophecy.getStatus()) {
    case HIDDEN:
        // Don't show to player
        break;
    case REVEALED:
        System.out.println("Prophecy known: " + prophecy.getTitle());
        break;
    case IN_PROGRESS:
        System.out.println("Prophecy unfolding: " + prophecy.getTitle());
        break;
    case FULFILLED:
        System.out.println("Prophecy fulfilled: " + prophecy.getTitle());
        break;
    case FAILED:
        System.out.println("Prophecy failed: " + prophecy.getTitle());
        break;
}
```

---

## Related Classes

- `Prophecy` — Entity using this status
- `ProphecyType` — Category of prophecy
- `ProphecyGenerator` — Creates prophecies with HIDDEN status
