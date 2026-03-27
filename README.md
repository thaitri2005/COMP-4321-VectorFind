# COMP-4321-VectorFind

VectorFind is a COMP4321 Phase 1 implementation of a web spider with integrated indexing.

## What Is Implemented

- Breadth-first spider (crawler)
- Integrated indexer (tokenize, remove stop words, stem, index)
- Persistent storage with JDBM (mappings, forward index, inverted indexes, link graph)
- Export program that reads the database and generates spider result.txt

## Component Map

### Spider (Crawler)

Responsible for URL traversal and fetching pages with BFS.

- Entry point: src/main/java/hk/ust/comp4321/SpiderMain.java
- Core crawler: src/main/java/hk/ust/comp4321/crawler/BfsCrawler.java
- URL normalization and fetch checks: src/main/java/hk/ust/comp4321/util/UrlUtil.java

### Indexer

Runs inside the crawler pipeline after each page is fetched.

- Tokenization: src/main/java/hk/ust/comp4321/util/TextUtil.java
- Stop-word loading/filtering: src/main/java/hk/ust/comp4321/util/StopWords.java
- Stemming: src/main/java/hk/ust/comp4321/util/PorterStemmer.java
- Indexed data model: src/main/java/hk/ust/comp4321/model/

### Storage Layer

Stores all persistent structures required by Phase 1.

- Database API: src/main/java/hk/ust/comp4321/storage/SearchDb.java
- URL <=> pageId mappings
- word <=> wordId mappings
- forward index (per page term vector)
- body/title inverted indexes
- parent/child link graph

### Result Exporter

Reads the indexed database and writes spider result.txt in the expected plain-text format.

- Export entry point: src/main/java/hk/ust/comp4321/SpiderResultExporter.java

## Project Layout

```text
src/main/java/hk/ust/comp4321/
  SpiderMain.java
  SpiderResultExporter.java
  config/CrawlConfig.java
  crawler/BfsCrawler.java
  model/
  storage/SearchDb.java
  util/
src/main/resources/
  stopwords.txt
data/
  phase1.db.db
  phase1.db.lg
db_schema_design.txt
readme.txt
```

## Prerequisites

- Java 17+
- Maven 3.8+
- Internet access for crawling test pages

Check your setup:

```bash
java -version
mvn -version
```

## Build

Run in project root:

```bash
mvn clean compile
```

## Run

### Run Spider + Indexer

Default run (seed URL, limit 30, default db path, default stopwords path):

```bash
mvn exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain
```

Custom run (recommended for Phase 1):

**PowerShell (use `--% ` to prevent argument parsing issues):**

```powershell
mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain -Dexec.args="https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm 30 data/phase1.db src/main/resources/stopwords.txt"
```

**Bash or cmd:**

```bash
mvn exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain -Dexec.args="https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm 30 data/phase1.db src/main/resources/stopwords.txt"
```

Expected output (PowerShell example):

```
[INFO] --- exec:3.5.0:java (default-cli) @ phase1-search-engine ---
Crawling and indexing completed.
[INFO] BUILD SUCCESS
```

### Run Exporter

Default output (reads data/phase1.db and writes spider result.txt):

**PowerShell:**

```powershell
mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter
```

**Bash or cmd:**

```bash
mvn exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter
```

Custom output:

```powershell
mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter -Dexec.args="<dbPathWithoutExtension> <outputFile>"
```

Expected output:

```
[INFO] --- exec:3.5.0:java (default-cli) @ phase1-search-engine ---
Written spider result: F:\...\spider result.txt
[INFO] BUILD SUCCESS
```

## Expected Outputs

- data/<name>.db
- data/<name>.lg
- spider result.txt (or your custom output file)

To check how many pages were exported (separator count):

PowerShell:

```powershell
(Select-String -Path "spider result.txt" -Pattern "^-+$" | Measure-Object).Count
```

Bash:

```bash
grep -c "^-\{5,\}$" "spider result.txt"
```

For a 30-page crawl, expected count is 30.

## Notes

- This README is environment-neutral and avoids user-specific local paths.
- You can replace src/main/resources/stopwords.txt with the official course dictionary if needed.
- The detailed Phase 1 submission-style instructions are in readme.txt.

## License

Repository for educational use in COMP4321.
