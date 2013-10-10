package rage.codebrowser.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import difflib.DiffUtils;
import difflib.Patch;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.Transient;

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
    @Transient
    private Exercise exercise;
    @Transient
    private CourseInfo course;
    @OneToMany
    private List<Testresult> tests;
    
    private boolean compiles;
    private Integer percentageOfTestsPassing;

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

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public CourseInfo getCourse() {
        return course;
    }

    public void setCourse(CourseInfo course) {
        this.course = course;
    }

    public boolean getCompiles() {
        return compiles;
    }

    public void setCompiles(boolean compiles) {
        this.compiles = compiles;
    }

    @Override
    public int compareTo(Snapshot o) {
        return this.snapshotTime.compareTo(o.snapshotTime);
    }

    public static List<Snapshot> filterSequentialUnalteredSnapshots(List<Snapshot> snapshots) {
        if (snapshots == null || snapshots.size() <= 1) {
            return snapshots;
        }

        List<Snapshot> filteredSnapshots = new ArrayList<Snapshot>();
        filteredSnapshots.add(snapshots.get(0));

        List<String> snapshotLines = filesToLines(snapshots.get(0).getFiles());

        for (int i = 1; i < snapshots.size(); i++) {
            Snapshot current = snapshots.get(i);
            List<String> currentLines = filesToLines(current.getFiles());

            Patch diff = DiffUtils.diff(snapshotLines, currentLines);
            if (diff == null || diff.getDeltas() == null || diff.getDeltas().size() > 0) {
                filteredSnapshots.add(current);
            }

            snapshotLines = currentLines;
        }

        return filteredSnapshots;
    }

    public static List<String> filesToLines(List<SnapshotFile> files) {
        List<String> lines = new ArrayList<String>();
        for (SnapshotFile file : files) {
            lines.add(file.getFilepath());
            lines.addAll(fileToLines(file.getFilepath()));
        }

        return lines;
    }

    public static List<String> fileToLines(String filename) {
        List<String> lines = new LinkedList<String>();
        String line;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(filename));
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {

                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return lines;
    }

    /**
     * @return the percentageOfTestsPassing
     */
    public Integer getPercentageOfTestsPassing() {
        //countPercentage();
        return percentageOfTestsPassing;
    }

    /**
     * @param percentageOfTestsPassing the percentageOfTestsPassing to set
     */
    public void setPercentageOfTestsPassing(Integer percentageOfTestsPassing) {
        this.percentageOfTestsPassing = percentageOfTestsPassing;
    }

    /**
     * @return the tests
     */
    public List<Testresult> getTests() {
        return tests;
    }

    /**
     * @param tests the tests to set
     */
    public void setTests(List<Testresult> tests) {
        this.tests = tests;
        countPercentage();
    }
    
    private void countPercentage() {
        if (tests.isEmpty()){
            this.setPercentageOfTestsPassing(null);
            return;
        }
        
        int count = 0;
        for (Testresult test : tests) {
            if (test.isPassed()){
                count++;
            }
        }
        double percentage = (count*1.0/tests.size())*100;
        this.setPercentageOfTestsPassing((int)percentage);    
    }
}