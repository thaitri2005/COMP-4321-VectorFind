package hk.ust.comp4321;

import hk.ust.comp4321.config.CrawlConfig;
import hk.ust.comp4321.crawler.BfsCrawler;
import hk.ust.comp4321.storage.SearchDb;
import hk.ust.comp4321.util.StopWords;

import java.nio.file.Path;
import java.util.Set;

public class SpiderMain {
    public static void main(String[] args) throws Exception {
        String seedUrl = args.length > 0 ? args[0]
            : "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
        int crawlLimit = args.length > 1 ? Integer.parseInt(args[1]) : 30;
        Path dbPath = Path.of(args.length > 2 ? args[2] : "data/phase1.db");
        Path stopWordsPath = Path.of(args.length > 3 ? args[3] : "src/main/resources/stopwords.txt");

        CrawlConfig config = new CrawlConfig(seedUrl, crawlLimit, dbPath, stopWordsPath);
        Set<String> stopWords = StopWords.load(config.getStopWordsFile());

        try (SearchDb db = new SearchDb(config.getDatabaseFile())) {
            new BfsCrawler(config, db, stopWords).crawl();
        }

        System.out.println("Crawling and indexing completed.");
    }
}
