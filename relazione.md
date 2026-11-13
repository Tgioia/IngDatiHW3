# Homework 3 — Ingegneria dei Dati 2026/2027

**Studente:** Tommaso G.  
**Email istituzionale:** `tom.cognome@stud.uniroma3.it`  
**Matricola:** *[Inserisci Matricola]*  
**Corso:** Ingegneria dei Dati — Prof. Paolo Merialdo  
**Repository GitHub:** *[https://github.com/username/repo_name]*

---

## 1. Descrizione del progetto

Il progetto implementa un motore di Information Retrieval basato su **Apache Lucene 9.12.0** in Java 17 con build system Gradle. Il sistema indicizza file di testo (`.txt`) separando l'informazione su due campi:

- **`nome`**: il nome del file (es. `wiki-deep-learning.txt`)
- **`contenuto`**: il testo completo del documento

L'applicazione offre:
1. **Indicizzazione batch** (`FileIndexer`): elabora i file `.txt` e popola l'indice su disco.
2. **Ricerca interattiva** (`FileSearcher`): interprete da console per eseguire query arbitrarie (sintassi `nome <termini>` o `contenuto <termini>`, con supporto a phrase queries racchiuse tra virgolette `""`).
3. **Valutazione quantitativa** (`BenchmarkEvaluator`): calcola metriche formali di IR (**Precision@10**, **Recall@10**, **F1-Score@10** e **MAP**) su un ground truth predeterminato.

---

## 2. Costruzione del Dataset per la Valutazione

Per superare i limiti di un dataset artificialmente ridotto e poter calcolare metriche statisticamente significative (in particolare la **Recall**), il dataset è stato composto su due livelli per un totale di **114 documenti .txt** (oltre 53.500 caratteri):

1. **Nucleo tematico artigianale (13 file):** file scritti a mano su argomenti diversificati con controllo lessicale preciso.
2. **Estensione automatica da Wikipedia Italia (101 file):** scraping condotto programmaticamente tramite le API REST ufficiali di Wikipedia (`/api/rest_v1/page/summary`) su una vasta gamma di voci correlate ai domini tematici:
   - *Informatica & IR:* Intelligenza artificiale, Machine learning, Deep learning, Indice inverso, Information retrieval, Big data, SQL, NoSQL, Crittografia, Sistemi operativi, Java, Python, Compilatori, Reti.
   - *Storia & Città:* Roma, Storia di Roma, Colosseo, Impero romano, Giulio Cesare, Augusto, Firenze, Venezia, Milano.
   - *Scienza & Astronomia:* Sistema solare, Terra, Sole, Luna, Marte, Giove, Galassie, Buchi neri.
   - *Ambiente & Clima:* Cambiamento climatico, Riscaldamento globale, Effetto serra, Energie rinnovabili (solare, eolica, idroelettrica, nucleare), Accordo di Parigi.
   - *Cultura, Cucina & Sport:* Dante, Divina Commedia, Calcio, Serie A, Formula 1, Cucina italiana, Carbonara, Pizza, Cucina giapponese, Sushi, Ramen, Musica classica, Opera lirica.

---

## 3. Scelta degli Analyzer

Gli analyzer sono configurati in modo centralizzato in `AnalyzerFactory` tramite `PerFieldAnalyzerWrapper`:

### Campo `nome` — CustomAnalyzer
- **Pipeline:** `StandardTokenizer` → `WordDelimiterGraphFilter` → `LowerCaseFilter` → `FlattenGraphFilter`
- **Motivazione:** I file contengono caratteri speciali e trattini (es. `wiki-sistema-solare.txt`). Il `WordDelimiterGraphFilter` spezza sui delimitatori (`-`, `_`, `.`) separando anche l'estensione `.txt`, consentendo la ricerca puntuale di ciascun token componente.

### Campo `contenuto` — ItalianAnalyzer
- **Pipeline:** `StandardTokenizer` → `LowerCaseFilter` → `ItalianStopFilter` → `SnowballStemmer (Italian)`
- **Motivazione:** Dato che il corpus è in italiano, l'analyzer elimina le stopword inutili e applica lo stemming. Questo massimizza la **Recall**: ad esempio la ricerca di *"climatico"* recupera anche documenti contenenti *"climatica"* o *"climatiche"*.

---

## 4. Prestazioni di Indicizzazione

| Parametro | Valore Misurato |
|---|---|
| **Documenti indicizzati** | **114 file .txt** |
| **Volume testuale** | ~53.500 caratteri |
| **Tempo totale di indicizzazione** | **558 ms** |
| **Tempo medio per documento** | **4.89 ms** |
| **Throughput** | ~204 documenti/secondo |

---

## 5. Valutazione Sperimentale con Metriche di IR

È stata definita una suite di **12 query di test** corredate dal rispettivo **Ground Truth** $\mathcal{R}$ di documenti rilevanti. Sono state calcolate:
- **Precision@10 ($P@10$)**
- **Recall@10 ($R@10$)**
- **F1-Score@10**
- **Average Precision (AP)** e **Mean Average Precision (MAP)**

### Risultati del Benchmark

| # | Campo | Query | \|Rel\| | P@10 | R@10 | F1@10 | AP |
|---|---|---|---|---|---|---|---|
| Q1 | contenuto | `"intelligenza artificiale"` | 7 | 0.8000 | 0.5714 | 0.6667 | 0.5429 |
| Q2 | contenuto | `"energie rinnovabili"` | 6 | 1.0000 | 0.5000 | 0.6667 | 0.5000 |
| Q3 | contenuto | `Roma` | 6 | 0.5000 | 0.8333 | 0.6250 | 0.5361 |
| Q4 | contenuto | `"sistema solare"` | 7 | 0.8750 | 1.0000 | 0.9333 | 0.9617 |
| Q5 | contenuto | `calcio` | 8 | 1.0000 | 0.6250 | 0.7692 | 0.6250 |
| Q6 | contenuto | `climatico` | 6 | 1.0000 | 0.6667 | 0.8000 | 0.6667 |
| Q7 | contenuto | `cucina` | 8 | 0.7500 | 0.7500 | 0.7500 | 0.6863 |
| Q8 | contenuto | `"information retrieval"` | 4 | 0.7500 | 0.7500 | 0.7500 | 0.7500 |
| Q9 | nome | `java` | 2 | 1.0000 | 1.0000 | 1.0000 | 1.0000 |
| Q10 | nome | `carbonara` | 2 | 1.0000 | 1.0000 | 1.0000 | 1.0000 |
| Q11 | nome | `solare` | 3 | 1.0000 | 1.0000 | 1.0000 | 1.0000 |
| Q12 | nome | `romano` | 2 | 1.0000 | 0.5000 | 0.6667 | 0.5000 |
| **MEDIA** | | **12 Query** | -- | **0.8896** | **0.7664** | **0.8023** | **0.7307 (MAP)** |

### Analisi dei Risultati
- **Alta precisione (P@10 medio = 88.96%):** Il modello di ranking BM25 premia fortemente i documenti rilevanti mettendoli ai primi posti.
- **Recall media solida (76.64%):** Il limite $K=10$ fa sì che per argomenti molto ampi con molti documenti rilevanti la recall sia limitata a 10/|Rel|, ma quasi tutti i documenti più specifici vengono estratti.
## 6. Conclusioni

L'esperienza condotta ha confermato l'importanza di una corretta progettazione della catena di analisi testuale in Apache Lucene. L'adozione del `WordDelimiterGraphFilter` per il campo `nome` e dell'`ItalianAnalyzer` per il `contenuto` ha garantito un equilibrio ideale tra precisione e capacità di recupero (confermata da una *Precision@10* dell'88.9% e una *MAP* di 0.73).

I tempi di indicizzazione registrati su 114 documenti (meno di 5 ms a file) evidenziano l'elevata efficienza computazionale del motore. Infine, la combinazione tra modello probabilistico BM25 e supporto alle *phrase queries* ha assicurato un ordinamento accurato e una gestione efficace della variabilità morfologica della lingua italiana.
