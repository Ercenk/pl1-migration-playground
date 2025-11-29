package psam1.handlers;

import psam1.domain.Transaction;

public class InvalidHandler implements TransactionHandler {
    @Override
    public void handle(Transaction txn, HandlerContext ctx) {
        ctx.stats.incTransaction();
        ctx.stats.incTransactionError();
        ctx.writer.writeTransactionEcho("INVALID TRANSACTION CODE: " + txn.getCode());
    }

    @Override
    public String code() {
        return "<INVALID>";
    }
}
