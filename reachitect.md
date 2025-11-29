# PSAM1 Rearchitecture Options

This document proposes multiple rearchitecture approaches for `PSAM1` based on observations in `analysis.md`. Each option includes: Goal, Key Changes, Pros, Cons/Risks, Estimated Effort (relative), and Suggested Incremental Steps.

---
## Option 1: Incremental Internal Refactor (Low Disruption)
**Goal:** Improve clarity and maintainability without changing external behavior or file interfaces.
**Key Changes:**
- Replace `DO WHILE (I > 0)` with explicit EOF loop conditions.
- Remove unused `TRAN_OK_FLAG` and correct duplicate `'ALL TYPES:'` label.
- Normalize record naming (`CUSTOMER_RECORD` vs `CUSTFILE_RECORD`).
- Add explicit `CLOSE` for all files.
- Extract PRINT and TOTALS transaction handlers into internal procedures with clear signatures.
**Pros:** Minimal risk; fastest improvement in readability.
**Cons/Risks:** Does not address deeper modularity, still monolithic.
**Effort:** Low.
**Steps:**
1. Introduce boolean loop conditions using EOF flags.
2. Standardize record variable names.
3. Factor PRINT logic into `DoPrintTransaction()` and totals into `DoTotalsTransaction()`.
4. Fix labels and remove vestigial variables.
5. Add final cleanup section before RETURN.

---
## Option 2: Modular Decomposition (Medium Scope)
**Goal:** Separate concerns: input parsing, reporting, statistics, and transaction dispatch.
**Key Changes:**
- Create separate internal procedures/files (if allowed) for:
  - `ReadTransaction()`
  - `ProcessPrint()`
  - `ProcessTotals()`
  - `UpdateStats()` wrapper around `PSAM2`.
- Encapsulate all counters/statistics in a single structure passed around.
- Centralize formatting definitions in one include (`REPORTFMT`).
**Pros:** Improved testability and clearer boundaries.
**Cons/Risks:** Increases number of interfaces; integration errors possible.
**Effort:** Medium.
**Steps:**
1. Define a `STAT_CTX` structure with all counters.
2. Replace global counter mutations with procedure calls.
3. Move format declarations to a dedicated include.
4. Implement dispatcher calling `ProcessPrint` / `ProcessTotals`.
5. Validate unchanged output via sample inputs.

---
## Option 3: Introduce Abstraction Layer for I/O (Strategic)
**Goal:** Decouple file I/O to enable alternate sources (e.g., memory, different file layouts) and easier unit testing.
**Key Changes:**
- Wrap `READ FILE` / `PUT FILE` in service procedures (`GetNextTransaction()`, `WriteReportLine()`).
- Add an adapter layer so file handles are not referenced directly in business logic.
- Use procedure parameters to pass current transaction context instead of globals.
**Pros:** Enables test harness and future replacement (e.g., reading from a DB).
**Cons/Risks:** Higher upfront complexity; may require pervasive changes.
**Effort:** Medium–High.
**Steps:**
1. Define I/O wrapper procedures.
2. Replace direct calls incrementally (start with transaction reads).
3. Move formatting selection logic into `ReportWriter` module.
4. Add a stub/mocked version for non-file testing.

---
## Option 4: Event/Callback Style Processing
**Goal:** Make transaction handling extensible by registering handlers.
**Key Changes:**
- Implement a table mapping `TRAN_CODE` to handler entries.
- Each handler receives transaction record + state context.
- Replace `SELECT` with lookup; unknown codes call a default error handler.
**Pros:** Extensible—new transaction types added without modifying core loop.
**Cons/Risks:** Overkill if transaction list remains tiny.
**Effort:** Medium.
**Steps:**
1. Create handler structure: `(CODE, ENTRY PROC)` array.
2. Migrate existing PRINT/TOTALS logic.
3. Add default/error handler.
4. Update main loop to perform lookup and invoke.

---
## Option 5: Full Domain Layer Separation
**Goal:** Formal separation into layers: Input Parsing, Domain (Stats & Validation), Reporting, Orchestration.
**Key Changes:**
- Domain layer procedures manipulate a domain model (customer, transaction, stats).
- Reporting only consumes domain models + formatting.
- Orchestrator drives sequence (open files, iterate, dispatch).
**Pros:** Clean architecture; facilitates future enhancements & portability.
**Cons/Risks:** Significant rewrite; may exceed acceptable change window.
**Effort:** High.
**Steps:**
1. Define domain record structures explicitly (avoid implicit include coupling).
2. Isolate formatting logic to a reporting module.
3. Centralize stats logic; remove scattered increments.
4. Refactor main loop to call domain functions then reporting.
5. Decommission global mutation pattern.

---
## Option 6: Modernization + Port (Cross-Language)
**Goal:** Reimplement in a more commonly used language (e.g., Java, Python, COBOL) while preserving behavior.
**Key Changes:**
- Define canonical input record schema (transaction + customer).
- Implement streaming parser and strategy pattern for transaction handlers.
- Use object model for stats; reporting via templating.
**Pros:** Easier access to modern tooling, CI/CD, libraries.
**Cons/Risks:** Risk of subtle differences (formatting, numeric rounding); migration cost.
**Effort:** High.
**Steps:**
1. Capture regression test cases from current PL/I output (golden files).
2. Extract record layouts; document numeric field semantics.
3. Implement parser + handlers in target language.
4. Validate output diff against golden files.
5. Plan phased cutover (dual-run, then replace).

---
## Option 7: Add Configuration & Extensibility
**Goal:** Externalize behavior (which reports to run, page headers, formats) to config (JSON/properties) to avoid code changes for format updates.
**Key Changes:**
- Introduce configuration loader at startup.
- Replace hard-coded header strings and format masks with config-driven values.
- Permit enabling/disabling transaction types via config.
**Pros:** Reduces future maintenance cost for simple changes.
**Cons/Risks:** Adds complexity; requires robust validation of config.
**Effort:** Medium.
**Steps:**
1. Define configuration schema.
2. Implement loader + validation.
3. Map current constants to config entries.
4. Replace usages incrementally.

---
## Option 8: Observability & Error Strategy Upgrade
**Goal:** Improve runtime diagnostics, error categorization, and recovery.
**Key Changes:**
- Standard error handler procedure (categorize: I/O, Data, Transaction).
- Add logging abstraction (severity levels) instead of raw `PUT` when not part of report output.
- Enhance invalid transaction handling to count and optionally skip totals.
**Pros:** Faster troubleshooting; foundation for monitoring.
**Cons/Risks:** Slight performance overhead; shifts some output semantics.
**Effort:** Low–Medium.
**Steps:**
1. Create `Log(severity, message)` procedure.
2. Refactor invalid transaction messaging.
3. Consolidate status updates into log calls.
4. Optional: Add summary of errors at program end.

---
## Comparative Summary
| Option | Scope | Risk | Main Benefit | Recommended When |
|--------|-------|------|--------------|------------------|
| 1 | Low | Very Low | Quick clarity | Immediate cleanup needed |
| 2 | Medium | Low-Med | Modularity | Ongoing maintenance expected |
| 3 | Med-High | Medium | Testability & IO flexibility | Future alternative sources planned |
| 4 | Medium | Medium | Extensible transactions | New types will be added |
| 5 | High | High | Clean layered architecture | Long-term strategic platform refactor |
| 6 | High | High | Modern ecosystem | Corporate mandate to move off PL/I |
| 7 | Medium | Medium | Config-driven adaptability | Frequent format/constant changes |
| 8 | Low-Med | Low | Better diagnostics | Operational visibility needed |

---
## Recommended Path (Pragmatic Roadmap)
1. Start with Option 1 to stabilize and clarify.
2. Move to Option 2 to modularize core logic.
3. If future integration/testing demands, adopt Option 3 (IO abstraction).
4. Evaluate need for Option 4 or 7 based on expected transaction/format churn.
5. Plan Option 5 or 6 only with a broader modernization strategy and regression test suite.
6. Implement Option 8 improvements opportunistically throughout.

---
## Regression Safeguards
- Capture sample transaction file + customer file.
- Produce golden report outputs before refactor.
- After each refactor phase, diff outputs (whitespace-insensitive where safe) to ensure behavioral parity.
- Track numeric rounding and picture formatting edge cases (large balances, zero orders, etc.).

## Key Risks to Monitor
- Silent changes in numeric formatting (locale, decimal separator).
- Record name mismatches causing incorrect stats aggregation.
- Page break handling differences if formatting module changes.
- Transaction codes relying on trailing spaces (e.g., `'PRINT '` vs `'PRINT'`).

## Next Actions (If Proceeding)
Choose preferred option tier (Foundational: 1+2, Strategic: 3+5, Modernization: 6) and create a phased implementation plan. I can scaffold the initial refactor once you select.
