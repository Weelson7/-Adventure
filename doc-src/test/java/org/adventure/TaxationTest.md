# TaxationTest

**Package:** `org.adventure`  
**Type:** JUnit 5 Test Class  
**Test Count:** 22 tests  
**Coverage:** 85%+ for taxation module

---

## Overview

`TaxationTest` validates the `TaxationSystem` and `TaxRecord` classes. Tests cover tax calculation, collection, payment recording, enforcement pipeline, and multi-structure scenarios.

---

## Test Categories

### 1. Parameter Validation (4 tests)

#### `testDefaultParameters()`
- Validates default tax system has correct parameters:
  - Tax rate: 0.05 (5%)
  - Cadence: 7 days
  - Grace period: 14 days
  - Seizure threshold: 21 days

#### `testCustomParameters()`
- Create system with custom parameters
- Validates all parameters set correctly

#### `testInvalidTaxRate()`
- Validates negative and >1.0 tax rates throw `IllegalArgumentException`

#### `testInvalidCadence()`
- Validates zero and negative cadence throws `IllegalArgumentException`

---

### 2. Registration (2 tests)

#### `testRegisterStructure()`
- Register structure at tick 0
- Validates TaxRecord created
- Validates nextTaxDueTick set to 7 days
- Validates initial state (no tax owed)

#### `testUnregisterStructure()`
- Register then unregister structure
- Validates TaxRecord removed
- Validates getTaxRecord returns null

---

### 3. Tax Calculation (2 tests)

#### `testCalculateTax()`
- Test tax calculation with default rate
- Validates 1000 income → 50 tax
- Validates floor operation: 1234.56 → 61

#### `testCalculateTaxWithCustomRate()`
- Test tax calculation with custom rate (10%)
- Validates 1000 income → 100 tax

---

### 4. Tax Collection (2 tests)

#### `testProcessTaxCollection()`
- Register structure
- Process tax with 1000 income
- Validates taxOwed = 50
- Validates taxableIncome = 1000
- Validates nextTaxDueTick updated

#### `testProcessTaxCollectionNotRegistered()`
- Attempt to process tax for unregistered structure
- Validates throws `IllegalArgumentException`

---

### 5. Payment Recording (3 tests)

#### `testRecordPayment()`
- Register, process tax (owed = 50)
- Record partial payment (30)
- Validates outstanding balance = 20
- Record full payment (20)
- Validates isPaid = true
- Validates unpaidSinceTick cleared

#### `testRecordPaymentRejectsNegative()`
- Validates negative payment throws `IllegalArgumentException`

#### `testRecordPaymentNotRegistered()`
- Validates payment for unregistered structure throws `IllegalArgumentException`

---

### 6. Enforcement Pipeline (6 tests)

#### `testUpdateEnforcementGracePeriod()`
- Register, process tax (unpaid)
- Move to 10 days (within grace period)
- Validates inGracePeriod = true
- Validates underSeizureRisk = false
- Validates not in seizure list

#### `testUpdateEnforcementSeizureRisk()`
- Register, process tax (unpaid)
- Move to 25 days (past grace, before seizure)
- Validates inGracePeriod = false
- Validates underSeizureRisk = true
- Validates not in seizure list

#### `testUpdateEnforcementSeizure()`
- Register, process tax (unpaid)
- Move to 42 days (seizure threshold)
- Validates structure in seizure list

#### `testUpdateEnforcementPaidStructuresNotSeized()`
- Register, process tax, pay in full
- Move to seizure threshold
- Validates paid structure not seized

#### `testGracePeriodExactThreshold()`
- Test exact boundary of grace period (21 days)
- Validates transitions from grace to seizure risk

#### `testSeizureExactThreshold()`
- Test exact boundary of seizure threshold (42 days)
- Validates structure added to seizure list at exact tick

---

### 7. Multi-Structure Taxation (2 tests)

#### `testMultipleStructuresTaxation()`
- Register 3 structures
- Process taxes for all
- Pay one structure in full
- Move to seizure threshold
- Validates 2 structures seized, 1 (paid) not seized

#### `testAccumulatedUnpaidTaxes()`
- Register structure
- Process tax 3 times without payment
- Validates taxes accumulate: 50 → 100 → 150

---

### 8. Query Methods (1 test)

#### `testGetAllTaxRecords()`
- Register 3 structures
- Validates getAllTaxRecords returns all 3

---

## Test Patterns

### Timeline Tests
```java
@Test
public void testUpdateEnforcementGracePeriod() {
    taxSystem.registerStructure("struct-001", 0);
    taxSystem.processTaxCollection("struct-001", 1000.0, 0);
    
    // Tax due at 7 days, unpaid starts there
    // Move to 10 days (within grace period)
    int gracePeriodTick = 10 * TICKS_PER_DAY;
    List<String> toSeize = taxSystem.updateEnforcement(gracePeriodTick);
    
    assertTrue(toSeize.isEmpty());
    
    TaxRecord record = taxSystem.getTaxRecord("struct-001");
    assertTrue(record.isInGracePeriod());
    assertFalse(record.isUnderSeizureRisk());
}
```

### Multi-Structure Tests
```java
@Test
public void testMultipleStructuresTaxation() {
    // Register 3 structures
    taxSystem.registerStructure("struct-001", 0);
    taxSystem.registerStructure("struct-002", 0);
    taxSystem.registerStructure("struct-003", 0);
    
    // Process taxes
    taxSystem.processTaxCollection("struct-001", 1000.0, 0);
    taxSystem.processTaxCollection("struct-002", 2000.0, 0);
    taxSystem.processTaxCollection("struct-003", 500.0, 0);
    
    // Pay one structure
    taxSystem.recordPayment("struct-002", 100.0, 100);
    
    // Check seizure
    int seizureTick = 42 * TICKS_PER_DAY;
    List<String> toSeize = taxSystem.updateEnforcement(seizureTick);
    
    assertEquals(2, toSeize.size());
    assertTrue(toSeize.contains("struct-001"));
    assertFalse(toSeize.contains("struct-002"));
    assertTrue(toSeize.contains("struct-003"));
}
```

---

## Coverage Analysis

### Covered Scenarios
- ✅ Default and custom parameters
- ✅ Parameter validation
- ✅ Structure registration/unregistration
- ✅ Tax calculation (including floor)
- ✅ Tax collection processing
- ✅ Payment recording (partial and full)
- ✅ Enforcement pipeline (grace → risk → seizure)
- ✅ Exact threshold boundaries
- ✅ Multi-structure scenarios
- ✅ Accumulated unpaid taxes
- ✅ Paid structures not seized

### Not Covered (Future Tests)
- ⏳ Fines during grace period
- ⏳ Tax rate per structure type
- ⏳ Payment plans/installments
- ⏳ Concurrent tax processing
- ⏳ Integration with Structure ownership transfer

---

## Test Execution

### Run All Taxation Tests
```bash
.\maven\mvn\bin\mvn.cmd test -Dtest=TaxationTest
```

### Run Specific Test
```bash
.\maven\mvn\bin\mvn.cmd test -Dtest=TaxationTest#testUpdateEnforcementSeizure
```

### Results
```
[INFO] Running org.adventure.TaxationTest
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
```

---

## Quality Gates

- ✅ All 22 tests passing
- ✅ 85%+ line coverage for TaxationSystem
- ✅ Zero failures in CI pipeline
- ✅ No flaky tests (100% deterministic)
- ✅ Exact boundary testing (grace, seizure)

---

## Time Calculation Reference

**Constant:** `TICKS_PER_DAY = 86400` (1 tick = 1 second)

### Key Timelines
- **Day 0**: Tax processed, owed recorded
- **Day 7**: Tax due (unpaidSinceTick set)
- **Day 21**: Grace period ends (7 + 14)
- **Day 42**: Seizure threshold (7 + 14 + 21)

---

## Design Decisions

1. **Why test exact boundaries?**: Off-by-one errors are common in time-based logic.

2. **Why test multi-structure?**: Ensures no cross-contamination between tax records.

3. **Why test accumulation?**: Critical for game balance; unpaid taxes must compound.

4. **Why test paid structures not seized?**: Prevents accidental seizure bug.

---

## Future Test Enhancements

1. **Parameterized Tests**: Test various tax rates and cadences
2. **Property-Based Tests**: Fuzz test with random incomes/payments
3. **Performance Tests**: Benchmark enforcement update with 1000+ structures
4. **Integration Tests**: Taxation + Structure + Region simulation

---

## References

- Source: `src/main/java/org/adventure/structure/TaxationSystem.java`
- Design: `docs/structures_ownership.md` → Taxation & Asset Management
- Specs: `docs/specs_summary.md` → Taxation Defaults
- Summary: `archive/PHASE_1.5_SUMMARY.md`
