
package rage.codebrowser.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import rage.codebrowser.dto.Comment;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.Exercise;
import rage.codebrowser.dto.Student;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    public List<Comment> findByStudentAndCourseAndExercise(Student student, Course course, Exercise exercise);
}