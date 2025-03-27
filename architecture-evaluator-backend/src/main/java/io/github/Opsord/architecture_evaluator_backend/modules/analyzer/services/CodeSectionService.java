package io.github.Opsord.architecture_evaluator_backend.modules.analyzer.services;

import io.github.Opsord.architecture_evaluator_backend.modules.analyzer.repositories.CodeSectionRepository;
import org.springframework.stereotype.Service;

@Service
public class CodeSectionService {

    // Repository injection
    private final CodeSectionRepository codeSectionRepository;
    public CodeSectionService(CodeSectionRepository codeSectionRepository) {
        this.codeSectionRepository = codeSectionRepository;
    }


}