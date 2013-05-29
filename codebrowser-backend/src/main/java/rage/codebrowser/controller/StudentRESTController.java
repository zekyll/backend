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
import rage.codebrowser.errors.ResourceNotFoundException;
import rage.codebrowser.repository.CourseRepository;
import rage.codebrowser.repository.ExerciseAnswerRepository;
import rage.codebrowser.repository.ExerciseRepository;
import rage.codebrowser.repository.SnapshotFileRepository;
import rage.codebrowser.repository.SnapshotRepository;
import rage.codebrowser.repository.StudentRepository;

@Controller
public class StudentRESTController {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ExerciseAnswerRepository exerciseAnswerRepository;
    @Autowired
    private ExerciseRepository exerciseRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private SnapshotRepository snapshotRepository;
    @Autowired
    private SnapshotFileRepository snapshotFileRepository;

    @RequestMapping(value = {"students"})
    @ResponseBody
    public List<Student> getStudents() {
        return studentRepository.findAll();
    }

    @RequestMapping(value = {"student/{studentId}", "students/{studentId}"})
    @ResponseBody
    public Student getStudent(@PathVariable Long studentId) {
        Student student = studentRepository.findOne(studentId);
        if (student == null) {
            throw new ResourceNotFoundException();
        }

        return student;
    }

    @RequestMapping(value = {"student/{studentId}/courses", "students/{studentId}/courses"})
    @ResponseBody
    public List<Course> getStudentCourses(@PathVariable Long studentId) {
        return courseRepository.findAll();
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}", "students/{studentId}/courses/{courseId}"})
    @ResponseBody
    public Course getStudentCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        Course course = courseRepository.findOne(courseId);
        if (course == null) {
            throw new ResourceNotFoundException();
        }

        return course;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercises", "students/{studentId}/courses/{courseId}/exercises"})
    @ResponseBody
    public List<Exercise> getStudentCourseExercises(@PathVariable Long studentId, @PathVariable Long courseId) {
        return courseRepository.findOne(courseId).getExercises();
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}"})
    @ResponseBody
    public Exercise getExerciseAnswer(@PathVariable Long studentId, @PathVariable Long courseId, @PathVariable Long exerciseId) {
        Exercise exercise = exerciseRepository.findOne(exerciseId);
        if (exercise == null) {
            throw new ResourceNotFoundException();
        }

        return exercise;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshots", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots"})
    @ResponseBody
    public List<Snapshot> getSnapshots(@PathVariable Long studentId, @PathVariable Long courseId, @PathVariable Long exerciseId) {
        List<Snapshot> snapshots = exerciseAnswerRepository.findByStudentAndExercise(studentRepository.findOne(studentId), exerciseRepository.findOne(exerciseId)).getSnapshots();
        Collections.sort(snapshots);

        return snapshots;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}"})
    @ResponseBody
    public Snapshot getSnapshot(@PathVariable Long snapshotId) {
        Snapshot snapshot = snapshotRepository.findOne(snapshotId);
        if (snapshot == null) {
            throw new ResourceNotFoundException();
        }

        return snapshot;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/files", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/files"})
    @ResponseBody
    public List<SnapshotFile> getSnapshotFiles(@PathVariable Long snapshotId) {
        return snapshotRepository.findOne(snapshotId).getFiles();
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/file/{snapshotFileId}", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/files/{snapshotFileId}"})    
    @ResponseBody
    public SnapshotFile getSnapshotFile(@PathVariable Long snapshotFileId) {
        SnapshotFile snapshotFile = snapshotFileRepository.findOne(snapshotFileId);
        if (snapshotFile == null) {
            throw new ResourceNotFoundException();
        }
        
        return snapshotFile;
    }    
    
    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/file/{snapshotFileId}/content", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/files/{snapshotFileId}/content"}, produces = "text/plain")
    @ResponseBody
    public FileSystemResource getSnapshotFileContent(@PathVariable Long snapshotFileId) {
        SnapshotFile snapshotFile = snapshotFileRepository.findOne(snapshotFileId);
        if (snapshotFile == null) {
            throw new ResourceNotFoundException();
        }
        
        return new FileSystemResource(snapshotFile.getFilepath());
    }
}
