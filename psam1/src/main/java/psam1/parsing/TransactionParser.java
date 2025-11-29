package psam1.parsing;

import psam1.domain.Transaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TransactionParser {
    public void parse(Path path, Consumer<Transaction> consumer) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;
                char first = line.charAt(0);
                if (first == '*') { // comment
                    continue;
                }
                String padded = line;
                if (padded.length() < 80) {
                    padded = String.format("%-80s", padded);
                } else if (padded.length() > 80) {
                    padded = padded.substring(0, 80);
                }
                String code = padded.substring(0, 6); // TRAN_CODE
                consumer.accept(new Transaction(code, padded));
            }
        }
    }

    public List<Transaction> parseAll(Path path) throws IOException {
        List<Transaction> list = new ArrayList<>();
        parse(path, list::add);
        return list;
    }
}
