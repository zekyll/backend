
package rage.codebrowser.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rage.codebrowser.dto.Comment;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.Exercise;
import rage.codebrowser.dto.Snapshot;
import rage.codebrowser.dto.Student;
import rage.codebrowser.repository.CommentRepository;

@Controller
public class CommentRESTController {


    @Autowired
    private CommentRepository commentRepository;



    @RequestMapping(value = {"comments"})
    @ResponseBody
    public List<Comment> getComments() {
        return commentRepository.findAll(new Sort(Sort.Direction.DESC, "createdAt"));
    }


    @RequestMapping(
            value = {"students/{studentId}/courses/{courseId}/exercises/{exerciseId}/comments"},
            method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Transactional
    public Comment postExerciseComment(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise, @RequestBody Comment comment) {
        return postSnapshotComment(student, course, exercise, null, comment);
    }


    @RequestMapping(
            value = {"students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/comments"},
            method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Transactional
    public Comment postSnapshotComment(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise, @PathVariable("snapshotId") Snapshot snapshot, @RequestBody Comment comment) {
        comment.setCourse(course);
        comment.setStudent(student);
        comment.setExercise(exercise);
        comment.setSnapshot(snapshot);

        return commentRepository.saveAndFlush(comment);
    }


    @RequestMapping(
            value = {"students/{studentId}/courses/{courseId}/exercises/{exerciseId}/comments"},
            method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Comment> getExerciseComments(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise) {

        List<Comment> comments = commentRepository.findByStudentAndCourseAndExercise(student, course, exercise);

        if (comments == null) {
            comments = new ArrayList<Comment>();
        }

        return comments;
    }
}
