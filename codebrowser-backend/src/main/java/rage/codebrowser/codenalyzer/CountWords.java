package rage.codebrowser.codenalyzer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import rage.codebrowser.dto.SnapshotFile;

public class CountWords implements SnapshotConcepts {

    
    private final String[] INTERESTING_WORDS = {
        "private",
        "public",
        "=",
        "==",
        "if",
        "else",
        "for",
        "while",
        "break",
        "continue"
    };

    @Override
    public Concepts getConcepts(SnapshotFile input) {

        Concepts concepts = new Concepts();

        try {
            countWordsInFile(input, concepts);
        } catch (Exception e) {
            concepts.put("error", 1);
        }

        return concepts;
    }

    private void countWordsInFile(SnapshotFile input, Concepts concepts) throws FileNotFoundException, IOException {

        BufferedReader reader = openReader(input.getFilepath());

        String line;

        while ((line = reader.readLine()) != null) {
            for (String w : getWords(line)) {
                processWord(w, concepts);
            }
        }
    }

    private BufferedReader openReader(String filePath) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(filePath);
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    private String[] getWords(String line) {
        return line.split("\\s");
    }

    private void processWord(String w, Concepts concepts) {
        if (isInterestingWord(w)) {
            int newValue = (concepts.containsKey(w)) ? concepts.get(w) + 1 : 1;
            concepts.put(w, newValue);
        }
    }

    private boolean isInterestingWord(String word) {
        return Arrays.asList(INTERESTING_WORDS).contains(word);
    }

    
    
    @Override
    public Concepts getConcepts(List<SnapshotFile> input) {
        Concepts snapshotConcepts = new Concepts();
        
        for (SnapshotFile file : input) {
            Concepts fileConcepts = getConcepts(file);
            
            combine(fileConcepts, snapshotConcepts);
        }
        
        return snapshotConcepts;
    }
    
    
    private void combine(Concepts from, Concepts to) {
        for (String entry : from.keySet()) {
            if (to.keySet().contains(entry)) {
                int currentValue = to.get(entry);
                int newValue = currentValue + from.get(entry);
                to.put(entry, newValue);
            } else {
                to.put(entry, from.get(entry));
            }
        }
    }
}
