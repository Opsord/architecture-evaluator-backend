package io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.controllers;

import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.entities.ExamResult;
import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.services.ExamResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-results")
public class ExamResultController {

    private final ExamResultService examResultService;

    @Autowired
    public ExamResultController(ExamResultService examResultService) {
        this.examResultService = examResultService;
    }

    @GetMapping
    public List<ExamResult> getAllResults() {
        return examResultService.findAllResults();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResult> getResultById(@PathVariable Long id) {
        return examResultService.findResultById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/exam/{examId}")
    public List<ExamResult> getResultsByExamId(@PathVariable Long examId) {
        return examResultService.findResultsByExamId(examId);
    }

    @GetMapping("/student/{studentId}")
    public List<ExamResult> getResultsByStudentId(@PathVariable Long studentId) {
        return examResultService.findResultsByStudentId(studentId);
    }

    @PutMapping("/{id}/grade")
    public ResponseEntity<ExamResult> gradeExamResult(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {

        Double score = Double.valueOf(payload.get("score").toString());
        String comments = payload.get("comments").toString();

        return examResultService.findResultById(id)
                .map(result -> ResponseEntity.ok(examResultService.gradeExamResult(id, score, comments)))
                .orElse(ResponseEntity.notFound().build());
    }
}