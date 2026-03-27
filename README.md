# COMP-4321-VectorFind

VectorFind is a Java-based web crawling and indexing project for COMP4321.

This repository currently includes a complete **Phase 1** implementation:
- Breadth-first spider (crawler)
- Integrated indexer with stop-word filtering and Porter stemming
- JDBM-backed storage for metadata, mappings, indexes, and link graph
- Export tool that generates `spider result.txt`

## Features

- Crawls pages recursively from a seed URL using BFS
- Handles cyclic links safely
- Stores URL <=> page ID and word <=> word ID mappings
- Builds separate body/title inverted indexes
- Stores parent/child page relationships
- Supports reruns with refresh logic based on page update checks

## Tech Stack

- Java 17
- Maven
- Jsoup (HTML fetch + parsing)
- JDBM (persistent key-value data structures)

## Repository Layout

```text
src/main/java/hk/ust/comp4321/
  SpiderMain.java                 # crawler + indexer entry point
  SpiderResultExporter.java       # exports plain-text report
  crawler/BfsCrawler.java
  storage/SearchDb.java
  model/*
  util/*
src/main/resources/
  stopwords.txt
data/
  phase1.db.db
  phase1.db.lg
db_schema_design.txt
readme.txt                        # course-oriented instructions
```

## Quick Start (PowerShell, Windows)

Run commands from the project root.

### 1) Configure Java + Maven (only if `mvn` is not recognized)

```powershell
$env:JAVA_HOME="C:\Users\Tri Thai\.jdk\jdk-17.0.16"
$env:PATH="C:\Users\Tri Thai\.jdk\jdk-17.0.16\bin;C:\Users\Tri Thai\.maven\maven-3.9.14\bin;" + $env:PATH
```

### 2) Build

```powershell
mvn clean compile
```

### 3) Run crawler + indexer (30 pages)

```powershell
mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderMain -Dexec.args="https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm 30 data/phase1.db src/main/resources/stopwords.txt"
```

### 4) Export report

```powershell
mvn --% exec:java -Dexec.mainClass=hk.ust.comp4321.SpiderResultExporter
```

### 5) Verify output count

```powershell
(Select-String -Path "spider result.txt" -Pattern "^-+$" | Measure-Object).Count
```

Expected result: `30`

## Output Files

- `data/phase1.db.db`
- `data/phase1.db.lg`
- `spider result.txt`

## Notes

- `readme.txt` contains the course submission-oriented instructions.
- If your environment parses Maven args differently in PowerShell, use `--%` as shown above.
- The stop-word list can be replaced with the official course-provided dictionary.

## License

This repository is for educational use in COMP4321.
