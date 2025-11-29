package psam1.handlers;

import psam1.domain.Transaction;
import psam1.parsing.CustomerRecordParser;
import psam1.domain.CustomerRecord;

import java.io.IOException;
import java.nio.file.Path;

public class PrintHandler implements TransactionHandler {
    @Override
    public void handle(Transaction txn, HandlerContext ctx) throws IOException {
        ctx.stats.incTransaction();
        ctx.stats.incPrintRequest();
        ctx.writer.writeHeader();
        CustomerRecordParser parser = new CustomerRecordParser();
        parser.parse(ctx.customerFile, cr -> {
            ctx.stats.update(cr);
            if ("C".equals(cr.getRecordType())) {
                ctx.writer.writeDetail(cr);
            }
        });
        ctx.stats.incPrintCompleted();
    }

    @Override
    public String code() {
        return "PRINT "; // preserve trailing space
    }
}
