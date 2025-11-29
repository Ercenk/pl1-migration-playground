package cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0 || Arrays.stream(args).anyMatch(a -> "--help".equals(a) || "-h".equals(a))) {
            printHelp();
            return;
        }
        // Expected usage (initial): --transactions <path> --customers <path> [--allowlist <paths;semicolon-separated>] [--status]
        Path transactions = null;
        Path customers = null;
        String allowlist = null;
        boolean status = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--transactions":
                    transactions = Paths.get(args[++i]);
                    break;
                case "--customers":
                    customers = Paths.get(args[++i]);
                    break;
                case "--allowlist":
                    allowlist = args[++i];
                    break;
                case "--status":
                    status = true;
                    break;
                default:
                    // ignore unknown for now; router will handle later
            }
        }
        if (status) {
            System.out.println("OK");
            return;
        }
        if (transactions == null || customers == null) {
            System.err.println("Missing required arguments: --transactions and --customers");
            printHelp();
            System.exit(2);
        }
        // TODO: Wire security allowlist, parsing services, and processors per tasks
        System.out.println("PSAM1 Java CLI scaffold initialized.");
    }

    private static void printHelp() {
        System.out.println("Usage: java -cp <jar> cli.Main --transactions <path> --customers <path> [--allowlist <paths>] [--status]");
    }
}
