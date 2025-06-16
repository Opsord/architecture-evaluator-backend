package io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.repositories;

import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.entities.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findBySubject(String subject);
    List<Exam> findByExamDateBetween(LocalDate startDate, LocalDate endDate);
    List<Exam> findByProfessorId(Long professorId);
}