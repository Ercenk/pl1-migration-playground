package psam1.parsing;

import psam1.domain.CustomerRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Provisional fixed-width parser based on legacy FMT_CUST1 format hints.
 * Layout (indices 0-based, end exclusive):
 * 0  : recordType (1)
 * 1-6: customerId (5)
 * 7  : space
 * 8-25: name (17)
 * 26 : space
 * 27-55: occupation (29 assumed incl. ambiguity)
 * 56-57: spaces (2)
 * 58-68: balance (11 incl commas/decimal) -> cleaned to BigDecimal
 * 69 : space
 * 70-80: ordersYtd (11) numeric
 * Lines shorter than expected are padded; longer are truncated.
 */
public class CustomerRecordParser {
    public void parse(Path path, Consumer<CustomerRecord> consumer) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;
                String padded = line.length() < 81 ? String.format("%-81s", line) : line.substring(0, 81);
                String recordType = padded.substring(0,1);
                String id = padded.substring(1,6).trim();
                String name = padded.substring(8,25).trim();
                String occupation = padded.substring(27,55).trim();
                String balanceRaw = safeSubstring(padded,58,69).trim();
                String ordersRaw = safeSubstring(padded,70,81).trim();
                BigDecimal balance = parseMoney(balanceRaw);
                int orders = parseIntSafe(ordersRaw);
                consumer.accept(new CustomerRecord(recordType, id, name, occupation, balance, orders));
            }
        }
    }

    private String safeSubstring(String s, int start, int end) {
        if (start >= s.length()) return "";
        return s.substring(start, Math.min(end, s.length()));
    }

    private BigDecimal parseMoney(String raw) {
        if (raw.isEmpty()) return BigDecimal.ZERO.setScale(2);
        String cleaned = raw.replace(",", "");
        if (!cleaned.contains(".")) {
            // assume last two digits are cents
            if (cleaned.length() >= 3) {
                cleaned = cleaned.substring(0, cleaned.length()-2) + "." + cleaned.substring(cleaned.length()-2);
            } else {
                return BigDecimal.ZERO.setScale(2);
            }
        }
        try {
            return new BigDecimal(cleaned).setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO.setScale(2);
        }
    }

    private int parseIntSafe(String raw) {
        if (raw.isEmpty()) return 0;
        String cleaned = raw.replace(",", "").trim();
        try { return Integer.parseInt(cleaned); } catch (NumberFormatException e) { return 0; }
    }
}
