# Homework 3 - Ingegneria dei Dati 2026/2027

Motore di ricerca testuale basato su Apache Lucene per l'indicizzazione e la ricerca di file `.txt`.

## Requisiti

- Java 17+
- Gradle

## Struttura del progetto

```
HW3/
├── build.gradle                          # Build file Gradle
├── test-data/                            # Directory con i file .txt da indicizzare
│   ├── ricetta-carbonara.txt
│   ├── storia-di-roma.txt
│   ├── intelligenza-artificiale.txt
│   ├── ...
├── src/main/java/it/uniroma3/ingdati/hw3/
│   ├── AnalyzerFactory.java              # Configurazione degli Analyzer
│   ├── FileIndexer.java                  # Indicizzazione dei file .txt
│   ├── FileSearcher.java                 # Ricerca interattiva
│   ├── QueryTester.java                  # Test automatico con 12 query
│   └── Main.java                         # Entry point
└── relazione.pdf                         # Relazione del progetto
```

## Compilazione

```bash
gradle build
```

## Esecuzione

### Indicizzare i file
```bash
gradle indexer
```

### Ricerca interattiva
```bash
gradle searcher
```

### Test automatico delle query
```bash
gradle testQueries
```

### Sintassi delle query
- `nome <termini>` — cerca nel nome del file
- `contenuto <termini>` — cerca nel contenuto del file
- `contenuto "frase esatta"` — phrase query (sequenza esatta di termini)

## Analyzer utilizzati

- **Campo "nome"**: `CustomAnalyzer` (StandardTokenizer + WordDelimiterGraphFilter + LowerCaseFilter)
- **Campo "contenuto"**: `ItalianAnalyzer` (tokenizzazione standard + stopword italiane + stemming Snowball Italian)

## Autore

Tommaso Gioia — Ingegneria dei Dati, Università Roma Tre, A.A. 2026/2027
