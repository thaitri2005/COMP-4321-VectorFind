COMP4321 Phase 1
Build and Run Guide

========================================
1) Prerequisites
========================================
1. Java 17 or above
2. Maven 3.8 or above
3. Internet access to crawl test pages

========================================
2) Project Files
========================================
- src/main/java/hk/ust/comp4321/SpiderMain.java
  Spider + indexer entry point

- src/main/java/hk/ust/comp4321/SpiderResultExporter.java
  Test program to generate spider result.txt

- src/main/resources/stopwords.txt
  Stop-word dictionary (replaceable)

- db_schema_design.txt
  JDBM schema documentation

========================================
3) Quick Start (PowerShell)
========================================
Run these commands from the project root.

Step A: If mvn is not recognized in a new terminal, set paths first

   $env:JAVA_HOME="C:\Users\Tri Thai\.jdk\jdk-17.0.16"
   $env:PATH="C:\Users\Tri Thai\.jdk\jdk-17.0.16\bin;C:\Users\Tri Thai\.maven\maven-3.9.14\bin;" + $env:PATH

Step B: Build

   mvn clean compile

Step C: Crawl and index 30 pages

   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain -Dexec.args="https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm 30 data/phase1.db src/main/resources/stopwords.txt"

Step D: Generate output file

   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter

Step E: Validate result count (should be 30)

   (Select-String -Path "spider result.txt" -Pattern "^-+$" | Measure-Object).Count

========================================
4) Command Reference
========================================
Run spider (default seed, default limit):

   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain

Run spider (custom args):

   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain -Dexec.args="<seedUrl> <limit> <dbPath> <stopWordsPath>"

Run exporter (default output name: spider result.txt):

   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter

Run exporter (custom output):

   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter -Dexec.args="<dbPath> <outputFile>"

========================================
5) Fallback (Direct Java)
========================================
Use this only if Maven argument parsing still causes problems.

   $cp="target/classes;C:\Users\Tri Thai\.m2\repository\jdbm\jdbm\1.0\jdbm-1.0.jar;C:\Users\Tri Thai\.m2\repository\org\jsoup\jsoup\1.18.3\jsoup-1.18.3.jar"
   java -cp $cp hk.ust.comp4321.SpiderMain "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm" 30 "data/phase1.db" "src/main/resources/stopwords.txt"
   java -cp $cp hk.ust.comp4321.SpiderResultExporter "data/phase1.db" "spider result.txt"

========================================
6) Expected Output
========================================
1. JDBM database files under data/
2. spider result.txt in project root

========================================
7) Notes
========================================
1. If the course-provided stop-word dictionary is available, replace src/main/resources/stopwords.txt.
2. If the primary seed URL is unavailable, use the backup URL in the assignment description.
