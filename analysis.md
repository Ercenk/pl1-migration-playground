# PSAM1 Program Analysis

## Overview
`PSAM1` is a PL/I main procedure that reads a transaction control file (`TRANFILE`) and, based on its records, optionally processes a customer data file (`CUSTFILE`) to produce a printed report (`CUSTRPT`). It collects statistics about customer account balances (via an external subroutine `PSAM2`) and can output a totals summary. The procedure returns a decimal (12,2) value: the average customer balance (`BALANCE_AVG`).

## High-Level Flow
1. Initialize status, timestamp, and flags.
2. Open `TRANFILE` and the report file `CUSTRPT`. (Customer file `CUSTFILE` opened only for PRINT transactions.)
3. Force an initial page header via `SIGNAL ENDPAGE(CUSTRPT)` which triggers the `ON ENDPAGE` handler.
4. Loop reading transaction records until end-of-file:
   - Skip comment records (first column '*').
   - Echo the transaction record to the report.
   - Dispatch by `TRAN_CODE` using a `SELECT` construct:
     - `PRINT `: Print detail report of customer records.
     - `TOTALS`: Print accumulated totals (if any PRINT done).
     - Otherwise: Flag an invalid transaction code.
5. After loop, procedure ends returning `BALANCE_AVG`.

The loop termination relies on ENDFILE conditions setting bit flags rather than a changing loop counter. The `DO WHILE (I > 0)` construct with `I` fixed at 1 creates an intentional pseudo-infinite loop broken by `LEAVE` when EOF flags are set.

## Included External Definitions
- `%INCLUDE CUSTPLI;`: Presumably defines the customer record layout (e.g., `CUSTOMER_RECORD`, perhaps `CUSTFILE_RECORD`).
- `%INCLUDE MYFILE (DATETIME);`: Provides `DATETIME()` function and date/time components (`CURRENT_YEAR`, etc.).
- `%INCLUDE MYLIB (REPTTOTL);`: Likely supplies report total related declarations or formats.
- `%INCLUDE BALSTATS;`: Supplies balance statistic variables (`BALANCE_TOTAL`, `BALANCE_MAX`, `BALANCE_AVG`, counters, and maybe a switch `BALANCE_FIRST_TIME_SW`).

Because these are external, internal logic assumes they define:
- Customer record fields: `CUST_ID`, `NAME`, `OCCUPATION`, `ACCT_BALANCE`, `ORDERS_YTD`, `RECORD_TYPE`.
- Balance statistics accumulation target: `CUSTOMER_BALANCE_STATS` and input record `CUSTFILE_RECORD` (note: one call still passes `CUSTFILE_RECORD` instead of `CUSTOMER_RECORD`, possibly legacy naming).

## Key Data Structures
### Transaction Record (`TRAN_RECORD`)
- `TRAN_CODE` (CHAR(6)): Dispatch control (values seen: `PRINT `, `TOTALS`).
- Fields for performance/test parameters: `CRUNCH_IO_LOOPS`, `CRUNCH_CPU_LOOPS` (not used in shown logic).
- Defined overlays: `TRAN_COMMENT` (first byte), `TRAN_RECORD_ALL` (full 80 bytes).

### Customer Records
From included file; processed only if `RECORD_TYPE = 'C'`.

### Statistics / Counters
Counters track numbers of transactions, requests, completions, record counts, errors, etc. Totals logic uses these to conditionally print summary in `TRANTOT`.

## Formatting and Report Output
PL/I `FORMAT` statements define reusable layouts (`FMT_HDR1`, `FMT_CUST1`, etc.). The program uses `PUT FILE ... EDIT(...) (R(format))` to render aligned columns and numeric masks:
- Customer detail lines use `FMT_CUST1` with zoned decimal picture strings (`P'ZZZ,ZZ9V.99'`, `P'ZZ,ZZZ,ZZ9'`).
- Totals report uses `FMT_TOT1` after assigning numeric values to picture variables (`NUMA_7V2`, `NUMB_7V2`, etc.).

## Condition Handling
`ON ENDFILE (CUSTFILE)` and `ON ENDFILE (TRANFILE)` set EOF flags instead of raising unhandled conditions. `ON ENDPAGE (CUSTRPT)` prints page headings (date/time + column headers for detail report if current transaction is PRINT). Page headings are refreshed when a page break occurs or from the initial forced `SIGNAL ENDPAGE`.

## Subprocedures
### `PRTHDG1`
Prints two header lines (column names and underlines) before customer detail rows.

### `TRANTOT`
Prints totals report:
- Account balance statistics (TOTAL, MAX, AVERAGE).
- Record counts (both overall and customer-specific; note duplication of `ALL TYPES:` line—maybe intentional or a minor copy issue).
- Transaction counts (total, processed, errors).
If no PRINT transaction was completed, warns that TOTALS cannot run standalone.

### External `PSAM2`
Called for each customer record to aggregate balance stats. Signature expects `(CUSTFILE_RECORD, CUSTOMER_BALANCE_STATS)`. Internal mismatch (`CUSTOMER_RECORD` used for detail printing vs `CUSTFILE_RECORD` passed to `PSAM2`) suggests historical refactor leaving one parameter name unchanged.

## Control Logic Nuances
- The transaction loop and customer loop both use invariant `DO WHILE (I > 0)`; termination relies purely on EOF flags + `LEAVE`. This is a PL/I idiom sometimes used to centralize termination logic via conditions.
- `TRAN_OK_FLAG` set but not further evaluated—possibly vestigial.
- Page header printing is conditional inside `ON ENDPAGE` on `TRAN_CODE = 'PRINT '` to avoid detail headers for TOTALS-only pages.

## Potential Issues / Observations
- Mixed use of `CUSTFILE_RECORD` vs `CUSTOMER_RECORD` in the `PSAM2` call could indicate a mismatch if the included record names changed. If `CUSTFILE_RECORD` is no longer defined, this would break at compile/link time.
- Duplicate `' ALL TYPES:'` label in totals output likely a typo; second should perhaps be `' CUST RECS:'` which is already printed later (results in two counts for all types and one for customer records).
- Error messaging for invalid transactions prints raw `TRAN_CODE` but does not halt processing.
- No explicit CLOSE for `TRANFILE` or `CUSTRPT` before RETURN (PL/I runtime may handle cleanup automatically).

## Return Value
`RETURN(BALANCE_AVG);` provides the average balance collected across processed customer records. If no customer records were processed, value depends on initialization and `PSAM2` logic (assumed to set defaults when first invoked).

## Summary
`PSAM1` orchestrates transaction-driven report generation: PRINT transactions produce detailed per-customer output and accumulate statistics; TOTALS transactions summarize accumulated metrics. It leverages PL/I condition handling for EOF and page breaks, modular formatting, and external include files for data/time, record layouts, and statistics management.

## Suggested Clarifications (If Maintaining)
- Confirm record name consistency for the `PSAM2` call.
- Adjust duplicate `' ALL TYPES:'` label in totals section.
- Consider making loop conditions explicit (`DO UNTIL(TRANFILE_EOF)` etc.) for readability.
- Ensure `TRAN_OK_FLAG` either used or removed.
- Add CLOSE statements for all opened files for explicit resource management.
