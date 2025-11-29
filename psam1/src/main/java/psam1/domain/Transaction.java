package psam1.domain;

public class Transaction {
    private final String code; // 6-char code including possible space (e.g. "PRINT ")
    private final String rawLine; // full 80-char record

    public Transaction(String code, String rawLine) {
        this.code = code;
        this.rawLine = rawLine;
    }

    public String getCode() {
        return code;
    }

    public String getRawLine() {
        return rawLine;
    }
}
