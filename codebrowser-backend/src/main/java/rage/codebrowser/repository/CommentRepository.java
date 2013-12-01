
package rage.codebrowser.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rage.codebrowser.dto.Comment;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.Exercise;
import rage.codebrowser.dto.Snapshot;
import rage.codebrowser.dto.Student;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    @Query("SELECT u from Comment u where (u.student = ?1 and u.course = ?2 and u.exercise = ?3 and u.snapshot = ?4) or (u.student = ?1 and u.course = ?2 and u.exercise = ?3 and u.snapshot = NULL)")
    Page<Comment> findByStudentAndCourseAndExerciseAndSnapshotOrSnapshotIsNull(Student student, Course course, Exercise exercise, Snapshot snapshot, Pageable pageable);
    Page<Comment> findAll(Pageable pageable);
    
}