# !Adventure — Phase 1.11.x: Core Gameplay Systems

**Version:** 0.1.0-SNAPSHOT (Phase 1.11.x)  
**Last Updated:** November 13, 2025  
**Status:** 🚧 CRITICAL GAP — Core Gameplay Mechanics Missing  
**Priority:** BLOCKING FOR MVP (Required for Playable Game)

---

## 🎯 Overview & Critical Gap

Phase 1.10.x added living world with NPCs, clans, and settlements. However, **critical gameplay systems** are still missing:

### ❌ What Players Can't Do Yet:
- ❌ **No way to gain experience or level up** (characters exist but don't grow)
- ❌ **No combat system** (can't fight NPCs, monsters, or other players)
- ❌ **No way to earn money** (gold exists in clan treasuries but no income for players)
- ❌ **No save/load UI** (persistence exists but no player-facing interface)
- ❌ **No death/respawn** (what happens when health reaches 0?)

### ✅ What We Have (Phase 1.1-1.10):
- Character data model with stats, traits, skills
- Item system with durability and rarity
- Clan treasuries with gold values
- SaveManager for JSON persistence
- Named NPCs with jobs and homes

**This Phase (1.11.x) Goals:**
1. ✅ Implement progression system (XP, leveling, skill improvement)
2. ✅ Implement combat system (attacks, damage, defense, death)
3. ✅ Implement economy system (earning gold, spending, prices)
4. ✅ Implement save/load system (manual saves, autosaves, character selection)
5. ✅ Implement reputation system (NPC relationships, faction standing)

---

## 🏗️ Phase 1.11.1: Progression System

### Goal
Implement character progression with experience, leveling, skill improvement, and achievements.

### Deliverables

#### 1. **ExperienceSystem.java**
**Purpose:** Manage XP gain, leveling, and stat distribution

**Features:**
- XP sources: combat, crafting, exploration, quests
- Level-up thresholds with exponential curve
- Stat points awarded on level-up
- Skill proficiency improvement through use

**Key Classes:**
```java
public class ExperienceSystem {
    private static final int BASE_XP_PER_LEVEL = 100;
    private static final double LEVEL_EXPONENT = 1.5;
    
    public int calculateXPForLevel(int level) {
        // Exponential: 100, 283, 520, 800, 1118...
        return (int)(BASE_XP_PER_LEVEL * Math.pow(level, LEVEL_EXPONENT));
    }
    
    public void awardExperience(Character character, int xp, ExperienceSource source);
    public boolean checkLevelUp(Character character);
    public void applyLevelUp(Character character);
}

public enum ExperienceSource {
    COMBAT_KILL(1.0),          // Full XP
    COMBAT_ASSIST(0.5),        // Half XP
    CRAFTING_SUCCESS(0.3),     // Scaled by item rarity
    CRAFTING_DISCOVERY(1.0),   // New recipe discovered
    EXPLORATION_NEW(0.5),      // First visit to region
    QUEST_COMPLETION(2.0),     // Double XP for quests
    SOCIAL_TRADE(0.1),         // Small XP for trading
    RESOURCE_HARVEST(0.2);     // Gathering resources
    
    private final double multiplier;
}

public class LevelUpReward {
    int statPoints;            // Points to allocate (default: 5)
    int skillPoints;           // Skill proficiency boost (default: 3)
    List<String> unlockedAbilities; // New abilities/spells
    int maxHealthIncrease;     // +10 per level
    int maxManaIncrease;       // +5 per level
}
```

**XP Calculation Examples:**
```java
// Combat: 50 XP base * enemy level * difficulty
awardExperience(player, 50 * enemyLevel * 1.5, COMBAT_KILL);

// Crafting: 10 XP * item rarity multiplier
awardExperience(player, 10 * rarityMultiplier, CRAFTING_SUCCESS);

// Quest: Quest XP from definition (100-1000)
awardExperience(player, quest.getRewardXP(), QUEST_COMPLETION);
```

---

#### 2. **SkillProgressionSystem.java**
**Purpose:** Improve skills through practice (Skyrim-style)

**Features:**
- Skills improve with use (not just level-ups)
- Proficiency levels: Novice → Apprentice → Journeyman → Expert → Master
- Skill XP separate from character XP
- Diminishing returns at high levels

**Key Classes:**
```java
public class SkillProgressionSystem {
    // Thresholds from docs/specs_summary.md
    private static final int NOVICE_THRESHOLD = 0;
    private static final int APPRENTICE_THRESHOLD = 100;
    private static final int JOURNEYMAN_THRESHOLD = 300;
    private static final int EXPERT_THRESHOLD = 600;
    private static final int MASTER_THRESHOLD = 1000;
    
    public void awardSkillXP(Character character, SkillType skill, int xp);
    public SkillProficiency calculateProficiency(int skillXP);
    public int getSkillBonus(SkillProficiency proficiency);
}

public enum SkillType {
    // Combat skills
    MELEE_COMBAT,
    RANGED_COMBAT,
    DEFENSE,
    
    // Crafting skills (already have proficiency in CraftingSystem)
    BLACKSMITHING,
    TAILORING,
    ALCHEMY,
    COOKING,
    
    // Utility skills
    STEALTH,
    PERSUASION,
    LOCKPICKING,
    TRADING,
    SURVIVAL
}
```

**Skill XP Gain:**
- **Melee Combat:** +5 XP per hit, +20 XP per kill
- **Crafting:** +10 XP per craft (already tracked in CraftingSystem)
- **Persuasion:** +15 XP per successful dialogue check
- **Trading:** +5 XP per trade, scaled by value

---

#### 3. **AchievementSystem.java**
**Purpose:** Track milestones and unlock rewards

**Features:**
- Achievement definitions (JSON-loadable)
- Progress tracking (kill 100 enemies, craft 50 items)
- Rewards: titles, cosmetics, stat bonuses
- Secret achievements

**Key Classes:**
```java
public class Achievement {
    String id;
    String name;
    String description;
    AchievementCategory category;
    AchievementCondition condition;
    AchievementReward reward;
    boolean hidden; // Secret achievement
    long unlockedTick; // When player unlocked it
}

public enum AchievementCategory {
    COMBAT,
    EXPLORATION,
    CRAFTING,
    SOCIAL,
    STORY,
    COLLECTION
}

public interface AchievementCondition {
    boolean check(Character character);
}

// Example conditions
public class KillCountCondition implements AchievementCondition {
    int requiredKills;
    String enemyType; // null = any enemy
}

public class CraftingCountCondition implements AchievementCondition {
    int requiredCrafts;
    ItemCategory category;
}

public class AchievementReward {
    String title; // "Dragonslayer", "Master Craftsman"
    int statBonus; // +5 to strength, etc.
    List<Item> items; // Special items
}
```

**Achievement Examples:**
- "First Blood" — Kill your first enemy (reward: +5 max health)
- "Master Blacksmith" — Reach Master in Blacksmithing (reward: Title)
- "World Explorer" — Visit all biome types (reward: +10% movement speed)
- "Wealthy" — Accumulate 10,000 gold (reward: Special merchant access)

---

#### 4. **Update Character.java**
Add progression tracking to existing Character class:

```java
// Add to Character class
private int experience;
private int level;
private int statPointsAvailable;
private Map<SkillType, Integer> skillXP;
private List<String> unlockedAchievements;
private int totalKills;
private int totalCrafts;
private int totalTradesCompleted;

// New methods
public void addExperience(int xp);
public boolean canLevelUp();
public void levelUp(Map<String, Integer> statAllocations);
public void improveSkill(SkillType skill, int xp);
public void unlockAchievement(String achievementId);
```

---

### Quality Gates (Phase 1.11.1)

**Progression:**
- [ ] XP awarded for all sources (combat, crafting, quests, exploration)
- [ ] Level-up increases stats and unlocks abilities
- [ ] Level 1→20 progression balanced (not too fast/slow)
- [ ] Stat allocation UI-ready (returns stat point costs)

**Skills:**
- [ ] Skills improve through practice (not just levels)
- [ ] Proficiency thresholds match specs_summary.md
- [ ] Skill bonuses affect related actions (+10% damage at Expert melee)

**Achievements:**
- [ ] At least 20 achievements defined
- [ ] Achievement progress tracked accurately
- [ ] Rewards applied on unlock

**Integration:**
- [ ] Character XP persists (save/load)
- [ ] Deterministic XP gain (same actions = same XP)
- [ ] No XP exploits (action spam prevention)

---

## 🏗️ Phase 1.11.2: Combat System

### Goal
Implement turn-based combat with attacks, defense, damage calculation, and death mechanics.

### Deliverables

#### 1. **CombatSystem.java**
**Purpose:** Core combat mechanics and resolution

**Features:**
- Turn-based combat (action economy)
- Attack types: melee, ranged, special abilities
- Damage calculation with stats and modifiers
- Defense mechanisms: armor, dodge, block
- Status effects: poison, stun, buff/debuff

**Key Classes:**
```java
public class CombatSystem {
    public CombatResult processCombat(
        Character attacker,
        Character defender,
        CombatAction action,
        long currentTick
    );
    
    public int calculateDamage(
        Character attacker,
        Item weapon,
        Character defender,
        CombatAction action
    );
    
    public boolean checkHit(Character attacker, Character defender);
    public CombatState initiateCombat(Character player, Character enemy);
}

public class CombatState {
    String combatId;
    List<Character> participants;
    int currentTurn;
    String currentActorId;
    Map<String, List<StatusEffect>> activeEffects;
    long startTick;
    boolean resolved;
}

public class CombatAction {
    CombatActionType type;
    String targetId;
    String itemUsed; // Weapon or consumable
    Map<String, Object> parameters;
}

public enum CombatActionType {
    MELEE_ATTACK,
    RANGED_ATTACK,
    SPECIAL_ABILITY,
    USE_ITEM,
    DEFEND,
    FLEE
}

public class CombatResult {
    boolean hit;
    int damageDealt;
    List<StatusEffect> appliedEffects;
    boolean targetDied;
    int xpAwarded;
    List<Item> loot;
    String description; // "You strike the goblin for 25 damage!"
}
```

**Damage Calculation:**
```java
public int calculateDamage(Character attacker, Item weapon, Character defender, CombatAction action) {
    // Base damage from weapon
    int baseDamage = weapon != null ? weapon.getBaseDamage() : attacker.getStrength() / 2;
    
    // Stat modifier (strength for melee, dexterity for ranged)
    int statMod = action.getType() == MELEE_ATTACK 
        ? attacker.getStrength() / 10 
        : attacker.getDexterity() / 10;
    
    // Skill modifier (skill level bonus)
    SkillType relevantSkill = action.getType() == MELEE_ATTACK 
        ? SkillType.MELEE_COMBAT 
        : SkillType.RANGED_COMBAT;
    int skillBonus = getSkillBonus(attacker, relevantSkill);
    
    // Total damage before defense
    int totalDamage = baseDamage + statMod + skillBonus;
    
    // Apply defender's armor and defense
    int armorReduction = defender.getEquippedArmor() != null 
        ? defender.getEquippedArmor().getArmorValue() 
        : 0;
    int defenseReduction = defender.getVitality() / 20;
    
    // Final damage (minimum 1)
    return Math.max(1, totalDamage - armorReduction - defenseReduction);
}
```

**Hit Chance:**
```java
public boolean checkHit(Character attacker, Character defender) {
    // Base 75% hit chance
    double baseChance = 0.75;
    
    // Attacker accuracy bonus (dexterity / 200)
    double accuracyBonus = attacker.getDexterity() / 200.0;
    
    // Defender evasion penalty (dexterity / 300)
    double evasionPenalty = defender.getDexterity() / 300.0;
    
    double finalChance = baseChance + accuracyBonus - evasionPenalty;
    
    // Clamp between 5% and 95%
    finalChance = Math.max(0.05, Math.min(0.95, finalChance));
    
    return Math.random() < finalChance;
}
```

---

#### 2. **StatusEffectSystem.java**
**Purpose:** Manage buffs, debuffs, and damage-over-time effects

**Key Classes:**
```java
public class StatusEffect {
    String id;
    StatusEffectType type;
    int duration; // In ticks
    int magnitude;
    long appliedTick;
    String sourceId; // Who applied it
}

public enum StatusEffectType {
    // Damage over time
    POISON(EffectCategory.DEBUFF),
    BLEED(EffectCategory.DEBUFF),
    BURNING(EffectCategory.DEBUFF),
    
    // Control effects
    STUNNED(EffectCategory.DEBUFF),
    ROOTED(EffectCategory.DEBUFF),
    SILENCED(EffectCategory.DEBUFF),
    
    // Buffs
    STRENGTH_BOOST(EffectCategory.BUFF),
    SPEED_BOOST(EffectCategory.BUFF),
    REGENERATION(EffectCategory.BUFF),
    SHIELD(EffectCategory.BUFF);
    
    private final EffectCategory category;
}

public enum EffectCategory {
    BUFF,
    DEBUFF,
    CONTROL
}
```

---

#### 3. **DeathSystem.java**
**Purpose:** Handle character death and respawn

**Features:**
- Death triggers: health reaches 0
- Death penalties: XP loss, item drop
- Respawn mechanics: location, cooldown
- Player vs NPC death differences

**Key Classes:**
```java
public class DeathSystem {
    public DeathResult processDeath(Character character, long currentTick);
    public RespawnInfo calculateRespawn(Character character);
    public List<Item> determineItemLoss(Character character);
}

public class DeathResult {
    int xpLost; // 10% of current level XP
    List<Item> droppedItems; // Some items dropped
    String respawnLocationId;
    long respawnCooldown; // 60 seconds default
    String deathMessage;
}

public class RespawnInfo {
    String locationId; // Home, town, or last save point
    int x;
    int y;
    int healthOnRespawn; // 50% max health
    int manaOnRespawn; // 50% max mana
}
```

**Death Penalties:**
- **Players:**
  - Lose 10% of current level XP (never de-level)
  - Drop 50% of equipped items (random selection)
  - Keep quest items and bound items
  - Respawn at home or last town visited
  - 60-second respawn cooldown

- **NPCs:**
  - Permanent death (can be looted)
  - Drop all equipped items
  - Generate "Death" story event
  - Remove from clan membership
  - Spouse becomes unmarried (NPCLifecycleManager integration)

---

### Quality Gates (Phase 1.11.2)

**Combat Mechanics:**
- [ ] Damage calculation includes weapon, stats, skills, armor
- [ ] Hit chance balanced (not too high or too low)
- [ ] Combat feels fair (player can win with skill/preparation)
- [ ] Status effects work correctly (duration, stacking)

**Death System:**
- [ ] Death triggers at health = 0
- [ ] Respawn works for players (location, health, cooldown)
- [ ] NPC death removes them from world permanently
- [ ] Item loss is fair (not punishing for new players)

**Integration:**
- [ ] Combat XP awards work (ExperienceSystem integration)
- [ ] Weapon durability decreases in combat
- [ ] Combat generates story events
- [ ] All combat actions validated server-side

---

## 🏗️ Phase 1.11.3: Economy System

### Goal
Implement currency flow, pricing, and economic simulation.

### Deliverables

#### 1. **EconomySystem.java**
**Purpose:** Manage currency, prices, and transactions

**Key Classes:**
```java
public class EconomySystem {
    public int calculatePrice(Item item, PriceContext context);
    public TransactionResult processTransaction(
        Character buyer,
        Character seller,
        List<Item> items,
        int agreedPrice
    );
    public void collectNPCJobIncome(NamedNPC npc, long currentTick);
}

public class PriceContext {
    double supply; // How common is this item?
    double demand; // How many players want it?
    double merchantSkill; // Seller's trading skill
    double reputation; // Buyer's reputation with seller
    double basePrice; // From item prototype
}

public class TransactionResult {
    boolean success;
    String failureReason;
    int finalPrice;
    List<Item> itemsTransferred;
    int goldTransferred;
}
```

**Price Calculation:**
```java
public int calculatePrice(Item item, PriceContext context) {
    int basePrice = item.getPrototype().getBaseValue();
    
    // Durability modifier (damaged items cheaper)
    double durabilityMod = item.getDurability() / (double)item.getMaxDurability();
    
    // Supply/demand (0.5x to 2.0x)
    double supplyDemandMod = context.getDemand() / context.getSupply();
    supplyDemandMod = Math.max(0.5, Math.min(2.0, supplyDemandMod));
    
    // Merchant skill (better traders get better prices)
    double merchantMod = 1.0 + (context.getMerchantSkill() / 100.0);
    
    // Reputation (friendly NPCs give discounts)
    double reputationMod = 1.0 - (context.getReputation() / 200.0); // Max 50% discount
    
    int finalPrice = (int)(basePrice * durabilityMod * supplyDemandMod * merchantMod * reputationMod);
    
    return Math.max(1, finalPrice);
}
```

---

#### 2. **IncomeSystem.java**
**Purpose:** Generate gold for NPCs and players

**Features:**
- NPC job income (based on NPCJob.productionValue)
- Player income sources: quests, selling, looting
- Clan treasury deposits from NPC workers

**Key Methods:**
```java
public class IncomeSystem {
    // Process NPC job income every 1000 ticks
    public void processNPCIncome(NamedNPC npc, Clan clan, long currentTick) {
        if (npc.getJob() == NPCJob.CHILD || npc.getJob() == NPCJob.UNEMPLOYED) {
            return;
        }
        
        // Get production value from job
        int income = npc.getJob().getProductionValue();
        
        // Add to clan treasury
        clan.deposit(income);
        
        // Track last payment
        npc.setLastIncomeTick(currentTick);
    }
    
    // Award quest gold to player
    public void awardQuestGold(Character player, int amount) {
        player.addGold(amount);
    }
    
    // Loot gold from defeated enemy
    public int calculateLootGold(Character enemy) {
        // NPCs carry gold based on level and job
        int baseGold = enemy.getLevel() * 10;
        if (enemy instanceof NamedNPC) {
            NamedNPC npc = (NamedNPC) enemy;
            baseGold += npc.getJob().getProductionValue() / 10;
        }
        return baseGold + (int)(Math.random() * baseGold * 0.5);
    }
}
```

---

#### 3. **TaxationSystem.java** (Phase 1.5 implementation)
**Purpose:** Collect taxes on structures and enforce penalties

**Features:**
- Weekly tax collection (from Phase 1.5 design)
- Grace period: 14 days
- Seizure after 21 days (structure becomes RUINS)

**Integration:**
```java
// In StructureLifecycleManager (Phase 1.10.3)
public void checkForNeglect(Structure structure, Clan owner) {
    int unpaidTicks = structure.getUnpaidTaxTicks();
    
    if (unpaidTicks > 21 * 7 * 1000) {
        // Convert to ruin (seizure)
        convertToRuin(structure);
    } else if (unpaidTicks > 14 * 7 * 1000) {
        // In grace period, send warning
        generateTaxWarningEvent(structure, owner);
    }
}
```

---

### Quality Gates (Phase 1.11.3)

**Economy Mechanics:**
- [ ] Prices adjust based on supply/demand
- [ ] NPC jobs generate gold for clan treasuries
- [ ] Players can earn gold from quests, combat, trading
- [ ] Tax system collects weekly, enforces penalties

**Balance:**
- [ ] Starting gold sufficient for basic needs (100-200 gold)
- [ ] Common items affordable (10-50 gold)
- [ ] Rare items expensive but obtainable (500-5000 gold)
- [ ] Money sinks prevent inflation (taxes, repairs, deaths)

**Integration:**
- [ ] Gold persists in save files
- [ ] Transactions logged for admin auditing
- [ ] No gold duplication exploits

---

## 🏗️ Phase 1.11.4: Save/Load System

### Goal
Implement player-facing save/load interface with autosaves and character management.

### Deliverables

#### 1. **SaveLoadManager.java**
**Purpose:** High-level save/load operations for players

**Features:**
- Manual save slots (named saves)
- Autosave every X minutes
- Character selection screen
- Save file management

**Key Classes:**
```java
public class SaveLoadManager {
    private SaveManager saveManager; // Existing Phase 1.8
    private AutosaveScheduler autosaveScheduler;
    
    public SaveSlot createManualSave(String characterId, String saveName);
    public List<SaveSlot> listSaves(String characterId);
    public GameState loadSave(String saveId);
    public void deleteSave(String saveId);
    public void enableAutosave(int intervalMinutes);
}

public class SaveSlot {
    String id;
    String characterId;
    String characterName;
    String saveName; // "Before Boss Fight", "Town #1"
    int characterLevel;
    String location;
    long saveTimestamp;
    long playTime; // Total ticks played
    String thumbnail; // Optional screenshot path
}

public class AutosaveScheduler {
    private int intervalTicks; // Default: 600000 (10 minutes)
    
    public void scheduleSave(Character character, long currentTick);
    public void cancelAutosaves();
    public void setInterval(int minutes);
}
```

---

#### 2. **CharacterManager.java**
**Purpose:** Manage multiple characters per player

**Key Classes:**
```java
public class CharacterManager {
    public Character createCharacter(String playerId, CharacterCreationData data);
    public List<CharacterSummary> listCharacters(String playerId);
    public void deleteCharacter(String characterId);
    public Character loadCharacter(String characterId);
}

public class CharacterCreationData {
    String name;
    Race race;
    Map<String, Integer> startingStats;
    String appearance; // Optional description
}

public class CharacterSummary {
    String id;
    String name;
    Race race;
    int level;
    String clanName;
    long lastPlayed;
    long totalPlayTime;
}
```

---

#### 3. **SaveCorruptionRecovery.java**
**Purpose:** Handle corrupted saves and backups

**Features:**
- Backup rotation (Phase 1.8 SaveManager has this)
- Corruption detection (JSON validation)
- Restore from backup

**Key Methods:**
```java
public class SaveCorruptionRecovery {
    public boolean validateSaveFile(String savePath);
    public List<String> listBackups(String characterId);
    public GameState restoreFromBackup(String backupPath);
}
```

---

### Quality Gates (Phase 1.11.4)

**Save System:**
- [ ] Manual saves work (named, stored, loadable)
- [ ] Autosave triggers at configured interval
- [ ] Character creation creates playable character
- [ ] Character deletion removes all saves

**Reliability:**
- [ ] Save files validated on load (no corruption)
- [ ] Backups preserved (N=5 from Phase 1.8)
- [ ] Save/load deterministic (same state after load)

**Performance:**
- [ ] Save operation < 1 second for typical world
- [ ] Load operation < 3 seconds
- [ ] Autosave doesn't interrupt gameplay

---

## 🏗️ Phase 1.11.5: Reputation System

### Goal
Implement NPC relationships and faction standing.

### Deliverables

#### 1. **ReputationSystem.java**
**Purpose:** Track player relationships with NPCs and factions

**Key Classes:**
```java
public class ReputationSystem {
    public void modifyReputation(String playerId, String targetId, int change, ReputationSource source);
    public int getReputation(String playerId, String targetId);
    public ReputationLevel getReputationLevel(int reputation);
}

public class ReputationRecord {
    String playerId;
    String targetId; // NPC or clan ID
    int reputation; // -100 to +100
    Map<ReputationSource, Integer> history;
    long lastModified;
}

public enum ReputationSource {
    QUEST_COMPLETION(+10),
    QUEST_FAILURE(-5),
    TRADE_COMPLETED(+2),
    KILLED_ALLY(-50),
    KILLED_ENEMY(+5),
    THEFT_CAUGHT(-20),
    DONATION(+3),
    INSULT(-5),
    COMPLIMENT(+3);
    
    private final int defaultChange;
}

public enum ReputationLevel {
    HATED(-100, -50, "Hated"),
    HOSTILE(-49, -25, "Hostile"),
    UNFRIENDLY(-24, -1, "Unfriendly"),
    NEUTRAL(0, 24, "Neutral"),
    FRIENDLY(25, 49, "Friendly"),
    HONORED(50, 74, "Honored"),
    REVERED(75, 100, "Revered");
    
    private final int min;
    private final int max;
    private final String label;
}
```

**Reputation Effects:**
- **Hated:** NPCs refuse to trade, guards attack on sight
- **Hostile:** Poor prices, NPCs avoid player
- **Unfriendly:** Standard prices, limited dialogue
- **Neutral:** Default state
- **Friendly:** 10% discount, more dialogue options
- **Honored:** 20% discount, access to special quests
- **Revered:** 30% discount, marriage proposals accepted

---

### Quality Gates (Phase 1.11.5)

**Reputation Mechanics:**
- [ ] Reputation changes tracked for all interactions
- [ ] Reputation affects prices, dialogue, and quest availability
- [ ] Reputation persists across sessions

**Balance:**
- [ ] Easy to reach Friendly (10-15 positive interactions)
- [ ] Hard to reach Revered (100+ positive interactions)
- [ ] Negative actions have weight (killing = -50)

---

## 📊 Testing Strategy

### Progression Tests
```java
@Test
public void testExperienceGain() {
    Character character = createTestCharacter(1);
    ExperienceSystem system = new ExperienceSystem();
    
    system.awardExperience(character, 100, COMBAT_KILL);
    
    assertEquals(100, character.getExperience());
}

@Test
public void testLevelUp() {
    Character character = createTestCharacter(1);
    character.setExperience(283); // Level 2 threshold
    
    ExperienceSystem system = new ExperienceSystem();
    boolean leveledUp = system.checkLevelUp(character);
    
    assertTrue(leveledUp);
    assertEquals(2, character.getLevel());
    assertEquals(5, character.getStatPointsAvailable());
}

@Test
public void testSkillProgression() {
    Character character = createTestCharacter(1);
    SkillProgressionSystem system = new SkillProgressionSystem();
    
    // Train melee combat to Apprentice
    system.awardSkillXP(character, SkillType.MELEE_COMBAT, 100);
    
    SkillProficiency prof = system.calculateProficiency(100);
    assertEquals(SkillProficiency.APPRENTICE, prof);
}
```

### Combat Tests
```java
@Test
public void testDamageCalculation() {
    Character attacker = createTestCharacter(10);
    attacker.setStrength(50);
    
    Item weapon = createTestWeapon(20); // 20 base damage
    
    Character defender = createTestCharacter(10);
    defender.setVitality(30);
    
    CombatSystem system = new CombatSystem();
    CombatAction action = new CombatAction(CombatActionType.MELEE_ATTACK);
    
    int damage = system.calculateDamage(attacker, weapon, defender, action);
    
    assertTrue(damage > 0);
    assertTrue(damage < 50); // Not unreasonably high
}

@Test
public void testDeath() {
    Character character = createTestCharacter(10);
    character.setHealth(0);
    
    DeathSystem system = new DeathSystem();
    DeathResult result = system.processDeath(character, 1000L);
    
    assertTrue(result.getXpLost() > 0);
    assertNotNull(result.getRespawnLocationId());
    assertEquals(60000, result.getRespawnCooldown());
}
```

### Economy Tests
```java
@Test
public void testPriceCalculation() {
    Item item = createTestItem(ItemRarity.COMMON);
    item.getPrototype().setBaseValue(100);
    
    PriceContext context = new PriceContext();
    context.setSupply(1.0);
    context.setDemand(1.0);
    context.setMerchantSkill(50);
    context.setReputation(0);
    
    EconomySystem system = new EconomySystem();
    int price = system.calculatePrice(item, context);
    
    assertEquals(100, price, 10); // Within 10% of base
}

@Test
public void testNPCIncome() {
    NamedNPC npc = createTestNPC(NPCJob.BLACKSMITH);
    Clan clan = createTestClan();
    int initialGold = clan.getTreasury();
    
    IncomeSystem system = new IncomeSystem();
    system.processNPCIncome(npc, clan, 1000L);
    
    assertEquals(initialGold + 100, clan.getTreasury()); // BLACKSMITH = 100 gold/1000 ticks
}
```

---

## 📁 File Structure

```
src/main/java/org/adventure/
├── progression/
│   ├── ExperienceSystem.java (NEW)
│   ├── ExperienceSource.java (NEW - enum)
│   ├── LevelUpReward.java (NEW)
│   ├── SkillProgressionSystem.java (NEW)
│   ├── SkillType.java (NEW - enum)
│   ├── AchievementSystem.java (NEW)
│   ├── Achievement.java (NEW)
│   └── AchievementCondition.java (NEW - interface)
├── combat/
│   ├── CombatSystem.java (NEW)
│   ├── CombatState.java (NEW)
│   ├── CombatAction.java (NEW)
│   ├── CombatResult.java (NEW)
│   ├── StatusEffectSystem.java (NEW)
│   ├── StatusEffect.java (NEW)
│   └── DeathSystem.java (NEW)
├── economy/
│   ├── EconomySystem.java (NEW)
│   ├── PriceContext.java (NEW)
│   ├── TransactionResult.java (NEW)
│   ├── IncomeSystem.java (NEW)
│   └── TaxationSystem.java (NEW - implements Phase 1.5 design)
├── saveload/
│   ├── SaveLoadManager.java (NEW - wraps SaveManager)
│   ├── SaveSlot.java (NEW)
│   ├── AutosaveScheduler.java (NEW)
│   ├── CharacterManager.java (NEW)
│   └── SaveCorruptionRecovery.java (NEW)
├── reputation/
│   ├── ReputationSystem.java (NEW)
│   ├── ReputationRecord.java (NEW)
│   ├── ReputationSource.java (NEW - enum)
│   └── ReputationLevel.java (NEW - enum)
└── character/
    └── Character.java (MODIFIED: add XP, level, skills, achievements)

src/test/java/org/adventure/
├── progression/
│   ├── ExperienceSystemTest.java (NEW)
│   ├── SkillProgressionSystemTest.java (NEW)
│   └── AchievementSystemTest.java (NEW)
├── combat/
│   ├── CombatSystemTest.java (NEW)
│   ├── StatusEffectSystemTest.java (NEW)
│   └── DeathSystemTest.java (NEW)
├── economy/
│   ├── EconomySystemTest.java (NEW)
│   ├── IncomeSystemTest.java (NEW)
│   └── TaxationSystemTest.java (NEW)
├── saveload/
│   ├── SaveLoadManagerTest.java (NEW)
│   └── CharacterManagerTest.java (NEW)
└── reputation/
    └── ReputationSystemTest.java (NEW)
```

---

## 🚀 Implementation Order

### Week 1: Progression & Skills
**Days 1-3:** ExperienceSystem + LevelUpReward  
**Days 4-5:** SkillProgressionSystem  
**Days 6-7:** AchievementSystem + tests

### Week 2: Combat System
**Days 1-3:** CombatSystem (damage, hit chance, actions)  
**Days 4-5:** StatusEffectSystem + DeathSystem  
**Days 6-7:** Integration tests + balance tuning

### Week 3: Economy & Reputation
**Days 1-3:** EconomySystem + PriceContext + IncomeSystem  
**Days 4-5:** TaxationSystem + ReputationSystem  
**Days 6-7:** Integration tests

### Week 4: Save/Load & Polish
**Days 1-3:** SaveLoadManager + CharacterManager  
**Days 4-5:** AutosaveScheduler + corruption recovery  
**Days 6-7:** Full integration tests + documentation

---

## 📈 Success Metrics

**Phase 1.11.1 Complete When:**
- [ ] Characters gain XP from all sources
- [ ] Level-up increases stats and unlocks abilities
- [ ] Skills improve through practice
- [ ] At least 20 achievements implemented

**Phase 1.11.2 Complete When:**
- [ ] Combat works with damage, hit chance, status effects
- [ ] Death triggers at health = 0
- [ ] Respawn works correctly for players and NPCs
- [ ] Combat feels balanced (not too easy/hard)

**Phase 1.11.3 Complete When:**
- [ ] Prices adjust based on supply/demand
- [ ] NPCs generate gold for clan treasuries
- [ ] Players can earn and spend gold
- [ ] Tax system enforces penalties

**Phase 1.11.4 Complete When:**
- [ ] Manual saves work (create, load, delete)
- [ ] Autosave triggers without interrupting gameplay
- [ ] Character creation and selection work
- [ ] Save corruption recovery functional

**Phase 1.11.5 Complete When:**
- [ ] Reputation tracks all player-NPC interactions
- [ ] Reputation affects prices and dialogue
- [ ] Reputation persists across sessions

**Overall Phase 1.11.x Complete When:**
- [ ] All 5 sub-phases complete
- [ ] 534+ tests passing (Phase 1) + 100+ new tests
- [ ] Game is actually playable (can fight, level up, earn gold, save/load)
- [ ] All systems integrate with existing Phase 1 backend

---

## 🔗 Related Documentation

- **Design Docs:**
  - [Characters, Stats, Traits, Skills](docs/characters_stats_traits_skills.md)
  - [Economy & Resources](docs/economy_resources.md)
  - [Persistence & Versioning](docs/persistence_versioning.md)

- **Build Guides:**
  - [Main Build Guide](BUILD.md) — Phase 1.1-1.10 complete
  - [Phase 1.10.x Guide](BUILD_PHASE1.10.x.md) — Living World systems
  - [Gameplay Build Guide](BUILD-GAMEPLAY.md) — UI development (next after this)
  - [Phase 2 Build Guide](BUILD_PHASE2.md) — Advanced systems (post-MVP)

- **Implementation:**
  - [Phase 1.5 Summary](archive/PHASE_1.5_SUMMARY.md) — Structures & taxation
  - [Phase 1.8 Summary](archive/PHASE_1.8_SUMMARY.md) — Persistence (SaveManager)
  - [Phase 1.9 Summary](archive/PHASE_1.9_SUMMARY.md) — Multiplayer (server validation)

---

**END OF BUILD_PHASE1.11.x.md**
