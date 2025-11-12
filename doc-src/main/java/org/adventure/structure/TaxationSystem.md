# TaxationSystem

**Package:** `org.adventure.structure`  
**Type:** Service Class (Stateful)  
**Default Parameters:** 5% rate, 7-day cadence, 14-day grace, 21-day seizure

---

## Overview

`TaxationSystem` manages taxation for structures in the game world. It calculates taxes, tracks payments, enforces grace periods, and identifies structures for seizure when taxes remain unpaid beyond thresholds.

---

## Default Parameters

From `docs/specs_summary.md`:

- **Tax Rate**: 0.05 (5%)
- **Tax Cadence**: 7 in-game days (604,800 ticks @ 1 tick/second)
- **Grace Period**: 14 in-game days (1,209,600 ticks)
- **Seizure Threshold**: 21 in-game days after grace (1,814,400 ticks)

---

## Class Structure

```java
public final class TaxationSystem {
    private final double defaultTaxRate;
    private final int taxCadenceDays;
    private final int gracePeriodDays;
    private final int seizureThresholdDays;
    private final Map<String, TaxRecord> taxRecords;
}
```

---

## Key Methods

### Construction
```java
// Default parameters
TaxationSystem taxSystem = new TaxationSystem();

// Custom parameters
TaxationSystem custom = new TaxationSystem(
    0.10,  // 10% tax rate
    14,    // 14-day cadence
    7,     // 7-day grace period
    30     // 30-day seizure threshold
);
```

### Registration
- **`registerStructure(structureId, currentTick)`**: Register structure for taxation
  - Creates initial TaxRecord
  - Sets first tax due at `currentTick + cadence`
  
- **`unregisterStructure(structureId)`**: Remove structure from taxation (e.g., when destroyed)

### Tax Calculation
- **`calculateTax(taxableIncome, taxRate)`**: Calculate tax owed
  - Formula: `floor(taxableIncome * taxRate)`
  - Uses defaultTaxRate if taxRate is null
  - Returns floored integer value

### Tax Collection
- **`processTaxCollection(structureId, taxableIncome, currentTick)`**: Process tax period
  - Calculates tax owed for current period
  - Accumulates to existing unpaid balance
  - Sets next tax due date
  - Marks as unpaid if not fully paid
  - Returns updated TaxRecord

### Payment
- **`recordPayment(structureId, amount, currentTick)`**: Record tax payment
  - Adds to paid amount
  - Clears unpaid status if fully paid
  - Resets grace/seizure flags
  - Returns updated TaxRecord

### Enforcement
- **`updateEnforcement(currentTick)`**: Update enforcement status for all structures
  - Skips paid structures
  - Checks unpaid duration
  - Updates grace period flag
  - Updates seizure risk flag
  - Returns list of structure IDs to seize
  
### Query Methods
- **`getTaxRecord(structureId)`**: Get tax record for structure
- **`getAllTaxRecords()`**: Get all tax records (unmodifiable)
- **`getStructuresInGracePeriod()`**: List of structure IDs in grace
- **`getStructuresUnderSeizureRisk()`**: List of structure IDs under seizure risk

---

## Enforcement Pipeline

### 1. Tax Due (Day 0)
- Tax processed via `processTaxCollection()`
- Tax owed recorded
- `unpaidSinceTick` set to next tax due date

### 2. Grace Period (Days 0-14)
- `isInGracePeriod = true`
- Structure owner receives warnings
- No penalties yet

### 3. Seizure Risk (Days 14-35)
- `isInGracePeriod = false`
- `isUnderSeizureRisk = true`
- Fines may be applied (future enhancement)
- Asset lien (restricted actions - future)

### 4. Seizure (Day 35+)
- Structure added to seizure list
- Government takes ownership (caller responsibility)
- Tax record cleared or archived

---

## Time Calculation

Conversion: **1 in-game day = 86,400 ticks** (assuming 1 tick = 1 second)

### Example Timeline
- **Day 0**: Tax processed, owed = 50
- **Day 7**: Next tax due (if unpaid, unpaidSinceTick = Day 7)
- **Day 21**: Grace period ends (7 + 14)
- **Day 42**: Seizure threshold (7 + 14 + 21)

---

## Usage Examples

### Register and Process Tax
```java
TaxationSystem taxSystem = new TaxationSystem();

// Register structure at tick 0
taxSystem.registerStructure("struct-001", 0);

// Process tax collection with 1000 income
TaxRecord record = taxSystem.processTaxCollection("struct-001", 1000.0, 0);
// record.getTaxOwed() == 50.0 (5% of 1000)
```

### Record Payment
```java
// Partial payment
taxSystem.recordPayment("struct-001", 30.0, 100);

// Complete payment
taxSystem.recordPayment("struct-001", 20.0, 200);

TaxRecord record = taxSystem.getTaxRecord("struct-001");
// record.isPaid() == true
```

### Update Enforcement
```java
// Move to seizure threshold (42 days)
int seizureTick = 42 * 86400;
List<String> toSeize = taxSystem.updateEnforcement(seizureTick);

// toSeize contains unpaid structures past threshold
for (String structureId : toSeize) {
    // Transfer ownership to government
    structure.transferOwnership(governmentId, OwnerType.GOVERNMENT, seizureTick);
}
```

---

## Validation Rules

### Construction
- `defaultTaxRate` must be between 0 and 1
- `taxCadenceDays` must be positive
- `gracePeriodDays` cannot be negative
- `seizureThresholdDays` cannot be negative

### Runtime
- Cannot process tax for unregistered structure
- Cannot record payment for unregistered structure
- Payment amount cannot be negative

---

## Related Classes

- **TaxRecord**: Tax tracking per structure
- **Structure**: Taxed entity
- **OwnerType**: Used for seizure (transfer to GOVERNMENT)

---

## Testing

**Test Class**: `TaxationTest.java`  
**Test Count**: 22 tests  
**Coverage**: 85%+

### Test Categories
- Parameter validation (4 tests)
- Registration (2 tests)
- Tax calculation (2 tests)
- Tax collection (2 tests)
- Payment recording (3 tests)
- Enforcement pipeline (6 tests)
- Multi-structure taxation (2 tests)
- Edge cases (1 test)

---

## Design Decisions

1. **Why floor tax amount?**: Prevents fractional currency issues; consistent with specs.

2. **Why accumulate unpaid taxes?**: Owner may skip multiple periods; debt must compound.

3. **Why separate grace and seizure thresholds?**: Allows warnings, fines, and escalation before seizure.

4. **Why mutable TaxRecords?**: Taxation is inherently stateful; immutability would create excessive object churn.

5. **Why updateEnforcement returns seizure list?**: Caller decides how to handle seizure (transfer ownership, notify admins, etc.).

---

## Future Enhancements

1. **Fines**: Multiplicative or additive fines during grace period
2. **Tax Rates by Structure Type**: Different rates for residential vs commercial
3. **Tax Holidays**: Temporary exemptions for events or disasters
4. **Payment Plans**: Allow installment payments with interest
5. **Bankruptcy**: Alternative to seizure for player structures
6. **Tax Audit Events**: Story events triggered by unpaid taxes

---

## Performance Considerations

- **O(n) enforcement update**: Iterates all tax records
- **O(1) lookups**: HashMap for tax records by structure ID
- **Optimization**: Consider caching unpaid structures list

---

## References

- Design: `docs/structures_ownership.md` → Taxation & Asset Management
- Specs: `docs/specs_summary.md` → Ownership, Taxation Defaults
- Summary: `archive/PHASE_1.5_SUMMARY.md`
