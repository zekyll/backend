package rage.codebrowser.codenalyzer;

import java.util.HashMap;
import java.util.List;
import rage.codebrowser.dto.SnapshotFile;

public interface SnapshotConcepts {

    Concepts getConcepts(SnapshotFile input);
    Concepts getConcepts(List<SnapshotFile> input);
}
