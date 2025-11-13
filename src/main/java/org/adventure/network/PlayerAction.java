package org.adventure.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a player action in the game.
 * All actions must be validated server-side before execution.
 */
public class PlayerAction {
    private final String actionId;
    private final String playerId;
    private final ActionType type;
    private final Map<String, Object> parameters;
    private final long timestamp;
    private ActionStatus status;
    private String errorMessage;

    public enum ActionType {
        MOVE,
        HARVEST,
        CRAFT,
        ATTACK,
        TRADE,
        BUILD,
        CHAT,
        USE_ITEM,
        DROP_ITEM,
        PICK_UP_ITEM,
        TRANSFER_OWNERSHIP,
        JOIN_CLAN,
        LEAVE_CLAN
    }

    public enum ActionStatus {
        PENDING,
        VALIDATED,
        REJECTED,
        EXECUTED,
        FAILED
    }

    private PlayerAction(Builder builder) {
        this.actionId = UUID.randomUUID().toString();
        this.playerId = builder.playerId;
        this.type = builder.type;
        this.parameters = new HashMap<>(builder.parameters);
        this.timestamp = System.currentTimeMillis();
        this.status = ActionStatus.PENDING;
    }

    public String getActionId() {
        return actionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public ActionType getType() {
        return type;
    }

    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }

    public void setStatusRejected(String errorMessage) {
        this.status = ActionStatus.REJECTED;
        this.errorMessage = errorMessage;
    }

    public void setStatusFailed(String errorMessage) {
        this.status = ActionStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerAction that = (PlayerAction) o;
        return Objects.equals(actionId, that.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionId);
    }

    @Override
    public String toString() {
        return String.format("PlayerAction{id='%s', playerId='%s', type=%s, status=%s, timestamp=%d}",
                actionId, playerId, type, status, timestamp);
    }

    public static class Builder {
        private final String playerId;
        private final ActionType type;
        private final Map<String, Object> parameters = new HashMap<>();

        public Builder(String playerId, ActionType type) {
            this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null");
            this.type = Objects.requireNonNull(type, "type cannot be null");
        }

        public Builder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        public PlayerAction build() {
            return new PlayerAction(this);
        }
    }
}
