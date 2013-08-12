package rage.codebrowser.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
public class Tag extends AbstractPersistable<Long> implements Comparable<Tag> {

    @JoinColumn
    @JsonIgnore
    @ManyToOne
    private Course course;
    @JoinColumn
    @JsonIgnore
    @ManyToOne
    private Student student;
    @JoinColumn
    @JsonIgnore
    @ManyToOne
    private Exercise exercise;
    @Column
    private String text;

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int compareTo(Tag o) {
        if (text == null) {
            return -1;
        }

        return text.compareTo(o.text);
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return super.isNew(); //To change body of generated methods, choose Tools | Templates.
    }
}