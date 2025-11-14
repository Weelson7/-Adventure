# !Adventure — Phase 2 Build Guide (Advanced Systems)

**Version:** 0.2.0-SNAPSHOT  
**Last Updated:** November 13, 2025  
**Status:** Phase 2 — Depth & Polish (Post-MVP)

---

## Overview

This document is the **dedicated guide** for Phase 2 development of !Adventure. Phase 1 (MVP) established the foundation; Phase 2 adds **depth, complexity, and polish** to create a truly immersive experience.

### Quick Links
- [Main Build Guide](BUILD_PHASE1.md) — Phase 1 (MVP) complete reference
- [Gameplay Build Guide](BUILD-GAMEPLAY.md) — UI and player experience development
- [Grand Plan](docs/grand_plan.md) — Strategic roadmap and feature prioritization
- [Architecture Design](docs/architecture_design.md) — Technical architecture and system contracts
- [Testing Plan](docs/testing_plan.md) — Test framework, coverage goals, and determinism checks

---

## Prerequisites

### Required Completion
- ✅ **Phase 1 Complete:** All 10 sub-phases (1.1-1.10) must be 100% complete
  - World generation, region simulation, characters, items, crafting, structures, societies, stories, persistence, multiplayer, CI/CD
- ✅ **All Phase 1 Tests Passing:** 534+ tests with 70%+ coverage
- ✅ **Deployment Infrastructure Ready:** Docker, CI/CD, cross-platform scripts functional

### Required Tools (Same as Phase 1)
- **Java Development Kit (JDK):** Version 21 LTS
- **Build Tool:** Maven 3.8.9+ (bundled in `maven/mvn/bin/`)
- **Git:** For version control and CI integration
- **IDE/Editor:** IntelliJ IDEA, VS Code with Java extensions, or Eclipse

### Optional Tools (for Phase 2)
- **Performance Profiling:** JProfiler, YourKit, VisualVM for optimization
- **Load Testing:** JMeter, Gatling for multiplayer scaling tests
- **Database Tools:** For dynamic economy and advanced persistence
- **AI/ML Tools:** If implementing advanced NPC AI or procedural content generation

---

## Phase 2 Philosophy

### Goals
1. **Add Depth:** Expand core systems with advanced mechanics
2. **Enhance Polish:** Improve UX, performance, and content quality
3. **Prepare for Scale:** Optimize for larger worlds and more players
4. **Enable Modding:** Build tools and APIs for community content
5. **Balance Gameplay:** Fine-tune all systems based on playtesting data

### Resource Allocation (Recommended)
- **40%** → High-priority systems (magic, advanced diplomacy, dynamic economy)
- **30%** → Polish and optimization (performance, UX, balancing)
- **20%** → Content creation (quests, NPCs, items, stories)
- **10%** → Modding tools and community support

---

## Development Phases

### Phase 2.1: Magic System (High Priority ⭐ 0% Complete)

**Goal:** Implement a deep, flexible magic system with rune-based spellcrafting, mana management, and risk/reward mechanics.

**Deliverables:**
- [ ] **Rune System (Magical Language)**
  - [ ] **Type Runes (8 "verbs"):** Define what the spell does
    - Projectile (launch ranged attack)
    - Area (create area effect)
    - Beam (channel continuous ray)
    - Enchant (buff target)
    - Summon (create entity)
    - Shield (defensive barrier)
    - Heal (restore vitality)
    - Curse (apply debuff)
  - [ ] **Element Runes (6 base + combinations):** Define spell's essence
    - Base: Fire, Water, Earth, Air, Light, Dark
    - 2-5 Element Combos:
      - 2-Element: Fire + Earth = Lava, Fire + Wind = Lightning, Water + Wind = Ice, Earth + Water = Nature, Light + Darkness = Twilight, Null + Light = Arcane, etc. (21 combinations)
      - 3-Element: Fire + Wind + Null = Plasma Storm, Water + Earth + Light = Sacred Grove, etc.
      - 4-Element: Fire + Water + Wind + Earth = Elemental Chaos, etc.
      - 5-Element: Null + Fire + Water + Earth + Wind = Primordial Force, etc.
  - [ ] **Modifier Runes (12 "adjectives"):** Adjust spell properties (max 2 per spell)
    - Power: Amplify (+50% damage, -15 stability), Weaken (-30% damage, +20 stability)
    - Speed: Swift (+50% cast speed, -10 stability), Slow (-30% cast speed, +15 stability)
    - Range: Extend (+50% range, -10 stability), Shorten (-30% range, +10 stability)
    - Duration: Prolong (+100% duration, -10 stability), Hasten (-50% duration, +10 stability)
    - Cost: Efficient (-30% mana, +5 stability), Reckless (+50% damage, -25 stability)
    - Stability: Stabilize (+30 stability), Volatile (+30% damage, -30 stability)
  - [ ] Rune discovery mechanics (exploration, lore, achievements, mentors)
  - [ ] Rune inventory and storage system
  - [ ] Rune trading and market dynamics

- [ ] **Spell Creation System (Rune Grammar)**
  - [ ] **Spell Structure (Fixed Syntax):**
    ```
    [TYPE] + [ELEMENT_1 to 5] + [MODIFIER_1 to 2 (optional)]
    Total: 2-8 runes per spell
    
    Examples:
    Simple (2-3):
    - Projectile + Fire = Basic fireball
    - Shield + Earth + Prolong = Long-lasting stone wall
    
    Intermediate (4):
    - Projectile + Fire + Wind + Amplify = Amplified lightning bolt
    - Area + Water + Wind + Swift = Quick ice storm
    
    Advanced (5-6):
    - Area + Fire + Water + Earth + Swift = Rapid lava-steam field
    - Summon + Water + Earth + Light + Prolong + Stabilize = Sacred treant guardian
    
    Master (7-8 - EXTREMELY DANGEROUS):
    - Ritual + Null + Fire + Water + Earth + Wind + Light + Amplify = Primordial storm
    - Area + Fire + Water + Earth + Wind + Darkness + Amplify + Volatile = Apocalyptic chaos (near-certain backlash!)
    ```
  - [ ] Spell formula validation (grammar checking, rune compatibility)
  - [ ] Rune compatibility matrix (incompatible combos reduce stability)
  - [ ] Spell naming (auto-generated from runes + custom override)
  - [ ] Spell effect preview and simulation
  - [ ] Spell cost calculation based on rune complexity
  - [ ] Spell success/failure probability based on caster stats + stability
  - [ ] Spell saving and sharing (personal spellbook with formula notes)
  - [ ] Spell experimentation log (track attempted combinations)

- [ ] **Mana Management**
  - [ ] Mana pool calculation: `maxMana = baseMana + (castingStat * manaPerStat)`
    - Default: `baseMana = 10`, `manaPerStat = 2` (from specs_summary.md)
  - [ ] Mana regeneration rate based on Intelligence/Wisdom
  - [ ] Mana regeneration bonuses (meditation, potions, enchanted items)
  - [ ] Mana overcasting (spend beyond max for power boost + backlash risk)
  - [ ] Mana exhaustion penalties (debuffs when mana depleted)
  - [ ] Mana UI indicators (current/max, regen rate, exhaustion status)

- [ ] **Backlash & Risk Mechanics**
  - [ ] **Spell stability calculation:**
    ```
    baseStability = average of all rune stabilities
    
    Penalties:
    - Opposing elements (Fire + Water, Light + Darkness): -20 per pair
    - Multi-element complexity: 3 elements = -10, 4 = -25, 5 = -40
    - Complexity penalty: -5 per rune above 4 (8-rune spell = -20)
    - Manual casting (unlearned): -20 stability
    - Volatile modifier: -30 stability
    
    Bonuses:
    - Compatible elements (Fire + Wind, Water + Wind, Null + Light): +5 to +10 per pair
    - Stabilize modifier: +30 stability
    - Caster proficiency: +1 per 10 skill points (max +100 at Master)
    - Learned spell: +15 stability
    
    finalStability = clamp(baseStability + penalties + bonuses, 0, 100)
    ```
  - [ ] Backlash probability: `backlashChance = (100 - stability) / 100 * backlashMultiplier`
  - [ ] Backlash severity levels (minor fizzle → major backfire → catastrophic explosion)
  - [ ] Backlash effects by severity:
    - Minor: Spell fizzles, 50% mana refunded
    - Moderate: Spell fizzles, caster takes 15% max HP damage
    - Major: Spell backfires, caster takes 40% damage + 10s debuff
    - Catastrophic: Spell explodes, 60% damage to caster + area damage to allies
  - [ ] Backlash mitigation (skills, traits, Stabilize rune, protective gear)
  - [ ] Environmental factors (magic zones increase power +20% but reduce stability -10)

- [ ] **Spellcasting in Combat**
  - [ ] Spell targeting (self, single target, AoE, cone, line)
  - [ ] Casting time and interruption mechanics
  - [ ] Spell cooldowns and resource management
  - [ ] Counterspell and dispel mechanics
  - [ ] Combo system (chain spells for bonus effects)
  - [ ] Spell resistance and penetration stats

- [ ] **Spell Learning & Mastery**
  - [ ] **Manual vs Learned Casting:**
    - Manual (unlearned): 2x cast time, 10-30% error chance, -20 stability, full mana cost
    - Learned (mastered): Normal cast time, 0% error, +15 stability, -20% mana cost
  - [ ] **Learning Methods:**
    - Mentor teaching: NPC teaches spell instantly (gold/quests/reputation cost)
    - Spell scrolls: Consume scroll to learn permanently (rare drops, merchants)
    - Repetition: Cast same spell 50 times successfully (or 100 total attempts) → auto-learn
    - Scholarly study: Study spellbooks in libraries for 1 hour → learn spell
  - [ ] Spellbook tracking: Learned/manual status, mastery progress (23/50 casts), notes
  - [ ] Learning progress UI: Progress bar, estimated time to mastery, success rate

- [ ] **Advanced Magic Features**
  - [ ] Enchanting items with magic (permanent or temporary)
  - [ ] Ritual magic (long-duration spells, group casting)
  - [ ] Magic schools/traditions (specialization bonuses)
  - [ ] Forbidden magic (powerful but dangerous spells)
  - [ ] Magic detection and anti-magic fields

**Quality Gates:**
- ✅ **Spell Variety:** At least 10,000+ viable spell combinations (15 types × 28 elements (7 base + 21 combos) × 12 modifiers × multi-element combos)
- ✅ **Grammar Validation:** Invalid rune sequences rejected with clear error messages (max 5 elements, max 2 modifiers, etc.)
- ✅ **Balance:** No single spell dominates all situations; trade-offs matter (longer spells = more power BUT higher cost + lower stability)
- ✅ **Risk/Reward:** High-power spells (8-rune master spells) have meaningful drawbacks (4.9x mana multiplier, -40 stability from complexity)
- ✅ **Element Combos Work:** All 21+ element combinations functional and distinct (2-5 element combos)
- ✅ **Learning System:** Manual vs Learned casting functional with distinct mechanics (2x cast time, error chance, stability penalties/bonuses)
- ✅ **Determinism:** Same rune formula + same conditions = same results (for testing)
- ✅ **Performance:** Spell creation UI responds in <100ms
- ✅ **Coverage:** 85%+ test coverage for magic module (critical system)

**Commands:**
```bash
# Run magic system tests
.\maven\mvn\bin\mvn.cmd test -Dtest=MagicSystemTest,RuneTest,SpellTest

# Test spell creation and validation
.\maven\mvn\bin\mvn.cmd test -Dtest=SpellCreationTest

# Test mana management and backlash
.\maven\mvn\bin\mvn.cmd test -Dtest=ManaSystemTest,BacklashTest

# Launch spell editor tool
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.SpellEditor

# Benchmark spell performance
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.SpellBenchmark
```

**Test Coverage:**
- Unit tests for rune grammar validation (valid/invalid formulas, 2-8 rune spells)
- Unit tests for element combination logic (Fire+Wind=Lightning, Fire+Wind+Null=Plasma Storm, etc.)
- Unit tests for modifier stacking (Amplify + Swift effects, max 2 modifiers)
- Unit tests for stability calculation (opposing elements, complexity penalties for 3-5 elements, manual casting penalty)
- Unit tests for mana cost calculation (exponential complexity scaling: 8-rune spell = 4.9x multiplier)
- Unit tests for mana regeneration and overcasting
- Unit tests for backlash probability and severity levels (4 tiers)
- Unit tests for manual vs learned casting (2x cast time, error chance, stability bonuses)
- Unit tests for learning methods (mentor, scrolls, repetition 50 casts, scholarly study)
- Integration tests for spellcasting in combat (spell effects, targeting, interruption)
- Determinism tests (same rune formula = same spell always)
- Balance tests (no overpowered rune combinations, all trade-offs work, 8-rune spells extremely risky)
- Performance tests (spell parsing, creation, casting, rendering)

**References:**
- Design: Create new `docs/magic_system.md` with detailed mechanics
- Specs: `docs/specs_summary.md` → Mana Defaults, Magic & Stats
- Architecture: `docs/architecture_design.md` → Object & Crafting System
- Existing: Phase 1.3 character stats (casting stat, mana pool)

---

### Phase 2.2: Advanced Diplomacy (High Priority ⭐ 0% Complete)

**Goal:** Expand diplomacy beyond simple reputation to include secret agendas, crises, influence networks, and emergent political dynamics.

**Deliverables:**
- [ ] **Secret Relationships & Agendas**
  - [ ] Hidden relationship scores (not visible to players initially)
  - [ ] Secret alliances and betrayals
  - [ ] Espionage system (spies reveal secrets)
  - [ ] Blackmail and leverage mechanics
  - [ ] Secret society memberships (hidden factions)
  - [ ] Trust vs public reputation (can differ)

- [ ] **Crisis System**
  - [ ] Crisis types (war, famine, plague, succession, rebellion)
  - [ ] Crisis triggers (relationship thresholds, events, player actions)
  - [ ] Crisis escalation stages (tension → conflict → resolution)
  - [ ] Multi-faction crisis involvement
  - [ ] Crisis resolution mechanics (diplomacy, military, economic)
  - [ ] Crisis consequences (territory changes, reputation shifts, casualties)

- [ ] **Influence Network**
  - [ ] Influence points system (earn via quests, trade, diplomacy)
  - [ ] Influence spending (persuade NPCs, sway votes, bribe officials)
  - [ ] Influence decay over time (requires maintenance)
  - [ ] Influence visualization (who influences whom)
  - [ ] Competing influence (clans fight for control)
  - [ ] Influence thresholds for major decisions

- [ ] **Advanced Diplomacy Actions**
  - [ ] Formal treaties (trade agreements, non-aggression pacts, mutual defense)
  - [ ] Treaty enforcement and violation penalties
  - [ ] Diplomatic missions (send envoys, negotiate)
  - [ ] Gifts and tribute (improve relations)
  - [ ] Sanctions and embargoes (punish enemies)
  - [ ] Diplomatic incidents (accidental insults, border disputes)

- [ ] **Faction Dynamics**
  - [ ] Faction power levels (military, economic, cultural)
  - [ ] Faction goals and priorities (expansion, trade, isolation)
  - [ ] Faction AI decision-making (when to ally, attack, trade)
  - [ ] Faction succession (leaders change, policies shift)
  - [ ] Player-created factions (splinter groups, new clans)

**Quality Gates:**
- ✅ **Emergent Stories:** Crises and conflicts arise organically from relationships
- ✅ **Player Agency:** Players can meaningfully influence diplomacy outcomes
- ✅ **AI Quality:** Faction AI makes sensible decisions 80%+ of the time
- ✅ **No Exploits:** No easy diplomatic exploits (e.g., infinite influence farming)
- ✅ **Coverage:** 85%+ test coverage for diplomacy module

**Commands:**
```bash
# Run diplomacy tests
.\maven\mvn\bin\mvn.cmd test -Dtest=AdvancedDiplomacyTest,CrisisTest,InfluenceTest

# Test secret relationships
.\maven\mvn\bin\mvn.cmd test -Dtest=SecretRelationshipTest

# Test crisis system
.\maven\mvn\bin\mvn.cmd test -Dtest=CrisisSystemTest

# Simulate faction dynamics
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.FactionSimulator --ticks 10000

# Launch diplomacy analyzer
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.DiplomacyAnalyzer
```

**References:**
- Design: Expand `docs/societies_clans_kingdoms.md` with Phase 2 mechanics
- Existing: Phase 1.6 basic diplomacy (reputation, influence, decay)
- Architecture: `docs/architecture_design.md` → Society Engine

---

### Phase 2.3: Crafting Proficiency Progression (High Priority ⭐ 0% Complete)

**Goal:** Transform crafting from basic recipes to a deep progression system with specializations, mastery bonuses, and unique crafted items.

**Deliverables:**
- [ ] **Proficiency XP System**
  - [ ] XP gain formula: `xp = baseXP * difficultyMultiplier * qualityBonus`
  - [ ] Proficiency tiers (from Phase 1.4):
    - Novice (0-99 XP)
    - Apprentice (100-299 XP)
    - Journeyman (300-599 XP)
    - Expert (600-999 XP)
    - Master (1000+ XP)
  - [ ] Tier-specific unlocks (recipes, quality bonuses, speed)
  - [ ] XP gain from crafting, repairing, experimenting
  - [ ] XP loss on failed crafts (partial penalty)

- [ ] **Specialization System**
  - [ ] Specialization categories (weaponsmith, armorsmith, alchemist, enchanter, etc.)
  - [ ] Max 2 specializations per character (from Phase 1.4)
  - [ ] Specialization bonuses: +20% XP, +10% success rate, -10% material cost
  - [ ] Specialization unlock requirements (proficiency threshold, quest, mentor)
  - [ ] Specialization skills and techniques (unique recipes, faster crafting)
  - [ ] Respecialization mechanics (cost, cooldown)

- [ ] **Advanced Recipes**
  - [ ] Multi-stage crafting (prepare materials → assemble → finish)
  - [ ] Tool requirements (specific anvil, alchemy lab, enchanting table)
  - [ ] Environmental requirements (forge temperature, moon phase)
  - [ ] Assistant NPCs (hire helpers for complex recipes)
  - [ ] Recipe experimentation (discover new combinations)
  - [ ] Legendary recipes (unique, one-time-craft items)

- [ ] **Quality Improvement**
  - [ ] Quality levels (from Phase 1.4): Flawed, Standard, High Quality, Masterwork
  - [ ] Quality affects item stats (durability, damage, bonuses)
  - [ ] Quality influenced by proficiency, tools, materials, RNG
  - [ ] Masterwork bonuses (unique properties, glowing effects)
  - [ ] Signature items (master crafters can "sign" items)

- [ ] **Crafting Challenges & Events**
  - [ ] Timed crafting challenges (speed vs quality tradeoff)
  - [ ] Bulk order quests (craft 10x swords for NPC)
  - [ ] Crafting competitions (players compete for best quality)
  - [ ] Crafting guilds (shared recipes, group bonuses)
  - [ ] Crafting mentorship (teach other players/NPCs)

**Quality Gates:**
- ✅ **Progression Feels Rewarding:** Noticeable benefits at each tier
- ✅ **Specialization Matters:** Specialists clearly outperform generalists
- ✅ **No Grinding:** Path to mastery feels engaging, not tedious
- ✅ **Balance:** All specializations equally viable
- ✅ **Coverage:** 85%+ test coverage for crafting progression module

**Commands:**
```bash
# Run crafting progression tests
.\maven\mvn\bin\mvn.cmd test -Dtest=CraftingProgressionTest,SpecializationTest

# Test XP gain and tier thresholds
.\maven\mvn\bin\mvn.cmd test -Dtest=ProficiencyXPTest

# Test advanced recipes
.\maven\mvn\bin\mvn.cmd test -Dtest=AdvancedRecipeTest

# Simulate crafting progression
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.CraftingSimulator --iterations 1000

# Launch recipe editor
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.RecipeEditor
```

**References:**
- Design: Expand `docs/objects_crafting_legacy.md` with Phase 2 mechanics
- Existing: Phase 1.4 basic crafting (recipes, durability, quality)
- Specs: `docs/specs_summary.md` → Crafting Progression Defaults

---

### Phase 2.4: Legacy & Evolution Systems (High Priority ⭐ 0% Complete)

**Goal:** Make items and structures evolve over time, gaining story significance and unique properties.

**Deliverables:**
- [ ] **Item Evolution**
  - [ ] Evolution points system (cap at 10,000 from Phase 1.4)
  - [ ] Evolution triggers (combat use, crafting use, story events)
  - [ ] Evolution paths (branching choices, specialization)
  - [ ] Evolution stages (mundane → notable → legendary → artifact)
  - [ ] Evolution effects (stat bonuses, special abilities, lore text)
  - [ ] Evolution visualization (item appearance changes)

- [ ] **Item History & Lore**
  - [ ] History tracking (who crafted, who owned, major events)
  - [ ] Story integration (items gain properties from stories)
  - [ ] Named items (gain unique names from deeds)
  - [ ] Legendary item quests (retrieve famous weapons)
  - [ ] Item souls/spirits (sentient items with personalities)
  - [ ] Cursed items (negative effects, hard to remove)

- [ ] **Structure Evolution**
  - [ ] Structure age tracking (building history)
  - [ ] Historical significance (buildings gain fame from events)
  - [ ] Architectural styles evolve (upgrades change appearance)
  - [ ] Ruins and decay (abandoned structures deteriorate)
  - [ ] Restoration quests (rebuild historical sites)
  - [ ] Structure-based stories (haunted houses, ancient temples)

- [ ] **Legacy Effects**
  - [ ] Family heirlooms (items passed down generations)
  - [ ] Clan relics (items tied to clan history)
  - [ ] Set bonuses (wearing items from same lineage)
  - [ ] Story-driven bonuses (item stronger in specific contexts)
  - [ ] Reputation inheritance (item carries owner's fame/infamy)

**Quality Gates:**
- ✅ **Memorable Items:** Players form attachments to evolved items
- ✅ **Story Integration:** Item histories feel organic, not forced
- ✅ **Balance:** Legendary items powerful but not game-breaking
- ✅ **Performance:** Item history doesn't bloat save files excessively
- ✅ **Coverage:** 80%+ test coverage for legacy module

**Commands:**
```bash
# Run legacy system tests
.\maven\mvn\bin\mvn.cmd test -Dtest=LegacyTest,EvolutionTest,ItemHistoryTest

# Test item evolution
.\maven\mvn\bin\mvn.cmd test -Dtest=ItemEvolutionTest

# Test structure evolution
.\maven\mvn\bin\mvn.cmd test -Dtest=StructureEvolutionTest

# Simulate item evolution
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.EvolutionSimulator

# Launch legacy editor
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.LegacyEditor
```

**References:**
- Design: `docs/objects_crafting_legacy.md` → Legacy System
- Existing: Phase 1.4 item system (evolution points cap)
- Design: Expand with Phase 2 mechanics

---

### Phase 2.5: Dynamic Economy (High Priority ⭐ 0% Complete)

**Goal:** Replace static pricing with dynamic supply/demand economics, trade routes, and emergent market behaviors.

**Deliverables:**
- [ ] **Supply & Demand System**
  - [ ] Dynamic pricing formula: `price = basePrice * (demand / supply) * scarcityMultiplier`
  - [ ] Regional supply tracking (available quantity per region)
  - [ ] Regional demand tracking (consumption rate, player activity)
  - [ ] Price history tracking (trend analysis)
  - [ ] Price arbitrage opportunities (buy low, sell high)
  - [ ] Market saturation (oversupply drives prices down)

- [ ] **Trade Routes**
  - [ ] Caravan system (NPC traders move between regions)
  - [ ] Trade route establishment (player-created, AI-created)
  - [ ] Trade route dangers (bandits, monsters, weather)
  - [ ] Trade route efficiency (distance, safety, speed)
  - [ ] Trade route monopolies (control routes for profit)
  - [ ] Trade route disruption (wars, disasters)

- [ ] **Market Mechanics**
  - [ ] Regional markets (different prices per region)
  - [ ] Market stalls and shops (player-owned, NPC-owned)
  - [ ] Auction system (bid on rare items)
  - [ ] Commodity trading (futures, speculation)
  - [ ] Market manipulation prevention (anti-exploit measures)
  - [ ] Black markets (illegal goods, higher risk/reward)

- [ ] **Currency & Banking**
  - [ ] Multiple currency types (gold, silver, regional currencies)
  - [ ] Exchange rates (currency conversion)
  - [ ] Banking system (deposits, withdrawals, interest)
  - [ ] Loans and debt (borrow money, repay with interest)
  - [ ] Inflation/deflation mechanics (money supply dynamics)
  - [ ] Counterfeiting detection (fake currency)

- [ ] **Economic Events**
  - [ ] Market crashes (overvaluation corrections)
  - [ ] Resource booms (new discovery drives prices down)
  - [ ] Embargo effects (trade restrictions)
  - [ ] Tax policy impacts (player/clan taxes affect economy)
  - [ ] Economic crises (recession, hyperinflation)

**Quality Gates:**
- ✅ **Realistic Behavior:** Prices respond logically to supply/demand
- ✅ **Stability:** Economy doesn't spiral into hyper-deflation or inflation
- ✅ **Player Impact:** Players can meaningfully participate in economy
- ✅ **No Exploits:** Anti-duplication, anti-infinite-money safeguards
- ✅ **Coverage:** 85%+ test coverage for economy module (critical system)

**Commands:**
```bash
# Run economy tests
.\maven\mvn\bin\mvn.cmd test -Dtest=EconomyTest,TradeRouteTest,MarketTest

# Test supply/demand dynamics
.\maven\mvn\bin\mvn.cmd test -Dtest=SupplyDemandTest

# Test trade routes
.\maven\mvn\bin\mvn.cmd test -Dtest=TradeRouteTest

# Simulate economy over time
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.EconomySimulator --ticks 100000

# Launch market analyzer
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.MarketAnalyzer
```

**References:**
- Design: Expand `docs/economy_resources.md` with Phase 2 mechanics
- Existing: Phase 1.4 basic economy (static pricing)
- Specs: Add new section to `docs/specs_summary.md` for economy formulas

---

### Phase 2.6: Advanced NPC AI (Medium Priority 📘 0% Complete)

**Goal:** Evolve NPCs from simple behavior types to intelligent agents with pathfinding, decision-making, and emergent behaviors.

**Deliverables:**
- [ ] **Pathfinding System**
  - [ ] A* algorithm for optimal path calculation
  - [ ] Obstacle avoidance (terrain, structures, other NPCs)
  - [ ] Path caching and reuse (performance optimization)
  - [ ] Dynamic pathfinding (adapt to changing terrain)
  - [ ] Group pathfinding (formations, follow leader)
  - [ ] Path smoothing (natural-looking movement)

- [ ] **Behavior Trees**
  - [ ] Behavior tree framework (composites, decorators, actions)
  - [ ] Common behaviors (wander, chase, flee, patrol, guard)
  - [ ] Context-aware behaviors (react to player, environment)
  - [ ] Priority system (choose best action from options)
  - [ ] Behavior interruption (cancel action when needed)
  - [ ] Behavior debugging tools (visualize decision-making)

- [ ] **NPC Memory & Learning**
  - [ ] Short-term memory (recent events, seen players)
  - [ ] Long-term memory (grudges, friendships, trauma)
  - [ ] Learning from experience (adapt tactics, avoid dangers)
  - [ ] Personality influence on decisions (brave vs cautious)
  - [ ] Emotion system (anger, fear, joy affect behavior)

- [ ] **Social AI**
  - [ ] NPC-to-NPC interactions (conversations, trading, fighting)
  - [ ] Relationship formation (NPCs make friends/enemies)
  - [ ] Group dynamics (cliques, hierarchies, alliances)
  - [ ] Reputation awareness (NPCs react to player reputation)
  - [ ] Gossip system (NPCs share information)

- [ ] **Advanced Behaviors**
  - [ ] Tactical combat AI (use cover, flank, retreat)
  - [ ] Economic AI (NPCs trade, craft, accumulate wealth)
  - [ ] Career progression (NPCs gain skills, change jobs)
  - [ ] Family dynamics (NPCs marry, have children, age)
  - [ ] Migration and settlement (NPCs move to better regions)

**Quality Gates:**
- ✅ **Believable NPCs:** NPCs don't feel robotic; actions make sense
- ✅ **Performance:** Pathfinding for 500+ NPCs per region without lag
- ✅ **Variety:** Different NPC types have distinct behaviors
- ✅ **Player Challenge:** Combat AI provides appropriate difficulty
- ✅ **Coverage:** 75%+ test coverage for AI module

**Commands:**
```bash
# Run AI tests
.\maven\mvn\bin\mvn.cmd test -Dtest=NPCAITest,PathfindingTest,BehaviorTreeTest

# Test pathfinding
.\maven\mvn\bin\mvn.cmd test -Dtest=PathfindingTest

# Test behavior trees
.\maven\mvn\bin\mvn.cmd test -Dtest=BehaviorTreeTest

# Simulate NPC AI
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.AISimulator --npcs 100 --ticks 10000

# Launch AI debugger
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.AIDebugger
```

**References:**
- Design: Create new `docs/npc_ai_system.md`
- Existing: Phase 1.3 NPCs (behavior types, spawning)
- Architecture: `docs/architecture_design.md` → Character Manager

---

### Phase 2.7: Event Propagation Enhancement (Medium Priority 📘 0% Complete)

**Goal:** Improve event propagation from basic BFS to sophisticated decay, saturation, and cross-region dynamics.

**Deliverables:**
- [ ] **Enhanced Propagation Algorithm**
  - [ ] Weighted propagation (events spread faster along roads, rivers)
  - [ ] Cultural propagation (events spread better among allied factions)
  - [ ] Decay formula refinement: `decay(h) = exp(-k * h)`, k = 0.8 (from Phase 1.7)
  - [ ] Alternative decay functions (linear, polynomial) with feature flags
  - [ ] Propagation speed based on event type (rumors fast, news slow)

- [ ] **Advanced Saturation Controls**
  - [ ] Dynamic saturation caps (adjust based on region activity)
  - [ ] Saturation pressure (high saturation prevents new events)
  - [ ] Event priority system (important events displace minor ones)
  - [ ] Player influence on saturation (players generate more events)
  - [ ] Saturation visualization (heatmaps of event density)

- [ ] **Cross-Region Event Chains**
  - [ ] Multi-region event sequences (event in Region A triggers in Region B)
  - [ ] Cascading crises (war in one region affects neighbors)
  - [ ] Event convergence (multiple events merge into mega-event)
  - [ ] Event resolution effects (resolved event changes world state)

- [ ] **Event Persistence & History**
  - [ ] Event archive (past events stored, can be queried)
  - [ ] Historical significance (major events remembered longer)
  - [ ] Event commemoration (monuments, holidays for important events)
  - [ ] Event-driven lore generation (events create stories)

**Quality Gates:**
- ✅ **Realistic Spread:** Events propagate believably (not instant, not too slow)
- ✅ **No Spam:** Saturation controls prevent event overload
- ✅ **Emergent Stories:** Event chains create interesting narratives
- ✅ **Performance:** Propagation doesn't cause lag spikes
- ✅ **Coverage:** 85%+ test coverage for event propagation module

**Commands:**
```bash
# Run event propagation tests
.\maven\mvn\bin\mvn.cmd test -Dtest=EventPropagationEnhancedTest

# Test saturation controls
.\maven\mvn\bin\mvn.cmd test -Dtest=SaturationEnhancedTest

# Simulate event propagation
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.EventPropagationSimulator

# Visualize event spread
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.EventHeatmapViewer
```

**References:**
- Design: `docs/stories_events.md` → Event Propagation
- Existing: Phase 1.7 basic propagation (BFS, decay k=0.8)
- Specs: `docs/specs_summary.md` → Event Propagation & Saturation

---

### Phase 2.8: Performance Optimization (Medium Priority 📘 0% Complete)

**Goal:** Optimize all systems for larger worlds, more players, and longer play sessions.

**Deliverables:**
- [ ] **Region Simulation Optimization**
  - [ ] Profiling active vs background region performance
  - [ ] Optimize tick processing (batch updates, multithreading)
  - [ ] Reduce memory footprint per region
  - [ ] Lazy-load region data (only load what's needed)
  - [ ] Region unloading optimization (efficient serialization)

- [ ] **Pathfinding Optimization**
  - [ ] Path caching (reuse previously calculated paths)
  - [ ] Hierarchical pathfinding (navigation meshes)
  - [ ] Parallel pathfinding (multiple NPCs calculate simultaneously)
  - [ ] Path smoothing optimization (reduce waypoint count)

- [ ] **Persistence Optimization**
  - [ ] Incremental saves (only save changed regions)
  - [ ] Compression (gzip save files)
  - [ ] Async writes (don't block gameplay for saves)
  - [ ] Save batching (group multiple region saves)
  - [ ] Checksum calculation optimization

- [ ] **Memory Management**
  - [ ] Object pooling (reuse objects instead of allocating)
  - [ ] Garbage collection tuning (G1GC parameters)
  - [ ] Memory leak detection and fixes
  - [ ] Reduce allocations in hot paths

- [ ] **Rendering Optimization** (if GUI implemented)
  - [ ] Viewport culling (only render visible regions)
  - [ ] Level-of-detail (less detail for distant objects)
  - [ ] Sprite batching (reduce draw calls)
  - [ ] Texture atlases (reduce texture switches)

**Quality Gates:**
- ✅ **World Size:** Support 1024x1024 worlds without performance degradation
- ✅ **Player Count:** Support 100+ concurrent players on single server
- ✅ **Memory:** <8GB RAM usage for typical server (10 active regions, 50 background)
- ✅ **Tick Rate:** Maintain 1 tick/second even under heavy load
- ✅ **Save Speed:** Save 512x512 world in <5 seconds

**Commands:**
```bash
# Run performance benchmarks
.\maven\mvn\bin\mvn.cmd test -Dtest=PerformanceBenchmarkTest

# Profile region simulation
java -XX:StartFlightRecording=filename=region_sim.jfr -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.RegionSimBenchmark

# Memory profiling
java -Xmx4G -XX:+HeapDumpOnOutOfMemoryError -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.MemoryProfiler

# Load testing (multiplayer)
.\maven\mvn\bin\mvn.cmd gatling:test -Dgatling.simulationClass=MultiplayerLoadTest
```

**References:**
- Architecture: `docs/architecture_design.md` → Performance Strategies, Scalability Thresholds
- Specs: `docs/specs_summary.md` → Performance Targets

---

### Phase 2.9: Modding Support & Tools (Medium Priority 📘 0% Complete)

**Goal:** Enable community content creation with safe, powerful modding tools.

**Deliverables:**
- [ ] **Mod Framework**
  - [ ] Mod manifest format (JSON or YAML)
  - [ ] Mod loading system (detect, validate, load mods)
  - [ ] Mod dependency resolution (mod A requires mod B)
  - [ ] Mod conflict detection (two mods modify same thing)
  - [ ] Mod priority system (which mod overrides)
  - [ ] Hot-reload mods (reload without restart)

- [ ] **Data-Only Mods**
  - [ ] Custom items (new weapons, armor, consumables)
  - [ ] Custom recipes (new crafting options)
  - [ ] Custom NPCs (new characters, behaviors)
  - [ ] Custom biomes (new terrain types)
  - [ ] Custom quests (new storylines)
  - [ ] Custom skills/traits/races

- [ ] **Scripted Mods** (Advanced)
  - [ ] WASM sandbox (safe script execution)
  - [ ] Scripting API (events, actions, queries)
  - [ ] Script resource limits (CPU, memory caps)
  - [ ] Script security auditing
  - [ ] Script debugging tools

- [ ] **Mod Creation Tools**
  - [ ] Item editor (visual item creation)
  - [ ] Recipe editor (visual crafting design)
  - [ ] NPC editor (character creation)
  - [ ] Quest editor (storyline builder)
  - [ ] World preset editor (custom worldgen params)
  - [ ] Mod validator (check for errors before publishing)

- [ ] **Mod Distribution**
  - [ ] Mod repository/browser (in-game mod store)
  - [ ] Mod ratings and reviews
  - [ ] Automatic mod updates
  - [ ] Mod collections (curated mod packs)

**Quality Gates:**
- ✅ **Safety:** No mod can crash server or compromise security
- ✅ **Ease of Use:** Non-programmers can create data-only mods
- ✅ **Power:** Modders can create substantial content (new game modes)
- ✅ **Documentation:** Complete modding guide and API reference
- ✅ **Coverage:** 70%+ test coverage for mod framework

**Commands:**
```bash
# Load mods
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.Game --mods-dir ./mods

# Validate mod
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.ModValidator --mod my_mod.zip

# Launch item editor
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.ItemEditor

# Launch quest editor
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.QuestEditor

# Test mod sandbox
.\maven\mvn\bin\mvn.cmd test -Dtest=ModSandboxTest
```

**References:**
- Design: `docs/modding_and_security.md` → Mod Sandboxing, Security Model
- Specs: `docs/specs_summary.md` → Mod Sandbox Caps
- Architecture: `docs/architecture_design.md` → Modding & Extensibility

---

### Phase 2.10: Content Creation & Balancing (Medium Priority 📘 0% Complete)

**Goal:** Populate the world with rich content and fine-tune game balance.

**Deliverables:**
- [ ] **Quest Content**
  - [ ] 50+ story quests (main storyline branches)
  - [ ] 100+ side quests (exploration, combat, crafting, social)
  - [ ] Dynamic quest generation (procedural quest templates)
  - [ ] Quest chains (multi-part storylines)
  - [ ] Faction-specific quests
  - [ ] Seasonal/event quests

- [ ] **NPC Population**
  - [ ] 100+ unique named NPCs (quest givers, merchants, trainers)
  - [ ] Diverse NPC backgrounds and personalities
  - [ ] NPC-driven storylines
  - [ ] Memorable NPC dialog and voice
  - [ ] NPC portraits and descriptions

- [ ] **Item Variety**
  - [ ] 200+ unique items (weapons, armor, tools, consumables)
  - [ ] Legendary item questlines
  - [ ] Set items with bonuses
  - [ ] Cultural item variations (different regions, different styles)
  - [ ] Seasonal/event items

- [ ] **Balance Tuning**
  - [ ] Combat balance (damage, health, difficulty curves)
  - [ ] Economic balance (prices, income, expenses)
  - [ ] Skill progression balance (time to mastery)
  - [ ] Quest reward balance (appropriate for difficulty)
  - [ ] Magic balance (mana costs, spell power)
  - [ ] Diplomacy balance (reputation gains/losses)

- [ ] **Playtesting & Iteration**
  - [ ] Internal playtesting (100+ hours per tester)
  - [ ] External alpha/beta testing
  - [ ] Balance feedback collection and analysis
  - [ ] Iterative tuning based on data
  - [ ] Final polish pass

**Quality Gates:**
- ✅ **Content Volume:** Enough content for 40+ hours of gameplay
- ✅ **Quality:** All quests tested, no broken/unfair quests
- ✅ **Balance:** No dominant strategies; multiple viable builds
- ✅ **Variety:** Different playstyles feel distinct and fun
- ✅ **Player Satisfaction:** 4.0+/5.0 in playtester surveys

**Commands:**
```bash
# Import content batch
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.ContentImporter --dir ./content

# Validate quest data
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.QuestValidator

# Balance analyzer
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.BalanceAnalyzer --world test_world.json

# Generate balance report
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.BalanceReporter --output balance_report.html
```

**References:**
- Design: All design docs in `docs/` for content creation guidelines
- Playtesting: Create new `docs/playtesting_feedback.md` to track findings

---

## Testing Strategy for Phase 2

### Unit Tests (Same rigor as Phase 1)
- All new modules: 70%+ coverage minimum, 85%+ for critical systems
- Determinism tests for all procedural systems (magic, economy, AI)
- Edge case coverage (overflow, underflow, boundary conditions)

### Integration Tests
- Cross-system interactions (magic affects combat, economy affects diplomacy)
- Multiplayer scenarios (100+ concurrent players, conflict resolution)
- Long-running simulations (10,000+ ticks without crashes)

### Performance Tests
- Benchmarks for all optimizations (before/after comparisons)
- Load tests (large worlds, many players, heavy event load)
- Memory leak detection (long sessions, repeated operations)

### Balance Tests
- Automated balance checking (detect overpowered combinations)
- AI vs AI combat (ensure fair matchups)
- Economic simulations (ensure stable prices)

### User Acceptance Tests
- Playtesting with real users (diverse playstyles)
- Usability studies (identify pain points)
- A/B testing for controversial features

---

## Quality Gates for Phase 2 Completion

### Technical Requirements
- ✅ All Phase 2 features implemented and tested
- ✅ 70%+ test coverage for all new modules
- ✅ 85%+ test coverage for critical modules (magic, economy, AI)
- ✅ All determinism tests passing
- ✅ Performance targets met (1024x1024 worlds, 100+ players)
- ✅ No critical bugs, <10 major bugs
- ✅ Documentation complete for all new features

### Content Requirements
- ✅ 50+ story quests implemented
- ✅ 100+ side quests implemented
- ✅ 200+ unique items created
- ✅ 100+ unique NPCs created
- ✅ All content playtested and balanced

### Player Experience
- ✅ 100+ hours of internal playtesting completed
- ✅ External alpha/beta testing with 50+ players
- ✅ 4.0+/5.0 player satisfaction in surveys
- ✅ Balance feedback incorporated and validated

---

## Build Commands for Phase 2

```bash
# Build with Phase 2 modules
.\maven\mvn\bin\mvn.cmd clean package -P phase2

# Run all Phase 2 tests
.\maven\mvn\bin\mvn.cmd test -Dtest=*Phase2Test

# Run specific Phase 2 module tests
.\maven\mvn\bin\mvn.cmd test -Dtest=MagicSystemTest
.\maven\mvn\bin\mvn.cmd test -Dtest=AdvancedDiplomacyTest
.\maven\mvn\bin\mvn.cmd test -Dtest=DynamicEconomyTest

# Performance benchmarks
.\maven\mvn\bin\mvn.cmd test -Dtest=Phase2PerformanceBenchmark

# Run balance analyzer
java -cp target\adventure-0.2.0-SNAPSHOT.jar org.adventure.tools.BalanceAnalyzer

# Package Phase 2 release
.\maven\mvn\bin\mvn.cmd package -P phase2-release
```

---

## Success Criteria

### Phase 2 Complete When:
1. All 10 Phase 2 sub-phases (2.1-2.10) are 100% complete
2. All quality gates passed for each sub-phase
3. Performance targets met (large worlds, many players)
4. Content volume meets targets (50+ story quests, 200+ items, etc.)
5. Playtesting shows 4.0+/5.0 satisfaction
6. Documentation complete and reviewed
7. No critical bugs, <10 major bugs
8. Game feels polished and complete

---

## Next Steps After Phase 2

1. **Launch Preparation** — Marketing, community building, final polish
2. **Post-Launch Support** — Bug fixes, balance patches, player feedback
3. **Phase 3 Planning** — New major features (PvP, guild wars, world events)
4. **DLC/Expansion** — New regions, races, stories, mechanics
5. **Community Growth** — Modding contests, official tournaments, content showcases

---

**Note:** This guide is a living document. Update as implementation progresses and design decisions evolve. Always refer to `docs/design_decisions.md` for canonical architectural choices.
