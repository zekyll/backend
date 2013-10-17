package rage.codebrowser.init;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
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
import rage.codebrowser.dto.Testresult;
import rage.codebrowser.repository.CourseRepository;
import rage.codebrowser.repository.ExerciseAnswerRepository;
import rage.codebrowser.repository.ExerciseRepository;
import rage.codebrowser.repository.SnapshotFileRepository;
import rage.codebrowser.repository.SnapshotRepository;
import rage.codebrowser.repository.StudentRepository;
import rage.codebrowser.repository.TestRepository;

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
    @Autowired
    private TestRepository testRepository;

    private static class FileComparator implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

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

//        readInExercises("k2013-ohpe", 100, "/home/group/rage/MOOCDATA/k2013-ohpe/events-decompressed/", "Karkausvuosi" , "Tietokanta", "JoukkueetJaPelaajat", "SilmukatLopetusMuistaminen", "suurempi_luku", "SuurempiLuku", "Lyyrakortti");
//        readInExercises("ohpe", 2, "/home/group/rage/MOOCDATA/s2012-ohpe/events-decompressed/", "Tietokanta", "Lyyrakortti");
//        readInExercises("mooc-ohja", 2, "/home/group/rage/MOOCDATA/k2013-mooc/events-decompressed/", "Matopeli", "Numerotiedustelu", "Sanakirja");


        readInExercises("mooc-en", 10, "/home/group/codebro/data/mooc-en/events-decompressed/", "Birdwatcher", "Divider", "EvenNumbers","LoopsEndingRemembering", "PrintingOutText", "PrintingLikeboss");
        readInExercises("mooc-fi", 2, "/home/group/codebro/data/mooc-en/events-decompressed/", "Birdwatcher", "Divider", "EvenNumbers", "LoopsEndingRemembering");
        System.out.println("**************** DONE");
    }

    private void readInExercises(String courseName, int maxStudentsToConsider, String dataPath, String... exercisesToAccept) {
        Course course = new Course();
        course.setName(courseName);
        course = courseRepository.save(course);

        int studentCount = 0;

        File[] studentDirs = new File(dataPath).listFiles();
        Arrays.sort(studentDirs, new FileComparator());

        for (File studentDir : studentDirs) {
            if (studentCount >= maxStudentsToConsider) {
                break;
            }

            if (readStudentDirectory(course, studentDir, Arrays.asList(exercisesToAccept))) {
                studentCount++;
            }
        }
    }

    private boolean readStudentDirectory(Course course, File studentDir, List<String> exerciseList) {

        if (!studentDir.isDirectory()) {
            return false;
        }

        // find exercises that this student has worked on
        // 1. snapshots
        File[] studentSnapshotDirs = studentDir.listFiles();
        if (studentSnapshotDirs.length < 100) {
            return false;
        }

        Arrays.sort(studentSnapshotDirs, new FileComparator());

        Map<String, List<File>> snapshotDirs = getAcceptedSnapshotDirs(studentSnapshotDirs, exerciseList);

        // 2. exercises
        int acceptedSnapshotCount = 0;
        for (List<File> exerciseSnapshots : snapshotDirs.values()) {
            acceptedSnapshotCount += exerciseSnapshots.size();
        }

        if (snapshotDirs.size() < exerciseList.size() - 1 || acceptedSnapshotCount < 50) {
            return false;
        }

        String studentName = "student_" + studentDir.getName();
        Student student = studentRepository.findByName(studentName);
        if (student == null) {
            student = new Student();
            student.setName(studentName);
            student = studentRepository.save(student);
        }

        if (!course.getStudents().contains(student)) {
            course.getStudents().add(student);
            course = courseRepository.save(course);
        }

        if (!student.getCourses().contains(course)) {
            student.getCourses().add(course);
            student = studentRepository.save(student);
        }

        for (String exerciseName : snapshotDirs.keySet()) {
            readSnapshotsForExercise(course, student, exerciseName, snapshotDirs);
        }

        return true;
    }

    private Map<String, List<File>> getAcceptedSnapshotDirs(File[] studentSnapshotDirs, List<String> exerciseList) {
        Map<String, List<File>> snapshotDirs = new TreeMap<String, List<File>>();
        for (File snapshotDir : studentSnapshotDirs) {
            if (!snapshotDir.isDirectory()) {
                continue;
            }

            boolean found = false;
            String exerciseName = getExerciseName(snapshotDir);
            for (String acceptableExercise : exerciseList) {
                if (!exerciseName.contains(acceptableExercise)) {
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

            snapshotDirs.get(exerciseName).add(snapshotDir);
        }

        return snapshotDirs;
    }

    private void readSnapshotsForExercise(Course course, Student student, String exerciseName, Map<String, List<File>> snapshotDirs) {
        Exercise exercise = exerciseRepository.findByCourseAndName(course, exerciseName);
        if (exercise == null) {
            exercise = new Exercise();
            exercise.setName(exerciseName);
            exercise = exerciseRepository.save(exercise);
        }

        if (exercise.getCourse() == null || !exercise.getCourse().equals(course)) {
            exercise.setCourse(course);
            exercise = exerciseRepository.save(exercise);
        }

        if (course.getExercises() == null || !course.getExercises().contains(exercise)) {
            course.addExercise(exercise);
            course = courseRepository.save(course);
        }

        for (File snapshotDir : snapshotDirs.get(exerciseName)) {
            readSnapshot(student, exercise, snapshotDir);
        }
    }
    /**
     * Creates dummy test data for snapshot.
     * 50% of snapshots will not have tests, others will have 2 tests.
     * @return 
     */
    private List<Testresult> createDummyTests(){
        List<Testresult> tests = new ArrayList<Testresult>();
        int x=(Math.random()<0.5)?1:0;
        if (x == 1) {
            Testresult test = new Testresult();
            test.setName("test1");
            test.setMessage("Test1 output");
            test.setPassed((Math.random()<0.5)?true:false);
            test = testRepository.save(test);
            tests.add(test);
            Testresult test2 = new Testresult();
            test2.setName("test2");
            test2.setMessage("Test2 output");
            test2.setPassed((Math.random()<0.5)?true:false);
            test2 = testRepository.save(test2); 
            tests.add(test2);
            test = new Testresult();
            test.setName("test3");
            test.setMessage("Test3 output");
            test.setPassed((Math.random()<0.5)?true:false);
            test = testRepository.save(test);
            tests.add(test);
        }
        
        return tests;
    }

    private void readSnapshot(Student student, Exercise exercise, File snapshotDir) {
        List<File> javaFiles = listJavaFiles(snapshotDir);
        if (javaFiles.isEmpty()) {
            return;
        }

        String location = snapshotDir.getName();
        location = location.trim();
        Snapshot ss = new Snapshot();
        
        //If there are more class files than java files, the code compiles
        int amountOfClassFiles = countClassFiles(snapshotDir).length;
        if(javaFiles.size() <= amountOfClassFiles) {
            ss.setCompiles(true);
        }
        else {
            ss.setCompiles(false);
        }
        
        //Create dummy test data for snapshot
        ss.setTests(createDummyTests());
        
        try {
            ss.setSnapshotTime(Config.SNAPSHOT_DATE_FORMAT.parse(location));
        } catch (ParseException ex) {
            Logger.getLogger(RepositoryInitService.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        ExerciseAnswer ea = eaRepository.findByStudentAndExercise(student, exercise);
        if (ea == null) {
            ea = new ExerciseAnswer();
            ea.setExercise(exercise);
            ea.setStudent(student);
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
            System.out.println("More than 1 file at " + snapshotDir.getAbsolutePath());
            System.out.println("*****************************");
        }

        Collections.sort(javaFiles, new FileComparator());

        for (File file : javaFiles) {
            String path = file.getAbsolutePath();
            SnapshotFile sf = new SnapshotFile();

            String filename = getFilenameWithPackage(snapshotDir.getAbsolutePath(), file.getAbsolutePath());
            Long filesize = file.length();

            sf.setName(filename);
            sf.setFilepath(path);
            sf.setFilesize(filesize);

            sf = snapshotFileRepository.save(sf);

            ss.getFiles().add(sf);
        }

        ss = snapshotRepository.save(ss);
    }
    
    /**
     * 
     * @param dir root directory of a snapshot, contains .class-files
     * @return a File-array of .class-files. The existence of .class files is used
     *         to determine whether or not the code compiles. 
     */
    public File[] countClassFiles(File dir){

        return dir.listFiles(new FilenameFilter() { 
                 public boolean accept(File dir, String filename)
                      { return filename.endsWith(".class"); }
        } );

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

    private String getExerciseName(File snapshotDir) {
        for (File file : snapshotDir.listFiles()) {
            String filename = file.getName();
            if (filename.toLowerCase().contains("viikko") || filename.toLowerCase().contains("week")) {
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
