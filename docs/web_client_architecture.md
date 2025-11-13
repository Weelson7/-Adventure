# Web Client Architecture - !Adventure

**Version:** 0.2.0  
**Last Updated:** November 13, 2025  
**Status:** Design Document for Gameplay Implementation

---

## Overview

This document outlines the web-based client architecture for !Adventure, chosen as the primary UI implementation. The web client leverages the existing authoritative server (Phase 1.9) and provides a modern, cross-platform player experience accessible through any browser.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Player Browser                          │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    React/Vue Frontend                     │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │  │
│  │  │Character │  │  World   │  │Inventory │  │  Quest   │  │  │
│  │  │  Sheet   │  │   Map    │  │  Panel   │  │   Log    │  │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │  │
│  │                                                            │  │
│  │  ┌──────────────────────────────────────────────────────┐ │  │
│  │  │         State Management (Zustand/Pinia)             │ │  │
│  │  └──────────────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────────┘  │
│         │                                        │               │
│         │ REST API (HTTP)                        │ WebSocket     │
│         │ (Actions)                              │ (Real-time)   │
└─────────┼────────────────────────────────────────┼───────────────┘
          │                                        │
          ▼                                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Backend Server                    │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                   API Controllers                         │  │
│  │  /api/characters   /api/items   /api/clans   /api/quests │  │
│  └───────────────────────────────────────────────────────────┘  │
│                          │                                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              WebSocket Message Handlers                   │  │
│  │  /ws/game/move   /ws/game/chat   /ws/game/combat         │  │
│  └───────────────────────────────────────────────────────────┘  │
│                          │                                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                 Game Logic Layer                          │  │
│  │  (Existing Phase 1 Backend - 100% Complete)               │  │
│  │                                                            │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │  │
│  │  │  World   │  │ Region   │  │Character │  │  Items   │  │  │
│  │  │   Gen    │  │   Sim    │  │  System  │  │ Crafting │  │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │  │
│  │                                                            │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │  │
│  │  │Structure │  │Societies │  │ Stories  │  │ Persist  │  │  │
│  │  │ Ownership│  │  Clans   │  │  Events  │  │ ence     │  │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │  │
│  └───────────────────────────────────────────────────────────┘  │
│                          │                                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              Persistence Layer (Phase 1.8)                │  │
│  │  JSON Save Files  │  Backup Rotation  │  Schema Version   │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Frontend (New Implementation)
- **Framework:** React 18+ with Vite (or Vue.js 3 alternative)
- **State Management:** Zustand (React) or Pinia (Vue)
- **Rendering:** HTML5 Canvas with PixiJS for map/sprites
- **Communication:**
  - Axios for REST API calls
  - SockJS + STOMP for WebSocket real-time updates
- **Routing:** React Router or Vue Router
- **Build Tool:** Vite (fast dev server, optimized builds)
- **Package Manager:** npm or pnpm

### Backend (Extend Phase 1.9)
- **Framework:** Spring Boot 3.2+ (already implemented)
- **WebSocket:** Spring WebSocket + STOMP messaging
- **REST API:** Spring MVC controllers
- **Authentication:** JWT tokens (already implemented in Phase 1.9)
- **JSON:** Jackson (already used in Phase 1.8)

### Deployment
- **Container:** Docker multi-stage build (frontend + backend)
- **Web Server:** Nginx for static files (or embedded in Spring Boot)
- **Orchestration:** Docker Compose (already set up in Phase 1.10)

---

## Communication Protocols

### REST API (HTTP) - For Actions
Used for **non-real-time actions** that require request/response:

**Authentication:**
```
POST   /api/auth/login
POST   /api/auth/register
POST   /api/auth/logout
GET    /api/auth/validate
```

**Character Management:**
```
POST   /api/characters/create
GET    /api/characters/{id}
PUT    /api/characters/{id}
DELETE /api/characters/{id}
GET    /api/characters/list
```

**World & Regions:**
```
GET    /api/world/info
GET    /api/regions/{id}
GET    /api/regions/{id}/npcs
GET    /api/regions/{id}/resources
```

**Inventory & Items:**
```
GET    /api/characters/{id}/inventory
POST   /api/characters/{id}/inventory/pickup
DELETE /api/characters/{id}/inventory/{itemId}
PUT    /api/characters/{id}/inventory/equip
```

**Crafting:**
```
GET    /api/crafting/recipes
POST   /api/crafting/craft
GET    /api/crafting/proficiency
```

**Societies & Clans:**
```
GET    /api/clans/list
GET    /api/clans/{id}
POST   /api/clans/create
POST   /api/clans/{id}/join
DELETE /api/clans/{id}/leave
```

**Quests & Stories:**
```
GET    /api/quests/active
GET    /api/quests/{id}
POST   /api/quests/{id}/accept
PUT    /api/quests/{id}/complete
GET    /api/stories/discovered
```

### WebSocket (STOMP) - For Real-Time Updates
Used for **real-time events** that need broadcasting:

**Movement & Location:**
```
Client -> Server:  /app/game/move
Server -> Client:  /topic/region/{regionId}/updates
```

**Combat:**
```
Client -> Server:  /app/game/combat/attack
Server -> Client:  /topic/region/{regionId}/combat
```

**Chat:**
```
Client -> Server:  /app/chat/send
Server -> Client:  /topic/chat/{channel}
                  /user/queue/messages  (private messages)
```

**Events & Notifications:**
```
Server -> Client:  /topic/events/world    (global events)
                  /topic/events/region/{id}  (regional events)
                  /user/queue/notifications  (personal notifications)
```

**Region State Sync:**
```
Server -> Client:  /topic/region/{regionId}/state
                  (NPC positions, resource updates, structure changes)
```

---

## Frontend Architecture

### Component Hierarchy
```
App
├── Router
│   ├── LoginView
│   │   └── LoginForm
│   ├── CharacterCreationView
│   │   ├── RaceSelector
│   │   ├── StatAllocator
│   │   └── TraitSelector
│   ├── GameView (Main Game Screen)
│   │   ├── TopBar
│   │   │   ├── CharacterStatus (HP, Mana, XP)
│   │   │   └── NotificationArea
│   │   ├── LeftSidebar
│   │   │   ├── CharacterSheet
│   │   │   └── InventoryPanel
│   │   ├── CenterPanel
│   │   │   ├── WorldMap (Canvas/PixiJS)
│   │   │   └── InteractionMenu
│   │   ├── RightSidebar
│   │   │   ├── QuestLog
│   │   │   ├── EventFeed
│   │   │   └── ClanPanel
│   │   └── BottomPanel
│   │       ├── ChatBox
│   │       └── ActionBar
│   └── SettingsView
│       ├── GameplaySettings
│       ├── UISettings
│       └── AccessibilitySettings
└── Modals
    ├── CraftingModal
    ├── TradingModal
    ├── NPCDialogModal
    └── ConfirmationModal
```

### State Management (Zustand Example)
```javascript
// stores/characterStore.js
import { create } from 'zustand';

const useCharacterStore = create((set) => ({
    character: null,
    stats: {},
    inventory: [],
    
    setCharacter: (char) => set({ character: char }),
    updateStats: (stats) => set({ stats }),
    addItem: (item) => set((state) => ({ 
        inventory: [...state.inventory, item] 
    })),
    removeItem: (itemId) => set((state) => ({ 
        inventory: state.inventory.filter(i => i.id !== itemId) 
    })),
}));

// stores/worldStore.js
const useWorldStore = create((set) => ({
    currentRegion: null,
    nearbyNPCs: [],
    resources: [],
    
    setRegion: (region) => set({ currentRegion: region }),
    updateNPCs: (npcs) => set({ nearbyNPCs: npcs }),
    updateResources: (resources) => set({ resources }),
}));

// stores/uiStore.js
const useUIStore = create((set) => ({
    activeModal: null,
    notifications: [],
    chatMessages: [],
    
    openModal: (modal) => set({ activeModal: modal }),
    closeModal: () => set({ activeModal: null }),
    addNotification: (notif) => set((state) => ({ 
        notifications: [...state.notifications, notif] 
    })),
    addChatMessage: (msg) => set((state) => ({ 
        chatMessages: [...state.chatMessages, msg] 
    })),
}));
```

### Services Layer
```javascript
// services/api.js - REST API client
import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add JWT token to requests
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('jwt');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const characterAPI = {
    create: (data) => api.post('/characters/create', data),
    get: (id) => api.get(`/characters/${id}`),
    move: (id, direction) => api.put(`/characters/${id}/move`, { direction }),
};

export const craftingAPI = {
    getRecipes: () => api.get('/crafting/recipes'),
    craft: (recipeId, materials) => api.post('/crafting/craft', { recipeId, materials }),
};

// services/websocket.js - WebSocket client
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class GameWebSocket {
    constructor() {
        this.client = null;
        this.subscriptions = new Map();
    }
    
    connect(token) {
        this.client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },
            onConnect: () => {
                console.log('WebSocket connected');
                this.onConnected();
            },
            onDisconnect: () => {
                console.log('WebSocket disconnected');
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame);
            },
        });
        
        this.client.activate();
    }
    
    disconnect() {
        if (this.client) {
            this.client.deactivate();
        }
    }
    
    subscribe(destination, callback) {
        const subscription = this.client.subscribe(destination, (message) => {
            const data = JSON.parse(message.body);
            callback(data);
        });
        
        this.subscriptions.set(destination, subscription);
        return subscription;
    }
    
    unsubscribe(destination) {
        const subscription = this.subscriptions.get(destination);
        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(destination);
        }
    }
    
    send(destination, body) {
        this.client.publish({
            destination,
            body: JSON.stringify(body),
        });
    }
    
    onConnected() {
        // Subscribe to personal notifications
        this.subscribe('/user/queue/notifications', (notif) => {
            useUIStore.getState().addNotification(notif);
        });
    }
}

export default new GameWebSocket();
```

---

## Backend Extensions

### WebSocket Configuration
```java
package org.adventure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for broadcasting
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for client messages
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Configure properly in production
                .withSockJS();
    }
}
```

### REST API Controller Example
```java
package org.adventure.api;

import org.adventure.character.Character;
import org.adventure.character.CharacterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/characters")
@CrossOrigin(origins = "*")  // Configure properly in production
public class CharacterController {
    
    @Autowired
    private CharacterManager characterManager;
    
    @PostMapping("/create")
    public ResponseEntity<Character> createCharacter(
            @RequestBody CharacterCreationDTO dto,
            @RequestHeader("Authorization") String token) {
        
        // Validate token (use existing Phase 1.9 auth)
        String playerId = validateToken(token);
        
        // Create character using existing Phase 1.3 system
        Character character = characterManager.createCharacter(
            playerId, 
            dto.getName(), 
            dto.getRace(), 
            dto.getStats(), 
            dto.getTraits()
        );
        
        return ResponseEntity.ok(character);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Character> getCharacter(@PathVariable String id) {
        Character character = characterManager.getCharacter(id);
        return ResponseEntity.ok(character);
    }
    
    @PutMapping("/{id}/move")
    public ResponseEntity<Void> moveCharacter(
            @PathVariable String id,
            @RequestBody MoveDTO moveData) {
        
        // Process movement (integrate with Phase 1.2 region simulation)
        characterManager.moveCharacter(id, moveData.getDirection());
        
        return ResponseEntity.ok().build();
    }
}
```

### WebSocket Message Handler Example
```java
package org.adventure.network;

import org.adventure.region.Region;
import org.adventure.region.RegionSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketHandler {
    
    @Autowired
    private RegionSimulator regionSimulator;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/game/move")
    @SendTo("/topic/region/{regionId}/updates")
    public RegionUpdateMessage handleMove(MoveMessage message) {
        // Process movement using Phase 1.2 region simulation
        Region region = regionSimulator.getRegion(message.getRegionId());
        
        // Update character position
        region.moveCharacter(message.getCharacterId(), message.getDirection());
        
        // Broadcast to all players in region
        return new RegionUpdateMessage(region.getId(), region.getState());
    }
    
    @MessageMapping("/game/chat")
    public void handleChat(ChatMessage message) {
        // Broadcast to channel
        messagingTemplate.convertAndSend(
            "/topic/chat/" + message.getChannel(), 
            message
        );
    }
    
    @MessageMapping("/game/combat/attack")
    public void handleCombatAttack(CombatMessage message) {
        // Process combat (to be implemented in Phase G.2)
        // ...
        
        // Broadcast combat results to region
        messagingTemplate.convertAndSend(
            "/topic/region/" + message.getRegionId() + "/combat",
            combatResult
        );
    }
}
```

---

## Future: Dedicated Desktop Browser

### Electron Wrapper
After the web client is stable, wrap it in Electron for a dedicated desktop experience:

**Benefits:**
- Offline mode (bundle server with client)
- Native OS integration (notifications, file system, tray icon)
- Auto-updates
- Better performance (no browser overhead)
- Custom window decorations

**Implementation:**
```bash
cd client
npm install --save-dev electron electron-builder

# Create electron/main.js
# Build with: npm run build:electron
```

### Tauri Wrapper (Alternative)
Lighter alternative to Electron:

**Benefits:**
- Smaller bundle (3-5 MB vs 100+ MB)
- Uses system WebView (less RAM)
- Rust backend (safe, fast)
- Better performance

**Implementation:**
```bash
cd client
npm install --save-dev @tauri-apps/cli
npm install @tauri-apps/api

# Requires Rust toolchain
# Build with: npm run tauri build
```

---

## Development Workflow

### Local Development
```bash
# Terminal 1: Start backend
.\maven\mvn\bin\mvn.cmd spring-boot:run

# Terminal 2: Start frontend dev server
cd client
npm run dev
# Opens http://localhost:5173 with hot reload
```

### Production Build
```bash
# Build frontend
cd client
npm run build
# Output: client/dist/

# Build backend + bundle frontend
.\maven\mvn\bin\mvn.cmd clean package
# Copies client/dist/ to src/main/resources/static/

# Or use deployment script
.\deployment\deploy.ps1 -BuildDocker
```

### Docker Deployment
```bash
# Build Docker image (multi-stage: frontend + backend)
docker build -f deployment/Dockerfile -t adventure:latest .

# Run container
docker run -d -p 8080:8080 adventure:latest

# Access game at http://localhost:8080
```

---

## Security Considerations

### Authentication
- JWT tokens (already implemented in Phase 1.9)
- HTTPS in production (TLS/SSL)
- Secure WebSocket (wss://)

### CORS Configuration
```java
@Configuration
public class WebConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",  // Dev
            "https://yourdomain.com"   // Prod
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### Input Validation
- Server-side validation for all actions (already in Phase 1.9)
- Rate limiting for API endpoints
- WebSocket message validation

---

## Performance Optimization

### Frontend
- Code splitting (lazy load routes/components)
- Canvas rendering optimization (viewport culling, sprite batching)
- WebSocket message batching (group frequent updates)
- Local state caching (reduce API calls)

### Backend
- WebSocket session pooling
- Message queue for high-volume broadcasts
- Database query optimization
- Caching layer (Redis) for frequent reads

---

## Next Steps

1. **Set up frontend project:** Create `client/` directory with Vite + React/Vue
2. **Extend backend:** Add WebSocket config and REST API controllers
3. **Implement Phase G.1:** Character creation UI (web-based)
4. **Implement Phase G.2:** Movement and map rendering (Canvas/PixiJS)
5. **Test real-time sync:** Verify WebSocket updates work correctly
6. **Iterate:** Add more gameplay phases (combat, inventory, quests)
7. **Polish:** Improve UX, add animations, optimize performance
8. **Wrap:** Create Electron/Tauri wrapper for desktop experience

---

**See BUILD-GAMEPLAY.md for detailed implementation phases.**
