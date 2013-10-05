package rage.codebrowser.codenalyzer;

import java.util.HashMap;
import rage.codebrowser.dto.SnapshotFile;

public interface SnapshotFileConcepts {

    HashMap<String, Integer> getConcepts(SnapshotFile input);
}
