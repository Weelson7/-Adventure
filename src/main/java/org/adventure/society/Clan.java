package org.adventure.society;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Represents a social organization (clan) with members, treasury, and diplomatic relationships.
 * Clans can merge, be destroyed, and engage in diplomatic interactions.
 * 
 * Design:
 * - Uses builder pattern for flexible construction
 * - Immutable after creation (modifications return new instances)
 * - Tracks founding tick for historical analysis
 * - Treasury operations validated to prevent negative balance
 * - Member operations maintain consistency
 * 
 * @see RelationshipRecord for diplomacy tracking
 */
public class Clan {
    private final String id;
    private final String name;
    private final ClanType type;
    private final List<String> members;
    private final double treasury;
    private final Map<String, RelationshipRecord> relationships;
    private final long foundingTick;
    private final long lastActiveTick;
    private final int schemaVersion;

    @JsonCreator
    private Clan(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("type") ClanType type,
            @JsonProperty("members") List<String> members,
            @JsonProperty("treasury") double treasury,
            @JsonProperty("relationships") Map<String, RelationshipRecord> relationships,
            @JsonProperty("foundingTick") long foundingTick,
            @JsonProperty("lastActiveTick") long lastActiveTick,
            @JsonProperty("schemaVersion") int schemaVersion) {
        this.id = Objects.requireNonNull(id, "Clan id cannot be null");
        this.name = Objects.requireNonNull(name, "Clan name cannot be null");
        this.type = Objects.requireNonNull(type, "Clan type cannot be null");
        this.members = List.copyOf(Objects.requireNonNull(members, "Members list cannot be null"));
        this.treasury = treasury;
        this.relationships = relationships != null ? Map.copyOf(relationships) : Map.of();
        this.foundingTick = foundingTick;
        this.lastActiveTick = lastActiveTick;
        this.schemaVersion = schemaVersion;

        if (treasury < 0) {
            throw new IllegalArgumentException("Treasury cannot be negative: " + treasury);
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Clan name cannot be empty");
        }
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public ClanType getType() { return type; }
    public List<String> getMembers() { return members; }
    public double getTreasury() { return treasury; }
    public Map<String, RelationshipRecord> getRelationships() { return relationships; }
    public long getFoundingTick() { return foundingTick; }
    public long getLastActiveTick() { return lastActiveTick; }
    public int getSchemaVersion() { return schemaVersion; }

    /**
     * Add a member to the clan.
     * @param memberId the character ID to add
     * @return new Clan instance with the member added
     * @throws IllegalArgumentException if member already exists
     */
    public Clan addMember(String memberId) {
        Objects.requireNonNull(memberId, "Member ID cannot be null");
        if (members.contains(memberId)) {
            throw new IllegalArgumentException("Member " + memberId + " already exists in clan " + id);
        }
        List<String> newMembers = new ArrayList<>(members);
        newMembers.add(memberId);
        return new Builder(this).members(newMembers).build();
    }

    /**
     * Remove a member from the clan.
     * @param memberId the character ID to remove
     * @return new Clan instance with the member removed
     * @throws IllegalArgumentException if member doesn't exist
     */
    public Clan removeMember(String memberId) {
        Objects.requireNonNull(memberId, "Member ID cannot be null");
        if (!members.contains(memberId)) {
            throw new IllegalArgumentException("Member " + memberId + " not found in clan " + id);
        }
        List<String> newMembers = new ArrayList<>(members);
        newMembers.remove(memberId);
        return new Builder(this).members(newMembers).build();
    }

    /**
     * Deposit funds into the treasury.
     * @param amount the amount to deposit (must be positive)
     * @return new Clan instance with updated treasury
     */
    public Clan deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive: " + amount);
        }
        return new Builder(this).treasury(treasury + amount).build();
    }

    /**
     * Withdraw funds from the treasury.
     * @param amount the amount to withdraw (must be positive and <= treasury)
     * @return new Clan instance with updated treasury
     * @throws IllegalArgumentException if amount exceeds treasury
     */
    public Clan withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive: " + amount);
        }
        if (amount > treasury) {
            throw new IllegalArgumentException(
                "Insufficient funds: attempting to withdraw " + amount + " but treasury only has " + treasury);
        }
        return new Builder(this).treasury(treasury - amount).build();
    }

    /**
     * Update the last active tick.
     * @param tick the current tick
     * @return new Clan instance with updated lastActiveTick
     */
    public Clan updateLastActiveTick(long tick) {
        return new Builder(this).lastActiveTick(tick).build();
    }

    /**
     * Add or update a relationship with another clan.
     * @param relationship the relationship record
     * @return new Clan instance with updated relationships
     */
    public Clan updateRelationship(RelationshipRecord relationship) {
        Objects.requireNonNull(relationship, "Relationship cannot be null");
        Map<String, RelationshipRecord> newRelationships = new HashMap<>(relationships);
        newRelationships.put(relationship.getTargetSocietyId(), relationship);
        return new Builder(this).relationships(newRelationships).build();
    }

    /**
     * Merge this clan with another clan.
     * Creates a new clan with combined members and treasuries.
     * @param other the clan to merge with
     * @param newId the ID for the merged clan
     * @param newName the name for the merged clan
     * @param currentTick the current game tick
     * @return new Clan representing the merged organization
     */
    public static Clan merge(Clan clan1, Clan clan2, String newId, String newName, long currentTick) {
        Objects.requireNonNull(clan1, "First clan cannot be null");
        Objects.requireNonNull(clan2, "Second clan cannot be null");
        Objects.requireNonNull(newId, "New clan ID cannot be null");
        Objects.requireNonNull(newName, "New clan name cannot be null");

        // Combine members (no duplicates)
        Set<String> combinedMembers = new HashSet<>();
        combinedMembers.addAll(clan1.getMembers());
        combinedMembers.addAll(clan2.getMembers());

        // Combine treasuries
        double combinedTreasury = clan1.getTreasury() + clan2.getTreasury();

        // Merge relationships (prefer clan1's relationships if both have the same target)
        Map<String, RelationshipRecord> combinedRelationships = new HashMap<>(clan1.getRelationships());
        clan2.getRelationships().forEach(combinedRelationships::putIfAbsent);

        return new Builder()
                .id(newId)
                .name(newName)
                .type(clan1.getType()) // Preserve the type of the first clan
                .members(new ArrayList<>(combinedMembers))
                .treasury(combinedTreasury)
                .relationships(combinedRelationships)
                .foundingTick(currentTick)
                .lastActiveTick(currentTick)
                .schemaVersion(1)
                .build();
    }

    /**
     * Check if the clan has a member.
     * @param memberId the member ID to check
     * @return true if the member exists
     */
    public boolean hasMember(String memberId) {
        return members.contains(memberId);
    }

    /**
     * Get the number of members.
     * @return member count
     */
    public int getMemberCount() {
        return members.size();
    }

    /**
     * Check if the clan is empty (no members).
     * @return true if no members
     */
    public boolean isEmpty() {
        return members.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clan clan = (Clan) o;
        return id.equals(clan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Clan{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", memberCount=" + members.size() +
                ", treasury=" + treasury +
                ", schemaVersion=" + schemaVersion +
                '}';
    }

    /**
     * Builder for creating Clan instances.
     */
    public static class Builder {
        private String id;
        private String name;
        private ClanType type = ClanType.CLAN;
        private List<String> members = new ArrayList<>();
        private double treasury = 0.0;
        private Map<String, RelationshipRecord> relationships = new HashMap<>();
        private long foundingTick = 0;
        private long lastActiveTick = 0;
        private int schemaVersion = 1;

        public Builder() {}

        public Builder(Clan clan) {
            this.id = clan.id;
            this.name = clan.name;
            this.type = clan.type;
            this.members = new ArrayList<>(clan.members);
            this.treasury = clan.treasury;
            this.relationships = new HashMap<>(clan.relationships);
            this.foundingTick = clan.foundingTick;
            this.lastActiveTick = clan.lastActiveTick;
            this.schemaVersion = clan.schemaVersion;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(ClanType type) {
            this.type = type;
            return this;
        }

        public Builder members(List<String> members) {
            this.members = new ArrayList<>(members);
            return this;
        }

        public Builder treasury(double treasury) {
            this.treasury = treasury;
            return this;
        }

        public Builder relationships(Map<String, RelationshipRecord> relationships) {
            this.relationships = new HashMap<>(relationships);
            return this;
        }

        public Builder foundingTick(long foundingTick) {
            this.foundingTick = foundingTick;
            return this;
        }

        public Builder lastActiveTick(long lastActiveTick) {
            this.lastActiveTick = lastActiveTick;
            return this;
        }

        public Builder schemaVersion(int schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }

        public Clan build() {
            return new Clan(id, name, type, members, treasury, relationships, 
                          foundingTick, lastActiveTick, schemaVersion);
        }
    }
}
