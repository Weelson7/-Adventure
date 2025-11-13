# Magic System Design - !Adventure

**Version:** 0.2.0 (Phase 2.1)  
**Last Updated:** November 13, 2025  
**Status:** Design Document

---

## Overview

The !Adventure magic system treats runes as a **magical language** where players combine rune "words" to form spell "sentences." This creates a flexible, emergent system where **thousands of unique spells** can be crafted through systematic rune combination.

**Key Features:**
- **15 Type runes** (spell verbs) + **28 Element runes** (7 base + 21 combinations) + **12 Modifier runes** = **55 total runes**
- **Up to 8 runes per spell** (1 Type + 1-5 Elements + 0-2 Modifiers)
- **Manual casting** (experimental) vs **Learned casting** (mastered)
- **Massive spell variety:** 10,000+ possible combinations
- **Risk/reward:** Longer incantations = more power BUT higher mana cost + lower stability

---

## Core Concept: Runes as Language

### Philosophical Foundation
- **Runes are Words:** Each rune represents a concept in the magical language
- **Spells are Sentences:** Combining runes in specific patterns creates spells
- **Grammar Matters:** Order and compatibility affect spell stability and power
- **Discovery Through Experimentation:** Players learn the language by trying combinations

### Design Goals
1. **Flexibility:** Players create unique spells matching their playstyle
2. **Depth:** Simple to learn, complex to master
3. **Risk/Reward:** Powerful spells require careful construction
4. **Emergent Gameplay:** Unexpected combinations create new strategies
5. **Social Learning:** Players share successful formulas

---

## Rune Categories

### 1. Type Runes (15 total) — "Verbs"
Define **what the spell does** (primary action). **Extensive variety for diverse spell creation.**

| Rune | Effect | Base Mana | Base Stability | Example Use |
|------|--------|-----------|----------------|-------------|
| **Projectile** | Launch ranged attack | 10 | 80 | Single-target damage |
| **Area** | Create area effect | 15 | 70 | AoE damage/buff |
| **Beam** | Channel continuous ray | 12 | 75 | Sustained damage |
| **Buff** | Apply enhancement to target | 8 | 90 | Strengthen ally/item |
| **Summon** | Create entity | 20 | 60 | Conjure minion |
| **Shield** | Create defensive barrier | 10 | 85 | Block damage |
| **Heal** | Restore vitality | 12 | 90 | Heal HP/remove debuff |
| **Curse** | Apply debuff to target | 10 | 75 | Weaken enemy |
| **Trap** | Create triggered effect | 14 | 65 | Place mine/rune |
| **Channel** | Continuous area effect | 18 | 60 | Aura, ongoing zone |
| **Conjure** | Create object/construct | 16 | 70 | Summon weapon/wall |
| **Burst** | Instant AoE from self | 12 | 75 | Shockwave/nova |
| **Strike** | Melee-range attack | 8 | 85 | Touch spell |
| **Ward** | Protective enchantment | 10 | 80 | Passive protection |
| **Ritual** | Long-cast powerful effect | 25 | 50 | Ultimate spells |

**Discovery:**
- **Projectile, Area, Shield, Strike:** Starting runes (known by all)
- **Heal, Buff, Ward:** Learn from mentors or temples
- **Beam, Curse, Burst:** Discover through experimentation or lore
- **Summon, Trap, Conjure:** Advanced runes, requires quest or high proficiency
- **Channel, Ritual:** Master-tier runes, very rare knowledge

---

### 2. Element Runes (28 total) — "Nouns"
Define **the spell's essence** (energy type).

#### Base Elements (7)
| Rune | Properties | Base Mana | Base Stability | Damage Type |
|------|-----------|-----------|----------------|-------------|
| **Null** | Raw mana, pure energy | +2 | 90 | Force/pure damage |
| **Fire** | Burning, high damage | +5 | 70 | Fire DoT |
| **Water** | Fluid, healing | +3 | 80 | Cold/heal |
| **Earth** | Solid, defensive | +4 | 85 | Physical/stun |
| **Wind** | Swift, ranged | +3 | 75 | Air/knockback |
| **Light** | Holy, purifying | +4 | 80 | Radiant/cure |
| **Darkness** | Shadow, draining | +5 | 70 | Necrotic/life drain |

#### Combined Elements (21)
Created by using multiple element runes in spell formula. **Players can combine 2-5 element runes per spell for massive variety.**

**Two-Element Combinations (21 examples):**
| Combo | Result | Properties | Stability Mod | Example Spell |
|-------|--------|-----------|---------------|---------------|
| **Null + Fire** | **Plasma** | Pure burning energy | 0 | Plasma bolt |
| **Null + Light** | **Arcane** | Raw magical force | +5 | Arcane missile |
| **Null + Darkness** | **Void** | Anti-magic, dispel | -5 | Void sphere |
| **Fire + Earth** | **Lava** | Slow, high DoT, area denial | -5 | Lava pool trap |
| **Fire + Wind** | **Lightning** | Fast, high damage, chain | -10 | Chain lightning |
| **Fire + Water** | **Steam** | Obscure, moderate damage | -15 (opposing) | Steam cloud |
| **Fire + Light** | **Radiance** | Blinding, fire+holy | +5 | Searing light |
| **Fire + Darkness** | **Inferno** | Corruption, fire+necrotic | -5 | Cursed flames |
| **Water + Wind** | **Ice** | Freeze, slow, shatter | +5 | Frozen shard |
| **Water + Earth** | **Nature** | Growth, healing, entangle | +10 | Vine snare |
| **Water + Light** | **Cleanse** | Purify, strong healing | +10 | Holy water |
| **Water + Darkness** | **Poison** | Toxic, DoT, weaken | -5 | Venom cloud |
| **Earth + Wind** | **Dust** | Disorient, armor break | 0 | Sand blast |
| **Earth + Light** | **Crystal** | Hard, reflective | +10 | Crystal shield |
| **Earth + Darkness** | **Decay** | Rot, weaken, slow | -5 | Withering curse |
| **Wind + Light** | **Thunder** | Loud, stunning, fast | 0 | Sonic boom |
| **Wind + Darkness** | **Gravity** | Pull/push, crush | -10 | Gravity well |
| **Light + Darkness** | **Twilight** | Balance, versatile | -20 (opposing) | Eclipse beam |
| **Null + Water** | **Mist** | Concealment, evasion | +5 | Mana mist |
| **Null + Earth** | **Metal** | Hardness, penetration | 0 | Metal shard |
| **Null + Wind** | **Force** | Kinetic energy, knockback | +5 | Force push |

**Three-Element Combinations (Examples):**
- **Fire + Wind + Null** = **Plasma Storm** (devastating AoE)
- **Water + Earth + Light** = **Sacred Grove** (healing sanctuary)
- **Fire + Earth + Darkness** = **Hellfire** (cursed lava)
- **Wind + Light + Null** = **Celestial Winds** (holy tornado)
- **Water + Wind + Darkness** = **Blizzard** (freezing darkness)

**Four-Element Combinations (Examples):**
- **Fire + Water + Wind + Earth** = **Elemental Chaos** (random effects)
- **Null + Fire + Wind + Light** = **Divine Storm** (holy lightning storm)
- **Earth + Water + Darkness + Wind** = **Toxic Cyclone** (poison tornado)

**Five-Element Combinations (Examples):**
- **Null + Fire + Water + Wind + Earth** = **Primordial Force** (unstable ultimate power)
- **Fire + Water + Earth + Wind + Light** = **Nature's Wrath** (complete elemental fury)
- **Null + Light + Darkness + Fire + Wind** = **Cosmic Annihilation** (extremely unstable)

**Element Interaction Rules:**
- **Compatible Elements:** (+5 to +10 stability) — Natural affinities
  - Water + Wind = Ice, Earth + Water = Nature, Fire + Light = Radiance, Null + Light = Arcane
- **Opposing Elements:** (-15 to -20 stability) — Conflict in nature
  - Fire + Water = Steam, Light + Darkness = Twilight
- **Neutral Elements:** (0 to -5 stability) — Functional but less synergy
  - Earth + Wind = Dust, Fire + Darkness = Inferno, Null + Earth = Metal
- **Null Element:** Works with all elements (versatile, +0 to +5 stability)

**Multi-Element Complexity Penalty:**
- **2 elements:** No penalty
- **3 elements:** -10 stability
- **4 elements:** -25 stability
- **5 elements:** -40 stability (extremely dangerous!)

**Discovery:**
- **Null, Fire, Water, Earth, Wind:** Starting elements (known by all)
- **Light, Darkness:** Discover through temples, dark rituals, or quests
- **Combined Elements:** Automatically discovered when using multiple element runes together

---

### 3. Modifier Runes (12 total) — "Adjectives"
Adjust **spell properties** (power, speed, cost, stability).

#### Power Modifiers
| Rune | Effect | Mana Mod | Stability Mod | Notes |
|------|--------|----------|---------------|-------|
| **Amplify** | +50% damage/effect | +10 | -15 | High risk, high reward |
| **Weaken** | -30% damage/effect | -5 | +20 | Safer, more stable |

#### Speed Modifiers
| Rune | Effect | Mana Mod | Stability Mod | Notes |
|------|--------|----------|---------------|-------|
| **Swift** | +50% cast speed | +5 | -10 | Quick casting |
| **Slow** | -30% cast speed | -3 | +15 | More deliberate |

#### Range Modifiers
| Rune | Effect | Mana Mod | Stability Mod | Notes |
|------|--------|----------|---------------|-------|
| **Extend** | +50% range | +8 | -10 | Long-distance |
| **Shorten** | -30% range | -4 | +10 | Melee range |

#### Duration Modifiers
| Rune | Effect | Mana Mod | Stability Mod | Notes |
|------|--------|----------|---------------|-------|
| **Prolong** | +100% duration | +12 | -10 | Long-lasting effects |
| **Hasten** | -50% duration | -6 | +10 | Quick burst |

#### Cost Modifiers
| Rune | Effect | Mana Mod | Stability Mod | Notes |
|------|--------|----------|---------------|-------|
| **Efficient** | -30% mana cost | -8 | +5 | Economical |
| **Reckless** | +50% damage | +15 | -25 | Dangerous power boost |

#### Stability Modifiers
| Rune | Effect | Mana Mod | Stability Mod | Notes |
|------|--------|----------|---------------|-------|
| **Stabilize** | +30 stability | +5 | +30 | Safer casting |
| **Volatile** | +30% damage | +3 | -30 | Unpredictable |

**Modifier Stacking Rules:**
- **Maximum 2 modifiers per spell** (prevents overpowered combos)
- **Conflicting modifiers:** Cannot use Amplify + Weaken, Swift + Slow, etc.
- **Effects are additive:** Amplify + Reckless = +50% + 50% = +100% damage (but very unstable)

**Discovery:**
- **Efficient, Stabilize, Swift:** Starting modifiers (known by all)
- **Others:** Discover through experimentation, mentors, lore books

---

## Spell Creation Grammar

### Syntax Structure (Fixed Order)
```
[TYPE] + [ELEMENT_1] + [ELEMENT_2] + [ELEMENT_3] + [ELEMENT_4] + [ELEMENT_5] + [MODIFIER_1] + [MODIFIER_2]
```

**Rules:**
1. **Exactly 1 Type rune** (required)
2. **1 to 5 Element runes** (at least 1 required, up to 5 for massive variety)
3. **0, 1, or 2 Modifier runes** (optional, stackable)
4. **Total: 2 to 8 runes per spell** (minimum TYPE + ELEMENT, maximum TYPE + 5 ELEMENTS + 2 MODIFIERS)

**Complexity Scaling:**
- **Longer incantations = Higher mana cost** (see Mana Cost Formula)
- **Longer incantations = Lower stability** (see Stability Calculation)
- **More runes = Higher backlash risk** but potentially more powerful effects

### Example Spell Formulas

**Simple Spells (2 runes):**
```
Projectile + Fire = Basic fireball
Shield + Earth = Stone wall
Heal + Water = Minor healing
Curse + Darkness = Weakness curse
Strike + Null = Mana-charged punch
```

**Intermediate Spells (3-4 runes):**
```
Projectile + Fire + Wind = Lightning bolt (fast fire projectile)
Area + Water + Wind + Light = Ice storm with radiant damage
Beam + Null + Amplify = Powerful arcane ray
Shield + Earth + Prolong = Long-lasting earth barrier
Buff + Light + Fire + Efficient = Radiant flame enhancement
```

**Advanced Spells (5-6 runes):**
```
Area + Fire + Earth + Wind = Lava tornado (3 elements, very unstable!)
Summon + Water + Earth + Light + Prolong = Sacred treant guardian
Burst + Null + Fire + Wind + Light + Amplify = Plasma nova (5 elements!)
Curse + Darkness + Null + Water + Volatile = Void poison curse
Channel + Fire + Water + Earth + Wind + Stabilize = Elemental aura
```

**Master Spells (7-8 runes - EXTREMELY DANGEROUS):**
```
Ritual + Null + Fire + Water + Earth + Wind + Light + Stabilize = Primordial summoning
Area + Fire + Water + Earth + Wind + Darkness + Amplify + Volatile = Apocalyptic storm (near-certain backlash!)
Projectile + Null + Light + Darkness + Fire + Wind + Swift + Reckless = Twilight comet (insane damage, 80%+ backlash)
```

**Invalid Formulas (Grammar Errors):**
```
❌ Fire + Water (no TYPE rune)
❌ Projectile + Projectile + Fire (duplicate TYPE)
❌ Projectile + Fire + Amplify + Weaken + Swift (3 modifiers, max is 2)
❌ Projectile + Fire + Water + Earth + Wind + Light + Darkness (6 elements, max is 5)
```

---

## Spell Calculations

### Mana Cost Formula
```
baseCost = typeCost + element1Cost + element2Cost + ... + elementNCost + modifier1Cost + modifier2Cost

// Longer incantations = exponentially higher mana cost
complexityMultiplier = 1.0 + (numRunes - 2) * 0.15 + (numRunes - 2)^2 * 0.05
// 2 runes = 1.0x
// 3 runes = 1.15x
// 4 runes = 1.5x
// 5 runes = 2.05x
// 6 runes = 2.8x
// 7 runes = 3.75x
// 8 runes = 4.9x (extremely expensive!)

proficiencyBonus = min(casterProficiency / 1000, 0.3)
// Max 30% reduction at 1000 proficiency (Master tier)

learnedSpellBonus = isLearned ? 0.2 : 0.0
// Learned spells cost 20% less mana

finalCost = baseCost * complexityMultiplier * (1 - proficiencyBonus - learnedSpellBonus)
```

**Examples:**
```
Simple Spell: Projectile + Fire
baseCost = 10 + 5 = 15
complexityMultiplier = 1.0
finalCost = 15 * 1.0 * 1.0 = 15 mana

Intermediate: Projectile + Fire + Wind + Amplify
baseCost = 10 + 5 + 3 + 10 = 28
complexityMultiplier = 1.5
proficiencyBonus = 300/1000 = 0.3
finalCost = 28 * 1.5 * 0.7 = 29.4 ≈ 29 mana

Advanced: Area + Null + Fire + Water + Wind + Earth + Amplify + Stabilize
baseCost = 15 + 2 + 5 + 3 + 3 + 4 + 10 + 5 = 47
complexityMultiplier = 4.9 (8 runes!)
proficiencyBonus = 1000/1000 = 0.3
learnedBonus = 0.2 (if learned)
finalCost = 47 * 4.9 * (1 - 0.3 - 0.2) = 115.15 ≈ 115 mana (very expensive!)
```

### Stability Calculation
```
// Base stability from runes
baseStability = (typeStability + element1Stability + ... + elementNStability + modifier1Stability + modifier2Stability) / numRunes
// Average stability to normalize

// Penalties (longer incantations = less stable!)
opposingElementPenalty = (Fire+Water or Light+Darkness) ? -20 per pair : 0
multiElementComplexityPenalty = 
  - 2 elements: 0
  - 3 elements: -10
  - 4 elements: -25
  - 5 elements: -40 (very unstable!)
complexityPenalty = (numRunes - 4) * -5  // -5 per rune above 4
manualCastPenalty = isLearned ? 0 : -20  // Manual casting less stable

// Bonuses
compatibleElementBonus = (compatible pair) ? +5 to +10 per pair : 0
proficiencyBonus = casterProficiency / 10  // +1 per 10 proficiency points
learnedSpellBonus = isLearned ? +15 : 0  // Learned spells more stable

finalStability = clamp(baseStability + penalties + bonuses, 0, 100)
```

**Examples:**
```
Simple Spell: Projectile + Fire (learned)
baseStability = (80 + 70) / 2 = 75
penalties = 0
proficiencyBonus = 300/10 = +30
learnedBonus = +15
finalStability = 75 + 30 + 15 = 120 → clamped to 100 (perfectly safe!)

Intermediate: Area + Fire + Wind + Amplify (manual)
baseStability = (70 + 70 + 75 - 15) / 4 = 50
multiElementPenalty = -10 (3 elements counting Fire+Wind)
manualCastPenalty = -20
proficiencyBonus = +30
finalStability = 50 - 10 - 20 + 30 = 50 (moderate risk)

Advanced: Ritual + Null + Fire + Water + Earth + Wind + Light + Volatile (manual, 8 runes, 5 elements!)
baseStability = (50 + 90 + 70 + 80 + 85 + 75 + 80 - 30) / 8 = 62.5
multiElementPenalty = -40 (5 elements!)
complexityPenalty = (8 - 4) * -5 = -20
opposingPenalty = -20 (Fire+Water)
manualCastPenalty = -20
proficiencyBonus = +100 (Master tier, 1000 proficiency)
finalStability = 62.5 - 40 - 20 - 20 - 20 + 100 = 62.5 ≈ 63% (high backlash risk!)
```

### Backlash Probability
```
backlashChance = (100 - finalStability) / 100 * backlashMultiplier

backlashMultiplier = 1.0 (default, tunable per environment)
// Magic zones might increase to 1.5x
```

**Example:**
```
finalStability = 60
backlashChance = (100 - 60) / 100 * 1.0 = 0.4 = 40% chance of backlash
```

### Backlash Severity
When backlash occurs, determine severity:
```
roll = random(0, 100)
severity = 
  if roll > 80: Minor (spell fizzles, 50% mana refunded)
  elif roll > 50: Moderate (spell fizzles, caster takes 15% max HP damage)
  elif roll > 20: Major (spell backfires, 40% damage + 10s debuff)
  else: Catastrophic (explosion, 60% damage + area damage to allies)
```

---

## Spell Effects by Type

### Projectile
- **Behavior:** Single-target ranged attack
- **Targeting:** Select enemy within range
- **Damage:** Based on element + modifiers
- **Travel Time:** Instant to 1 second depending on range

### Area
- **Behavior:** AoE effect centered on location
- **Targeting:** Select ground point or self-centered
- **Radius:** 3m base, +2m per Extend modifier
- **Damage/Effect:** All targets in radius

### Beam
- **Behavior:** Continuous channeled ray
- **Targeting:** Select direction, lock on target
- **Duration:** 3 seconds base, affected by Prolong
- **Damage:** Ticks every 0.5 seconds
- **Interruption:** Damage interrupts channel

### Enchant
- **Behavior:** Apply buff to target
- **Targeting:** Self, ally, or item
- **Duration:** 30 seconds base, affected by Prolong
- **Effect:** Based on element (Fire = +damage, Earth = +defense, etc.)

### Summon
- **Behavior:** Create NPC entity
- **Targeting:** Select ground point
- **Duration:** 60 seconds base, affected by Prolong
- **Entity:** Based on element (Fire = fire elemental, Nature = treant, etc.)
- **HP/Stats:** Scale with caster proficiency

### Shield
- **Behavior:** Create damage-absorbing barrier
- **Targeting:** Self or ally
- **Duration:** 20 seconds base, affected by Prolong
- **Absorption:** Based on element + modifiers
- **Break:** Shield breaks if absorption depleted

### Heal
- **Behavior:** Restore HP or remove debuff
- **Targeting:** Self or ally
- **Healing:** Based on element power + modifiers
- **Special:** Water heals HP, Light removes debuffs, Nature does both slowly

### Curse
- **Behavior:** Apply debuff to target
- **Targeting:** Select enemy
- **Duration:** 15 seconds base, affected by Prolong
- **Effect:** Based on element (Fire = DoT, Earth = slow, Dark = weaken, etc.)

---

## Spell Discovery & Learning

### Learning System: Manual vs Learned Casting

#### Manual Casting (Unlearned Spells)
Players can **freely experiment** with any runes they've discovered.
- **Cast Time:** 2x longer (e.g., 3s instead of 1.5s)
- **Error Chance:** 10-30% chance of miscast (spell fizzles, 50% mana refunded)
- **Stability Penalty:** -20 stability (higher backlash risk)
- **Mana Cost:** Full cost (no learned spell bonus)
- **Use Case:** Experimentation, discovery, emergency improvisation

#### Learned Casting (Mastered Spells)
Once a spell is **learned**, it can be cast automatically with benefits.
- **Cast Time:** Normal speed (1-3s depending on spell type)
- **Error Chance:** 0% (no miscast, reliable)
- **Stability Bonus:** +15 stability (safer casting)
- **Mana Cost:** -20% (efficiency from mastery)
- **Use Case:** Combat, reliable spellcasting, optimized builds

### How to Learn Spells

#### 1. Mentor Teaching (Fastest, Most Reliable)
- **NPCs:** Mage guilds, temples, wandering wizards
- **Cost:** Gold, quests, reputation
- **Result:** Instant spell learning (added to spellbook as "learned")
- **Examples:** 
  - Guild master teaches "Projectile + Fire + Amplify" for 500 gold
  - Temple priest teaches "Heal + Light + Water" after quest completion

#### 2. Spell Scrolls (Rare, Expensive)
- **Found:** Dungeons, libraries, enemy drops, merchant sales
- **Usage:** Consume scroll to learn spell permanently
- **Result:** Instant spell learning
- **Examples:**
  - Ancient scroll of "Area + Fire + Wind + Amplify" found in dragon hoard
  - Merchant sells "Shield + Earth + Prolong" scroll for 1000 gold

#### 3. Repetition (Slow, Free)
- **Method:** Manually cast the same spell many times
- **Threshold:** 50 successful casts (tunable) OR 100 total attempts
- **Progress:** Track in spellbook (e.g., "Mastery: 23/50 successful casts")
- **Result:** After threshold, spell becomes "learned" automatically
- **Examples:**
  - Cast "Projectile + Fire" 50 times → becomes learned
  - Cast "Ritual + Null + Fire + Water + Earth + Wind + Light" 100 times → becomes learned (if you survive!)

#### 4. Scholarly Study (Medium Speed, Requires Books)
- **Method:** Study spellbooks/tomes in libraries
- **Time:** 1 hour per spell (real-time or tick-based)
- **Requirements:** Access to library, specific tome
- **Result:** Spell learned after study period
- **Examples:**
  - Study "Elemental Magic Vol. 2" for 1 hour → learn "Area + Water + Wind"

### Discovery Methods (Runes, Not Spells)
Players discover **runes** (not full spells) through:
1. **Starting Knowledge:** Null, Fire, Water, Earth, Wind, Projectile, Area, Shield, Strike
2. **Quests:** Complete mage guild trials → unlock Summon rune
3. **Exploration:** Find ancient rune stones → unlock Ritual, Channel
4. **Mentors:** Pay NPC to teach Light, Darkness, Curse runes
5. **Experimentation:** Use Fire+Wind 10 times → "You feel the lightning element awaken" (unlock Lightning as named combo)

### Spellbook System
```
Spellbook Entry:
- Formula: "Projectile + Fire + Wind + Amplify"
- Name: "Lightning Bolt" (auto-generated or custom)
- Status: "Learned" or "Manual" (color-coded: green vs yellow)
- Mastery Progress: "47/50 successful casts" (if manual)
- Discovery Date: [timestamp]
- Success Rate: 85% (tracked over time)
- Times Cast: 142
- Fastest Cast: 1.2s
- Highest Damage: 487
- Notes: "Great for bosses, watch mana cost. Learned from Guild Master Aldric."
```

**Features:**
- **Status Indicator:** Green (learned), Yellow (manual), Red (unknown runes)
- **Mastery Tracker:** Progress bar toward learning via repetition
- **Auto-save:** All attempted spells logged (successful and failed)
- **Favorites:** Star frequently used spells for quick access
- **Search/Filter:** By rune, name, status, element, learned/manual
- **Export/Import:** Share formulas with friends (they still need to learn them!)
- **Failed Attempts:** Separate log for debugging/learning

---

## Proficiency & Mastery

### Magic Proficiency XP
Separate from crafting proficiency (see Phase 2.3).

**XP Gain:**
```
xp = baseXP * spellComplexity * successMultiplier

baseXP = 5
spellComplexity = numRunes (2-5)
successMultiplier = successful cast ? 1.0 : 0.5 (half XP for failures)
```

**Tiers:**
- Novice (0-99 XP): -10% mana cost, +5 stability
- Apprentice (100-299 XP): -15% mana cost, +10 stability
- Journeyman (300-599 XP): -20% mana cost, +15 stability
- Expert (600-999 XP): -25% mana cost, +20 stability
- Master (1000+ XP): -30% mana cost, +30 stability

### Specialization (Optional Phase 2 Feature)
- **Schools:** Evocation (damage), Abjuration (defense), Conjuration (summons), etc.
- **Bonus:** +10% effect for spells in school, +20% XP
- **Max:** 1 specialization per character

---

## Balance Considerations

### Power Budget
Each spell has a "power budget" to prevent overpowered combos:
```
powerBudget = 100 points

Costs:
- High damage: 40 points
- AoE: 30 points
- Long duration: 20 points
- High range: 15 points
- Low mana: 25 points

If powerBudget exceeded, reduce effect or increase cost.
```

### Spell Caps
- **Max Damage:** 200% of base (prevents one-shots)
- **Max Range:** 50m (prevents off-screen kills)
- **Max Duration:** 5 minutes (prevents permanent buffs)
- **Max Mana Cost:** 200 (prevents unusable spells)

### PvP Considerations
- **Diminishing Returns:** Repeated same-element hits on players reduce damage
- **CC Limits:** Max 3 seconds stun/freeze on players (half duration vs NPCs)
- **Dispel Availability:** Anti-magic runes accessible to all

---

## Integration with Other Systems

### Combat (Phase G.2)
- Spells use same combat action queue as melee/ranged attacks
- Casting time adds delay before spell fires
- Interruption on damage (if caster takes hit mid-cast)

### Items (Phase 1.4)
- Staves/wands reduce mana cost 10-20%
- Robes increase stability +5-15
- Enchanted items can store pre-made spells (1-3 charges)

### Societies (Phase 1.6)
- Mage guilds teach exclusive rune combinations
- Clan bonuses for magic-focused clans (+10% proficiency XP)

### Stories (Phase 1.7)
- Legendary spells tied to story events
- Ancient runes discovered in lore-rich regions

---

## UI/UX Requirements (for BUILD-GAMEPLAY.md)

### Spell Creation Interface
- **Rune Palette:** Drag-and-drop runes into formula slots
- **Formula Preview:** Real-time cost/stability/effect display
- **Validation:** Red highlight for invalid combos
- **Test Mode:** Cast spell at dummy target to preview

### Spellbook UI
- **List View:** All saved spells with icons
- **Detail View:** Rune formula, stats, notes
- **Quick Cast:** Assign spells to hotkeys (1-9)
- **Search/Filter:** By rune, element, type, name

### In-Combat UI
- **Spell Bar:** Show assigned spells with cooldowns
- **Mana Bar:** Current/max mana, regen rate
- **Casting Indicator:** Progress bar during cast
- **Backlash Warning:** Visual cue if stability <50%

---

## Implementation Notes

### Data Model
```java
public class Rune {
    String id;           // "fire", "projectile", "amplify"
    RuneCategory category; // TYPE, ELEMENT, MODIFIER
    int manaCost;
    int stabilityMod;
    Map<String, Object> properties;
}

public class Spell {
    String id;
    String name;         // Custom or auto-generated
    List<Rune> formula;  // Ordered list of runes
    int manaCost;        // Calculated
    int stability;       // Calculated
    SpellEffect effect;  // What it does
}

public class SpellEffect {
    SpellType type;      // PROJECTILE, AREA, etc.
    Element element;     // FIRE, LIGHTNING, etc.
    int baseDamage;
    int range;
    int duration;
    List<Modifier> modifiers;
}
```

### Spell Parsing
```java
public Spell parseSpell(List<Rune> runes) {
    // Validate grammar
    if (!isValidGrammar(runes)) {
        throw new InvalidSpellException("Invalid rune sequence");
    }
    
    // Extract components
    Rune typeRune = extractType(runes);
    List<Rune> elementRunes = extractElements(runes);
    List<Rune> modifierRunes = extractModifiers(runes);
    
    // Calculate properties
    int manaCost = calculateManaCost(runes);
    int stability = calculateStability(runes);
    
    // Build effect
    SpellEffect effect = buildEffect(typeRune, elementRunes, modifierRunes);
    
    return new Spell(runes, manaCost, stability, effect);
}
```

---

## Future Enhancements (Post-Phase 2.1)

### Phase 3+ Features
- **Ritual Magic:** Multi-caster cooperative spells
- **Rune Crafting:** Create custom runes (very rare)
- **Spell Evolution:** Spells improve with repeated use
- **Wild Magic Zones:** Random rune effects in chaotic areas
- **Metamagic:** Advanced modifiers (Twin Spell, Quicken Spell)

---

## References
- Implementation: BUILD_PHASE2.md → Phase 2.1
- Mana Defaults: docs/specs_summary.md
- Character Stats: docs/characters_stats_traits_skills.md
- Combat Integration: BUILD-GAMEPLAY.md → Phase G.2

---

**Status:** Design Complete — Ready for Implementation in Phase 2.1
