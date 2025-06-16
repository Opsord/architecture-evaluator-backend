package io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.services;

import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.entities.Exam;
import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.repositories.ExamRepository;
import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.repositories.ExamResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExamService {
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;

    @Autowired
    public ExamService(ExamRepository examRepository, ExamResultRepository examResultRepository) {
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
    }

    @Transactional(readOnly = true)
    public List<Exam> findAllExams() {
        return examRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Exam> findExamById(Long id) {
        return examRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Exam> findExamsBySubject(String subject) {
        return examRepository.findBySubject(subject);
    }

    @Transactional(readOnly = true)
    public List<Exam> findExamsByDateRange(LocalDate startDate, LocalDate endDate) {
        return examRepository.findByExamDateBetween(startDate, endDate);
    }

    @Transactional
    public Exam saveExam(Exam exam) {
        return examRepository.save(exam);
    }

    @Transactional
    public void deleteExam(Long id) {
        examRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Double calculateAverageScoreForExam(Long examId) {
        return examResultRepository.calculateAverageScoreForExam(examId);
    }
}