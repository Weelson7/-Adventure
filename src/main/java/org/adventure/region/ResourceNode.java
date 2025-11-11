package org.adventure.region;

/**
 * Represents a resource node (e.g., tree, ore vein, crop field) in the world.
 * Resources regenerate over time using a capped regeneration formula.
 */
public class ResourceNode {
    private final int id;
    private final ResourceType type;
    private final int x;
    private final int y;
    private final double rMax;        // Maximum resource quantity
    private final double regenRate;   // Regeneration rate per second
    private double currentQuantity;

    public enum ResourceType {
        WOOD,        // Renewable, fast regeneration
        ORE,         // Finite, very slow/zero regeneration
        CROPS,       // Renewable, moderate regeneration
        STONE,       // Finite, zero regeneration
        HERBS        // Renewable, moderate regeneration
    }

    public ResourceNode(int id, ResourceType type, int x, int y, double rMax, double regenRate) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.rMax = rMax;
        this.regenRate = regenRate;
        this.currentQuantity = rMax; // Start at full capacity
    }

    /**
     * Regenerate resources using the formula:
     * R(t+Δt) = R(t) + regenRate * Δt * (1 - R(t)/Rmax)
     * 
     * @param deltaTime Time elapsed in seconds
     */
    public void regenerate(double deltaTime) {
        if (currentQuantity >= rMax) {
            return; // Already at max
        }

        double regenerated = regenRate * deltaTime * (1.0 - currentQuantity / rMax);
        currentQuantity = Math.min(rMax, currentQuantity + regenerated);
    }

    /**
     * Harvest resources from this node.
     * 
     * @param amount Amount to harvest
     * @return Actual amount harvested (may be less if insufficient resources)
     */
    public double harvest(double amount) {
        double harvested = Math.min(amount, currentQuantity);
        currentQuantity -= harvested;
        return harvested;
    }

    // Getters
    public int getId() {
        return id;
    }

    public ResourceType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getRMax() {
        return rMax;
    }

    public double getRegenRate() {
        return regenRate;
    }

    public double getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(double quantity) {
        this.currentQuantity = Math.max(0, Math.min(rMax, quantity));
    }

    /**
     * Check if this resource node is depleted.
     */
    public boolean isDepleted() {
        return currentQuantity <= 0.0;
    }

    /**
     * Check if this resource node is at full capacity.
     */
    public boolean isFull() {
        return currentQuantity >= rMax;
    }
}
