# Client

**Package:** `org.adventure.network`  
**Phase:** 1.9 (Multiplayer & Networking)  
**Related Classes:** `Server`, `Player`, `PlayerSession`, `PlayerAction`

## Purpose

Simple text-based CLI client for player interaction with the game server. Provides command-line interface for registration, login, action submission, and logout. MVP implementation uses direct `Server` reference (future: network socket).

## Key Responsibilities

1. **User Interface:** Present CLI for player commands
2. **Authentication:** Handle registration and login flows
3. **Action Submission:** Build and submit player actions to server
4. **Session Management:** Track authentication state and session
5. **User Feedback:** Display server responses and errors

## Design Decisions

### Why CLI (Not GUI)?
- **MVP Simplicity:** No UI framework required (Swing/JavaFX)
- **Focus on Logic:** Networking logic independent of UI
- **Easy Testing:** Can script CLI commands for automated testing
- **Future Migration:** Easy to replace with GUI, web, or mobile client

### Why Direct Server Reference?
- **MVP Testing:** Simplifies testing without network setup
- **Future Refactoring:** Replace `Server server` with `Socket socket`
- **Same API:** Client code unchanged when migrating to network protocol

### Command-Driven Architecture
- **User Input:** Player types commands (login, action, logout)
- **Parser:** Split input into command + arguments
- **Handler:** Dispatch to command-specific handler
- **Feedback:** Print result or error message

## Class Structure

```java
public class Client {
    private final Server server;        // Direct reference (MVP), future: network socket
    private PlayerSession session;      // Current session, null if not logged in
    private Player player;              // Current player, null if not logged in
    private boolean connected;          // Authentication state flag
    
    public void start() { ... }         // Main CLI loop
    private void handleRegister(Scanner scanner) { ... }
    private void handleLogin(Scanner scanner) { ... }
    private void handleAction(Scanner scanner) { ... }
    private void handleLogout() { ... }
    private boolean checkConnected() { ... }
}
```

## Lifecycle

### `start()`
**Purpose:** Main CLI loop, read and process commands until quit

**Flow:**
```
1. Print banner: "=== !Adventure Client ==="
2. Print help: "Commands: login, register, action, logout, quit"
3. Loop:
   a. Print prompt: "> "
   b. Read line from System.in
   c. Parse command (first word)
   d. Dispatch to handler
   e. Print result or error
4. Exit on "quit" command
```

**Commands:**
- `register` - Create new account
- `login` - Authenticate and start session
- `action` - Submit player action (requires login)
- `logout` - End session
- `quit` - Exit client
- `help` - Show available commands

**Example Session:**
```
=== !Adventure Client ===
Commands: login, register, action, logout, quit
> register
Enter username: alice
Enter password: secure123
Registration successful!
> login
Enter username: alice
Enter password: secure123
Login successful! Welcome, alice
> action
Available actions: MOVE, HARVEST, CRAFT, ATTACK, TRADE, BUILD, CHAT, USE_ITEM, DROP_ITEM, PICK_UP_ITEM, TRANSFER_OWNERSHIP, JOIN_CLAN, LEAVE_CLAN
Enter action type: CHAT
Enter message: Hello world!
Action submitted successfully.
> logout
Logged out successfully.
> quit
Goodbye!
```

## Command Handlers

### `handleRegister(Scanner scanner)`
**Purpose:** Register new player account

**Prompts:**
1. "Enter username: " → read username
2. "Enter password: " → read password

**Steps:**
1. Read username and password from user
2. Call `server.registerPlayer(username, password)`
3. Print "Registration successful!"

**Error Handling:**
- Catch `IllegalArgumentException` → print error (duplicate username, password too short)

**Example:**
```
> register
Enter username: alice
Enter password: 123
Error: Password must be at least 6 characters
> register
Enter username: alice
Enter password: secure123
Registration successful!
```

### `handleLogin(Scanner scanner)`
**Purpose:** Authenticate and create session

**Prompts:**
1. "Enter username: " → read username
2. "Enter password: " → read password

**Steps:**
1. Read username and password
2. Call `server.login(username, password)` → returns `Player`
3. Store player: `this.player = result`
4. Store session: `this.session = player.getSession()`
5. Set connected: `this.connected = true`
6. Print "Login successful! Welcome, {username}"

**Error Handling:**
- Catch `IllegalArgumentException` → print "Invalid credentials"

**Example:**
```
> login
Enter username: alice
Enter password: wrong
Invalid credentials
> login
Enter username: alice
Enter password: secure123
Login successful! Welcome, alice
```

### `handleAction(Scanner scanner)`
**Purpose:** Build and submit player action

**Precondition:** `checkConnected()` returns true (must be logged in)

**Prompts:**
1. "Available actions: {list all ActionType enums}"
2. "Enter action type: " → read action type (e.g., MOVE, HARVEST, CHAT)
3. Action-specific prompts (e.g., "Enter targetRegionId: " for MOVE)

**Steps:**
1. Display available action types
2. Read action type from user
3. Prompt for action-specific parameters (see Action-Specific Prompts below)
4. Build `PlayerAction` with builder pattern
5. Call `server.submitAction(action)`
6. Print "Action submitted successfully."

**Error Handling:**
- Invalid action type → print "Unknown action type"
- Missing parameters → print "Missing required parameter: {param}"

**Example:**
```
> action
Available actions: MOVE, HARVEST, CRAFT, ATTACK, TRADE, BUILD, CHAT, USE_ITEM, DROP_ITEM, PICK_UP_ITEM, TRANSFER_OWNERSHIP, JOIN_CLAN, LEAVE_CLAN
Enter action type: MOVE
Enter targetRegionId: region-123
Action submitted successfully.
```

### Action-Specific Prompts

#### MOVE
- "Enter targetRegionId: " → `parameter("targetRegionId", value)`

#### HARVEST
- "Enter resourceNodeId: " → `parameter("resourceNodeId", value)`

#### CRAFT
- "Enter recipeId: " → `parameter("recipeId", value)`
- "Enter quantity: " → `parameter("quantity", Integer.parseInt(value))`

#### ATTACK
- "Enter targetCharacterId: " → `parameter("targetCharacterId", value)`

#### TRADE
- "Enter targetPlayerId: " → `parameter("targetPlayerId", value)`
- "Enter offeredItems (comma-separated): " → `parameter("offeredItems", Arrays.asList(value.split(",")))`
- "Enter requestedItems (comma-separated): " → `parameter("requestedItems", Arrays.asList(value.split(",")))`

#### BUILD
- "Enter structureType: " → `parameter("structureType", value)`
- "Enter roomCategory: " → `parameter("roomCategory", value)`
- "Enter location: " → `parameter("location", value)`

#### CHAT
- "Enter message: " → `parameter("message", value)`

#### USE_ITEM
- "Enter itemId: " → `parameter("itemId", value)`
- "Enter targetId (optional): " → `parameter("targetId", value)` (if not empty)

#### DROP_ITEM / PICK_UP_ITEM
- "Enter itemId: " → `parameter("itemId", value)`

#### TRANSFER_OWNERSHIP
- "Enter structureId: " → `parameter("structureId", value)`
- "Enter newOwnerId: " → `parameter("newOwnerId", value)`
- "Enter transferType: " → `parameter("transferType", value)`

#### JOIN_CLAN
- "Enter clanId: " → `parameter("clanId", value)`

#### LEAVE_CLAN
- No parameters

### `handleLogout()`
**Purpose:** End session and disconnect

**Steps:**
1. Call `server.logout(player.getPlayerId())`
2. Clear session: `this.session = null`
3. Clear player: `this.player = null`
4. Set connected: `this.connected = false`
5. Print "Logged out successfully."

**Example:**
```
> logout
Logged out successfully.
```

### `checkConnected()`
**Purpose:** Verify player is logged in before action submission

**Returns:** `boolean`

**Side Effect:** If not connected, print "You must login first."

**Example:**
```java
if (!checkConnected()) {
    return;  // Skip action submission
}
```

## Integration Points

### With Server
- **Registration:** Client calls `server.registerPlayer(username, password)`
- **Login:** Client calls `server.login(username, password)` → stores returned `Player`
- **Action Submission:** Client calls `server.submitAction(action)`
- **Logout:** Client calls `server.logout(playerId)`

### With Player
- **Session Storage:** Client stores `player` reference after login
- **PlayerId:** Client uses `player.getPlayerId()` for action builder

### With PlayerSession
- **Session Storage:** Client stores `session` reference after login
- **Token Usage:** Future: Include JWT token in action requests

### With PlayerAction
- **Action Building:** Client uses `PlayerAction.Builder` to construct actions
- **Parameter Mapping:** Client prompts for parameters based on action type

## User Experience

### Error Messages
- **Not Logged In:** "You must login first."
- **Invalid Credentials:** "Invalid credentials"
- **Registration Failed:** "Error: {reason}" (e.g., "Password must be at least 6 characters")
- **Unknown Command:** "Unknown command: {command}. Type 'help' for available commands."
- **Unknown Action Type:** "Unknown action type: {type}"

### Success Messages
- **Registration:** "Registration successful!"
- **Login:** "Login successful! Welcome, {username}"
- **Action Submission:** "Action submitted successfully."
- **Logout:** "Logged out successfully."
- **Quit:** "Goodbye!"

### Help Text
```
Available commands:
  register - Create a new account
  login    - Login to your account
  action   - Submit a game action (requires login)
  logout   - Logout from your account
  quit     - Exit the client
  help     - Show this help message
```

## Thread Safety

### Single-Threaded
- **Main Thread:** All user input and server calls on main thread
- **No Concurrency:** Client is single-threaded (no race conditions)

### Future Multi-Threading
- **Network I/O:** Separate thread for receiving server messages (future)
- **Event Loop:** React-style event loop for async messages (future)

## Testing

### Manual Testing
1. Start server: `Server server = new Server(8080); server.start();`
2. Start client: `Client client = new Client(server); client.start();`
3. Execute commands in CLI
4. Verify responses match expected behavior

### Automated Testing
- **ServerTest:** Includes client interaction scenarios (registration, login, action submission)
- **Integration Tests:** Test full client-server flow end-to-end

## Known Limitations

### MVP Constraints
- **Direct Server Reference:** Not a real network client (no TCP/WebSocket)
- **Blocking I/O:** `Scanner.nextLine()` blocks until user input
- **No Server Messages:** Cannot receive unsolicited messages from server (e.g., chat, events)
- **No Async:** All actions synchronous (submit and wait)

### UX Limitations
- **No Command History:** Cannot use arrow keys to repeat commands
- **No Auto-Complete:** Must type full command and parameter names
- **No Color:** Plain text output (no ANSI colors)
- **No Progress Indicators:** No loading spinners for slow actions

### Security Limitations
- **Password Echo:** Password visible when typed (no masking)
- **No TLS:** Plaintext communication (future: HTTPS/WSS)

## Future Enhancements

1. **Network Protocol:** Replace direct `Server` reference with socket
   ```java
   Socket socket = new Socket("localhost", 8080);
   PrintWriter out = new PrintWriter(socket.getOutputStream());
   BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
   ```

2. **Async Messaging:** Receive server push notifications (chat, events)
   ```java
   Thread receiver = new Thread(() -> {
       while (true) {
           String message = in.readLine();
           System.out.println("[Server] " + message);
       }
   });
   receiver.start();
   ```

3. **JLine Library:** Rich CLI with history, auto-complete, colors
   ```java
   LineReader reader = LineReaderBuilder.builder()
       .terminal(terminal)
       .completer(new StringsCompleter("login", "register", "action"))
       .build();
   String line = reader.readLine("prompt> ");
   ```

4. **GUI Client:** Swing/JavaFX desktop client
   ```java
   Button loginButton = new Button("Login");
   loginButton.setOnAction(e -> handleLogin());
   ```

5. **Web Client:** HTML/JavaScript browser client
   ```javascript
   fetch('/api/login', {
       method: 'POST',
       body: JSON.stringify({ username, password })
   }).then(response => response.json());
   ```

6. **Mobile Client:** Android/iOS native app
   ```kotlin
   val retrofit = Retrofit.Builder()
       .baseUrl("https://api.adventure.com")
       .build()
   ```

7. **Password Masking:** Hide password input
   ```java
   Console console = System.console();
   char[] password = console.readPassword("Enter password: ");
   ```

## Example Transcript

```
=== !Adventure Client ===
Commands: login, register, action, logout, quit

> help
Available commands:
  register - Create a new account
  login    - Login to your account
  action   - Submit a game action (requires login)
  logout   - Logout from your account
  quit     - Exit the client
  help     - Show this help message

> register
Enter username: alice
Enter password: secure123
Registration successful!

> login
Enter username: alice
Enter password: secure123
Login successful! Welcome, alice

> action
Available actions: MOVE, HARVEST, CRAFT, ATTACK, TRADE, BUILD, CHAT, USE_ITEM, DROP_ITEM, PICK_UP_ITEM, TRANSFER_OWNERSHIP, JOIN_CLAN, LEAVE_CLAN
Enter action type: CHAT
Enter message: Hello, world!
Action submitted successfully.

> action
Available actions: MOVE, HARVEST, CRAFT, ATTACK, TRADE, BUILD, CHAT, USE_ITEM, DROP_ITEM, PICK_UP_ITEM, TRANSFER_OWNERSHIP, JOIN_CLAN, LEAVE_CLAN
Enter action type: MOVE
Enter targetRegionId: region-north
Action submitted successfully.

> logout
Logged out successfully.

> quit
Goodbye!
```

## Related Documentation

- `Server.md` - Server-side action processing
- `Player.md` - Player entity with session tracking
- `PlayerAction.md` - Action data model
- `AuthenticationManager.md` - Authentication flow
- `docs/design_decisions.md` - Client-server architecture
- `archive/PHASE_1.9_SUMMARY.md` - Phase 1.9 implementation details
