package lib.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {
    public static List<Path> parseAllowlist(String raw) {
        List<Path> out = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return out;
        for (String part : raw.split(";")) {
            if (!part.isBlank()) out.add(Paths.get(part.trim()));
        }
        return out;
    }
}
