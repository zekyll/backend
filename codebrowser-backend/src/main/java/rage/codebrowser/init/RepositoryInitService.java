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
        
        readInExercises("ohpe", 4, "/home/group/rage/MOOCDATA/s2012-ohpe/events-decompressed/", "Tietokanta", "Lyyrakortti");
        readInExercises("ohja", 4, "/home/group/rage/MOOCDATA/s2012-ohja/events-decompressed/", "Matopeli", "Numerotiedustelu", "Sanakirja");      
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
            if (!studentDir.isDirectory()) {
                continue;
            }
            
            // find exercises that this student has worked on 
            // 1. snapshots
            File[] studentSnapshotDirs = studentDir.listFiles();
            if(studentSnapshotDirs.length < 100) {
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
                
                String exerciseName = getExerciseName(studentsExerciseDir);
                for (String acceptableExercises : exerciseList) {
                    if(!exerciseName.contains(acceptableExercises)) {
                        continue;
                    }
                }
                
                if(!snapshotDirs.containsKey(exerciseName)) {
                    snapshotDirs.put(exerciseName, new ArrayList<File>());
                }
                
                snapshotDirs.get(exerciseName).add(studentsExerciseDir);
                okSnapshots++;
            }
            
            if(snapshotDirs.size() < exercisesToAccept.length - 1 || okSnapshots < 50) {
                continue;
            }
            
            studentCount++;
            if (studentCount > maxStudentsToConsider) {
                break;
            }

            SimpleDateFormat snapshotDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSSSSS");
            for (String exerciseName : snapshotDirs.keySet()) {
                
                Exercise e = null;
                ExerciseAnswer ea = null;
                Student s = new Student();
                s.setName("student_" + studentDir.getName());
                s = studentRepository.save(s);

                c.getStudents().add(s);
                s.getCourses().add(c);
                
                
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
                        if (!c.getExercises().contains(e)) {
                            c.addExercise(e);
                        }
                        
                        if (!e.getCourse().equals(c)) {
                            e.setCourse(c);
                        }
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
