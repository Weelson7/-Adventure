package org.adventure.network;

import org.adventure.character.Character;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a connected player in the multiplayer game.
 * Links network session to in-game character.
 */
public class Player {
    private final String playerId;
    private final String username;
    private String characterId;  // Reference to Character entity
    private Character character;  // Cached character instance
    private PlayerSession session;
    private long lastActionTimestamp;
    private boolean authenticated;

    public Player(String username) {
        this.playerId = UUID.randomUUID().toString();
        this.username = username;
        this.authenticated = false;
        this.lastActionTimestamp = System.currentTimeMillis();
    }

    public Player(String playerId, String username, String characterId) {
        this.playerId = playerId;
        this.username = username;
        this.characterId = characterId;
        this.authenticated = false;
        this.lastActionTimestamp = System.currentTimeMillis();
    }

    // Getters
    public String getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    public String getCharacterId() {
        return characterId;
    }

    public Character getCharacter() {
        return character;
    }

    public PlayerSession getSession() {
        return session;
    }

    public long getLastActionTimestamp() {
        return lastActionTimestamp;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    // Setters
    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public void setCharacter(Character character) {
        this.character = character;
        if (character != null) {
            this.characterId = character.getId();
        }
    }

    public void setSession(PlayerSession session) {
        this.session = session;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void updateLastActionTimestamp() {
        this.lastActionTimestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(playerId, player.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }

    @Override
    public String toString() {
        return String.format("Player{id='%s', username='%s', characterId='%s', authenticated=%s}",
                playerId, username, characterId, authenticated);
    }
}
