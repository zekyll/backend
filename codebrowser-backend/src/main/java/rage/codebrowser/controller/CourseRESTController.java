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

@Controller
public class CourseRESTController {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ExerciseAnswerRepository exerciseAnswerRepository;

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
        return exerciseAnswerRepository.findByStudentAndExercise(student, exercise).getSnapshots();
    }
}
