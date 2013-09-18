package rage.codebrowser.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
public class Tag extends AbstractPersistable<Long> implements Comparable<Tag> {

    @JoinColumn
    @ManyToOne
    @JsonIgnoreProperties({"exercises"})
    private Course course;

    @JoinColumn
    @ManyToOne
    @JsonIgnoreProperties({"courses"})
    private Student student;

    @JoinColumn
    @ManyToOne
    private Exercise exercise;

    @ManyToOne
    @JsonIgnoreProperties({"tags"})
    private TagName tagName;

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

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

    public TagName getTagName() {
        return tagName;
    }

    public void setTagName(TagName tagName) {
        this.tagName = tagName;
    }

    @Override
    public int compareTo(Tag o) {
        if (getTagName() == null) {
            return -1;
        }

        return getTagName().compareTo(o.getTagName());
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return super.isNew();
    }
}
