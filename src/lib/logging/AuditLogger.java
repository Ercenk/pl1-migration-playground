package lib.logging;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuditLogger {
    private static final Logger LOGGER = Logger.getLogger("audit");

    public static void info(String event, String message) {
        // Simple structured pattern: time|event|message
        LOGGER.log(Level.INFO, String.format("%s|%s|%s", Instant.now().toString(), event, sanitize(message)));
    }

    public static void warn(String event, String message) {
        LOGGER.log(Level.WARNING, String.format("%s|%s|%s", Instant.now().toString(), event, sanitize(message)));
    }

    public static void error(String event, String message) {
        LOGGER.log(Level.SEVERE, String.format("%s|%s|%s", Instant.now().toString(), event, sanitize(message)));
    }

    private static String sanitize(String s) {
        if (s == null) return "";
        // Basic redaction placeholder - refine per FR-008
        return s.replaceAll("(acct|balance|orders|name|id)", "<redacted>");
    }
}
