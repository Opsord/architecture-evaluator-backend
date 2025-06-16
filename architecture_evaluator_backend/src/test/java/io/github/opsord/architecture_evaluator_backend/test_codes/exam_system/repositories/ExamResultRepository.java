package io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.repositories;

import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.entities.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    List<ExamResult> findByExamId(Long examId);
    List<ExamResult> findByStudentId(Long studentId);

    @Query("SELECT r FROM ExamResult r WHERE r.student.id = ?1 AND r.exam.subject = ?2")
    List<ExamResult> findByStudentIdAndExamSubject(Long studentId, String subject);

    @Query("SELECT AVG(r.score) FROM ExamResult r WHERE r.exam.id = ?1")
    Double calculateAverageScoreForExam(Long examId);
}