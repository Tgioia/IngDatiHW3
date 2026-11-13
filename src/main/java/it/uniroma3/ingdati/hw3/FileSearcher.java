package it.uniroma3.ingdati.hw3;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import static it.uniroma3.ingdati.hw3.AnalyzerFactory.*;

public class FileSearcher {

    private static final String DEFAULT_INDEX_DIR = "lucene-index";
    private static final int MAX_RESULTS = 10;

    public static void main(String[] args) throws IOException {
        String indexDir = args.length > 0 ? args[0] : DEFAULT_INDEX_DIR;

        try (FSDirectory directory = FSDirectory.open(Paths.get(indexDir));
             IndexReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = createAnalyzer();

            System.out.println("=== File Searcher ===");
            System.out.println("Indice: " + Paths.get(indexDir).toAbsolutePath());
            System.out.println("Documenti nell'indice: " + reader.numDocs());
            System.out.println();
            System.out.println("Sintassi query:");
            System.out.println("  nome <termini>       - cerca per nome file");
            System.out.println("  contenuto <termini>  - cerca nel contenuto");
            System.out.println("  (usa \"...\" per phrase query)");
            System.out.println("  Digita 'exit' per uscire.");
            System.out.println();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Query> ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) break;

                executeQuery(line, searcher, analyzer);
            }
        }
    }

    public static void executeQuery(String line, IndexSearcher searcher, Analyzer analyzer) throws IOException {
        String field;
        String queryString;

        if (line.toLowerCase().startsWith("nome ")) {
            field = FIELD_NOME;
            queryString = line.substring(5).trim();
        } else if (line.toLowerCase().startsWith("contenuto ")) {
            field = FIELD_CONTENUTO;
            queryString = line.substring(10).trim();
        } else {
            System.out.println("  Errore: la query deve iniziare con 'nome' o 'contenuto'.");
            System.out.println("  Esempio: contenuto \"intelligenza artificiale\"");
            return;
        }

        if (queryString.isEmpty()) {
            System.out.println("  Errore: specificare almeno un termine di ricerca.");
            return;
        }

        try {
            QueryParser parser = new QueryParser(field, analyzer);
            Query query = parser.parse(queryString);

            System.out.println("  Campo: " + field);
            System.out.println("  Query Lucene: " + query.toString());

            TopDocs hits = searcher.search(query, MAX_RESULTS);
            StoredFields storedFields = searcher.storedFields();

            System.out.println("  Risultati trovati: " + hits.totalHits);

            if (hits.scoreDocs.length == 0) {
                System.out.println("  Nessun risultato.");
            } else {
                for (int i = 0; i < hits.scoreDocs.length; i++) {
                    ScoreDoc scoreDoc = hits.scoreDocs[i];
                    Document doc = storedFields.document(scoreDoc.doc);
                    String nome = doc.get(FIELD_NOME);
                    String contenuto = doc.get(FIELD_CONTENUTO);
                    String preview = contenuto.length() > 150
                            ? contenuto.substring(0, 150) + "..."
                            : contenuto;
                    preview = preview.replace("\n", " ");

                    System.out.printf("  [%d] (score: %.4f) %s%n", i + 1, scoreDoc.score, nome);
                    System.out.printf("      %s%n", preview);
                }
            }
            System.out.println();

        } catch (ParseException e) {
            System.out.println("  Errore nel parsing della query: " + e.getMessage());
        }
    }
}
