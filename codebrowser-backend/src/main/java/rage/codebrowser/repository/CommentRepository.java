package rage.codebrowser.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rage.codebrowser.dto.Comment;
import rage.codebrowser.dto.Course;
import rage.codebrowser.dto.Exercise;
import rage.codebrowser.dto.Snapshot;
import rage.codebrowser.dto.Student;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT u from Comment u where (u.student = ?1 and u.course = ?2 and u.exercise = ?3 and u.snapshot = ?4) or (u.student = ?1 and u.course = ?2 and u.exercise = ?3 and u.snapshot = NULL)")
    Page<Comment> findByStudentAndCourseAndExerciseAndSnapshotOrSnapshotIsNull(Student student, Course course, Exercise exercise, Snapshot snapshot, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.comment) LIKE %:searchString% OR LOWER(c.exercise.name) LIKE %:searchString% OR LOWER(c.course.name) LIKE %:searchString% OR LOWER(c.student.name) LIKE %:searchString% OR LOWER(c.username) LIKE %:searchString%")
    Page<Comment> findByCommentContaining(@Param("searchString") String searchString, Pageable pageable);
}