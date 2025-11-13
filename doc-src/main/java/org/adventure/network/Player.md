# Player

**Package:** `org.adventure.network`  
**Phase:** 1.9 (Multiplayer & Networking)  
**Related Classes:** `PlayerSession`, `Character`, `PlayerAction`

## Purpose

Represents a player in the multiplayer game, linking their network session to their in-game character. Acts as the bridge between authentication/networking layer and game state.

## Key Responsibilities

1. **Identity Management:** Maintains unique player ID and username
2. **Session Tracking:** Links to active `PlayerSession` with JWT token
3. **Character Association:** References the player's in-game `Character`
4. **Authentication State:** Tracks whether player is currently authenticated
5. **Activity Monitoring:** Records timestamp of last action for idle detection

## Design Decisions

### Why Separate Player from Character?
- **Network vs Game State:** Player handles networking concerns (session, auth), Character handles game mechanics (stats, inventory)
- **Multiple Characters:** Future support for players having multiple characters
- **Session Lifecycle:** Player persists across sessions, session is ephemeral

### Authentication Flag
- Redundant with session expiry check, but provides fast O(1) lookup
- Set to false on logout without destroying Player object (preserves history)

## Class Structure

```java
public class Player {
    private final String playerId;          // UUID, unique identifier
    private final String username;          // Display name, unique
    private String characterId;             // Reference to Character, nullable
    private PlayerSession session;          // Current session, nullable
    private boolean authenticated;          // Fast auth check
    private Instant lastActionTime;         // For idle timeout detection
}
```

## Key Methods

### `Player(String username)`
- **Purpose:** Create new player account
- **Generated:** `playerId` as UUID, `lastActionTime` as now
- **Defaults:** `authenticated = false`, `session = null`, `characterId = null`

### `setSession(PlayerSession session)`
- **Purpose:** Attach active session to player
- **Side Effect:** Sets `authenticated = true`
- **Validation:** None (trust caller, Server validates)

### `clearSession()`
- **Purpose:** Remove session on logout
- **Side Effect:** Sets `authenticated = false`
- **Preserves:** `playerId`, `username`, `characterId` for reconnection

### `updateLastActionTime()`
- **Purpose:** Record activity timestamp
- **Called By:** Server after processing each action
- **Use Case:** Idle timeout detection, session cleanup

## Integration Points

### With Server
- **Registration:** Server creates Player, stores in `activePlayers` map
- **Login:** Server creates PlayerSession, calls `setSession()`
- **Action Processing:** Server calls `updateLastActionTime()` after each action
- **Logout:** Server calls `clearSession()`, removes session from `activeSessions`

### With Character
- **Character Creation:** When player creates character, set `characterId`
- **Character Lookup:** Server uses `characterId` to fetch Character for action validation
- **Orphan Prevention:** Character should track `playerId` for bidirectional link

### With PlayerSession
- **One-to-One:** Each Player has at most one active session
- **Lifecycle:** Session created on login, destroyed on logout/expiry
- **JWT Storage:** Session holds JWT token, Player doesn't need to know token details

## Thread Safety

### Current Implementation
- **Not Thread-Safe:** No synchronization on field access
- **Acceptable for MVP:** Server uses single-threaded action processing
- **Future Enhancement:** Add `synchronized` or use `AtomicReference` for session field

## Persistence Considerations

### MVP (In-Memory)
- Players stored in `ConcurrentHashMap<String, Player>` in Server
- Lost on server restart
- Character data persists separately via SaveManager

### Post-MVP
- Persist to database: `players` table with `player_id`, `username`, `character_id`
- Session tracking: Redis for distributed session management
- Activity logging: Track `last_action_time` for analytics

## Testing

### AuthTest Coverage
- `testUserRegistration`: Validates Player creation with unique username
- `testSessionCreation`: Validates `setSession()` sets authenticated flag
- `testSessionInvalidation`: Validates `clearSession()` resets auth state
- `testMultipleSessions`: Validates one session per player

### ServerTest Coverage
- `testPlayerLogin`: Validates Player created and authenticated
- `testPlayerLogout`: Validates Player exists but not authenticated after logout
- `testMultiplePlayersCanLogin`: Validates multiple Player objects coexist

## Known Limitations

### MVP Constraints
- **No Password Storage:** AuthenticationManager handles passwords separately
- **No Character Creation:** Character must be created outside Player (Game.java handles)
- **No Multi-Character:** One character per player
- **No Offline State:** Player deleted on disconnect (no persistence)

### Security Considerations
- **No Rate Limiting:** Server should throttle actions per player
- **No IP Tracking:** Should track IP for ban enforcement
- **No Device Management:** Should track device/session for multi-login prevention

## Future Enhancements

1. **Persistence Layer:** Database integration with player profile data
2. **Multi-Character Support:** `List<String> characterIds` instead of single ID
3. **Presence System:** Online/offline/idle status tracking
4. **Friends List:** Social graph for player-to-player connections
5. **Preferences:** Store UI settings, keybindings, accessibility options
6. **Achievements:** Track player-level (not character-level) achievements

## Related Documentation

- `PlayerSession.md` - Session lifecycle and JWT handling
- `AuthenticationManager.md` - Registration and login logic
- `Server.md` - Server-side player management
- `Character.md` - In-game character entity
- `docs/design_decisions.md` - Authentication architecture decisions
