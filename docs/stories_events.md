# Stories & Events System

## Overview
Lore, story creation, and event mechanics in the world.

## Core Concepts
- Stories proportional to world size, geographically distributed
- Events triggered by conditions (e.g., dragonslayer)
- Local impact, spread to NPCs

## Data Structures & Relationships
- Story: Linked to objects, places, characters
- Event: Trigger, effect, spread

## Generation & Initialization
- Stories created at worldgen, assigned to characters/storybooks
- Events triggered by gameplay actions

## Interactions & Edge Cases
- Story/event propagation
- Edge cases: multiple events, story overlap, event impact

## Expansion & Modularity
- Add new story types, event triggers
- Modular story/event system

## Open Questions
## Open Questions

- Saturation tuning: what are recommended default numeric caps per-region and per-event-type (e.g., 50 active stories per region)? How should caps scale with region population.
- Archive/retention policy: how long should resolved/archived stories remain in persistent storage before pruning? Should certain legendary stories be retained permanently?
- Player-driven story creation: allow full player-created stories/events or restrict to triggers that create story meta-events only? Define moderation/audit paths for player-created content.
- Cross-region propagation constraints: should long-distance propagation be limited by trade routes only, or also by magical channels? Define connectionFactor sources.

Log unresolved items in `docs/open_questions.md` for prioritization and owner assignment.
## Design Decisions

For canonical design decisions, see **`docs/design_decisions.md`**. Key decisions for stories/events:

- Players do not directly create stories, but actions can trigger them (e.g., killing a powerful sorcerer may create a lich event).
- Story/event saturation managed by decreasing probability as story/event population increases (see saturation controls section above).
- Event propagation uses deterministic seeded algorithms with decay formulas.

Refer to `docs/design_decisions.md` for cross-cutting event and story policies.

## Story Types & Structure
- Legends, rumors, quests, prophecies, tragedies, comedies, mysteries
- Stories can have branching paths, multiple resolutions, and hidden outcomes
- Story arcs span multiple regions, NPCs, and events

## Event System Details
- Event categories: world (global), regional, personal (NPC/player), random, triggered
- Event chaining: one event can trigger others, escalate, or resolve
- Cooldowns and escalation mechanics to manage event frequency

## Story/Event Propagation
- Stories/events spread via NPC gossip, books, songs, and player actions
- Rumor and misinformation mechanics: false stories can circulate and affect gameplay
- Propagation speed and reach depend on region connectivity, NPC relationships, and player involvement
## Story/Event Propagation
- Stories/events spread via NPC gossip, books, songs, and player actions
- Rumor and misinformation mechanics: false stories can circulate and affect gameplay
- Propagation speed and reach depend on region connectivity, NPC relationships, and player involvement

### Deterministic propagation model (recommended)

Propagation must be deterministic given a seed and event parameters to allow reproducible testing and replay. Use a seeded RNG per-region and per-event when randomization is needed.

Fields to store per Event/Story:
- id, originTileId, originTick, baseProbability, hopCount, maxHops, saturationScore, lastProcessedTick

Suggested propagation algorithm (graph-based BFS with decaying probability):

1. Initialize queue with origin node: (tileId, probability = baseProbability, hops = 0).
2. While queue not empty and hops < maxHops:
	 - Pop next node (FIFO or priority by probability).
	 - For each neighbor (connected region/tile or NPC contact) compute p' = probability * decay(hops+1) * connectionFactor * (1 - saturation/localLoad).
	 - If p' > threshold, schedule event for that neighbor and enqueue (neighbor, p', hops+1).

Where decay(h) can be one of:
- exponential: decay(h) = exp(-k * h) (k>0) — strong falloff with distance
- linear: decay(h) = max(0, 1 - k*h) — gentler falloff

ConnectionFactor is based on connectivity strength (e.g., trade route strength, NPC relationships) in [0,1].

Saturation control: maintain per-region/event-type counters. Effective probability reduces as saturation approaches cap:

	effectiveP = p' * max(0, 1 - saturation/currentCap)

Example numeric parameters (tunable):
- baseProbability = 0.9
- maxHops = 6
- k (exponential decay) = 0.8
- saturation cap = 50 concurrent stories of that type per region

Edge cases & fallback:
- Closed networks: if no neighbors reachable, mark event as contained; could convert to an archival story (lake/monument) or spawn local legacy features.
- Conflicting events: events with mutually exclusive outcomes are resolved by priority field (higher priority wins) or by deterministic tiebreak based on seed+eventId.

Persistence & replay:
- Store minimal propagation state (lastProcessedTick, saturation counters) so the algorithm can resume deterministically after interruptions.

Testing guidance:
- Unit test propagation on small graphs with seeded RNGs. Add regression tests to assert that propagation paths are identical for the same seed and parameters.

## Impact & Consequences
- Stories/events affect world state, NPC behavior, player choices, and available quests
- Long-term consequences: legacy effects, changes in loyalty, new crises, altered biomes
- Resolution can unlock rewards, trigger new events, or permanently change the world

## Player Interaction
- Players discover stories/events through exploration, interaction, and investigation
- Player actions can influence, resolve, or escalate stories/events
- Special mechanics for player-driven event triggers and story progression

## Story Saturation & Management
- Maximum number of active stories/events per region/NPC to prevent overload
- Pruning, merging, or archiving old stories/events to maintain relevance
- Story/event probability decreases as saturation increases

### Event Saturation Controls (concrete decay functions and caps)

**Per-Region Caps:**
- Default: max 50 active stories per region, max 20 active events per region (tunable per world size/parameters).
- Exceeding soft cap (80% of max) triggers probability reduction for new events.

**Saturation Decay Function:**
- Effective probability for new story/event:
  ```
  effectiveP = baseP * max(0, 1 - (currentCount / maxCap))
  ```
  - Example: baseP=0.9, currentCount=40, maxCap=50 → effectiveP = 0.9 * (1 - 0.8) = 0.18
  - When currentCount ≥ maxCap, effectiveP = 0 (no new stories spawn until old ones resolve/archive).

**Pruning & Archival:**
- Stories with status=`resolved` are archived after 1000 ticks (configurable).
- Archived stories stored in compressed form (title, summary, key outcomes) for historical reference.
- Legendary stories (flagged) are never pruned; count against cap but persist indefinitely.

**Merging:**
- Similar or overlapping stories can merge (e.g., two "bandit raid" stories in same region combine into one larger event).
- Merge conditions: same region, same event type, within N ticks of each other (default 100).

**Priority System:**
- Events have `priority` field (0-10). High-priority events (e.g., major crises, player-triggered) bypass saturation caps.
- Low-priority background events are pruned first when cap is reached.

Notes:
- Link to `docs/design_decisions.md` for canonical caps and decay parameters.
- See `docs/open_questions.md` for tuning questions (optimal caps for different world sizes, player counts).


## Generation & Customization
- Procedural algorithms for story/event generation based on world state, NPCs, and player actions
- Support for custom or modded stories/events via config files or scripting
- Dynamic story chaining and event escalation for emergent gameplay

## Edge Case Handling
- Overlapping or conflicting stories/events resolved by priority, merging, or player choice
- Event failure, interruption, or abandonment tracked and can trigger new stories/events