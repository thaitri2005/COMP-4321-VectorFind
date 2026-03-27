package hk.ust.comp4321;

import hk.ust.comp4321.model.PageInfo;
import hk.ust.comp4321.model.TermVectorEntry;
import hk.ust.comp4321.storage.SearchDb;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpiderResultExporter {
    public static void main(String[] args) throws Exception {
        Path dbPath = Path.of(args.length > 0 ? args[0] : "data/phase1.db");
        Path outputPath = Path.of(args.length > 1 ? args[1] : "spider result.txt");

        List<String> lines = new ArrayList<>();
        try (SearchDb db = new SearchDb(dbPath)) {
            List<PageInfo> pages = db.allPages().values().stream()
                .filter(PageInfo::isFetched)
                .sorted(Comparator.comparingInt(PageInfo::getPageId))
                .toList();

            for (PageInfo page : pages) {
                lines.add(page.getTitle());
                lines.add(page.getUrl());
                lines.add(page.getLastModified() + ", " + page.getSize());
                lines.add(formatKeywords(db.forwardTermsForPage(page.getPageId())));

                db.childrenOf(page.getPageId()).stream()
                    .map(db::urlForPageId)
                    .filter(url -> url != null && !url.isBlank())
                    .limit(10)
                    .forEach(lines::add);

                lines.add("--------------------------------------------------------------------------------");
            }
        }

        Files.write(outputPath, lines);
        System.out.println("Written spider result: " + outputPath.toAbsolutePath());
    }

    private static String formatKeywords(Map<String, TermVectorEntry> terms) {
        return terms.entrySet().stream()
            .sorted((a, b) -> {
                int byFreq = Integer.compare(b.getValue().totalFrequency(), a.getValue().totalFrequency());
                if (byFreq != 0) {
                    return byFreq;
                }
                return a.getKey().compareTo(b.getKey());
            })
            .limit(10)
            .map(e -> e.getKey() + " " + e.getValue().totalFrequency())
            .collect(Collectors.joining("; "));
    }
}
