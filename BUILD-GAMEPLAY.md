# !Adventure — Gameplay & User Interface Build Guide

**Version:** 0.2.0-SNAPSHOT  
**Last Updated:** November 13, 2025  
**Status:** Gameplay Development Phase — UI & Player Experience

---

## Overview

This document is the **dedicated guide** for building the gameplay loop and user interface for !Adventure. While BUILD_PHASE1.md covers backend systems, this guide focuses on making the game **actually playable** with rich UI, intuitive controls, and engaging player experiences.

### Quick Links
- [Grand Plan & MVP Matrix](docs/grand_plan.md) — Strategic roadmap and feature prioritization
- [Architecture Design](docs/architecture_design.md) — Technical architecture and system contracts
- [Testing Plan](docs/testing_plan.md) — Test framework, coverage goals, and determinism checks
- [Main Build Guide](BUILD_PHASE1.md) — Backend systems development (Phase 1 complete)
- [Phase 2 Build Guide](BUILD_PHASE2.md) — Advanced systems development

---

## Web Client Architecture

### Project Structure
```
!Adventure/
├── src/                          # Java backend (existing)
│   └── main/java/org/adventure/
│       ├── network/              # Phase 1.9 server (extend for WebSocket)
│       └── api/                  # NEW: REST API controllers
├── client/                       # NEW: Web frontend (separate npm project)
│   ├── public/                   # Static assets
│   ├── src/
│   │   ├── components/           # React/Vue components
│   │   ├── services/             # API and WebSocket clients
│   │   ├── store/                # State management
│   │   ├── views/                # Page components
│   │   └── utils/                # Helpers
│   ├── package.json
│   └── vite.config.js
├── deployment/
│   ├── Dockerfile                # Multi-stage: backend + frontend
│   └── nginx.conf                # Nginx config for serving frontend
└── pom.xml                       # Backend dependencies
```

### Communication Flow
```
Player Browser
    │
    ├─> HTTP/REST API ─────────> Spring Boot Controller
    │   (Actions: login, create character, craft item)
    │                                  │
    │                                  ▼
    │                          Game Logic (existing Phase 1)
    │                                  │
    │                                  ▼
    └─> WebSocket ◄────────────> WebSocket Handler
        (Real-time: movement, combat, chat, events)
```

### Backend Extensions Needed

**1. WebSocket Configuration** (extend Phase 1.9 server)
```java
// src/main/java/org/adventure/network/WebSocketConfig.java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // Server -> Client
        config.setApplicationDestinationPrefixes("/app"); // Client -> Server
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }
}
```

**2. REST API Controllers** (new package: `org.adventure.api`)
```java
// src/main/java/org/adventure/api/CharacterController.java
@RestController
@RequestMapping("/api/characters")
public class CharacterController {
    @PostMapping("/create")
    public ResponseEntity<Character> createCharacter(@RequestBody CharacterDTO dto);
    
    @GetMapping("/{id}")
    public ResponseEntity<Character> getCharacter(@PathVariable String id);
    
    @PutMapping("/{id}/move")
    public ResponseEntity<Void> moveCharacter(@PathVariable String id, @RequestBody MoveDTO dto);
}

// Similar controllers for:
// - GameController (world info, regions)
// - ItemController (inventory, crafting)
// - ClanController (societies, diplomacy)
// - QuestController (quests, stories)
```

**3. WebSocket Message Handlers**
```java
// src/main/java/org/adventure/network/GameWebSocketHandler.java
@Controller
public class GameWebSocketHandler {
    @MessageMapping("/game/move")
    @SendTo("/topic/region/{regionId}")
    public RegionUpdateMessage handleMove(MoveMessage msg) {
        // Process movement, broadcast to all in region
    }
    
    @MessageMapping("/game/chat")
    @SendTo("/topic/chat/{channel}")
    public ChatMessage handleChat(ChatMessage msg) {
        // Broadcast chat to channel subscribers
    }
}
```

### Frontend Structure (React Example)

**Directory Layout:**
```
client/src/
├── components/
│   ├── CharacterSheet.jsx        # Display stats, inventory
│   ├── WorldMap.jsx              # Canvas-based map
│   ├── ChatBox.jsx               # Multiplayer chat
│   ├── InventoryPanel.jsx        # Item management
│   ├── CraftingInterface.jsx     # Crafting UI
│   └── QuestLog.jsx              # Quest tracking
├── services/
│   ├── api.js                    # REST API client (axios)
│   ├── websocket.js              # WebSocket client (STOMP)
│   └── gameState.js              # Local state sync
├── store/
│   ├── characterStore.js         # Character state (Zustand/Pinia)
│   ├── worldStore.js             # World/region state
│   └── uiStore.js                # UI state (menus, modals)
├── views/
│   ├── LoginView.jsx             # Login/register
│   ├── CharacterCreation.jsx     # Character creation
│   ├── GameView.jsx              # Main game screen
│   └── SettingsView.jsx          # Settings
└── App.jsx                       # Main app component
```

**WebSocket Service Example:**
```javascript
// client/src/services/websocket.js
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class GameWebSocket {
    constructor() {
        this.client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            onConnect: () => this.onConnected(),
            onDisconnect: () => this.onDisconnected(),
        });
    }
    
    connect() {
        this.client.activate();
    }
    
    subscribe(topic, callback) {
        return this.client.subscribe(topic, callback);
    }
    
    send(destination, body) {
        this.client.publish({ destination, body: JSON.stringify(body) });
    }
}

export default new GameWebSocket();
```

### Deployment (Docker Multi-Stage Build)

**Updated Dockerfile:**
```dockerfile
# Stage 1: Build frontend
FROM node:20 AS frontend-build
WORKDIR /app/client
COPY client/package*.json ./
RUN npm ci
COPY client/ ./
RUN npm run build
# Output: /app/client/dist

# Stage 2: Build backend (existing)
FROM eclipse-temurin:21-jdk AS backend-build
WORKDIR /app
COPY maven/ ./maven/
COPY pom.xml ./
COPY src/ ./src/
RUN ./maven/mvn/bin/mvn clean package -DskipTests
# Output: /app/target/adventure-0.1.0-SNAPSHOT.jar

# Stage 3: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy backend JAR
COPY --from=backend-build /app/target/adventure-0.1.0-SNAPSHOT.jar ./adventure.jar

# Copy frontend static files
COPY --from=frontend-build /app/client/dist ./static

# Expose ports
EXPOSE 8080

# Run backend (serves frontend from /static)
ENTRYPOINT ["java", "-jar", "adventure.jar"]
```

**Spring Boot Static Resource Config:**
```java
// src/main/java/org/adventure/config/StaticResourceConfig.java
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "file:./static/")
                .setCachePeriod(3600);
    }
}
```

### Development Workflow

**Local Development (Two Processes):**
```bash
# Terminal 1: Backend (with hot reload)
.\maven\mvn\bin\mvn.cmd spring-boot:run

# Terminal 2: Frontend (with hot reload)
cd client
npm run dev
# Vite dev server on http://localhost:5173
# Proxies API requests to http://localhost:8080
```

**vite.config.js Proxy:**
```javascript
export default {
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/ws': {
        target: 'http://localhost:8080',
        ws: true
      }
    }
  }
}
```

**Production Build:**
```bash
# Build everything
.\deployment\deploy.ps1 -BuildDocker

# Or manual
cd client && npm run build && cd ..
.\maven\mvn\bin\mvn.cmd clean package
docker build -f deployment/Dockerfile -t adventure:latest .
```

### Future: Dedicated Desktop Browser

**Electron Wrapper (Chromium-based):**
```javascript
// client/electron/main.js
const { app, BrowserWindow } = require('electron');

app.whenReady().then(() => {
    const win = new BrowserWindow({
        width: 1280,
        height: 720,
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true
        }
    });
    
    // Load production build or connect to server
    win.loadURL(process.env.GAME_URL || 'http://localhost:8080');
});
```

**Benefits of Electron Wrapper:**
- ✅ Offline mode (bundle server with client)
- ✅ Native OS integration (notifications, file system)
- ✅ Auto-updates via electron-updater
- ✅ Better performance (no browser overhead)
- ✅ Custom window decorations and controls

**Tauri Wrapper (Lighter Alternative):**
```rust
// src-tauri/src/main.rs
fn main() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
```

**Benefits of Tauri:**
- ✅ Smaller bundle size (3-5 MB vs 100+ MB Electron)
- ✅ Uses system WebView (less RAM)
- ✅ Rust backend (safe, fast)
- ✅ Cross-platform (Windows, macOS, Linux)

---

## Prerequisites

### Required Tools
- **Java Development Kit (JDK):** Version 21 LTS (installed and verified)
  - Check: `java -version` should report `21.0.x`
- **Build Tool:** Maven 3.8.9+ (bundled in `maven/mvn/bin/`)
  - Check: `.\maven\mvn\bin\mvn.cmd -v` (Windows) or `./maven/mvn/bin/mvn -version` (Linux/macOS)
- **Git:** For version control and CI integration
- **IDE/Editor:** IntelliJ IDEA, VS Code with Java extensions, or Eclipse

### Optional Tools (for UI development)
- **UI Framework:** JavaFX (for GUI client) or terminal libraries for enhanced TUI
- **Asset Tools:** Image editors for icons, maps, sprites (if adding visual elements)
- **Profiler:** JProfiler, YourKit, or VisualVM for performance tuning
- **Test Automation:** Selenium/TestFX for UI testing

---

## Current State Assessment

### ✅ What We Have (Phase 1 Complete)
**Backend Infrastructure (100% Complete):**
- ✅ World generation system (plates, biomes, rivers, regional features)
- ✅ Region simulation engine (tick-based, active/background)
- ✅ Character system (stats, traits, skills, races)
- ✅ Items & crafting (24 categories, recipes, durability)
- ✅ Structures & ownership (buildings, taxation, transfers)
- ✅ Societies & clans (membership, diplomacy, treasury)
- ✅ Stories & events (propagation, saturation)
- ✅ Persistence (save/load, backup rotation)
- ✅ Multiplayer networking (authoritative server, auth)
- ✅ CI/CD & deployment infrastructure

**Current Entry Points:**
- `org.adventure.Game` — ASCII world viewer prototype only
  - Displays world map with biome colors
  - No player movement, combat, or interaction
  - No multiplayer client implementation
- `org.adventure.network.Server` — Multiplayer server (no client UI)

### ❌ What We're Missing (Gameplay Gap)
**No Actual Gameplay Loop:**
- ❌ No player character creation UI
- ❌ No movement/exploration interface
- ❌ No combat system UI
- ❌ No inventory management interface
- ❌ No crafting interface
- ❌ No NPC interaction dialogs
- ❌ No quest/story UI
- ❌ No multiplayer client with proper command interface
- ❌ No help system or tutorials

---

## Development Philosophy

### Design Principles
1. **Player-First:** Every feature must enhance player experience
2. **Accessibility:** Support text-based, GUI, and hybrid interfaces
3. **Gradual Learning Curve:** Tutorial → Basic gameplay → Advanced features
4. **Responsive Feedback:** Immediate visual/text feedback for all actions
5. **Scalable Complexity:** Simple for beginners, deep for veterans

### Interface Options (Choose One or Support Multiple)

**✅ CHOSEN: Option C - Web Client** (Browser-based with future dedicated browser wrapper)

**Primary Implementation: Web Client**
- **Frontend:** React or Vue.js (modern JavaScript framework)
- **Backend:** Spring Boot WebSocket server (extends existing Phase 1.9 server)
- **Rendering:** HTML5 Canvas or WebGL for map/graphics
- **Communication:** WebSocket for real-time updates, REST API for actions
- **Deployment:** Static files + Java backend in Docker container
- **Future:** Wrap in Electron/Tauri for "dedicated browser" desktop app

**Why Web Client is Optimal:**
- ✅ Cross-platform by default (Windows, Linux, macOS, mobile)
- ✅ No installation required (just visit URL and play)
- ✅ Easy updates (server-side, no client downloads)
- ✅ Perfect fit for authoritative server architecture (Phase 1.9)
- ✅ Can be wrapped in Electron/Tauri later for desktop app
- ✅ Progressive Web App (PWA) support for "install" experience
- ✅ Modern UX with rich JavaScript ecosystem
- ✅ Mobile-friendly with responsive design

**Alternative Options (Deferred):**
- **Option A: Rich Terminal UI (TUI)** — Enhanced text interface with colors, menus, panels
  - Pros: Fast, accessible, retro aesthetic
  - Tools: Lanterna (Java), JCurses, or custom ANSI rendering
  - Status: Defer to post-MVP or community mod
- **Option B: JavaFX GUI** — Full graphical interface with windows, buttons, maps
  - Pros: Modern, intuitive, supports images/animations
  - Tools: JavaFX 21+, Scene Builder
  - Status: Defer to post-MVP or dedicated desktop client
- **Option D: Hybrid Approach** — Text commands with optional GUI overlay
  - Pros: Combines accessibility and visual appeal
  - Tools: Web client with terminal emulator component
  - Status: Possible enhancement to web client

---

## Gameplay Development Phases

### Phase G.1: Core Gameplay Loop (Essential ✅ 0% Complete)

**Goal:** Implement the fundamental gameplay cycle: create character → explore → interact → survive.

**Deliverables:**
- [ ] **Character Creation UI**
  - [ ] Race selection (8 races from Phase 1.3)
  - [ ] Initial stat allocation (point-buy or preset)
  - [ ] Starting trait selection (3 traits maximum)
  - [ ] Name entry and validation
  - [ ] Starting region selection or randomization
  - [ ] Visual character summary before confirmation
  - [ ] Save created character to persistence layer

- [ ] **Movement & Exploration Interface**
  - [ ] Cardinal direction movement (N/S/E/W or arrow keys)
  - [ ] Diagonal movement (NE/NW/SE/SW)
  - [ ] Map view showing current region (ASCII or tile-based)
  - [ ] Minimap showing nearby regions
  - [ ] Current location display (coordinates, biome, region name)
  - [ ] Movement cost and stamina system
  - [ ] Region transition animations/notifications
  - [ ] Fog of war for unexplored areas
  - [ ] Fast travel to discovered locations

- [ ] **Basic Interaction System**
  - [ ] Context-sensitive action menu ("what can I do here?")
  - [ ] Resource harvesting interface (gather wood, ore, herbs)
  - [ ] NPC interaction prompt ("talk to", "trade with", "fight")
  - [ ] Structure interaction (enter building, view ownership)
  - [ ] Item pickup/drop from ground
  - [ ] Examine/inspect objects and NPCs
  - [ ] Quick action hotkeys (configurable keybindings)

- [ ] **Status & Information Displays**
  - [ ] Character stat panel (health, mana, stats, level)
  - [ ] Inventory panel (items, weight, equipment)
  - [ ] Quest/objective tracker
  - [ ] Message log/combat log (scrollable history)
  - [ ] Current time and weather (if implemented)
  - [ ] Reputation/faction standings
  - [ ] Active effects/buffs/debuffs display

- [ ] **Save/Load UI** (ADDED: Critical Missing Feature)
  - [ ] Save game menu with named save slots
  - [ ] Quick save hotkey (F5 or configurable)
  - [ ] Character selection screen (list all characters)
  - [ ] Save file management (rename, delete, backup)
  - [ ] Autosave indicator and settings
  - [ ] Load game preview (character level, location, playtime)
  - [ ] Corruption detection and backup restoration
  - [ ] Integration with Phase 1.11.4 SaveLoadManager backend

**Quality Gates:**
- ✅ **Playable Loop:** Player can create character, move around world, gather resources, and save progress
- ✅ **No Crashes:** 1 hour of continuous play without crashes or freezes
- ✅ **Response Time:** All UI actions respond in <100ms (95th percentile)
- ✅ **Accessibility:** All core actions accessible via keyboard shortcuts
- ✅ **Tutorial:** First-time player can complete tutorial without external help
- ✅ **Save Reliability:** Save/load works 100% of the time, no data loss
- ✅ **Error Handling:** All user errors (invalid input, network failure) show clear error messages (ADDED)

**Error Handling Guidelines (ADDED):**
- **Graceful Degradation:** Network errors don't crash game; show "Reconnecting..." message
- **Input Validation:** Reject invalid inputs with helpful messages ("Name must be 3-20 characters")
- **State Recovery:** Game can recover from unexpected states (stuck in combat, corrupted UI)
- **User Feedback:** All errors explained in plain language, no stack traces shown to players
- **Logging:** All errors logged to file for debugging (see Phase 2.10.2 Error Logging)

**Commands:**
```bash
# Run gameplay client (TUI or GUI depending on implementation)
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.GameplayClient

# Run with debug mode (show additional UI diagnostics)
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.GameplayClient --debug

# Connect to multiplayer server
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.GameplayClient --server localhost:8080

# Run gameplay integration tests
.\maven\mvn\bin\mvn.cmd test -Dtest=GameplayIntegrationTest
```

**Test Coverage:**
- Unit tests for UI components (character creation validation, movement logic)
- Integration tests for full gameplay loops (create → move → interact → save → load)
- UI automation tests (if using TestFX or similar)
- Accessibility tests (keyboard-only navigation)

**References:**
- Design: `docs/characters_stats_traits_skills.md` → Character Creation Rules
- Design: `docs/architecture_design.md` → Player Gameplay Flow
- Specs: `docs/specs_summary.md` → Movement Costs, Interaction Rules

---

### Phase G.2: Combat & Survival (Essential ✅ 0% Complete)

**Goal:** Implement engaging combat mechanics and survival systems.

**Deliverables:**
- [ ] **Combat System UI**
  - [ ] Turn-based or real-time combat mode selection
  - [ ] Attack/defend/flee action buttons or commands
  - [ ] Skill/spell selection during combat
  - [ ] Target selection (single, AoE)
  - [ ] Damage numbers and combat feedback
  - [ ] Hit/miss/critical visual indicators
  - [ ] Combat log with detailed action history
  - [ ] Health/mana bars for player and enemies
  - [ ] Status effects display during combat
  - [ ] Combat victory/defeat screens
  - [ ] Experience and loot distribution UI

- [ ] **Survival Mechanics**
  - [ ] Hunger/thirst/fatigue meters (if implemented)
  - [ ] Rest/sleep interface (campfire, beds in structures)
  - [ ] Food/water consumption UI
  - [ ] Healing item usage (potions, bandages)
  - [ ] Death/respawn system (corpse recovery, penalties)
  - [ ] Safe zone indicators (towns, player structures)
  - [ ] Environmental hazards display (temperature, radiation, magic)

- [ ] **Enemy AI Visualization**
  - [ ] Enemy detection radius indicators
  - [ ] Aggro/threat level displays
  - [ ] Enemy movement patterns (patrol routes)
  - [ ] Boss encounter special UI (health bars, phases)
  - [ ] Flee/pursue behavior feedback

**Quality Gates:**
- ✅ **Combat Balance:** Player can defeat enemies of equal level 60-70% of the time
- ✅ **Fair Death:** Player deaths feel fair; clear indicators before lethal situations
- ✅ **Skill Expression:** Player skill matters; combat not purely stat-based
- ✅ **Performance:** Combat animations/updates run at 30+ FPS
- ✅ **Clarity:** Combat log clearly explains what happened and why

**Commands:**
```bash
# Run combat simulation tests
.\maven\mvn\bin\mvn.cmd test -Dtest=CombatSystemTest

# Test combat balance with AI opponents
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.tools.CombatBalanceTester

# Run survival mechanics tests
.\maven\mvn\bin\mvn.cmd test -Dtest=SurvivalSystemTest
```

**References:**
- Design: `docs/characters_stats_traits_skills.md` → Combat Stats & Formulas
- Design: Create new `docs/combat_system.md` for detailed combat mechanics
- Specs: `docs/specs_summary.md` → Damage Formulas, Status Effects

---

### Phase G.3: Inventory & Crafting UI (Essential ✅ 0% Complete)

**Goal:** Make item management and crafting intuitive and enjoyable.

**Deliverables:**
- [ ] **Inventory Management Interface**
  - [ ] Grid-based or list-based inventory view
  - [ ] Item categories/tabs (weapons, armor, consumables, materials)
  - [ ] Drag-and-drop item organization (if GUI)
  - [ ] Item tooltips (stats, durability, lore)
  - [ ] Quick-equip slots (weapon, armor, accessories)
  - [ ] Weight/capacity indicators
  - [ ] Item sorting options (name, type, value, weight)
  - [ ] Search/filter functionality for large inventories
  - [ ] Item comparison tool (compare two items side-by-side)
  - [ ] Bulk actions (drop all, sell all junk)

- [ ] **Crafting Interface**
  - [ ] Recipe discovery system (unlock recipes via skills/exploration)
  - [ ] Recipe book/catalog with search
  - [ ] Crafting preview (materials needed, success chance, output)
  - [ ] Material availability indicators (have/need)
  - [ ] Crafting station requirements (anvil, alchemy lab)
  - [ ] Craft progress bar and time estimate
  - [ ] Success/failure feedback with detailed reasons
  - [ ] Bulk crafting queue (craft 10x arrows)
  - [ ] Proficiency XP gain display
  - [ ] Quality preview (Flawed to Masterwork)

- [ ] **Equipment Management**
  - [ ] Paper doll or equipment slots display
  - [ ] Stat comparison when equipping (green up arrows, red down arrows)
  - [ ] Set bonuses display (if implemented)
  - [ ] Durability warnings (item about to break)
  - [ ] Repair interface (select item, pay materials/gold)
  - [ ] Enchanting/upgrading interface (if magic items)

**Quality Gates:**
- ✅ **Usability:** New player can craft their first item in <2 minutes without tutorial
- ✅ **Clarity:** All recipe requirements clearly visible before attempting craft
- ✅ **No Frustration:** Failed crafts don't consume all materials (partial refund or guaranteed success option)
- ✅ **Performance:** Inventory with 1000+ items loads in <1 second
- ✅ **Accessibility:** All inventory actions keyboard-accessible

**Commands:**
```bash
# Run inventory UI tests
.\maven\mvn\bin\mvn.cmd test -Dtest=InventoryUITest

# Test crafting system integration
.\maven\mvn\bin\mvn.cmd test -Dtest=CraftingUITest

# Launch standalone inventory/crafting UI test harness
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.tools.InventoryTestHarness
```

**References:**
- Design: `docs/objects_crafting_legacy.md` → Item Categories, Crafting Recipes
- Implementation: Phase 1.4 backend already complete
- Specs: `docs/specs_summary.md` → Crafting Proficiency, Durability

---

### Phase G.4: Social & Trading UI (Important ⏳ 0% Complete)

**Goal:** Enable player-to-player interaction, trading, and clan management.

**Deliverables:**
- [ ] **Trading Interface**
  - [ ] Player-to-player trade window (offer/accept/decline)
  - [ ] NPC merchant UI (buy/sell, haggling if implemented)
  - [ ] Price display with value comparisons
  - [ ] Trade history/receipt
  - [ ] Safe trade confirmation (prevent scams)
  - [ ] Auction house/marketplace UI (if implemented)
  - [ ] Trade chat or message system

- [ ] **Clan/Society Management UI**
  - [ ] Clan roster (member list, roles, status)
  - [ ] Clan treasury management (deposit/withdraw, history)
  - [ ] Clan creation/joining interface
  - [ ] Diplomacy panel (reputation with other clans)
  - [ ] Alliance/war declaration UI
  - [ ] Clan message board or announcements
  - [ ] Clan permissions management (rank-based)

- [ ] **Social Features**
  - [ ] Player list (who's online, location if visible)
  - [ ] Friend list and party system
  - [ ] Private messaging (whispers)
  - [ ] Public chat channels (global, region, clan)
  - [ ] Emotes and social actions
  - [ ] Player profiles (stats, achievements, bio)

**Quality Gates:**
- ✅ **Trade Safety:** No item duplication exploits; all trades logged
- ✅ **Clan Usability:** Clan leader can manage 50+ members without confusion
- ✅ **Chat Performance:** 100+ concurrent messages/second without lag
- ✅ **Moderation:** Chat filtering and player reporting tools functional

**Commands:**
```bash
# Run social features tests
.\maven\mvn\bin\mvn.cmd test -Dtest=SocialSystemTest

# Test trading system
.\maven\mvn\bin\mvn.cmd test -Dtest=TradingSystemTest

# Launch clan management UI test
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.tools.ClanManagementTester
```

**References:**
- Design: `docs/societies_clans_kingdoms.md` → Clan Mechanics
- Implementation: Phase 1.6 backend complete
- Design: Create new `docs/trading_system.md` for economy UI

---

### Phase G.5: Quest & Story UI (Important ⏳ 0% Complete)

**Goal:** Surface the rich story system with engaging quest interfaces.

**Deliverables:**
- [ ] **Quest Log Interface**
  - [ ] Active quests panel (main quest, side quests)
  - [ ] Quest categories (story, exploration, combat, crafting)
  - [ ] Objective tracking with progress bars
  - [ ] Quest rewards preview (XP, items, reputation)
  - [ ] Quest journal with lore text
  - [ ] Map markers for quest objectives
  - [ ] Completed quests archive
  - [ ] Recommended level/difficulty indicators

- [ ] **Story Presentation**
  - [ ] Dialog system with NPC portraits (if GUI)
  - [ ] Choice-based dialog trees (branching conversations)
  - [ ] Story cutscenes or narrative text displays
  - [ ] Event notifications ("A story has begun...")
  - [ ] Rumors and legends panel (discovered stories)
  - [ ] Story progression tracking (chapters, milestones)
  - [ ] Lore codex (discovered world history, characters)

- [ ] **Event System UI**
  - [ ] World event announcements (server-wide)
  - [ ] Regional event notifications
  - [ ] Event participation UI (contribute resources, fight waves)
  - [ ] Event leaderboards (if competitive)
  - [ ] Event rewards claim interface

**Quality Gates:**
- ✅ **Engagement:** 70%+ of players complete at least one quest
- ✅ **Clarity:** Quest objectives never ambiguous; always clear next step
- ✅ **Pacing:** Quests don't feel grindy; mix of short/medium/long quests
- ✅ **Variety:** At least 5 different quest types (kill, gather, escort, puzzle, story)

**Commands:**
```bash
# Run quest system tests
.\maven\mvn\bin\mvn.cmd test -Dtest=QuestSystemTest

# Test story propagation UI
.\maven\mvn\bin\mvn.cmd test -Dtest=StoryPresentationTest

# Launch quest editor tool (for content creators)
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.tools.QuestEditor
```

**References:**
- Design: `docs/stories_events.md` → Story Types, Event Triggers
- Implementation: Phase 1.7 backend complete
- Design: Create new `docs/quest_system.md` for quest mechanics

---

### Phase G.6: World Map & Navigation (Important ⏳ 0% Complete)

**Goal:** Help players understand and navigate the vast procedural world.

**Deliverables:**
- [ ] **World Map Interface**
  - [ ] Zoomable world map (overview to region detail)
  - [ ] Biome color coding and legend
  - [ ] Discovered vs unexplored areas (fog of war)
  - [ ] Player position marker
  - [ ] Points of interest markers (cities, dungeons, resources)
  - [ ] Region boundaries and names
  - [ ] Distance measurement tool
  - [ ] Path planning/routing suggestions
  - [ ] Bookmark/favorite locations
  - [ ] Map notes and annotations

- [ ] **Navigation Aids**
  - [ ] Compass indicator (direction to objectives)
  - [ ] Waypoint system (set destination, follow arrow)
  - [ ] Breadcrumb trail (recent movement history)
  - [ ] Road/path highlighting (easier travel routes)
  - [ ] Danger zone warnings (high-level areas)
  - [ ] Fast travel network (discovered teleport points)

- [ ] **Region Information Panel**
  - [ ] Current region statistics (level, resources, NPCs)
  - [ ] Ownership information (who controls this region)
  - [ ] Historical events (what happened here)
  - [ ] Climate and terrain details
  - [ ] Notable NPCs or structures
  - [ ] Recommended activities (what to do here)

**Quality Gates:**
- ✅ **No Getting Lost:** Players can always find their way back to safety
- ✅ **Performance:** 512x512 world map renders in <2 seconds
- ✅ **Clarity:** Biome colors distinct and colorblind-friendly
- ✅ **Utility:** Map used by 90%+ of players regularly

**Commands:**
```bash
# Run map rendering tests
.\maven\mvn\bin\mvn.cmd test -Dtest=MapRenderingTest

# Test navigation system
.\maven\mvn\bin\mvn.cmd test -Dtest=NavigationSystemTest

# Launch map viewer tool
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.tools.MapViewer --world test_world.json
```

**References:**
- Design: `docs/world_generation.md` → Biomes, Regional Features
- Design: `docs/biomes_geography.md` → Biome Characteristics
- Implementation: Phase 1.1 world generation complete

---

### Phase G.7: Settings & Customization (Nice to Have 🌟 0% Complete)

**Goal:** Let players tailor the game experience to their preferences.

**Deliverables:**
- [ ] **Gameplay Settings**
  - [ ] Difficulty presets (Easy/Normal/Hard/Custom)
  - [ ] Combat speed/turn timer adjustments
  - [ ] Auto-save frequency
  - [ ] Permadeath toggle (hardcore mode)
  - [ ] XP gain multipliers
  - [ ] Resource abundance sliders

- [ ] **UI/UX Settings**
  - [ ] Theme selection (light/dark/custom colors)
  - [ ] Font size and style options
  - [ ] UI scale (for high-DPI displays)
  - [ ] Animation speed toggles
  - [ ] Colorblind mode options
  - [ ] Screen reader compatibility
  - [ ] Custom keybindings editor
  - [ ] Message log verbosity (minimal/normal/detailed)

- [ ] **Audio Settings** (if audio implemented)
  - [ ] Master volume control
  - [ ] Music/SFX/voice volume sliders
  - [ ] Mute specific sound types
  - [ ] Audio device selection

- [ ] **Accessibility Features**
  - [ ] High contrast mode
  - [ ] Dyslexia-friendly fonts
  - [ ] Motion sickness reduction (disable camera shake)
  - [ ] One-handed mode (alternative control schemes)
  - [ ] Text-to-speech for dialogs
  - [ ] Customizable notification durations

**Quality Gates:**
- ✅ **Persistence:** All settings saved per-player across sessions
- ✅ **Defaults:** Sensible defaults work for 80%+ of players
- ✅ **Accessibility:** Game playable by users with common disabilities
- ✅ **Performance:** Settings changes apply instantly without restart (where possible)

**Commands:**
```bash
# Test settings persistence
.\maven\mvn\bin\mvn.cmd test -Dtest=SettingsSystemTest

# Launch settings editor
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.SettingsEditor
```

**References:**
- Design: Create new `docs/accessibility_guidelines.md`
- Specs: `docs/specs_summary.md` → Default Settings

---

### Phase G.8: Tutorial & Help System (Essential ✅ 0% Complete)

**Goal:** Ensure new players can learn the game without frustration.

**Deliverables:**
- [ ] **Interactive Tutorial**
  - [ ] Guided first-time experience (5-10 minutes)
  - [ ] Step-by-step character creation
  - [ ] Movement basics (WASD or arrow keys)
  - [ ] Combat basics (attack a dummy target) — NOTE: Requires Phase 1.11.2 Combat System
  - [ ] Inventory basics (pick up, equip, drop)
  - [ ] Crafting basics (craft first item) — UI connects to Phase 1.4 CraftingSystem
  - [ ] XP and leveling introduction — UI connects to Phase 1.11.1 ExperienceSystem
  - [ ] First quest completion — UI connects to Phase G.5 Quest System
  - [ ] Save/load introduction — UI connects to Phase 1.11.4 SaveLoadManager
  - [ ] Tutorial skippable for experienced players
  - [ ] Optional tutorial replay from main menu

- [ ] **In-Game Help System**
  - [ ] Context-sensitive help (F1 or "?")
  - [ ] Searchable help database
  - [ ] Tooltips on all UI elements
  - [ ] Command reference (for text interface)
  - [ ] Keybinding cheat sheet
  - [ ] FAQ section for common questions
  - [ ] Glossary of game terms
  - [ ] Gameplay tips and tricks section
  - [ ] Video tutorials (optional, links to YouTube)

- [ ] **New Player Guidance**
  - [ ] Recommended first steps checklist ("Complete tutorial", "Join a clan", "Craft your first weapon")
  - [ ] Progressive feature unlocking (don't overwhelm)
  - [ ] Tooltips for new features (first-time hints)
  - [ ] Suggested builds or playstyles ("Try a warrior build", "Focus on crafting")
  - [ ] "What to do next?" prompt when idle (idle for 30s → suggest action)
  - [ ] Community resources links (wiki, Discord, Reddit)
  - [ ] Mentor/helper NPC (in-game guide character)

- [ ] **Tutorial Content Areas (EXPANDED):**
  - [ ] **Basic Movement:** Arrow keys, WASD, mouse click (if GUI)
  - [ ] **Resource Gathering:** Find and harvest trees, ore nodes (Phase 1.2 integration)
  - [ ] **Combat Introduction:** Attack enemy dummy, use skills (Phase 1.11.2)
  - [ ] **Crafting Your First Item:** Craft a simple weapon or tool (Phase 1.4)
  - [ ] **Understanding Stats:** Explain strength, dexterity, vitality, intelligence (Phase 1.3)
  - [ ] **Inventory Management:** Pick up, equip, drop items (Phase G.3)
  - [ ] **Experience and Leveling:** Gain XP, level up, allocate stat points (Phase 1.11.1)
  - [ ] **Quest Basics:** Accept quest, complete objective, claim reward (Phase G.5)
  - [ ] **Clan Introduction:** Join starter clan, view treasury (Phase 1.6)
  - [ ] **Trading Basics:** Buy from NPC merchant, sell items (Phase 1.11.3)
  - [ ] **Saving Your Progress:** Manual save, autosave explanation (Phase 1.11.4)

**Quality Gates:**
- ✅ **Completion Rate:** 80%+ of new players complete tutorial
- ✅ **Time to First Success:** New player completes first quest in <15 minutes
- ✅ **Help Usage:** <20% of players need external help (wiki, forums)
- ✅ **Clarity:** Tutorial feedback shows 90%+ comprehension
- ✅ **Coverage:** Tutorial covers all Phase G.1 core mechanics
- ✅ **Accessibility:** Tutorial works with keyboard-only, screen readers

**Commands:**
```bash
# Run tutorial system tests
.\maven\mvn\bin\mvn.cmd test -Dtest=TutorialSystemTest

# Test help system search
.\maven\mvn\bin\mvn.cmd test -Dtest=HelpSystemTest

# Launch tutorial designer tool
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.tools.TutorialDesigner
```

**References:**
- Design: Create new `docs/tutorial_design.md`
- UX: User testing feedback and iteration

---

## Testing Strategy for Gameplay

### Unit Tests
- UI component validation (buttons, panels, dialogs)
- Input handling (keyboard, mouse, text commands)
- State management (game state transitions)
- Rendering logic (map generation, sprite rendering)

### Integration Tests
- Full gameplay loops (create character → play → save → load → resume)
- Multiplayer interactions (trade, combat, chat)
- Cross-system interactions (quest triggers crafting, etc.)

### User Acceptance Tests
- Playtesting with real users (internal and external)
- Usability studies (watch players, identify pain points)
- Accessibility testing (screen readers, keyboard-only, colorblind)
- Performance profiling (FPS, memory usage, load times)

### Automated UI Tests (if using GUI)
- TestFX for JavaFX UI testing
- Selenium for web client testing
- Snapshot testing for visual regressions

### Quality Metrics
- **Crash Rate:** <1 crash per 100 hours of play
- **Bug Severity:** No critical bugs, <5 major bugs at launch
- **Performance:** 60 FPS on target hardware (define minimum specs)
- **Player Satisfaction:** >4.0/5.0 in post-play surveys

---

## Build Commands (Gameplay-Specific)

### Development Workflow

```bash
# Build with gameplay modules
.\maven\mvn\bin\mvn.cmd clean package -P gameplay

# Run gameplay client (TUI mode)
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.GameplayClient --mode tui

# Run gameplay client (GUI mode)
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.GameplayClient --mode gui

# Run gameplay client (web mode)
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.GameplayClient --mode web --port 3000

# Run all gameplay tests
.\maven\mvn\bin\mvn.cmd test -Dtest=*GameplayTest,*UITest

# Run UI automation tests
.\maven\mvn\bin\mvn.cmd test -Dtest=*UIIntegrationTest

# Package gameplay client for distribution
.\maven\mvn\bin\mvn.cmd package -P gameplay-release
```

### Performance Profiling

```bash
# Run with Java Flight Recorder
java -XX:StartFlightRecording=filename=gameplay.jfr -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.GameplayClient

# Run with memory profiling
java -Xmx4G -XX:+HeapDumpOnOutOfMemoryError -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.GameplayClient

# Benchmark UI rendering
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.tools.UIBenchmark
```

---

## Dependencies & Technologies

### Web Client Stack (Chosen Implementation)

**Backend Extensions (Spring Boot WebSocket)**
```xml
<!-- Already have Spring Boot from Phase 1.9, add WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
    <version>3.2.0</version>
</dependency>

<!-- STOMP messaging protocol for WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.2.0</version>
</dependency>

<!-- JSON processing (already have Jackson from Phase 1.8) -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

**Frontend Stack (Separate Project in `client/` Directory)**

**Option 1: React + Vite (Recommended)**
```bash
# Create React app with Vite
npm create vite@latest adventure-client -- --template react

# Key dependencies (package.json)
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "sockjs-client": "^1.6.1",        // WebSocket client
    "@stomp/stompjs": "^7.0.0",       // STOMP protocol
    "axios": "^1.6.0",                // REST API calls
    "react-router-dom": "^6.20.0",    // Routing
    "zustand": "^4.4.0",              // State management (lightweight)
    "pixi.js": "^7.3.0"               // 2D rendering (optional, for maps)
  },
  "devDependencies": {
    "vite": "^5.0.0",
    "@vitejs/plugin-react": "^4.2.0"
  }
}
```

**Option 2: Vue.js 3 + Vite (Alternative)**
```bash
# Create Vue app with Vite
npm create vite@latest adventure-client -- --template vue

# Key dependencies (package.json)
{
  "dependencies": {
    "vue": "^3.3.0",
    "vue-router": "^4.2.0",           // Routing
    "pinia": "^2.1.0",                // State management
    "sockjs-client": "^1.6.1",        // WebSocket client
    "@stomp/stompjs": "^7.0.0",       // STOMP protocol
    "axios": "^1.6.0",                // REST API calls
    "pixi.js": "^7.3.0"               // 2D rendering (optional)
  }
}
```

**Map Rendering Options**
```bash
# Option A: HTML5 Canvas with PixiJS (2D, fast)
npm install pixi.js

# Option B: WebGL with Three.js (3D capable, more complex)
npm install three

# Option C: Simple SVG/DOM (lightest, good for grid-based maps)
# No additional dependencies needed
```

**Testing Frameworks (Web Client)**
```bash
# Unit testing
npm install --save-dev vitest @testing-library/react @testing-library/user-event

# E2E testing
npm install --save-dev playwright
```

### Alternative UI Frameworks (Deferred)

**Option A: Terminal UI (Lanterna)**
```xml
<dependency>
    <groupId>com.googlecode.lanterna</groupId>
    <artifactId>lanterna</artifactId>
    <version>3.1.1</version>
</dependency>
```

**Option B: JavaFX GUI**
```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21</version>
</dependency>
```

### Electron/Tauri Wrapper (Future - Dedicated Browser)

**Electron (Chromium-based, more mature)**
```bash
# In client/ directory after web client is complete
npm install --save-dev electron electron-builder

# package.json
{
  "main": "electron/main.js",
  "scripts": {
    "electron": "electron .",
    "build:electron": "electron-builder"
  }
}
```

**Tauri (Rust-based, smaller, faster)**
```bash
# Alternative to Electron (uses system WebView, smaller bundle)
npm install --save-dev @tauri-apps/cli
npm install @tauri-apps/api

# Requires Rust toolchain
# cargo install tauri-cli
```

### Testing Frameworks (Backend - Already from Phase 1)
```xml
<!-- Already have JUnit 5 from Phase 1 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.9.3</version>
    <scope>test</scope>
</dependency>

<!-- WebSocket testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <version>3.2.0</version>
    <scope>test</scope>
</dependency>
```

---

## Documentation Requirements

### Player-Facing Documentation
- [ ] Getting Started Guide (installation, first login)
- [ ] Gameplay Manual (controls, combat, crafting)
- [ ] FAQ (common questions and troubleshooting)
- [ ] Command Reference (all text commands)
- [ ] Keybinding Reference (all keyboard shortcuts)

### Developer Documentation
- [ ] UI Architecture Overview (component hierarchy)
- [ ] Adding New UI Features (templates and patterns)
- [ ] Testing UI Components (best practices)
- [ ] Performance Optimization Guide (rendering, state management)

---

## Success Criteria for Gameplay MVP

### Must Have
- ✅ Character creation works smoothly
- ✅ Player can move around world and see map
- ✅ Basic combat functional and engaging
- ✅ Inventory and crafting usable
- ✅ Save/load works from UI
- ✅ Tutorial gets players started
- ✅ 0 critical bugs, <5 major bugs

### Nice to Have
- ✅ Multiplayer chat and trading functional
- ✅ Quest system with at least 10 quests
- ✅ Map system with fog of war
- ✅ Clan management UI
- ✅ Settings and customization

### Phase Complete When
- All "Must Have" items checked off
- 100+ hours of internal playtesting completed
- User acceptance testing shows 80%+ satisfaction
- Performance meets targets on minimum spec hardware
- Documentation complete and reviewed

---

## Next Steps After Gameplay MVP

1. **Polish & Balancing** — Iterate based on playtester feedback
2. **Content Creation** — Add quests, NPCs, items, stories
3. **Advanced Features** — Magic system UI, advanced combat
4. **Modding Support** — UI for mod management and content creation
5. **Marketing & Launch** — Prepare for public release

---

**Note:** This guide is a living document. Update phases as implementation progresses and player feedback is gathered.
