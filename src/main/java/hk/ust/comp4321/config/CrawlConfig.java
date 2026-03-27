package hk.ust.comp4321.config;

import java.nio.file.Path;

public class CrawlConfig {
    private final String seedUrl;
    private final int crawlLimit;
    private final Path databaseFile;
    private final Path stopWordsFile;

    public CrawlConfig(String seedUrl, int crawlLimit, Path databaseFile, Path stopWordsFile) {
        this.seedUrl = seedUrl;
        this.crawlLimit = crawlLimit;
        this.databaseFile = databaseFile;
        this.stopWordsFile = stopWordsFile;
    }

    public String getSeedUrl() {
        return seedUrl;
    }

    public int getCrawlLimit() {
        return crawlLimit;
    }

    public Path getDatabaseFile() {
        return databaseFile;
    }

    public Path getStopWordsFile() {
        return stopWordsFile;
    }
}
