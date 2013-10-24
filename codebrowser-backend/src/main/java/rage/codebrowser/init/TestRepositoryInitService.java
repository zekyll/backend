package rage.codebrowser.init;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.Student;
import rage.codebrowser.repository.CourseRepository;
import rage.codebrowser.repository.ExerciseAnswerRepository;
import rage.codebrowser.repository.ExerciseRepository;
import rage.codebrowser.repository.SnapshotFileRepository;
import rage.codebrowser.repository.SnapshotRepository;
import rage.codebrowser.repository.StudentRepository;
import rage.codebrowser.repository.TestRepository;




public class TestRepositoryInitService implements RepositoryInitService {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ExerciseRepository exerciseRepository;
    @Autowired
    private SnapshotRepository snapshotRepository;
    @Autowired
    private SnapshotFileRepository snapshotFileRepository;
    @Autowired
    private ExerciseAnswerRepository exerciseAnswerRepository;
    @Autowired
    private TestRepository testRepository;


    @Override
    public void initRepository() {
        seedTestData();
        System.out.println("**************** TEST REPOSITORY INIT DONE");
    }

    private void seedTestData() {
        createCourses();
        createStudents();
    }

    private void createCourses() {
        String[] courses = new String[]{"unit-test-course", "integration-test-course"};
        for (String name : courses) {
            createCourse(name);
        }
    }

    private void createCourse(String name) {
        Course course = new Course();
        course.setName(name);
        courseRepository.save(course);
    }

    private void createStudents() {
        int courseIndex = 1;
        for( Course c : courseRepository.findAll() ) {
            for (int i = 1; i <= courseIndex * 10 ; i++) {

                Student s = new Student();
                s.setName(c.getName() + "_student_" + String.format("%04d", i));
                s = studentRepository.save(s);

                c.getStudents().add(s);
                c = courseRepository.save(c);

                s.getCourses().add(c);
                s = studentRepository.save(s);
            }
            courseIndex++;
        }
    }


}
