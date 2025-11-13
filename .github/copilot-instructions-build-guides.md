# Build Guides Overview - Copilot Instructions

## Purpose
This project now has **THREE comprehensive build guides** that work together to cover all aspects of development:

## 1. BUILD.md — Phase 1 (MVP) Backend Systems
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

## 2. BUILD-GAMEPLAY.md — Gameplay Loop & UI Development
**Focus:** Making the game actually playable with UI and player experience  
**Status:** 📋 Planning (0% complete)  
**Audience:** UI/UX developers, game designers, frontend developers

**Current Gap:** Game has complete backend but NO gameplay interface:
- ❌ No character creation UI
- ❌ No movement/exploration interface
- ❌ No combat UI
- ❌ No inventory management
- ❌ No quest system UI
- ❌ No multiplayer client

**Phases Defined:**
- G.1 Core Gameplay Loop (character creation, movement, interaction)
- G.2 Combat & Survival (combat UI, health/mana, death/respawn)
- G.3 Inventory & Crafting UI (inventory, equipment, crafting interface)
- G.4 Social & Trading UI (trading, clans, chat)
- G.5 Quest & Story UI (quest log, dialog, events)
- G.6 World Map & Navigation (zoomable map, waypoints, fog of war)
- G.7 Settings & Customization (preferences, accessibility)
- G.8 Tutorial & Help System (onboarding, in-game help)

**Interface Options:**
- ✅ **CHOSEN: Web Client** (Browser-based, future Electron/Tauri wrapper)
  - React/Vue.js frontend with HTML5 Canvas/WebGL rendering
  - Spring Boot WebSocket backend (extends Phase 1.9)
  - Progressive Web App (PWA) support
  - Future: Electron or Tauri wrapper for dedicated desktop app
- Option A: Rich Terminal UI (Lanterna, JCurses) — Deferred
- Option B: JavaFX GUI (desktop application) — Deferred
- Option D: Hybrid (text commands + GUI overlay) — Possible web client enhancement

## 3. BUILD_PHASE2.md — Advanced Systems (Post-MVP)
**Focus:** Depth, polish, and advanced features after MVP is playable  
**Status:** 📋 Planning (0% complete)  
**Audience:** All developers, content creators

**Phases Defined:**
- 2.1 Magic System (runes, spellcrafting, mana, backlash)
- 2.2 Advanced Diplomacy (secret agendas, crises, influence)
- 2.3 Crafting Proficiency (XP curves, specializations, mastery)
- 2.4 Legacy & Evolution (item/structure evolution, history)
- 2.5 Dynamic Economy (supply/demand, trade routes, markets)
- 2.6 Advanced NPC AI (pathfinding, behavior trees, learning)
- 2.7 Event Propagation Enhancement (decay, saturation, chains)
- 2.8 Performance Optimization (scaling, memory, rendering)
- 2.9 Modding Support (mod framework, tools, sandbox)
- 2.10 Content Creation (quests, NPCs, items, balancing)

**Priority Levels:**
- ⭐ High Priority: Magic, diplomacy, crafting, legacy, economy
- 📘 Medium Priority: NPC AI, events, performance, modding, content

## Development Flow

### Typical Development Path:
1. **Phase 1 (BUILD.md)** — Build backend systems ✅ COMPLETE
2. **Gameplay (BUILD-GAMEPLAY.md)** — Make it playable ⏳ NEXT
3. **Phase 2 (BUILD_PHASE2.md)** — Add depth and polish ⏳ FUTURE

### Current State:
- **Where We Are:** Phase 1 complete, deployment ready
- **What We Have:** Fully functional backend with 534 tests
- **What's Missing:** Actual gameplay interface (UI)
- **Next Step:** Choose interface option and start BUILD-GAMEPLAY.md Phase G.1

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

### Use BUILD.md When:
- Implementing core backend systems
- Working on world generation, simulation, persistence
- Setting up deployment infrastructure
- Reviewing Phase 1 architecture

### Use BUILD-GAMEPLAY.md When:
- Creating user interfaces (TUI, GUI, web)
- Implementing gameplay loops (movement, combat, crafting UI)
- Working on player experience (tutorials, help, settings)
- Making the game actually playable

### Use BUILD_PHASE2.md When:
- Phase 1 complete AND gameplay MVP playable
- Adding advanced features (magic, advanced diplomacy)
- Optimizing performance for scale
- Building modding tools
- Creating content (quests, NPCs, items)

## Quick Reference

### Documentation Hierarchy:
```
BUILD.md (Phase 1)          ← Backend foundation
├─ Phase 1.1-1.10 complete
└─ 534 tests, 70%+ coverage

BUILD-GAMEPLAY.md           ← Player experience
├─ Phase G.1-G.8 planned
└─ 0% complete (no UI yet)

BUILD_PHASE2.md (Phase 2)   ← Advanced systems
├─ Phase 2.1-2.10 planned
└─ 0% complete (post-MVP)
```

### File Locations:
- `BUILD.md` — Root directory
- `BUILD-GAMEPLAY.md` — Root directory
- `BUILD_PHASE2.md` — Root directory
- All reference `docs/` for design specs
- All reference `BUILD.md` for Phase 1 foundations

## Important Notes for Copilot

1. **Phase 1 is complete** — Don't suggest redoing Phase 1 work
2. **Game is NOT playable yet** — Despite complete backend, there's no UI
3. **Gameplay development is the gap** — Focus on BUILD-GAMEPLAY.md next
4. **All three guides have same rigor** — Quality gates, testing, documentation
5. **Guides reference each other** — They're designed to work together
6. **Follow the structure** — When implementing, match the guide's format

## Next Steps Guidance

### If User Asks About:
- **"What should I build next?"** → Point to BUILD-GAMEPLAY.md Phase G.1
- **"How do I make the game playable?"** → BUILD-GAMEPLAY.md
- **"When do we add magic system?"** → BUILD_PHASE2.md Phase 2.1 (after gameplay MVP)
- **"How's the backend?"** → BUILD.md (100% complete)
- **"What UI options do we have?"** → BUILD-GAMEPLAY.md interface options

### Recommended Next Action:
1. Review BUILD-GAMEPLAY.md web client architecture section
2. Set up frontend project structure (`client/` directory with React/Vue)
3. Extend Phase 1.9 server with WebSocket and REST API controllers
4. Start Phase G.1: Core Gameplay Loop (web-based character creation)
5. Implement basic movement and map rendering (Canvas/WebGL)
6. Test with real players in browser
7. Iterate based on feedback
8. Later: Wrap in Electron/Tauri for dedicated desktop browser

**Web Client Benefits:**
- ✅ Cross-platform by default (works on any device with browser)
- ✅ No installation needed (just visit URL)
- ✅ Perfect fit for authoritative server (Phase 1.9 already complete)
- ✅ Can be wrapped in Electron/Tauri later for desktop experience
- ✅ Progressive Web App support for "install" experience
- ✅ Modern UX with rich JavaScript ecosystem

---

**Last Updated:** November 13, 2025  
**Status:** All three build guides created and cross-referenced
