package io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.services;

import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.entities.Exam;
import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.entities.ExamResult;
import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.entities.Student;
import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.repositories.ExamRepository;
import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.repositories.ExamResultRepository;
import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExamResultService {
    private final ExamResultRepository examResultRepository;
    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public ExamResultService(
            ExamResultRepository examResultRepository,
            ExamRepository examRepository,
            StudentRepository studentRepository) {
        this.examResultRepository = examResultRepository;
        this.examRepository = examRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<ExamResult> findAllResults() {
        return examResultRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ExamResult> findResultById(Long id) {
        return examResultRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ExamResult> findResultsByExamId(Long examId) {
        return examResultRepository.findByExamId(examId);
    }

    @Transactional(readOnly = true)
    public List<ExamResult> findResultsByStudentId(Long studentId) {
        return examResultRepository.findByStudentId(studentId);
    }

    @Transactional
    public ExamResult submitExamResult(Long examId, Long studentId, Double score) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        ExamResult result = new ExamResult();
        result.setExam(exam);
        result.setStudent(student);
        result.setScore(score);
        result.setSubmissionDate(LocalDateTime.now());
        result.setStatus(ExamResult.ExamStatus.SUBMITTED);

        return examResultRepository.save(result);
    }

    @Transactional
    public ExamResult gradeExamResult(Long resultId, Double score, String comments) {
        ExamResult result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Exam result not found"));

        result.setScore(score);
        result.setGradingComments(comments);
        result.setStatus(ExamResult.ExamStatus.GRADED);

        return examResultRepository.save(result);
    }
}