package org.adventure.structure;

/**
 * Represents an error encountered during structure placement validation.
 * Contains error type and detailed message for debugging/user feedback.
 * 
 * Design: BUILD_PHASE1.10.x.md → Phase 1.10.2
 */
public final class PlacementError {
    private final PlacementErrorType type;
    private final String message;
    
    public PlacementError(PlacementErrorType type, String message) {
        if (type == null) {
            throw new IllegalArgumentException("Placement error type cannot be null");
        }
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Placement error message cannot be null or empty");
        }
        
        this.type = type;
        this.message = message;
    }
    
    public PlacementErrorType getType() {
        return type;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return type + ": " + message;
    }
}
