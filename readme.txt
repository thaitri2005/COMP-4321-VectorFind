COMP4321 Phase 1
Build and Run Guide

========================================
1) Prerequisites
========================================
1. Java 17 or above
2. Maven 3.8 or above
3. Internet access to crawl test pages

Optional checks:
   java -version
   mvn -version

========================================
2) Where Each Part Is
========================================
Spider (Crawler)
- src/main/java/hk/ust/comp4321/SpiderMain.java
  Main entry point for crawling + indexing pipeline
- src/main/java/hk/ust/comp4321/crawler/BfsCrawler.java
  Breadth-first traversal, fetch logic, and page update checks

Indexer
- src/main/java/hk/ust/comp4321/util/TextUtil.java
  Tokenization
- src/main/java/hk/ust/comp4321/util/StopWords.java
  Stop-word loading/filtering
- src/main/java/hk/ust/comp4321/util/PorterStemmer.java
  Stemming

Storage / Database
- src/main/java/hk/ust/comp4321/storage/SearchDb.java
  JDBM mappings, forward index, inverted indexes, and link graph
- db_schema_design.txt
  Schema design document

Test Program (Required Output)
- src/main/java/hk/ust/comp4321/SpiderResultExporter.java
  Reads DB and generates spider result.txt

Other Required Files
- src/main/resources/stopwords.txt
  Stop-word dictionary (replaceable)
- data/
  Indexed database files

========================================
3) Build
========================================
Run from project root:

   mvn clean compile

========================================
4) Run Spider + Indexer
========================================
Default run (uses built-in defaults):

   mvn exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain

Recommended Phase 1 command (30 pages):

PowerShell (use --% to prevent argument issues):

   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain -Dexec.args="https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm 30 data/phase1.db src/main/resources/stopwords.txt"

Bash or cmd (no --% needed):

   mvn exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain -Dexec.args="https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm 30 data/phase1.db src/main/resources/stopwords.txt"

Expected output:

   [INFO] --- exec:3.5.0:java (default-cli) @ phase1-search-engine ---
   Crawling and indexing completed.
   [INFO] BUILD SUCCESS

Backup seed URL if primary is unavailable:

   https://comp4321-hkust.github.io/testpages/testpage.htm

========================================
5) Run Test Program (Exporter)
========================================
Default run:

PowerShell:

   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter

Bash or cmd:

   mvn exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter

Custom output file:

   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter -Dexec.args="<dbPathWithoutExtension> <outputFile>"

Expected output:

   [INFO] --- exec:3.5.0:java (default-cli) @ phase1-search-engine ---
   Written spider result: F:\...\spider result.txt
   [INFO] BUILD SUCCESS

========================================
6) Validate Output
========================================
Expected output files:
1. data/<name>.db
2. data/<name>.lg
3. spider result.txt

Separator count check (should be 30 for 30-page crawl):

PowerShell:
   (Select-String -Path "spider result.txt" -Pattern "^-+$" | Measure-Object).Count

Bash:
   grep -c "^-\{5,\}$" "spider result.txt"

========================================
7) Notes
========================================
1. This guide is environment-neutral and does not use any user-specific machine paths.
2. If the course stop-word dictionary is provided, replace src/main/resources/stopwords.txt.
