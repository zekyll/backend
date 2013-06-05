package rage.codebrowser.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;

@Entity
public class Snapshot extends AbstractNamedPersistable implements Comparable<Snapshot> {

    @JsonIgnore
    @ManyToOne
    private ExerciseAnswer exerciseAnswer;
    private String type;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date snapshotTime;
    @OneToMany
    private List<SnapshotFile> files;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getSnapshotTime() {
        return snapshotTime;
    }

    public void setSnapshotTime(Date snapshotTime) {
        this.snapshotTime = snapshotTime;
    }

    public List<SnapshotFile> getFiles() {
        return files;
    }

    public void setFiles(List<SnapshotFile> snapshotFiles) {
        this.files = snapshotFiles;
    }

    public ExerciseAnswer getExerciseAnswer() {
        return exerciseAnswer;
    }

    public void setExerciseAnswer(ExerciseAnswer exerciseAnswer) {
        this.exerciseAnswer = exerciseAnswer;
    }

    @Override
    public int compareTo(Snapshot o) {
        return this.snapshotTime.compareTo(o.snapshotTime);
    }
}