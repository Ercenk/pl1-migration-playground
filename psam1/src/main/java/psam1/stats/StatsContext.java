package psam1.stats;

import psam1.domain.CustomerRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class StatsContext {
    private long totalCustomerRecords; // NUM_CUSTOMER_RECS
    private long totalCustFileRecords; // NUM_CUSTFILE_RECS
    private long totalTransactions;    // NUM_TRANSACTIONS
    private long printRequests;
    private long printCompleted;
    private long totalsRequests;
    private long totalsCompleted;
    private long transactionErrors;    // NUM_TRAN_ERRORS

    private BigDecimal balanceTotal = BigDecimal.ZERO; // BALANCE_TOTAL
    private BigDecimal balanceMax = BigDecimal.ZERO;   // BALANCE_MAX

    public void update(CustomerRecord cr) {
        totalCustFileRecords++; // all types encountered
        if (!"C".equals(cr.getRecordType())) {
            return;
        }
        totalCustomerRecords++;
        BigDecimal bal = cr.getAccountBalance();
        balanceTotal = balanceTotal.add(bal);
        if (bal.compareTo(balanceMax) > 0) {
            balanceMax = bal;
        }
    }

    public BigDecimal getBalanceAvg() {
        if (totalCustomerRecords == 0) return BigDecimal.ZERO;
        return balanceTotal.divide(BigDecimal.valueOf(totalCustomerRecords), 2, RoundingMode.HALF_UP);
    }

    // Increment helpers
    public void incTransaction() { totalTransactions++; }
    public void incPrintRequest() { printRequests++; }
    public void incPrintCompleted() { printCompleted++; }
    public void incTotalsRequest() { totalsRequests++; }
    public void incTotalsCompleted() { totalsCompleted++; }
    public void incTransactionError() { transactionErrors++; }

    // Getters
    public long getTotalCustomerRecords() { return totalCustomerRecords; }
    public long getTotalCustFileRecords() { return totalCustFileRecords; }
    public long getTotalTransactions() { return totalTransactions; }
    public long getPrintRequests() { return printRequests; }
    public long getPrintCompleted() { return printCompleted; }
    public long getTotalsRequests() { return totalsRequests; }
    public long getTotalsCompleted() { return totalsCompleted; }
    public long getTransactionErrors() { return transactionErrors; }
    public BigDecimal getBalanceTotal() { return balanceTotal; }
    public BigDecimal getBalanceMax() { return balanceMax; }
}
