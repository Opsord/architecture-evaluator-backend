package io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.repositories;

import io.github.opsord.architecture_evaluator_backend.test_codes.exam_system.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}