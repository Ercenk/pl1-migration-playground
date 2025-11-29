# PSAM1 Java Migration (Prototype)

Implements a provisional Java port of legacy `PSAM1` PL/I program following the high-level design in `javamigrationplan.md`.

## Build

```bash
mvn -q package
```

## Run

```bash
java -jar target/psam1-0.1.0-SNAPSHOT.jar transactions.txt customers.txt
```

Prototype expects:
- Transaction file: each non-comment line (not starting with `*`) begins with a 6-char code (`PRINT ` or `TOTALS`) followed by any data (padded/truncated to 80 chars).
- Customer file: provisional fixed-width format (see `CustomerRecordParser`). Records with first char `C` treated as customer records.

## Output
Writes report to stdout (analogous to `CUSTRPT`). Average balance returned via program exit code (truncated to integer).

## Next Steps
- Finalize exact fixed-width spec (Phase 0).
- Add unit tests for parsers, handlers, stats.
- Introduce golden file regression harness.
- Refine numeric formatting to match PL/I PIC definitions precisely.

## Notes
Formatting and field boundaries are provisional; adjust `CustomerRecordParser` once authoritative spec is available.
