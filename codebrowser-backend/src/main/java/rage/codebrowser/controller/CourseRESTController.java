package rage.codebrowser.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rage.codebrowser.codenalyzer.SnapshotFileConcepts;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.Exercise;
import rage.codebrowser.dto.ExerciseAnswer;
import rage.codebrowser.dto.Snapshot;
import rage.codebrowser.dto.SnapshotFile;
import rage.codebrowser.dto.Student;
import rage.codebrowser.repository.CourseRepository;
import rage.codebrowser.repository.ExerciseAnswerRepository;
import rage.codebrowser.repository.TagRepository;

@Controller
public class CourseRESTController {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ExerciseAnswerRepository exerciseAnswerRepository;
    @Autowired
    private TagRepository tagRepository;
    
    @RequestMapping(value = {"courses"})
    @ResponseBody
    public List<Course> getCourses() {
        return courseRepository.findAll();
    }

    @RequestMapping(value = {"courses/{courseId}", "course/{courseId}"})
    @ResponseBody
    public Course getCourse(@PathVariable("courseId") Course course) {
        return course;
    }

    @RequestMapping(value = {"courses/{courseId}/exercises", "course/{courseId}/exercises"})
    @ResponseBody
    public List<Exercise> getCourseExercises(@PathVariable("courseId") Course course) {
        return course.getExercises();
    }

    @RequestMapping(value = {"courses/{courseId}/exercises/{exerciseId}", "course/{courseId}/exercise/{exerciseId}"})
    @ResponseBody
    public Exercise getCourseExercise(@PathVariable Long courseId, @PathVariable("exerciseId") Exercise exercise) {
        return exercise;
    }

    @RequestMapping(value = {"courses/{courseId}/exercises/{exerciseId}/students", "course/{courseId}/exercise/{exerciseId}/students"})
    @ResponseBody
    public List<Student> getStudentsThatWorkedOnExercise(@PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise) {
        List<Student> studentsThatWorkedOnExercise = new ArrayList<Student>();
        for (Student student : course.getStudents()) {
            ExerciseAnswer answer = exerciseAnswerRepository.findByStudentAndExercise(student, exercise);
            if (answer != null) {
                studentsThatWorkedOnExercise.add(student);
            }
        }

        return studentsThatWorkedOnExercise;
    }

    @RequestMapping(value = {"courses/{courseId}/students", "course/{courseId}/students"})
    @ResponseBody
    public List<Student> getCourseStudents(@PathVariable("courseId") Course course) {
        return course.getStudents();
    }

    @RequestMapping(value = {"courses/{courseId}/students/{studentId}", "course/{courseId}/students/{studentId}"})
    @ResponseBody
    public Student getCourseStudent(@PathVariable Long courseId, @PathVariable("studentId") Student student) {
        return student;
    }

    @RequestMapping(value = {"course/{courseId}/student/{studentId}/exercise/{exerciseId}/snapshots", "courses/{courseId}/students/{studentId}/exercises/{exerciseId}/snapshots"})
    @ResponseBody
    public List<Snapshot> getStudentCourseSnapshots(@PathVariable("studentId") Student student, @PathVariable Long courseId, @PathVariable("exerciseId") Exercise exercise) {
        List<Snapshot> snapshots = exerciseAnswerRepository.findByStudentAndExercise(student, exercise).getSnapshots();
        return Snapshot.filterSequentialUnalteredSnapshots(snapshots);
    }

    @RequestMapping(value = {"course/{courseId}/student/{studentId}/exercise/{exerciseId}/snapshot/{snapshotId}/files", "courses/{courseId}/students/{studentId}/exercises/{exerciseId}/snapshots/{snapshotId}/files"})
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

    @RequestMapping(value = {"course/{courseId}/student/{studentId}/exercise/{exerciseId}/snapshot/{snapshotId}/file/{snapshotFileId}", "courses/{courseId}/students/{studentId}/exercises/{exerciseId}/snapshots/{snapshotId}/files/{snapshotFileId}"})
    @ResponseBody
    public SnapshotFile getSnapshotFile(@PathVariable("snapshotFileId") SnapshotFile snapshotFile) {
        return snapshotFile;
    }

    @RequestMapping(value = {"course/{courseId}/student/{studentId}/exercise/{exerciseId}/snapshot/{snapshotId}/file/{snapshotFileId}/content", "courses/{courseId}/students/{studentId}/exercises/{exerciseId}/snapshots/{snapshotId}/files/{snapshotFileId}/content"}, produces = "text/plain")
    @ResponseBody
    public FileSystemResource getSnapshotFileContent(@PathVariable("snapshotFileId") SnapshotFile snapshotFile) {
        return new FileSystemResource(snapshotFile.getFilepath());
    }
}
