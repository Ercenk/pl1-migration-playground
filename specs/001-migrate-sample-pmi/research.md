# Phase 0 Research: Language Selection for PSAM1 Migration

Date: 2025-11-29
Branch: 001-migrate-sample-pmi
Context: Confidential financial data; zero functionality loss; batch CLI; deterministic builds; strong security; observability; Windows/Linux.

## Unknowns to Resolve

- Target language/runtime
- Test framework choice
- Tooling for deterministic builds and SBOM
- Logging/metrics libraries (or stdlib)
- File access allowlist mechanism across OSes

## Candidates Evaluated

### Rust (1.75+)
- Decision: STRONG CANDIDATE
- Rationale: Memory safety (no GC), strong type system, zero-cost abstractions, excellent CLI tooling (clap), structured logging (tracing), deterministic builds via `cargo` with `Cargo.lock`, robust testing (`cargo test`), good Windows/Linux support.
- Security: Enforced by language (no uncontrolled pointer arithmetic), supply-chain via Cargo.lock, easy SBOM via `cargo auditable` / `cargo about`.
- Observability: `tracing` with JSON formatters; metrics via `metrics` crate.
- Alternatives considered: Go and hardened Python.

### Go (1.22+)
- Decision: STRONG CANDIDATE
- Rationale: Simple concurrency, static binaries, solid stdlib for file I/O and logging, reproducible builds with `go.mod` and `go.sum`, testing via `go test`.
- Security: Memory safety (with caveats vs Rust), good cross-compilation, SBOM via `go version -m` metadata and external tools.
- Observability: `zap`/`zerolog`, OpenTelemetry support; metrics via `prometheus` client.
- Alternatives considered: Rust.

### Python (3.11 hardened)
- Decision: CONDITIONAL CANDIDATE (if speed not critical)
- Rationale: Rapid development, rich stdlib; needs strict hardening (virtualenv, pinned deps, type hints, mypy, pytest). Performance may lag for large batches; can be mitigated with optimized parsing.
- Security: Requires discipline (no dynamic eval, strict dependency pinning), SBOM via `pip-audit`, `pip-compile`.
- Observability: `structlog` or `logging`, `opentelemetry`.
- Alternatives considered: Rust/Go preferred for static binaries and stricter memory safety.

## Decision Framework

- Parity & determinism → favor static, typed, reproducible toolchains (Rust/Go)
- Security posture → favor memory-safe languages; minimal runtime footprint
- Operations → prefer single static binary distribution (Go or Rust)
- Team familiarity → TBD (NEEDS CLARIFICATION)

## Preliminary Recommendation

- Choose Rust if maximum security and fine-grained control are top priorities and team is comfortable with Rust.
- Choose Go for simpler development, static binaries, and fast delivery while maintaining strong safety.
- Choose hardened Python only if development speed is critical and performance demands are moderate.

## Next Steps

- Confirm team expertise and deployment constraints (Windows vs Linux predominance, distribution model).
- Bench simple parsers in Rust and Go with representative data to compare P95 latency.
- Decide test framework accordingly (cargo test vs go test) and finalize plan.
