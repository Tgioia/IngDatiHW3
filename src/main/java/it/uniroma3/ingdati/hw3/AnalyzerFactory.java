package it.uniroma3.ingdati.hw3;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.IOException;
import java.util.Map;

public class AnalyzerFactory {

    public static final String FIELD_NOME = "nome";
    public static final String FIELD_CONTENUTO = "contenuto";

    public static Analyzer createAnalyzer() {
        try {
            Analyzer nomeAnalyzer = CustomAnalyzer.builder()
                    .withTokenizer("standard")
                    .addTokenFilter("wordDelimiterGraph")
                    .addTokenFilter("lowercase")
                    .addTokenFilter("flattenGraph")
                    .build();

            Map<String, Analyzer> perFieldAnalyzers = Map.of(
                    FIELD_NOME, nomeAnalyzer,
                    FIELD_CONTENUTO, new ItalianAnalyzer()
            );
            return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzers);
        } catch (IOException e) {
            throw new RuntimeException("Errore nella creazione degli analyzer", e);
        }
    }
}
