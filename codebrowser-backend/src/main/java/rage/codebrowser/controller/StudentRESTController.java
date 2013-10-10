package rage.codebrowser.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rage.codebrowser.codenalyzer.Concepts;
import rage.codebrowser.codenalyzer.SnapshotConcepts;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.CourseInfo;
import rage.codebrowser.dto.Exercise;
import rage.codebrowser.dto.ExerciseAnswer;
import rage.codebrowser.dto.Snapshot;
import rage.codebrowser.dto.SnapshotFile;
import rage.codebrowser.dto.Student;
import rage.codebrowser.dto.Tag;
import rage.codebrowser.dto.TagName;
import rage.codebrowser.repository.ExerciseAnswerRepository;
import rage.codebrowser.repository.StudentRepository;
import rage.codebrowser.repository.TagNameRepository;
import rage.codebrowser.repository.TagRepository;

@Controller
public class StudentRESTController {

    @Autowired
    private ExerciseAnswerRepository exerciseAnswerRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagNameRepository tagNameRepository;
    @Autowired
    private SnapshotConcepts snapshotConcepts;

    @RequestMapping(value = {"students"})
    @ResponseBody
    public List<Student> getStudents() {
        return studentRepository.findAll();
    }

    @RequestMapping(value = {"student/{studentId}", "students/{studentId}"})
    @ResponseBody
    public Student getStudent(@PathVariable("studentId") Student student) {
        return student;
    }

    @RequestMapping(value = {"student/{studentId}/courses", "students/{studentId}/courses"})
    @ResponseBody
    public List<Course> getStudentCourses(@PathVariable("studentId") Student student) {
        return student.getCourses();
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}", "students/{studentId}/courses/{courseId}"})
    @ResponseBody
    public Course getStudentCourse(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course) {
        List<Exercise> exercises = course.getExercises();

        Set<Exercise> existingExercises = new HashSet<Exercise>();
        List<ExerciseAnswer> answers = exerciseAnswerRepository.findByStudent(student);
        for (ExerciseAnswer exerciseAnswer : answers) {
            existingExercises.add(exerciseAnswer.getExercise());
        }

        exercises.retainAll(existingExercises);
        course.setExercises(exercises);
        return course;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercises", "students/{studentId}/courses/{courseId}/exercises"})
    @ResponseBody
    public List<Exercise> getStudentCourseExercises(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course) {
        return getStudentCourse(student, course).getExercises();
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}"})
    @ResponseBody
    public Exercise getExerciseAnswer(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise) {
        List<Exercise> exercises = getStudentCourseExercises(student, course);
        if (!exercises.contains(exercise)) {
            return null;
        }

        return exercise;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshots", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots"})
    @ResponseBody
    public List<Snapshot> getSnapshots(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise) {
        List<Exercise> exercises = getStudentCourseExercises(student, course);
        if (!exercises.contains(exercise)) {
            return null;
        }

        List<Snapshot> snapshots = exerciseAnswerRepository.findByStudentAndExercise(student, exercise).getSnapshots();
        Collections.sort(snapshots);
        for (Snapshot snapshot : snapshots) {
            snapshot.setExercise(exercise);
            snapshot.setCourse(new CourseInfo(course.getId(), course.getName()));
        }

        return snapshots;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/tags", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/tags"}, method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Tag> getTags(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise) {
        List<Tag> tags = tagRepository.findByStudentAndCourseAndExercise(student, course, exercise);
        if (tags == null) {
            tags = new ArrayList<Tag>();
        }

        Collections.sort(tags);
        return tags;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/tag",
        "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/tags"}, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Transactional
    public Tag postTag(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise, @RequestBody Tag tag) {
        return postTag(student, course, exercise, null, tag);
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/tag",
        "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/tags"}, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Transactional
    public Tag postTag(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise, @PathVariable("snapshotId") Snapshot snapshot, @RequestBody Tag tag) {
        tag.setCourse(course);
        tag.setStudent(student);
        tag.setExercise(exercise);
        tag.setSnapshot(snapshot);

        TagName tagName = tagNameRepository.findByName(tag.getTagName().getName());
        if (tagName == null) {
            tagName = tagNameRepository.saveAndFlush(tag.getTagName());
        }

        tag.setTagName(tagName);
        tagName.addTag(tag);

        return tagRepository.saveAndFlush(tag);
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/tag/{tagId}",
        "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/tags/{tagId}"}, method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Tag getTag(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise, @PathVariable("tagId") Long tagId) {
        return tagRepository.findOne(tagId);
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/tag/{tagId}",
        "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/tags/{tagId}",
        "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/tags/{tagId}"},
            method = RequestMethod.DELETE)
    @ResponseBody
    @Transactional
    public Tag deleteTag(@PathVariable("tagId") Tag tag) {
        TagName tagName = tag.getTagName();
        tagName.getTags().remove(tag);
        tagRepository.delete(tag);
        if (tagName.getTags().isEmpty()) {
            tagNameRepository.delete(tagName);
        } else {
            tagNameRepository.save(tagName);
        }
        return tag;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}"})
    @ResponseBody
    public Snapshot getSnapshot(@PathVariable("snapshotId") Snapshot snapshot, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise) {
        Collections.sort(snapshot.getFiles(), new Comparator<SnapshotFile>() {
            @Override
            public int compare(SnapshotFile o1, SnapshotFile o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        snapshot.setExercise(exercise);
        snapshot.setCourse(new CourseInfo(course.getId(), course.getName()));
        return snapshot;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/files", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/files"})
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

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/file/{snapshotFileId}", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/files/{snapshotFileId}"})
    @ResponseBody
    public SnapshotFile getSnapshotFile(@PathVariable("snapshotFileId") SnapshotFile snapshotFile) {
        return snapshotFile;
    }

    @RequestMapping(value = {"student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/file/{snapshotFileId}/content", "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/files/{snapshotFileId}/content"}, produces = "text/plain")
    @ResponseBody
    public FileSystemResource getSnapshotFileContent(@PathVariable("snapshotFileId") SnapshotFile snapshotFile) {
        return new FileSystemResource(snapshotFile.getFilepath());
    }
    
        
    @RequestMapping(value = {
        "student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/file/{snapshotFileId}/concepts",
        "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/files/{snapshotFileId}/concepts"
    })
    @ResponseBody
    public Concepts getSnapshotFileConcepts(@PathVariable("snapshotFileId") SnapshotFile snapshotFile) {
        
        return snapshotConcepts.getConcepts(snapshotFile);
    }
        
    @RequestMapping(value = {
        "student/{studentId}/course/{courseId}/exercise/{exerciseId}/snapshot/{snapshotId}/concepts",
        "students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/concepts"
    })
    @ResponseBody
    public Concepts getSnapshotConcepts(@PathVariable("snapshotId") Snapshot snapshot) {
        return snapshotConcepts.getConcepts(snapshot.getFiles());
    }
}
