package rage.codebrowser.dto;

import rage.codebrowser.codeanalyzer.domain.DiffList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;

@Entity
public class SnapshotFile extends AbstractNamedPersistable {

    @JsonIgnore
    private String filepath;
    private long filesize;
    private DiffList diffs;

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    /**
     * @return the filesize
     */
    public long getFilesize() {
        return filesize;
    }

    /**
     * @param filesize the filesize to set
     */
    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    /**
     * @return the diffs
     */
    public DiffList getDiffs() {
        return diffs;
    }

    /**
     * @param diffs the diffs to set
     */
    public void setDiffs(DiffList diffs) {
        this.diffs = diffs;
    }

}