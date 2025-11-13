# PlayerSession

**Package:** `org.adventure.network`  
**Phase:** 1.9 (Multiplayer & Networking)  
**Related Classes:** `Player`, `AuthenticationManager`, `Server`

## Purpose

Represents an active authenticated session for a player, including JWT token storage, expiration tracking, and activity monitoring. Provides session lifecycle management and validation.

## Key Responsibilities

1. **Token Storage:** Holds JWT authentication token for request validation
2. **Expiration Management:** Tracks session creation and expiry times
3. **Activity Tracking:** Records last activity for idle timeout detection
4. **Session Validation:** Provides `isExpired()` check for quick validation

## Design Decisions

### Why Separate Session from Player?
- **Lifecycle Independence:** Player persists, session is ephemeral
- **Multi-Login Prevention:** One session per player at a time
- **Security Isolation:** Session contains sensitive token, Player does not
- **Expiry Semantics:** Session expires, Player does not

### Fixed 24-Hour Expiry
- **Rationale:** Balance between security (short sessions) and UX (avoid frequent re-auth)
- **No Sliding Window:** MVP uses fixed expiry, not activity-based extension
- **Future Enhancement:** Implement refresh tokens for longer-lived sessions

### Instant vs Long for Timestamps
- **Precision:** `Instant` provides nanosecond precision (overkill but future-proof)
- **Time Zones:** `Instant` is UTC-based, avoids time zone issues
- **Compatibility:** Easily converts to `Date` for JJWT library

## Class Structure

```java
public class PlayerSession {
    private final String sessionId;       // UUID, unique identifier
    private final String playerId;        // Foreign key to Player
    private final String jwtToken;        // JWT authentication token
    private final Instant createdAt;      // Session creation timestamp (UTC)
    private final Instant expiresAt;      // Session expiration timestamp (UTC)
    private Instant lastActivity;         // Last action timestamp (UTC)
}
```

## Key Methods

### `PlayerSession(String playerId, String jwtToken, Instant expiresAt)`
- **Purpose:** Create new session on successful login
- **Generated:** `sessionId` as UUID, `createdAt` as now, `lastActivity` as now
- **Validation:** None (trust caller, AuthenticationManager validates)
- **Immutability:** All fields except `lastActivity` are final

### `isExpired()`
- **Purpose:** Check if session has expired
- **Logic:** `Instant.now().isAfter(expiresAt)`
- **Returns:** `true` if expired, `false` if valid
- **Thread-Safe:** `Instant` is immutable, safe for concurrent reads

### `updateActivity()`
- **Purpose:** Record activity timestamp
- **Side Effect:** Sets `lastActivity = Instant.now()`
- **Called By:** Server after processing each authenticated action
- **Use Case:** Idle timeout detection (future enhancement)

### Getters
- **All Final Fields:** Standard getters for `sessionId`, `playerId`, `jwtToken`, `createdAt`, `expiresAt`, `lastActivity`
- **No Setters:** Immutable session (except `lastActivity`)

## Integration Points

### With AuthenticationManager
- **Session Creation:** `authenticate()` creates PlayerSession with JWT token
- **Token Validation:** `validateToken()` checks JWT signature and expiry
- **Session Storage:** Stores in `activeSessions` map keyed by `sessionId`
- **Cleanup:** `cleanupExpiredSessions()` removes sessions where `isExpired() == true`

### With Server
- **Login Flow:** Server calls `authManager.authenticate()` → receives PlayerSession → stores in Player
- **Action Processing:** Server checks `session.isExpired()` before validating actions
- **Logout Flow:** Server removes session from `activeSessions`, calls `player.clearSession()`

### With Player
- **One-to-One:** Each Player has at most one active PlayerSession
- **Lifecycle:** Player outlives session (Player persists, session expires)
- **Bidirectional Link:** Session stores `playerId`, Player stores `session` reference

## JWT Token Structure

### Claims
- **Subject (`sub`):** `playerId` (UUID string)
- **Issued At (`iat`):** `createdAt.getEpochSecond()`
- **Expiration (`exp`):** `expiresAt.getEpochSecond()`

### Signature
- **Algorithm:** HMAC-SHA256 (HS256)
- **Secret Key:** 256-bit key stored in `AuthenticationManager.SECRET_KEY`
- **Validation:** JJWT library validates signature on `validateToken()` call

### Token Format
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI8cGxheWVySWQ+IiwiaWF0IjoxNzMxNDU2MDAwLCJleHAiOjE3MzE1NDI0MDB9.<signature>
```

## Thread Safety

### Current Implementation
- **Mostly Immutable:** All fields final except `lastActivity`
- **Race Condition:** `updateActivity()` not synchronized, but benign (last-write-wins)
- **Read Safety:** Getters safe for concurrent access (volatile not needed for immutable fields)

### Synchronization Points
- **Session Map:** `ConcurrentHashMap` in AuthenticationManager ensures thread-safe session storage
- **Expiry Check:** `isExpired()` safe (compares immutable `Instant` objects)

## Persistence Considerations

### MVP (In-Memory)
- Sessions stored in `ConcurrentHashMap<String, PlayerSession>` in AuthenticationManager
- Lost on server restart (players must re-authenticate)
- No session persistence to disk

### Post-MVP
- **Redis:** Store sessions in Redis for distributed session management
- **Database:** Persist session history for audit logging
- **Refresh Tokens:** Store long-lived refresh tokens for session renewal

## Testing

### AuthTest Coverage
- `testSessionCreation`: Validates PlayerSession created with correct expiry (24 hours)
- `testSessionRetrievalBySessionId`: Validates session lookup by ID
- `testSessionInvalidation`: Validates session removed on logout
- `testMultipleSessions`: Validates one session per player (old session replaced)
- `testCleanupExpiredSessions`: Validates expired sessions removed from map

### ServerTest Coverage
- `testPlayerLogin`: Validates session created and linked to Player
- `testPlayerLogout`: Validates session removed from activeSessions map
- `testActionSubmission`: Validates actions processed only for authenticated sessions

## Known Limitations

### MVP Constraints
- **Fixed Expiry:** No sliding window or refresh tokens
- **No Revocation:** Cannot revoke token before expiry (must wait 24 hours)
- **No Device Tracking:** Cannot distinguish sessions by device/IP
- **No Concurrent Sessions:** Only one session per player (no multi-device support)

### Security Considerations
- **Token Theft:** If JWT stolen, attacker can impersonate player until expiry
- **No Logout Propagation:** Cannot force logout across distributed servers (in-memory only)
- **Secret Key Management:** SECRET_KEY hardcoded (should use environment variable)

## Expiry Calculation

### Formula
```java
Instant expiresAt = createdAt.plus(24, ChronoUnit.HOURS);
```

### Edge Cases
- **Clock Skew:** Server and client clocks may differ (use server time as source of truth)
- **Daylight Saving Time:** `Instant` is UTC, unaffected by DST
- **Leap Seconds:** `Instant` handles leap seconds correctly

## Activity Tracking

### Current Usage
- **Recorded:** `updateActivity()` called after each action
- **Not Enforced:** MVP does not implement idle timeout (future enhancement)

### Future Enhancements
- **Idle Timeout:** Expire session after 15 minutes of inactivity
- **Activity Metrics:** Track actions per session for analytics
- **Anomaly Detection:** Flag unusual activity patterns (e.g., 1000 actions/second)

## Session Cleanup

### Automatic Cleanup
- **Method:** `AuthenticationManager.cleanupExpiredSessions()`
- **Frequency:** Called on login (passive cleanup, not scheduled)
- **Logic:** Iterate `activeSessions`, remove where `isExpired() == true`

### Future Enhancements
- **Scheduled Task:** Run cleanup every 5 minutes via `ScheduledExecutorService`
- **Idle Cleanup:** Remove sessions with `lastActivity > 15 minutes ago`
- **Metrics:** Log number of sessions cleaned per run

## Related Documentation

- `Player.md` - Player entity with session reference
- `AuthenticationManager.md` - Session creation and validation logic
- `Server.md` - Server-side session management
- `docs/design_decisions.md` - Authentication architecture decisions
- `archive/PHASE_1.9_SUMMARY.md` - Phase 1.9 implementation details
