package rage.codebrowser.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
public class ExerciseAnswer extends AbstractPersistable<Long> implements Serializable {

    @JsonIgnore
    @ManyToOne
    private Student student;
    @JsonIgnore
    @ManyToOne
    private Exercise exercise;
    @OneToMany(mappedBy = "exerciseAnswer")
    private List<Snapshot> snapshots;

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public List<Snapshot> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<Snapshot> snapshots) {
        this.snapshots = snapshots;
    }
}
