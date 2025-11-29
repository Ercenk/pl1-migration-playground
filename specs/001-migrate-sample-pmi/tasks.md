---

description: "Task list for PSAM1 migration (Java stdlib-only)"
---

# Tasks: Migrate PSAM1 (sample.pmi) to Modern Secure Architecture

**Input**: Design documents from `/specs/001-migrate-sample-pmi/`
**Prerequisites**: plan.md (required), spec.md (placeholder; user stories inferred from plan), research.md, data-model.md, contracts/

**Tests**: Only include if requested. For parity, golden-file contract tests are included.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- [P]: Can run in parallel (different files, no dependencies)
- [Story]: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Initialize Gradle Java 21 project structure in src/ and tests/
- [ ] T002 Configure `java.util.logging` basic JSON-like formatter in src/lib/logging/LoggingConfig.java
- [ ] T003 [P] Add configuration loader for allowlist paths in src/lib/config/ConfigLoader.java
- [ ] T004 [P] Add CLI entry (Picocli not used) simple `Main` args parsing in src/cli/Main.java
- [ ] T005 Setup JUnit 5 in tests/unit/ and tests/contract/

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T006 Implement TransactionRecord parser in src/models/TransactionRecord.java
- [ ] T007 [P] Implement CustomerRecord model in src/models/CustomerRecord.java
- [ ] T008 Implement BalanceStats in src/models/BalanceStats.java
- [ ] T009 [P] Implement file allowlist validator in src/lib/security/AllowlistValidator.java
- [ ] T010 Implement deterministic timestamp service in src/lib/time/TimestampService.java

---

## Phase 3: User Story 1 - Run Java CLI with identical outputs (Priority: P1) 🎯 MVP

**Goal**: Java CLI reads transaction and customer files, produces reports identical to legacy outputs.

**Independent Test**: Golden-file comparison of detail and totals reports vs baseline outputs.

### Tests for User Story 1 (included for parity)

- [ ] T011 [P] [US1] Create golden baseline outputs in tests/contract/baseline/
- [ ] T012 [P] [US1] Contract test for detail report in tests/contract/TestDetailReport.java
- [ ] T013 [P] [US1] Contract test for totals report in tests/contract/TestTotalsReport.java

### Implementation for User Story 1

- [ ] T014 [P] [US1] Implement report header formatting in src/services/ReportFormatter.java
- [ ] T015 [US1] Implement PRINT processing loop in src/services/PrintProcessor.java (depends on T006, T007, T008)
- [ ] T016 [US1] Implement TOTALS processing in src/services/TotalsProcessor.java (depends on T008)
- [ ] T017 [US1] Implement CLI command execution wiring in src/cli/Main.java (depends on T015, T016)
- [ ] T018 [US1] Add error handling for unknown TRAN_CODE in src/services/TransactionRouter.java
- [ ] T019 [US1] Ensure comments ('*' first column) are ignored in src/models/TransactionRecord.java

**Checkpoint**: User Story 1 fully functional and testable independently.

---

## Phase 4: User Story 2 - Security controls and audit logging (Priority: P2)

**Goal**: Enforce allowlist paths and structured audit logging; no sensitive data in logs.

**Independent Test**: Attempt reads outside allowlist; validate access denied and audit log entries; verify no sensitive fields in logs.

### Tests for User Story 2

- [ ] T020 [P] [US2] Integration test: deny unauthorized paths in tests/integration/TestAllowlistEnforcement.java
- [ ] T021 [P] [US2] Integration test: audit log entries for file open/close/errors in tests/integration/TestAuditLogging.java

### Implementation for User Story 2

- [ ] T022 [US2] Apply allowlist check before file open in src/lib/security/AllowlistValidator.java
- [ ] T023 [US2] Implement audit logging wrappers in src/lib/logging/AuditLogger.java
- [ ] T024 [US2] Redact sensitive values in log messages in src/lib/logging/AuditLogger.java

**Checkpoint**: Security controls enforced and auditable.

---

## Phase 5: User Story 3 - Observability and performance baselines (Priority: P3)

**Goal**: In-process metrics and health status command; establish latency baselines.

**Independent Test**: Execute with large inputs; verify counters and status; capture P95 runtime.

### Tests for User Story 3

- [ ] T025 [P] [US3] Integration test for metrics counters in tests/integration/TestMetrics.java
- [ ] T026 [P] [US3] Integration test for health command in tests/integration/TestHealthStatus.java

### Implementation for User Story 3

- [ ] T027 [US3] Implement in-process counters in src/lib/metrics/MetricsRegistry.java
- [ ] T028 [US3] Implement health/status CLI flag in src/cli/Main.java
- [ ] T029 [US3] Emit metrics summary at end of run in src/cli/Main.java

**Checkpoint**: Observability implemented and baseline documented.

---

## Phase N: Polish & Cross-Cutting Concerns

- [ ] T030 [P] Documentation updates in specs/001-migrate-sample-pmi/quickstart.md
- [ ] T031 Code cleanup and refactoring across src/
- [ ] T032 Performance tuning (I/O buffering, parsing) in src/services/
- [ ] T033 [P] Additional unit tests in tests/unit/
- [ ] T034 Security hardening review per constitution
- [ ] T035 Run SBOM export via JDK tooling notes (no external plugin)

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup (Phase 1): No dependencies - can start immediately
- Foundational (Phase 2): Depends on Setup completion - blocks all stories
- User Stories (Phase 3+): Depend on Foundational completion; proceed P1 → P2 → P3
- Polish (Final): After desired stories complete

### User Story Dependencies

- US1: Starts after Foundational; no other story dependencies
- US2: Starts after Foundational; independent, integrates with allowlist and logging
- US3: Starts after Foundational; independent

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Models before services; services before CLI wiring
- Core implementation before integration

### Parallel Opportunities

- [P] tasks in Setup/Foundational can run in parallel
- Tests for a story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Setup
2. Complete Foundational
3. Implement US1
4. Stop and validate parity via golden files

### Incremental Delivery

- Add US2 (security) → validate → deliver
- Add US3 (observability) → validate → deliver

