package psam1;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java -jar psam1.jar <transactionFile> <customerFile>");
            System.exit(1);
        }
        Path transactionFile = Path.of(args[0]);
        Path customerFile = Path.of(args[1]);
        Psam1App app = new Psam1App();
        BigDecimal avg = app.run(transactionFile, customerFile, new PrintWriter(System.out, true));
        // Exit code based on scaled average truncated to int (example mapping)
        int exitCode = avg.intValue();
        System.exit(exitCode);
    }
}
