package rage.codebrowser.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.Exercise;
import rage.codebrowser.dto.Snapshot;
import rage.codebrowser.dto.Student;
import rage.codebrowser.repository.CourseRepository;
import rage.codebrowser.repository.ExerciseAnswerRepository;
import rage.codebrowser.repository.ExerciseRepository;
import rage.codebrowser.repository.SnapshotFileRepository;
import rage.codebrowser.repository.SnapshotRepository;
import rage.codebrowser.repository.StudentRepository;

@Controller
public class CourseRESTController {

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

    @RequestMapping(value = {"courses"})
    @ResponseBody
    public List<Course> getCourses() {
        return courseRepository.findAll();
    }

    @RequestMapping(value = {"courses/{courseId}", "course/{courseId}"})
    @ResponseBody
    public Course getCourse(@PathVariable Long courseId) {
        return courseRepository.findOne(courseId);
    }

    @RequestMapping(value = {"courses/{courseId}/exercises", "course/{courseId}/exercises"})
    @ResponseBody
    public List<Exercise> getCourseExercises(@PathVariable Long courseId) {
        return courseRepository.findOne(courseId).getExercises();
    }

    @RequestMapping(value = {"courses/{courseId}/exercises/{exerciseId}", "course/{courseId}/exercise/{exerciseId}"})
    @ResponseBody
    public Exercise getCourseExercise(@PathVariable Long courseId, @PathVariable Long exerciseId) {
        return exerciseRepository.findOne(exerciseId);
    }

    @RequestMapping(value = {"courses/{courseId}/students", "course/{courseId}/students"})
    @ResponseBody
    public List<Student> getCourseStudents(@PathVariable Long courseId) {
        return courseRepository.findOne(courseId).getStudents();
    }

    @RequestMapping(value = {"courses/{courseId}/students/{studentId}", "course/{courseId}/students/{studentId}"})
    @ResponseBody
    public Student getCourseStudent(@PathVariable Long courseId, @PathVariable Long studentId) {
        return studentRepository.findOne(studentId);
    }

    @RequestMapping(value = {"course/{courseId}/student/{studentId}/exercise/{exerciseId}/snapshots", "courses/{courseId}/students/{studentId}/exercises/{exerciseId}/snapshots"})
    @ResponseBody
    public List<Snapshot> getStudentCourseSnapshots(@PathVariable Long studentId, @PathVariable Long courseId, @PathVariable Long exerciseId) {
        return exerciseAnswerRepository.findByStudentAndExercise(studentRepository.findOne(studentId), exerciseRepository.findOne(exerciseId)).getSnapshots();
    }
}
