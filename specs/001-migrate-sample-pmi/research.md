# Phase 0 Research: Language Selection for PSAM1 Migration

Date: 2025-11-29
Branch: 001-migrate-sample-pmi
Context: Confidential financial data; zero functionality loss; batch CLI; deterministic builds; strong security; observability; Windows/Linux.

## Unknowns to Resolve

- Target language/runtime: RESOLVED → Java 21 LTS
- Test framework choice
- Tooling for deterministic builds and SBOM
- Logging/metrics libraries (or stdlib)
- File access allowlist mechanism across OSes

## Candidates Evaluated

### Java (21 LTS)
- Decision: STRONG CANDIDATE (enterprise-aligned)
- Rationale: Industry-standard, long-term support, excellent tooling (Maven/Gradle), mature security libraries, robust file I/O, and cross-platform JVM.
- Security: Strong ecosystem (JEPs improving security), dependency management with reproducible builds via Gradle/Maven lockfiles; SBOM via CycloneDX plugins.
- Observability: SLF4J + Logback/Log4j2 with JSON layouts, Micrometer metrics, OpenTelemetry SDK; rich audit logging patterns.
- Testing: JUnit 5 for unit/integration; contract tests via golden-file comparisons.
- Packaging: Fat JARs or native images (GraalVM) for operational simplicity when needed.
- Alternatives considered: Rust, Go, hardened Python.

<!-- Non-selected alternatives removed per clarification: use Java only -->

## Decision Framework

- Parity & determinism → JVM with reproducible builds (Gradle/Maven lockfiles)
- Security posture → memory-safe managed runtime; minimal unsafe operations
- Operations → fat JAR or native image (GraalVM) distribution
- Team familiarity → enterprise Java alignment

## Preliminary Recommendation

- Choose Java 21 (final) for enterprise standards and tooling alignment, rich observability, and audit logging needs.

## Next Steps

- Confirm team expertise and deployment constraints (Windows vs Linux predominance, JVM availability, distribution model).
- Bench simple parsers in Java (stdlib-only) with representative data to confirm P95 latency baseline.
- Decide test framework accordingly (JUnit 5) and finalize plan.
