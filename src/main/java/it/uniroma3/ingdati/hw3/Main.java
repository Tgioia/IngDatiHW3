package it.uniroma3.ingdati.hw3;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            System.out.println("\nNessun argomento specificato. Esecuzione del test completo...\n");
            QueryTester.main(args);
            return;
        }

        String command = args[0].toLowerCase();
        String[] remaining = new String[Math.max(0, args.length - 1)];
        System.arraycopy(args, 1, remaining, 0, remaining.length);

        switch (command) {
            case "index":
                FileIndexer.main(remaining);
                break;
            case "search":
                FileSearcher.main(remaining);
                break;
            case "test":
                QueryTester.main(remaining);
                break;
            case "benchmark":
                BenchmarkEvaluator.main(remaining);
                break;
            default:
                printUsage();
                break;
        }
    }

    private static void printUsage() {
        System.out.println("Uso: java -jar hw3.jar [index|search|test|benchmark] [docsDir] [indexDir]");
        System.out.println();
        System.out.println("  index     - Indicizza i file .txt dalla directory specificata");
        System.out.println("  search    - Avvia la modalita' interattiva di ricerca");
        System.out.println("  test      - Esegue le query di test");
        System.out.println("  benchmark - Calcola metriche di IR (Precision, Recall, MAP)");
    }
}
