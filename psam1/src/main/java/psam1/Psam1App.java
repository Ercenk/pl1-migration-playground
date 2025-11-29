package psam1;

import psam1.domain.Transaction;
import psam1.handlers.HandlerRegistry;
import psam1.handlers.TransactionHandler;
import psam1.handlers.TransactionHandler.HandlerContext;
import psam1.parsing.TransactionParser;
import psam1.report.ReportWriter;
import psam1.stats.StatsContext;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Path;

public class Psam1App {
    public BigDecimal run(Path transactionFile, Path customerFile, PrintWriter out) throws Exception {
        StatsContext stats = new StatsContext();
        ReportWriter writer = new ReportWriter(out);
        writer.writeStartup();
        HandlerRegistry registry = new HandlerRegistry();
        TransactionParser parser = new TransactionParser();
        parser.parse(transactionFile, txn -> {
            writer.writeTransactionEcho(txn.getRawLine());
            TransactionHandler handler = registry.resolve(txn.getCode());
            try {
                handler.handle(txn, new HandlerContext(stats, writer, customerFile));
            } catch (Exception e) {
                out.println("ERROR DURING HANDLER: " + e.getMessage());
                stats.incTransactionError();
            }
        });
        return stats.getBalanceAvg();
    }
}
