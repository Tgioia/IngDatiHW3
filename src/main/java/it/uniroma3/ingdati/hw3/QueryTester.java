package it.uniroma3.ingdati.hw3;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

import static it.uniroma3.ingdati.hw3.AnalyzerFactory.*;

public class QueryTester {

    private static final String DEFAULT_INDEX_DIR = "lucene-index";

    private static final String[][] TEST_QUERIES = {
            {"contenuto", "Roma"},
            {"contenuto", "\"intelligenza artificiale\""},
            {"contenuto", "Italia"},
            {"contenuto", "\"energie rinnovabili\""},
            {"contenuto", "Lucene"},
            {"contenuto", "climatico"},
            {"contenuto", "calcio campionato mondiale"},
            {"contenuto", "\"sistema solare\""},
            {"nome", "carbonara"},
            {"nome", "java"},
            {"nome", "italiano"},
            {"nome", "retrieval"}
    };

    public static void main(String[] args) throws IOException {
        String indexDir = args.length > 0 ? args[0] : DEFAULT_INDEX_DIR;

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║     HOMEWORK 3 - Test Automatico Query      ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        int numFiles = FileIndexer.indexFiles("test-data", indexDir);
        System.out.println();

        try (FSDirectory directory = FSDirectory.open(Paths.get(indexDir));
             IndexReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = createAnalyzer();

            System.out.println("══════════════════════════════════════════════");
            System.out.println("  ESECUZIONE QUERY DI TEST");
            System.out.println("══════════════════════════════════════════════\n");

            for (int i = 0; i < TEST_QUERIES.length; i++) {
                String campo = TEST_QUERIES[i][0];
                String queryText = TEST_QUERIES[i][1];
                String fullQuery = campo + " " + queryText;

                System.out.println("──────────────────────────────────────────────");
                System.out.printf("Query %d: %s%n", i + 1, fullQuery);
                System.out.println("──────────────────────────────────────────────");

                FileSearcher.executeQuery(fullQuery, searcher, analyzer);
            }

            System.out.println("══════════════════════════════════════════════");
            System.out.println("  TEST COMPLETATO: " + TEST_QUERIES.length + " query eseguite");
            System.out.println("══════════════════════════════════════════════");
        }
    }
}
