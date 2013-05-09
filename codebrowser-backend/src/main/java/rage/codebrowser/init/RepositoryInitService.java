package rage.codebrowser.init;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.Exercise;
import rage.codebrowser.dto.ExerciseAnswer;
import rage.codebrowser.dto.Snapshot;
import rage.codebrowser.dto.SnapshotFile;
import rage.codebrowser.dto.Student;
import rage.codebrowser.repository.CourseRepository;
import rage.codebrowser.repository.ExerciseAnswerRepository;
import rage.codebrowser.repository.ExerciseRepository;
import rage.codebrowser.repository.SnapshotFileRepository;
import rage.codebrowser.repository.SnapshotRepository;
import rage.codebrowser.repository.StudentRepository;

@Component
public class RepositoryInitService {

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
    private ExerciseAnswerRepository eaRepository;

    // currently loading exactly one student for frontend implementation
    // testing
    @PostConstruct
    @Transactional
    public void init() {
        for (String dataPath : Config.POSSIBLE_DATA_PATHS) {
            File dataLocation = new File(dataPath);
            if (dataLocation.exists() && dataLocation.isDirectory() && dataLocation.canRead()) {
                Config.DATA_PATH = dataPath;
                break;
            }
        }


        Student s = new Student();
        s.setName("El Barto");
        s = studentRepository.save(s);

        Course c = new Course();
        c.setName("oh-pe");
        c = courseRepository.save(c);

        Exercise e = new Exercise();
        e.setName("Arvosana");
        e = exerciseRepository.save(e);

        c.getExercises().add(e);
        e.setCourse(c);

        s.getCourses().add(c);
        c.getStudents().add(s);

        ExerciseAnswer ea = new ExerciseAnswer();
        ea.setExercise(e);
        ea.setStudent(s);
        ea = eaRepository.save(ea);


        SimpleDateFormat snapshotDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSSSSS");
        for (String location : Config.LOCATIONS.split(",")) {
            location = location.trim();
            Snapshot ss = new Snapshot();
            try {
                ss.setSnapshotTime(snapshotDateFormat.parse(location));
            } catch (ParseException ex) {
                Logger.getLogger(RepositoryInitService.class.getName()).log(Level.SEVERE, null, ex);
            }

            ss.setName(location);
            ss.setType("EVENT");
            ss.setExerciseAnswer(ea);

            ss = snapshotRepository.save(ss);
            ea.getSnapshots().add(ss);

            File fileLocation = new File(Config.DATA_PATH + "/" + location + "/");
            List<File> javaFiles = listJavaFiles(fileLocation);

            for (File file : javaFiles) {
                String path = file.getAbsolutePath();

                SnapshotFile sf = new SnapshotFile();
                sf.setName(file.getName());
                sf.setFilepath(path);

                sf = snapshotFileRepository.save(sf);

                ss.getFiles().add(sf);

            }

            ss = snapshotRepository.save(ss);
        }


        s = studentRepository.save(s);
        c = courseRepository.save(c);
        e = exerciseRepository.save(e);
        ea = eaRepository.save(ea);
    }

    private List<File> listJavaFiles(File fromDir) {
        List<File> files = new ArrayList<File>();
        addFiles(fromDir, files);
        return files;
    }

    private void addFiles(File fromDir, List<File> toList) {
        if (fromDir == null || !fromDir.isDirectory()) {
            return;
        }

        for (File file : fromDir.listFiles()) {
            if (file.getName().endsWith(".java")) {
                toList.add(file);
                continue;
            }

            if (file.isDirectory()) {
                addFiles(file, toList);
            }
        }
    }
}
