package org.adventure.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Simple text-based client for connecting to the game server.
 * Provides a CLI interface for player interaction.
 */
public class Client {
    private final Server server; // In real implementation, this would be a network connection
    private PlayerSession session;
    private Player player;
    private boolean connected;

    public Client(Server server) {
        this.server = server;
        this.connected = false;
    }

    /**
     * Starts the client CLI.
     */
    public void start() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== !Adventure Client ===");
        System.out.println("Commands: login, register, action, logout, quit");
        
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            String[] parts = line.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            
            try {
                switch (command) {
                    case "register":
                        handleRegister(scanner);
                        break;
                    case "login":
                        handleLogin(scanner);
                        break;
                    case "action":
                        if (checkConnected()) {
                            handleAction(scanner);
                        }
                        break;
                    case "logout":
                        handleLogout();
                        break;
                    case "quit":
                    case "exit":
                        handleLogout();
                        System.out.println("Goodbye!");
                        scanner.close();
                        return;
                    case "help":
                        printHelp();
                        break;
                    default:
                        System.out.println("Unknown command. Type 'help' for available commands.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handleRegister(Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        try {
            server.registerPlayer(username, password);
            System.out.println("Registration successful! You can now login.");
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void handleLogin(Scanner scanner) {
        if (connected) {
            System.out.println("Already logged in. Logout first.");
            return;
        }
        
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        try {
            session = server.login(username, password);
            player = server.getPlayer(session.getPlayerId());
            connected = true;
            System.out.println("Login successful! Welcome, " + username);
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private void handleAction(Scanner scanner) {
        System.out.println("Action types: MOVE, HARVEST, CRAFT, ATTACK, CHAT");
        System.out.print("Action type: ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        
        try {
            PlayerAction.ActionType type = PlayerAction.ActionType.valueOf(typeStr);
            
            Map<String, Object> params = new HashMap<>();
            
            // Get parameters based on action type
            switch (type) {
                case MOVE:
                    System.out.print("X coordinate: ");
                    int x = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Y coordinate: ");
                    int y = Integer.parseInt(scanner.nextLine().trim());
                    params.put("x", x);
                    params.put("y", y);
                    break;
                case HARVEST:
                    System.out.print("Resource node ID: ");
                    String nodeId = scanner.nextLine().trim();
                    params.put("resourceNodeId", nodeId);
                    break;
                case CRAFT:
                    System.out.print("Recipe ID: ");
                    String recipeId = scanner.nextLine().trim();
                    params.put("recipeId", recipeId);
                    break;
                case CHAT:
                    System.out.print("Message: ");
                    String message = scanner.nextLine().trim();
                    params.put("message", message);
                    break;
                default:
                    System.out.println("Action type not fully implemented in client yet.");
                    return;
            }
            
            PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), type)
                    .parameters(params)
                    .build();
            
            server.submitAction(action);
            System.out.println("Action submitted: " + action.getActionId());
            
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid action type.");
        } catch (Exception e) {
            System.out.println("Error submitting action: " + e.getMessage());
        }
    }

    private void handleLogout() {
        if (connected && player != null) {
            server.logout(player.getPlayerId());
            connected = false;
            session = null;
            player = null;
            System.out.println("Logged out successfully.");
        } else {
            System.out.println("Not logged in.");
        }
    }

    private boolean checkConnected() {
        if (!connected) {
            System.out.println("Not logged in. Please login first.");
            return false;
        }
        return true;
    }

    private void printHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  register - Register a new account");
        System.out.println("  login    - Login to your account");
        System.out.println("  action   - Perform an action (MOVE, HARVEST, CRAFT, CHAT, etc.)");
        System.out.println("  logout   - Logout from your account");
        System.out.println("  quit     - Exit the client");
        System.out.println("  help     - Show this help message\n");
    }

    /**
     * Main method for running standalone client.
     */
    public static void main(String[] args) {
        // For MVP, client and server run in same process
        Server server = new Server(8080);
        server.start();
        
        Client client = new Client(server);
        client.start();
        
        server.stop();
    }
}
