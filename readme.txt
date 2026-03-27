COMP4321 Phase 1 - Build and Run Instructions

Prerequisites
1. Java 17 or above
2. Maven 3.8 or above
3. Internet access to crawl test pages

Project Layout
- src/main/java/hk/ust/comp4321/SpiderMain.java : crawler + indexer entry
- src/main/java/hk/ust/comp4321/SpiderResultExporter.java : test program
- src/main/resources/stopwords.txt : stop-word dictionary (replaceable)
- db_schema_design.txt : JDBM schema documentation

Build
1. Open terminal at project root.
2. If mvn is not recognized in a new terminal, set Java/Maven paths first (Windows PowerShell):
   $env:JAVA_HOME="C:\Users\Tri Thai\.jdk\jdk-17.0.16"
   $env:PATH="C:\Users\Tri Thai\.jdk\jdk-17.0.16\bin;C:\Users\Tri Thai\.maven\maven-3.9.14\bin;" + $env:PATH
3. Run:
   mvn clean compile

Run Spider (BFS crawl + indexing)
Default (seed URL + 30 pages):
   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain

Custom arguments:
   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain -Dexec.args="<seedUrl> <limit> <dbPath> <stopWordsPath>"

Example:
   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain -Dexec.args="https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm 30 data/phase1.db src/main/resources/stopwords.txt"

Run Test Program (generate spider result.txt)
Default:
   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter

Custom arguments:
   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter -Dexec.args="<dbPath> <outputFile>"

Example:
   mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter

Validate Result Count (should be 30)
   (Select-String -Path "spider result.txt" -Pattern "^-+$" | Measure-Object).Count

Fallback (if Maven argument parsing still fails in PowerShell)
1. Build classpath and run directly with java:
   $cp="target/classes;C:\Users\Tri Thai\.m2\repository\jdbm\jdbm\1.0\jdbm-1.0.jar;C:\Users\Tri Thai\.m2\repository\org\jsoup\jsoup\1.18.3\jsoup-1.18.3.jar"
   java -cp $cp hk.ust.comp4321.SpiderMain "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm" 30 "data/phase1.db" "src/main/resources/stopwords.txt"
   java -cp $cp hk.ust.comp4321.SpiderResultExporter "data/phase1.db" "spider result.txt"

Expected Output
- JDBM database files under data/
- spider result.txt in project root

Notes
- If the course-provided stop-word dictionary is available, replace src/main/resources/stopwords.txt.
- If the primary seed URL is unavailable, use the backup URL in the assignment description.
