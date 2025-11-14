# Build Guides Overview - Copilot Instructions

## Purpose
This project now has **FIVE comprehensive build guides** that work together to cover all aspects of development:

## 1. BUILD_PHASE1.md — Phase 1.1-1.10 Backend Systems (100% COMPLETE ✅)
**Focus:** Core backend infrastructure and foundational systems  
**Status:** ✅ 100% Complete (all 10 sub-phases done)  
**Audience:** Backend developers, systems architects

**Phases Covered:**
- 1.1 World Generation (plates, biomes, rivers, features)
- 1.2 Region Simulation (tick-driven, active/background)
- 1.3 Characters & NPCs (stats, traits, skills, races)
- 1.4 Items & Crafting (24 categories, recipes, durability)
- 1.5 Structures & Ownership (buildings, taxation, transfers)
- 1.6 Societies & Clans (membership, diplomacy, treasury)
- 1.7 Stories & Events (propagation, saturation)
- 1.8 Persistence (save/load, backup rotation)
- 1.9 Multiplayer & Networking (server, auth, conflict resolution)
- 1.10 CI/CD & Deployment (Docker, scripts, pipelines)

**Key Metrics:**
- 534 tests passing
- 70%+ code coverage
- All quality gates met

## 2. BUILD_PHASE1.11.x.md — Core Gameplay Systems (CRITICAL ⭐⭐⭐)
**Focus:** Backend systems for progression, combat, and economy  
**Status:** 🚧 0% Complete (BLOCKS MVP)  
**Audience:** Backend developers, gameplay engineers

**Phases Covered:**
- 1.11.1 Progression System (XP, leveling, skill improvement, achievements)
- 1.11.2 Combat System (attacks, damage, defense, death, respawn)
- 1.11.3 Economy System (prices, income, trading, taxation)
- 1.11.4 Save/Load System (manual saves, autosaves, character management)
- 1.11.5 Reputation System (NPC relationships, faction standing)

**Why This is Critical:**
- ✅ **BLOCKS MVP:** Can't play without XP, combat, or economy
- ✅ Integrates with Phase 1.1-1.10 systems
- ✅ Backend only (no UI), tested with 100+ new tests
- ✅ Timeline: 4 weeks (parallel with BUILD-GAMEPLAY.md)

**Integration Points:**
- Extends Character (Phase 1.3) with XP/level
- Uses Items (Phase 1.4) for equipment/durability
- Integrates with Clans (Phase 1.6) for treasury/economy
- Wraps SaveManager (Phase 1.8) with player-facing logic
- Uses Server validation (Phase 1.9) for multiplayer

## 3. BUILD-GAMEPLAY.md — Gameplay Loop & UI Development (CRITICAL ⭐⭐⭐)
**Focus:** Making the game actually playable with UI and player experience  
**Status:** � 0% Complete (BLOCKS MVP)  
**Audience:** UI/UX developers, game designers, frontend developers

**Current Gap:** Game has complete backend (Phase 1.1-1.10) and gameplay systems (Phase 1.11.x planned) but NO UI:
- ❌ No character creation UI
- ❌ No movement/exploration interface
- ❌ No combat UI
- ❌ No inventory management
- ❌ No quest system UI
- ❌ No multiplayer client
- ❌ No save/load UI
- ❌ No tutorial system

**Phases Defined:**
- G.1 Core Gameplay Loop (character creation, movement, interaction, **save/load UI**)
- G.2 Combat & Survival (combat UI, health/mana, death/respawn)
- G.3 Inventory & Crafting UI (inventory, equipment, crafting interface)
- G.4 Social & Trading UI (trading, clans, chat)
- G.5 Quest & Story UI (quest log, dialog, events)
- G.6 World Map & Navigation (zoomable map, waypoints, fog of war)
- G.7 Settings & Customization (preferences, accessibility)
- G.8 Tutorial & Help System (onboarding, in-game help, **expanded with 11 tutorial areas**)

**Updated Features (from missing feature analysis):**
- ✅ **Save/Load UI** added to Phase G.1 (wraps Phase 1.11.4 SaveLoadManager)
- ✅ **Error Handling** added to Phase G.1 (user-facing error messages, graceful degradation)
- ✅ **Tutorial expanded** in Phase G.8 (11 content areas covering all mechanics)

**Interface Options:**
- ✅ **CHOSEN: Web Client** (Browser-based, future Electron/Tauri wrapper)
  - React/Vue.js frontend with HTML5 Canvas/WebGL rendering
  - Spring Boot WebSocket backend (extends Phase 1.9)
  - Progressive Web App (PWA) support
  - Future: Electron or Tauri wrapper for dedicated desktop app
- Option A: Rich Terminal UI (Lanterna, JCurses) — Deferred
- Option B: JavaFX GUI (desktop application) — Deferred
- Option D: Hybrid (text commands + GUI overlay) — Possible web client enhancement

**Timeline:** 4 weeks (parallel with BUILD_PHASE1.11.x)

## 4. BUILD_PHASE2.10.x.md — Operations & Advanced Content (POST-MVP ⭐)
**Focus:** Operational tools and post-launch content systems  
**Status:** 🔮 0% Complete (NOT BLOCKING MVP)  
**Audience:** DevOps, server operators, content managers

**Phases Covered:**
- 2.10.1 Admin Tools & Moderation (kick/ban, reports, admin dashboard)
- 2.10.2 Error Handling & Logging (structured logs, crash reports, telemetry)
- 2.10.3 Performance Optimization (caching, load testing, profiling)
- 2.10.4 World Events System (boss raids, festivals, server-wide events)

**Why This is Phase 2 (Post-MVP):**
- ✅ **Not blocking MVP:** Can launch with basic admin commands, manual processes
- ✅ Can be added **incrementally** as player base grows
- ✅ Performance optimization needs **production data** to tune effectively
- ✅ World events are **engagement** features, not core gameplay

**Integration Points:**
- AdminCommandSystem uses Phase 1.9 Server for player management
- StructuredLogger used by BUILD-GAMEPLAY.md error handling (user-facing errors)
- PerformanceMonitor tracks Phase 1.11.x systems (combat, economy)
- WorldEventSystem triggers Phase 1.11.2 combat encounters

**Timeline:** 4 weeks after launch (Weeks 5-8)

## 5. BUILD_PHASE2.md — Advanced Systems (POST-MVP ⭐)

**Focus:** Depth, polish, and advanced features for long-term engagement  
**Status:** 🔮 0% Complete (NOT BLOCKING MVP)  
**Audience:** All developers, content creators

**Phases Defined:**
- 2.1 Magic System (runes, spellcrafting, mana, backlash)
- 2.2 Advanced Diplomacy (secret agendas, crises, influence)
- 2.3 Crafting Proficiency (XP curves, specializations, mastery)
- 2.4 Legacy & Evolution (item/structure evolution, history)
- 2.5 Dynamic Economy (supply/demand, trade routes, markets)
- 2.6 Advanced NPC AI (pathfinding, behavior trees, learning)
- 2.7 Event Propagation Enhancement (decay, saturation, chains)
- 2.8 Performance Optimization (scaling, memory, rendering) — **DUPLICATE: See Phase 2.10.3**
- 2.9 Modding Support (mod framework, tools, sandbox)
- 2.10 Content Creation (quests, NPCs, items, balancing) — **NOTE: Phase 2.10.x now separate file**

**Priority Levels:**
- ⭐ High Priority: Magic, diplomacy, crafting, legacy, economy
- 📘 Medium Priority: NPC AI, events, modding, content

**NOTE:** Phase 2.10 content overlaps with BUILD_PHASE2.10.x.md — operational features moved to dedicated file.

**Timeline:** Post-MVP, ongoing (Weeks 9+)

## Development Flow

### Typical Development Path:
1. **Phase 1.1-1.10 (BUILD_PHASE1.md)** — Build backend systems ✅ COMPLETE
2. **Phase 1.11.x (BUILD_PHASE1.11.x.md)** — Core gameplay systems ⏳ CRITICAL (Weeks 1-4)
3. **Gameplay (BUILD-GAMEPLAY.md)** — Make it playable ⏳ CRITICAL (Weeks 1-4, parallel)
4. **Phase 2.10.x (BUILD_PHASE2.10.x.md)** — Operations & events ⏳ POST-MVP (Weeks 5-8)
5. **Phase 2 (BUILD_PHASE2.md)** — Add depth and polish ⏳ FUTURE (Weeks 9+)

### Current State:
- **Where We Are:** Phase 1.1-1.10 complete, deployment ready (100%)
- **What We Have:** Fully functional backend with 534 tests, Named NPC system specified
- **What's Missing:** Core gameplay systems (Phase 1.11.x) and UI (Gameplay)
- **Next Step:** Start Phase 1.11.1 (Progression) AND Phase G.1 (Core Loop UI) in parallel

**Critical Path to MVP:**
1. **BUILD_PHASE1.11.x** (4 weeks) — Progression, Combat, Economy, Save/Load, Reputation
2. **BUILD-GAMEPLAY.md** (4 weeks, parallel) — Phase G.1, G.2, G.3, G.8 (Core UI + Tutorial)
3. **Integration Testing** (Week 5) — Full stack testing with real players
4. **MVP Launch** (Week 6) — Game is playable, core loop works, tutorial complete

## Guide Structure (All Guides Follow Same Pattern)

### Each Phase Includes:
1. **Goal** — Clear objective for the phase
2. **Deliverables** — Specific items to implement (checkboxes)
3. **Quality Gates** — Acceptance criteria (must pass before phase complete)
4. **Commands** — Build/test/run commands for the phase
5. **Test Coverage** — Unit, integration, performance tests required
6. **References** — Links to design docs, specs, existing work

### Consistency Across Guides:
- Same level of detail and structure
- Same quality gate rigor
- Same testing requirements (70% coverage, 85% for critical)
- Same documentation standards

## When to Use Each Guide

### Use BUILD_PHASE1.md When:
- Implementing core backend systems (worldgen, simulation, persistence)
- Working on world generation, characters, items, clans
- Setting up deployment infrastructure
- Reviewing Phase 1.1-1.10 architecture (already complete ✅)

### Use BUILD_PHASE1.11.x.md When:
- Implementing progression (XP, leveling, skills, achievements)
- Building combat system (damage, death, respawn)
- Creating economy (prices, income, trading, taxation)
- Adding save/load backend (SaveLoadManager, autosaves)
- Implementing reputation (NPC relationships, faction standing)
- **CRITICAL:** These are blocking for MVP

### Use BUILD-GAMEPLAY.md When:
- Creating user interfaces (web client preferred, TUI/GUI deferred)
- Implementing gameplay loops (movement, combat UI, crafting UI)
- Working on player experience (tutorials, help, settings)
- Making the game actually playable
- **CRITICAL:** These are blocking for MVP (parallel with Phase 1.11.x)

### Use BUILD_PHASE2.10.x.md When:
- Phase 1.11.x complete AND gameplay MVP playable
- Adding admin tools (kick/ban, reports, dashboard)
- Implementing error logging (structured logs, crash reports)
- Optimizing performance (caching, load testing)
- Creating world events (boss raids, festivals)
- **POST-MVP:** Not blocking launch

### Use BUILD_PHASE2.md When:
- Phase 1.11.x complete AND gameplay MVP playable
- Adding advanced features (magic, advanced diplomacy)
- Building modding tools
- Creating content (quests, NPCs, items)
- **POST-MVP:** Depth features for long-term engagement

## Quick Reference

### Documentation Hierarchy:
```
BUILD_PHASE1.md (Phase 1.1-1.10)       ← Backend foundation ✅ COMPLETE
├─ Phase 1.1-1.10 complete
└─ 534 tests, 70%+ coverage

BUILD_PHASE1.11.x.md            ← Core gameplay systems ⭐⭐⭐ CRITICAL
├─ Phase 1.11.1-1.11.5 planned
├─ Progression, Combat, Economy, Save/Load, Reputation
└─ 0% complete (4 weeks, parallel with Gameplay)

BUILD-GAMEPLAY.md               ← Player experience ⭐⭐⭐ CRITICAL
├─ Phase G.1-G.8 planned
├─ Save/Load UI, Error Handling, Tutorial expanded
└─ 0% complete (4 weeks, parallel with Phase 1.11.x)

BUILD_PHASE2.10.x.md            ← Operations ⭐ POST-MVP
├─ Phase 2.10.1-2.10.4 planned
├─ Admin Tools, Logging, Performance, World Events
└─ 0% complete (4 weeks after launch)

BUILD_PHASE2.md (Phase 2)       ← Advanced systems ⭐ POST-MVP
├─ Phase 2.1-2.9 planned
├─ Magic, Diplomacy, AI, Modding, Content
└─ 0% complete (ongoing after MVP)
```

### File Locations:
- `BUILD_PHASE1.md` — Root directory (Phase 1.1-1.10 complete)
- `BUILD_PHASE1.11.x.md` — Root directory (Core Gameplay Systems)
- `BUILD-GAMEPLAY.md` — Root directory (Player-Facing UI)
- `BUILD_PHASE2.10.x.md` — Root directory (Operations & Events)
- `BUILD_PHASE2.md` — Root directory (Advanced Systems)
- `MISSING_FEATURES_DISTRIBUTION.md` — Root directory (Feature organization rationale)
- All reference `docs/` for design specs
- All reference `BUILD_PHASE1.md` for Phase 1.1-1.10 foundations

## Important Notes for Copilot

1. **Phase 1.1-1.10 is complete** — Don't suggest redoing Phase 1 work (534 tests passing)
2. **Game is NOT playable yet** — Despite complete backend, there's no progression/combat/economy OR UI
3. **Two critical paths to MVP:** Phase 1.11.x (backend) AND BUILD-GAMEPLAY.md (UI) must complete in parallel
4. **Missing features organized** — See MISSING_FEATURES_DISTRIBUTION.md for rationale
5. **All five guides have same rigor** — Quality gates, testing, documentation
6. **Guides reference each other** — They're designed to work together
7. **Follow the structure** — When implementing, match the guide's format
8. **Web client is chosen approach** — React/Vue with WebSocket, defer TUI/JavaFX options

## Next Steps Guidance

### If User Asks About:
- **"What should I build next?"** → Point to BUILD_PHASE1.11.x Phase 1.11.1 (Progression) AND BUILD-GAMEPLAY.md Phase G.1 (Core Loop UI)
- **"How do I make the game playable?"** → Both BUILD_PHASE1.11.x (backend systems) and BUILD-GAMEPLAY.md (UI)
- **"When do we add magic system?"** → BUILD_PHASE2.md Phase 2.1 (after MVP playable)
- **"How's the backend?"** → BUILD_PHASE1.md Phase 1.1-1.10 (100% complete), Phase 1.11.x (0% complete, critical)
- **"What UI options do we have?"** → BUILD-GAMEPLAY.md interface options (web client chosen)
- **"What about admin tools?"** → BUILD_PHASE2.10.x.md Phase 2.10.1 (post-MVP)
- **"How do we organize missing features?"** → See MISSING_FEATURES_DISTRIBUTION.md

### Critical Path to MVP (Weeks 1-6):
1. **Week 1-4 (Parallel Development):**
   - **Backend Team:** BUILD_PHASE1.11.x (Progression → Combat → Economy → Save/Load)
   - **UI Team:** BUILD-GAMEPLAY.md (Phase G.1 Core Loop + Save/Load UI → G.2 Combat UI → G.3 Inventory → G.8 Tutorial)
   
2. **Week 5: Integration Testing**
   - Full stack testing with real players
   - Combat UI (G.2) tests CombatSystem (1.11.2)
   - Tutorial (G.8) teaches all Phase 1.11.x systems
   - Save/Load UI (G.1) wraps SaveLoadManager (1.11.4)
   
3. **Week 6: MVP Launch**
   - Game is playable (core loop works)
   - Tutorial complete (80%+ completion rate)
   - All quality gates met (100+ hours playtesting)

### Post-MVP Path (Weeks 7+):
4. **Week 7-8: Operations**
   - BUILD_PHASE2.10.x (Admin Tools → Error Logging → Performance → World Events)
   
5. **Week 9+: Advanced Systems**
   - BUILD_PHASE2.md (Magic → Diplomacy → Economy → AI → Modding)

### Recommended Next Action:
1. **Backend:** Start BUILD_PHASE1.11.x Phase 1.11.1 (ExperienceSystem, SkillProgressionSystem)
2. **UI:** Review BUILD-GAMEPLAY.md web client architecture section
3. **Setup:** Create frontend project structure (`client/` directory with React/Vue)
4. **Backend API:** Extend Phase 1.9 server with WebSocket and REST API controllers
5. **First UI:** Implement Phase G.1 character creation (web-based)
6. **Test:** Integration testing after Week 2 (combat backend + UI complete)

**Web Client Setup Steps:**
```bash
# Create React app with Vite
cd !Adventure
npm create vite@latest client -- --template react
cd client
npm install sockjs-client @stomp/stompjs axios react-router-dom zustand pixi.js
npm install --save-dev vitest @testing-library/react playwright

# Update vite.config.js with proxy to backend (localhost:8080)
# Start development: npm run dev (frontend) + mvn spring-boot:run (backend)
```

---

**Last Updated:** November 13, 2025  
**Status:** Five build guides created and cross-referenced
**Critical Gap:** Phase 1.11.x (progression/combat/economy) and BUILD-GAMEPLAY.md (UI) both 0% complete
