package psam1.report;

import psam1.domain.CustomerRecord;
import psam1.stats.StatsContext;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportWriter {
    private final PrintWriter out;
    private final DecimalFormat moneyFmt = new DecimalFormat("###,##0.00");
    private final DecimalFormat countFmt = new DecimalFormat("###,###,##0");

    public ReportWriter(PrintWriter out) { this.out = out; }

    public void writeStartup() {
        LocalDateTime now = LocalDateTime.now();
        out.printf("PSAM1 STARTED  DATE = %s (YYYY/MM/DD)%n", now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        out.printf("               TIME = %s%n", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    public void writeTransactionEcho(String raw) {
        out.println();
        out.printf(" TRANSACTION:  %s%n", raw.trim());
        out.println();
    }

    public void writeHeader() {
        out.println("ID    CUSTOMER NAME     OCCUPATION                     BALANCE ORDERS-YTD");
        out.println("----- ----------------- ------------------------------ -------- -----------");
    }

    public void writeDetail(CustomerRecord cr) {
        out.printf("%5s %-17s %-30s %8s %11s%n",
                cr.getCustomerId(),
                truncate(cr.getName(),17),
                truncate(cr.getOccupation(),30),
                moneyFmt.format(cr.getAccountBalance()),
                countFmt.format(cr.getOrdersYtd()));
    }

    private String truncate(String s, int len) {
        if (s.length() <= len) return s;
        return s.substring(0,len);
    }

    public void writeTotals(StatsContext stats) {
        out.println();
        out.println("TOTALS REPORT");
        out.println("--------------------------------------------------------------------------------");
        if (stats.getPrintCompleted() > 0) {
            BigDecimal total = stats.getBalanceTotal();
            BigDecimal max = stats.getBalanceMax();
            BigDecimal avg = stats.getBalanceAvg();
            out.printf("ACCT BALANCE:     TOTAL: %12s   MAX: %12s   AVERAGE: %12s%n",
                    moneyFmt.format(total), moneyFmt.format(max), moneyFmt.format(avg));
            out.printf("RECORD COUNTS: ALL TYPES: %11s ALL TYPES: %11s CUST RECS: %11s%n",
                    countFmt.format(stats.getTotalCustFileRecords()),
                    countFmt.format(stats.getTotalCustFileRecords()),
                    countFmt.format(stats.getTotalCustomerRecords()));
            long processed = stats.getTotalTransactions() - stats.getTransactionErrors();
            out.printf("TRANSACTIONS:     COUNT: %11s PROCESSED: %11s    ERRORS: %11s%n",
                    countFmt.format(stats.getTotalTransactions()),
                    countFmt.format(processed),
                    countFmt.format(stats.getTransactionErrors()));
        } else {
            out.println("CANNOT COMPLETE TOTALS TRAN. A PRINT TRAN MUST BE REQUESTED/PROCESSED FIRST.");
        }
    }
}
