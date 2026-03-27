package hk.ust.comp4321.crawler;

import hk.ust.comp4321.config.CrawlConfig;
import hk.ust.comp4321.model.PageInfo;
import hk.ust.comp4321.storage.SearchDb;
import hk.ust.comp4321.util.PorterStemmer;
import hk.ust.comp4321.util.TextUtil;
import hk.ust.comp4321.util.UrlUtil;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

public class BfsCrawler {
    private static final String USER_AGENT = "COMP4321-Phase1-Crawler/1.0";

    private final CrawlConfig config;
    private final SearchDb db;
    private final Set<String> stopWords;

    public BfsCrawler(CrawlConfig config, SearchDb db, Set<String> stopWords) {
        this.config = config;
        this.db = db;
        this.stopWords = stopWords;
    }

    public void crawl() {
        Queue<String> queue = new ArrayDeque<>();
        Set<String> seen = new HashSet<>();

        String seed = UrlUtil.normalize(config.getSeedUrl());
        if (seed == null) {
            throw new IllegalArgumentException("Invalid seed URL: " + config.getSeedUrl());
        }

        queue.offer(seed);
        seen.add(seed);

        int indexedCount = 0;
        while (!queue.isEmpty() && indexedCount < config.getCrawlLimit()) {
            String url = queue.poll();
            try {
                HeadInfo headInfo = inspectHead(url);
                if (!shouldFetch(url, headInfo.lastModified)) {
                    continue;
                }

                Connection.Response response = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .ignoreHttpErrors(true)
                    .timeout(15000)
                    .execute();

                if (response.statusCode() >= 400) {
                    continue;
                }

                Document document = response.parse();
                int parentId = db.ensurePageId(url);

                // Re-indexing should replace old term/link data for this page.
                db.clearPageIndex(parentId);
                db.clearOutgoingLinks(parentId);

                PageInfo info = db.getPageInfo(parentId);
                info.setTitle(document.title());
                info.setLastModified(headInfo.lastModified == null ? "Unknown" : headInfo.lastModified);
                info.setSize(headInfo.size > 0 ? headInfo.size : response.body().length());
                info.setFetched(true);
                db.savePageInfo(info);

                indexTitle(parentId, document.title());
                indexBody(parentId, document.body() == null ? document.text() : document.body().text());

                List<String> links = document.select("a[href]").stream()
                    .map(link -> UrlUtil.normalize(link.absUrl("href")))
                    .filter(candidate -> candidate != null && !candidate.isBlank())
                    .distinct()
                    .sorted(Comparator.naturalOrder())
                    .toList();

                for (String childUrl : links) {
                    int childId = db.ensurePageId(childUrl);
                    db.addLink(parentId, childId);
                    if (seen.add(childUrl)) {
                        queue.offer(childUrl);
                    }
                }

                db.commit();
                indexedCount += 1;
                System.out.println("Indexed [" + indexedCount + "/" + config.getCrawlLimit() + "]: " + url);
            } catch (IOException ex) {
                System.err.println("Failed to process URL: " + url + " -> " + ex.getMessage());
            }
        }
    }

    private HeadInfo inspectHead(String url) throws IOException {
        Connection.Response head = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .method(Connection.Method.HEAD)
            .ignoreHttpErrors(true)
            .timeout(15000)
            .execute();

        String modified = head.header("Last-Modified");
        String contentLength = head.header("Content-Length");
        long size = 0;
        if (contentLength != null) {
            try {
                size = Long.parseLong(contentLength);
            } catch (NumberFormatException ignored) {
                size = 0;
            }
        }
        return new HeadInfo(modified, size);
    }

    private boolean shouldFetch(String url, String remoteLastModified) {
        Integer pageId = db.getPageId(url);
        if (pageId == null) {
            return true;
        }

        PageInfo existing = db.getPageInfo(pageId);
        if (existing == null) {
            return true;
        }
        if (!existing.isFetched()) {
            return true;
        }

        String localLastModified = existing.getLastModified();
        if (remoteLastModified == null || remoteLastModified.isBlank() || "Unknown".equals(localLastModified)) {
            return false;
        }

        try {
            ZonedDateTime remote = ZonedDateTime.parse(remoteLastModified, DateTimeFormatter.RFC_1123_DATE_TIME);
            ZonedDateTime local = ZonedDateTime.parse(localLastModified, DateTimeFormatter.RFC_1123_DATE_TIME);
            return remote.isAfter(local);
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private void indexTitle(int pageId, String title) {
        List<String> tokens = TextUtil.tokenize(title);
        int position = 0;
        for (String token : tokens) {
            String stem = tokenToStem(token);
            if (stem == null) {
                continue;
            }
            db.addTitleTerm(pageId, stem, position++);
        }
    }

    private void indexBody(int pageId, String bodyText) {
        List<String> tokens = TextUtil.tokenize(bodyText);
        int position = 0;
        for (String token : tokens) {
            String stem = tokenToStem(token);
            if (stem == null) {
                continue;
            }
            db.addBodyTerm(pageId, stem, position++);
        }
    }

    private String tokenToStem(String token) {
        String normalized = token.toLowerCase(Locale.ROOT);
        if (normalized.length() <= 1 || stopWords.contains(normalized)) {
            return null;
        }
        String stem = PorterStemmer.stem(normalized);
        if (stem == null || stem.isBlank() || stopWords.contains(stem)) {
            return null;
        }
        return stem;
    }

    private record HeadInfo(String lastModified, long size) {
    }
}
