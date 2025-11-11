# Economy & Resources

This document defines the core resource and economy rules needed to avoid ambiguity between multiple docs.

Key concepts
- Resource types: renewable (wood, crops), finite (ore veins), currency (coins).
- Storage: each structure or character has a storage capacity. Surplus may be lost or taxed depending on rules.

Regeneration model (suggested)
- Use a capped regeneration formula per resource node:
  - R(t+Δt) = R(t) + regenRate * Δt * (1 - R(t)/Rmax)
  - regenRate and Rmax are configured per-resource and per-biome.
- Finite nodes (veins) have very low regenRate (near 0) or zero.

Harvesting & depletion
- Harvest amount limited by tool efficiency and worker skill. Apply hit-rate reductions to prevent instant-clear exploitation.

## Taxation & economy
- Taxation cadence: weekly by in-game day (configurable). Apply percentage of taxable income to owner treasury.
- Example: taxRate = 0.05 (5%); taxCollected = floor( taxableIncome * taxRate ).

### Economic Model (currency, trade, pricing)

**Currency:**
- Base currency: "Gold Coins" (or configurable name). Used for trade, taxation, and asset transactions.
- Currency is tracked per-character and per-society (`treasury` field).

**Trade Mechanics:**
- **Barter**: direct item-for-item exchange. No currency involved; valuation based on item rarity and demand.
- **Currency-based trade**: items have base prices (defined per prototype). Actual transaction price modified by:
  - `tradeSkill` of buyer/seller (negotiation bonus/penalty)
  - Supply/demand (if dynamic economy enabled): scarce items cost more
  - Reputation between societies: higher reputation → better prices

**Pricing Model (static for MVP, extensible to dynamic):**
- Static: each item prototype has a `basePrice` field. Actual price = `basePrice * priceModifier` where `priceModifier` accounts for quality, enchantments, and negotiation.
- Dynamic (future): track regional supply/demand and adjust prices per-region based on resource availability and population needs.

**Integration with Societies:**
- Societies earn income via taxation (from owned structures), trade agreements, and tributes.
- Taxation income flows into society `treasury`; can be spent on infrastructure, military, diplomacy.
- Trade routes between societies generate ongoing income and increase `influence` metrics.

**Example Price Calculation:**
- Iron Sword: basePrice=100, quality=high (+20%), negotiation bonus (+10%) → finalPrice = 100 * 1.2 * 1.1 = 132 gold

Notes:
- For MVP, use static pricing. Add dynamic supply/demand system in later phases.
- Link to `docs/societies_clans_kingdoms.md` for how treasury and trade affect diplomacy.
- See `docs/design_decisions.md` for canonical currency name and tax rate defaults.


Balancing guidance
- Introduce soft caps on gather rates per-player to avoid runaway accumulation.
- Use event-driven resource booms (temporary multipliers) rather than permanent exponential increases.

Notes
- Formal economic simulation (market prices, supply/demand curves) is optional for MVP. Start with static prices and local trade rules.
