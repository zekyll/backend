package rage.codebrowser.init;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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


        Course c = new Course();
        c.setName("ohpe");
        c = courseRepository.save(c);

        int studentCount = 0;
        for (File studentDir : new File(Config.DATA_PATH).listFiles()) {
            if (!studentDir.isDirectory()) {
                continue;
            }

            studentCount++;
            if (studentCount > 4) {
                break;
            }


            Exercise e = null;
            ExerciseAnswer ea = null;
            Student s = new Student();
            s.setName("student_" + studentDir.getName());
            s = studentRepository.save(s);


            c.getStudents().add(s);
            s.getCourses().add(c);

            SimpleDateFormat snapshotDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSSSSS");
            int count = 0;

            File[] snapshotDirs = studentDir.listFiles();
            Arrays.sort(snapshotDirs);

            for (File studentsExerciseDir : snapshotDirs) {
                if (!studentsExerciseDir.isDirectory()) {
                    continue;
                }

                count++;
                if (count > 2000) {
                    break;
                }

                String location = studentsExerciseDir.getName();
                location = location.trim();
                Snapshot ss = new Snapshot();
                try {
                    ss.setSnapshotTime(snapshotDateFormat.parse(location));
                } catch (ParseException ex) {
                    Logger.getLogger(RepositoryInitService.class.getName()).log(Level.SEVERE, null, ex);
                }

                String exerciseName = getExerciseName(studentsExerciseDir);
                if (!exerciseName.contains("Tietokanta") && !exerciseName.contains("Lyyrakortti")) {
                    continue;
                }

                e = exerciseRepository.findByName(exerciseName);
                if (e == null) {
                    e = new Exercise();
                    e.setName(exerciseName);

                    e = exerciseRepository.save(e);

                    c.addExercise(e);
                    e.setCourse(c);

                    e = exerciseRepository.save(e);
                    c = courseRepository.save(c);
                } else {
                    c.addExercise(e);
                    e.setCourse(c);
                }

                ea = eaRepository.findByStudentAndExercise(s, e);
                if (ea == null) {
                    ea = new ExerciseAnswer();
                    ea.setExercise(e);
                    ea.setStudent(s);
                    ea = eaRepository.save(ea);
                }

                ss.setName(location);
                ss.setType("EVENT");
                ss.setExerciseAnswer(ea);

                ss = snapshotRepository.save(ss);
                ea.addSnapshot(ss);


                File fileLocation = studentsExerciseDir;
                List<File> javaFiles = listJavaFiles(fileLocation);
                if (javaFiles.size() > 1) {
                    System.out.println("*****************************");
                    System.out.println("*****************************");
                    System.out.println("*****************************");
                    System.out.println("More than 1 file at " + fileLocation.getAbsolutePath());
                    System.out.println("*****************************");
                    System.out.println("*****************************");
                    System.out.println("*****************************");
                }
                
                Collections.sort(javaFiles);

                for (File file : javaFiles) {
                    String path = file.getAbsolutePath();

                    SnapshotFile sf = new SnapshotFile();
                    sf.setName(file.getName());
                    sf.setFilepath(path);

                    sf = snapshotFileRepository.save(sf);

                    ss.getFiles().add(sf);
                }

                ea = eaRepository.save(ea);
                ss = snapshotRepository.save(ss);
            }


            s = studentRepository.save(s);
            if (e != null) {
                e = exerciseRepository.save(e);
            }
            if (ea != null) {
                ea = eaRepository.save(ea);
            }
        }
        
        c = courseRepository.save(c);

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
            if (file.isDirectory()) {
                addFiles(file, toList);
                continue;
            }

            if (file.getName().endsWith(".java")) {
                toList.add(file);
                continue;
            }
        }
    }

    private String getExerciseName(File location) {
        for (File file : location.listFiles()) {
            String filename = file.getName();
            if (filename.toLowerCase().contains("viikko")) {
                return filename.substring(filename.indexOf("-") + 1);
            }
        }

        return "NA";
    }
}
