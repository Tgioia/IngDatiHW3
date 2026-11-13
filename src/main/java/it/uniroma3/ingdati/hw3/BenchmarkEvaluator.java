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
import java.util.*;

import static it.uniroma3.ingdati.hw3.AnalyzerFactory.*;

public class BenchmarkEvaluator {

    private static final String DEFAULT_INDEX_DIR = "lucene-index";
    private static final int TOP_K = 10;

    static class BenchmarkQuery {
        String campo;
        String queryText;
        String description;
        Set<String> relevantDocs;

        BenchmarkQuery(String campo, String queryText, String description, String[] relevant) {
            this.campo = campo;
            this.queryText = queryText;
            this.description = description;
            this.relevantDocs = new HashSet<>(Arrays.asList(relevant));
        }
    }

    public static void main(String[] args) throws IOException {
        String indexDir = args.length > 0 ? args[0] : DEFAULT_INDEX_DIR;

        List<BenchmarkQuery> testSuite = List.of(
            new BenchmarkQuery("contenuto", "\"intelligenza artificiale\"", 
                "Phrase query su IA", 
                new String[]{"intelligenza-artificiale.txt", "wiki-intelligenza-artificiale.txt", "wiki-deep-learning.txt", "wiki-machine-learning.txt", "wiki-visione-artificiale.txt", "wiki-rete-neurale-artificiale.txt", "wiki-robotica.txt"}),
            
            new BenchmarkQuery("contenuto", "\"energie rinnovabili\"", 
                "Phrase query su energie pulite", 
                new String[]{"energia-rinnovabile.txt", "wiki-energia-rinnovabile.txt", "cambiamento-climatico.txt", "wiki-energia-solare.txt", "wiki-energia-eolica.txt", "wiki-energia-idroelettrica.txt"}),
            
            new BenchmarkQuery("contenuto", "Roma", 
                "Term query sulla città/storia di Roma", 
                new String[]{"storia-di-roma.txt", "wiki-roma.txt", "wiki-storia-di-roma.txt", "wiki-colosseo.txt", "wiki-impero-romano.txt", "wiki-repubblica-romana.txt"}),
            
            new BenchmarkQuery("contenuto", "\"sistema solare\"", 
                "Phrase query astronomia", 
                new String[]{"sistema-solare.txt", "wiki-sistema-solare.txt", "wiki-sole.txt", "wiki-terra.txt", "wiki-marte-astronomia.txt", "wiki-giove-astronomia.txt", "wiki-saturno-astronomia.txt"}),
            
            new BenchmarkQuery("contenuto", "calcio", 
                "Term query sport calcio", 
                new String[]{"calcio-italiano.txt", "wiki-calcio-sport.txt", "wiki-campionato-mondiale-di-calcio.txt", "wiki-serie-a.txt", "wiki-juventus-football-club.txt", "wiki-associazione-calcio-milan.txt", "wiki-football-club-internazionale-milano.txt", "wiki-associazione-sportiva-roma.txt"}),

            new BenchmarkQuery("contenuto", "climatico", 
                "Term query con stemming su clima", 
                new String[]{"cambiamento-climatico.txt", "wiki-cambiamento-climatico.txt", "wiki-riscaldamento-globale.txt", "wiki-effetto-serra.txt", "energia-rinnovabile.txt", "wiki-accordo-di-parigi-2015.txt"}),

            new BenchmarkQuery("contenuto", "cucina", 
                "Term query gastronomia", 
                new String[]{"cucina-giapponese.txt", "wiki-cucina-giapponese.txt", "wiki-cucina-italiana.txt", "ricetta-carbonara.txt", "wiki-carbonara.txt", "wiki-pizza.txt", "wiki-ramen.txt", "wiki-sushi.txt"}),

            new BenchmarkQuery("contenuto", "\"information retrieval\"", 
                "Phrase query IR", 
                new String[]{"information-retrieval.txt", "wiki-information-retrieval.txt", "wiki-motore-di-ricerca.txt", "wiki-indice-inverso.txt"}),

            new BenchmarkQuery("nome", "java", 
                "Query nome file su Java", 
                new String[]{"programmazione-java.txt", "wiki-java-linguaggio-di-programmazione.txt"}),

            new BenchmarkQuery("nome", "carbonara", 
                "Query nome file su Carbonara", 
                new String[]{"ricetta-carbonara.txt", "wiki-carbonara.txt"}),

            new BenchmarkQuery("nome", "solare", 
                "Query nome file su solare", 
                new String[]{"sistema-solare.txt", "wiki-sistema-solare.txt", "wiki-energia-solare.txt"}),

            new BenchmarkQuery("nome", "romano", 
                "Query nome file su romano", 
                new String[]{"wiki-impero-romano.txt", "wiki-repubblica-romana.txt"})
        );

        System.out.println("╔════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                VALUTAZIONE QUANTITATIVA DEL SISTEMA IR (114 DOCUMENTI)                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════════╝\n");

        try (FSDirectory directory = FSDirectory.open(Paths.get(indexDir));
             IndexReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = createAnalyzer();
            StoredFields storedFields = searcher.storedFields();

            double totalP = 0;
            double totalR = 0;
            double totalF1 = 0;
            double totalAP = 0;

            System.out.printf("%-4s | %-10s | %-30s | %-6s | %-8s | %-8s | %-8s | %-8s%n",
                    "#", "Campo", "Query", "|Rel|", "P@10", "R@10", "F1@10", "AP");
            System.out.println("--------------------------------------------------------------------------------------------------");

            int qIndex = 1;
            for (BenchmarkQuery bq : testSuite) {
                QueryParser parser = new QueryParser(bq.campo, analyzer);
                Query query;
                try {
                    query = parser.parse(bq.queryText);
                } catch (ParseException e) {
                    System.err.println("Errore parsing: " + e.getMessage());
                    continue;
                }

                TopDocs hits = searcher.search(query, TOP_K);
                List<String> retrieved = new ArrayList<>();
                for (ScoreDoc sd : hits.scoreDocs) {
                    Document doc = storedFields.document(sd.doc);
                    retrieved.add(doc.get(FIELD_NOME));
                }

                int tp = 0;
                double sumPrecision = 0.0;
                int hitsFound = 0;

                for (int i = 0; i < retrieved.size(); i++) {
                    String docName = retrieved.get(i);
                    if (bq.relevantDocs.contains(docName)) {
                        tp++;
                        hitsFound++;
                        sumPrecision += (double) hitsFound / (i + 1);
                    }
                }

                int k = Math.max(retrieved.size(), 1);
                double pAtK = (double) tp / k;
                double rAtK = bq.relevantDocs.isEmpty() ? 0.0 : (double) tp / bq.relevantDocs.size();
                double f1AtK = (pAtK + rAtK > 0) ? (2 * pAtK * rAtK) / (pAtK + rAtK) : 0.0;
                double ap = bq.relevantDocs.isEmpty() ? 0.0 : (sumPrecision / bq.relevantDocs.size());

                totalP += pAtK;
                totalR += rAtK;
                totalF1 += f1AtK;
                totalAP += ap;

                System.out.printf("Q%-3d | %-10s | %-30s | %-6d | %-8.4f | %-8.4f | %-8.4f | %-8.4f%n",
                        qIndex++, bq.campo, bq.queryText, bq.relevantDocs.size(), pAtK, rAtK, f1AtK, ap);
            }

            int n = testSuite.size();
            System.out.println("--------------------------------------------------------------------------------------------------");
            System.out.printf("MEDIA GLOBALE (%d query)                                        | %-8.4f | %-8.4f | %-8.4f | %-8.4f (MAP)%n",
                    n, totalP / n, totalR / n, totalF1 / n, totalAP / n);
            System.out.println("==================================================================================================\n");
        }
    }
}
