package psam1.handlers;

import psam1.domain.Transaction;
import psam1.stats.StatsContext;
import psam1.report.ReportWriter;

public interface TransactionHandler {
    void handle(Transaction txn, HandlerContext ctx) throws Exception;
    String code();

    class HandlerContext {
        public final StatsContext stats;
        public final ReportWriter writer;
        public final java.nio.file.Path customerFile;

        public HandlerContext(StatsContext stats, ReportWriter writer, java.nio.file.Path customerFile) {
            this.stats = stats;
            this.writer = writer;
            this.customerFile = customerFile;
        }
    }
}
