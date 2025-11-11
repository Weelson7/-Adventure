# Structures & Ownership

## Overview
Physical buildings, rooms, and ownership mechanics in the world.

## Core Concepts
- Structures attached to regional subgrid, contain rooms
- Upgrades possible; expansion requires destruction
- No decay, but damage possible; ownership tax
- Individual ownership, family assets via marriage
- Legacy effects (haunting, legendary status)

## Data Structures & Relationships
- Structure: Type, owner, rooms, upgrades
- Room: Size, contents
- Ownership: Individual, family

## Generation & Initialization
- Structures placed at worldgen or by player/NPC action
- Ownership assigned by conquest, succession, trade/duel

## Interactions & Edge Cases
- Damage, repair, upgrade mechanics
- Ownership transfer rules
- Edge cases: contested ownership, inheritance, taxation

## Expansion & Modularity
- Add new structure types, upgrade paths
- Modular ownership system

## Structure Types & Hierarchies
- Residential: houses, manors, apartments, castles
- Military: barracks, fortresses, watchtowers, armories
- Commercial: shops, markets, warehouses, inns
- Magical: wizard towers, enchanted libraries, ritual chambers
- Ruins & Dungeons: ancient ruins, crypts, labyrinths, submerged cities
- Special: temples, guild halls, training centers, legendary structures
- Hierarchies allow for upgrade paths, specialization, and unique effects

## Room System
- Room categories: living quarters, storage, training, crafting, magical chambers, treasury
- Room effects: stat bonuses, event triggers, NPC spawning, resource generation
- Room upgrades: increase size, add features, improve effects

## Upgrade & Expansion Mechanics
- Upgrades require resources, time, and sometimes special conditions
- Expansion limited by tile size, destruction of previous structures, and local laws
- Upgrades affect structure stats, functions, and legacy potential

## Damage, Repair & Decay
- Damage sources: combat, disasters, magical events, neglect
- Repair mechanics: required materials, skill checks, time investment
- Legendary/magical structures may require special rituals or rare items for repair
- No natural decay, but damage accumulates if not repaired

## Ownership & Succession
- Succession: inheritance (family/clan), merit, conquest, legal transfer
- Ownership transfer: conquest, trade, duel, sale, legal means
- Contested ownership: resolved by negotiation, duel, or event triggers
- Disputes can lead to crises, story events, or legal battles

### Ownership model (concrete)

- Ownership is single-owner by default. Owner types: Character, Clan, Society. Structures store `ownerId` and `ownerType`.
- Access is separate from ownership: each Structure has a `permissions` table mapping AccessRole -> AccessLevel. Example roles: owner, clan_member, ally, public. AccessLevels: none, read, use, modify, manage.

### Permission semantics

- owner: full control (transfer, change permissions, manage taxation)
- clan_member: access based on structure's `clanAccessLevel` (e.g., use/modify)
- ally: limited access configured per-structure
- public: read/use only if allowed

### Ownership transfer & inheritance (deterministic order)

1. Voluntary transfer: owner calls transferOwnership(targetId). Transfers are logged into audit with tick and require payment/fee if configured.
2. Sale/Trade: exchange of assets via `createSaleOffer()` -> `acceptOffer()` sequence. Settlement occurs atomically; if settlement fails, whole transaction rolls back.
3. Succession (on owner death/absence): inheritance rules applied in order: legal heirs (explicit will) -> primary family/clan heir -> designated successor -> government claim (if any). Ties are resolved by claim strength (military/reputation) and, if equal, by randomized deterministic tiebreaker seeded by world seed.
4. Conquest: ownership change via conflict resolution module; applies immediately but creates contested flag for N ticks where disputes can be raised and reversed by legal/duel actions.

### Contested Ownership & Rollback

- Contested structures are flagged; certain operations (e.g., demolish) are disabled until dispute resolution.
- Rollbacks: structure transfer events are stored as idempotent events. To roll back, apply inverse event(s) until desired state restored. For severe corruption, operator-driven rollback using backups is required.

### Taxation & Failure Consequences (concrete rules)

- Tax cadence: configurable; default: every 7 in-game days (configurable in `game_parameters_setup.md`).
- Tax formula: taxCollected = floor(taxableIncome * taxRate) where taxableIncome is computed from structure-generated income (rent, production).
- Failure consequences: unpaid taxes for `gracePeriod` (configurable) will result in (in order): warning -> fines -> asset lien (restricted actions) -> seizure (transfer to governing authority) after `seizureThreshold` ticks.

### Conflict resolution patterns

- Use authoritative server arbitration for simultaneous conflicting transfers: transactions are ordered by tick and by a monotonic operation ID. If conflicting operations arrive in same tick, deterministic tie-break by actor ID hash.
- Consider optimistic concurrency for player-initiated transfers (attempt -> validate -> commit) with clear failure messages.

### Audit & Logging

- All ownership and taxation events are written to append-only audit logs with actorId, targetId, tick, and checksum. Admin interfaces exist to inspect and replay events for debugging.

See `docs/design_decisions.md` for canonical rules, and `docs/persistence_versioning.md` for migration and schema details.

## Taxation & Asset Management
- Taxation: regular payments to rulers, affects stability and asset retention
- Asset management: resource allocation, upgrades, maintenance
- Consequences: unpaid taxes lead to asset seizure, instability, or rebellion

## Legacy Effects & Story Integration
- Legacy effects: haunting, legendary status, magical resonance, historical significance
- Triggered by events, usage, or story discovery
- Integrated with story/event system for quests, bonuses, or curses

## Edge Case Handling
- Destroyed structures: leave ruins, trigger events, or allow rebuilding
- Abandoned structures: can be claimed, looted, or become quest locations
- Duplicated structures: unique structures flagged to prevent exploits
- Special rules for story-linked or legendary structures

## Modularity & Extensibility
- Guidelines for adding new structure types, room categories, upgrade paths, and ownership rules
- Support for modding/custom structure definitions via config files or scripting

## Open Questions

- Fractional/shared ownership: current design says ownership is not fractional—confirm whether shared ownership (co-ownership, joint titles) should be supported as an access-level construct instead of ownership semantics.
- Inheritance edge-cases: clarify precedence when multiple heirs/claims have equal strength and whether auctions or duels are required as tiebreakers.
- Tax defaults and tuning: confirm default taxRate, gracePeriod, and seizureThreshold policy and whether fines escalate multiplicatively.
- Legacy persistence: should structures always record a history entry (for archeology/legacy effects) or only when tied to stories (storage vs fidelity tradeoff)?
- Dispute resolution automation: how many ticks before a contested flag auto-expires and what actions pause the contest timer?

Add unresolved items to `docs/open_questions.md` and assign owners for decisions.

## Design Decisions

For canonical design decisions, see **`docs/design_decisions.md`**. Key decisions for structures/ownership:

- Structures have unique gameplay functions (residency, training, learning, fortification, etc.).
- Ownership is not fractional or shared (single-owner model with separate access permissions).
- Taxation formulas, inheritance order, and conflict resolution rules detailed in sections above.

Refer to `docs/design_decisions.md` for authoritative cross-system policies.
