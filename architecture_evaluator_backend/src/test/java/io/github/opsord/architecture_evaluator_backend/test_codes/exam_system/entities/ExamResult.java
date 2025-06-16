package io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "grading_comments")
    private String gradingComments;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExamStatus status;

    public enum ExamStatus {
        PENDING, SUBMITTED, GRADED, CONTESTED
    }
}