package hk.ust.comp4321.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class StopWords {
    private StopWords() {
    }

    public static Set<String> load(Path path) throws IOException {
        Set<String> words = new HashSet<>();
        for (String line : Files.readAllLines(path)) {
            String normalized = line.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty() && !normalized.startsWith("#")) {
                words.add(normalized);
            }
        }
        return words;
    }
}
