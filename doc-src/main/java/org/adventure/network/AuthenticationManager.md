# AuthenticationManager

**Package:** `org.adventure.network`  
**Phase:** 1.9 (Multiplayer & Networking)  
**Related Classes:** `Player`, `PlayerSession`, `Server`  
**Dependencies:** `io.jsonwebtoken` (JJWT 0.12.3)

## Purpose

Manages player authentication, JWT token generation/validation, and session lifecycle. Provides centralized authentication services for the authoritative server.

## Key Responsibilities

1. **User Registration:** Create new player accounts with password validation
2. **Authentication:** Verify credentials and generate JWT tokens
3. **Token Validation:** Parse and verify JWT tokens for request authorization
4. **Session Management:** Track active sessions and handle expiration
5. **Security:** Prevent duplicate usernames, validate passwords, cleanup expired sessions

## Design Decisions

### Why JWT (JSON Web Tokens)?
- **Stateless Auth:** Token contains all needed info (playerId, expiry), no DB lookup per request
- **Standard:** Industry-standard format (RFC 7519), well-supported libraries
- **Secure:** HMAC-SHA256 signature prevents tampering
- **Self-Contained:** Client can decode token to see claims (but can't modify without invalidating signature)

### Why HMAC-SHA256 (HS256)?
- **Symmetric:** Single secret key for sign and verify (simpler than RSA public/private keys)
- **Fast:** ~10x faster than RSA-256 for signing and verification
- **Secure:** 256-bit key provides adequate security for MVP (future: rotate keys periodically)

### Plaintext Password Storage (MVP Only!)
- **Security Risk:** Passwords stored in `ConcurrentHashMap<String, String>` as plaintext
- **Acceptable for MVP:** No production deployment, single-player/LAN testing only
- **Post-MVP:** Migrate to bcrypt/Argon2 with salted hashing (see Future Enhancements)

### In-Memory Session Storage
- **Volatile:** Sessions lost on server restart
- **Acceptable for MVP:** No persistence layer yet
- **Post-MVP:** Migrate to Redis for distributed session management

## Class Structure

```java
public class AuthenticationManager {
    private static final String SECRET_KEY = "...";  // 256-bit base64-encoded key
    private static final long TOKEN_EXPIRY_HOURS = 24;
    
    private final Map<String, String> userPasswords;              // username → password (plaintext)
    private final Map<String, PlayerSession> activeSessions;      // sessionId → session
    
    public void registerUser(String username, String password) { ... }
    public PlayerSession authenticate(String username, String password) { ... }
    public boolean validateToken(String token) { ... }
    public Claims getClaimsFromToken(String token) { ... }
    public PlayerSession getSessionById(String sessionId) { ... }
    public void invalidateSession(String sessionId) { ... }
    public void cleanupExpiredSessions() { ... }
}
```

## Key Methods

### `registerUser(String username, String password)`
**Purpose:** Create new player account

**Validation:**
1. Username not blank: `!username.isBlank()`
2. Username unique: `!userPasswords.containsKey(username)`
3. Password length >= 6: `password.length() >= 6`

**Side Effects:**
- Stores `userPasswords.put(username, password)`
- No `Player` object created yet (happens on first login)

**Throws:**
- `IllegalArgumentException` if validation fails

**Example:**
```java
authManager.registerUser("alice", "secure123");  // Success
authManager.registerUser("alice", "pass");       // Throws: username exists
authManager.registerUser("bob", "12345");        // Throws: password too short
```

### `authenticate(String username, String password)`
**Purpose:** Verify credentials and generate JWT session

**Steps:**
1. Check username exists: `userPasswords.containsKey(username)`
2. Check password matches: `userPasswords.get(username).equals(password)`
3. Generate JWT token with 24-hour expiry
4. Create `PlayerSession` with token and expiry
5. Store session: `activeSessions.put(sessionId, session)`
6. Return session

**Returns:** `PlayerSession` with JWT token

**Throws:**
- `IllegalArgumentException("Invalid credentials")` if username not found or password wrong

**JWT Claims:**
- **Subject (`sub`):** `playerId` (generated from username, should be UUID in future)
- **Issued At (`iat`):** Current timestamp (`Instant.now().getEpochSecond()`)
- **Expiration (`exp`):** 24 hours from now

**Example:**
```java
PlayerSession session = authManager.authenticate("alice", "secure123");
String token = session.getJwtToken();
// token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSIsImlhdCI6MTczMTQ1NjAwMCwiZXhwIjoxNzMxNTQyNDAwfQ.signature"
```

### `validateToken(String token)`
**Purpose:** Verify JWT token signature and expiration

**Steps:**
1. Parse token: `Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)`
2. JJWT library automatically verifies:
   - Signature matches (using `SECRET_KEY`)
   - Token not expired (`exp` claim > now)
3. Return `true` if valid, `false` if invalid/expired

**Returns:** `boolean`

**Catches:**
- `JwtException` → return `false` (invalid signature, malformed token, expired)

**Example:**
```java
boolean valid = authManager.validateToken(token);  // true if valid
boolean valid = authManager.validateToken("fake");  // false (invalid signature)
```

### `getClaimsFromToken(String token)`
**Purpose:** Extract claims from JWT without validation (use after `validateToken`)

**Steps:**
1. Parse token (unsigned): `Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()`
2. Return `Claims` object with `sub`, `iat`, `exp`

**Returns:** `Claims` (contains `playerId`, `issuedAt`, `expiresAt`)

**Use Case:**
```java
if (authManager.validateToken(token)) {
    Claims claims = authManager.getClaimsFromToken(token);
    String playerId = claims.getSubject();  // Extract playerId from token
}
```

### `getSessionById(String sessionId)`
**Purpose:** Retrieve session by ID

**Returns:** `PlayerSession` or `null` if not found

**Thread-Safe:** `ConcurrentHashMap` ensures safe concurrent access

**Example:**
```java
PlayerSession session = authManager.getSessionById(sessionId);
if (session != null && !session.isExpired()) {
    // Session valid, proceed
}
```

### `invalidateSession(String sessionId)`
**Purpose:** Remove session from active sessions (logout)

**Side Effects:**
- Removes from `activeSessions` map
- Client must discard token (server cannot revoke already-issued JWT)

**Limitation:**
- Token remains valid until expiry (24 hours) if client keeps it
- Future: Implement token revocation list (blacklist) in Redis

**Example:**
```java
authManager.invalidateSession(sessionId);  // Session removed from map
// Token still valid for 24 hours if client has it (JWT limitation)
```

### `cleanupExpiredSessions()`
**Purpose:** Remove expired sessions from map (memory cleanup)

**Steps:**
1. Iterate `activeSessions.values()`
2. For each session, if `session.isExpired() == true`, remove from map
3. Return count of removed sessions

**Called By:**
- `authenticate()` on every login (passive cleanup)
- Future: Scheduled task every 5 minutes

**Performance:**
- O(N) where N = number of active sessions
- Fast for MVP (< 100 sessions), may need optimization for production (> 10,000 sessions)

**Example:**
```java
int removed = authManager.cleanupExpiredSessions();  // Returns 5 (removed 5 expired sessions)
```

## JWT Token Structure

### Header
```json
{
  "alg": "HS256"
}
```
- **Algorithm:** HMAC-SHA256
- **Type:** JWT (default, omitted)

### Payload (Claims)
```json
{
  "sub": "player-123",           // playerId (Subject)
  "iat": 1731456000,             // Issued At (Unix timestamp)
  "exp": 1731542400              // Expiration (Unix timestamp, 24 hours later)
}
```

### Signature
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  SECRET_KEY
)
```

### Full Token
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwbGF5ZXItMTIzIiwiaWF0IjoxNzMxNDU2MDAwLCJleHAiOjE3MzE1NDI0MDB9.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk
```
- **Part 1:** Header (base64url-encoded)
- **Part 2:** Payload (base64url-encoded)
- **Part 3:** Signature (HMAC-SHA256)

## Secret Key Management

### Current Implementation
```java
private static final String SECRET_KEY = 
    "YourSecretKeyHere12345678901234567890123456789012345678901234567890";
```
- **Length:** 256 bits (64 hex chars or 32 bytes)
- **Hardcoded:** Secret key in source code (insecure for production)
- **Single Key:** Same key for all tokens (no key rotation)

### Future Enhancements
1. **Environment Variable:** Load from `System.getenv("JWT_SECRET_KEY")`
2. **Key Rotation:** Support multiple keys with `kid` (Key ID) claim
3. **Per-Environment:** Different keys for dev/staging/prod
4. **Secure Storage:** Use AWS Secrets Manager, Azure Key Vault, etc.

## Session Storage

### Data Structure
```java
private final Map<String, PlayerSession> activeSessions = new ConcurrentHashMap<>();
```

### Key: Session ID (UUID)
- **Why Not playerId?** Multiple sessions per player in future (multi-device)
- **Why Not JWT Token?** Token is long (200+ chars), UUID is shorter (36 chars)

### Value: PlayerSession
- Contains: `sessionId`, `playerId`, `jwtToken`, `createdAt`, `expiresAt`, `lastActivity`

### Thread Safety
- **ConcurrentHashMap:** Safe for concurrent reads and writes
- **No Locking:** Multiple threads can authenticate simultaneously

## Integration Points

### With Server
- **Registration:** Server calls `authManager.registerUser()` when player creates account
- **Login:** Server calls `authManager.authenticate()` → receives `PlayerSession` → stores in `Player`
- **Action Validation:** Server calls `authManager.validateToken()` before processing actions
- **Logout:** Server calls `authManager.invalidateSession()` → removes session

### With Player
- **Session Link:** `Player.setSession(session)` stores session returned by `authenticate()`
- **Authentication Flag:** Player sets `authenticated = true` when session attached
- **Logout:** Player calls `clearSession()` when session invalidated

### With PlayerSession
- **Creation:** `authenticate()` creates `PlayerSession` with JWT token
- **Validation:** `validateToken()` checks session.isExpired() indirectly (via JWT exp claim)
- **Cleanup:** `cleanupExpiredSessions()` removes expired sessions

## Security Considerations

### MVP Security Gaps
1. **Plaintext Passwords:** Stored in memory as plaintext (not hashed)
   - **Risk:** Memory dump or debugger can reveal passwords
   - **Mitigation:** Use bcrypt post-MVP
   
2. **Hardcoded Secret Key:** Secret key in source code
   - **Risk:** Anyone with source code can forge tokens
   - **Mitigation:** Use environment variable post-MVP
   
3. **No Token Revocation:** Cannot revoke token before expiry
   - **Risk:** Stolen token valid for 24 hours even after logout
   - **Mitigation:** Implement blacklist in Redis post-MVP
   
4. **No Rate Limiting:** Unlimited login attempts
   - **Risk:** Brute-force password attacks
   - **Mitigation:** Implement rate limiting (e.g., 5 attempts/minute) post-MVP
   
5. **No HTTPS:** Tokens sent over plaintext (MVP is local/LAN only)
   - **Risk:** Network sniffing can intercept tokens
   - **Mitigation:** Require HTTPS for production deployment

### Production Security Checklist
- [ ] Migrate to bcrypt password hashing
- [ ] Load SECRET_KEY from environment variable
- [ ] Implement token revocation (Redis blacklist)
- [ ] Add rate limiting (e.g., Spring Security)
- [ ] Require HTTPS for all connections
- [ ] Add 2FA support (TOTP)
- [ ] Implement account lockout after N failed attempts
- [ ] Add password complexity requirements (uppercase, lowercase, number, symbol)
- [ ] Log authentication events for audit trail

## Performance Considerations

### Authentication Latency
- **Current:** <5ms (in-memory map lookup + JJWT signing)
- **Future with DB:** 20-50ms (database query for password hash)

### Token Validation Latency
- **Current:** <1ms (JJWT parsing + signature verification)
- **Scalability:** Can validate 100,000+ tokens/second on single core

### Memory Usage
- **Per Session:** ~200 bytes (UUID + token + timestamps)
- **10,000 Sessions:** ~2 MB memory
- **Cleanup:** `cleanupExpiredSessions()` prevents unbounded growth

## Testing

### AuthTest Coverage (21 Tests)
- **Registration:** `testUserRegistration`, `testCannotRegisterDuplicateUsername`, `testPasswordTooShort`, `testUsernameCannotBeBlank`
- **Authentication:** `testSuccessfulAuthentication`, `testAuthenticationWithWrongPassword`, `testAuthenticationWithNonExistentUser`
- **Token Validation:** `testJWTTokenValidation`, `testInvalidJWTToken`, `testExpiredTokenRejected`
- **Session Management:** `testSessionCreation`, `testSessionRetrievalBySessionId`, `testSessionInvalidation`, `testMultipleSessions`, `testSessionExpiryCheck`
- **Cleanup:** `testCleanupExpiredSessions`, `testCleanupDoesNotRemoveValidSessions`

## Known Limitations

### MVP Constraints
- **Single-Server Only:** In-memory sessions don't work with load balancer (no shared state)
- **No Persistence:** Sessions lost on server restart
- **No Multi-Device:** One session per player (old session replaced on new login)
- **No Refresh Tokens:** Must re-authenticate after 24 hours

### Scalability Limits
- **Memory:** 10,000 sessions = 2 MB (acceptable), 1,000,000 sessions = 200 MB (need Redis)
- **CPU:** Signing/validation is fast, but cleanup is O(N) every call

## Future Enhancements

1. **Password Hashing:** Migrate to bcrypt with cost factor 12
   ```java
   String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
   boolean valid = BCrypt.checkpw(password, hashed);
   ```

2. **Redis Session Store:** Distributed sessions for multi-server deployment
   ```java
   redisTemplate.opsForValue().set("session:" + sessionId, session, 24, TimeUnit.HOURS);
   ```

3. **Refresh Tokens:** Long-lived refresh token (30 days) + short-lived access token (15 minutes)
   ```java
   RefreshToken refreshToken = new RefreshToken(playerId, 30_DAYS);
   AccessToken accessToken = refreshToken.generateAccessToken(15_MINUTES);
   ```

4. **OAuth2 Integration:** Support login via Google, Discord, Steam
   ```java
   PlayerSession session = authManager.authenticateOAuth2(provider, oauthToken);
   ```

5. **2FA Support:** TOTP-based two-factor authentication
   ```java
   authManager.enable2FA(playerId);
   boolean valid = authManager.verify2FA(playerId, totpCode);
   ```

6. **Audit Logging:** Log all authentication events (success, failure, logout)
   ```java
   auditLog.log("LOGIN_SUCCESS", playerId, ipAddress, userAgent);
   ```

## Related Documentation

- `Player.md` - Player entity with session reference
- `PlayerSession.md` - Session lifecycle and expiry tracking
- `Server.md` - Server-side authentication flow
- `docs/design_decisions.md` - Authentication architecture decisions
- `archive/PHASE_1.9_SUMMARY.md` - Phase 1.9 implementation details
- JJWT Documentation: https://github.com/jwtk/jjwt
