package psam1.handlers;

import java.util.HashMap;
import java.util.Map;

public class HandlerRegistry {
    private final Map<String, TransactionHandler> handlers = new HashMap<>();
    private final TransactionHandler invalidHandler;

    public HandlerRegistry() {
        register(new PrintHandler());
        register(new TotalsHandler());
        invalidHandler = new InvalidHandler();
    }

    public void register(TransactionHandler handler) {
        handlers.put(handler.code(), handler);
    }

    public TransactionHandler resolve(String code) {
        return handlers.getOrDefault(code, invalidHandler);
    }
}
