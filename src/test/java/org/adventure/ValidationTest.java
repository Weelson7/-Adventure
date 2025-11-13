package org.adventure;

import org.adventure.character.Character;
import org.adventure.character.Race;
import org.adventure.network.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security tests for server-side action validation.
 */
public class ValidationTest {
    private ActionValidator validator;
    private Player player;
    private Character character;

    @BeforeEach
    public void setUp() {
        validator = new ActionValidator();
        player = new Player("testuser");
        player.setAuthenticated(true);
        
        character = new Character("char_1", "TestChar", Race.HUMAN);
        player.setCharacter(character);
    }

    @Test
    public void testValidMoveAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.MOVE)
                .parameter("x", 10)
                .parameter("y", 20)
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testMoveActionMissingParameters() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.MOVE)
                .parameter("x", 10)
                // Missing y parameter
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testUnauthenticatedPlayerRejected() {
        player.setAuthenticated(false);
        
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CHAT)
                .parameter("message", "Hello")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("not authenticated"));
    }

    @Test
    public void testActionBelongsToDifferentPlayer() {
        PlayerAction action = new PlayerAction.Builder("different_player", PlayerAction.ActionType.CHAT)
                .parameter("message", "Hello")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("does not belong to player"));
    }

    @Test
    public void testExpiredActionRejected() throws Exception {
        // Create action and wait for it to expire
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CHAT)
                .parameter("message", "Hello")
                .build();
        
        // Wait for action to expire (5 seconds + buffer)
        Thread.sleep(6000);
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("expired"));
    }

    @Test
    public void testValidHarvestAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.HARVEST)
                .parameter("resourceNodeId", "node_123")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testHarvestActionMissingResourceNodeId() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.HARVEST)
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("resourceNodeId"));
    }

    @Test
    public void testValidCraftAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CRAFT)
                .parameter("recipeId", "recipe_123")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testCraftActionMissingRecipeId() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CRAFT)
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("recipeId"));
    }

    @Test
    public void testValidAttackAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.ATTACK)
                .parameter("targetId", "npc_123")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testAttackActionMissingTargetId() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.ATTACK)
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("targetId"));
    }

    @Test
    public void testValidTradeAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.TRADE)
                .parameter("targetPlayerId", "player_2")
                .parameter("offeredItems", "item_1,item_2")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testTradeActionMissingParameters() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.TRADE)
                .parameter("targetPlayerId", "player_2")
                // Missing offeredItems
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
    }

    @Test
    public void testValidBuildAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.BUILD)
                .parameter("structureType", "house")
                .parameter("x", 10)
                .parameter("y", 20)
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testBuildActionMissingCoordinates() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.BUILD)
                .parameter("structureType", "house")
                .parameter("x", 10)
                // Missing y
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
    }

    @Test
    public void testValidChatAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CHAT)
                .parameter("message", "Hello, world!")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testChatActionWithEmptyMessage() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CHAT)
                .parameter("message", "   ")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("empty"));
    }

    @Test
    public void testChatActionWithTooLongMessage() {
        String longMessage = "a".repeat(501); // Exceeds 500 character limit
        
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CHAT)
                .parameter("message", longMessage)
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("too long"));
    }

    @Test
    public void testChatActionMissingMessage() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CHAT)
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("message"));
    }

    @Test
    public void testChatDoesNotRequireCharacter() {
        // Remove character from player
        player.setCharacter(null);
        
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CHAT)
                .parameter("message", "Hello")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testNonChatActionRequiresCharacter() {
        player.setCharacter(null);
        
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.MOVE)
                .parameter("x", 10)
                .parameter("y", 20)
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("requires a character"));
    }

    @Test
    public void testValidUseItemAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.USE_ITEM)
                .parameter("itemId", "item_123")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidDropItemAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.DROP_ITEM)
                .parameter("itemId", "item_123")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidPickUpItemAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.PICK_UP_ITEM)
                .parameter("itemId", "item_123")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidTransferOwnershipAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.TRANSFER_OWNERSHIP)
                .parameter("structureId", "struct_1")
                .parameter("targetPlayerId", "player_2")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidJoinClanAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.JOIN_CLAN)
                .parameter("clanId", "clan_123")
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidLeaveClanAction() {
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.LEAVE_CLAN)
                .build();
        
        ActionValidator.ValidationResult result = validator.validate(action, player);
        assertTrue(result.isValid());
    }

    @Test
    public void testNullActionThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            validator.validate(null, player);
        });
    }

    @Test
    public void testNullPlayerThrowsException() {
        PlayerAction action = new PlayerAction.Builder("player1", PlayerAction.ActionType.CHAT)
                .parameter("message", "Hello")
                .build();
        
        assertThrows(NullPointerException.class, () -> {
            validator.validate(action, null);
        });
    }

    @Test
    public void testValidationResultAccept() {
        ActionValidator.ValidationResult result = ActionValidator.ValidationResult.accept();
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testValidationResultReject() {
        ActionValidator.ValidationResult result = ActionValidator.ValidationResult.reject("Error message");
        assertFalse(result.isValid());
        assertEquals("Error message", result.getErrorMessage());
    }
}
