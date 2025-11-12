# TaxRecord

**Package:** `org.adventure.structure`  
**Type:** Mutable Data Class (package-private setters)  
**Schema Version:** 1

---

## Overview

`TaxRecord` tracks taxation status for a single structure. It records taxable income, amounts owed/paid, unpaid duration, grace period status, and seizure risk. TaxRecords are managed by `TaxationSystem`.

---

## Class Structure

```java
public final class TaxRecord {
    // Identity
    private final String structureId;
    private final int schemaVersion;
    
    // Tax timeline
    private final int lastTaxTick;
    private int nextTaxDueTick;
    
    // Financial
    private double taxableIncome;
    private double taxOwed;
    private double taxPaid;
    
    // Enforcement tracking
    private int unpaidSinceTick;
    private boolean inGracePeriod;
    private boolean underSeizureRisk;
    private int gracePeriodStartTick;
}
```

---

## Key Methods

### Query Methods
- **`isPaid()`**: Returns true if `taxPaid >= taxOwed`
- **`getOutstandingBalance()`**: Returns `max(0, taxOwed - taxPaid)`

### Getters
All fields have standard getters:
- `getStructureId()`, `getLastTaxTick()`, `getNextTaxDueTick()`
- `getTaxableIncome()`, `getTaxOwed()`, `getTaxPaid()`
- `getUnpaidSinceTick()`, `isInGracePeriod()`, `isUnderSeizureRisk()`
- `getGracePeriodStartTick()`, `getSchemaVersion()`

### Setters (Package-Private)
Only `TaxationSystem` can modify:
- `setNextTaxDueTick(int)`
- `setTaxableIncome(double)`
- `setTaxOwed(double)`
- `setTaxPaid(double)`
- `setUnpaidSinceTick(int)`
- `setInGracePeriod(boolean)`
- `setUnderSeizureRisk(boolean)`
- `setGracePeriodStartTick(int)`

All setters enforce non-negative values for financial fields.

---

## Builder Pattern

### Construction
```java
TaxRecord record = new TaxRecord.Builder()
    .structureId("struct-001")
    .lastTaxTick(0)
    .nextTaxDueTick(604800)  // 7 days
    .taxableIncome(1000.0)
    .taxOwed(50.0)
    .taxPaid(0.0)
    .unpaidSinceTick(604800)
    .inGracePeriod(false)
    .underSeizureRisk(false)
    .schemaVersion(1)
    .build();
```

---

## Lifecycle States

### 1. Registered (Initial)
```
taxOwed: 0
taxPaid: 0
unpaidSinceTick: 0
inGracePeriod: false
underSeizureRisk: false
```

### 2. Tax Processed (Unpaid)
```
taxOwed: 50.0
taxPaid: 0.0
unpaidSinceTick: 604800 (next tax due tick)
inGracePeriod: false (not yet reached unpaid tick)
underSeizureRisk: false
```

### 3. In Grace Period
```
taxOwed: 50.0
taxPaid: 0.0
unpaidSinceTick: 604800
inGracePeriod: true
underSeizureRisk: false
```

### 4. Under Seizure Risk
```
taxOwed: 50.0
taxPaid: 0.0
unpaidSinceTick: 604800
inGracePeriod: false
underSeizureRisk: true
```

### 5. Paid
```
taxOwed: 50.0
taxPaid: 50.0
unpaidSinceTick: 0 (cleared)
inGracePeriod: false
underSeizureRisk: false
```

---

## Validation Rules

### At Construction
- `structureId` cannot be null or empty
- `taxableIncome` cannot be negative
- `taxOwed` cannot be negative
- `taxPaid` cannot be negative

### At Runtime
- All setters enforce non-negative values for financial fields

---

## Equality and Hashing

- **Equality**: Based on `structureId` only
- **Hash Code**: Based on `structureId` only

---

## Persistence

### JSON Schema (v1)
```json
{
  "structureId": "struct-001",
  "lastTaxTick": 0,
  "nextTaxDueTick": 604800,
  "taxableIncome": 1000.0,
  "taxOwed": 50.0,
  "taxPaid": 30.0,
  "unpaidSinceTick": 604800,
  "inGracePeriod": true,
  "underSeizureRisk": false,
  "gracePeriodStartTick": 1000000,
  "schemaVersion": 1
}
```

---

## Usage Examples

### Check Payment Status
```java
TaxRecord record = taxSystem.getTaxRecord("struct-001");

if (!record.isPaid()) {
    double owed = record.getOutstandingBalance();
    System.out.println("Outstanding: " + owed);
}
```

### Check Enforcement Status
```java
if (record.isInGracePeriod()) {
    // Display warning to player
    System.out.println("Grace period active. Pay within 14 days.");
}

if (record.isUnderSeizureRisk()) {
    // Display urgent warning
    System.out.println("SEIZURE IMMINENT! Pay immediately.");
}
```

---

## Related Classes

- **TaxationSystem**: Creates and manages TaxRecords
- **Structure**: Entity being taxed

---

## Testing

**Test Coverage**: Included in `TaxationTest.java` (22 tests)

### Key Tests
- `testRecordPayment()`: Partial and full payments
- `testUpdateEnforcementGracePeriod()`: Grace period logic
- `testUpdateEnforcementSeizureRisk()`: Seizure risk flagging
- `testUpdateEnforcementSeizure()`: Seizure threshold

---

## Design Decisions

1. **Why mutable?**: Taxation is inherently stateful; updating records in-place is more efficient than creating new immutable copies.

2. **Why package-private setters?**: Encapsulation; only `TaxationSystem` should modify records.

3. **Why separate grace and seizure flags?**: Different UI/warning levels for players; enables graduated response.

4. **Why track gracePeriodStartTick?**: Enables time-based fines or dynamic messaging ("5 days left in grace period").

---

## Future Enhancements

1. **Payment History**: Array of (amount, tick) tuples for audit trail
2. **Fine Amounts**: Track fines separately from base tax
3. **Tax Type**: Different tax types (property, income, sales)
4. **Discount Reasons**: Track exemptions or discounts applied

---

## References

- Design: `docs/structures_ownership.md` → Taxation & Asset Management
- Specs: `docs/specs_summary.md` → Taxation Defaults
- Summary: `archive/PHASE_1.5_SUMMARY.md`
