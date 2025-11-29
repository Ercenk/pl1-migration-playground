# Data Model: PSAM1 Migration

## Entities

### TransactionRecord
- Fields: `code` (PRINT|TOTALS), `commentFlag` (bool), `crunchParams` (ioLoops:int, cpuLoops:int), `rawLine` (string)
- Validation: comment lines start with `*`; codes validated; params parsed when present

### CustomerRecord
- Fields: `custId` (string), `name` (string), `occupation` (string), `acctBalance` (decimal 7,2), `ordersYTD` (int), `recordType` (char: 'C' or other)
- Validation: only `recordType='C'` generates detail output

### BalanceStats
- Fields: `balanceTotal` (decimal 7,2), `balanceMax` (decimal 7,2), `balanceAvg` (decimal 7,2),
  `numCustfileRecs` (int), `numCustomerRecs` (int), `numTransactions` (int), `numTranErrors` (int), `numPrintCompleted` (int), `numTotalsCompleted` (int)
- Transitions: updated during print and totals commands

## Notes
- Decimal precision must match legacy formatting spec.
- Totals report requires at least one completed print before totals.
