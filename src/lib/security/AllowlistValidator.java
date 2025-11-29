package lib.security;

import java.nio.file.Path;
import java.util.List;

public class AllowlistValidator {
    public static boolean isAllowed(Path target, List<Path> allowlist) {
        if (allowlist == null || allowlist.isEmpty()) return true; // default allow if policy not set
        for (Path allowed : allowlist) {
            if (target.normalize().startsWith(allowed.normalize())) {
                return true;
            }
        }
        return false;
    }
}
