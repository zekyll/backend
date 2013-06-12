package rage.codebrowser.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.Exercise;
import rage.codebrowser.dto.ExerciseAnswer;
import rage.codebrowser.dto.Snapshot;
import rage.codebrowser.dto.SnapshotFile;
import rage.codebrowser.dto.Student;
import rage.codebrowser.repository.ExerciseAnswerRepository;
import rage.codebrowser.repository.StudentRepository;

@Controller
public class StudentRESTController {

    @Autowired
    private ExerciseAnswerRepository exerciseAnswerRepository;
    @Autowired
    private StudentRepository studentRepository;

    @RequestMapping(value = {"students"})
    @ResponseBody
    public List<Student> getStudents() {
        return studentRepository.findAll();
    }

    @RequestMapping(value = {"student/{studentId}", "students/{studentId}"})
    @ResponseBody
    public Student getStudent(@PathVariable("studentId") Student student) {
        return student;
    }

    @RequestMapping(value = {"student/{studentId}/courses", "students/{studentId}/courses"})
    @ResponseBody
    public List<Course> getStudentCourses(@PathVariable("studentId") Student student) {
        return student.getCourses();
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}", "students/{studentId}/courses/{courseId}"})
    @ResponseBody
    public Course getStudentCourse(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course) {
        List<Exercise> exercises = course.getExercises();
        
        Set<Exercise> existingExercises = new HashSet<Exercise>();
        List<ExerciseAnswer> answers = exerciseAnswerRepository.findByStudent(student);
        for (ExerciseAnswer exerciseAnswer : answers) {
            existingExercises.add(exerciseAnswer.getExercise());
        }
        
        exercises.retainAll(existingExercises);
        course.setExercises(exercises);
        return course;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercises", "students/{studentId}/courses/{courseId}/exercises"})
    @ResponseBody
    public List<Exercise> getStudentCourseExercises(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course) {
        return getStudentCourse(student, course).getExercises();
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}"})
    @ResponseBody
    public Exercise getExerciseAnswer(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise) {
        List<Exercise> exercises = getStudentCourseExercises(student, course);
        if(!exercises.contains(exercise)) {
            return null;
        }
        
        return exercise;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshots", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots"})
    @ResponseBody
    public List<Snapshot> getSnapshots(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise) {
        List<Exercise> exercises = getStudentCourseExercises(student, course);
        if(!exercises.contains(exercise)) {
            return null;
        }
        
        List<Snapshot> snapshots = exerciseAnswerRepository.findByStudentAndExercise(student, exercise).getSnapshots();
        Collections.sort(snapshots);

        return snapshots;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}"})
    @ResponseBody
    public Snapshot getSnapshot(@PathVariable("snapshotId") Snapshot snapshot) {
        Collections.sort(snapshot.getFiles(), new Comparator<SnapshotFile>() {
            @Override
            public int compare(SnapshotFile o1, SnapshotFile o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return snapshot;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/files", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/files"})
    @ResponseBody
    public List<SnapshotFile> getSnapshotFiles(@PathVariable("snapshotId") Snapshot snapshot) {
        List<SnapshotFile> files = snapshot.getFiles();
        Collections.sort(files, new Comparator<SnapshotFile>() {
            @Override
            public int compare(SnapshotFile o1, SnapshotFile o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        return files;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/file/{snapshotFileId}", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/files/{snapshotFileId}"})
    @ResponseBody
    public SnapshotFile getSnapshotFile(@PathVariable("snapshotFileId") SnapshotFile snapshotFile) {
        return snapshotFile;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/file/{snapshotFileId}/content", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/files/{snapshotFileId}/content"}, produces = "text/plain")
    @ResponseBody
    public FileSystemResource getSnapshotFileContent(@PathVariable("snapshotFileId") SnapshotFile snapshotFile) {
        return new FileSystemResource(snapshotFile.getFilepath());
    }
}
