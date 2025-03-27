package io.github.Opsord.architecture_evaluator_backend.modules.analyzer.repositories;

import io.github.Opsord.architecture_evaluator_backend.modules.analyzer.entities.CodeSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeSectionRepository extends JpaRepository<CodeSectionEntity, Long> {
  // Custom query methods here...
}