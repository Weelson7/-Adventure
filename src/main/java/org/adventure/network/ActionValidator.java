package org.adventure.network;

import org.adventure.character.Character;

import java.util.Map;
import java.util.Objects;

/**
 * Server-side validation for all player actions.
 * No client-side trust - all actions must be validated before execution.
 */
public class ActionValidator {
    private static final long MAX_ACTION_AGE_MS = 5000; // 5 seconds

    /**
     * Validates a player action before it can be executed.
     * 
     * @param action the action to validate
     * @param player the player attempting the action
     * @return ValidationResult with success/failure and error message
     */
    public ValidationResult validate(PlayerAction action, Player player) {
        Objects.requireNonNull(action, "action cannot be null");
        Objects.requireNonNull(player, "player cannot be null");

        // Check action belongs to player
        if (!action.getPlayerId().equals(player.getPlayerId())) {
            return ValidationResult.reject("Action does not belong to player");
        }

        // Check player is authenticated
        if (!player.isAuthenticated()) {
            return ValidationResult.reject("Player not authenticated");
        }

        // Check action is not too old (prevent replay attacks)
        long age = System.currentTimeMillis() - action.getTimestamp();
        if (age > MAX_ACTION_AGE_MS) {
            return ValidationResult.reject("Action expired (too old)");
        }

        // Check action is not from the future (clock sync issues)
        if (age < -1000) {  // Allow 1 second clock skew
            return ValidationResult.reject("Action timestamp is in the future");
        }

        // Validate action-specific requirements
        return validateActionType(action, player);
    }

    private ValidationResult validateActionType(PlayerAction action, Player player) {
        Character character = player.getCharacter();
        if (character == null && requiresCharacter(action.getType())) {
            return ValidationResult.reject("Action requires a character");
        }

        switch (action.getType()) {
            case MOVE:
                return validateMove(action, character);
            case HARVEST:
                return validateHarvest(action, character);
            case CRAFT:
                return validateCraft(action, character);
            case ATTACK:
                return validateAttack(action, character);
            case TRADE:
                return validateTrade(action, character);
            case BUILD:
                return validateBuild(action, character);
            case USE_ITEM:
                return validateUseItem(action, character);
            case DROP_ITEM:
                return validateDropItem(action, character);
            case PICK_UP_ITEM:
                return validatePickUpItem(action, character);
            case TRANSFER_OWNERSHIP:
                return validateTransferOwnership(action, character);
            case JOIN_CLAN:
                return validateJoinClan(action, character);
            case LEAVE_CLAN:
                return validateLeaveClan(action, character);
            case CHAT:
                return validateChat(action);
            default:
                return ValidationResult.reject("Unknown action type: " + action.getType());
        }
    }

    private boolean requiresCharacter(PlayerAction.ActionType type) {
        return type != PlayerAction.ActionType.CHAT;
    }

    private ValidationResult validateMove(PlayerAction action, Character character) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("x") || !params.containsKey("y")) {
            return ValidationResult.reject("MOVE action missing x or y parameters");
        }
        return ValidationResult.accept();
    }

    private ValidationResult validateHarvest(PlayerAction action, Character character) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("resourceNodeId")) {
            return ValidationResult.reject("HARVEST action missing resourceNodeId");
        }
        return ValidationResult.accept();
    }

    private ValidationResult validateCraft(PlayerAction action, Character character) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("recipeId")) {
            return ValidationResult.reject("CRAFT action missing recipeId");
        }
        return ValidationResult.accept();
    }

    private ValidationResult validateAttack(PlayerAction action, Character character) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("targetId")) {
            return ValidationResult.reject("ATTACK action missing targetId");
        }
        return ValidationResult.accept();
    }

    private ValidationResult validateTrade(PlayerAction action, Character character) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("targetPlayerId") || !params.containsKey("offeredItems")) {
            return ValidationResult.reject("TRADE action missing required parameters");
        }
        return ValidationResult.accept();
    }

    private ValidationResult validateBuild(PlayerAction action, Character character) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("structureType") || !params.containsKey("x") || !params.containsKey("y")) {
            return ValidationResult.reject("BUILD action missing required parameters");
        }
        return ValidationResult.accept();
    }

    private ValidationResult validateUseItem(PlayerAction action, Character character) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("itemId")) {
            return ValidationResult.reject("USE_ITEM action missing itemId");
        }
        return ValidationResult.accept();
    }

    private ValidationResult validateDropItem(PlayerAction action, Character character) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("itemId")) {
            return ValidationResult.reject("DROP_ITEM action missing itemId");
        }
        return ValidationResult.accept();
    }

    private ValidationResult validatePickUpItem(PlayerAction action, Character character) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("itemId")) {
            return ValidationResult.reject("PICK_UP_ITEM action missing itemId");
        }
        return ValidationResult.accept();
    }

    private ValidationResult validateTransferOwnership(PlayerAction action, Character character) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("structureId") || !params.containsKey("targetPlayerId")) {
            return ValidationResult.reject("TRANSFER_OWNERSHIP action missing required parameters");
        }
        return ValidationResult.accept();
    }

    private ValidationResult validateJoinClan(PlayerAction action, Character character) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("clanId")) {
            return ValidationResult.reject("JOIN_CLAN action missing clanId");
        }
        return ValidationResult.accept();
    }

    private ValidationResult validateLeaveClan(PlayerAction action, Character character) {
        // No parameters needed for leaving clan
        return ValidationResult.accept();
    }

    private ValidationResult validateChat(PlayerAction action) {
        Map<String, Object> params = action.getParameters();
        if (!params.containsKey("message")) {
            return ValidationResult.reject("CHAT action missing message");
        }
        String message = (String) params.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ValidationResult.reject("CHAT message cannot be empty");
        }
        if (message.length() > 500) {
            return ValidationResult.reject("CHAT message too long (max 500 characters)");
        }
        return ValidationResult.accept();
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult accept() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult reject(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
