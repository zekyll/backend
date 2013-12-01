
package rage.codebrowser.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import rage.codebrowser.dto.Comment;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.Exercise;
import rage.codebrowser.dto.Snapshot;
import rage.codebrowser.dto.Student;
import rage.codebrowser.errors.ResourceNotFoundException;
import rage.codebrowser.repository.CommentRepository;

@Controller
public class CommentRESTController {


    @Autowired
    private CommentRepository commentRepository;

    @RequestMapping( value = "comments",params = { "page", "size" },method = RequestMethod.GET )
    @ResponseBody
    public Page<Comment> findPaginated(
     @RequestParam( "page" ) int page, @RequestParam( "size" ) int size){
       
       Pageable pageable = new PageRequest(page, size, Sort.Direction.DESC, "createdAt");
       Page< Comment > resultPage = commentRepository.findAll( pageable );
       
       return resultPage;
    }

//    @RequestMapping(value = {"comments"})
//    @ResponseBody
//    public List<Comment> getComments() {
//        return commentRepository.findAll(new Sort(Sort.Direction.DESC, "createdAt"));
//    }

    @RequestMapping(value = {"students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/comments"},
            params = { "page", "size" },method = RequestMethod.GET)
    @ResponseBody
    public Page<Comment> getExerciseComments(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise, @PathVariable("snapshotId") Snapshot snapshot,
                                                @RequestParam( "page" ) int page, @RequestParam( "size" ) int size) {

        Pageable pageable = new PageRequest(page, size, Sort.Direction.DESC, "createdAt");
        Page<Comment> comments = commentRepository.findByStudentAndCourseAndExerciseAndSnapshotOrSnapshotIsNull(student, course, exercise, snapshot, pageable);

        return comments;
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
            value = {"students/{studentId}/courses/{courseId}/exercises/{exerciseId}/comments/{commentId}"},
            method = RequestMethod.DELETE)
    @ResponseBody
    public Comment deleteExerciseComment(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise, @PathVariable("commentId") Comment comment) {
        return deleteComment(comment);
    }

    @RequestMapping(
            value = {"students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/comments/{commentId}"},
            method = RequestMethod.DELETE)
    @ResponseBody
    public Comment deleteSnapshotComment(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise, @PathVariable("snapshotId") Snapshot snapshot, @PathVariable("commentId") Comment comment) {
        return deleteComment(comment);
    }

    @RequestMapping(value = {"comments/{commentId}"}, method = RequestMethod.DELETE)
    @ResponseBody
    public Comment deleteComment(@PathVariable("commentId") Comment comment) {
        commentRepository.delete(comment);
        return comment;
    }


    @RequestMapping(
            value = {"students/{studentId}/courses/{courseId}/exercises/{exerciseId}/comments/{commentId}"},
            method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Comment updateExerciseComment(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise, @PathVariable("commentId") Comment comment, @RequestBody Comment updated) {
        return updateCommentText(comment, updated);
    }

   @RequestMapping(
            value = {"students/{studentId}/courses/{courseId}/exercises/{exerciseId}/snapshots/{snapshotId}/comments/{commentId}"},
            method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Comment updateSnapshotComment(@PathVariable("studentId") Student student, @PathVariable("courseId") Course course, @PathVariable("exerciseId") Exercise exercise, @PathVariable("snapshotId") Snapshot snapshot, @PathVariable("commentId") Comment comment, @RequestBody Comment updated) {
        return updateCommentText(comment, updated);
    }

    @RequestMapping(value = {"comments/{commentId}"}, method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Comment updateCommentText(@PathVariable("commentId") Comment oldComment, @RequestBody Comment updatedComment) {
        oldComment.setComment(updatedComment.getComment());
        return commentRepository.save(oldComment);
    }
}
