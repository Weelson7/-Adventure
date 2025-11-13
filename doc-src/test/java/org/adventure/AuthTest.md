# AuthTest

**Package:** `org.adventure`  
**Phase:** 1.9 (Multiplayer & Networking)  
**Related Classes:** `AuthenticationManager`, `Player`, `PlayerSession`  
**Test Count:** 21 tests

## Purpose

Comprehensive test suite for authentication system, JWT token management, and session lifecycle. Validates user registration, login flow, token validation, session management, and cleanup mechanisms.

## Test Coverage

### Registration Tests (5 tests)

#### `testUserRegistration()`
**Validates:** Basic user registration creates account

**Steps:**
1. Register user "alice" with password "password123"
2. Verify no exception thrown
3. Authenticate with same credentials
4. Verify session created successfully

**Assertions:**
- Registration succeeds without exception
- Login returns valid PlayerSession

#### `testCannotRegisterDuplicateUsername()`
**Validates:** Username uniqueness constraint

**Steps:**
1. Register user "alice"
2. Attempt to register "alice" again
3. Expect `IllegalArgumentException`

**Assertions:**
- Second registration throws exception with message "Username already exists"

#### `testPasswordTooShort()`
**Validates:** Password length requirement (minimum 6 characters)

**Steps:**
1. Attempt to register with password "12345" (5 chars)
2. Expect `IllegalArgumentException`

**Assertions:**
- Registration throws exception with message "Password must be at least 6 characters"

#### `testUsernameCannotBeBlank()`
**Validates:** Username cannot be empty or whitespace-only

**Steps:**
1. Attempt to register with blank username ""
2. Expect `IllegalArgumentException`

**Assertions:**
- Registration throws exception with message "Username cannot be blank"

#### `testPasswordCannotBeNull()`
**Validates:** Password cannot be null

**Steps:**
1. Attempt to register with null password
2. Expect `IllegalArgumentException`

**Assertions:**
- Registration throws exception

### Authentication Tests (7 tests)

#### `testSuccessfulAuthentication()`
**Validates:** Valid credentials return session with JWT token

**Steps:**
1. Register user "bob" with password "secure123"
2. Authenticate with correct credentials
3. Verify session created with valid JWT token
4. Verify session expiry is 24 hours from now

**Assertions:**
- `session != null`
- `session.getJwtToken()` is not empty
- `session.getExpiresAt()` approximately equals `now + 24 hours`

#### `testAuthenticationWithWrongPassword()`
**Validates:** Incorrect password rejected

**Steps:**
1. Register user "charlie"
2. Attempt to authenticate with wrong password
3. Expect `IllegalArgumentException`

**Assertions:**
- Authentication throws exception with message "Invalid credentials"

#### `testAuthenticationWithNonExistentUser()`
**Validates:** Non-existent username rejected

**Steps:**
1. Attempt to authenticate with username "nonexistent"
2. Expect `IllegalArgumentException`

**Assertions:**
- Authentication throws exception with message "Invalid credentials"

#### `testAuthenticateMultipleTimes()`
**Validates:** User can login multiple times (new session replaces old)

**Steps:**
1. Register and authenticate user "dave"
2. Store first session ID
3. Authenticate again
4. Verify second session ID differs from first

**Assertions:**
- `firstSessionId != secondSessionId`
- Both sessions valid

#### `testAuthenticateAfterLogout()`
**Validates:** User can re-authenticate after logout

**Steps:**
1. Register, authenticate, logout user "eve"
2. Authenticate again
3. Verify new session created

**Assertions:**
- Second authentication succeeds
- New session created

#### `testMultipleUsersAuthenticate()`
**Validates:** Multiple users can login simultaneously

**Steps:**
1. Register users "frank", "grace", "harry"
2. Authenticate all three
3. Verify all sessions valid and distinct

**Assertions:**
- All three sessions exist
- All session IDs unique

#### `testAuthenticationCreatesPlayerSession()`
**Validates:** Authentication creates PlayerSession with correct playerId

**Steps:**
1. Register and authenticate user "iris"
2. Verify session contains playerId matching username (MVP uses username as ID)

**Assertions:**
- `session.getPlayerId()` equals expected player ID

### JWT Token Tests (4 tests)

#### `testJWTTokenValidation()`
**Validates:** Valid JWT token passes validation

**Steps:**
1. Register and authenticate user "jack"
2. Extract JWT token from session
3. Call `authManager.validateToken(token)`
4. Verify returns true

**Assertions:**
- `validateToken(token) == true`

#### `testInvalidJWTToken()`
**Validates:** Invalid/malformed token rejected

**Steps:**
1. Call `validateToken("invalid_token")`
2. Verify returns false

**Assertions:**
- `validateToken("invalid_token") == false`

#### `testExpiredTokenRejected()`
**Validates:** Expired token rejected (future enhancement, currently skipped)

**Steps:**
1. Create token with expiry in past
2. Call `validateToken(token)`
3. Verify returns false

**Status:** Skipped in MVP (requires time manipulation or mock clock)

#### `testTokenContainsClaims()`
**Validates:** JWT token contains expected claims (sub, iat, exp)

**Steps:**
1. Authenticate user "karen"
2. Extract claims from token
3. Verify subject (playerId), issued-at, expiration set

**Assertions:**
- `claims.getSubject()` equals playerId
- `claims.getIssuedAt()` approximately equals now
- `claims.getExpiration()` approximately equals now + 24 hours

### Session Management Tests (5 tests)

#### `testSessionCreation()`
**Validates:** Authentication creates session in activeSessions map

**Steps:**
1. Authenticate user "larry"
2. Retrieve session by session ID
3. Verify session exists and matches

**Assertions:**
- `authManager.getSessionById(sessionId) != null`
- Session playerId matches user

#### `testSessionRetrievalBySessionId()`
**Validates:** Session lookup by ID works

**Steps:**
1. Authenticate user "mary"
2. Get session ID
3. Retrieve session by ID
4. Verify matches original session

**Assertions:**
- `authManager.getSessionById(sessionId).getSessionId()` equals original ID

#### `testSessionInvalidation()`
**Validates:** Logout removes session from activeSessions

**Steps:**
1. Authenticate user "nancy"
2. Store session ID
3. Call `invalidateSession(sessionId)`
4. Verify session no longer retrievable

**Assertions:**
- `authManager.getSessionById(sessionId) == null` after invalidation

#### `testMultipleSessions()`
**Validates:** One session per player (new login replaces old session)

**Steps:**
1. Authenticate user "oliver" → session1
2. Authenticate "oliver" again → session2
3. Verify session1 no longer valid (replaced by session2)

**Assertions:**
- `authManager.getSessionById(session1.getSessionId()) == null`
- `authManager.getSessionById(session2.getSessionId()) != null`

#### `testSessionExpiryCheck()`
**Validates:** `PlayerSession.isExpired()` correctly identifies expired sessions

**Steps:**
1. Create session with expiry in past (mock)
2. Call `session.isExpired()`
3. Verify returns true

**Implementation:** Uses `Thread.sleep()` or mock clock (future enhancement)

### Session Cleanup Tests (2 tests)

#### `testCleanupExpiredSessions()`
**Validates:** Cleanup removes expired sessions, keeps valid ones

**Steps:**
1. Create 3 sessions:
   - Session A: expires in 1 hour (valid)
   - Session B: expires in past (expired)
   - Session C: expires in 2 hours (valid)
2. Call `cleanupExpiredSessions()`
3. Verify Session B removed, A and C remain

**Assertions:**
- `authManager.getSessionById(sessionB.getSessionId()) == null`
- `authManager.getSessionById(sessionA.getSessionId()) != null`
- `authManager.getSessionById(sessionC.getSessionId()) != null`

**MVP Status:** Partially implemented (requires time manipulation)

#### `testCleanupDoesNotRemoveValidSessions()`
**Validates:** Cleanup only removes expired sessions

**Steps:**
1. Create 5 valid sessions (all expire in 24 hours)
2. Call `cleanupExpiredSessions()`
3. Verify all 5 sessions still exist

**Assertions:**
- `cleanupExpiredSessions()` returns 0 (no sessions removed)
- All 5 sessions retrievable

## Key Test Scenarios

### Happy Path: Registration → Login → Action → Logout
```java
@Test
public void testFullAuthenticationFlow() {
    // Register
    authManager.registerUser("alice", "password123");
    
    // Login
    PlayerSession session = authManager.authenticate("alice", "password123");
    assertNotNull(session);
    assertTrue(authManager.validateToken(session.getJwtToken()));
    
    // Session exists
    assertNotNull(authManager.getSessionById(session.getSessionId()));
    
    // Logout
    authManager.invalidateSession(session.getSessionId());
    assertNull(authManager.getSessionById(session.getSessionId()));
}
```

### Security: Prevent Common Attacks
```java
@Test
public void testSecurityScenarios() {
    // Cannot register duplicate username (account takeover prevention)
    authManager.registerUser("bob", "password1");
    assertThrows(IllegalArgumentException.class, () -> 
        authManager.registerUser("bob", "password2"));
    
    // Cannot login with wrong password (brute force mitigation, future: rate limiting)
    assertThrows(IllegalArgumentException.class, () -> 
        authManager.authenticate("bob", "wrongpassword"));
    
    // Cannot use invalid token (token forgery prevention)
    assertFalse(authManager.validateToken("forged_token"));
}
```

## Test Utilities

### Helper Methods
```java
private PlayerSession createAndAuthenticateUser(String username) {
    authManager.registerUser(username, "password123");
    return authManager.authenticate(username, "password123");
}

private void assertSessionValid(PlayerSession session) {
    assertNotNull(session);
    assertNotNull(session.getJwtToken());
    assertFalse(session.isExpired());
}
```

## Known Limitations

### MVP Test Gaps
- **No Time Manipulation:** Cannot easily test token expiry (requires mock clock or Thread.sleep)
- **No Concurrency Tests:** All tests single-threaded (future: test concurrent logins)
- **No Performance Tests:** No load testing of authentication system
- **No Security Scans:** No penetration testing of JWT implementation

### Future Test Enhancements
1. **Time Mocking:** Use `Clock` abstraction for controllable time
   ```java
   Clock mockClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
   authManager.setClock(mockClock);
   ```

2. **Concurrency Tests:** Test thread-safety of session map
   ```java
   ExecutorService executor = Executors.newFixedThreadPool(10);
   for (int i = 0; i < 100; i++) {
       executor.submit(() -> authManager.authenticate("alice", "password"));
   }
   ```

3. **Security Tests:** Test JWT vulnerabilities
   ```java
   testNoneAlgorithmRejected();
   testSignatureStrippingRejected();
   testClaimInjectionPrevented();
   ```

## Related Documentation

- `AuthenticationManager.md` - Authentication implementation
- `PlayerSession.md` - Session data model
- `Player.md` - Player entity with session reference
- `ServerTest.md` - Server integration tests
- `docs/testing_plan.md` - Overall testing strategy
- `archive/PHASE_1.9_SUMMARY.md` - Phase 1.9 test summary
