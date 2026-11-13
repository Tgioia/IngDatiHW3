package it.uniroma3.ingdati.hw3;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static it.uniroma3.ingdati.hw3.AnalyzerFactory.*;

public class FileIndexer {

    private static final String DEFAULT_DOCS_DIR = "test-data";
    private static final String DEFAULT_INDEX_DIR = "lucene-index";

    public static void main(String[] args) throws IOException {
        String docsDir = args.length > 0 ? args[0] : DEFAULT_DOCS_DIR;
        String indexDir = args.length > 1 ? args[1] : DEFAULT_INDEX_DIR;

        System.out.println("=== File Indexer ===");
        System.out.println("Directory documenti: " + new File(docsDir).getAbsolutePath());
        System.out.println("Directory indice:    " + new File(indexDir).getAbsolutePath());

        int count = indexFiles(docsDir, indexDir);

        System.out.println("\nIndicizzazione completata: " + count + " file indicizzati.");
    }

    public static int indexFiles(String docsPath, String indexPath) throws IOException {
        Path indexDir = Paths.get(indexPath);
        Analyzer analyzer = createAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        int count = 0;
        long startTime = System.currentTimeMillis();

        try (FSDirectory directory = FSDirectory.open(indexDir);
             IndexWriter writer = new IndexWriter(directory, config)) {

            File docsDir = new File(docsPath);
            if (!docsDir.exists() || !docsDir.isDirectory()) {
                System.err.println("Errore: la directory '" + docsPath + "' non esiste.");
                return 0;
            }

            File[] txtFiles = docsDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (txtFiles == null || txtFiles.length == 0) {
                System.err.println("Nessun file .txt trovato in '" + docsPath + "'.");
                return 0;
            }

            System.out.println("Trovati " + txtFiles.length + " file .txt\n");

            for (File file : txtFiles) {
                String fileName = file.getName();
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);

                Document doc = new Document();
                doc.add(new TextField(FIELD_NOME, fileName, Field.Store.YES));
                doc.add(new TextField(FIELD_CONTENUTO, content, Field.Store.YES));

                writer.addDocument(doc);
                count++;
                System.out.println("  Indicizzato: " + fileName + " (" + content.length() + " caratteri)");
            }

            writer.commit();
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("\nTempo di indicizzazione: " + elapsed + " ms");

        return count;
    }
}
