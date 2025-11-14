package org.adventure.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a named NPC for population simulation and clan membership.
 * 
 * <p><b>Design Note:</b> This is distinct from {@link org.adventure.character.NPC},
 * which handles combat AI and region-based spawning. NamedNPC focuses on:
 * <ul>
 *   <li>Clan membership and society simulation</li>
 *   <li>Lifecycle events (birth, marriage, reproduction, death)</li>
 *   <li>Economic roles (jobs, workplaces, production)</li>
 *   <li>Family relationships (spouse, children)</li>
 * </ul>
 * 
 * <p>For combat encounters and AI behavior, use {@link org.adventure.character.NPC}.
 * A NamedNPC can optionally be linked to a Character instance for stat tracking.
 * 
 * <p>NPCs can marry, reproduce, age, and die naturally.
 * Players are represented as NamedNPC instances with isPlayer=true.
 * 
 * @see org.adventure.character.NPC for combat/AI NPCs
 * @see org.adventure.society.Clan for clan management
 */
public class NamedNPC {
    private final String id;
    private final String name;
    private final String clanId;
    private int age;
    private final Gender gender;
    private NPCJob job;
    private final String homeStructureId;
    private String workplaceStructureId;
    private String spouseId;
    private final List<String> childrenIds;
    private int fertility;
    private final boolean isPlayer;
    private final long birthTick;
    private long lastReproductionCheck;
    
    // Optional link to Character instance for stats/combat (null for simple simulation)
    private String characterId;
    
    private NamedNPC(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.clanId = builder.clanId;
        this.age = builder.age;
        this.gender = builder.gender;
        this.job = builder.job;
        this.homeStructureId = builder.homeStructureId;
        this.workplaceStructureId = builder.workplaceStructureId;
        this.spouseId = builder.spouseId;
        this.childrenIds = new ArrayList<>(builder.childrenIds);
        this.fertility = builder.fertility;
        this.isPlayer = builder.isPlayer;
        this.birthTick = builder.birthTick;
        this.lastReproductionCheck = builder.lastReproductionCheck;
        this.characterId = builder.characterId;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getClanId() { return clanId; }
    public int getAge() { return age; }
    public Gender getGender() { return gender; }
    public NPCJob getJob() { return job; }
    public String getHomeStructureId() { return homeStructureId; }
    public String getWorkplaceStructureId() { return workplaceStructureId; }
    public String getSpouseId() { return spouseId; }
    public List<String> getChildrenIds() { return new ArrayList<>(childrenIds); }
    public int getFertility() { return fertility; }
    public boolean isPlayer() { return isPlayer; }
    public long getBirthTick() { return birthTick; }
    public long getLastReproductionCheck() { return lastReproductionCheck; }
    public String getCharacterId() { return characterId; }
    
    // Setters for mutable fields
    public void setAge(int age) { this.age = age; }
    public void setJob(NPCJob job) { this.job = job; }
    public void setWorkplaceStructureId(String workplaceStructureId) { 
        this.workplaceStructureId = workplaceStructureId; 
    }
    public void setSpouseId(String spouseId) { this.spouseId = spouseId; }
    public void addChild(String childId) { this.childrenIds.add(childId); }
    public void setFertility(int fertility) { this.fertility = fertility; }
    public void setLastReproductionCheck(long tick) { this.lastReproductionCheck = tick; }
    public void setCharacterId(String characterId) { this.characterId = characterId; }
    
    /**
     * Checks if this NPC is married.
     * 
     * @return true if spouseId is not null
     */
    public boolean isMarried() {
        return spouseId != null;
    }
    
    /**
     * Checks if this NPC is a child (under 18).
     * 
     * @return true if age < 18
     */
    public boolean isChild() {
        return age < 18;
    }
    
    /**
     * Checks if this NPC is of reproductive age (18-45).
     * 
     * @return true if age between 18 and 45
     */
    public boolean isReproductiveAge() {
        return age >= 18 && age <= 45;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedNPC namedNPC = (NamedNPC) o;
        return Objects.equals(id, namedNPC.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("NamedNPC{id='%s', name='%s', age=%d, gender=%s, job=%s, married=%s}", 
            id, name, age, gender, job, isMarried());
    }
    
    /**
     * Builder for creating NamedNPC instances.
     */
    public static class Builder {
        private String id;
        private String name;
        private String clanId;
        private int age;
        private Gender gender;
        private NPCJob job = NPCJob.UNEMPLOYED;
        private String homeStructureId;
        private String workplaceStructureId;
        private String spouseId;
        private List<String> childrenIds = new ArrayList<>();
        private int fertility;
        private boolean isPlayer = false;
        private long birthTick;
        private long lastReproductionCheck = 0;
        private String characterId;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder clanId(String clanId) { this.clanId = clanId; return this; }
        public Builder age(int age) { this.age = age; return this; }
        public Builder gender(Gender gender) { this.gender = gender; return this; }
        public Builder job(NPCJob job) { this.job = job; return this; }
        public Builder homeStructureId(String homeStructureId) { 
            this.homeStructureId = homeStructureId; 
            return this; 
        }
        public Builder workplaceStructureId(String workplaceStructureId) { 
            this.workplaceStructureId = workplaceStructureId; 
            return this; 
        }
        public Builder spouseId(String spouseId) { this.spouseId = spouseId; return this; }
        public Builder childrenIds(List<String> childrenIds) { 
            this.childrenIds = new ArrayList<>(childrenIds); 
            return this; 
        }
        public Builder fertility(int fertility) { this.fertility = fertility; return this; }
        public Builder isPlayer(boolean isPlayer) { this.isPlayer = isPlayer; return this; }
        public Builder birthTick(long birthTick) { this.birthTick = birthTick; return this; }
        public Builder lastReproductionCheck(long lastReproductionCheck) { 
            this.lastReproductionCheck = lastReproductionCheck; 
            return this; 
        }
        public Builder characterId(String characterId) {
            this.characterId = characterId;
            return this;
        }
        
        public NamedNPC build() {
            Objects.requireNonNull(id, "id cannot be null");
            Objects.requireNonNull(name, "name cannot be null");
            Objects.requireNonNull(clanId, "clanId cannot be null");
            Objects.requireNonNull(gender, "gender cannot be null");
            Objects.requireNonNull(homeStructureId, "homeStructureId cannot be null");
            
            if (age < 0) {
                throw new IllegalArgumentException("age cannot be negative");
            }
            
            return new NamedNPC(this);
        }
    }
}
