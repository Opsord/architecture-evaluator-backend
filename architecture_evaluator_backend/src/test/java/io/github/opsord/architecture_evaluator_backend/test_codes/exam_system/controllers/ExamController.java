package io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.controllers;

import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.entities.Exam;
import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.services.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    @Autowired
    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public List<Exam> getAllExams() {
        return examService.findAllExams();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        return examService.findExamById(id)
                .map(exam -> {
                    examService.deleteExam(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/average-score")
    public ResponseEntity<Double> getAverageScore(@PathVariable Long id) {
        Double averageScore = examService.calculateAverageScoreForExam(id);
        return averageScore != null ?
                ResponseEntity.ok(averageScore) :
                ResponseEntity.notFound().build();
    }
}