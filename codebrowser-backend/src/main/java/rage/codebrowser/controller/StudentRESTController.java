package rage.codebrowser.controller;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.Exercise;
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
    public Course getStudentCourse(@PathVariable Long studentId, @PathVariable("courseId") Course course) {
        return course;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercises", "students/{studentId}/courses/{courseId}/exercises"})
    @ResponseBody
    public List<Exercise> getStudentCourseExercises(@PathVariable Long studentId, @PathVariable("courseId") Course course) {
        return getStudentCourse(studentId, course).getExercises();
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}"})
    @ResponseBody
    public Exercise getExerciseAnswer(@PathVariable Long studentId, @PathVariable Long courseId, @PathVariable("exerciseId") Exercise exercise) {
        return exercise;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshots", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots"})
    @ResponseBody
    public List<Snapshot> getSnapshots(@PathVariable("studentId") Student student, @PathVariable Long courseId, @PathVariable("exerciseId") Exercise exercise) {
        List<Snapshot> snapshots = exerciseAnswerRepository.findByStudentAndExercise(student, exercise).getSnapshots();
        Collections.sort(snapshots);

        return snapshots;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}"})
    @ResponseBody
    public Snapshot getSnapshot(@PathVariable("snapshotId") Snapshot snapshot) {
        return snapshot;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/files", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/files"})
    @ResponseBody
    public List<SnapshotFile> getSnapshotFiles(@PathVariable("snapshotId") Snapshot snapshot) {
        return snapshot.getFiles();
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
