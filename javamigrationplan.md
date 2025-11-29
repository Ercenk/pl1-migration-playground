# PSAM1 Java Migration Plan (Option 6)

Goal: Reimplement legacy PL/I program `PSAM1` as a modern, maintainable Java application while preserving functional behavior (transaction-driven reporting, detail and totals output, statistics collection). Ensure regression fidelity using golden output comparisons.

---
## 1. Scope & Constraints
- Preserve external behavior: report layout, numeric formatting, transaction codes (including trailing space significance for `PRINT `). 
- Output destination: initially console + optional file (mimicking `CUSTRPT`).
- Input sources: transaction file and customer file (fixed-width or delimited as per legacy layout; will formalize schema).
- Return value equivalent: average balance (`BALANCE_AVG`) => exposed via final summary and process exit code or returned from main orchestrator.

---
## 2. High-Level Architecture
Layered approach:
1. Infrastructure: File readers, line parsing utilities.
2. Domain Model: `Transaction`, `CustomerRecord`, `StatsContext`.
3. Services:
   - `TransactionHandler` interface with implementations: `PrintHandler`, `TotalsHandler`, `InvalidHandler`.
   - `StatsService` (aggregations akin to `PSAM2`).
4. Reporting:
   - `ReportWriter` (header, detail line, totals block, error lines).
   - Formatting helpers (number masks, line builders).
5. Orchestrator: `Psam1App` coordinates read loop, dispatch, lifecycle.
6. Configuration: constants for headers, formats (allow later externalization).
7. Regression Test Harness: Golden file comparison.

---
## 3. Data Model Design
```java
class Transaction {
    String code; // "PRINT " (with space) or "TOTALS" or other
    String rawLine; // Full 80 chars
    // Potential performance/test fields (CRUNCH params) kept if needed
}

class CustomerRecord {
    String recordType; // 'C' indicates customer
    String customerId;
    String name;
    String occupation;
    BigDecimal accountBalance; // Scale 2
    int ordersYtd;
}

class StatsContext {
    long totalCustomerRecords; // NUM_CUSTOMER_RECS
    long totalCustFileRecords; // NUM_CUSTFILE_RECS
    long totalTransactions;    // NUM_TRANSACTIONS
    long printRequests;
    long printCompleted;
    long totalsRequests;
    long totalsCompleted;
    long transactionErrors;    // NUM_TRAN_ERRORS

    BigDecimal balanceTotal; // BALANCE_TOTAL
    BigDecimal balanceMax;   // BALANCE_MAX
    BigDecimal balanceAvg;   // BALANCE_AVG (derived)

    void update(CustomerRecord cr) { /* compute totals/max/avg */ }
    void finalizeAverages() { /* compute avg from totalCustomerRecords */ }
}
```

---
## 4. File Parsing Strategy
- Adopt a fixed-width parser matching original column spans (needs specification from `%INCLUDE CUSTPLI`). If unknown, define provisional schema adjustable later.
- Transaction file: first 1 char may be '*'; next 6 chars = code; rest retained for echo.
- Customer file: parse recordType + fields; skip non-'C' records.
- Use streaming line-by-line with `BufferedReader`.

---
## 5. Formatting & Output Fidelity
- Implement number masks using `DecimalFormat`: `"###,##0.00"` for balances; `"###,###,##0"` for counts.
- Reproduce headers exactly (including spaces) via template constants.
- Manage page breaks optionally later (phase 2) unless required now. For initial migration treat continuous output.

---
## 6. Transaction Dispatch
```java
interface TransactionHandler {
    void handle(Transaction txn, Context ctx); // Context holds StatsContext, writers, file paths
    String code(); // registration key
}
```
- Registry map: `Map<String, TransactionHandler>`; keys include "PRINT " (retain space) and "TOTALS".
- Fallback handler for invalid codes increments error counters and writes message.

---
## 7. Stats Migration (PSAM2 Equivalent)
- Integrate logic into `StatsService.update(CustomerRecord)`; keep track of running total and max; compute average on demand.
- Maintain order of updates to match PL/I semantics (avg likely computed after all print processing; verify rounding behavior).

---
## 8. Error & Logging Strategy
- Use SLF4J or simple internal logger (initially `System.err` wrapped) for non-report log messages.
- Report messages that were PL/I `PUT FILE` retained in report output stream.
- Distinguish operational logs from business output for regression diff clarity.

---
## 9. Regression Testing
1. Capture sample legacy outputs (golden files). If unavailable, simulate with crafted inputs.
2. For each migration phase, run Java app and diff output normalized for line endings.
3. Add JUnit tests for:
   - Parsing correctness.
   - Stats calculations (max, total, avg with rounding).
   - Transaction dispatch (PRINT before TOTALS constraint).
4. Edge case tests: zero customers, only TOTALS request first, invalid transaction codes.

---
## 10. Phased Implementation Plan
| Phase | Deliverable | Description |
|-------|-------------|-------------|
| 0 | Spec Finalization | Confirm field widths & numeric semantics. |
| 1 | Core Models & Parsing | Implement `Transaction`, `CustomerRecord`, parsers. |
| 2 | Stats Service | Implement aggregation & tests. |
| 3 | ReportWriter | Headers, detail, totals formatting. |
| 4 | Handlers & Dispatch | PRINT, TOTALS, INVALID handlers. |
| 5 | Orchestrator | Main loop, lifecycle management. |
| 6 | Regression Harness | Golden file diff tooling. |
| 7 | Cleanup & Validation | Resource closing, final average return. |
| 8 | Optional Enhancements | Config externalization, logging framework. |

---
## 11. Detailed Task Lists
### Phase 0: Specification
- [ ] Extract actual column spec for customer file.
- [ ] Confirm transaction code exact spacing.
- [ ] Define numeric scale & rounding (half-up?).

### Phase 1: Models & Parsing
- [ ] Create `Transaction` class.
- [ ] Implement `TransactionParser` (skip comments).
- [ ] Create `CustomerRecord` class.
- [ ] Implement `CustomerRecordParser` (fixed-width version 1).
- [ ] Unit tests for parsing edge cases.

### Phase 2: Stats Service
- [ ] Implement `StatsContext` structure.
- [ ] Implement `StatsService.update()` logic (total, max).
- [ ] Implement average computation method.
- [ ] Tests: totals after multiple records; max updates.

### Phase 3: Report Writer
- [ ] Define header constants.
- [ ] Implement number formatting helpers.
- [ ] Implement `writeStartup()` (date/time lines).
- [ ] Implement `writeHeader()` for PRINT.
- [ ] Implement `writeDetail(CustomerRecord)`.
- [ ] Implement `writeTotals(StatsContext)`.
- [ ] Tests for format fidelity.

### Phase 4: Handlers & Dispatch
- [ ] Implement `PrintHandler` (opens customer file, loops records, updates stats).
- [ ] Implement `TotalsHandler` (guards against missing prior PRINT).
- [ ] Implement `InvalidHandler`.
- [ ] Register handlers map in `HandlerRegistry`.
- [ ] Tests for handler selection & error counting.

### Phase 5: Orchestrator
- [ ] Implement `Psam1App` main loop (read transaction stream, dispatch).
- [ ] Resource management (open/close files).
- [ ] Final average calculation & return.
- [ ] Integration test with combined sample inputs.

### Phase 6: Regression Harness
- [ ] Capture golden output sample.
- [ ] Implement diff utility (ignore trailing whitespace if needed).
- [ ] CI test comparing current output to golden.

### Phase 7: Cleanup & Validation
- [ ] Ensure all streams closed in `finally` or try-with-resources.
- [ ] Validate totals only after PRINT.
- [ ] Document any divergence from PL/I (if any). 

### Phase 8: Optional Enhancements
- [ ] Externalize headers & formats to properties file.
- [ ] Introduce structured logging (SLF4J).
- [ ] Add command-line args (input paths, output path).

---
## 12. Rounding & Numeric Considerations
- Use `BigDecimal` with `scale=2` for balances; apply `setScale(2, RoundingMode.HALF_UP)` after each addition if source format demands.
- Average = `balanceTotal.divide(new BigDecimal(totalCustomerRecords), 2, RoundingMode.HALF_UP)`.

---
## 13. Build & Packaging
- Use Maven (simple pom) or Gradle (Kotlin DSL) for project setup.
- Java version recommendation: 17 LTS.
- Modules: single module `psam1-core` initially.

---
## 14. Risk Mitigation
| Risk | Mitigation |
|------|------------|
| Formatting mismatch | Early report snapshot + dedicated tests. |
| Trailing space in code lost | Preserve raw 6-char field exactly; use constants. |
| Parsing errors due to unknown widths | Start with provisional spec; refactor once confirmed. |
| Numeric rounding drift | Centralize formatting & averaging; unit test extremes. |

---
## 15. Success Criteria
- Java output matches legacy output line-for-line for given golden inputs.
- All counters & statistics identical.
- Handlers easily extensible (add new code with minimal changes).
- Clear, test-covered parsing & stats logic.

---
## 16. Next Steps
Select Phase 0 tasks to begin specification capture. After confirming field widths, I can scaffold the Maven project and implement Phase 1.
