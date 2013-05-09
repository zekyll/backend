package rage.codebrowser.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;

@Entity
public class SnapshotFile extends AbstractNamedPersistable {

    @JsonIgnore
    private String filepath;

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }
}