package psam1.handlers;

import psam1.domain.Transaction;

public class TotalsHandler implements TransactionHandler {
    @Override
    public void handle(Transaction txn, HandlerContext ctx) {
        ctx.stats.incTransaction();
        ctx.stats.incTotalsRequest();
        ctx.writer.writeTotals(ctx.stats);
        if (ctx.stats.getPrintCompleted() > 0) {
            ctx.stats.incTotalsCompleted();
        }
    }

    @Override
    public String code() {
        return "TOTALS";
    }
}
