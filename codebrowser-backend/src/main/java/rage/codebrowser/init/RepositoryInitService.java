package rage.codebrowser.init;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
//        for (String dataPath : Config.POSSIBLE_DATA_PATHS) {
//            File dataLocation = new File(dataPath);
//            if (dataLocation.exists() && dataLocation.isDirectory() && dataLocation.canRead()) {
//                Config.DATA_PATH = dataPath;
//                break;
//            }
//        }

        readInExercises("ohpe", 2, "/home/group/rage/MOOCDATA/s2012-ohpe/events-decompressed/", "Tietokanta", "Lyyrakortti");
        readInExercises("mooc-ohja", 2, "/home/group/rage/MOOCDATA/k2013-mooc/events-decompressed/", "Matopeli", "Numerotiedustelu", "Sanakirja");
    }

    private void readInExercises(String courseName, int maxStudentsToConsider, String dataPath, String... exercisesToAccept) {
        List<String> exerciseList = Arrays.asList(exercisesToAccept);

        Course c = new Course();
        c.setName(courseName);
        c = courseRepository.save(c);

        int studentCount = 0;

        File[] studentDirs = new File(dataPath).listFiles();
        Arrays.sort(studentDirs, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });


        for (File studentDir : studentDirs) {
            if (studentCount >= maxStudentsToConsider) {
                break;
            }
            
            if (!studentDir.isDirectory()) {
                continue;
            }

            // find exercises that this student has worked on 
            // 1. snapshots
            File[] studentSnapshotDirs = studentDir.listFiles();
            if (studentSnapshotDirs.length < 100) {
                continue;
            }

            Arrays.sort(studentSnapshotDirs, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            int okSnapshots = 0;
            // 2. exercises
            Map<String, List<File>> snapshotDirs = new TreeMap<String, List<File>>();
            for (File studentsExerciseDir : studentSnapshotDirs) {
                if (!studentsExerciseDir.isDirectory()) {
                    continue;
                }

                boolean found = false;
                String exerciseName = getExerciseName(studentsExerciseDir);
                for (String acceptableExercises : exerciseList) {
                    if (!exerciseName.contains(acceptableExercises)) {
                        continue;
                    }

                    found = true;
                    break;
                }

                if (!found) {
                    continue;
                }

                if (!snapshotDirs.containsKey(exerciseName)) {
                    snapshotDirs.put(exerciseName, new ArrayList<File>());
                }

                snapshotDirs.get(exerciseName).add(studentsExerciseDir);
                okSnapshots++;
            }

            if (snapshotDirs.size() < exercisesToAccept.length - 1 || okSnapshots < 50) {
                continue;
            }

            // snapshots for this student are accepted, increment 
            // student counter
            studentCount++;
            
            String studentName = "student_" + studentDir.getName();
            Student s = studentRepository.findByName(studentName);
            if (s == null) {
                s = new Student();
                s.setName(studentName);
                s = studentRepository.save(s);
            }

            if (!c.getStudents().contains(s)) {
                c.getStudents().add(s);
                c = courseRepository.save(c);
            }

            if (!s.getCourses().contains(c)) {
                s.getCourses().add(c);
                s = studentRepository.save(s);
            }

            SimpleDateFormat snapshotDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSSSSS");
            for (String exerciseName : snapshotDirs.keySet()) {

                Exercise e = exerciseRepository.findByName(exerciseName);
                if (e == null) {
                    e = new Exercise();
                    e.setName(exerciseName);
                    e = exerciseRepository.save(e);
                }

                if (e.getCourse() == null || !e.getCourse().equals(c)) {
                    e.setCourse(c);
                    e = exerciseRepository.save(e);
                }

                if (c.getExercises() == null || !c.getExercises().contains(e)) {
                    c.addExercise(e);
                    c = courseRepository.save(c);
                }

                for (File studentsExerciseDir : snapshotDirs.get(exerciseName)) {
                    List<File> javaFiles = listJavaFiles(studentsExerciseDir);
                    if (javaFiles.isEmpty()) {
                        continue;
                    }

                    String location = studentsExerciseDir.getName();
                    location = location.trim();
                    Snapshot ss = new Snapshot();
                    try {
                        ss.setSnapshotTime(snapshotDateFormat.parse(location));
                    } catch (ParseException ex) {
                        Logger.getLogger(RepositoryInitService.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    }

                    ExerciseAnswer ea = eaRepository.findByStudentAndExercise(s, e);
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
                    ea = eaRepository.save(ea);

                    if (javaFiles.size() > 1) {
                        System.out.println("*****************************");
                        System.out.println("More than 1 file at " + studentsExerciseDir.getAbsolutePath());
                        System.out.println("*****************************");
                    }

                    Collections.sort(javaFiles, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });

                    for (File file : javaFiles) {
                        String path = file.getAbsolutePath();
                        SnapshotFile sf = new SnapshotFile();

                        String filename = getFilenameWithPackage(studentsExerciseDir.getAbsolutePath(), file.getAbsolutePath());

                        sf.setName(filename);
                        sf.setFilepath(path);

                        sf = snapshotFileRepository.save(sf);

                        ss.getFiles().add(sf);
                    }

                    ss = snapshotRepository.save(ss);
                }

            }
        }
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

    private static String getFilenameWithPackage(String exerciseFolder, String path) {
        try {
            path = path.substring(path.indexOf(exerciseFolder) + exerciseFolder.length());
        } catch (Exception e) {
        }
        try {
            path = path.substring(path.indexOf("/src/") + "/src/".length());
        } catch (Exception e) {
        }

        return path;
    }
}
